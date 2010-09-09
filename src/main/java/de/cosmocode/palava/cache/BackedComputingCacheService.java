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
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ComputationException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.inject.Inject;

/**
 * A {@link ComputingCacheService} which is backed by an ordinary {@link CacheService}.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
final class BackedComputingCacheService implements ComputingCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(BackedComputingCacheService.class);

    private final ConcurrentMap<Serializable, Future<?>> futures = Maps.newConcurrentMap();
    
    private final CacheService service;
    
    private CancelStrategy strategy = Cancelling.WAIT;
    
    @Inject
    public BackedComputingCacheService(CacheService service) {
        this.service = Preconditions.checkNotNull(service, "Service");
    }

    private void cancelIfRunning(Serializable key) {
        final Future<?> future = futures.get(key);
        if (future == null) {
            // the strategy could handle null, but we don't want to log null
            return;
        } else {
            LOG.debug("Cancelling running computation: {}", future);
            strategy.cancel(future);
        }
    }

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
        cancelIfRunning(key);
        service.store(key, value, maxAge, maxAgeUnit);
    }

    @Override
    public void store(Serializable key, Callable<?> callable) {
        store(key, callable, getMaxAge(), TimeUnit.SECONDS);
    }
    
    @Override
    public void store(Serializable key, Callable<?> callable, long maxAge, TimeUnit maxAgeUnit) {
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkNotNull(callable, "Callable");
        Preconditions.checkArgument(maxAge >= 0, "Max age must not be negative");
        Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
        
        cancelIfRunning(key);
        
        final RunnableFuture<Object> future = new RemovingFuture(key, callable, maxAge, maxAgeUnit);
        
        // as soon as the future is in there, other clients can request and wait for the computation
        // to finish
        futures.put(key, future);
        
        // make sure it's placed in the map and accessible before we actually start computing
        future.run();
    }
    
    /**
     * A custom {@link RunnableFuture} which removes itself from {@link BackedComputingCacheService#futures}
     * as soon as the underlying {@link Callable} finishes computation, either successfully or
     * by throwing an exception.
     *
     * @since 2.4
     * @author Willi Schoenborn
     */
    private final class RemovingFuture extends AbstractFuture<Object> implements RunnableFuture<Object> {
        
        private final Serializable key;
        private final Callable<?> callable;
        private final long maxAge;
        private final TimeUnit maxAgeUnit;
        
        public RemovingFuture(Serializable key, Callable<?> callable, long maxAge, TimeUnit maxAgeUnit) {
            this.key = key;
            this.callable = callable;
            this.maxAge = maxAge;
            this.maxAgeUnit = maxAgeUnit;
        }

        @Override
        public void run() {
            if (isCancelled())  {
                LOG.debug("{} has been cancelled before running", this);
            } else {
                try {
                    LOG.trace("Computing value using {}", callable);
                    final Object value = callable.call();
                    LOG.trace("Computed value {}", value);
                    service.store(key, value, maxAge, maxAgeUnit);
                    set(value);
                    /* CHECKSTYLE:OFF */
                } catch (Exception e) {
                    /* CHECKSTYLE:ON */
                    setException(e);
                    throw new ComputationException(e);
                } finally {
                    futures.remove(key);
                }
            }
        }
        
        @Override
        public String toString() {
            // TODO define better toString
            return key + " => " + callable;
        }
        
    }
    
    @Override
    public <T> T read(Serializable key) throws CancellationException {
        Preconditions.checkNotNull(key, "Key");
        final Future<?> future = futures.get(key);
        if (future == null) {
            LOG.trace("No running computation for key {}", key);
            return service.<T>read(key);
        } else {
            try {
                LOG.debug("Waiting for {} to finish computation", future);
                @SuppressWarnings("unchecked")
                final T value = (T) future.get();
                return value;
            } catch (InterruptedException e) {
                return strategy.<T>handle(e);
            } catch (CancellationException e) {
                LOG.trace("{} has been cancelled during read", future);
                throw e;
            } catch (ExecutionException e) {
                throw Throwables.propagate(e.getCause());
            }
        }
    }

    @Override
    public <T> T remove(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        cancelIfRunning(key);
        return service.<T>remove(key);
    }

    @Override
    public void clear() {
        final Collection<Future<?>> toBeCancelled = Lists.newArrayList(futures.values());
        futures.values().removeAll(toBeCancelled);
        
        for (Future<?> future : toBeCancelled) {
            strategy.cancel(future);
        }
    }

}
