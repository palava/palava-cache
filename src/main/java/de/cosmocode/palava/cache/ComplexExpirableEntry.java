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

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;

/**
 * <p>
 * An {@link ExpirableEntry} that expires if it has not been accessed since a given amount of time.
 * </p>
 * <p>
 * Created on: 06.01.11
 * </p>
 *
 * @since 3.0
 * @author Oliver Lorenz
 */
@Beta
final class ComplexExpirableEntry implements ExpirableEntry {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleExpirableEntry.class);

    private final long timestamp = System.currentTimeMillis();
    private final Object value;
    private final long maxAge;
    private final TimeUnit maxAgeUnit;
    private final long idleTime;
    private final TimeUnit idleTimeUnit;
    private final boolean checkAge;

    private long lastAccess = timestamp;

    ComplexExpirableEntry(@Nullable Object value, CacheExpiration expiration) {
        this.value = value;
        this.maxAge = expiration.getLifeTime();
        this.maxAgeUnit = Preconditions.checkNotNull(expiration.getLifeTimeUnit(), "MaxAgeUnit");
        this.checkAge = expiration.getLifeTime() > 0;
        this.idleTime = expiration.getIdleTime();
        this.idleTimeUnit = Preconditions.checkNotNull(expiration.getIdleTimeUnit(), "IdleTimeUnit");
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public long getAge(TimeUnit unit) {
        Preconditions.checkNotNull(unit, "Unit");
        return unit.convert(System.currentTimeMillis() - timestamp, TimeUnit.MILLISECONDS);
    }

    private long getTimeSinceLastAccess(TimeUnit unit) {
        Preconditions.checkNotNull(unit, "Unit");
        return unit.convert(System.currentTimeMillis() - lastAccess, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isExpired() {
        return (checkAge && getAge(maxAgeUnit) > maxAge) || getTimeSinceLastAccess(idleTimeUnit) > idleTime;
    }

    @Override
    public <V> V getValue() {
        if (isExpired()) {
            logExpiredMessage();
            return null;
        } else {
            lastAccess = System.currentTimeMillis();
            @SuppressWarnings("unchecked")
            final V typed = (V) value;
            return typed;
        }
    }

    private void logExpiredMessage() {
        if (LOG.isTraceEnabled()) {
            if (getTimeSinceLastAccess(idleTimeUnit) > idleTime) {
                LOG.trace("Entry found but has idled out (no access since {})", new Date(lastAccess));
            } else {
                LOG.trace("Entry found but was too old");
            }
        }
    }

}
