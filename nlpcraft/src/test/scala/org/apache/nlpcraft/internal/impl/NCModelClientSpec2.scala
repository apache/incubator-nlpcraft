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
import org.apache.nlpcraft.nlp.entity.parser.semantic.{NCSemanticTestElement, *}
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.Test

import java.util
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Using

/**
  * 
  */
class NCModelClientSpec2:
    @Test
    def test(): Unit =
        val mdl = new NCTestModelAdapter:
            import NCSemanticTestElement as TE
            override val getPipeline =
                val pl = mkEnPipeline
                pl.entParsers += NCTestUtils.mkEnSemanticParser(TE("e1"), TE("e2"))
                pl.tokEnrichers += EN_TOK_LEMMA_POS_ENRICHER
                pl

            @NCIntent("intent=i1 term(t1)={# == 'e1'} term(t2List)={# == 'e2'}*")
            def onMatch(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("t1") act: NCEntity, @NCIntentTerm("t2List") locs: List[NCEntity]): NCResult =
                E("Shouldn't be called.")

        Using.resource(new NCModelClient(mdl)) { client =>
            case class Result(txt: String):
                private val wi = client.debugAsk(txt, "userId", true)
                private val allArgs: List[List[NCEntity]] = wi.getCallbackArguments

                val intentId: String = wi.getIntentId
                val size: Int = allArgs.size

                lazy val first: Seq[NCEntity] = allArgs.head
                lazy val second: Seq[NCEntity] = allArgs.last

                // 1. One argument.
            var res = Result("e1")

            require(res.intentId == "i1")
            require(res.size == 2)

            def check(e: NCEntity, txt: String): Unit =
                require(e.mkText == txt)
                // All data aren't lost.
                require(e.getTokens.head.keysSet.contains("lemma"))

            require(res.first.size == 1)
            check(res.first.head, "e1")

            require(res.second.isEmpty)

            // 2. One argument.
            res = Result("e1 e2 e2")

            require(res.intentId == "i1")
            require(res.size == 2)

            require(res.first.size == 1)
            check(res.first.head, "e1")

            require(res.second.size == 2)
            check(res.second.head, "e2")
            check(res.second.last, "e2")

            // 3. No winners.
            try
                client.debugAsk("x", "userId", false)

                require(false)
            catch
                case e: NCRejection => println(s"Expected rejection: ${e.getMessage}")
                case e: Throwable => throw e
        }



