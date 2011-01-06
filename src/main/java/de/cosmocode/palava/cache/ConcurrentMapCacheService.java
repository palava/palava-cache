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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;

/**
 * A {@link ConcurrentMap} backed {@link CacheService}.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
final class ConcurrentMapCacheService implements CacheService, Initializable {
    
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentMapCacheService.class);

    private ReferenceMode keyMode = ReferenceMode.SOFT;
    private ReferenceMode valueMode = ReferenceMode.SOFT;
    
    private int maximumSize;
    private int initialCapacity = 16;
    private int concurrencyLevel = 16;

    // defaultMaxAge is 0, which means no max age by interface definition
    private long defaultMaxAge;
    private TimeUnit defaultMaxAgeUnit = TimeUnit.MINUTES;
    
    private ConcurrentMap<Serializable, AgingEntry> cache;
    
    @Override
    public void initialize() throws LifecycleException {
        final MapMaker maker = new MapMaker();
        
        LOG.info("Using {} keys", keyMode.name().toLowerCase());
        switch (keyMode) {
            case SOFT: {
                maker.softKeys();
                break;
            }
            case WEAK: {
                maker.weakKeys();
                break;
            }
            case STRONG: {
                // nothing to do
                break;
            }
            default: {
                
            }
        }

        LOG.info("Using {} values", valueMode.name().toLowerCase());
        switch (valueMode) {
            case SOFT: {
                maker.softValues();
                break;
            }
            case WEAK: {
                maker.weakValues();
                break;
            }
            case STRONG: {
                // nothing to do
                break;
            }
            default: {
                
            }
        }
        
        LOG.info("Setting initial capacity to {}", initialCapacity);
        maker.initialCapacity(initialCapacity);
        
        LOG.info("Setting concurrency level to {}", concurrencyLevel);
        maker.concurrencyLevel(concurrencyLevel);
        
        if (maximumSize == 0) {
            LOG.info("Not limiting elements");
        } else {
            LOG.info("Limiting to {} elements", maximumSize);
        }
        
        this.cache = maker.makeComputingMap(AgedEntry.INSTANCE);
    }

    /**
     * Sets the max age to the given value.
     * @param maxAge the maximum age for an entry to live
     */
    @Inject(optional = true)
    void setMaxAge(@Named(ConcurrentMapCacheConfig.MAX_AGE) long maxAge) {
        Preconditions.checkArgument(maxAge >= 0, "Max age must not be negative");
        this.defaultMaxAge = maxAge;
    }

    @Inject(optional = true)
    void setMaxAgeUnit(@Named(ConcurrentMapCacheConfig.MAX_AGE_UNIT) TimeUnit maxAgeUnit) {
        this.defaultMaxAgeUnit = Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
    }
    
    @Inject(optional = true)
    void setMaximumSize(@Named(ConcurrentMapCacheConfig.MAXIMUM_SIZE) int maximumSize) {
        this.maximumSize = maximumSize;
    }

    @Inject(optional = true)
    void setConcurrencyLevel(@Named(ConcurrentMapCacheConfig.CONCURRENCY_LEVEL) int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
    }

    @Inject(optional = true)
    void setInitialCapacity(@Named(ConcurrentMapCacheConfig.INITIAL_CAPACITY) int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    @Override
    public void store(Serializable key, Object value) {
        Preconditions.checkNotNull(key, "Key");
        store(key, value, new CacheExpiration(defaultMaxAge, defaultMaxAgeUnit));
    }

    @Override
    public void store(Serializable key, Object value, long maxAge, TimeUnit maxAgeUnit) {
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkArgument(maxAge >= 0, "Max age must not be negative");
        Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
        store(key, value, new CacheExpiration(maxAge, maxAgeUnit));
    }

    @Override
    public void store(Serializable key, Object value, CacheExpiration expiration) {
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkNotNull(expiration, "Expiration");
        if (expiration.isEternal()) {
            cache.put(key, new ImmortalEntry(value));
        } else if (expiration.getIdleTime() == 0L) {
            cache.put(key, new SimpleAgingEntry(value, expiration.getLifeTime(), expiration.getLifeTimeUnit()));
        } else {
            cache.put(key, new IdlingOutAgingEntry(value, expiration));
        }
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
