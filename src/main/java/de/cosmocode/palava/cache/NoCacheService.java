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

import com.google.common.base.Preconditions;

/**
 * Noop {@link CacheService} implementation.
 *
 * @since 2.3
 * @author Willi Schoenborn
 */
final class NoCacheService implements CacheService {

    @Override
    public void store(Serializable key, Object value) {
        Preconditions.checkNotNull(key, "Key");
    }

    @Override
    public void store(Serializable key, Object value, long maxAge, TimeUnit maxAgeUnit) {
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkArgument(maxAge >= 0, "Max age must not be negative");
        Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
    }

    @Override
    public void store(Serializable key, Object value, CacheExpiration expiration) {
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkNotNull(expiration, "Expiration");
    }

    @Override
    public <V> V read(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        return null;
    }

    @Override
    public <V> V remove(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        return null;
    }

    @Override
    public void clear() {

    }

}
