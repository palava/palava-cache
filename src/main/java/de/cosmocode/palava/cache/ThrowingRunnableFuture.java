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

import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;

/**
 * A different version of {@link RunnableFuture} which allows throwing checked
 * exceptions.
 *
 * @since 2.4
 * @author Willi Schoenborn 
 * @param <T> the target type being returned by {@link #get()}
 * @param <X> the exception type
 */
interface ThrowingRunnableFuture<T, X extends Throwable> extends Future<T> {

    /**
     * Runs this future.
     * 
     * @see RunnableFuture#run()
     * @since 2.4
     * @throws X if running failed
     */
    void run() throws X;
    
}
