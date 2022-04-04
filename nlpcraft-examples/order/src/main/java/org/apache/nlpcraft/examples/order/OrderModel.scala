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

import com.typesafe.scalalogging.LazyLogging
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import opennlp.tools.stemmer.PorterStemmer
import org.antlr.v4.runtime.misc.Predicate
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCResourceReader
import org.apache.nlpcraft.nlp.*
import org.apache.nlpcraft.nlp.entity.parser.*

import scala.collection.mutable
import org.apache.nlpcraft.NCResultType.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.{NCSemanticEntityParser, NCSemanticStemmer}
import org.apache.nlpcraft.nlp.entity.parser.stanford.NCStanfordNLPEntityParser
import org.apache.nlpcraft.nlp.token.parser.stanford.NCStanfordNLPTokenParser

import java.util.Properties
import scala.jdk.CollectionConverters.*

object OrderModel extends LazyLogging:
    private val STANFORD =
        val props = new Properties()
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
        new StanfordCoreNLP(props)
    private val TOK_PARSER = new NCStanfordNLPTokenParser(STANFORD)

    private def extractPizzaKind(e: NCEntity): String = e.get[String]("ord:pizza:kind:value")
    private def extractPizzaSize(e: NCEntity): PizzaSize = PizzaSize.valueOf(e.get[String]("ord:pizza:size:value").toUpperCase)
    private def extractDrink(e: NCEntity): String = e.get[String]("ord:drink:value")
    private def isStopWord(t: NCToken): Boolean = t.get[Boolean]("stopword")
    private def confirmOrSpecify(ord: OrderState): NCResult =
        NCResult(if ord.isValid() then ord.ask2Confirm() else ord.ask2Specify(), ASK_DIALOG)
    private def continue(ord: OrderState): NCResult =
        NCResult(if ord.isValid() then s"OK, please continue your order: $ord" else ord.ask2Specify(), ASK_DIALOG)

    private def log(o: OrderState): Unit = logger.info(o.getState())
    private def onStart(im: NCIntentMatch, o: OrderState): Unit =
        logger.info(s"Initial state before request: ${im.getContext.getRequest.getText}")
    private def onFinish(o: OrderState): Unit =
        logger.info(s"Result state")
        logger.info(o.getState())

    private def withRelations(
        ents: Seq[NCEntity], relations: Seq[NCEntity], skip: Seq[NCEntity], allToks: Seq[NCToken]
    ): Map[NCEntity, NCEntity] =
        case class IdxHolder(from: Int, to: Int)

        def getIndexes(e: NCEntity): IdxHolder =
            val toks = e.getTokens.asScala
            IdxHolder(toks.head.getIndex, toks.last.getIndex)

        val skipIdxs = skip.flatMap(_.getTokens.asScala.map(_.getIndex)).toSet
        val used = mutable.ArrayBuffer.empty[NCEntity]

        def areNeighbours(i1: Int, i2: Int): Boolean =
            if i2 == i1 + 1 then true else Range(i1 + 1, i2).forall(i => isStopWord(allToks(i)) || skipIdxs.contains(i))

        ents.map(e => {
            val eIdxs = getIndexes(e)
            val r = relations.filter(r => !used.contains(r)).find(r => {
                val rIdxs = getIndexes(r)
                areNeighbours(rIdxs.to, eIdxs.from) || areNeighbours(eIdxs.to, rIdxs.from)
            }).orNull

            if r != null then used += r
            e -> r
        }).toMap

import org.apache.nlpcraft.examples.order.OrderModel.*
/**
  *
  */
class OrderModel extends NCModelAdapter (
    new NCModelConfig("nlpcraft.order.ex", "Order Example Model", "1.0"),
    new NCPipelineBuilder().
        withTokenParser(TOK_PARSER).
        withEntityParser(new NCStanfordNLPEntityParser(STANFORD, "number")).
        withEntityParser(new NCSemanticEntityParser(
            new NCSemanticStemmer():
                final private val ps = new PorterStemmer
                override def stem(txt: String): String = ps.synchronized { ps.stem(txt) }
            ,
            TOK_PARSER,
            "order_model.yaml"
        )).
        build()
) with LazyLogging:
    private val ords = mutable.HashMap.empty[String, OrderState]

    private def getOrder(im: NCIntentMatch): OrderState = ords.getOrElseUpdate(im.getContext.getRequest.getUserId, new OrderState)

    @NCIntent("intent=confirm term(confirm)={has(ent_groups, 'confirm')}")
    def onConfirm(im: NCIntentMatch, @NCIntentTerm("confirm") confirm: NCEntity): NCResult =
        val ord = getOrder(im)
        onStart(im, ord)
        val dlg = im.getContext.getConversation.getDialogFlow

        def cancelAll(): NCResult =
            ord.clear()
            im.getContext.getConversation.clearStm(_ => true)
            im.getContext.getConversation.clearDialog(_ => true)
            NCResult("Order canceled. We are ready for new orders.", ASK_RESULT)

        val res =
            // 'stop' command.
            if !dlg.isEmpty && dlg.get(dlg.size() - 1).getIntentMatch.getIntentId == "stop" then
                confirm.getId match
                    case "ord:confirm:yes" => cancelAll()
                    case "ord:confirm:no" => confirmOrSpecify(ord)
                    case "ord:confirm:continue" => continue(ord)
                    case _ => throw new AssertionError()
            // 'confirm' command.
            else
                if !ord.inProgress() then throw new NCRejection("No orders in progress.")

                confirm.getId match
                    case "ord:confirm:yes" =>
                        if ord.isValid() then
                            logger.info(s"ORDER EXECUTED: $ord")
                            ord.clear()
                            NCResult("Congratulations. Your order executed. You can start make new orders.", ASK_RESULT)
                        else
                            NCResult(ord.ask2Specify(), ASK_DIALOG)
                    case "ord:confirm:no" => cancelAll()
                    case "ord:confirm:continue" => continue(ord)
                    case _ => throw new AssertionError()

        onFinish(ord)
        res

    @NCIntent("intent=stop term(stop)={# == 'ord:stop'}")
        def onStop(im: NCIntentMatch, @NCIntentTerm("stop") stop: NCEntity): NCResult =
            val ord = getOrder(im)
            onStart(im, ord)
            val res =
                if ord.inProgress() then NCResult("Are you sure that you want to cancel current order?", ASK_DIALOG)
                else NCResult("Nothing to cancel", ASK_RESULT)
            onFinish(ord)
            res

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

        val ord = getOrder(im)
        onStart(im, ord)
        val m = withRelations(pizzaKinds, pizzaSizes, Seq.empty, im.getContext.getTokens.asScala.toSeq)

        for ((p, sz) <- m)
            val pz = extractPizzaKind(p)
            if sz != null then ord.addPizza(pz, extractPizzaSize(sz)) else ord.addPizza(pz)

        for (p <- drinks.map(extractDrink)) ord.addDrink(p)

        val res = confirmOrSpecify(ord)
        onFinish(ord)
        res

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

        onStart(im, ord)

        val sz = extractPizzaSize(size)

        pizzaOpt match
            case Some(pizza) => ord.addPizza(extractPizzaKind(pizza), sz)
            case None => if !ord.specifyPizzaSize(sz) then throw new NCRejection("What specified?")

        val res = confirmOrSpecify(ord)
        onFinish(ord)
        res