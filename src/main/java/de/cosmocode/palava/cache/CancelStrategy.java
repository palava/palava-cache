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

import javax.annotation.Nullable;

/**
 * A strategy pattern do specify the way {@link ComputingCacheService}s
 * handle cancelling of running computations.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
public interface CancelStrategy {

    /**
     * Cancels the given future.
     * 
     * @since 2.4
     * @param future the future to be cancelled (may be null)
     */
    void cancel(@Nullable Future<?> future);
    
    /**
     * Handles the occured {@link InterruptedException} in either
     * throwing another exception or returning a result
     * which will be passed to the caller of {@link ComputingCacheService#read(Serializable)}.
     * 
     * @since 2.4
     * @param <T> the generic result type
     * @param e the exception to be handled
     * @return the result
     */
    <T> T handle(InterruptedException e);
    
}
