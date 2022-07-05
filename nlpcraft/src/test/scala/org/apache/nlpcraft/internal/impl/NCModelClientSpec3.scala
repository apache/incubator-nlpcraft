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
import org.apache.nlpcraft.annotations.*
import org.apache.nlpcraft.nlp.entity.parser.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.*
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.Test

import java.util
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Using

/**
  * 
  */
class NCModelClientSpec3:
    @Test
    def test(): Unit =
        import NCSemanticTestElement as TE

        val mdl: NCTestModelAdapter = new NCTestModelAdapter:
            override val getPipeline: NCPipeline =
                val pl = mkEnPipeline
                pl.entParsers += NCTestUtils.mkEnSemanticParser(TE("e1"))
                pl

            @NCIntent("intent=i1 term(t1)={# == 'e1'}")
            def onMatch(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("t1") t1: NCEntity): NCResult = NCResult("Data", NCResultType.ASK_RESULT)

        Using.resource(new NCModelClient(mdl)) { client =>
            def ask(): NCCallbackData = client.debugAsk("e1", "userId", true)
            def execCallback(cb: NCCallbackData): NCResult = cb.getCallback.apply(cb.getCallbackArguments)
            def execCallbackOk(cb: NCCallbackData): Unit = println(s"Result: ${execCallback(cb).body}")
            def execCallbackFail(cb: NCCallbackData): Unit =
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



