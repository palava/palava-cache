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

import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

/**
 * Reusable {@link CacheExpiration}s.
 *
 * @since 3.0
 * @author Willi Schoenborn
 */
public enum CacheExpirations implements CacheExpiration {

    ETERNAL(0L, TimeUnit.MINUTES, 0L, TimeUnit.MINUTES);
    
    private final long lifeTime;
    private final TimeUnit lifeTimeUnit;
    private final long idleTime;
    private final TimeUnit idleTimeUnit;
    
    private CacheExpirations(long lifeTime, TimeUnit lifeTimeUnit, long idleTime, TimeUnit idleTimeUnit) {
        Preconditions.checkArgument(lifeTime >= 0, "LifeTime must not be negative");
        Preconditions.checkNotNull(lifeTimeUnit, "LifeTimeUnit");
        Preconditions.checkArgument(idleTime >= 0, "IdleTime must not be negative");
        Preconditions.checkNotNull(idleTimeUnit, "IdleTimeUnit");

        this.lifeTime = lifeTime;
        this.lifeTimeUnit = lifeTimeUnit;
        this.idleTime = idleTime;
        this.idleTimeUnit = idleTimeUnit;
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
