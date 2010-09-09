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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Reusable constant {@link CancelStrategy} implementations. 
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
public enum CancelStrategies implements CancelStrategy {

    /**
     * Cancels {@link Future}s by permitting {@link Thread#interrupt()}. 
     * Handles {@link InterruptedException} by ignoring them and return {@code null}.
     */
    INTERRUPT {
        
        @Override
        protected void doCancel(Future<?> future) {
            future.cancel(true);
        }
        
        @Override
        public <T> T handle(InterruptedException e) {
            return null;
        }
        
    },
    
    /**
     * Cancels {@link Future}s by waiting for the computation to finish.
     * Handles {@link InterruptedException} by throwing an {@link AssertionError}.
     */
    WAIT {
        
        @Override
        protected void doCancel(Future<?> future) {
            future.cancel(false);
        }
        
        @Override
        public <T> T handle(InterruptedException e) {
            throw new AssertionError(e);
        }
        
    };
    
    @Override
    public final void cancel(@Nullable Future<?> future) {
        if (future == null || future.isDone() || future.isCancelled()) {
            return;
        } else {
            doCancel(future);
        }
    }
    
    /**
     * Cancel the given future.
     * 
     * @since 2.4
     * @param future the non-null future to be cancelled
     */
    protected abstract void doCancel(@Nonnull Future<?> future);
    
}
