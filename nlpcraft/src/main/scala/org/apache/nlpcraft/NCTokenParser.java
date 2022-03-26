/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft;

import java.util.List;

/**
 * A tokenizer that splits a text into the list of {@link NCToken tokens}.
 * <p>
 * See {@link NCPipeline} for documentation on the token parser place
 * in the overall processing pipeline.
 *
 * @see NCToken
 * @see NCTokenEnricher
 * @see NCTokenValidator
 * @see NCPipeline
 */
public interface NCTokenParser {
    /**
     * Splits given text into list of tokens. Can return an empty list but never {@code null}.
     *
     * @param text A text to split into tokens. Can be empty but never {@code null}.
     * @return List of tokens. Can be empty but never {@code null}.
     */
    List<NCToken> tokenize(String text);
}
