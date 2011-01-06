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
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *   A Service used to cache objects. It can be configured with a maxAge
 *   so that cached objects expire after the given amount of time.
 * </p>
 *
 * @author Willi Schoenborn
 * @author Markus Baumann
 * @author Oliver Lorenz
 */
public interface CacheService {

    /**
     * Adds an object to the cache.
     *
     * <p>
     *   It is not guaranteed that the value will remain in the cache forever.
     *   The implementation may evict some cached values due to outside limitations
     *   (e.g. a full memory) or because it was configured to evict it after a certain time,
     *   but it should otherwise attempt to cache the value as long as possible.
     * </p>
     * 
     * @param key the cache key
     * @param value the value being stored
     * @throws NullPointerException if key is null
     */
    void store(Serializable key, Object value);
    
    /**
     * Adds an object to the cache.
     * 
     * <p>
     *   The maxAge parameter determines the maximum age that the value should live.
     *   This means that once the maxAge has passed, a {@link #read(Serializable))}
     *   with the given key returns null until a new value has been stored for the key.
     * </p>
     * 
     * @param key the cache key
     * @param value the value being stored
     * @param maxAge the maximum age that the stored value should be cached, in `maxAgeUnit`
     * @param maxAgeUnit the TimeUnit of the maxAge (like DAYS, SECONDS, etc.)
     * @throws NullPointerException if key or maxAgeUnit is null
     * @throws IllegalArgumentException if maxAge is negative
     */
    void store(Serializable key, Object value, long maxAge, TimeUnit maxAgeUnit);
    
    /**
     * Reads an object from the cache.
     * 
     * <p>
     *   <strong>Note:</strong>
     *   This method automatically casts into the needed
     *   type. It's in the callers responsibilities that
     *   no ClassCastException occurs.
     * </p>
     * 
     * @param <V> the generic object type
     * @param key the cache key
     * @return a casted instance of T or null, if there was
     *         no value cached for the given key or the value has expired its max age
     * @throws NullPointerException if key is null
     * @throws ClassCastException if the found value couln't be cast into T
     */
    <V> V read(Serializable key);
    
    /**
     * Clears an object from the cache by key.
     * 
     * <p>
     *   <strong>Note:</strong>
     *   This method automatically casts into the needed
     *   type. It's in the callers responsibilities that
     *   no ClassCastException occurs.
     * </p>
     * 
     * @param <V> the generic object type
     * @param key the cache key
     * @return the object that associated with key or null if there was none
     * @throws NullPointerException if key is null
     */
    <V> V remove(Serializable key);
    
    /**
     * Clears the cache.
     */
    void clear(); 
}
