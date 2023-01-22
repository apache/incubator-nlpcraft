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

package org.apache.nlpcraft.internal.conversation

import org.apache.nlpcraft.*
import org.apache.nlpcraft.annotations.*
import org.apache.nlpcraft.nlp.parsers.{NCSemanticTestElement as TE, *}
import org.apache.nlpcraft.nlp.util.*
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Using

/**
  *
  */
class NCConversationSpec2 extends AnyFunSuite:
    /**
      *
      */
    test("test") {
        case class Holder(intentId: String, elements: Set[String])
        object Holder:
            def apply(intentId: String, elems: String*): Holder = new Holder(intentId, elems.toSet)

        def mkResult(using im: NCIntentMatch)(elems: String*): NCResult = NCResult(Holder(im.getIntentId, elems*))

        val mdl: NCModel =
            new NCTestModelAdapter:
                override val getPipeline: NCPipeline = mkEnPipeline(
                    TE("sell", synonyms = "sales"),
                    TE("best_employee", synonyms = Set("best", "best employee", "best colleague"), groups = Set("sell", "buy")),
                    TE("buy", synonyms = "purchase")
                )

                @NCIntent("intent=sellIntent term~{# == 'sell'}")
                def sell(using ctx: NCContext, im: NCIntentMatch) = mkResult("sell")

                @NCIntent("intent=bestSellerIntent term~{# == 'sell'} term~{# == 'best_employee'}")
                def bestSeller(using ctx: NCContext, im: NCIntentMatch) = mkResult("best_employee", "sell")

                @NCIntent("intent=buyIntent term~{# == 'buy'}")
                def buy(using ctx: NCContext, im: NCIntentMatch) = mkResult("buy")

                @NCIntent("intent=bestBuyerIntent term~{# == 'buy'} term~{# == 'best_employee'}")
                def bestBuyer(using ctx: NCContext, im: NCIntentMatch) = mkResult("best_employee", "buy")

        Using.resource(new NCModelClient(mdl)) { cli =>
            val questions = Seq(
                "Give me the sales data" -> Holder("sellIntent", "sell"),
                "Who was the best?" -> Holder("bestSellerIntent", "best_employee", "sell"),
                "OK, give me the purchasing report now." -> Holder("buyIntent", "buy"),
                "Who was the best?" -> Holder("bestBuyerIntent", "best_employee", "buy")
            )

            for ((qry, h) <- questions)
                require(h == cli.ask(qry, "usrId").getBody)
        }
    }
