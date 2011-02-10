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
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Predicate;

/**
 * A region of a cache.
 *
 * @since 3.1
 * @author Willi Schoenborn
 * @param <K> generic key type
 * @param <V> generic value type
 */
public interface CacheRegion<K extends Serializable, V> extends ConcurrentMap<K, V> {

    /**
     * Adds an object to the cache, using the given {@link CacheExpiration}.
     *
     * @since 3.1
     * @see #put(Object, Object)
     * @param key the cache key
     * @param value the value being stored
     * @param expiration the configuration for when and how the entry should expire
     * @return the previous value associated with key, or null if there was no mapping for key
     * @throws NullPointerException if key or expiration is null
     */
    V put(K key, V value, CacheExpiration expiration);

    /**
     * Adds an object to the cache if no mapping for the given key exists, using the given {@link CacheExpiration}.
     *
     * @since 3.1
     * @see #put(Object, Object)
     * @param key the cache key
     * @param value the value being stored
     * @param expiration the configuration for when and how the entry should expire
     * @return the previous value associated with key, or null if there was no mapping for key
     * @throws NullPointerException if key or expiration is null
     */
    V putIfAbsent(K key, V value, CacheExpiration expiration);
    
    /**
     * Removes all mappings from this cache that satisfy the given predicate.
     *
     * @since 3.1
     * @param predicate the key predicate
     * @return true if any mapping has been removed
     * @throws NullPointerException if predicate is null
     */
    boolean removeIf(Predicate<? super K> predicate);
    
    /**
     * Provides the name of this region.
     *
     * @since 3.1
     * @return the name of this region
     */
    String getName();
    
}
