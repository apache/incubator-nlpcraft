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
import org.apache.nlpcraft.examples.order.components.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.*
import org.apache.nlpcraft.nlp.entity.parser.stanford.*
import org.apache.nlpcraft.nlp.token.parser.stanford.*
import org.apache.nlpcraft.examples.order.State.*

import scala.jdk.CollectionConverters.*
import java.util.Properties
import scala.jdk.OptionConverters._

object StanfordEn:
    val PIPELINE: NCPipeline =
        val stanford =
            val props = new Properties()
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
            new StanfordCoreNLP(props)
        val tokParser = new NCStanfordNLPTokenParser(stanford)
        val stemmer = new NCSemanticStemmer():
            private val ps = new PorterStemmer
            override def stem(txt: String): String = ps.synchronized { ps.stem(txt) }

        new NCPipelineBuilder().
            withTokenParser(tokParser).
            withEntityParser(new NCStanfordNLPEntityParser(stanford, "number")).
            withEntityParser(new NCSemanticEntityParser(stemmer, tokParser, "order_model.yaml")).
            withEntityMappers(Seq(new PizzaSizeExtender, new PizzaQtyExtender, new DrinkQtyExtender).asJava).
            withEntityValidator(new OrderValidator).
            build()

object OrderModel extends LazyLogging:
    private def norm(s: String) = s.trim.replaceAll("(?m)^[ \t]*\r?\n", "")
    private def withComma[T](iter: Iterable[T]): String = iter.mkString(", ")
    private def seq2Str[T](name: String, seq: Iterable[T]): String = if seq.nonEmpty then s"$name: ${withComma(seq)}." else ""

    private def extractPizzaSize(e: NCEntity): String = e.get[String]("ord:pizza:size:value")
    private def extractQty(e: NCEntity, qty: String): Option[Int] = Option.when(e.contains(qty))(e.get[String](qty).toInt)
    private def extractPizza(e: NCEntity): Pizza =
        Pizza(e.get[String]("ord:pizza:value"), e.getOpt[String]("ord:pizza:size").toScala, extractQty(e, "ord:pizza:qty"))
    private def extractDrink(e: NCEntity): Drink = Drink(e.get[String]("ord:drink:value"), extractQty(e, "ord:drink:qty"))

    private def getContent(o: Order): String =
        s"""
       |${seq2Str("Pizza", o.getPizzas.values.map(p => s"${p.name} ${p.size.getOrElse("undefined")} ${p.qty.getOrElse(1)}"))}
       |${seq2Str("Drinks", o.getDrinks.values.map(p => s"${p.name} ${p.qty.getOrElse(1)}"))}
        """.stripMargin


    private def toString(o: Order): String =
        norm(
            s"""
           |Order
           |${getContent(o)}
            """.stripMargin
        )

import org.apache.nlpcraft.examples.order.OrderModel.*
/**
  *
  */
class OrderModel extends NCModelAdapter (
    new NCModelConfig("nlpcraft.order.ex", "Order Example Model", "1.0"), StanfordEn.PIPELINE
) with LazyLogging:
    private val ords = mutable.HashMap.empty[String, Order]

    private def getOrder(im: NCIntentMatch): Order = ords.getOrElseUpdate(im.getContext.getRequest.getUserId, new Order)
    private def getLastIntentId(im: NCIntentMatch): Option[String] =
        im.getContext.getConversation.getDialogFlow.asScala.lastOption match
            case Some(e) => Some(e.getIntentMatch.getIntentId)
            case None => None

    private def mkOrderFinishDialog(o: Order): NCResult =
//        if o.isValid then
//            o.wait4Approve(true)
//            new NCResult("Is order ready?", ASK_DIALOG)
//        else NCResult(s"What is size size (large, medium or small) for: ${o.getPizzaNoSize.name}", ASK_DIALOG)
        null

    private def mkOrderContinueDialog(o: Order): NCResult =
