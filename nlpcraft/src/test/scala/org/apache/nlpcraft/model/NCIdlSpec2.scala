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

import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
import scala.language.implicitConversions

/**
  * IDL test model.
  */
class NCIdlSpecModel2 extends NCModelAdapter(
    "nlpcraft.intents.idl.test", "IDL Test Model", "1.0"
) {
    override def getElements: util.Set[NCElement] = Set(NCTestElement("a"))

    @NCIntent("intent=a_11 term(a)={# == 'a'}")
    private def a11(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=a_23 term(a)={# == 'a'}[2,3]")
    private def a23(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=a_15 term(a)={# == 'a'}[1,5]")
    private def a15(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=a_plus term(a)={# == 'a'}+")
    private def a1Inf(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=a_star term(a)={# == 'a'}*")
    private def a0Inf(ctx: NCIntentMatch): NCResult = NCResult.text("OK")
}

/**
  * IDL test.
  */
@NCTestEnvironment(model = classOf[NCIdlSpecModel2], startClient = true)
class NCIdlSpec2 extends NCTestContext {
    @Test
    def test(): Unit = {
        checkIntent("a", "a_11")
        checkIntent("a a", "a_23")
        checkIntent("a a a", "a_23")
        checkIntent("a a a a", "a_15")
        checkIntent("a a a a a a ", "a_plus")
    }
}

