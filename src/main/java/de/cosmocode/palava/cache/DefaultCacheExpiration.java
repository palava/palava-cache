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

import javax.annotation.concurrent.ThreadSafe;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;

/**
 * <p>
 * A CacheExpiration defines when an entry stored in a {@link CacheService} should expire.
 * It must be configured with a life time and an idle time.
 * More information about the configuration can be found in the constructor:
 * {@link #DefaultCacheExpiration(long, TimeUnit, long, TimeUnit)}.
 * </p>
 * <p>
 * Created on: 06.01.11
 * </p>
 *
 * @since 3.0
 * @author Oliver Lorenz
 */
@Beta
@ThreadSafe
public final class DefaultCacheExpiration implements Serializable, CacheExpiration {

    private static final long serialVersionUID = 6224050536184774904L;

    private final long lifeTime;
    private final TimeUnit lifeTimeUnit;
    private final long idleTime;
    private final TimeUnit idleTimeUnit;

    /**
     * <p>
     *   Constructs a new CacheExpiration with the given life time for a {@link CacheService}.
     *   The life time is set in its {@link TimeUnit}.
     * </p>
     * <p>
     *   If lifeTime is 0 then it means that the value should never expire.
     *   The cache entry may still be removed from the CacheService
     *   if outside limitations occur (e.g. when the application runs out of memory).
     *   The constant {@link CacheExpirations#ETERNAL} is already configured this way.
     * </p>
     * <p>
     *   If lifeTime is greater than 0
     *   then the value is cached at most until lifeTime has passed.
     *   The {@link CacheService} may still remove the cached entry before that amount of time
     *   because of outside limitations (e.g a full memory).
     * </p>
     *
     * @param lifeTime the maximum amount of time for the cached value to live, in lifeTimeUnit; 0 means forever
     * @param lifeTimeUnit the TimeUnit for lifeTime
     * @throws NullPointerException if lifeTimeUnit or idleTimeUnit is null
     * @throws IllegalArgumentException if lifeTime or idleTime is negative (less than 0)
     */
    public DefaultCacheExpiration(long lifeTime, TimeUnit lifeTimeUnit) {
        Preconditions.checkArgument(lifeTime >= 0, "LifeTime must not be negative");
        Preconditions.checkNotNull(lifeTimeUnit, "LifeTimeUnit");

        this.lifeTime = lifeTime;
        this.lifeTimeUnit = lifeTimeUnit;
        this.idleTime = 0L;
        this.idleTimeUnit = TimeUnit.MINUTES;
    }

    /**
     * <p>
     *   Constructs a new CacheExpiration with the given life and idle time for a {@link CacheService}.
     *   The life time and the idle time are set in their respective {@link TimeUnit}s.
     * </p>
     * <p>
     *   If both lifeTime and idleTime are 0 then it means that the value should never expire.
     *   The cache entry may still be removed from the CacheService
     *   if outside limitations occur (e.g. when the application runs out of memory).
     *   The constant {@link CacheExpirations#ETERNAL} is already configured this way.
     * </p>
     * <p>
     *   If lifeTime is 0 and idleTime is greater than 0
     *   then the value is only checked for idleTime, but otherwise lives eternally.
     *   Idling means that if the entry key is not read from the CacheService
     *   for the amount of time denoted by idleTime then it will be evicted/removed from the cache.
     *   This implies that the maximum amount of time that can pass between two reads for the cached entry
     *   is the amount of time denoted by idleTime.
     * </p>
     * <p>
     *   If lifeTime is greater than 0 and idleTime is 0
     *   then the value is cached at most until lifeTime has passed.
     *   The {@link CacheService} may still remove the cached entry before that amount of time
     *   because of outside limitations (e.g a full memory).
     * </p>
     * <p>
     *   If both lifeTime and idleTime are greater than 0
     *   then the value is cached at most until lifeTime has passed or if it has idled for too long.
     *   Idling means that if the entry key is not read from the CacheService
     *   for the amount of time denoted by idleTime then it will be evicted/removed from the cache.
     *   This implies that a cached entry is cached for at least the amount of time in idleTime
     *   and at most for the amount of time in lifeTime.
     * </p>
     *
     * @param lifeTime the maximum amount of time for the cached value to live; 0 means forever
     * @param lifeTimeUnit the TimeUnit for lifeTime
     * @param idleTime the maximum amount of time a value "survives" between reads; 0 means no idle checks
     * @param idleTimeUnit the TimeUnit for idleTime
     * @throws NullPointerException if lifeTimeUnit or idleTimeUnit is null
     * @throws IllegalArgumentException if lifeTime or idleTime is negative (less than 0)
     */
    public DefaultCacheExpiration(long lifeTime, TimeUnit lifeTimeUnit, long idleTime, TimeUnit idleTimeUnit) {
        Preconditions.checkArgument(lifeTime >= 0, "LifeTime must not be negative");
        Preconditions.checkArgument(idleTime >= 0, "IdleTime must not be negative");

        this.lifeTime = lifeTime;
        this.lifeTimeUnit = Preconditions.checkNotNull(lifeTimeUnit, "LifeTimeUnit");
        this.idleTime = idleTime;
        this.idleTimeUnit = Preconditions.checkNotNull(idleTimeUnit, "IdleTimeUnit");
    }

    /**
     * Convenience constructor in the case that both life time and idle time are in the same TimeUnit.
     * This is equivalent to: {@code new CacheExpiration(lifeTime, unit, idleTime, unit)}.
     *
     * @see #DefaultCacheExpiration(long, TimeUnit, long, TimeUnit)
     * @param lifeTime the maximum amount of time for the cached value to live; 0 means forever
     * @param idleTime the maximum amount of time a value "survives" between reads; 0 means no idle checks
     * @param unit the TimeUnit for both lifeTime and idleTime
     */
    public DefaultCacheExpiration(long lifeTime, long idleTime, TimeUnit unit) {
        this(lifeTime, unit, idleTime, unit);
    }

    @Override
    public boolean isEternal() {
        return lifeTime == 0L && idleTime == 0L;
    }

    @Override
    public long getLifeTime() {
        return lifeTime;
    }

    @Override
    public TimeUnit getLifeTimeUnit() {
        return lifeTimeUnit;
    }

    @Override
    public long getLifeTimeIn(final TimeUnit unit) {
        Preconditions.checkNotNull(unit, "Unit");
        return unit.convert(lifeTime, lifeTimeUnit);
    }

    @Override
    public long getIdleTime() {
        return idleTime;
    }

    @Override
    public TimeUnit getIdleTimeUnit() {
        return idleTimeUnit;
    }

    @Override
    public long getIdleTimeIn(final TimeUnit unit) {
        Preconditions.checkNotNull(unit, "Unit");
        return unit.convert(idleTime, idleTimeUnit);
    }
    
}
