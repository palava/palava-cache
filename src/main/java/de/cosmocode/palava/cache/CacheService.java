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
    
    long DEFAULT_MAX_AGE = Long.MAX_VALUE;
    
    TimeUnit DEFAULT_MAX_AGE_TIMEUNIT = TimeUnit.DAYS;
    
    /**
     * Returns the max age for every stored item, in seconds.
     *  
     * @return the max age in seconds
     */
    long getMaxAge();
    
    /**
     * Returns the default max age for every stored item.
     * The result is converted using the given TimeUnit.
     * 
     * @param unit the TimeUnit in which the max Age value is returned
     * @return the max age, in the given TimeUnit
     * @throws NullPointerException if unit is null
     */
    long getMaxAge(TimeUnit unit);
    
    /**
     * Sets the maximum age for the store method, in seconds.
     * This method calls {@link #setMaxAge(long, TimeUnit)} with {@link TimeUnit#SECONDS}.
     * 
     * @param maxAgeSeconds the maximum age of every stored item
     * @throws IllegalArgumentException if maxAge is negative
     * @see #setMaxAge(long, TimeUnit)
     */
    void setMaxAge(long maxAgeSeconds);
    
    /**
     * <p>
     *   Sets the maximum age for the store method.
     *   The given value is then used as a default for {@link #store(Serializable, Object)}.
     *   This can be called in a configuration stage to set an explicit maxAge.
     * </p>
     * <p>
     *   A negative maxAge is illegal and results in an IllegalArgumentException.
     *   If eternal caching is intended use {@link #DEFAULT_MAX_AGE} and {@link #DEFAULT_MAX_AGE_TIMEUNIT}.
     * </p>
     * <p>
     *   The default max age is DEFAULT_MAX_AGE with DEFAULT_MAX_AGE_TIMEUNIT,
     *   which is equivalent to an eternal caching.
     * </p>
     * <p>
     *   Note: {@link #store(Serializable, Object, long, TimeUnit)} overrides the default value
     *   with its given parameters. 
     * </p>
     * 
     * @param maxAge the new default maxAge for every stored item
     * @param maxAgeUnit the TimeUnit for maxAge
     * @throws IllegalArgumentException if maxAge is negative
     * @throws NullPointerException if maxAgeUnit is null
     */
    void setMaxAge(long maxAge, TimeUnit maxAgeUnit);

    /**
     * Adds an object to the cache. 
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
     * @throws NullPointerException if key is null
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
     * @param <T> the generic object type
     * @param key the cache key
     * @return a casted instance of T or null, if there was
     *         no value cached for the given key or the value has expired its max age
     * @throws NullPointerException if key is null
     * @throws ClassCastException if the found value couln't be cast into T
     */
    <T> T read(Serializable key);
    
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
     * @param <T> the generic object type
     * @param key the cache key
     * @return the object that associated with key or null if there was none
     * @throws NullPointerException if key is null
     */
    <T> T remove(Serializable key);
    
    /**
     * Clears the cache.
     */
    void clear(); 
}
