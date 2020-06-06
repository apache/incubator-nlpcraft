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

package org.apache.nlpcraft.model.intent.utils;

import org.apache.nlpcraft.model.*;
import java.io.*;
import java.util.function.*;

/**
 * Internal DSL intent term representation.
 */
public class NCDslTerm implements Serializable {
    // Term's predicate.
    final private Function<NCToken, Boolean> pred;

    // Terms quantifiers.
    final private int min, max;

    // Optional term ID.
    final private String id;

    /**
     * Creates new term with given predicate and quantifiers.
     *
     * @param id Optional term ID.
     * @param pred Token predicate.
     * @param min Minimum quantifier for the predicate (inclusive).
     * @param max Maximum quantifier for the predicate (inclusive).
     */
    public NCDslTerm(String id, Function<NCToken, Boolean> pred, int min, int max) {
        if (pred == null)
            throw new IllegalArgumentException("Intent DSL term predicate cannot be null.");
        if (min < 0 || min > max)
            throw new IllegalArgumentException(String.format(
                "Invalid intent DSL term min quantifiers: %d (must be min >= 0 && min <= max).", min));
        if (max < 1)
            throw new IllegalArgumentException(String.format(
                "Invalid intent DSL term max quantifiers: %d (must be max >= 1).", max));

        this.id = id;
        this.pred = pred;
        this.min = min;
        this.max = max;
    }

    /**
     * Gets term's predicate.
     *
     * @return Term's predicate.
     */
    public Function<NCToken, Boolean> getPredicate() {
        return pred;
    }

    /**
     * Gets optional term ID.
     *
     * @return Term ID or {@code null} if not specified.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets min quantifier for this term.
     *
     * @return Min quantifier for this term.
     */
    public int getMin() {
        return min;
    }

    /**
     * Gets max quantifier for this term.
     *
     * @return Max quantifier for this term.
     */
    public int getMax() {
        return max;
    }

    @Override
    public String toString() {
        String minMax;

        if (min == 1 && max == 1)
            minMax = "";
        else if (min == 0 && max == 1)
            minMax = "?";
        else if (min == 0 && max == Integer.MAX_VALUE)
            minMax = "*";
        else if (min == 1 && max == Integer.MAX_VALUE)
            minMax = "+";
        else
            minMax = String.format("[%d,%d]", min, max);

        if (id == null)
            return String.format("term={%s}%s", pred, minMax);
        else
            return String.format("term(%s)={%s}%s", id, pred, minMax);
    }
}
