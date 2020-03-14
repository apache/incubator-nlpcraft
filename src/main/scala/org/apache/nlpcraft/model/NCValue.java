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

import java.io.Serializable;
import java.util.List;

/**
 * Model element's value.
 * <p>
 * Each model element can generally be recognized either by one of its synonyms or values. Elements and their values
 * are analogous to types and instances of that type in programming languages. Each value
 * has a name and optional set of its own synonyms by which that value, and ultimately its element, can be
 * recognized by. Note that value name itself acts as an implicit synonym even when no additional synonyms added
 * for that value.
 *
 * @see NCElement#getValues()
 */
public interface NCValue extends Serializable {
    /**
     * Gets value name.
     *
     * @return Value name.
     */
    String getName();

    /**
     * Gets optional list of value's synonyms.
     *
     * @return Potentially empty list of value's synonyms.
     */
    List<String> getSynonyms();
}