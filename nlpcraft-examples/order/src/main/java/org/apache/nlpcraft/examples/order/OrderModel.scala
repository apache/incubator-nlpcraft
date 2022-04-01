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

package org.apache.nlpcraft.examples.order

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCResourceReader
import org.apache.nlpcraft.nlp.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticEntityParser
import org.apache.nlpcraft.nlp.entity.parser.*
import org.apache.nlpcraft.nlp.token.enricher.NCEnStopWordsTokenEnricher
import org.apache.nlpcraft.nlp.token.parser.NCOpenNLPTokenParser
import scala.collection.mutable
import org.apache.nlpcraft.NCResultType.*
import scala.jdk.CollectionConverters.*

/**
  *
  */
class OrderModel extends NCModelAdapter(
    new NCModelConfig("nlpcraft.order.ex", "Order Example Model", "1.0"),
    new NCPipelineBuilder().withSemantic("en", "order_model.yaml").build()
):
    private val ords = mutable.HashMap.empty[String, Order]

    private def getOrder(im: NCIntentMatch): Order = ords.getOrElseUpdate(im.getContext.getRequest.getUserId, new Order)

    private def extractPizzaKind(e: NCEntity): String = e.get[String]("ord:pizza:kind:value")
    private def extractPizzaSize(e: NCEntity): String = e.get[String]("ord:pizza:size:value")
    private def extractDrink(e: NCEntity): String = e.get[String]("ord:drink:value")

    private def confirmOrSpecify(ord: Order): NCResult =
        NCResult(if ord.isValid() then ord.ask2Confirm() else ord.ask2Specify(), ASK_DIALOG)

    private def getAvgPosition(e: NCEntity): Double =
        val toks = e.getTokens.asScala
        (toks.head.getIndex + toks.last.getIndex) / 2.0

    @NCIntent("intent=confirm term(confirm)={has(ent_groups, 'confirm')}")
    def onConfirm(im: NCIntentMatch, @NCIntentTerm("confirm") confirm: NCEntity): NCResult =
        val ord = getOrder(im)
        val dlg = im.getContext.getConversation.getDialogFlow

        def cancelAll(): NCResult =
            ord.clear()
            NCResult("Order canceled. We are ready for new orders.", ASK_RESULT)

        // 'stop' command.
        if !dlg.isEmpty && dlg.get(dlg .size() - 1).getIntentMatch.getIntentId == "ord:stop" then
            confirm.getId match
                case "ord:confirm:yes" => cancelAll()
                case "ord:confirm:no" => confirmOrSpecify(ord)
                case _ => throw new AssertionError()
        // 'confirm' command.
        else
            if !ord.inProgress() then throw new NCRejection("No orders in progress.")

            confirm.getId match
                case "ord:confirm:yes" =>
                    if ord.isValid() then
                        println(s"Done: $ord")
                        ord.clear()
                        NCResult("Congratulations. Your order executed. You can start make new orders.", ASK_RESULT)
                    else
                        NCResult(ord.ask2Specify(), ASK_DIALOG)
                case "ord:confirm:no" => cancelAll()
                case _ => throw new AssertionError()

    @NCIntent("intent=stop term(stop)={# == 'ord:stop'}")
        def onStop(im: NCIntentMatch, @NCIntentTerm("stop") stop: NCEntity): NCResult =
            if getOrder(im).inProgress() then NCResult("Are you sure that you want to cancel current order?", ASK_DIALOG)
            else NCResult("Nothing to cancel", ASK_RESULT)

    @NCIntent(
        "intent=order " +
        "  term(common)={# == 'ord:common'}* " +
        "  term(pizzaList)={# == 'ord:pizza:kind'}*" +
        "  term(pizzaSizesList)={# == 'ord:pizza:size'}* " +
        "  term(drinkList)={# == 'ord:drink'}*"
    )
    def onCommonOrder(
        im: NCIntentMatch,
        @NCIntentTerm("common") common: List[NCEntity],
        @NCIntentTerm("pizzaList") pizzaKinds: List[NCEntity],
        @NCIntentTerm("pizzaSizesList") pizzaSizes: List[NCEntity],
        @NCIntentTerm("drinkList") drinks: List[NCEntity]
    ): NCResult =
        if pizzaKinds.isEmpty && drinks.isEmpty then throw new NCRejection("Please order some pizza or drinks")
        if pizzaSizes.size > pizzaKinds.size then throw new NCRejection("Pizza and their sizes cannot be recognized")

        case class Holder(entity: NCEntity, position: Double)

        val ord = getOrder(im)
        val hsSizes = mutable.ArrayBuffer.empty ++ pizzaSizes.map(p => Holder(p ,getAvgPosition(p)))

        // Pizza. Each pizza can be specified by its size. Or size will be asked additionally.
        pizzaKinds.foreach(p => {
            hsSizes.size match
                case 0 => ord.addPizza(extractPizzaKind(p))
                case _ =>
                    val avgPos = getAvgPosition(p)
                    val nextNeighbour = hsSizes.minBy(p => Math.abs(avgPos - p.position))
                    ord.addPizza(extractPizzaKind(p), PizzaSize.valueOf(extractPizzaSize(nextNeighbour.entity).toUpperCase))
                    hsSizes -= nextNeighbour
        })

        for (p <- drinks.map(extractDrink)) ord.addDrink(p)

        confirmOrSpecify(ord)

    @NCIntent(
        "intent=specifyPizzaSize " +
        "  term(common)={# == 'ord:common'}* " +
        "  term(size)={# == 'ord:pizza:size'} " +
        "  term(pizza)={# == 'ord:pizza:kind'}?"
    )
    def onSpecifyPizzaSize(
        im: NCIntentMatch,
        @NCIntentTerm("common") common: List[NCEntity],
        @NCIntentTerm("size") size: NCEntity,
        @NCIntentTerm("pizza") pizzaOpt: Option[NCEntity]
    ): NCResult =
        val ord = getOrder(im)
        require(!ord.isValid())

        val sz = PizzaSize.valueOf(extractPizzaSize(size).toUpperCase)

        pizzaOpt match
            case Some(pizza) => ord.addPizza(extractPizzaKind(pizza), sz)
            case None => if !ord.specifyPizzaSize(sz) then throw new NCRejection("What specified?")

        confirmOrSpecify(ord)