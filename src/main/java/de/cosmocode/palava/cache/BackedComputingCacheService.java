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
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.MapMaker;
import com.google.common.util.concurrent.Callables;
import com.google.common.util.concurrent.ValueFuture;
import com.google.inject.Inject;

/**
 * A {@link ComputingCacheService} which is backed by an ordinary {@link CacheService}.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
final class BackedComputingCacheService implements ComputingCacheService {

    static final Logger LOG = LoggerFactory.getLogger(BackedComputingCacheService.class);
    
    private final CacheService service;

    private final ConcurrentMap<Serializable, Queue<ValueFuture<Object>>> computations;
    
    @Inject
    public BackedComputingCacheService(@Backing CacheService service) {
        this.service = Preconditions.checkNotNull(service, "Service");
        
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
    public void store(Serializable key, Object value) {
        store(key, value, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public void store(Serializable key, Object value, long maxAge, TimeUnit maxAgeUnit) {
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkArgument(maxAge >= 0, "Max age must not be negative");
        Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
        
        try {
            computeAndStore(key, Callables.returning(value), maxAge, maxAgeUnit);
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getCause());
        }
    }

    @Override
    public void store(Serializable key, Object value, CacheExpiration expiration) {
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkNotNull(expiration, "Expiration");

        try {
            computeAndStore(key, Callables.returning(value), expiration);
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getCause());
        }
    }

    @Override
    public <V> V computeAndStore(Serializable key, Callable<? extends V> callable) throws ExecutionException {
        return computeAndStore(key, callable, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public <V> V computeAndStore(Serializable key, final Callable<? extends V> callable,
            long maxAge, TimeUnit maxAgeUnit) throws ExecutionException {

        Preconditions.checkArgument(maxAge >= 0, "Max age must not be negative");
        Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
        return computeAndStore(key, callable, new CacheExpiration(maxAge, maxAgeUnit));
    }

    @Override
    public <V> V computeAndStore(Serializable key, Callable<? extends V> callable,
            CacheExpiration expiration) throws ExecutionException {
        
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkNotNull(callable, "Callable");
        Preconditions.checkNotNull(expiration, "Expiration");

        final Collection<ValueFuture<Object>> futures = computations.get(key);
        final ValueFuture<Object> future = ValueFuture.create();

        // it's important to do this asap, to allow other callers
        // to wait on read instead of starting the same computation
        futures.add(future);
        
        try {
            LOG.trace("Computing value for key '{}' using {}", key, callable);
            final V value = callable.call();
            
            if (future.isCancelled()) {
                LOG.warn("{} has been cancelled", future);
            } else if (future.isDone()) {
                LOG.trace("Another computation was faster and already computed a value for key '{}'", key);
            } else {
                LOG.trace("Computed value '{}' for key '{}'", value, key);
                future.set(value);
                for (ValueFuture<Object> other : futures) {
                    if (other == future) {
                        // every computation after this is newer
                        break;
                    } else if (other.isDone()) {
                        LOG.trace("Skipping finished computation: {}", other);
                        continue;
                    } else {
                        // make older and still running computations use my computed value
                        LOG.trace("Setting faster computed value '{}' on {}", value, other);
                        other.set(value);
                    }
                }
                LOG.trace("Storing '{}' to '{}' in underlying store", key, value);
                service.store(key, value, expiration);
            }
            
            final V returned = this.<V>cast(future.get());
            if (returned == null) {
                LOG.trace("Key '{}' has been removed during computation, returning value '{}'", key, value);
                return value;
            } else {
                LOG.trace("Returning value '{}' for key '{}'", returned, key);
                return returned;
            }
        } catch (ExecutionException e) {
            LOG.warn("Exception during {}.call()", callable);
            future.setException(e.getCause());
            throw e;
        /* CHECKSTYLE:OFF */
        } catch (Exception e) {
        /* CHECKSTYLE:ON */
            LOG.warn("Exception during {}.call()", callable);
            future.setException(e);
            throw new ExecutionException(e);
        } finally {
            futures.remove(future);
        }
    }
    
    @Override
    public <V> V read(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        
        final V cached = service.<V>read(key);
        
        if (cached == null) {
            LOG.trace("No pre-computed value for key '{}' exists, looking for computations", key);
            final Future<Object> future = computations.get(key).peek();
            
            if (future == null) {
                LOG.trace("No computation for key '{}', returning null", key);
                return null;
            } else {
                try {
                    LOG.trace("Waiting for {} to compute value for key '{}'", future, key);
                    return this.<V>cast(future.get());
                } catch (CancellationException e) {
                    LOG.debug("Computation for {} has been cancelled during read", key);
                    return null;
                } catch (InterruptedException e) {
                    LOG.warn("Thread has been interrupted during wait for key '{}'", key);
                    return null;
                } catch (ExecutionException e) {
                    LOG.warn("Exception while waiting for " + future, e.getCause());
                    throw Throwables.propagate(e.getCause());
                }
            }
        } else {
            LOG.trace("Reading pre-computed value for key '{}' from underlying cache", key);
            return cached;
        }
    }
    
    @SuppressWarnings("unchecked")
    private <V> V cast(Object value) {
        return (V) value;
    }

    @Override
    public <V> V remove(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final Queue<ValueFuture<Object>> futures = computations.get(key);
        
        if (futures.isEmpty()) {
            LOG.trace("Removing key '{}' from underlying cache", key);
            // no running computation, the easy part
            return service.<V>remove(key);
        } else {
            LOG.trace("Forcing all running computations for key '{}' to return null", key);
            while (true) {
                // get and remove in one shot
                final ValueFuture<Object> future = futures.poll();
                if (future == null) {
                    // no running computation left
                    // weak values should take care of empty queue
                    break;
                } else if (future.isDone()) {
                    LOG.trace("{} finished during remove", future);
                } else {
                    // return null to callers waiting on Future#get()
                    future.set(null);
                }
            }
            // make sure the underlying cache removes any pre-computed value
            return service.<V>remove(key);
        }
    }

    @Override
    public void clear() {
        // force every pending and running computation to return null on waiting callers
        for (Serializable key : computations.keySet()) {
            remove(key);
        }
        
        // force the underlying cache to remove all pre-computed values
        service.clear();
    }

}
