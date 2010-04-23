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
 * Used to configure different ways to handle cache
 * updates/refreshes.
 *
 * @author Willi Schoenborn
 */
public enum CacheMode {

    /**
     * Least recently used.
     */
    LRU,
    
    /**
     * Least frequently used.
     */
    LFU,
    
    /**
     * First in, first out.
     */
    FIFO,
    
    /**
     * No limit.
     */
    UNLIMITED,
    
    /**
     * Soft references.
     */
    AUTOMATIC,
    
    /**
     * Discard old values.
     */
    TIMEOUT,
    
    /**
     * {@link CacheMode#LRU} and {@link CacheMode#AUTOMATIC}.
     */
    HYBRID;
    
}
