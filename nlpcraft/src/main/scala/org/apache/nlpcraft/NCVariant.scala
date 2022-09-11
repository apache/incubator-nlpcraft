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
  * A parsing variant is a list of entities defining one possible parsing of the input query. Note that a given user
  * input almost always has one or more possible different parsing variants. Furthermore, depending on the model
  * configuration a user input can produce hundreds and even thousands of parsing variants.
  *
  * Pipeline provides user-defined variant filter component [[NCVariantFilter]] to allow a programmatic filtration of
  * the variants. Note that even a few dozens of variants can significantly slow down the overall NLPCraft processing.
  *
  * @see [[NCModel.onVariant()]]
  */
trait NCVariant:
    /**
      * Gets the list of entities for this variant.
      *
      * @return List of entities for this variant. Can be empty but never `null`.
      */
    def getEntities: List[NCEntity]
