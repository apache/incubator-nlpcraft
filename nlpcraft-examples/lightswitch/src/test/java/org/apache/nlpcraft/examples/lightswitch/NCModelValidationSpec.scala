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

package org.apache.nlpcraft.examples.lightswitch

import org.apache.nlpcraft.*
import org.junit.jupiter.api.*

import scala.util.Using

/**
  * JUnit models validation.
  */
class NCModelValidationSpec:
    private lazy val MDL_SCALA = new LightSwitchScalaModel(
        "opennlp/en-token.bin",
        "opennlp/en-pos-maxent.bin",
        "opennlp/en-lemmatizer.dict"
    )

    private lazy val MDL_JAVA = new LightSwitchJavaModel(
        "opennlp/en-token.bin",
        "opennlp/en-pos-maxent.bin",
        "opennlp/en-lemmatizer.dict"
    )

    private lazy val MDL_GROOVY = new LightSwitchGroovyModel(
        "opennlp/en-token.bin",
        "opennlp/en-pos-maxent.bin",
        "opennlp/en-lemmatizer.dict"
    )

    private lazy val MDL_KOTLIN = new LightSwitchKotlinModel(
        "opennlp/en-token.bin",
        "opennlp/en-pos-maxent.bin",
        "opennlp/en-lemmatizer.dict"
    )

    private def test(mdl: NCModel): Unit = Using.resource(new NCModelClient(mdl)) { client => client.validateSamples() }

    @Test
    def testJava(): Unit = test(MDL_JAVA)

    @Test
    def testGroovy(): Unit = test(MDL_GROOVY)

    @Test
    def testKotlin(): Unit = test(MDL_KOTLIN)

    @Test
    def testScala(): Unit = test(MDL_SCALA)
