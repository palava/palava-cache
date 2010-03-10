/**
 * palava - a java-php-bridge
 * Copyright (C) 2007-2010  CosmoCode GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.cosmocode.palava.services.cache;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract test-class for CacheService.
 * 
 * @author Markus Baumann
 * @author Oliver Lorenz (everything with maxAge)
 *
 */
public abstract class CacheServiceTest {

    private CacheService cacheServiceObj;
    
    /**
     * Creates new instance of CacheService.
     * @return instance of CacheService 
     */
    protected abstract CacheService create();

    /**
     * Create instance before testing.
     */
    @Before
    public void before() {
        cacheServiceObj = create();
    }
    
    /**
     * Tests {@link CacheService#setMaxAge(long, TimeUnit)} with a negative value.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setMaxAgeTimeUnitNegative() {
        cacheServiceObj.setMaxAge(-1, TimeUnit.SECONDS);
    }
    
    /**
     * Tests {@link CacheService#setMaxAge(long, TimeUnit)} with a TimeUnit of null.
     */
    @Test(expected = NullPointerException.class)
    public void setMaxAgeTimeUnitNull() {
        cacheServiceObj.setMaxAge(10, null);
    }
    
    /**
     * Tests {@link CacheService#setMaxAge(long, TimeUnit)}.
     */
    @Test
    public void setMaxAgeTimeUnit() {
        final TimeUnit unit = TimeUnit.HOURS;
        cacheServiceObj.setMaxAge(5, unit);
        Assert.assertEquals(5, cacheServiceObj.getMaxAge(unit));
    }
    
    /**
     * Tests {@link CacheService#setMaxAge(long)} with a negative value.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setMaxAgeNegative() {
        cacheServiceObj.setMaxAge(-1);
    }
    
    /**
     * Tests {@link CacheService#setMaxAge(long)}.
     */
    @Test
    public void setMaxAge() {
        cacheServiceObj.setMaxAge(30);
        Assert.assertEquals(30, cacheServiceObj.getMaxAge());
    }
    
    /**
     * Tests {@link CacheService#store(java.io.Serializable, Object, long, TimeUnit)}
     * with a max age of 1 second and waits till the time is expired.
     * @throws InterruptedException if the Thread.sleep is interrupted
     */
    @Test
    public void testStoreWithMaxAge() throws InterruptedException {
        final int maxAge = 1;
        final TimeUnit unit = TimeUnit.SECONDS;
        final Serializable key = 1;
        
        cacheServiceObj.store(key, "TestEntry", maxAge, unit);
        Thread.sleep(unit.toMillis(maxAge) + 1000);
        Assert.assertNull("should be expired, but is not", cacheServiceObj.read(key));
    }

    /**
     * Tests {@link CacheService#store(Serializable, Object, long, TimeUnit)} with a negative max age.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testStoreMaxAgeNegative() {
        cacheServiceObj.store(1, "test", -1, TimeUnit.SECONDS);
    }

    /**
     * Tests {@link CacheService#store(Serializable, Object, long, TimeUnit)} with a null key.
     * Expects a NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testStoreMaxAgeKeyNull() {
        cacheServiceObj.store(null, "test", 10, TimeUnit.SECONDS);
    }
    
    /**
     * Tests {@link CacheService#store(Serializable, Object, long, TimeUnit)} with a null TimeUnit.
     * Expects a NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testStoreMaxAgeTimeUnitNull() {
        cacheServiceObj.store(1, "test", 10, null);
    }
    
    /**
     * Testing Store-Function.
     */
    @Test
    public void testStoreAndRead() {
        cacheServiceObj.store(1 , "TestEntry");
        Assert.assertTrue("TestEntry".equals(cacheServiceObj.read(1)));
        Assert.assertNull(cacheServiceObj.read("null"));
    }

    /**
     * Testing Store-Function.
     * expecting NullPointerException
     */
    @Test(expected = NullPointerException.class)
    public void testStoreNullPointerException() {
        cacheServiceObj.store(null, "TestEntry");
    }
    
    /**
     * Testing Read-Function.
     * expecting NullPointerException
     */
    @Test(expected = NullPointerException.class)
    public void testReadNullPointerException() {
        cacheServiceObj.read(null);
    }
    
    /**
     * Testing Remove-Function.
     */
    @Test
    public void testRemove() {
        cacheServiceObj.store(1 , "TestEntry");
        Assert.assertTrue("TestEntry".equals(cacheServiceObj.remove(1)));
        Assert.assertTrue(cacheServiceObj.read(1) == null);
        Assert.assertNull(cacheServiceObj.remove("null"));
    }
    
    /**
     * Testing Remove-Function.
     * Expecting NullPointerException
     */
    @Test(expected = NullPointerException.class)
    public void testRemoveNullPointerException() {
        cacheServiceObj.remove(null);
    }

    /**
     * Testing the Clear-Function.
     */
    @Test
    public void testClear() {
        for (int i = 0; i < 10; i++) {
            cacheServiceObj.store(i , "TestEntry");
        }
        cacheServiceObj.clear();
        for (int j = 0; j < 10; j++) {
            Assert.assertTrue(cacheServiceObj.read(j) == null);
        }
    }
}
