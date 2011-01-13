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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.cosmocode.commons.Strings;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import de.cosmocode.palava.cron.CronService;

/**
 * A {@link ConcurrentMap} backed {@link CacheService}.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
final class ConcurrentMapCacheService extends AbstractCacheService implements Initializable, Runnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentMapCacheService.class);

    private ReferenceMode keyMode = ReferenceMode.SOFT;
    private ReferenceMode valueMode = ReferenceMode.SOFT;
    
    private int maximumSize;
    private int initialCapacity = 16;
    private int concurrencyLevel = 16;

    private final CronService cron;
    private String cronExpression = "0 * * * * ?";
    
    private ConcurrentMap<Serializable, ExpirableEntry> cache;
    
    @Inject
    ConcurrentMapCacheService(CronService cron) {
        this.cron = Preconditions.checkNotNull(cron, "Cron");
    }
    
    @Inject(optional = true)
    void setKeyMode(@Named(ConcurrentMapCacheServiceConfig.KEY_MODE) ReferenceMode keyMode) {
        this.keyMode = Preconditions.checkNotNull(keyMode, "KeyMode");
    }

    @Inject(optional = true)
    void setValueMode(@Named(ConcurrentMapCacheServiceConfig.VALUE_MODE) ReferenceMode valueMode) {
        this.valueMode = Preconditions.checkNotNull(valueMode, "ValueMode");
    }

    @Inject(optional = true)
    void setMaximumSize(@Named(ConcurrentMapCacheServiceConfig.MAXIMUM_SIZE) int maximumSize) {
        this.maximumSize = maximumSize;
    }

    @Inject(optional = true)
    void setConcurrencyLevel(@Named(ConcurrentMapCacheServiceConfig.CONCURRENCY_LEVEL) int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
    }

    @Inject(optional = true)
    void setInitialCapacity(@Named(ConcurrentMapCacheServiceConfig.INITIAL_CAPACITY) int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    @Inject(optional = true)
    void setCronExpression(@Named(ConcurrentMapCacheServiceConfig.CRON_EXPRESSION) String cronExpression) {
        this.cronExpression = Strings.checkNotBlank(cronExpression, "CronExpression");
    }

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
                throw new AssertionError("Default case matched " + keyMode);
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
                throw new AssertionError("Default case matched " + valueMode);
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
        
        this.cache = maker.makeMap();
        
        cron.schedule(this, cronExpression);
    }
    
    @Override
    public void run() {
        for (Entry<Serializable, ExpirableEntry> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                remove(entry.getKey());
            }
        }
    }

    @Override
    public void store(Serializable key, Object value, CacheExpiration expiration) {
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkNotNull(expiration, "Expiration");
        final ExpirableEntry entry = ExpirableEntries.create(expiration, value);
        cache.put(key, entry);
    }

    @Override
    public <V> V read(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final ExpirableEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        } else if (entry.isExpired()) {
            cache.remove(key);
            return null;
        } else {
            return entry.<V>getValue();
        }
    }

    @Override
    public <V> V remove(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final ExpirableEntry entry = cache.remove(key);
        return entry == null ? null : entry.<V>getValue();
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public String toString() {
        return "ConcurrentMapCacheService [" +
                "keyMode=" + keyMode + ", " +
                "valueMode=" + valueMode + ", " +
                "maximumSize=" + maximumSize + ", " +
                "initialCapacity=" + initialCapacity + ", " +
                "concurrencyLevel=" + concurrencyLevel + ", " +
                "cronExpression=" + cronExpression + 
            "]";
    }
    
}
