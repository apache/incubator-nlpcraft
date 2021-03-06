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

/**
 * An item of the dialog flow. Dialog flow is a chronologically ordered list of dialog flow
 * items. Each item represents a snapshot of winning intent's match and its associated data. An instance
 * of this interface is passed into a custom user-defined dialog flow match method.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/intent-matching.html">Intent Matching</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 */
public interface NCDialogFlowItem {
    /**
     * Gets ID of the matched intent.
     *
     * @return ID of the matched intent.
     */
    String getIntentId();

    /**
     * Gets a subset of tokens representing matched intent. This subset is grouped by the matched terms
     * where a {@code null} sub-list defines an optional term. Order and index of sub-lists corresponds
     * to the order and index of terms in the matching intent. Number of sub-lists will always be the same
     * as the number of terms in the matched intent.
     * <p>
     * Note that unlike {@link #getVariant()} method
     * this method returns only subset of the tokens that were part of the matched intent. Specifically, it will
     * not return tokens for free words, stopwords or unmatched ("dangling") tokens.
     * <p>
     * Consider using {@link NCIntentTerm} annotation instead for simpler access to intent tokens.
     *
     * @return List of list of tokens representing matched intent.
     * @see #getVariant()
     * @see NCIntentTerm
     */
    List<List<NCToken>> getIntentTokens();

    /**
     * Gets tokens for given term. This is a companion method for {@link #getIntentTokens()}.
     * <p>
     * Consider using {@link NCIntentTerm} annotation instead for simpler access to intent tokens.
     *
     * @param idx Index of the term (starting from <code>0</code>).
     * @return List of tokens, potentially {@code null}, for given term.
     * @see NCIntentTerm
     * @see #getTermTokens(String)
     */
    List<NCToken> getTermTokens(int idx);

    /**
     * Gets tokens for given term. This is a companion method for {@link #getIntentTokens()}.
     * <p>
     * Consider using {@link NCIntentTerm} annotation instead for simpler access to intent tokens.
     *
     * @param termId ID of the term for which to get tokens.
     * @return List of tokens, potentially {@code null}, for given term.
     * @see NCIntentTerm
     * @see #getTermTokens(int)
     */
    List<NCToken> getTermTokens(String termId);

    /**
     * Gets sentence parsing variant that produced the matching for the winning intent. Returned variant is one of the
     * variants provided by {@link NCContext#getVariants()} methods. Note that tokens returned by this method are
     * a superset of the tokens returned by {@link #getIntentTokens()} method, i.e. not all tokens
     * from this variant may have been used in matching of the winning intent.
     *
     * @return Sentence parsing variant that produced the matching for the winning intent.
     * @see #getIntentTokens()
     */
    NCVariant getVariant();

    /**
     * Indicates whether or not the intent match was ambiguous (not exact).
     * <p>
     * An exact match means that for the intent to match it has to use all non-free word tokens
     * in the user input, i.e. only free word tokens can be left unused after the match. An ambiguous match
     * doesn't have this restriction. Note that an ambiguous match should be used with a great care.
     * An ambiguous match completely ignores extra found user or system tokens (which are not part
     * of the intent template) which could have altered the matching outcome had they been considered.
     *
     * @return {@code True} if the intent match was exact, {@code false} otherwise.
     */
    boolean isAmbiguous();

    /**
     * Gets descriptor of the user on behalf of which the input request was submitted.
     *
     * @return User descriptor.
     */
    NCUser getUser();

    /**
     * Gets descriptor of the user's company on behalf of which the input request was submitted.
     *
     * @return User company descriptor.
     */
    NCCompany getCompany();

    /**
     * Gets globally unique server ID of the input request.
     * <p>
     * Server request is defined as a processing of a one user input request.
     * Note that the model can be accessed multiple times during processing of a single user request
     * and therefore multiple instances of this interface can return the same server
     * request ID. In fact, users of this interfaces can use this fact by using this ID,
     * for example, as a map key for a session scoped storage.
     *
     * @return Server request ID.
     */
    String getServerRequestId();

    /**
     * Gets normalized text of the user input.
     *
     * @return Normalized text of the user input.
     */
    String getNormalizedText();

    /**
     * Gets UTC/GMT timestamp in milliseconds when user input was received.
     *
     * @return UTC/GMT timestamp in milliseconds when user input was received.
     */
    long getReceiveTimestamp();

    /**
     * Gets optional address of the remote client that made the initial REST request.
     *
     * @return Optional address of the remote client.
     */
    Optional<String> getRemoteAddress();

    /**
     * Gets string representation of the user client agent that made the initial REST
     * request .
     *
     * @return User agent string from user client (web browser, REST client, etc.).
     */
    Optional<String> getClientAgent();

    /**
     * Gets optional JSON data passed in with the user request.
     *
     * @return Optional JSON data, can be empty but never {@code null}.
     */
    Map<String, Object> getRequestData();
}
