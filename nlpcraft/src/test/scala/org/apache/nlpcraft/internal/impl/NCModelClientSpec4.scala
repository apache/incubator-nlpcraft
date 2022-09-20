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
import org.apache.nlpcraft.annotations.NCIntent
import org.apache.nlpcraft.nlp.parsers.NCNLPEntityParser
import org.apache.nlpcraft.nlp.util.*
import org.scalatest.funsuite.AnyFunSuite

/**
  * 
  */
class NCModelClientSpec4 extends AnyFunSuite:
    test("test") {
        val pl = mkEnPipeline

        //  For intents matching, we have to add at least one entity parser.
        pl.entParsers += new NCNLPEntityParser

        val mdl: NCModel = new NCModelAdapter(CFG, pl) :
            @NCIntent("intent=i term(any)={true}")
            def onMatch(ctx: NCContext, im: NCIntentMatch): NCResult = TEST_RESULT

        val client = new NCModelClient(mdl)

        val allCalls = Seq(
            () => client.ask("test", "userId"),
            () => client.debugAsk("test", "userId", false),
            () => client.clearStm("userId", _ => true),
            () => client.clearStm("userId"),
            () => client.clearDialog("userId"),
            () => client.clearDialog("userId", _ => true)
        )

        for (call <- allCalls) call.apply()

        client.close()

        for (call <- allCalls ++ Seq(() => client.close()))
            try
                call.apply()
                require(false)
            catch case e: IllegalStateException => println(s"Expected: ${e.getLocalizedMessage}")
    }


