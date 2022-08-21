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
  * A context containing a fully parsed data from the input query.
  *
  * @see [[NCModel.onContext()]]
  */
trait NCContext:
    /**
      * Tests if given entity is part of the query this context is associated with.
      *
      * @param ent Entity to check.
      */
    def isOwnerOf(ent: NCEntity): Boolean

    /**
      * Gets configuration of the model this context is associated with.
      *
      * @return Model.
      */
    def getModelConfig: NCModelConfig

    /**
      * Gets user request container.
      *
      * @return User request.
      */
    def getRequest: NCRequest

    /**
      * Gets current conversation for this context.
      *
      * @return Current conversation.
      */
    def getConversation: NCConversation

    /**
      * Gets the list of parsing variants. Returned list always contains as least one parsing variant.
      *
      * @return A non-empty list of parsing variants.
      * @see [[NCModel.onVariant()]]
      */
    def getVariants: List[NCVariant]

    /**
      * Gets the list of all tokens for the input query this context is associated with.
      *
      * @return List of tokens for this context. Can be empty but never `null`.
      */
    def getTokens: List[NCToken]
