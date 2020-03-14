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

import java.io.*;
import java.util.*;

/**
 * Data model query context. This context defines fully processed user input and its associated data that
 * the model's intents need to process and return the result.
 * 
 * @see NCIntentMatch
 */
public interface NCContext extends Serializable {
    /**
     * Tests if given token is part of this query.
     *
     * @param tok Token to check.
     * @return {@code true} if given token is from this sentence, {@code false} otherwise.
     */
    boolean isOwnerOf(NCToken tok);

    /**
     * Gets collection of all parsing variants for this query. Each parsing variant is a list of detected tokens.
     * Note that a given user input can have one or more possible different parsing variants.
     *
     * @return All parsing variants of this query. Always contains at least one variant.
     */
    Collection<? extends NCVariant> getVariants();

    /**
     * Gets globally unique ID of the current request. Server request is defined as a processing of
     * a one user input sentence.
     *
     * @return Server request ID.
     */
    String getServerRequestId();

    /**
     * Gets model instance for this query.
     *
     * @return Model.
     */
    NCModelView getModel();

    /**
     * Gets supplemental information about user request.
     *
     * @return Supplemental information about user request.
     */
    NCRequest getRequest();

    /**
     * Gets current conversation.
     *
     * @return Current conversation.
     */
    NCConversation getConversation();
}
