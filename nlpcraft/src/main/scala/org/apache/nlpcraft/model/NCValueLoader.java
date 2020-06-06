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

import java.util.Set;

/**
 * Dynamic value loader that can be used by JSON/YAML model declarations.
 * <p>
 * <b>JSON</b>
 * <br>
 * When using JSON/YAML model presentation element values can be defined statically. However, in some
 * cases, it is required to load these values from external sources like database or REST services while
 * keeping the rest of the model declaration static (i.e. in JSON/YAML). To accomplish this you can
 * define <code>valueLoader</code> property and provide a fully qualified class name that implements
 * this interface. During the model instantiation an instance of that class will be created once per
 * each model and class of loader and method {@link #load(NCElement)} will be called to load
 * element's values. Note that you can use both statically defined values (i.e. <code>values</code> property)
 * and dynamically loaded values together and they will be merged:
 * <pre class="brush: js, highlight: [11]">
 *     "elements": [
 *         {
 *             "id": "my:id",
 *             "description": "My description.",
 *             "values": [
 *                  {
 *                      "name": "name1",
 *                      "synonyms": ["syn1", "syn2"]
 *                  }
 *             ],
 *             "valueLoader": "my.package.MyLoader"
 *         }
 *     ]
 * </pre>
 */
public interface NCValueLoader extends NCLifecycle {
    /**
     * Loads values for given model element.
     * 
     * @param owner Model element to which this value loader belongs to.
     * @return Set of values, potentially empty but never {@code null}.
     */
    Set<NCValue> load(NCElement owner);
}
