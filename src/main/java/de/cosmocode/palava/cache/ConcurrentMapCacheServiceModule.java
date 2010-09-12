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

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;

/**
 * Binds {@link CacheService} to a noop implementation.
 *
 * @since 2.3
 * @author Willi Schoenborn
 */
public final class ConcurrentMapCacheServiceModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(CacheService.class).to(NoCacheService.class).in(Singleton.class);
    }

    /**
     * Creates a module which can be used to bind a noop {@link CacheService} implementation
     * using a custom binding annotation.
     * 
     * @since 2.3
     * @param annotation the binding annotation
     * @return a module which binds a noop {@link CacheService} using the specified binding annotation
     * @throws NullPointerException if annotation is null
     */
    public static Module annotatedWith(Class<? extends Annotation> annotation) {
        return new AnnotatedNoCacheModule(annotation);
    }
    
    /**
     * Private {@link Module} implementation which binds {@link NoCacheService} to an
     * annotated {@link CacheService}.
     *
     * @since 2.3
     * @author Willi Schoenborn
     */
    private static final class AnnotatedNoCacheModule implements Module {
        
        private final Class<? extends Annotation> annotation;

        public AnnotatedNoCacheModule(Class<? extends Annotation> annotation) {
            this.annotation = Preconditions.checkNotNull(annotation, "Annotation");
        }
        
        @Override
        public void configure(Binder binder) {
            binder.bind(CacheService.class).annotatedWith(annotation).to(NoCacheService.class).in(Singleton.class);
        }
        
    }
    
}
