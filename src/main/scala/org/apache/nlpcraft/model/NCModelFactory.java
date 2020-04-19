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
 * Optional factory for data models. Model factory is an optional mechanism for model creation. It is
 * necessary when you want to use some external framework to inject dependencies and configure the
 * data model externally, e.g. <a target=_ href="https://spring.io/">Spring</a>-based factory and configuration.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/data-model.html">Data Model</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 */
public interface NCModelFactory {
    /**
     * Initializes this factory with properties specified in probe configuration.
     *
     * @param props Configuration properties.
     */
    void initialize(Map<String, String> props);

    /**
     * Constructs a model instance.
     *
     * @param type Model type.
     *
     * @return Model.
     */
    NCModel mkModel(Class<? extends NCModel> type);

    /**
     * Terminates this factory when probe stops.
     */
    void terminate();
}
