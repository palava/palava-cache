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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A special {@link CacheService} which allows storing computations
 * in form of {@link Callable}s to allow waiting for long running computations
 * instead of performing them concurrently.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
public interface ComputingCacheService extends CacheService {

    /**
     * {@inheritDoc}
     * 
     * Cancels the current running execution for the
     * specified key if there is any.
     */
    @Override
    void store(Serializable key, Object value);
    
    /**
     * {@inheritDoc}
     * 
     * Cancels the current running execution for the
     * specified key if there is any.
     */
    @Override
    void store(Serializable key, Object value, long maxAge, TimeUnit maxAgeUnit);
    
    /**
     * Stores a computation and in case of success it's result in 
     * this cache using the specified key.
     * 
     * @since 2.4
     * @param key the key under which the result will be found
     * @param callable the computing callable
     * @throws NullPointerException if key or callable is null
     * @throws ExecutionException if callable throws an {@link ExecutionException} it will be 
     *         propagated directly, any other exception will be wrapped
     */
    void store(Serializable key, Callable<?> callable) throws ExecutionException;

    /**
     * Stores a computation and in case of success it's result in 
     * this cache using the specified key.
     * 
     * @since 2.4
     * @param key the key under which the result will be found
     * @param callable the computing callable
     * @param maxAge the maximum age
     * @param maxAgeUnit the unit of maxAge
     * @throws NullPointerException if key, callable or maxAgeUnit is null
     * @throws IllegalArgumentException if maxAge is negative
     * @throws ExecutionException if callable throws an {@link ExecutionException} it will be 
     *         propagated directly, any other exception will be wrapped
     */
    void store(Serializable key, Callable<?> callable, long maxAge, TimeUnit maxAgeUnit) throws ExecutionException;
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     *   When a computation is currently running, this method will block
     *   until it's result is computed or cancelled. In case the 
     *   computation is cancelled this method will return {@code null}.
     * </p>
     * 
     * @since 2.4
     * @return an existing value or the new result of a finished computation
     * @throws RuntimeException any exception thrown by the computing unit
     */
    @Override
    <T> T read(Serializable key);

    /**
     * {@inheritDoc}
     * 
     * <p>
     *   When a computation is currently running for the specified key
     *   it will be cancelled and any pre-computed value will be removed. 
     * </p>
     */
    @Override
    <T> T remove(Serializable key);
    
    /**
     * {@inheritDoc}
     * 
     * Cancels all running computations.
     */
    @Override
    void clear(); 
    
}
