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

import de.cosmocode.commons.Bijection;

/**
 * A marshaller can be used to convert cache values.
 *
 * @since 3.0
 * @author Willi Schoenborn
 */
@Beta
public interface Marshaller extends Bijection<Object, Serializable> {

}
