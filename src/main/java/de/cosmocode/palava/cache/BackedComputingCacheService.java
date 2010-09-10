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
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
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

    private final ConcurrentMap<Serializable, List<ValueFuture<Object>>> computations;
    
    @Inject
    public BackedComputingCacheService(CacheService service) {
        this.service = Preconditions.checkNotNull(service, "Service");
        
        // soft values should remove obsolete lists
        final MapMaker maker = new MapMaker().softValues();
        this.computations = maker.makeComputingMap(new Function<Serializable, List<ValueFuture<Object>>>() {
            
            @Override
            public List<ValueFuture<Object>> apply(Serializable from) {
                return new CopyOnWriteArrayList<ValueFuture<Object>>();
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
            LOG.warn("Exception during storing constant value {} for key {}", value, key);
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

        final List<ValueFuture<Object>> futures = computations.get(key);
        final ValueFuture<Object> future = ValueFuture.create();
        
        futures.add(future);
        
        if (future.isCancelled()) {
            LOG.trace("{} has been cancelled", future);
        } else if (future.isDone()) {
            LOG.trace("{} has already finished", future);
        } else {
            try {
                final V computed = callable.call();
                if (future.isDone()) {
                    LOG.debug("Another computation was faster and already computed a value for {}", key);
                } else {
                    future.set(computed);
                    for (ValueFuture<Object> other : futures) {
                        if (other == future) {
                            // every computation after this is newer
                            break;
                        } else if (other.isDone()) {
                            // skip finished computations
                            continue;
                        } else {
                            // make older and running computations use my computed value
                            other.set(computed);
                        }
                    }
                }
            /* CHECKSTYLE:OFF */
            } catch (Exception e) {
            /* CHECKSTYLE:ON */
                LOG.warn("Exception during {}.call()", callable);
                future.setException(e);
            } finally {
                futures.remove(future);
            }
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
        final List<ValueFuture<Object>> futures = computations.get(key);
        
        if (futures.isEmpty()) {
            // no running computation, the easy part
            return service.<T>read(key);
        } else {
            final ListIterator<ValueFuture<Object>> iterator = futures.listIterator(futures.size());
            try {
                final ValueFuture<Object> last = iterator.previous();
                LOG.trace("Waiting for {} to compute value for key {}", last, key);
                @SuppressWarnings("unchecked")
                final T t = (T) last.get();
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
        final List<ValueFuture<Object>> futures = computations.get(key);
        
        if (futures.isEmpty()) {
            LOG.trace("Removing {} from underlying cache", key);
            // no running computation, the easy part
            return service.<T>remove(key);
        } else {
            LOG.trace("Forcing all running computations for {} to return null", key);
            for (ValueFuture<Object> future : futures) {
                // forces running computations to return null on Future#get()
                future.set(null);
                // futures in concurrent, so this should work
                futures.remove(future);
            }
            // make sure the underlying cache removes any pre-computed value
            return service.<T>remove(key);
        }
    }

    @Override
    public void clear() {
        // cancel all pending and running computations
        final Set<Serializable> keys = Sets.newHashSet(computations.keySet());
        
        for (Serializable key : keys) {
            remove(key);
        }
        
        // force the underlying cache to remove all precomputed values
        service.clear();
    }

}
