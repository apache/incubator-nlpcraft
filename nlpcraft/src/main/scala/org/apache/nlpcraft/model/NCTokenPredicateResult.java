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

package org.apache.nlpcraft.model;

/**
 * Result value of user-defined token predicate. Token predicates can be used in intent and synonym DSL.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/intent-matching.html">Intent Matching</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 *
 * @see NCTokenPredicateContext
 */
public class NCTokenPredicateResult {
    private final boolean result;
    private final boolean wasTokenUsed;

    /**
     * Creates token predicate result.
     *
     * @param result Token predicate result.
     * @param wasTokenUsed Whether or not a token was used by this predicate (if result is {@code true}).
     */
    NCTokenPredicateResult(boolean result, boolean wasTokenUsed) {
        this.result = result;
        this.wasTokenUsed = wasTokenUsed;
    }

    /**
     * Gets result of this predicate.
     *
     * @return Predicate result.
     */
    boolean getResult() {
        return result;
    }

    /**
     * Whether or not a token was used by this predicate (if result is {@code true}).
     *
     * @return {@code true} if token was used by this predicate, {@code false} otherwise.
     */
    boolean wasTokenUsed() {
        return wasTokenUsed;
    }
}
