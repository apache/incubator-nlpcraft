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
 *
 */
public interface NCEntity {
    /**
     * Gets ID of the request this entity is part of.
     *
     * @return ID of the request this entity is part of.
     */
    String getRequestId();

    /**
     *
     * @return
     */
    String getId();

    /**
     * Gets the optional parent ID of the model element this entity represents. This only available
     * for user-defined model elements - built-in entities do not have parents and this will return {@code null}.
     *
     * @return ID of the entity's element immediate parent or {@code null} if not available.
     * @see NCElement#getParentId()
     * @see #getAncestors()
     */
    String getParentId();

    /**
     * Gets the list of all parent IDs from this entity up to the root. This only available
     * for user-defined model elements = built-in entities do not have parents and will return an empty list.
     *
     * @return List, potentially empty but never {@code null}, of all parent IDs from this entity up to the root.
     * @see #getParentId()
     */
    List<String> getAncestors();

    /**
     * Tests whether this entity is a child of given entity ID. It is equivalent to:
     * <pre class="brush: java">
     *     return getAncestors().contains(tokId);
     * </pre>
     *
     * @param tokId Ancestor entity ID.
     * @return <code>true</code> this entity is a child of given entity ID, <code>false</code> otherwise.
     */
    default boolean isChildOf(String tokId) {
        return getAncestors().contains(tokId);
    }

    /**
     * Gets the value if this entity was detected via element's value (or its synonyms). Otherwise,
     * returns {@code null}. Only applicable for user-defined model elements - built-in entities
     * do not have values, and it will return {@code null}.
     *
     * @return Value for the user-defined model element or {@code null}, if not available.
     * @see NCElement#getValues()
     */
    String getValue();

    /**
     * Gets the list of groups this entity belongs to. Note that, by default, if not specified explicitly,
     * entity always belongs to one group with ID equal to entity ID.
     *
     * @return entity groups list. Never {@code null} - but can be empty.
     * @see NCElement#getGroups()
     */
    List<String> getGroups();

    /**
     * Tests whether this entity belongs to the given group. It is equivalent to:
     * <pre class="brush: java">
     *      return getGroups().contains(grp);
     * </pre>
     *
     * @param grp Group to test.
     * @return <code>True</code> if this entity belongs to the group <code>grp</code>, {@code false} otherwise.
     */
    default boolean isMemberOf(String grp) {
        return getGroups().contains(grp);
    }

    /**
     * Gets start character index of this entity in the original text.
     *
     * @return Start character index of this entity.
     */
    int getStartCharIndex();

    /**
     * Gets end character index of this entity in the original text.
     *
     * @return End character index of this entity.
     */
    int getEndCharIndex();

    /**
     *
     * @return Whether this entity is a stopword.
     */
    boolean isStopWord();
    
    /**
     *
     * @return Original user input text for this entity.
     */
    String getOriginalText();

    /**
     *
     * @return Index of this entity in the sentence.
     */
    int getIndex();

    /**
     *
     * @return Normalized user input text for this entity.
     */
    String getNormalizedText();
    /**
     *
     * @return Lemma of this entity, i.e. a canonical form of this word.
     */
    String getLemma();

    /**
     *
     * @return Stem of this entity.
     */
    String getStem();

    /**
     *
     * @return Penn Treebank POS tag for this entity.
     */
    String getPos();

    /**
     * A shortcut method that gets internal globally unique system ID of the entity.
     * <p>
     * This method is equivalent to:
     * <pre class="brush: java">
     *     return meta("nlpcraft:nlp:unid");
     * </pre>
     *
     * @return Internal globally unique system ID of the entity.
     */
    String getGuid();
}
