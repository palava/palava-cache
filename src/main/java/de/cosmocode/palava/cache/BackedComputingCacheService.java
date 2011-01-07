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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * A {@link ComputingCacheService} which is backed by an ordinary {@link CacheService}.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
final class BackedComputingCacheService extends AbstractComputingCacheService {

    private final CacheService service;

    @Inject
    public BackedComputingCacheService(@Backing CacheService service) {
        this.service = Preconditions.checkNotNull(service, "Service");
    }

    @Override
    protected void doStore(Serializable key, Object value, CacheExpiration expiration) {
        service.store(key, value, expiration);
    }

    @Override
    protected <V> V doRead(Serializable key) {
        return service.read(key);
    }

    @Override
    protected <V> V doRemove(Serializable key) {
        return service.remove(key);
    }

    @Override
    protected void doClear() {
        service.clear();
    }

    

}
