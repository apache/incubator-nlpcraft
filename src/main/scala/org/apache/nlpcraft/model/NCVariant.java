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

import java.util.*;
import java.util.stream.*;

/**
 * A list of tokens representing one possible parsing variant of the user input.
 * <p>
 * Note that a given user input can have one or more possible different parsing variants. Depending on model
 * configuration a user input can produce hundreds or even thousands of parsing variants.
 *
 * @see NCModel#onParsedVariant(NCVariant)
 * @see NCContext#getVariants()
 */
public interface NCVariant extends List<NCToken> {
    /**
     * Utility method that returns all non-freeword tokens. It's equivalent to:
     * <pre class="brush: java">
     *     return stream().filter(tok -> !tok.isFreeWord()).collect(Collectors.toList());
     * </pre>
     *
     * @return All non-freeword tokens.
     */
    default List<NCToken> getMatchedTokens() {
        return stream().filter(tok -> !tok.isFreeWord()).collect(Collectors.toList());
    }
}
