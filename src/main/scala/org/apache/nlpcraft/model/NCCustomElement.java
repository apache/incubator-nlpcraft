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
 * Detected model element returning from custom parser.
 * Note that model elements returned from {@link NCCustomParser#parse(NCRequest, NCModelView, List, List)} method must be
 * defined in the model.
 *
 * @see NCModel#getParsers()
 */
public interface NCCustomElement extends NCMetadata {
    /**
     * Gets ID of the detected model element. Note that it <b>must correspond</b> to one of the elements
     * defined in the model. In other words, the parser doesn't define a new model element but rather
     * references the element that's already defined in the model.
     *
     * @return ID of the detected model element.
     * @see NCElement#getId()
     * @see NCModel#getElements()
     */
    String getElementId();

    /**
     * Gets a list of NLP custom words that matched detected model element. These must be the same custom words
     * that were originally passed to {@link NCCustomParser#parse(NCRequest, NCModelView, List, List)} method.
     *
     * @return List of NLP custom words that comprise detected custom model element.
     */
    List<NCCustomWord> getWords();

    /**
     * Optional metadata that will be added to the resulting {@link NCToken} object and that would be
     * accessible via {@link NCToken#getMetadata()}.
     *
     * @return Optional metadata for the detected custom model element.
     */
    Map<String, Object> getMetadata();
}
