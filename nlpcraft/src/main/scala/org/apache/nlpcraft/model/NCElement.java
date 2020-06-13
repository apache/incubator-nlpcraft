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

import java.io.*;
import java.util.*;

/**
 * Data model element.
 * <p>
 * Data model element defines an semantic entity that will be detected in the user input. A model element
 * typically is one or more individual words that have a consistent semantic meaning and typically denote
 * a real-world object, such as persons, locations, number, date and time, organizations, products, etc.
 * Such object can be abstract or have a physical existence.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/data-model.html">Data Model</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 *
 * @see NCCustomParser
 */
public interface NCElement extends NCMetadata, Serializable {
    /**
     * Gets unique ID of this element.
     * <p>
     * This unique ID should be human readable for simpler debugging and testing of the model.
     * Although element ID could be any arbitrary string it is highly recommended to have
     * element ID as a lower case string starting with some model prefix, followed by colon and
     * then the element's name. For example, some built-in NLPCraft IDs are: <code>nlpcraft:date</code>,
     * <code>nlpcraft:city</code>.
     * <p>
     * Few important notes:
     * <ul>
     *      <li>Element IDs starting with <code>nlpcraft:</code> are reserved for built-in NLPCraft IDs.</li>
     *      <li>
     *          Element ID is an implicit synonym for that element.
     *          Thus element ID can be used in the user input directly to clearly
     *          disambiguate the element in the input sentence instead of relying on synonyms or other
     *          ways of detection.
     *      </li>
     * </ul>
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>id</code> property:
     * <pre class="brush: js, highlight: [3]">
     *     "elements": [
     *         {
     *             "id": "phone:act",
     *             "description": "Phone action.",
     *             "synonyms": [
     *                 "{give|*} {call|phone|ring|dial|dial up|ping|contact}"
     *             ]
     *         }
     *     ]
     * </pre>
     *
     * @see NCToken#getId()
     * @return Unique ID of this element.
     */
    String getId();

    /**
     * Gets the list of groups this elements belongs to.
     * <p>
     * Model element can belong to one or more groups. By default the element belongs to a single group whose group
     * ID is equal to its {@link #getId() ID}. The proper grouping of the model elements is required for operation
     * of Short-Term-Memory (STM) in {@link NCConversation conversation} (if and when conversation
     * is used). Specifically, a token (i.e. found model element) that is part of the group set will override
     * other tokens from the same set or a its superset. In other words, tokens with a smaller group set
     * (more specific token) will override the tokens from a larger group set (more generic tokens).
     * <p>
     * Note that built-in tokens (including from 3rd party token providers) belong to a single group whose group
     * ID is equal to their IDs.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>groups</code> property:
     * <pre class="brush: js, highlight: [5]">
     *     "elements": [
     *         {
     *             "id": "phone:act",
     *             "description": "Phone action.",
     *             "groups": ["group1", "group2"]
     *             "synonyms": [
     *                 "{give|*} {call|phone|ring|dial|dial up|ping|contact}"
     *             ]
     *         }
     *     ]
     * </pre>
     *
     * @return List of groups this element belongs to. By default - the model element belongs to one group
     *      with ID equal to the element {@link #getId() ID}.
     * @see NCConversation
     * @see #getId() 
     */
    default List<String> getGroups() {
        return Collections.singletonList(getId());
    }

    /**
     * Shortcut method to test if this element is a member of given group. It is equivalent to:
     * <pre class="brush: java">
     *     return getGroups().contains(grp);
     * </pre>
     *
     * @param grp Token group to test.
     * @return {@code True} if this element belongs to the given group, {@code false} otherwise.
     */
    default boolean isMemberOf(String grp) {
        return getGroups().contains(grp);
    }

    /**
     * Gets optional user-defined element's metadata. When a {@link NCToken token} for this element
     * is detected in the input this metadata is merged into {@link NCToken#getMetadata()} method returned metadata.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>description</code> property:
     * <pre class="brush: js, highlight: [8,9,10,11,12]">
     *     "elements": [
     *         {
     *             "id": "phone:act",
     *             "description": "Phone action.",
     *             "synonyms": [
     *                 "{give|*} {call|phone|ring|dial|dial up|ping|contact}"
     *             ],
     *             "metadata": {
     *                 "str": "val1",
     *                 "num": 100,
     *                 "bool": false
     *             }
     *         }
     *     ]
     * </pre>
     *
     * @return Element's metadata or {@code null} if none provided. Default implementation return {@code null}.
     */
    default Map<String, Object> getMetadata() {
        return null;
    }

    /**
     * Gets optional element description.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>description</code> property:
     * <pre class="brush: js, highlight: [4]">
     *     "elements": [
     *         {
     *             "id": "phone:act",
     *             "description": "Phone action.",
     *             "synonyms": [
     *                 "{give|*} {call|phone|ring|dial|dial up|ping|contact}"
     *             ]
     *         }
     *     ]
     * </pre>
     *
     * @return Optional element description. Default implementation returns {@code null}.
     */
    default String getDescription() {
        return null;
    }

