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

import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import de.cosmocode.palava.core.inject.AbstractRebindModule;
import de.cosmocode.palava.core.inject.Config;
import de.cosmocode.palava.core.inject.RebindModule;

/**
 * Binds {@link CacheService} to {@link ConcurrentMapCacheService}.
 *
 * @since 2.3
 * @author Willi Schoenborn
 */
public final class ConcurrentMapCacheServiceModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(CacheService.class).to(ConcurrentMapCacheService.class).in(Singleton.class);
    }

    /**
     * Creates a module which can be used to bind {@link CacheService} implementation
     * backed by a {@link ConcurrentMap} using a custom binding annotation without 
     * rebinding optional configuration dependencies.
     * 
     * @since 2.3
     * @param annotation the binding annotation
     * @return a module which binds a concurrent map {@link CacheService} using the specified binding annotation
     * @throws NullPointerException if annotation is null
     */
    public static Module annotatedWith(Class<? extends Annotation> annotation) {
        return new AnnotatedModule(annotation);
    }
    
    /**
     * Private {@link Module} implementation which binds {@link ConcurrentMapCacheService} to an
     * annotated {@link CacheService} without rebinding optional configuration dependencies.
     *
     * @since 2.3
     * @author Willi Schoenborn
     */
    private static final class AnnotatedModule implements Module {
        
        private final Class<? extends Annotation> annotation;

        public AnnotatedModule(Class<? extends Annotation> annotation) {
            this.annotation = Preconditions.checkNotNull(annotation, "Annotation");
        }
        
        @Override
        public void configure(Binder binder) {
            binder.bind(CacheService.class).annotatedWith(annotation).
                to(ConcurrentMapCacheService.class).in(Singleton.class);
        }
        
    }

    /**
     * Creates a module which can be used to bind {@link CacheService} implementation
     * backed by a {@link ConcurrentMap} using a custom binding annotation including
     * a rebinding of the optional configuration depdendencies.
     * 
     * @since 3.0
     * @param annotation the binding annotation
     * @param prefix the config prefix
     * @return a module which binds a concurrent map {@link CacheService} using the specified binding annotation
     * @throws NullPointerException if annotation is null
     */
    public static RebindModule annotatedWith(Class<? extends Annotation> annotation, String prefix) {
        return new AnnotatedPrefixedModule(annotation, prefix);
    }
    
    /**
     * A {@link RebindModule} used by {@link ConcurrentMapCacheServiceModule#annotatedWith(Class, String)}.
     *
     * @since 3.0
     * @author Willi Schoenborn
     */
    private static final class AnnotatedPrefixedModule extends AbstractRebindModule {

        private final Class<? extends Annotation> annotation;
        private final Config config;
        
        public AnnotatedPrefixedModule(Class<? extends Annotation> annotation, String prefix) {
            this.annotation = Preconditions.checkNotNull(annotation, "Annotation");
            this.config = new Config(prefix);
        }
        
        @Override
        protected void configuration() {
            // no non-optional configuration required
        }
        
        @Override
        protected void optionals() {
            bind(ReferenceMode.class).annotatedWith(Names.named(ConcurrentMapCacheServiceConfig.KEY_MODE)).
                to(Key.get(ReferenceMode.class, Names.named(
                config.prefixed(ConcurrentMapCacheServiceConfig.KEY_MODE))));
            
            bind(ReferenceMode.class).annotatedWith(Names.named(ConcurrentMapCacheServiceConfig.VALUE_MODE)).
                to(Key.get(ReferenceMode.class, Names.named(
                config.prefixed(ConcurrentMapCacheServiceConfig.VALUE_MODE))));
            
            bind(int.class).annotatedWith(Names.named(ConcurrentMapCacheServiceConfig.MAXIMUM_SIZE)).
                to(Key.get(int.class, Names.named(
                config.prefixed(ConcurrentMapCacheServiceConfig.MAXIMUM_SIZE))));
            
            bind(int.class).annotatedWith(Names.named(ConcurrentMapCacheServiceConfig.INITIAL_CAPACITY)).
                to(Key.get(int.class, Names.named(
                config.prefixed(ConcurrentMapCacheServiceConfig.INITIAL_CAPACITY))));
            
            bind(int.class).annotatedWith(Names.named(ConcurrentMapCacheServiceConfig.CONCURRENCY_LEVEL)).
                to(Key.get(int.class, Names.named(
                config.prefixed(ConcurrentMapCacheServiceConfig.CONCURRENCY_LEVEL))));
            
            bind(String.class).annotatedWith(Names.named(ConcurrentMapCacheServiceConfig.CRON_EXPRESSION)).
                to(Key.get(String.class, Names.named(
                config.prefixed(ConcurrentMapCacheServiceConfig.CRON_EXPRESSION))));
        }
        
        @Override
        protected void bindings() {
            bind(CacheService.class).annotatedWith(annotation).
                to(ConcurrentMapCacheService.class).in(Singleton.class);
        }
        
        @Override
        protected void expose() {
            expose(CacheService.class).annotatedWith(annotation);
        }
        
    }
    
}
