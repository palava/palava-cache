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

import java.util.concurrent.TimeUnit;

/**
 * A decorator for values used by {@link ConcurrentMapCacheService}
 * to provide expiration.
 * 
 * @since 2.4
 * @author Willi Schoenborn
 */
interface ExpirableEntry {

    /**
     * Returns the timestamp of this entry.
     * 
     * @since 2.4
     * @return the timestamp
     */
    long getTimestamp();
    
    /**
     * Calculates the age of this entry and returns it
     * in the given time unit.
     * 
     * @since 2.4
     * @param unit the desired time unit
     * @return the age of this entry in the specified unit
     * @throws NullPointerException if unit is null
     */
    long getAge(TimeUnit unit);
    
    /**
     * Checks whether this entry is expired.
     * 
     * @since 2.4
     * @return true if this entry is expired, false otherwise
     */
    boolean isExpired();

    /**
     * Provides the value of this entry if the age is no greater
     * than the maximum age or null if this entry is expired.
     * 
     * @param <V> the generic value type
     * @return the value of this entry or null if it is expired
     */
    <V> V getValue();

}
