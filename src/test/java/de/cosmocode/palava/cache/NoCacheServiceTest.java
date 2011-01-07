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

import org.junit.Assert;

/**
 * Test for {@link NoCacheService}.
 *
 * This test overwrites some methods which forces a {@link CacheService} to actually
 * cache an object when {@link CacheService#store(Serializable, Object)} is called.
 *
 * @since 2.3
 * @author Willi Schoenborn
 */
public final class NoCacheServiceTest extends CacheServiceTest {

    @Override
    public CacheService unit() {
        return new NoCacheService();
    }

    @Override
    public void testStoreWithCacheExpirationEternal() {
        final CacheService unit = unit();
        unit.store(1, "test", CacheExpirations.ETERNAL);
    }

    @Override
    public void testStoreWithIdleTime() throws InterruptedException {
        final CacheService unit = unit();
        unit.store(1, "TestEntry", new DefaultCacheExpiration(0, 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public void testStoreWithLifeAndIdleTime() throws InterruptedException {
        final CacheService unit = unit();
        unit.store(1, "TestEntry", new DefaultCacheExpiration(50, 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public void testStoreAndRead() {
        final CacheService unit = unit();
        unit.store(1, "TestEntry");
        Assert.assertNull(unit.read("null"));
    }
    
    @Override
    public void testStoreAndReadObject() {
        final Object value = new Object();
        final CacheService unit = unit();
        unit.store(1, value);
    }
    
    @Override
    public void testRemove() {
        final CacheService unit = unit();
        unit.store(1, "TestEntry");
        Assert.assertTrue(unit.read(1) == null);
        Assert.assertNull(unit.remove("null"));
    }
    
}
