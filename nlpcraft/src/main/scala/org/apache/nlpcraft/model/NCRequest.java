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

import java.util.*;

/**
 * Supplemental information about the user request.
 *
 * @see NCContext#getRequest()
 */
public interface NCRequest {
    /**
     * Gets descriptor of the user on behalf of which this request was submitted.
     *
     * @return User descriptor.
     */
    NCUser getUser();

    /**
     * Gets descriptor of the user's company on behalf of which this request was submitted.
     *
     * @return User company descriptor.
     */
    NCCompany getCompany();

    /**
     * Gets globally unique server ID of the current request.
     * <p>
     * Server request is defined as a processing of a one user input request (a session).
     * Note that model can be accessed multiple times during processing of a single user request
     * and therefor multiple instances of this interface can return the same server
     * request ID. In fact, users of this interfaces can use this fact by using this ID,
     * for example, as a map key for a session scoped storage.
     *
     * @return Server request ID.
     */
    String getServerRequestId();

    /**
     * Gets normalized text of the user input.
     *
     * @return Normalized text of the user input.
     */
    String getNormalizedText();

    /**
     * Gets UTC/GMT timestamp in ms when user input was received.
     *
     * @return UTC/GMT timestamp in ms when user input was received.
     */
    long getReceiveTimestamp();

    /**
     * Gets optional address of the remote client.
     *
     * @return Optional address of the remote client.
     */
    Optional<String> getRemoteAddress();

    /**
     * Gets string representation of the user client agent that made the call with
     * this request.
     *
     * @return User agent string from user client (web browser, REST client, etc.).
     */
    Optional<String> getClientAgent();

    /**
     * Gets optional JSON data passed in with user request.
     *
     * @return Optional user input data.
     * @see NCUser#getProperties()
     */
    Optional<String> getData();
}