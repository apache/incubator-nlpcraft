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
public interface NCIntentMatch {
    /**
     * Gets ID of the matched intent.
     *
     * @return ID of the matched intent.
     */
    String getIntentId();

    /**
     * Gets context of the user request.
     *
     * @return Original query context.
     */
    NCContext getContext();

    /**
     * Gets a subset of entities representing matched intent. This subset is grouped by the matched terms
     * where a {@code null} sub-list defines an optional term. Order and index of sub-lists corresponds
     * to the order and index of terms in the matching intent. Number of sub-lists will always be the same
     * as the number of terms in the matched intent.
     * <p>
     * Consider using {@link NCIntentTerm} annotation instead for simpler access to intent entities.
     *
     * @return List of list of entities representing matched intent.
     * @see #getVariant()
     * @see NCIntentTerm
     */
    List<List<NCEntity>> getIntentEntities();

    /**
     * Gets entities for given term. This is a companion method for {@link #getIntentEntities()}.
     * <p>
     * Consider using {@link NCIntentTerm} annotation instead for simpler access to intent entities.
     *
     * @param idx Index of the term (starting from <code>0</code>).
     * @return List of entities, potentially {@code null}, for given term.
     * @see NCIntentTerm
     * @see #getTermEntities(String)
     */
    List<NCEntity> getTermEntities(int idx);

    /**
     * Gets entities for given term. This is a companion method for {@link #getIntentEntities()}.
     * <p>
     * Consider using {@link NCIntentTerm} annotation instead for simpler access to intent entities.
     *
     * @param termId ID of the term for which to get entities.
     * @return List of entities, potentially {@code null}, for given term.
     * @see NCIntentTerm
     * @see #getTermEntities(int)
     */
    List<NCEntity> getTermEntities(String termId);

    /**
     * Gets parsing variant that produced the matching for this intent. Returned variant is one of the
     * variants provided by {@link NCContext#getVariants()} methods. Note that entities returned by this method are
     * a superset of the entities returned by {@link #getIntentEntities()}  method, i.e. not all entities
     * from this variant may have been used in matching of the winning intent.
     *
     * @return Parsing variant that produced the matching for this intent.
     * @see #getIntentEntities()
     */
    List<NCEntity> getVariant();
}
