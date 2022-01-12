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

package org.apache.nlpcraft.nlp.entity.parser.semantic;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public interface NCSemanticElement {
    /**
     *
     * @return
     */
    String getId();

    /**
     *
     * @return
     */
    default List<String> getGroups() {
        return Collections.singletonList(getId());
    }

    /**
     * TODO: why do we need it?
     * @return
     */
    default boolean isMemberOf(String grp) {
        return getGroups().contains(grp);
    }

    /**
     * TODO: why do we need it?
     * @return
     */
    default String getDescription() {
        return null;
    }

    /**
     *
     * @return
     */
    default Map<String, Set<String>> getValues() {
        return Collections.emptyMap();
    }

    /**
     *
     * @return
     */
    default Set<String> getSynonyms() {
        return Collections.emptySet();
    }

    /**
     *
     * @return
     */
    default Map<String, Object> getProperties() {
        return Collections.emptyMap();
    }
}