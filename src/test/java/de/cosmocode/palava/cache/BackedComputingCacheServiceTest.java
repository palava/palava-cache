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

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import de.cosmocode.palava.concurrent.DefaultThreadProviderModule;
import de.cosmocode.palava.core.DefaultRegistryModule;
import de.cosmocode.palava.core.Framework;
import de.cosmocode.palava.core.Palava;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import de.cosmocode.palava.core.lifecycle.LifecycleModule;
import de.cosmocode.palava.core.lifecycle.Startable;
import de.cosmocode.palava.cron.CronSchedulerModule;
import de.cosmocode.palava.cron.DefaultCronServiceModule;
import de.cosmocode.palava.jmx.FakeMBeanServerModule;

/**
 * Tests {@link BackedComputingCacheService}.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
public final class BackedComputingCacheServiceTest extends ComputingCacheServiceTest implements Startable {

    private final Framework framework = Palava.newFramework(new AbstractModule() {
        
        @Override
        protected void configure() {
            install(new LifecycleModule());
            install(new DefaultRegistryModule());
            install(new DefaultThreadProviderModule());
            install(new FakeMBeanServerModule());
            install(new DefaultCronServiceModule());
            install(new CronSchedulerModule());
            bindConstant().annotatedWith(Names.named("executors.named.cron.minPoolSize")).to(5);
            bindConstant().annotatedWith(Names.named("executors.named.cron.shutdownTimeout")).to(30L);
            bindConstant().annotatedWith(Names.named("executors.named.cron.shutdownTimeoutUnit")).to(TimeUnit.SECONDS);
            install(ConcurrentMapCacheServiceModule.annotatedWith(Real.class));
            install(BackedComputingCacheServiceModule.backedBy(Real.class));
        }
        
    }, new Properties());
    
    @Before
    @Override
    public void start() throws LifecycleException {
        framework.start();
    }
    
    @Override
    public ComputingCacheService unit() {
        return framework.getInstance(ComputingCacheService.class);
    }

    @After
    @Override
    public void stop() throws LifecycleException {
        framework.stop();
    }

}
