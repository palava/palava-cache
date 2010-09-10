package de.cosmocode.palava.cache;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.util.concurrent.Callables;

/**
 * Abstract test case for {@link ComputingCacheService}s.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
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
        unit.computeAndStore("key", Callables.returning("value"));
        Assert.assertSame("value", unit.read("key"));
    }
    
}