//        require(o.inProgress)
//        NCResult("OK. Please continue", ASK_DIALOG)
        null

    private def mkOrderConfirmDialog(o: Order): NCResult =
        NCResult(
            norm(
                s"""
                   |Let me specify your order.
                   |${getContent(o)}
                   |Is it correct?
                """.stripMargin
            ),
            ASK_DIALOG
        )

    private def mkClearResult(im: NCIntentMatch, o: Order): NCResult =
        o.clear()
        val conv = im.getContext.getConversation
        conv.clearStm(_ => true)
        conv.clearDialog(_ => true)
        NCResult("Order canceled. We are ready for new orders.", ASK_RESULT)

    private def mkExecuteResult(o: Order): NCResult =
        println(s"EXECUTED:")
        println(OrderModel.toString(o))
        o.clear()
        NCResult("Congratulations. Your order executed. You can start make new orders.", ASK_RESULT)

    @NCIntent("intent=yes term(yes)={# == 'ord:yes'}")
    def onYes(im: NCIntentMatch, @NCIntentTerm("yes") yes: NCEntity): NCResult =
        val o = getOrder(im)
        val lastIntentId = getLastIntentId(im).orNull

//        if o.isWait4Approve then
//            o.wait4Approve(false)
//            mkOrderConfirmDialog(o)
//        else if lastIntentId == "stop" then mkOrderContinueDialog(o)
//        else mkOrderFinishDialog(o)
        null

    @NCIntent("intent=no term(no)={# == 'ord:no'}")
    def onNo(im: NCIntentMatch, @NCIntentTerm("no") no: NCEntity): NCResult =
        val o = getOrder(im)
        val lastIntentId = getLastIntentId(im).orNull

//        if o.isWait4Approve then
//            o.wait4Approve(false)
//            mkClearResult(im, o)
//        else if lastIntentId == "stop" then mkOrderContinueDialog(o)
//        else mkOrderFinishDialog(o)
        null

    @NCIntent("intent=stop term(stop)={# == 'ord:stop'}")
    def onStop(im: NCIntentMatch, @NCIntentTerm("stop") stop: NCEntity): NCResult =
        val o = getOrder(im)

        o.getState match
            case ORDER_VALID | ORDER_INVALID =>
                o.setState(ASK_CANCEL)
                NCResult("Are you sure that you want to cancel current order?", ASK_DIALOG)
            case _ => NCResult("Nothing to cancel.", ASK_RESULT)

    @NCIntent("intent=order term(ps)={# == 'ord:pizza'}* term(ds)={# == 'ord:drink'}*")
    def onOrder(im: NCIntentMatch, @NCIntentTerm("ps") ps: List[NCEntity], @NCIntentTerm("ds") ds: List[NCEntity]): NCResult =
        if ps.isEmpty && ds.isEmpty then throw new NCRejection("Please order some pizza or drinks")

        val o = getOrder(im)

        for (p <- ps) o.addPizza(extractPizza(p))
        for (d <- ds) o.addDrink(extractDrink(d))

        mkOrderFinishDialog(o)

    @NCIntent("intent=orderPizzaSize term(size)={# == 'ord:pizza:size'}")
    def onOrderPizzaSize(im: NCIntentMatch, @NCIntentTerm("size") size: NCEntity): NCResult =
        val o = getOrder(im)

        o.getState match
            case ORDER_INVALID => o.setPizzaNoSize(extractPizzaSize(size))
            case _ => NCRejection("") // TODO

        mkOrderFinishDialog(o)

    @NCIntent("intent=status term(status)={# == 'ord:status'}")
    def onStatus(im: NCIntentMatch, @NCIntentTerm("status") s: NCEntity): NCResult =
        val o = getOrder(im)

        o.getState match
            case ORDER_VALID | ORDER_INVALID => NCResult(OrderModel.toString(o), ASK_RESULT)
            case _ => NCResult("Nothing ordered.", ASK_RESULT)

    @NCIntent("intent=finish term(finish)={# == 'ord:finish'}")
    def onFinish(im: NCIntentMatch, @NCIntentTerm("finish") f: NCEntity): NCResult =
        val o = getOrder(im)

        o.getState match
            case ORDER_VALID | ASK_CONTINUE =>
                o.setState(ASK_CONFIRM)
                mkOrderConfirmDialog(o)
            case _ => NCResult("Nothing to finish.", ASK_RESULT)

    @NCIntent("intent=menu term(menu)={# == 'ord:menu'}")
    def onMenu(im: NCIntentMatch, @NCIntentTerm("menu") m: NCEntity): NCResult =
        NCResult(
            "There are margherita, marbonara and marinara. Sizes: large, medium or small. " +
            "Also there are tea, grean tea, coffee and cola.",
            ASK_RESULT
        )