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
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.util.concurrent.Callables;

import de.cosmocode.commons.Throwables;
import de.cosmocode.junit.LoggingRunner;

/**
 * Abstract test case for {@link ComputingCacheService}s.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
@RunWith(LoggingRunner.class)
public abstract class ComputingCacheServiceTest extends CacheServiceTest {

    @Override
    public abstract ComputingCacheService unit();

    /**
     * Tests {@link ComputingCacheService#computeAndStore(Serializable, Callable)}.
     * 
     * @throws ExecutionException should not happen
     * @throws CancellationException should not happen
     */
    @Test
    public void simpleComputeAndStore() throws CancellationException, ExecutionException {
        final ComputingCacheService unit = unit();
        unit.computeAndStore("computing", Callables.returning("computed-value"));
        Assert.assertSame("computed-value", unit.read("computing"));
    }
    
}
