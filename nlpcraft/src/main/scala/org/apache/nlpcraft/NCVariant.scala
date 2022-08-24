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

/**
  * A parsing variant is a list of entities defining one possible parsing of the ipnut query. Note that a given input
  * query can have one or more possible different parsing variants. Depending on model configuration a user input
  * can produce hundreds or even thousands of parsing variants.
  *
  * @see [[NCModel#onVariant(NCVariant) */
trait NCVariant:
    /**
      * Gets the list of entities for this variant.
      *
      * @return List of entities for this variant. Can be empty but never `null`.
      */
    def getEntities: List[NCEntity]
