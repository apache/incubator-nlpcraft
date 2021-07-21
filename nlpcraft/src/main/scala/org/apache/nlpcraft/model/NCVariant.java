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
 * A parsing variant is a list of tokens representing one possible parsing variant of the user input.
 * <p>
 * Note that a given user input can have one or more possible different parsing variants. Depending on model
 * configuration a user input can produce hundreds or even thousands of parsing variants.
 *
 * @see NCModel#onParsedVariant(NCVariant)
 * @see NCContext#getVariants()
 */
public interface NCVariant extends List<NCToken>, NCMetadata {
    /**
     * Utility method that returns all non-freeword tokens. It's equivalent to:
     * <pre class="brush: java">
     *     return stream().filter(tok -&gt; !tok.isFreeWord() && !tok.isStopWord()).collect(Collectors.toList());
     * </pre>
     *
     * @return All non-freeword tokens.
     * @see NCToken#isFreeWord()
     */
    default List<NCToken> getMatchedTokens() {
        return stream().filter(tok -> !tok.isFreeWord() && !tok.isStopWord()).collect(Collectors.toList());
    }

    /**
     * Utility method that returns all freeword tokens. It's equivalent to:
     * <pre class="brush: java">
     *     return stream().filter(NCToken::isFreeWord).collect(Collectors.toList());
     * </pre>
     *
     * @return All freeword tokens.
     * @see NCToken#isFreeWord()
     */
    default List<NCToken> getFreeTokens() {
        return stream().filter(NCToken::isFreeWord).collect(Collectors.toList());
    }

    /**
     * Utility method that returns all abstract tokens. It's equivalent to:
     * <pre class="brush: java">
     *     return stream().filter(NCToken::isAbstract).collect(Collectors.toList());
     * </pre>
     *
     * @return All abstract tokens.
     * @see NCToken#isAbstract() ()
     */
    default List<NCToken> getAbstractTokens() {
        return stream().filter(NCToken::isAbstract).collect(Collectors.toList());
    }

    /**
     * Utility method that returns all stop word tokens. It's equivalent to:
     * <pre class="brush: java">
     *     return stream().filter(NCToken::isStopWord).collect(Collectors.toList());
     * </pre>
     *
     * @return All stop word tokens.
     * @see NCToken#isAbstract() ()
     */
    default List<NCToken> getStopWordTokens() {
        return stream().filter(NCToken::isStopWord).collect(Collectors.toList());
    }

    /**
     * Utility method that returns all user-defined tokens. It's equivalent to:
     * <pre class="brush: java">
     *     return stream().filter(NCToken::isUserDefined).collect(Collectors.toList());
     * </pre>
     *
     * @return All user-defined tokens.
     * @see NCToken#isUserDefined() ()
     */
    default List<NCToken> getUserDefinedTokens() {
        return stream().filter(NCToken::isUserDefined).collect(Collectors.toList());
    }
}
