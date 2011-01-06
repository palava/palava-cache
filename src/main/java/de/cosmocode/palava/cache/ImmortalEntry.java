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

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * An {@link AgingEntry} that never expires.
 * </p>
 * <p>
 * Created on: 06.01.11
 * </p>
 *
 * @since 3.0
 * @author Oliver Lorenz
 */
final class ImmortalEntry implements AgingEntry {

    private final long timestamp = System.currentTimeMillis();
    private final Object value;

    public ImmortalEntry(@Nullable Object value) {
        this.value = value;
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
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getValue() {
        return (V) value;
    }

}