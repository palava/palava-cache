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
import org.junit.Test;
import org.junit.runner.RunWith;

import de.cosmocode.junit.LoggingRunner;
import de.cosmocode.junit.UnitProvider;

/**
 * Abstract test-class for CacheService.
 *
 * <p>
 *   The following methods can be overridden if the cache can not handle millisecond precision expiration:
 *   {@link #lifeTime()}, {@link #idleTime()}, {@link #sleepBeforeIdleTime()}, {@link #sleepBeforeIdleTime()},
 *   {@link #timeUnit()}.
 * </p>
 * 
 * @author Markus Baumann
 * @author Oliver Lorenz (everything with life and idle time)
 */
@RunWith(LoggingRunner.class)
public abstract class CacheServiceTest implements UnitProvider<CacheService> {

    /**
     * The life time for testing, in {@link #timeUnit()}.
     * @return the life time for testing
     */
    protected long lifeTime() {
        return 50;
    }

    /**
     * The idle time for testing, in {@link #timeUnit()}.
     * @return the idle time for testing
     */
    protected long idleTime() {
        return 50;
    }

    /**
     * Returns a time in {@link #timeUnit()} after which the element has not idled out,
     * so that a read still returns the element.
     * @return the time (to sleep) after which a read still returns the element
     */
    protected long sleepBeforeIdleTime() {
        return 40;
    }

    /**
     * Returns the time in milliseconds after which the cached element is expired,
     * either because it idled out or the life time has been reached.
     * @return the time (to sleep) after which the element has definitely expired
     */
    protected long sleepTimeExpires() {
        return 100;
    }

    /**
     * Returns the timeunit for {@link #lifeTime()}, {@link #idleTime()}, {@link #sleepTimeExpires()}
     * and {@link #sleepBeforeIdleTime()}.
     * @return the TimeUnit used for testing
     */
    protected TimeUnit timeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    /**
     * Tests {@link CacheService#store(Serializable, Object, CacheExpiration)} with {@link CacheExpirations#ETERNAL}.
     * It does not test that it is stored eternally, just that it is stored beyond the expiration sleep time.
     * @throws InterruptedException if the Thread.sleep is interrupted
     */
    @Test
    public void testStoreWithCacheExpirationEternal() throws InterruptedException {
        final CacheService unit = unit();
        unit.store(1, "TestEntry", CacheExpirations.ETERNAL);
        Thread.sleep(TimeUnit.MILLISECONDS.convert(sleepTimeExpires(), timeUnit()));
        Assert.assertEquals("TestEntry", unit.read(1));
    }

    /**
     * Tests {@link CacheService#store(Serializable, Object, CacheExpiration)}
     * with a CacheExpiration where only the life time is set.
     * @throws InterruptedException if the Thread.sleep is interrupted
     */
    @Test
    public void testStoreWithLifeTime() throws InterruptedException {
        final CacheService unit = unit();
        unit.store(1, "TestEntry", new DefaultCacheExpiration(lifeTime(), timeUnit()));
        Thread.sleep(TimeUnit.MILLISECONDS.convert(sleepTimeExpires(), timeUnit()));
        Assert.assertNull("should be expired, but is not", unit.read(1));
    }

    /**
     * Tests {@link CacheService#store(Serializable, Object, CacheExpiration)}
     * with a CacheExpiration where only the idle time is set.
     * @throws InterruptedException if the Thread.sleep is interrupted
     */
    @Test
    public void testStoreWithIdleTime() throws InterruptedException {
        final CacheService unit = unit();
        unit.store(1, "TestEntry", new DefaultCacheExpiration(0L, idleTime(), timeUnit()));
        Thread.sleep(TimeUnit.MILLISECONDS.convert(sleepBeforeIdleTime(), timeUnit()));
        Assert.assertEquals("Entry has idled out too early", "TestEntry", unit.read(1));
        Thread.sleep(TimeUnit.MILLISECONDS.convert(sleepTimeExpires(), timeUnit()));
        Assert.assertNull("should be expired, but is not", unit.read(1));
    }

    /**
     * Tests {@link CacheService#store(Serializable, Object, CacheExpiration)}
     * with a CacheExpiration where only the idle time is set.
     * @throws InterruptedException if the Thread.sleep is interrupted
     */
    @Test
    public void testStoreWithLifeAndIdleTime() throws InterruptedException {
        final CacheService unit = unit();
        unit.store(1, "TestEntry", new DefaultCacheExpiration(lifeTime(), idleTime(), timeUnit()));
        Thread.sleep(TimeUnit.MILLISECONDS.convert(sleepBeforeIdleTime(), timeUnit()));
        Assert.assertEquals("Entry has idled out too early", "TestEntry", unit.read(1));
        Thread.sleep(TimeUnit.MILLISECONDS.convert(sleepTimeExpires(), timeUnit()));
        Assert.assertNull("should be expired, but is not", unit.read(1));
    }
    
    /**
     * Testing Store-Function.
     */
    @Test
    public void testStoreAndRead() {
        final CacheService unit = unit();
        unit.store(1 , "TestEntry");
        Assert.assertTrue("TestEntry".equals(unit.read(1)));
        Assert.assertNull(unit.read("null"));
    }

    /**
     * Testing Store-Function with a non-serializable value.
     */
    @Test
    public void testStoreAndReadObject() {
        final Object value = new Object();
        final CacheService unit = unit();

        unit.store(1 , value);
        Assert.assertEquals(value, unit.read(1));
    }

    /**
     * Testing Store-Function.
     * expecting NullPointerException
     */
    @Test(expected = NullPointerException.class)
    public void testStoreNullPointerException() {
        unit().store(null, "TestEntry");
    }
    
    /**
     * Testing Read-Function.
     * expecting NullPointerException
     */
    @Test(expected = NullPointerException.class)
    public void testReadNullPointerException() {
        unit().read(null);
    }
    
    /**
     * Testing Remove-Function.
     */
    @Test
    public void testRemove() {
        final CacheService unit = unit();
        unit.store(1 , "TestEntry");
        Assert.assertTrue("TestEntry".equals(unit.remove(1)));
        Assert.assertTrue(unit.read(1) == null);
        Assert.assertNull(unit.remove("null"));
    }
    
    /**
     * Testing Remove-Function.
     * Expecting NullPointerException
     */
    @Test(expected = NullPointerException.class)
    public void testRemoveNullPointerException() {
        unit().remove(null);
    }

    /**
     * Testing the Clear-Function.
     */
    @Test
    public void testClear() {
        final CacheService unit = unit();
        for (int i = 0; i < 10; i++) {
            unit.store(i , "TestEntry");
        }
        unit.clear();
        for (int j = 0; j < 10; j++) {
            Assert.assertSame(null, unit.read(j));
        }
    }
}
