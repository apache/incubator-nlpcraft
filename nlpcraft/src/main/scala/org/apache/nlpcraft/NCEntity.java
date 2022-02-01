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

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public interface NCEntity extends NCPropertyMap {
    /**
     *
     * @return
     */
    List<NCToken> getTokens();

    /**
     * Joins all tokens' text with trimming using space as a delimiter. This function does not cache the
     * result and performs text construction on each call. Make sure to cache the result to avoid
     * unnecessary parasitic workload if and when method {@link #getTokens()} does not change.
     */
    default String mkText() {
        return getTokens().stream().map(s -> s.getText().trim()).collect(Collectors.joining(" ")).trim();
    }

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
    default Set<String> getGroups() { return Collections.singleton(getId()); }

    /**
     *
     * @return
     */
    String getId();
}
