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

import com.google.common.annotations.Beta;

/**
 * A container for {@link CacheRegion}s.
 *
 * @since 3.1
 * @author Willi Schoenborn
 */
@Beta
public interface CacheRepository {

    /**
     * Retrieves a named {@link CacheRegion} by either returning a previously
     * created instance or by constructing a new one.
     *
     * @since 3.1
     * @param <K> the generic key type
     * @param <V> the generic value type
     * @param name the region's name
     * @return a named {@link CacheRegion}
     */
    <K extends Serializable, V> CacheRegion<K, V> getRegion(String name);
    
}
