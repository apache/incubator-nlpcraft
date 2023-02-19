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

package org.apache.nlpcraft.internal.impl

import org.apache.nlpcraft.*
import annotations.*
import nlp.parsers.*
import nlp.util.*
import org.scalatest.funsuite.AnyFunSuite

import java.util
import scala.collection.mutable
import scala.util.Using

/**
  * 
  */
class NCModelClientSpec3 extends AnyFunSuite:
    test("test") {
        import org.apache.nlpcraft.nlp.parsers.NCSemanticTestElement as TE

        val mdl: NCTestModelAdapter = new NCTestModelAdapter :
            override val getPipeline: NCPipeline = mkEnPipeline(TE("e1"))

            @NCIntent("intent=i1 term(t1)={# == 'e1'}")
            def onMatch(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("t1") t1: NCEntity): NCResult = TEST_RESULT

        Using.resource(new NCModelClient(mdl)) { client =>
            def ask(): NCMatchedCallback = client.debugAsk("e1", "userId", true).get
            def execCallback(cb: NCMatchedCallback): NCResult = cb.getCallback.apply(cb.getCallbackArguments)
            def execCallbackOk(cb: NCMatchedCallback): Unit = println(s"Result: ${execCallback(cb).getBody}")
            def execCallbackFail(cb: NCMatchedCallback): Unit =
                try execCallback(cb)
                catch case e: NCException => println(s"Expected error: ${e.getMessage}")

            var cbData = ask()
            execCallbackOk(cbData)
            execCallbackFail(cbData) // It cannot be called again (Error is 'Callback was already called.')

            cbData = ask()
            execCallbackOk(cbData)

            cbData = ask()
            ask()
            execCallbackFail(cbData) // Cannot be called, because there are new requests  (Error is 'Callback is out of date.')
        }
    }


