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
import java.util.function.Predicate;

/**
 *
 */
public interface NCConversation {
    /**
     *
     * @return
     */
    NCPropertyMap getSession();

    /**
     * 
     */
    List<NCEntity> getStm();

    /**
     * Gets the chronologically ordered list of previously matched intents sorted from oldest to newest
     * for the current user.
     *
     * @return List of chronologically ordered previously matched dialog flow items.
     */
    List<NCDialogFlowItem> getDialogFlow();

    /**
     * 
     * @param filter Entity remove filter.
     */
    void clearStm(Predicate<NCEntity> filter);

    /**
     * Removes all previously matched intents using given dialog flow item predicate.
     * <p>
     * History of matched intents (i.e. the dialog flow) can be used in intent definition as part of its
     * matching template. NLPCraft maintains the window of previously matched intents based on time, i.e.
     * after certain period of time the oldest previously matched intents are forgotten and removed from
     * dialog flow. This method allows explicitly clear previously matched intents from the
     * dialog flow based on user logic other than time window.
     *
     * @param filter Dialog flow filter.
     */
    void clearDialog(Predicate<String> filter); // TODO: was NCDialogFlowItem
}
