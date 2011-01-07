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

import com.google.common.annotations.Beta;

/**
 * A CacheExpiration defines when an entry stored in a {@link CacheService} should expire.
 * It must be configured with a life time and an idle time.
 * More information about the configuration can be found in the constructor:
 * {@link DefaultCacheExpiration#DefaultCacheExpiration(long, TimeUnit, long, TimeUnit)}.
 *
 * @since 3.0
 * @author Oliver Lorenz
 */
@Beta
public interface CacheExpiration {

    /**
     * Returns true if this CacheExpiration is eternal, false otherwise.
     * A CacheExpiration is considered eternal if both lifeTime and idleTime are 0.
     *
     * @return true if this CacheExpiration is eternal, false otherwise
     */
    boolean isEternal();

    /**
     * Returns the life time set by the constructor.
     * @return the life time
     */
    long getLifeTime();

    /**
     * Returns the life time unit set by the constructor.
     * @return the life time unit
     */
    TimeUnit getLifeTimeUnit();

    /**
     * Returns the life time in the given TimeUnit.
     *
     * @param unit the target TimeUnit in which to return the life time
     * @return the lifeTime, in the given TimeUnit
     * @throws NullPointerException if the given TimeUnit is null
     */
    long getLifeTimeIn(final TimeUnit unit);

    /**
     * Returns the idle time set by the constructor.
     * @return the idle time
     */
    long getIdleTime();

    /**
     * Returns the idle time unit set by the constructor.
     * @return the idle time unit
     */
    TimeUnit getIdleTimeUnit();

    /**
     * Returns the idle time in the given TimeUnit.
     *
     * @param unit the target TimeUnit in which to return the idle time
     * @return the idleTime, in the given TimeUnit
     * @throws NullPointerException if the given TimeUnit is null
     */
    long getIdleTimeIn(final TimeUnit unit);

}
