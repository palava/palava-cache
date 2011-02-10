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

import com.google.common.annotations.Beta;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingConcurrentMap;

/**
 * A forwarding {@link CacheRegion} implementation allowing easy composition.
 *
 * @since 3.1
 * @author Willi Schoenborn
 * @param <K> generic key type
 * @param <V> generic value type
 */
@Beta
public abstract class ForwardingCacheRegion<K extends Serializable, V> extends ForwardingConcurrentMap<K, V>
    implements CacheRegion<K, V> {

    @Override
    protected abstract CacheRegion<K, V> delegate();

    @Override
    public V put(K key, V value, CacheExpiration expiration) {
        return delegate().put(key, value, expiration);
    }

    @Override
    public V putIfAbsent(K key, V value, CacheExpiration expiration) {
        return delegate().putIfAbsent(key, value, expiration);
    }

    @Override
    public boolean removeIf(Predicate<? super K> predicate) {
        return delegate().removeIf(predicate);
    }

    @Override
    public String getName() {
        return delegate().getName();
    }
    
}
