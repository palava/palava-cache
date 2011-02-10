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
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import com.google.common.annotations.Beta;

/**
 * A computing {@link CacheRegion} which allows blocking on running computations
 * to minimize the workload in case of high frequent cache misses for the same key.
 *
 * @since 3.1
 * @author Willi Schoenborn
 * @param <K> the generic key type
 * @param <V> the generic value type
 */
@Beta
public interface ComputingCacheRegion<K extends Serializable, V> extends CacheRegion<K, V> {

    /**
     * {@inheritDoc}
     *
     * <p>
     *   When a computation for the specified key is currently in progress,
     *   that computation will be kept running but the specified value will
     *   be used in favor of the already "old" value to be computed.
     *   All threads waiting on {@link #get(Object)} will be returned
     *   the given value.
     * </p>
     */
    @Override
    V put(K key, V value);

    /**
     * {@inheritDoc}
     *
     * <p>
     *   When a computation for the specified key is currently in progress,
     *   that computation will be kept running but the specified value will
     *   be used in favor of the already "old" value to be computed.
     *   All threads waiting on {@link #get(Object)} will be returned
     *   the given value.
     * </p>
     *
     * @throws NullPointerException if key or expiration is null
     */
    @Override
    V put(K key, V value, CacheExpiration expiration);

    @Override
    V putIfAbsent(K key, V value);

    /**
     * Stores a computation and in case of success it's result in 
     * this cache using the specified key.
     * 
     * @since 3.1
     * @param key the key under which the result will be found
     * @param computation the computing callable
     * @return the computed value
     * @throws NullPointerException if key or computation is null
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if computation throws an {@link ExecutionException} it will be 
     *         propagated directly, any other exception will be wrapped
     */
    V computeAndPut(K key, Callable<? extends V> computation) throws CancellationException, ExecutionException;

    /**
     * Stores a computation and in case of success it's result in 
     * this cache using the specified key if and only if no mapping
     * for the given key exists.
     * 
     * @since 3.1
     * @param key the key under which the result will be found
     * @param computation the computing callable
     * @return the computed value
     * @throws NullPointerException if key or computation is null
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if computation throws an {@link ExecutionException} it will be 
     *         propagated directly, any other exception will be wrapped
     */
    V computeAndPutIfAbsent(K key, Callable<? extends V> computation) 
        throws CancellationException, ExecutionException;
    
    /**
     * Stores a computation and in case of success it's result in 
     * this cache using the specified key.
     * 
     * <p>
     *   When a computation for the given key is currently in progress
     *   both computations will run concurrently and every thread will receive
     *   at least it's "own" computed value or a newer one. A newer value is the result
     *   of a computation that started after the beginning of the old value's computation.
     *   In other words, if another thread begins to store or compute a value for 
     *   the same key after this computation began, that value will be stored in this cache and
     *   in case any later computation for the same key finishes before this computation,
     *   the newest will be returned.
     * </p>
     * 
     * <p>
     *   Threads waiting on {@link #get(Object)} for a computation to finish
     *   will receive the most current value.
     * </p>
     * 
     * @see #put(Serializable, Callable, CacheExpiration)
     * @since 3.0
     * @param key the key under which the result will be found
     * @param computation the computing callable
     * @param expiration the configuration for when and how the entry should expire
     * @return the computed value
     * @throws NullPointerException if key, computation or expiration is null
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if computation throws an {@link ExecutionException} it will be
     *         propagated directly, any other exception will be wrapped
     */
    V computeAndPut(K key, Callable<? extends V> computation, CacheExpiration expiration)
        throws CancellationException, ExecutionException;

    /**
     * Stores a computation and in case of success it's result in 
     * this cache using the specified key.
     * 
     * 
     * @see #computeAndPut(Serializable, Callable, CacheExpiration)
     * @since 3.0
     * @param key the key under which the result will be found
     * @param computation the computing callable
     * @param expiration the configuration for when and how the entry should expire
     * @return the computed value
     * @throws NullPointerException if key, computation or expiration is null
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if computation throws an {@link ExecutionException} it will be
     *         propagated directly, any other exception will be wrapped
     */
    V computeAndPutIfAbsent(K key, Callable<? extends V> computation, CacheExpiration expiration)
        throws CancellationException, ExecutionException;

    /**
     * {@inheritDoc}
     * 
     * <p>
     *   When a computation is currently running, this method will block
     *   until it's result is computed or cancelled. In case the 
     *   computation is cancelled this method will return {@code null}.
     * </p>
     * 
     * <p>
     *   In case a pre a pre-computed (and old) value exists, it is returned
     *   even if a computation for the given key is currently in progress.
     * </p>
     * 
     * <p>
     *   This leads to the following walkthrough:
     *   <ol>
     *     <li>if a pre-computed value exists, return it</li>
     *     <li>if a computation is in progress, wait for it and return the result</li>
     *     <li>return null</li>
     *   </ol>
     * </p>
     * 
     * @since 3.1
     * @return an existing value or the new result of a finished computation
     * @throws RuntimeException any unchecked exception thrown by the computing unit
     */
    @Override
    V get(Object key);

    @Override
    V replace(K key, V value);

    @Override
    boolean replace(K key, V oldValue, V newValue);

    /**
     * {@inheritDoc}
     * 
     * <p>
     *   When a computation is currently running, all threads
     *   waiting on {@link #get(Object)} for the computation to finish
     *   will immediately receive {@code null}. Any thread which
     *   is storing or computing a value for the given key
     *   will keep computing and receive it's own value, but the computed
     *   value won't persisted.
     * </p>
     * 
     * @since 3.1
     */
    @Override
    V remove(Object key);

    /**
     * {@inheritDoc}
     * 
     * @since 3.1
     * @see #remove(Object)
     */
    @Override
    boolean remove(Object key, Object value);

    /**
     * {@inheritDoc}
     * 
     * <p>
     *   Performs {@link #remove(Serializable)} for every computation
     *   currently in progress. All pre-computed values will be removed.
     * </p>
     * 
     * @since 3.1
     */
    @Override
    void clear();
    
}
