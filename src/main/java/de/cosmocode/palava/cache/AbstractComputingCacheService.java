/**
 * Copyright 2010 CosmoCode GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cosmocode.palava.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.MapMaker;
import com.google.common.util.concurrent.Callables;
import com.google.common.util.concurrent.ValueFuture;

/**
 * Abstract base implementation for {@link ComputingCacheService}.
 *
 * @since 3.0
 * @author Willi Schoenborn
 */
public abstract class AbstractComputingCacheService extends AbstractCacheService implements ComputingCacheService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final ConcurrentMap<Serializable, Queue<ValueFuture<Object>>> computations;
    
    public AbstractComputingCacheService() {
        // weak value "should" remove empty queues from the map
        final MapMaker maker = new MapMaker().weakValues();
        this.computations = maker.makeComputingMap(new Function<Serializable, Queue<ValueFuture<Object>>>() {
            
            @Override
            public Queue<ValueFuture<Object>> apply(Serializable from) {
                return new ConcurrentLinkedQueue<ValueFuture<Object>>();
            }
            
        });
    }

    @Override
    public final void store(Serializable key, Object value, CacheExpiration expiration) {
        try {
            computeAndStore(key, Callables.returning(value), expiration);
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getCause());
        }
    }
    
    @Override
    public final void store(Serializable key, Object value) {
        super.store(key, value);
    }

    @Override
    public final <V> V computeAndStore(Serializable key, Callable<? extends V> callable) throws ExecutionException {
        return computeAndStore(key, callable, CacheExpirations.ETERNAL);
    }
    
    @Override
    public final <V> V computeAndStore(Serializable key, Callable<? extends V> callable, CacheExpiration expiration)
        throws CancellationException, ExecutionException {
        
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkNotNull(callable, "Callable");
        Preconditions.checkNotNull(expiration, "Expiration");

        final Collection<ValueFuture<Object>> futures = computations.get(key);
        final ValueFuture<Object> future = ValueFuture.create();

        // it's important to do this asap, to allow other callers
        // to wait on read instead of starting the same computation
        futures.add(future);
        
        try {
            log.trace("Computing value for key '{}' using {}", key, callable);
            final V value = callable.call();
            
            if (future.isCancelled()) {
                log.warn("{} has been cancelled", future);
            } else if (future.isDone()) {
                log.trace("Another computation was faster and already computed a value for key '{}'", key);
            } else {
                log.trace("Computed value '{}' for key '{}'", value, key);
                future.set(value);
                for (ValueFuture<Object> other : futures) {
                    if (other == future) {
                        // every computation after this is newer
                        break;
                    } else if (other.isDone()) {
                        log.trace("Skipping finished computation: {}", other);
                        continue;
                    } else {
                        // make older and still running computations use my computed value
                        log.trace("Setting faster computed value '{}' on {}", value, other);
                        other.set(value);
                    }
                }
                log.trace("Storing '{}' to '{}' in underlying store", key, value);
                doStore(key, value, expiration);
            }
            
            final V returned = cast(future.get());
            if (returned == null) {
                log.trace("Key '{}' has been removed during computation, returning value '{}'", key, value);
                return value;
            } else {
                log.trace("Returning value '{}' for key '{}'", returned, key);
                return returned;
            }
        } catch (ExecutionException e) {
            log.warn("Exception during {}.call()", callable);
            future.setException(e.getCause());
            throw e;
        /* CHECKSTYLE:OFF */
        } catch (Exception e) {
        /* CHECKSTYLE:ON */
            log.warn("Exception during {}.call()", callable);
            future.setException(e);
            throw new ExecutionException(e);
        } finally {
            futures.remove(future);
        }
    }
    
    /**
     * Hook for sub classes to provide the behaviour for {@link #store(Serializable, Object, CacheExpiration)}.
     *
     * @since 3.0
     * @see #store(Serializable, Object, CacheExpiration)
     * @param key the cache key
     * @param value the cache value
     * @param expiration the cache expiration
     */
    protected abstract void doStore(Serializable key, Object value, CacheExpiration expiration);
    
    @Override
    public final <V> V read(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        
        final V cached = doRead(key);
        
        if (cached == null) {
            log.trace("No pre-computed value for key '{}' exists, looking for computations", key);
            final Future<Object> future = computations.get(key).peek();
            
            if (future == null) {
                log.trace("No computation for key '{}', returning null", key);
                return null;
            } else {
                try {
                    log.trace("Waiting for {} to compute value for key '{}'", future, key);
                    return cast(future.get());
                } catch (CancellationException e) {
                    log.debug("Computation for {} has been cancelled during read", key);
                    return null;
                } catch (InterruptedException e) {
                    log.warn("Thread has been interrupted during wait for key '{}'", key);
                    return null;
                } catch (ExecutionException e) {
                    log.warn("Exception while waiting for " + future, e.getCause());
                    throw Throwables.propagate(e.getCause());
                }
            }
        } else {
            log.trace("Reading pre-computed value for key '{}' from underlying cache", key);
            return cached;
        }
    }
    
    /**
     * Hook for sub classes to provide the behaviour for {@link #read(Serializable)}.
     *
     * @since 3.0
     * @see #read(Serializable)
     * @param <V> the generic value type
     * @param key the cache key
     * @return the cached value or null, if no such value exists
     */
    protected abstract <V> V doRead(Serializable key);
    
    @SuppressWarnings("unchecked")
    private <V> V cast(Object value) {
        return (V) value;
    }

    @Override
    public final <V> V remove(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final Queue<ValueFuture<Object>> futures = computations.get(key);
        
        if (futures.isEmpty()) {
            log.trace("Removing key '{}' from underlying cache", key);
            // no running computation, the easy part
            return doRemove(key);
        } else {
            log.trace("Forcing all running computations for key '{}' to return null", key);
            while (true) {
                // get and remove in one shot
                final ValueFuture<Object> future = futures.poll();
                if (future == null) {
                    // no running computation left
                    // weak values should take care of empty queue
                    break;
                } else if (future.isDone()) {
                    log.trace("{} finished during remove", future);
                } else {
                    // return null to callers waiting on Future#get()
                    future.set(null);
                }
            }
            // make sure the underlying cache removes any pre-computed value
            return doRemove(key);
        }
    }
    
    /**
     * Hook for sub classes to provide the behaviour for {@link #remove(Serializable)}.
     *
     * @since 3.0
     * @see #remove(Serializable)
     * @param <V> the generic value type
     * @param key the cache key
     * @return the value which has been removed or null if no value for the given key exists
     */
    protected abstract <V> V doRemove(Serializable key);

    @Override
    public final void clear() {
        // force every pending and running computation to return null on waiting callers
        for (Serializable key : computations.keySet()) {
            remove(key);
        }
        
        // force the underlying cache to remove all pre-computed values
        doClear();
    }

    /**
     * Hook for sub classes to provide the behaviour for {@link #clear()}.
     *
     * @since 1.3
     * @see #clear()
     */
    protected abstract void doClear();
    
}
