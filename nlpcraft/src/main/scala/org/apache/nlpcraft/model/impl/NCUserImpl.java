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

package org.apache.nlpcraft.model.impl;

import org.apache.nlpcraft.model.*;
import java.util.*;

/**
 * User descriptor implementation.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class NCUserImpl implements NCUser {
    private long id;
    private Optional<String> firstName;
    private Optional<String> lastName;
    private Optional<String> email;
    private Optional<String> avatarUrl;
    private Optional<Map<String, String>> props;
    private boolean isAdmin;
    private long signupTstamp;

    /**
     * 
     * @param id
     * @param firstName
     * @param lastName
     * @param email
     * @param avatarUrl
     * @param props
     * @param isAdmin
     * @param signupTstamp
     */
    public NCUserImpl(
        long id,
        Optional<String> firstName,
        Optional<String> lastName,
        Optional<String> email,
        Optional<String> avatarUrl,
        Optional<Map<String, String>> props,
        boolean isAdmin,
        long signupTstamp
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.props = props;
        this.isAdmin = isAdmin;
        this.signupTstamp = signupTstamp;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Optional<String> getFirstName() {
        return firstName;
    }

    @Override
    public Optional<String> getLastName() {
        return lastName;
    }

    @Override
    public Optional<Map<String, String>> getProperties() {
        return props;
    }

    @Override
    public Optional<String> getEmail() {
        return email;
    }

    @Override
    public Optional<String> getAvatarUrl() {
        return avatarUrl;
    }

    @Override
    public boolean isAdmin() {
        return isAdmin;
    }

    @Override
    public long getSignupTimestamp() {
        return signupTstamp;
    }
}
