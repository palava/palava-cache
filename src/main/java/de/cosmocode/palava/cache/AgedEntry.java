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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

/**
 * A constant {@link AgingEntry} which is always expired.
 * 
 * @author Willi Schoenborn
 */
enum AgedEntry implements AgingEntry, Function<Serializable, AgingEntry> {

    INSTANCE;
    
    private static final Logger LOG = LoggerFactory.getLogger(AgedEntry.class);
    
    @Override
    public long getTimestamp() {
        return 0;
    }
    
    @Override
    public long getAge(TimeUnit unit) {
        Preconditions.checkNotNull(unit, "Unit");
        return unit.convert(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    @Override
    public boolean isExpired() {
        return true;
    }
    
    @Override
    public <V> V getValue() {
        LOG.trace("No entry found");
        return null;
    }

    @Override
    public AgingEntry apply(Serializable key) {
        return this;
    }
    
}
