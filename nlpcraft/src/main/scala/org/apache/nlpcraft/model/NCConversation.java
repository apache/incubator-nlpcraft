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

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Conversation container for specific user and data model.
 * <p>
 * Conversation management is based on idea of a short-term-memory (STM). STM can be viewed as a condensed
 * short-term history of the input for a given user and data model. Every submitted user request that wasn't
 * rejected is added to the conversation STM as a list of {@link NCToken tokens}. Existing STM tokens belonging to
 * the same {@link NCElement#getGroups() group} will be overridden by the more recent tokens from the same group.
 * Note also that tokens in STM automatically expire (i.e. context is "forgotten") after a certain period of time and/or
 * based on the depth of the conversation since the last mention.
 * <p>
 * You can also maintain user state-machine between requests using method {@link #getUserData()}. This
 * method returns mutable thread-safe container that can hold any arbitrary user data while supporting the same
 * expiration logic as the rest of the conversation elements (i.e. tokens and previously matched intent IDs).
 *
 * @see NCContext#getConversation()
 * @see NCModelView#getConversationDepth()
 * @see NCModelView#getConversationTimeout()
 */
public interface NCConversation {
    /**
     * Gets an ordered list of tokens stored in the conversation STM for the current
     * user and data model. Tokens in the returned list are ordered by their conversational depth, i.e.
     * the tokens from more recent requests appear before tokens from older requests.
     * <p>
     * Note that this list excludes free words and stopwords. Note also that specific rules
     * by which STM operates are undefined for the purpose of this function (i.e. callers should not rely on
     * any observed behavior of how STM stores and evicts its content).
     *
     * @return List of tokens for this conversation's STM. The list can be empty which indicates that
     *      conversation is brand new (or timed out).
     */
    List<NCToken> getTokens();

    /**
     * Gets IDs for the previously matched intents sorted from oldest to newest for the current
     * user and data model.
     *
     * @return IDs for the previously matched intents.
     */
    List<String> getDialogFlow();

    /**
     * Removes all tokens satisfying given predicate from the conversation STM.
     * This is particularly useful when the logic processing the user input makes an implicit
     * assumption not present in the user input itself. Such assumption may alter the conversation (without
     * having an explicit token responsible for it) and therefore this method can be used to remove "stale" tokens
     * from conversation STM.
     * <p>
     * For example, in some cases the intent logic can assume the user current location as an implicit geo
     * location and therefore all existing geo tokens should be removed from the conversation STM
     * to maintain correct context.
     *
     * @param filter Token remove filter.
     */
    void clearStm(Predicate<NCToken> filter);

    /**
     * Clears history of matched intents using given intent predicate.
     * <p>
     * History of matched intents (i.e. the dialog flow) can be used in intent definition as part of its
     * matching template. NLPCraft maintains the window of previously matched intents based on time, i.e.
     * after certain period of time the oldest previously matched intents are forgotten and removed from
     * dialog flow. This method allows explicitly clear previously matched intents from the
     * dialog flow based on user logic other than time window.
     * 
     * @param filter Dialog flow filter based on IDs of previously matched intents.
     */
    void clearDialog(Predicate<String/* Intent ID. */> filter);

    /**
     * Gets modifiable user data container that can be used to store user data in the conversation.
     * Note that this data will expire the same way as other elements in the conversation (i.e. tokens and
     * previously matched intents).
     * <p>
     * Note that you should not cache or clone the data from this container because it won't be properly expired
     * in that case. You can, however, cache the return reference itself, if required.
     *
     * @return Mutable and thread-safe user data container.
     */
    Map<String, Object> getUserData();
}
