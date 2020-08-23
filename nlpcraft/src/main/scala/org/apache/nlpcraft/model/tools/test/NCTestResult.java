/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.model.tools.test;

import java.util.Optional;

/**
 * Result of the test sentence processing.
 */
public interface NCTestResult {
    /**
     * Gets test sentence text.
     *
     * @return Test sentence text.
     */
    String getText();
    
    /**
     * Gets total sentence processing time in milliseconds.
     *
     * @return Processing time in milliseconds.
     */
    long getProcessingTime();
    
    /**
     * Gets model ID the test sentence was sent with.
     *
     * @return Model ID.
     */
    String getModelId();
    
    /**
     * Gets optional execution result. Only provided if processing succeeded.
     *
     * @return Optional execution result.
     * @see #isFailed()
     * @see #isOk()
     */
    Optional<String> getResult();
    
    /**
     * Gets optional execution result type. Only provided if processing succeeded.
     *
     * @return Optional execution result type.
     * @see #isFailed()
     * @see #isOk()
     */
    Optional<String> getResultType();
    
    /**
     * Gets optional execution error. Only provided if processing failed.
     *
     * @return Optional execution error.
     * @see #isFailed()
     * @see #isOk()
     */
    Optional<String> getResultError();

    /**
     * Tests whether or not this result corresponds to a failed execution. This is identical to:
     * <pre class="brush: java">
     *      return getResultError().isPresent();
     * </pre>
     *
     * @return {@code true} if result corresponds to a failed execution, {@code false} otherwise.
     */
    default boolean isFailed() {
        return getResultError().isPresent();
    }

    /**
     * Tests whether or not this result corresponds to a successful execution. This is identical to:
     * <pre class="brush: java">
     *      return getResult().isPresent() &amp;&amp; getResultType().isPresent();
     * </pre>
     *
     * @return {@code true} if result corresponds to a successful execution, {@code false} otherwise.
     */
    default boolean isOk() {
        return getResult().isPresent() && getResultType().isPresent();
    }

    // TODO:
    String getIntentId();
}
