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
 * Descriptor of the user company. Returned from {@link NCRequest#getCompany()} method.
 *
 * @see NCRequest
 */
public interface NCCompany {
    /**
     * Gets ID of the company.
     *
     * @return ID of the company.
     */
    long getId();

    /**
     * Gets name of the company.
     *
     * @return Name of the company.
     */
    String getName();

    /**
     * Gets optional website of the company.
     *
     * @return Website of the company.
     */
    Optional<String> getWebsite();

    /**
     * Gets optional country of the company.
     *
     * @return Country of the company.
     */
    Optional<String> getCountry();

    /**
     * Gets optional region of the company.
     *
     * @return Region of the company.
     */
    Optional<String> getRegion();

    /**
     * Gets optional city of the company.
     *
     * @return City of the company.
     */
    Optional<String> getCity();

    /**
     * Gets optional postal address of the company.
     *
     * @return Postal address of the company.
     */
    Optional<String> getAddress();

    /**
     * Gets optional postal code of the company.
     *
     * @return Postal code of the company.
     */
    Optional<String> getPostalCode();
}
