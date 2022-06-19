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
  * Conversation container. Conversation is essentially a container for everything that should be implicitly remembered
  * during the active, ongoing conversation and forgotten once the conversation stops. Conversation contains the
  * following elements:
  * <ul>
  * <li>List of entities defining a "short-term-memory (STM)" of this conversation.</li>
  * <li>Chronological list of previously matched intents.</li>
  * <li>Auto-expiring user data.</li>
  * </ul>
  * Note that the conversation is unique for given combination of user and data model.
  * <p>
  * Conversation management is based on idea of a short-term-memory (STM). STM can be viewed as a condensed
  * short-term history of the input for a given user and data model. Every submitted user request that wasn't
  * rejected is added to the conversation STM as a list of tokens. Existing STM tokens belonging to the same
  * group will be overridden by the more recent tokens from the same group. Note also that tokens in STM automatically
  * expire (i.e. context is "forgotten") after a certain period of time and/or based on the depth of the
  * conversation since the last mention.
  * <p>
  * You can also maintain user state-machine between requests using conversation's session. Conversation's {@link # getData ( ) data} is
  * a mutable thread-safe container that can hold any arbitrary user data while supporting the same expiration logic as
  * the rest of the conversation elements (i.e. tokens and previously matched intent IDs).
  * <p>
  * Conversation expiration policy is configured by two configuration properties:
  * <ul>
  * <li>{@link NCModelConfig# getConversationDepth ( )}</li>
  * <li>{@link NCModelConfig# getConversationTimeout ( )}</li>
  * </ul>
  *
  * @see NCContext#getConversation()
  * @see NCModelConfig#getConversationDepth()
  * @see NCModelConfig#getConversationTimeout() */
trait NCConversation:
    /**
      * Gets user-defined as a mutable thread-safe property container. Note tha this container has the same expiration
      * policy as the conversation it belongs to. Specifically, this returned container will be cleared when the
      * conversation gets cleared automatically (by timeout or depth) or manually.
      *
      * @return User-defined conversation data container. Can be empty but never {@code null}. */
    def getData: NCPropertyMap

    /**
      * Gets an ordered list of entities stored in the conversation STM for the current user and data model. Entities in
      * the returned list are ordered by their conversational depth, i.e. the entities from more recent requests appear
      * before entities from older requests.
      * <p>
      * Note that specific rules by which STM operates are undefined for the purpose of this function (i.e. callers
      * should not rely on any observed behavior of how STM stores and evicts its content).
      *
      * @return List of entities for this conversation's STM. The list can be empty which indicates that conversation
      * is brand new or expired - but never {@code null}. */
    def getStm: List[NCEntity]

    /**
      * Gets the chronologically ordered list of previously matched intents sorted from oldest to newest
      * for the current user.
      *
      * @return List of chronologically ordered previously matched dialog flow items. */
    def getDialogFlow: List[NCDialogFlowItem]

    /**
      * Removes all entities satisfying given predicate from the conversation STM. This is particularly useful when the
      * logic processing the user input makes an implicit assumption not present in the user input itself. Such
      * assumption may alter the conversation (without having an explicit entities responsible for it) and therefore
      * this method can be used to remove "stale" entities from conversation STM.
      * <p>
      * For example, in some cases the intent logic can assume the user current location as an implicit geographical
      * location and therefore all existing geographical-related entities should be removed from the conversation
      * STM to maintain correct context.
      *
      * @param filter Entity remove filter. */
    def clearStm(filter: NCEntity => Boolean): Unit

    /**
      * Removes all previously matched intents using given dialog flow item predicate.
      * <p>
      * History of matched intents (i.e. the dialog flow) can be used in intent definition as part of its
      * matching template. NLPCraft maintains the window of previously matched intents based on time, i.e.
      * after certain period of time the oldest previously matched intents are forgotten and removed from
      * dialog flow. This method allows explicitly clear previously matched intents from the
      * dialog flow based on user logic other than time window.
      *
      * @param filter Dialog flow filter. */
    def clearDialog(filter: NCDialogFlowItem => Boolean): Unit
