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

package org.apache.nlpcraft.internal.intent

/**
  * Intent options container.
  */
class NCIDLIntentOptions:
    /** Whether to ignore unused free words for intent match. */
    var ignoreUnusedFreeWords: Boolean = true
    /** Whether to ignore unused entities for intent match. */
    var ignoreUnusedEntities: Boolean = true
    /** Whether or not to allow intent to match if all matching entities came from STM only. */
    var allowStmEntityOnly: Boolean = false
    /** Whether or not the order of term is important for intent match. */
    var ordered: Boolean = false 

/**
  *
  */
object NCIDLIntentOptions:
    /*
     * JSON field names.
     */
    final val JSON_UNUSED_FREE_WORDS = "unused_free_words"
    final val JSON_UNUSED_ENTS = "unused_entities"
    final val JSON_ALLOW_STM_ONLY = "allow_stm_only"
    final val JSON_ORDERED = "ordered"

