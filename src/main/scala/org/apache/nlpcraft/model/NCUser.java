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
 * Descriptor of the user. Returned from {@link NCRequest#getUser()} method.
 *
 * @see NCRequest
 */
public interface NCUser {
    /**
     * Gets ID of this user.
     * 
     * @return User ID.
     */
    long getId();

    /**
     * Gets first name of the user.
     *
     * @return First name of the user.
     */
    Optional<String> getFirstName();

    /**
     * Gets last name of the user.
     *
     * @return Last name of the user.
     */
    Optional<String> getLastName();

    /**
     * Gets properties associated with the user.
     *
     * @return Optional map of properties associated with the user.
     */
    Optional<Map<String, String>> getProperties();

    /**
     * Gets email of the user.
     *
     * @return Email of the user.
     */
    Optional<String> getEmail();

    /**
     * Gets optional user avatar URL ({@code data:} or {@code http:} scheme URLs).
     *
     * @return User avatar URL ({@code data:} or {@code http:} scheme URLs).
     */
    Optional<String> getAvatarUrl();

    /**
     * Tests whether or not the user has administrative privileges.
     *
     * @return Whether or not the user has administrative privileges.
     */
    boolean isAdmin();

    /**
     * Gets signup timestamp of the user.
     *
     * @return Signup timestamp of the user.
     */
    long getSignupTimestamp();
}
