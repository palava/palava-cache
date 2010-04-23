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

import de.cosmocode.junit.UnitProvider;

/**
 * Abstract test-class for CacheService.
 * 
 * @author Markus Baumann
 * @author Oliver Lorenz (everything with maxAge)
 *
 */
public abstract class CacheServiceTest implements UnitProvider<CacheService> {

    /**
     * Tests {@link CacheService#setMaxAge(long, TimeUnit)} with a negative value.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setMaxAgeTimeUnitNegative() {
        unit().setMaxAge(-1, TimeUnit.SECONDS);
    }
    
    /**
     * Tests {@link CacheService#setMaxAge(long, TimeUnit)} with a TimeUnit of null.
     */
    @Test(expected = NullPointerException.class)
    public void setMaxAgeTimeUnitNull() {
        unit().setMaxAge(10, null);
    }
    
    /**
     * Tests {@link CacheService#setMaxAge(long, TimeUnit)}.
     */
    @Test
    public void setMaxAgeTimeUnit() {
        final TimeUnit timeUnit = TimeUnit.HOURS;
        final CacheService unit = unit();
        unit.setMaxAge(5, timeUnit);
        Assert.assertEquals(5, unit.getMaxAge(timeUnit));
    }
    
    /**
     * Tests {@link CacheService#setMaxAge(long)} with a negative value.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setMaxAgeNegative() {
        unit().setMaxAge(-1);
    }
    
    /**
     * Tests {@link CacheService#setMaxAge(long)}.
     */
    @Test
    public void setMaxAge() {
        final CacheService unit = unit();
        unit.setMaxAge(30);
        Assert.assertEquals(30, unit.getMaxAge());
    }
    
    /**
     * Tests {@link CacheService#store(java.io.Serializable, Object, long, TimeUnit)}
     * with a max age of 1 second and waits till the time is expired.
     * @throws InterruptedException if the Thread.sleep is interrupted
     */
    @Test
    public void testStoreWithMaxAge() throws InterruptedException {
        final int maxAge = 1;
        final TimeUnit timeUnit = TimeUnit.SECONDS;
        final Serializable key = 1;
        final CacheService unit = unit();
        
        unit.store(key, "TestEntry", maxAge, timeUnit);
        Thread.sleep(timeUnit.toMillis(maxAge) + 1000);
        Assert.assertNull("should be expired, but is not", unit.read(key));
    }

    /**
     * Tests {@link CacheService#store(Serializable, Object, long, TimeUnit)} with a negative max age.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testStoreMaxAgeNegative() {
        unit().store(1, "test", -1, TimeUnit.SECONDS);
    }

    /**
     * Tests {@link CacheService#store(Serializable, Object, long, TimeUnit)} with a null key.
     * Expects a NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testStoreMaxAgeKeyNull() {
        unit().store(null, "test", 10, TimeUnit.SECONDS);
    }
    
    /**
     * Tests {@link CacheService#store(Serializable, Object, long, TimeUnit)} with a null TimeUnit.
     * Expects a NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testStoreMaxAgeTimeUnitNull() {
        unit().store(1, "test", 10, null);
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
            Assert.assertTrue(unit.read(j) == null);
        }
    }
}
