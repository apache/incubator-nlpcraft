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
 * Detected model element. A token is a detected model element and is a part of a
 * parsed user input. Sequence of tokens represents fully parsed (see {@link NCContext#getVariants()} method) user input. A single
 * token corresponds to a one or more words, sequential or not, in the user sentence.
 * <p>
 * <b>Configuring Token Providers</b><br>
 * Token providers (built-in or 3rd party) have to be enabled in the REST server <a href="https://nlpcraft.apache.org/server-and-probe.html">configuration</a>.
 * Data models also have to specify tokens they are expecting the REST server and probe to detect. This is done to
 * limit the unnecessary processing since implicit enabling of all token providers and all tokens can lead to a
 * significant slow down of processing. REST server <a href="https://nlpcraft.apache.org/server-and-probe.html">configuration</a>
 * property <code>nlpcraft.server.tokenProvides</code> provides the list of enabled token providers.
 * Data models provide their required tokens in {@link NCModel#getEnabledBuiltInTokens()} method.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/data-model.html">Data Model</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 *
 * @see NCElement
 * @see NCContext#getVariants()
 */
public interface NCToken extends NCMetadata {
    /**
     * Gets reference to the model this token belongs to.
     *
     * @return Model reference.
     */
    NCModelView getModel();

    /**
     * Gets ID of the server request this token is part of.
     *
     * @return ID of the server request this token is part of.
     */
    String getServerRequestId();

    /**
     * If this token represents user defined model element this method returns
     * the ID of that element. Otherwise, it returns ID of the built-in system token.
     * Note that a sentence can have multiple tokens with the same element ID. 
     *
     * @return ID of the element (system or user defined).
     * @see NCElement#getId()
     */
    String getId();

    /**
     * Gets the optional parent ID of the model element this token represents. This only available
     * for user-defined model elements (built-in tokens do not have parents).
     *
     * @return ID of the token's element immediate parent or {@code null} if not available.
     * @see NCElement#getParentId()
     * @see #getAncestors() 
     */
    String getParentId();

    /**
     * Gets the list of all parent IDs from this token up to the root. This only available
     * for user-defined model elements (built-in tokens do not have parents).
     *
     * @return List, potentially empty but never {@code null}, of all parent IDs from this token up to the root.
     * @see #getParentId()
     */
    List<String> getAncestors();

    /**
     * Tests whether this token is a child of given token ID. It is equivalent to:
     * <pre class="brush: java">
     *     return getAncestors().contains(tokId);
     * </pre>
     *
     * @param tokId Ancestor token ID.
     * @return <code>true</code> this token is a child of given token ID, <code>false</code> otherwise.
     */
    default boolean isChildOf(String tokId) {
        return getAncestors().contains(tokId);
    }

    /**
     * Gets the list of tokens this tokens is composed of. This method returns only immediate part tokens.
     *
     * @return List of constituent tokens, potentially empty but never {@code null}, that this token is composed of.
     * @see #findPartTokens(String...) 
     */
    List<NCToken> getPartTokens();

    /**
     * Gets the list of all part tokens with given IDs or aliases traversing entire part token graph.
     *
     * @param idOrAlias List of token IDs or aliases, potentially empty. If empty, the entire tree of part tokens
     *      is return as a list.
     * @return List of all part tokens with given IDs or aliases. Potentially empty but never {@code null}.
     * @see #getPartTokens() 
     */
    default List<NCToken> findPartTokens(String... idOrAlias) {
        List<NCToken> parts = getPartTokens();

        List<NCToken> list = new ArrayList<>();

        if (idOrAlias.length == 0) {
            if (!parts.isEmpty()) {
                list.addAll(parts);

                parts.forEach(p -> list.addAll(p.findPartTokens(idOrAlias)));
            }
        }
        else {
            // NOTE: re-sorting is fast enough on small arrays.
            Arrays.sort(idOrAlias);

            for (NCToken part : parts) {
                // Check ID first.
                boolean found = Arrays.binarySearch(idOrAlias, part.getId()) >= 0;

                if (!found)
                    // Check aliases if not found by ID.
                    for (String alias : getAliases()) {
                        found = Arrays.binarySearch(idOrAlias, alias) >= 0;

                        if (found)
                            break;
                    }

                if (found)
                    list.add(part);

                // Recursive call for the part.
                list.addAll(part.findPartTokens(idOrAlias));
            }
        }

        return list;
    }

    /**
     * Gets optional list of aliases this token is known by. Token can get an alias if it is a part of
     * other composed token and token DSL expression that was used to match it specified an alias. Note
     * that token can have zero, one or more aliases.
     *
     * @return List of aliases this token is known by. Can be empty, but never {@code null}.
     */
    List<String> getAliases();

    /**
     * Tests whether or not this token has given alias. It is equivalent to:
     * <pre class="brush: java">
     *      return getAliases().contains(alias);
     * </pre>
     *
     * @param alias Alias to test.
     * @return <code>True</code> if this token has alias <code>alias</code>, {@code false} otherwise.
     */
    default boolean isOfAlias(String alias) {
        return getAliases().contains(alias);
    }

    /**
     * Gets the value if this token was detected via element's value (or its synonyms). Otherwise
     * returns {@code null}. Only applicable for user-defined model elements (built-in tokens
     * do not have values).
     * 
     * @return Value for the user-defined model element or {@code null}, if not available.
     * @see NCElement#getValues()
     */
    String getValue();

    /**
     * Gets the list of groups this token belongs to. By default, if not specified explicitly, the group
     * is token's ID.
     *
     * @return Token groups list. Never {@code null} - but can be empty.
     * @see NCElement#getGroups()
     */
    List<String> getGroups();

    /**
     * Tests whether or not this token belongs to the given group. It is equivalent to:
     * <pre class="brush: java">
     *      return getGroups().contains(grp);
     * </pre>
     *
     * @param grp Group to test.
     * @return <code>True</code> if this token belongs to the group <code>grp</code>, {@code false} otherwise.
     */
    default boolean isMemberOf(String grp) {
        return getGroups().contains(grp);
    }

    /**
     * Gets start character index of this token in the original text.
     *
     * @return Start character index of this token.
     */
    int getStartCharIndex();

    /**
     * Gets end character index of this token in the original text.
     *
     * @return End character index of this token.
     */
    int getEndCharIndex();
    
    /**
     * A shortcut method checking whether or not this token is a stopword. Stopwords are some extremely common
     * words which add little value in helping understanding user input and are excluded from the
     * processing entirely. For example, words like a, the, can, of, about, over, etc. are
     * typical stopwords in English. NLPCraft has built-in set of stopwords.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:stopword");
     * </pre>
     * 
     * @return Whether or not this token is a stopword.
     */
    default boolean isStopWord() {
        return meta("nlpcraft:nlp:stopword");
    }

    /**
     * A shortcut method checking whether or not this token represents a free word. A free word is a
     * token that was detected neither as a part of user defined or system tokens.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:freeword");
     * </pre>
     *
     * @return Whether or not this token is a freeword.
     */
    default boolean isFreeWord() {
        return meta("nlpcraft:nlp:freeword");
    }

    /**
     * A shortcut method that gets original user input text for this token.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:origtext");
     * </pre>
     *
     * @return Original user input text for this token.
     */
    default String getOriginalText() {
        return meta("nlpcraft:nlp:origtext");
    }

    /**
     * A shortcut method that gets index of this token in the sentence.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:index");
     * </pre>
     *
     * @return Index of this token in the sentence.
     */
    default int getIndex() {
        return meta("nlpcraft:nlp:index");
    }

    /**
     * A shortcut method that gets internal globally unique system ID of the token.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:unid");
     * </pre>
     *
     * @return Internal globally unique system ID of the token.
     */
    default String getUnid() {
        return meta("nlpcraft:nlp:unid");
    }

    /**
     * Tests whether or not this token is a user-defined token.
     *
     * @return {code true} if this token is defined by the model element in the user model, {@code false} otherwise.
     */
    default boolean isUserDefined() {
        String id = getId();
        int i = id.indexOf(':');

        return i <=0 || !"nlpcraft google opennlp spacy stanford".contains(id.substring(0, i));
    }
}