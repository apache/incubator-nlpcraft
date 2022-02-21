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

package org.apache.nlpcraft.examples.weather

import org.apache.nlpcraft.NCModelClient
import org.junit.jupiter.api.*

import scala.util.Using

/**
  * JUnit model validation.
  */
class NCModelValidationSpec:
    private final val propName = "OWM_API_KEY"

    private var mdl: WeatherModel = _

    @BeforeEach
    def setUp(): Unit =
        // Set your own API key here.
        var apiKey: String = System.getProperty(propName)
        if apiKey == null then apiKey = System.getenv(propName)
        // Default value, used for tests.
        if apiKey == null then apiKey = "8a51a2eb343bf87dc55ffd352f5641ea"
        mdl = new WeatherModel(apiKey)

    @AfterEach
    def tearDown(): Unit = if mdl != null then mdl.close()

    @Test
    def test(): Unit = Using.resource(new NCModelClient(mdl)) { client => client.validateSamples() }
