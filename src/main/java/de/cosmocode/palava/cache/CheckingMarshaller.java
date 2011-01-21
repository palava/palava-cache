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

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;

import de.cosmocode.commons.Bijection;

/**
 * A {@link Marshaller} implementstion which just checks the incoming type
 * for being {@link Serializable}.
 *
 * @since 3.0
 * @author Willi Schoenborn
 */
@Beta
public enum CheckingMarshaller implements Marshaller {

    INSTANCE;
    
    @Override
    public Serializable apply(Object input) {
        Preconditions.checkArgument(input instanceof Serializable, "%s is not Serializable", input);
        return Serializable.class.cast(input);
    }
    
    @Override
    public Bijection<Serializable, Object> inverse() {
        return Inverse.MARSHALLER;
    }
    
    /**
     * The inverse implementation of {@link CheckingMarshaller}.
     *
     * @since 3.0
     * @author Willi Schoenborn
     */
    private enum Inverse implements Bijection<Serializable, Object> {
        
        MARSHALLER;
        
        @Override
        public Object apply(Serializable input) {
            return input;
        }
        
        @Override
        public Bijection<Object, Serializable> inverse() {
            return CheckingMarshaller.INSTANCE;
        }
        
    }
    
}