    /**
     * Gets optional map of {@link NCValue values} for this element.
     * <p>
     * Each element can generally be recognized either by one of its synonyms or values. Elements and their values
     * are analogous to types and instances of that type in programming languages. Each value
     * has a name and optional set of its own synonyms by which that value, and ultimately its element, can be
     * recognized by. Note that value name itself acts as an implicit synonym even when no additional synonyms added
     * for that value.
     * <p>
     * Consider this example. A model element {@code x:car} can have:
     * <ul>
     *      <li>
     *          Set of general synonyms:
     *          <code>{transportation|transport|*} {vehicle|car|sedan|auto|automobile|suv|crossover|coupe|truck}</code>
     *      </li>
     *      <li>Set of values:
     *          <ul>
     *              <li>{@code mercedes} with synonyms {@code (mercedes, mercedes-benz, mb, benz)}</li>
     *              <li>{@code bmw} with synonyms {@code (bmw, bimmer)}</li>
     *              <li>{@code chevrolet} with synonyms {@code (chevy, chevrolet)}</li>
     *          </ul>
     *      </li>
     * </ul>
     * With that setup {@code x:car} element will be recognized by any of the following input sub-string:
     * <ul>
     *      <li>{@code transport car}</li>
     *      <li>{@code benz}</li>
     *      <li>{@code automobile}</li>
     *      <li>{@code transport vehicle}</li>
     *      <li>{@code sedan}</li>
     *      <li>{@code chevy}</li>
     *      <li>{@code bimmer}</li>
     *      <li>{@code x:car}</li>
     * </ul>
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>values</code> property:
     * <pre class="brush: js, highlight: [8,9,10,11,12,13]">
     *     "elements": [
     *         {
     *             "id": "phone:act",
     *             "description": "Phone action.",
     *             "synonyms": [
     *                 "{give|*} {call|phone|ring|dial|dial up|ping|contact}"
     *             ],
     *             "values": [
     *                  {
     *                      "name": "name1",
     *                      "synonyms": ["syn1", "syn2"]
     *                  }
     *             ]
     *         }
     *     ]
     * </pre>
     *
     * @return Map of value's name and its synonyms or {@code null} if not defined.
     */
    default List<NCValue> getValues() {
        return Collections.emptyList();
    }

    /**
     * Gets optional ID of the immediate parent element. Parent ID allows model elements to form into hierarchy.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>parentId</code> property:
     * <pre class="brush: js, highlight: [5]">
     *     "elements": [
     *         {
     *             "id": "phone:act",
     *             "description": "Phone action.",
     *             "parentId": "parent",
     *             "synonyms": [
     *                 "{give|*} {call|phone|ring|dial|dial up|ping|contact}"
     *             ]
     *         }
     *     ]
     * </pre>
     *
     * @return Optional parent element ID, or {@code null} if not specified. Default implementation returns
     *      {@code null}.
     */
    default String getParentId() {
        return null;
    }

    /**
     * Gets the list of synonyms by which this semantic element will be recognized by. Read more about
     * many different forms of synonyms in <a target=_ href="https://nlpcraft.apache.org/data-model.html">Data Model</a> section
     * and review <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>synonyms</code> property:
     * <pre class="brush: js, highlight: [5,6,7]">
     *     "elements": [
     *         {
     *             "id": "phone:act",
     *             "description": "Phone action.",
     *             "synonyms": [
     *                 "{give|*} {call|phone|ring|dial|dial up|ping|contact}"
     *             ]
     *         }
     *     ]
     * </pre>
     *
     * @return List of synonyms for this element. List is generally optional since element's ID acts
     *      as an implicit synonym. Default implementation returns an empty list.
     */
    default List<String> getSynonyms() {
        return Collections.emptyList();
    }

    /**
     * Gets optional dynamic value loader. This loader will be used additionally to any
     * values defined in {@link #getValues()} method. Default implementation returns {@code null}.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>valueLoader</code> property with value
     * of a fully qualified class name implementing {@link NCValueLoader} interface. Note that
     * only one instance of the value loader will be created per model and given class name:
     * <pre class="brush: js, highlight: [14]">
     *     "elements": [
     *         {
     *             "id": "phone:act",
     *             "description": "Phone action.",
     *             "synonyms": [
     *                 "{give|*} {call|phone|ring|dial|dial up|ping|contact}"
     *             ],
     *             "values": [
     *                  {
     *                      "name": "name1",
     *                      "synonyms": ["syn1", "syn2"]
     *                  }
     *             ],
     *             "valueLoader": "my.package.ValueLoader"
     *         }
     *     ]
     * </pre>
     *
     * @return Optional instance of dynamic value loader.
     */
    default NCValueLoader getValueLoader() {
        return null;
    }
}
