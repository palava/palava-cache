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
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;

/**
 * Binds {@link BackedComputingCacheService}s.
 *
 * @author Willi Schoenborn
 */
public final class BackedComputingCacheServiceModule implements Module {
    
    @Override
    public void configure(Binder binder) {
        binder.bind(ComputingCacheService.class).to(BackedComputingCacheService.class).in(Singleton.class);
    }

    /**
     * Creates a module which binds a {@link BackedComputingCacheService} using
     * the given binding annotation and the key to the backing {@link CacheService}.
     * 
     * @since 2.4
     * @param annotation the binding annotation
     * @param serviceKey the key of the backing {@link CacheService}
     * @return a module binding a {@link BackedComputingCacheService} using the specified binding
     *         annotation
     */
    public static Module annotatedWithAndBackedBy(Class<? extends Annotation> annotation, 
        Key<? extends CacheService> serviceKey) {
        
        return new AnnotatedModule(annotation, serviceKey);
    }
    
    /**
     * Private module implementation used by
     * {@link BackedComputingCacheServiceModule#annotatedWithAndBackedBy(Class, Key)}.
     * 
     * @since 2.4
     * @author Willi Schoenborn
     */
    private static final class AnnotatedModule extends PrivateModule {
        
        private final Class<? extends Annotation> annotation;
        private final Key<? extends CacheService> serviceKey;
        
        public AnnotatedModule(Class<? extends Annotation> annotation, Key<? extends CacheService> serviceKey) {
            this.annotation = Preconditions.checkNotNull(annotation, "Annotation");
            this.serviceKey = Preconditions.checkNotNull(serviceKey, "ServiceKey");
        }

        @Override
        protected void configure() {
            bind(CacheService.class).to(serviceKey);
            bind(ComputingCacheService.class).annotatedWith(annotation).
                to(BackedComputingCacheService.class).in(Singleton.class);
            
            expose(ComputingCacheService.class).annotatedWith(annotation);
        }
        
    }
    
}
