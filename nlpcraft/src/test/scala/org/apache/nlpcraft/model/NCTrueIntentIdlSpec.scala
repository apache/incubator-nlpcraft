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

package org.apache.nlpcraft.model

import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

import scala.language.implicitConversions

/**
  * IDL test model.
  */
class NCTrueIntentIdlModel extends NCModelAdapter("nlpcraft.true.intent.test", "IDL Test Model", "1.0") {
    @NCIntent("intent=i term(a)={true}")
    private def callback(ctx: NCIntentMatch): NCResult = NCResult.text("OK")
}

/**
  * IDL test.
  */
@NCTestEnvironment(model = classOf[NCTrueIntentIdlModel], startClient = true)
class NCTrueIntentIdlSpec extends NCTestContext {
    @Test
    def testStopWord(): Unit = checkFail("the")

    @Test
    def testNotStopWord(): Unit = checkIntent("test", "i")
}

