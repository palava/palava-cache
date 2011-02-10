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
import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.MapMaker;
import com.google.common.util.concurrent.Callables;
import com.google.common.util.concurrent.ValueFuture;

/**
 * Default {@link ComputingCacheRegion} implementation.
 * 
 * @since 3.1
 * @author Willi Schoenborn
 * @param <K> generic key type
 * @param <V> generic value type
 */
@Beta
final class DefaultComputingCacheRegion<K extends Serializable, V> extends ForwardingCacheRegion<K, V> implements
        ComputingCacheRegion<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultComputingCacheRegion.class);

    private final CacheRegion<K, V> region;
    private final ConcurrentMap<K, Queue<ValueFuture<V>>> computations;

    DefaultComputingCacheRegion(CacheRegion<K, V> region) {
        this.region = Preconditions.checkNotNull(region, "Region");
        this.computations = new MapMaker().weakValues().makeComputingMap(new QueueProducer());
    }

    /**
     * A function which creates {@link Queue}s used to store pending
     * computations.
     * 
     * @since 3.1
     * @author Willi Schoenborn
     */
    private final class QueueProducer implements Function<K, Queue<ValueFuture<V>>> {

        @Override
        public Queue<ValueFuture<V>> apply(K from) {
            return new ConcurrentLinkedQueue<ValueFuture<V>>();
        }

    }

    @Override
    protected CacheRegion<K, V> delegate() {
        return region;
    }

    @Override
    public V put(K key, V value) {
        return put(key, value, CacheExpirations.ETERNAL);
    }

    @Override
    public V put(K key, V value, CacheExpiration expiration) {
        Preconditions.checkNotNull(value, "Value");
        try {
            return computeAndPut(key, Callables.returning(value), expiration);
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getCause());
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return putIfAbsent(key, value, CacheExpirations.ETERNAL);
    }

    @Override
    public V putIfAbsent(K key, V value, CacheExpiration expiration) {
        Preconditions.checkNotNull(value, "Value");
        try {
            return computeAndPutIfAbsent(key, Callables.returning(value), expiration);
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getCause());
        }
    }

    @Override
    public V computeAndPut(K key, Callable<? extends V> computation) throws ExecutionException {
        return computeAndPut(key, computation, CacheExpirations.ETERNAL);
    }

    @Override
    public V computeAndPut(K key, Callable<? extends V> computation, CacheExpiration expiration)
        throws ExecutionException {

        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkNotNull(computation, "Computation");
        Preconditions.checkNotNull(expiration, "Expiration");

        final Collection<ValueFuture<V>> futures = computations.get(key);
        final ValueFuture<V> future = ValueFuture.create();

        // it's important to do this asap, to allow other callers
        // to wait on read instead of starting the same computation
        futures.add(future);

        try {
            LOG.trace("Computing value for key '{}' using {}", key, computation);
            final V value = computation.call();

            if (future.isCancelled()) {
                LOG.warn("{} has been cancelled", future);
            } else if (future.isDone()) {
                LOG.trace("Another computation was faster and already computed a value for key '{}'", key);
            } else {
                LOG.trace("Computed value '{}' for key '{}'", value, key);
                future.set(value);
                for (ValueFuture<V> other : futures) {
                    if (other == future) {
                        // every computation after this is newer
                        break;
                    } else if (other.isDone()) {
                        LOG.trace("Skipping finished computation: {}", other);
                        continue;
                    } else {
                        // make older and still running computations use my
                        // computed value
                        LOG.trace("Setting faster computed value '{}' on {}", value, other);
                        other.set(value);
                    }
                }
                LOG.trace("Storing '{}' to '{}' in underlying store", key, value);
                region.put(key, value, expiration);
            }

            final V returned = future.get();
            if (returned == null) {
                LOG.trace("Key '{}' has been removed during computation, returning computed value '{}'", key, value);
                return value;
            } else {
                LOG.trace("Returning value '{}' for key '{}'", returned, key);
                return returned;
            }
        } catch (ExecutionException e) {
            LOG.warn("Exception during {}.call()", computation);
            future.setException(e.getCause());
            throw e;
            /* CHECKSTYLE:OFF */
        } catch (Exception e) {
            /* CHECKSTYLE:ON */
            LOG.warn("Exception during {}.call()", computation);
            future.setException(e);
            throw new ExecutionException(e);
        } finally {
            futures.remove(future);
        }

    }

    @Override
    public V computeAndPutIfAbsent(K key, Callable<? extends V> computation) throws ExecutionException {
        return computeAndPutIfAbsent(key, computation, CacheExpirations.ETERNAL);
    }

    @Override
    public V computeAndPutIfAbsent(K key, Callable<? extends V> computation, CacheExpiration expiration)
        throws ExecutionException {
        
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkNotNull(computation, "Computation");
        Preconditions.checkNotNull(expiration, "Expiration");

        final V cached = get(key);

        if (cached == null) {
            LOG.trace("No pre-computed value for key '{}' exists, computing...", key);
            return computeAndPut(key, computation, expiration);
        } else {
            LOG.trace("Read pre-computed value for key '{}'", key);
            return cached;
        }
    }

    @Override
    public V get(Object key) {
        Preconditions.checkNotNull(key, "Key");

        final V cached = region.get(key);

        if (cached == null) {
            LOG.trace("No pre-computed value for key '{}' exists, looking for computations", key);
            final Future<V> future = computations.get(key).peek();

            if (future == null) {
                LOG.trace("No computation for key '{}', returning null", key);
                return null;
            } else {
                try {
                    LOG.trace("Waiting for {} to compute value for key '{}'", future, key);
                    return future.get();
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
            LOG.trace("Read pre-computed value for key '{}' from underlying cache", key);
            return cached;
        }
    }

    @Override
    public V replace(K key, V value) {
        Preconditions.checkNotNull(key, "Key");
        return containsKey(key) ? put(key, value) : null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Preconditions.checkNotNull(key, "Key");

        final V cached = region.get(key);
        if (cached == null) {
            return false;
        } else if (cached.equals(oldValue)) {
            put(key, newValue);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public V remove(Object key) {
        Preconditions.checkNotNull(key, "Key");

        final Queue<ValueFuture<V>> futures = computations.get(key);

        if (futures.isEmpty()) {
            LOG.trace("Removing key '{}' from underlying cache", key);
            // no running computation, the easy part
            return region.remove(key);
        } else {
            LOG.trace("Forcing all running computations for key '{}' to return null", key);
            while (true) {
                // get and remove in one shot
                final ValueFuture<V> future = futures.poll();
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
            return region.remove(key);
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        final V cached = region.get(key);
        if (cached == null) {
            return false;
        } else if (cached.equals(value)) {
            remove(key);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeIf(Predicate<? super K> predicate) {
        boolean removedAny = false;
        for (K key : keySet()) {
            if (predicate.apply(key)) {
                remove(key);
                removedAny = true;
            }
        }
        return removedAny;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(entrySet());
    }

    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(keySet());
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableCollection(values());
    }

    @Override
    public void clear() {
        // force every pending and running computation to return null on waiting
        // callers
        for (K key : computations.keySet()) {
            remove(key);
        }
        region.clear();
    }

}
