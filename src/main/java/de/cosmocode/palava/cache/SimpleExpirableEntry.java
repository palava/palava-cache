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

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * A map value which holds it's age.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
final class SimpleExpirableEntry implements ExpirableEntry {
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleExpirableEntry.class);
    
    private final long timestamp = System.currentTimeMillis();
    private final Object value;
    private final long maxAge;
    private final TimeUnit maxAgeUnit;
    
    public SimpleExpirableEntry(@Nullable Object value, long maxAge, TimeUnit maxAgeUnit) {
        this.value = value;
        this.maxAge = maxAge;
        this.maxAgeUnit = Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
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
    
    @Override
    public boolean isExpired() {
        return getAge(maxAgeUnit) > maxAge;
    }
    
    @Override
    public <V> V getValue() {
        if (isExpired()) {
            LOG.trace("Entry found but was too old");
            return null;
        } else {
            @SuppressWarnings("unchecked")
            final V typed = (V) value;
            return typed;
        }
    }
    
}
