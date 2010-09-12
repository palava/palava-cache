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
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;

/**
 * A {@link ConcurrentMap} backed {@link CacheService}.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
final class ConcurrentMapCacheService implements CacheService {
    
    private final ConcurrentMap<Serializable, AgingEntry> cache;

    private long defaultMaxAge;
    private TimeUnit defaultMaxAgeUnit = TimeUnit.SECONDS;
    
    public ConcurrentMapCacheService() {
        final MapMaker maker = new MapMaker().softKeys().softValues();
        this.cache = maker.makeComputingMap(AgedEntry.PRODUCER);
    }
    
    @Override
    public long getMaxAge() {
        return defaultMaxAgeUnit.toSeconds(defaultMaxAge);
    }

    @Override
    public long getMaxAge(TimeUnit unit) {
        Preconditions.checkNotNull(unit, "Unit");
        return unit.convert(defaultMaxAge, defaultMaxAgeUnit);
    }

    @Override
    public void setMaxAge(long maxAgeInSeconds) {
        Preconditions.checkArgument(maxAgeInSeconds >= 0, "Max age must not be negative");
        this.defaultMaxAge = defaultMaxAgeUnit.convert(maxAgeInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void setMaxAge(long maxAge, TimeUnit maxAgeUnit) {
        Preconditions.checkArgument(maxAge >= 0, "Max age must not be negative");
        Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
        this.defaultMaxAge = maxAge;
        this.defaultMaxAgeUnit = maxAgeUnit;
    }

    @Override
    public void store(Serializable key, Object value) {
        Preconditions.checkNotNull(key, "Key");
        store(key, value, defaultMaxAge, defaultMaxAgeUnit);
    }

    @Override
    public void store(Serializable key, Object value, long maxAge, TimeUnit maxAgeUnit) {
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkArgument(maxAge >= 0, "Max age must not be negative");
        Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
        cache.put(key, new SimpleAgingEntry(value, maxAge, maxAgeUnit));
    }

    @Override
    public <V> V read(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        return cache.get(key).<V>getValue();
    }

    @Override
    public <V> V remove(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final V value = this.<V>read(key);
        cache.remove(key);
        return value;
    }

    @Override
    public void clear() {
        cache.clear();
    }

}
