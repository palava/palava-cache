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

/**
 * A special {@link CacheService} which allows storing computations
 * in form of {@link Callable}s to allow waiting for long running computations
 * instead of performing them concurrently.
 *
 * @deprecated use {@link ComputingCacheRegion}
 * @since 2.4
 * @author Willi Schoenborn
 */
@Deprecated
public interface ComputingCacheService extends CacheService {

    /**
     * {@inheritDoc}
     *
     * <p>
     *   When a computation for the specified key is currently in progress,
     *   that computation will be kept running but the specified value will
     *   be used in favor of the already "old" value to be computed.
     *   All threads waiting on {@link #read(Serializable)} will be returned
     *   the given value.
     * </p>
     */
    @Override
    void store(Serializable key, Object value);
    
    /**
     * {@inheritDoc}
     *
     * <p>
     *   When a computation for the specified key is currently in progress,
     *   that computation will be kept running but the specified value will
     *   be used in favor of the already "old" value to be computed.
     *   All threads waiting on {@link #read(Serializable)} will be returned
     *   the given value.
     * </p>
     *
     * @throws NullPointerException if key or expiration is null
     */
    @Override
    void store(Serializable key, Object value, CacheExpiration expiration);

    /**
     * Stores a computation and in case of success it's result in 
     * this cache using the specified key.
     * 
     * @since 2.4
     * @param <V> the generic value type
     * @param key the key under which the result will be found
     * @param callable the computing callable
     * @return the computed value
     * @throws NullPointerException if key or callable is null
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if callable throws an {@link ExecutionException} it will be 
     *         propagated directly, any other exception will be wrapped
     */
    <V> V computeAndStore(Serializable key, Callable<? extends V> callable) 
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
     *   Threads waiting on {@link #read(Serializable)} for a computation to finish
     *   will receive the most current value.
     * </p>
     * 
     * @see #store(java.io.Serializable, Object, CacheExpiration)
     * @since 3.0
     * @param <V> the generic value type
     * @param key the key under which the result will be found
     * @param callable the computing callable
     * @param expiration the configuration for when and how the entry should expire
     * @return the computed value
     * @throws NullPointerException if key, callable or expiration is null
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if callable throws an {@link ExecutionException} it will be
     *         propagated directly, any other exception will be wrapped
     */
    <V> V computeAndStore(Serializable key, Callable<? extends V> callable, CacheExpiration expiration)
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
     * @since 2.4
     * @return an existing value or the new result of a finished computation
     * @throws RuntimeException any unchecked exception thrown by the computing unit
     */
    @Override
    <V> V read(Serializable key);

    /**
     * {@inheritDoc}
     * 
     * <p>
     *   When a computation is currently running, all threads
     *   waiting on {@link #read(Serializable)} for the computation to finish
     *   will immediately receive {@code null}. Any thread which
     *   is storing or computing a value for the given key
     *   will keep computing and receive it's own value, but the computed
     *   value won't persisted.
     * </p>
     * 
     * @since 2.4
     */
    @Override
    <V> V remove(Serializable key);
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     *   Performs {@link #remove(Serializable)} for every computation
     *   currently in progress. All pre-computed values will be removed.
     * </p>
     * 
     * @since 2.4
     */
    @Override
    void clear(); 
    
}
