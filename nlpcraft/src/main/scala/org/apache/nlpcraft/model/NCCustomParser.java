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
 * Custom model element parser for programmatic NER implementations. This parser allows to define your own
 * Named Entity Recognizer (NER) implementation in cases when the standard declarative methods are not expressive
 * enough. Instance of the parser should be made available in the model via {@link NCModel#getParsers()} method.
 * <p>
 * By default the semantic data model detects its elements by their declarative synonyms. However,
 * in some cases this is not expressive enough. In such cases, one or more user-defined parsers can be defined
 * for the model that would allow the user to define its own NER logic to detect the model elements in the user
 * input programmatically. Note that there can be multiple custom parsers per model and each one can detect
 * any number of model elements.
 *
 * @see NCModel#getParsers()
 */
public interface NCCustomParser extends NCLifecycle {
    /**
     * Analyses user input provided as a list of {@link NCCustomWord} objects and returns a list
     * of {@link NCCustomElement} objects. Note that model elements returned from this method must
     * be defined in the model, i.e. this method only provides an additional logic of detecting these
     * elements but they still need to be defined normally in the model.
     *
     * @param req User request descriptor.
     * @param mdl Instance of data model this parser belongs to.
     * @param words Entire user input represented as a list of custom words.
     * @param elements List of already parsed and detected model elements at the point of this call.
     * @return List of custom elements. List can be empty or {@link null} if no model elements detected.
     * @see NCModel#getParsers()
     */
    List<NCCustomElement> parse(NCRequest req, NCModelView mdl, List<NCCustomWord> words, List<NCCustomElement> elements);
}
