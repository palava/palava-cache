/**
 * palava - a java-php-bridge
 * Copyright (C) 2007  CosmoCode GmbH
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract test-class for CacheService.
 * 
 * @author Markus Baumann
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
     * Testing Store-Function.
     * expecting NullPointerException
     */

    @Test(expected = NullPointerException.class)
    public void testStoreNullPointerException() {
        cacheServiceObj.store(null, "TestEntry");
    }

    /**
     * Testing Store-Function.
     */

    @Test
    public void testStoreAndRead() {
        cacheServiceObj.store(1 , "TestEntry");
        Assert.assertTrue("TestEntry".equals(cacheServiceObj.read(1)));
    }

    /**
     * Testing Remove-Function.
     */

    @Test
    public void testRemove() {
        cacheServiceObj.store(1 , "TestEntry");
        Assert.assertTrue("TestEntry".equals(cacheServiceObj.remove(1)));
        Assert.assertTrue(cacheServiceObj.read(1) == null);
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
