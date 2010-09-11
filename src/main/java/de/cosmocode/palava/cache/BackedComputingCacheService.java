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
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
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
    public BackedComputingCacheService(CacheService service) {
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
    public long getMaxAge() {
        return getMaxAge(TimeUnit.SECONDS);
    }

    @Override
    public long getMaxAge(TimeUnit unit) {
        Preconditions.checkNotNull(unit, "Unit");
        return service.getMaxAge(unit);
    }

    @Override
    public void setMaxAge(long maxAgeSeconds) {
        setMaxAge(maxAgeSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void setMaxAge(long maxAge, TimeUnit maxAgeUnit) {
        Preconditions.checkArgument(maxAge >= 0, "Max age must not be negative");
        Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
        service.setMaxAge(maxAge, maxAgeUnit);
    }

    @Override
    public void store(Serializable key, Object value) {
        store(key, value, getMaxAge(), TimeUnit.SECONDS);
    }

    @Override
    public void store(Serializable key, Object value, long maxAge, TimeUnit maxAgeUnit) {
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkArgument(maxAge >= 0, "Max age must not be negative");
        Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
        
        try {
            computeAndStore(key, Callables.returning(value), maxAge, maxAgeUnit);
        } catch (ExecutionException e) {
        	throw new IllegalArgumentException(e);
        }
    }

    @Override
    public <V> V computeAndStore(Serializable key, Callable<? extends V> callable) throws ExecutionException {
        return computeAndStore(key, callable, getMaxAge(), TimeUnit.SECONDS);
    }
    
    @Override
    public <V> V computeAndStore(Serializable key, final Callable<? extends V> callable, 
            long maxAge, TimeUnit maxAgeUnit) throws ExecutionException {
        
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkNotNull(callable, "Callable");
        Preconditions.checkArgument(maxAge >= 0, "Max age must not be negative");
        Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");

        final Collection<ValueFuture<Object>> futures = computations.get(key);
        final ValueFuture<Object> future = ValueFuture.create();
        
        // it's important to do this asap, to allow other callers
        // to wait on read instead of starting the same computation
        futures.add(future);
        
        try {
            final V value = callable.call();
            if (future.isCancelled()) {
                LOG.warn("{} has been cancelled", future);
            } else if (future.isDone()) {
                LOG.debug("Another computation was faster and already computed a value for {}", key);
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
                        // make older and running computations use my computed value
                        LOG.trace("Setting faster computed value '{}' on {}", value, other);
                        other.set(value);
                    }
                }
                LOG.trace("Storing '{}' to '{}' in underlying store", key, value);
                service.store(key, value, maxAge, maxAgeUnit);
            }
        /* CHECKSTYLE:OFF */
        } catch (Exception e) {
        /* CHECKSTYLE:ON */
            LOG.warn("Exception during {}.call()", callable);
            future.setException(e);
        } finally {
            futures.remove(future);
        }
        
        try {
            @SuppressWarnings("unchecked")
            final V value = (V) future.get();
            return value;
        } catch (InterruptedException e) {
            throw new ExecutionException(e);
        }
    }
    
    @Override
    public <T> T read(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final Collection<ValueFuture<Object>> futures = computations.get(key);
        
        if (futures.isEmpty()) {
            LOG.trace("Reading pre-computed value for {} from underlying cache", key);
            // no running computation, the easy part
            return service.<T>read(key);
        } else {
            final Iterator<ValueFuture<Object>> iterator = futures.iterator();
            try {
                final ValueFuture<Object> future = iterator.next();
                LOG.trace("Waiting for {} to compute value for key {}", future, key);
                @SuppressWarnings("unchecked")
                final T t = (T) future.get();
                return t;
            } catch (CancellationException e) {
                LOG.debug("Computation for {} has been cancelled during read", key);
                return null;
            } catch (InterruptedException e) {
                LOG.warn("Thread has been interrupted during wait for {}", key);
                return null;
            } catch (ExecutionException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    @Override
    public <T> T remove(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final Queue<ValueFuture<Object>> futures = computations.get(key);
        
        if (futures.isEmpty()) {
            LOG.trace("Removing {} from underlying cache", key);
            // no running computation, the easy part
            return service.<T>remove(key);
        } else {
            LOG.trace("Forcing all running computations for {} to return null", key);
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
            return service.<T>remove(key);
        }
    }

    @Override
    public void clear() {
        // force every pending and running computation to return null on waiting callers
        final Set<Serializable> keys = Sets.newHashSet(computations.keySet());
        
        for (Serializable key : keys) {
            remove(key);
        }
        
        // force the underlying cache to remove all pre-computed values
        service.clear();
    }

}