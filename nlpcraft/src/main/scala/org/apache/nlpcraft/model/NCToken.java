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

package org.apache.nlpcraft.model;

import java.util.List;

/**
 * Detected model element.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/data-model.html">Data Model</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft-examples">examples</a>.
 *
 * @see NCElement
 */
public interface NCToken {
    /**
     * Gets reference to the model this token belongs to.
     *
     * @return Model reference.
     */
    NCModel getModel();

    /**
     * Gets ID of the request this token is part of.
     *
     * @return ID of the request this token is part of.
     */
    String getRequestId();

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
     * for user-defined model elements - built-in tokens do not have parents and this will return {@code null}.
     *
     * @return ID of the token's element immediate parent or {@code null} if not available.
     * @see NCElement#getParentId()
     * @see #getAncestors()
     */
    String getParentId();

    /**
     * Gets the list of all parent IDs from this token up to the root. This only available
     * for user-defined model elements = built-in tokens do not have parents and will return an empty list.
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
     * Gets the value if this token was detected via element's value (or its synonyms). Otherwise,
     * returns {@code null}. Only applicable for user-defined model elements - built-in tokens
     * do not have values, and it will return {@code null}.
     *
     * @return Value for the user-defined model element or {@code null}, if not available.
     * @see NCElement#getValues()
     */
    String getValue();

    /**
     * Gets the list of groups this token belongs to. Note that, by default, if not specified explicitly,
     * token always belongs to one group with ID equal to token ID.
     *
     * @return Token groups list. Never {@code null} - but can be empty.
     * @see NCElement#getGroups()
     */
    List<String> getGroups();

    /**
     * Tests whether this token belongs to the given group. It is equivalent to:
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
     * A shortcut method checking whether this token is a stopword. Stopwords are some extremely common
     * words which add little value in helping to understand user input and are excluded from the
     * processing entirely. For example, words like a, the, can, of, about, over, etc. are
     * typical stopwords in English. NLPCraft has built-in set of stopwords.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:stopword");
     * </pre>
     * See more information on token metadata <a target=_ href="https://nlpcraft.apache.org/data-model.html">here</a>.
     *
     * @return Whether this token is a stopword.
     */
    boolean isStopWord();

    /**
     * A shortcut method checking whether this token represents a free word. A free word is a
     * token that was detected neither as a part of user defined nor system tokens.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:freeword");
     * </pre>
     * See more information on token metadata <a target=_ href="https://nlpcraft.apache.org/data-model.html">here</a>.
     *
     * @return Whether this token is a freeword.
     */
    boolean isFreeWord();
    /**
     * A shortcut method that gets original user input text for this token.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:origtext");
     * </pre>
     * See more information on token metadata <a target=_ href="https://nlpcraft.apache.org/data-model.html">here</a>.
     *
     * @return Original user input text for this token.
     */
    String getOriginalText();

    /**
     * A shortcut method that gets index of this token in the sentence.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:index");
     * </pre>
     * See more information on token metadata <a target=_ href="https://nlpcraft.apache.org/data-model.html">here</a>.
     *
     * @return Index of this token in the sentence.
     */
    int getIndex();

    /**
     * A shortcut method that gets normalized user input text for this token.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:normtext");
     * </pre>
     * See more information on token metadata <a target=_ href="https://nlpcraft.apache.org/data-model.html">here</a>.
     *
     * @return Normalized user input text for this token.
     */
    String getNormalizedText();

    /**
     * A shortcut method on whether this token is a swear word. NLPCraft has built-in list of
     * common English swear words.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:swear");
     * </pre>
     * See more information on token metadata <a target=_ href="https://nlpcraft.apache.org/data-model.html">here</a>.
     *
     * @return Whether this token is a swear word.
     */
    boolean isSwear();

    /**
     * A shortcut method to get lemma of this token, i.e. a canonical form of this word. Note that
     * stemming and lemmatization allow reducing inflectional forms and sometimes derivationally related
     * forms of a word to a common base form. Lemmatization refers to the use of a vocabulary and
     * morphological analysis of words, normally aiming to remove inflectional endings only and to
     * return the base or dictionary form of a word, which is known as the lemma.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:lemma");
     * </pre>
     * See more information on token metadata <a target=_ href="https://nlpcraft.apache.org/data-model.html">here</a>.
     *
     * @return Lemma of this token, i.e. a canonical form of this word.
     */
    String getLemma();

    /**
     * A shortcut method to get stem of this token. Note that stemming and lemmatization allow to reduce
     * inflectional forms and sometimes derivationally related forms of a word to a common base form.
     * Unlike lemma, stemming is a basic heuristic process that chops off the ends of words in the
     * hope of achieving this goal correctly most of the time, and often includes the removal of derivational affixes.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:stem");
     * </pre>
     * See more information on token metadata <a target=_ href="https://nlpcraft.apache.org/data-model.html">here</a>.
     *
     * @return Stem of this token.
     */
    String getStem();

    /**
     * A shortcut method to get Penn Treebank POS tag for this token. Note that additionally to standard Penn
     * Treebank POS tags NLPCraft introduced '---' synthetic tag to indicate a POS tag for multiword tokens.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:pos");
     * </pre>
     * See more information on token metadata <a target=_ href="https://nlpcraft.apache.org/data-model.html">here</a>.
     *
     * @return Penn Treebank POS tag for this token.
     */
    String getPos();

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
    String getUnid();
}
