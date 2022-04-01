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
  * This example provides very simple implementation for NLI-powered light switch.
  * You can say something like this:
  * <ul>
  *     <li>"Turn the lights off in the entire house."</li>
  *     <li>"Switch on the illumination in the master bedroom closet."</li>
  * </ul>
  * You can easily modify intent callbacks to perform the actual light switching using
  * HomeKit or Arduino-based controllers.
  * <p>
  * See 'README.md' file in the same folder for running and testing instructions.
  */

class OrderModel extends NCModelAdapter(
    new NCModelConfig("nlpcraft.order.ex", "Order Example Model", "1.0"),
    new NCPipelineBuilder().withSemantic("en", "order_model.yaml").build()
):
    private val orders = mutable.HashMap.empty[String, Order]

    private def getOrder(im: NCIntentMatch): Order = orders.getOrElseUpdate(im.getContext.getRequest.getUserId, new Order)
    private def extractPizza(e: NCEntity): String = e.get[String]("ord:pizza:kind:value")
    private def extractPizzaSize(e: NCEntity): String = e.get[String]("ord:pizza:size:value")
    private def extractDrink(e: NCEntity): String = e.get[String]("ord:drink:value")

    private def confirmOrSpecify(ord: Order): NCResult =
        if ord.isValid() then
            NCResult(ord.ask2Confirm(), ASK_DIALOG)
        else
            NCResult(ord.ask2Specify(), ASK_DIALOG)

    private def getAvgPosition(e: NCEntity): Double =
        val toks = e.getTokens.asScala

        (toks.head.getIndex + toks.last.getIndex) / 2.0

    @NCIntent("intent=confirm term(confirm)={has(ent_groups, 'confirm')}")
    def onConfirm(im: NCIntentMatch, @NCIntentTerm("confirm") confirm: NCEntity): NCResult =
        val ord = getOrder(im)

        if !ord.inProgress() then throw new NCRejection("No orders in progress")

        if confirm.getId == "ord:confirm:yes" then
            if ord.isValid() then
                println("Congratulations. Order executed!")
                ord.clear()
                NCResult("Order executed.", ASK_RESULT)
            else
                NCResult(ord.ask2Specify(), ASK_DIALOG)
        else
            ord.clear()
            NCResult("Order cleared. We are ready for new orders.", ASK_RESULT)

    @NCIntent(
        "intent=order " +
        "  term(common)={# == 'ord:common'}* " +
        "  term(pizzaList)={# == 'ord:pizza:kind'}*" +
        "  term(pizzaSizesList)={# == 'ord:pizza:size'}* " +
        "  term(drinkList)={# == 'ord:drink'}*"
    )
    @NCIntentSample(Array(
        "I want to order margherita, marinara and tea"
    ))
    def onCommonOrder(
        im: NCIntentMatch,
        @NCIntentTerm("common") common: List[NCEntity],
        @NCIntentTerm("pizzaList") pizzas: List[NCEntity],
        @NCIntentTerm("pizzaSizesList") pizzaSizes: List[NCEntity],
        @NCIntentTerm("drinkList") drinks: List[NCEntity]
    ): NCResult =
        if pizzas.isEmpty && drinks.isEmpty then throw new NCRejection("Please order some pizza or drinks")
        if pizzaSizes.size > pizzas.size then throw new NCRejection("Pizza and their sizes cannot be recognized")

        val ord = getOrder(im)

        case class Size(entity: NCEntity, position: Double)

        val sizes = mutable.ArrayBuffer.empty ++ pizzaSizes.map(p => Size( p,getAvgPosition(p)))

        pizzas.foreach(p => {
            sizes.size match
                case 0 => ord.addPizza(extractPizza(p))
                case _ =>
                    val avgPos = getAvgPosition(p)
                    val nextNeighbour = sizes.minBy(p => Math.abs(avgPos - p.position))
                    ord.addPizza(extractPizza(p), PizzaSize.valueOf(extractPizzaSize(nextNeighbour.entity).toUpperCase))
                    sizes -= nextNeighbour
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
            case Some(pizza) => ord.addPizza(extractPizza(pizza), sz)
            case None => if !ord.specifyPizzaSize(sz) then throw new NCRejection("What specified?")

        confirmOrSpecify(ord)