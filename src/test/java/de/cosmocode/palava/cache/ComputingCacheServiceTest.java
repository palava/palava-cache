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

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.util.concurrent.Callables;

import de.cosmocode.commons.Throwables;
import de.cosmocode.junit.LoggingRunner;

/**
 * Abstract test case for {@link ComputingCacheService}s.
 *
 * @deprecated until the unit under test is removed
 * @since 2.4
 * @author Willi Schoenborn
 */
@Deprecated
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
        unit.computeAndStore("key", Callables.returning("computed-value"));
        Assert.assertEquals("computed-value", unit.read("key"));
    }
    
    /**
     * Tests whether {@link ComputingCacheService#read(Serializable)} waits on a concurrent
     * call to {@link ComputingCacheService#computeAndStore(Serializable, Callable)}.
     *
     * @since 2.4
     * @throws InterruptedException  should not happen
     */
    @Test
    public void waitOnRead() throws InterruptedException {
        final ComputingCacheService unit = unit();
        
        final Executor executor = Executors.newCachedThreadPool();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch all = new CountDownLatch(3);
        
        // compute and store
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    final String value = unit.computeAndStore("key", new Callable<String>() {

                        @Override
                        public String call() throws Exception {
                            start.countDown();
                            Thread.sleep(250);
                            return "computed-value";
                        }
                        
                    });
                    Assert.assertEquals("computed-value", value);
                } catch (ExecutionException e) {
                    Throwables.sneakyThrow(e);
                }
                all.countDown();
            }
            
        });
        
        start.await();
        
        final Runnable read = new Runnable() {
            
            @Override
            public void run() {
                final String value = unit.read("key");
                Assert.assertEquals("computed-value", value);
                all.countDown();
            }
            
        };

        executor.execute(read);
        executor.execute(read);
        
        Assert.assertTrue("Did not finish normally", all.await(500, TimeUnit.MILLISECONDS));
    }

    /**
     * Tests whether a faster computation passed to 
     * {@link ComputingCacheService#computeAndStore(Serializable, Callable)} forces older and still
     * running computations to return the most current value.
     *
     * @since 2.4
     * @throws InterruptedException  should not happen
     */
    @Test
    public void overwriteSlower() throws InterruptedException {
        final ComputingCacheService unit = unit();

        final Executor executor = Executors.newCachedThreadPool();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(1);
        final CountDownLatch all = new CountDownLatch(3);
        
        // compute and store slow computation
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    final String value = unit.computeAndStore("key", new Callable<String>() {
                        
                        @Override
                        public String call() throws Exception {
                            start.countDown();
                            Thread.sleep(250);
                            try {
                                return "old-value";
                            } finally {
                                end.countDown();
                            }
                        }
                        
                    });
                    Assert.assertEquals("new-value", value);
                } catch (ExecutionException e) {
                    Throwables.sneakyThrow(e);
                }
                all.countDown();
            }
            
        });
        
        start.await();
        
        // compute and store fast computation
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    final String value = unit.computeAndStore("key", new Callable<String>() {

                        @Override
                        public String call() throws Exception {
                            Thread.sleep(100);
                            return "new-value";
                        }
                        
                    });
                    Assert.assertEquals("new-value", value);
                } catch (ExecutionException e) {
                    Throwables.sneakyThrow(e);
                }
                all.countDown();
            }
            
        });
        
        // read
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                final String value = unit.read("key");
                Assert.assertEquals("new-value", value);
                all.countDown();
            }
            
        });
        
        Assert.assertTrue("Slow computation did not end", end.await(500, TimeUnit.MILLISECONDS));
        Assert.assertTrue("At least one computation did not end", all.await(500, TimeUnit.MILLISECONDS));
    }
    
    /**
     * Tests {@link ComputingCacheService#read(Serializable)} and {@link ComputingCacheService#remove(Serializable)}
     * while a computation in {@link ComputingCacheService#computeAndStore(Serializable, Callable)} is
     * currently running.
     *
     * @since 2.4
     * @throws InterruptedException  should not happen
     */
    @Test
    public void readAndRemoveWhileCompute() throws InterruptedException {
        final ComputingCacheService unit = unit();

        final Executor executor = Executors.newCachedThreadPool();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(1);
        final CountDownLatch all = new CountDownLatch(3);
        
        // compute and store
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    final String value = unit.computeAndStore("key", new Callable<String>() {
                        
                        @Override
                        public String call() throws Exception {
                            start.countDown();
                            Thread.sleep(250);
                            try {
                                return "computed-value";
                            } finally {
                                end.countDown();
                            }
                        }
                        
                    });
                    Assert.assertEquals("computed-value", value);
                } catch (ExecutionException e) {
                    Throwables.sneakyThrow(e);
                }
                all.countDown();
            }
            
        });
        
        start.await();
        
        // read
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                Assert.assertNull(unit.read("key"));
                all.countDown();
            }
            
        });
        
        // remove
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                Assert.assertNull(unit.remove("key"));
                all.countDown();
            }
            
        });

        Assert.assertTrue("Slow computation did not end", end.await(500, TimeUnit.MILLISECONDS));
        Assert.assertTrue("At least one computation did not end", all.await(500, TimeUnit.MILLISECONDS));
    }
    
    /**
     * Tests whether {@link ComputingCacheService#read(Serializable)} returns the pre-computed value
     * while a new computation is running.
     *
     * @since 2.4
     * @throws InterruptedException should not happen 
     */
    @Test
    public void readOldValueWhileComputeAndStore() throws InterruptedException {
        final ComputingCacheService unit = unit();
        
        final Executor executor = Executors.newCachedThreadPool();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(1);
        final CountDownLatch all = new CountDownLatch(3);

        // store
        unit.store("key", "old-value");
        
        // compute and store
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    final String value = unit.computeAndStore("key", new Callable<String>() {
                        
                        @Override
                        public String call() throws Exception {
                            start.countDown();
                            Thread.sleep(250);
                            try {
                                return "new-value";
                            } finally {
                                end.countDown();
                            }
                        }
                        
                    });
                    Assert.assertEquals("new-value", value);
                } catch (ExecutionException e) {
                    Throwables.sneakyThrow(e);
                }
                all.countDown();
            }
            
        });
        
        start.await();

        // read old value
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                Assert.assertEquals("old-value", unit.read("key"));
                all.countDown();
            }
            
        });
        
        end.await();
        
        // read new value
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                Assert.assertEquals("new-value", unit.read("key"));
                all.countDown();
            }
            
        });
        
        Assert.assertTrue("At least one computation did not end", all.await(500, TimeUnit.MILLISECONDS));
    }
    
    /**
     * Tests whether unchecked {@link Exception} are passed to blocking calls to
     * {@link ComputingCacheService#read(Serializable)}.
     *
     * @since 2.4
     * @throws InterruptedException should not happen 
     */
    @Test
    public void uncheckedExceptionWhileRead() throws InterruptedException {
        final ComputingCacheService unit = unit();
        
        final Executor executor = Executors.newCachedThreadPool();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(1);
        final CountDownLatch all = new CountDownLatch(2);
        
        final RuntimeException expected = new IllegalArgumentException();
        
        // fail during compute and store
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    unit.computeAndStore("key", new Callable<String>() {
                       
                        @Override
                        public String call() throws Exception {
                            start.countDown();
                            Thread.sleep(250);
                            try {
                                throw expected;
                            } finally {
                                end.countDown();
                            }
                        }
                        
                    });
                } catch (ExecutionException e) {
                    Assert.assertSame(expected, e.getCause());
                    all.countDown();
                }
            }
            
        });
        
        start.await();
        
        // read
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    unit.read("key");
                /* CHECKSTYLE:OFF */
                } catch (RuntimeException e) {
                /* CHECKSTYLE:ON */
                    Assert.assertSame(expected, e);
                    all.countDown();
                }
            }
            
        });

        Assert.assertTrue("Slow computation did not end", end.await(500, TimeUnit.MILLISECONDS));
        Assert.assertTrue("At least one computation did not end", all.await(500, TimeUnit.MILLISECONDS));
    }
    
    /**
     * Tests whether checked {@link Exception} are passed to blocking calls to
     * {@link ComputingCacheService#read(Serializable)}.
     *
     * @since 2.4
     * @throws InterruptedException should not happen 
     */
    @Test
    public void checkedExceptionWhileRead() throws InterruptedException {
        final ComputingCacheService unit = unit();
        
        final Executor executor = Executors.newCachedThreadPool();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(1);
        final CountDownLatch all = new CountDownLatch(2);
        
        final Exception expected = new IOException();
        
        // fail during compute and store
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    unit.computeAndStore("key", new Callable<String>() {
                       
                        @Override
                        public String call() throws Exception {
                            start.countDown();
                            Thread.sleep(250);
                            try {
                                throw expected;
                            } finally {
                                end.countDown();
                            }
                        }
                        
                    });
                } catch (ExecutionException e) {
                    Assert.assertSame(expected, e.getCause());
                    all.countDown();
                }
            }
            
        });
        
        start.await();
        
        // read
        executor.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    unit.read("key");
                /* CHECKSTYLE:OFF */
                } catch (Exception e) {
                /* CHECKSTYLE:ON */
                    Assert.assertSame(expected, e.getCause());
                    all.countDown();
                }
            }
            
        });

        Assert.assertTrue("Slow computation did not end", end.await(500, TimeUnit.MILLISECONDS));
        Assert.assertTrue("At least one computation did not end", all.await(500, TimeUnit.MILLISECONDS));
    }
    
}
