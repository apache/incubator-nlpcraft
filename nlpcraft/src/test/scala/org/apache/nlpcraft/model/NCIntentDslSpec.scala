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

package org.apache.nlpcraft.model

import org.apache.nlpcraft.NCTestElement._
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
import scala.language.implicitConversions

/**
  * Intents DSL test model.
  */
class NCIntentDslSpecModel extends NCModelAdapter(
    "nlpcraft.intents.dsl.test", "Intents DSL Test Model", "1.0"
) {
    private implicit def convert(s: String): NCResult = NCResult.text(s)

    // It overrides city.
    override def getElements: util.Set[NCElement] = Set(NCTestElement("paris"))

    // Moscow population filter.
    @NCIntent("intent=bigCity " +
        "term(city)={" +
            "id() == 'nlpcraft:city' && " +
            "get(meta_token('nlpcraft:city:citymeta'), 'population') >= 10381222" +
        "}")
    private def onBigCity(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntent("intent=otherCity term(city)={id() == 'nlpcraft:city'}")
    private def onOtherCity(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntent("intent=userElement term(x)={id() == 'paris'}")
    private def onUserElement(ctx: NCIntentMatch): NCResult = "OK"
}

/**
  * Intents DSL test.
  */
@NCTestEnvironment(model = classOf[NCIntentDslSpecModel], startClient = true)
class NCIntentDslSpec extends NCTestContext {
    @Test
    def testBigCity(): Unit = checkIntent("Moscow", "bigCity")

    @Test
    def testOtherCity(): Unit = checkIntent("San Francisco", "otherCity")

    @Test
    def testUserPriority(): Unit = checkIntent("Paris", "userElement")
}

