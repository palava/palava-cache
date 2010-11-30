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

/**
 * Static constant holder class for concurrent map cache config key names.
 * 
 * @version 2.4
 * @author Willi Schoenborn
 */
final class ConcurrentMapCacheConfig {
    
    public static final String MAX_AGE = CacheConfig.PREFIX + "maxAge";

    public static final String MAX_AGE_UNIT = CacheConfig.PREFIX + "maxAgeUnit";
    
    public static final String MAXIMUM_SIZE = CacheConfig.PREFIX + "maximumSize";
    
    public static final String INITIAL_CAPACITY = CacheConfig.PREFIX + "initialCapacity";
    
    public static final String CONCURRENCY_LEVEL = CacheConfig.PREFIX + "concurrencyLevel";
    
    private ConcurrentMapCacheConfig() {
        
    }

}
