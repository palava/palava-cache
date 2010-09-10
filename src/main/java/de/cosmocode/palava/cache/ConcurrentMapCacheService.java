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

    /**
     * A map value which holds it's age.
     *
     * @since 2.4
     * @author Willi Schoenborn
     */
    private static final class AgingEntry {
        
        private final long timestamp = System.currentTimeMillis();
        private final Object value;
        
        public AgingEntry(Object value) {
            this.value = value;
        }
        
        public long getAge(TimeUnit unit) {
            return unit.convert(System.currentTimeMillis() - timestamp, TimeUnit.MILLISECONDS);
        }
        
        public Object getValue() {
            return value;
        }
        
    }
    
    private final MapMaker maker = new MapMaker().softValues();
    private final ConcurrentMap<Serializable, AgingEntry> cache = maker.makeMap();

    private long maxAgeInSeconds;
    
    @Override
    public long getMaxAge() {
        return maxAgeInSeconds;
    }

    @Override
    public long getMaxAge(TimeUnit unit) {
        Preconditions.checkNotNull(unit, "Unit");
        return unit.convert(maxAgeInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void setMaxAge(long maxAgeSeconds) {
        Preconditions.checkArgument(maxAgeSeconds >= 0, "Max age must not be negative");
        this.maxAgeInSeconds = maxAgeSeconds;
    }

    @Override
    public void setMaxAge(long maxAge, TimeUnit maxAgeUnit) {
        Preconditions.checkArgument(maxAge >= 0, "Max age must not be negative");
        Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
        this.maxAgeInSeconds = maxAgeUnit.toSeconds(maxAge);
    }

    @Override
    public void store(Serializable key, Object value) {
        Preconditions.checkNotNull(key, "Key");
        store(key, value, maxAgeInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void store(Serializable key, Object value, long maxAge, TimeUnit maxAgeUnit) {
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkArgument(maxAge >= 0, "Max age must not be negative");
        Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
        cache.put(key, new AgingEntry(value));
    }

    @Override
    public <T> T read(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final AgingEntry entry = cache.get(key);
        if (entry == null || entry.getAge(TimeUnit.SECONDS) > maxAgeInSeconds) {
            return null;
        } else {
            @SuppressWarnings("unchecked")
            final T value = (T) entry.getValue();
            return value;
        }
    }

    @Override
    public <T> T remove(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final T value = this.<T>read(key);
        cache.remove(key);
        return value;
    }

    @Override
    public void clear() {
        cache.clear();
    }

}
