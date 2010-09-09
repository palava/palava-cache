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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ComputationException;

// extends cache service?
public interface ComputingCacheService extends CacheService {

    /**
     * Cancels currently running execution
     */
    @Override
    void store(Serializable key, Object value);
    
    /**
     * Cancels currently running execution
     */
    @Override
    void store(Serializable key, Object value, long maxAge, TimeUnit maxAgeUnit);
    
    void store(Serializable key, Callable<?> callable) throws ComputationException;
    
    void store(Serializable key, Callable<?> callable, long maxAge, TimeUnit maxAgeUnit) throws ComputationException;
    
    /**
     * 
     * 
     * @since 
     * @param <T>
     * @param key
     * @return
     * @throws IllegalStateException wrapped {@link InterruptedException}
     */
    <T> T read(Serializable key) throws CancellationException;

    /**
     * Cancels running execution.
     * 
     */
    <T> T remove(Serializable key);
    
    /**
     * Cancels all running execution
     */
    void clear(); 
    
}
