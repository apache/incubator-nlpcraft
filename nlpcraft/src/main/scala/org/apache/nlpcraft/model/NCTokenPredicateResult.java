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
 * Result value of user-defined token predicate. Token predicates can be used in IDL.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/intent-matching.html">Intent Matching</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 *
 * @see NCTokenPredicateContext
 */
public class NCTokenPredicateResult {
    private final boolean res;
    private final int tokUses;

    /**
     * Creates token predicate result.
     *
     * @param res Token predicate result.
     * @param tokUses How many times a token was used to match this predicate (if {@link #getResult() result} is {@code true}).
     *      The more times a token was used the "stronger" the this match will be when used by intent solver.
     */
    public NCTokenPredicateResult(boolean res, int tokUses) {
        this.res = res;
        this.tokUses = tokUses;
    }

    /**
     * Gets result of this predicate.
     *
     * @return Predicate result.
     */
    public boolean getResult() {
        return res;
    }

    /**
     * Gets how many times a token was used to match this predicate (if {@link #getResult() result} is {@code true}).
     *
     * @return Number of times a token was used to match this term.
     */
    public int getTokenUses() {
        return tokUses;
    }
}
