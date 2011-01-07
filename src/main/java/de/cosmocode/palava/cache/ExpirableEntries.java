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

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

/**
 * Static utility class for {@link ExpirableEntry}s.
 *
 * @since 3.0
 * @author Willi Schoenborn
 */
final class ExpirableEntries {

    private ExpirableEntries() {
        
    }

    /**
     * Creates an {@link ExpirableEntry} based on the specified.
     *
     * @since 3.0
     * @param expiration the cache expiration
     * @param value the value
     * @return the expirable entry containing value
     * @throws NullPointerException if expiration is null
     */
    static ExpirableEntry create(@Nonnull CacheExpiration expiration, Object value) {
        Preconditions.checkNotNull(expiration, "Expiration");
        if (expiration.isEternal()) {
            return new EternalEntry(value);
        } else if (expiration.getIdleTime() == 0L) {
            return new SimpleExpirableEntry(value, expiration.getLifeTime(), expiration.getLifeTimeUnit());
        } else {
            return new ComplexExpirableEntry(value, expiration);
        }
    }
    
}
