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

/**
 * Abstract {@link CacheService} implementation.
 *
 * @deprecated no replacement
 * @since 3.0
 * @author Willi Schoenborn
 */
@Deprecated
public abstract class AbstractCacheService implements CacheService {

    @Override
    public void store(Serializable key, Object value) {
        store(key, value, CacheExpirations.ETERNAL);
    }
    
}
