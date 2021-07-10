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

package org.apache.nlpcraft.examples.phone

import org.apache.nlpcraft.model.tools.test.NCTestAutoModelValidator
import org.junit.jupiter.api.{Assertions, Test, Disabled}

/**
 * JUnit model validation.
 * <p>
 * Make sure to do the following:
 * <ul>
 * <li>Change config for server in 'nlpcraft.conf' file to add Google provider: tokenProviders = “nlpcraft,google”</li>
 * <li>Set GOOGLE_APPLICATION_CREDENTIALS environment variable to point to the JSON file with Google credentials.</li>
 * </ul>
 */
class NCModelValidationSpec {
    @Disabled("Test needs to be manually enabled. Please read documentation on the config and credential setup needed.")
    @Test
    def test(): Unit = {
        // Instruct auto-validator what models to test.
        System.setProperty("NLPCRAFT_TEST_MODELS", "org.apache.nlpcraft.examples.phone.PhoneModel")

        // Start model auto-validator.
        Assertions.assertTrue(NCTestAutoModelValidator.isValid(), "See error logs above.")
    }
}
