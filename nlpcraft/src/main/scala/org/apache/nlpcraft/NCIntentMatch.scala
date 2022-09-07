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

package org.apache.nlpcraft

import scala.annotation.StaticAnnotation

/**
  * Descriptor of the matched intent.
  */
trait NCIntentMatch:
    /**
      * Gets ID of the matched intent.
      */
    def getIntentId: String

    /**
      * Gets a subset of entities representing matched intent. This subset is grouped by the matched terms
      * where a `null` sub-list defines an optional term. Order and index of sub-lists corresponds
      * to the order and index of terms in the matching intent. Number of sub-lists will always be the same
      * as the number of terms in the matched intent.
      *
      * Consider using `NCIntentTerm` annotation instead for simpler access to the intent entities.
      *
      * @return List of lists of entities representing matched intent.
      * @see [[getVariant()]]
      */
    def getIntentEntities: List[List[NCEntity]]

    /**
      * Gets entities for given term. This is a companion method for [[getIntentEntities()]] method.
      *
      * Consider using `NCIntentTerm` annotation instead for simpler access to the intent entities.
      *
      * @param idx Index of the term (starting from `0`).
      * @return List of entities, potentially `null`, for given term.
      */
    def getTermEntities(idx: Int): List[NCEntity]

    /**
      * Gets entities for given term. This is a companion method for [[getIntentEntities()]].
      *
      * Consider using `NCIntentTerm` annotation instead for simpler access to the intent entities.
      *
      * @param termId ID of the term for which to get entities.
      * @return List of entities, potentially `null`, for given term.
      */
    def getTermEntities(termId: String): List[NCEntity]

    /**
      * Gets parsing variant that produced the matching for this intent. Returned variant is one of the
      * variants provided by [[NCContext.getVariants()]] methods. Note that entities returned by this method are
      * a superset of the entities returned by [[getIntentEntities()]] method, i.e. not all entities
      * from this variant may have been used in matching of the winning intent.
      *
      * @return Parsing variant that produced the matching for this intent.
      */
    def getVariant: NCVariant