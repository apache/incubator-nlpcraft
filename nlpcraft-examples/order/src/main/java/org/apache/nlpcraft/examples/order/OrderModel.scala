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
import org.apache.nlpcraft.*
import org.apache.nlpcraft.NCResultType.*
import org.apache.nlpcraft.examples.order.State.*
import org.apache.nlpcraft.examples.order.components.*
import org.apache.nlpcraft.internal.util.NCResourceReader
import org.apache.nlpcraft.nlp.*
import org.apache.nlpcraft.nlp.entity.parser.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.*
import org.apache.nlpcraft.nlp.entity.parser.stanford.*
import org.apache.nlpcraft.nlp.token.parser.stanford.*

import java.util.Properties
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

/**
  *
  */
object OrderModel extends LazyLogging:
    private val DFLT_QTY = 1
    private val UNEXPECTED = new NCRejection("Unexpected request in given context.")
    private def toStr[T](name: String, seq: Iterable[T]): String = if seq.nonEmpty then s"$name: ${seq.mkString(", ")}." else ""
    private def extractPizzaSize(e: NCEntity): String = e.get[String]("ord:pizza:size:value")
    private def extractQty(e: NCEntity, qty: String): Option[Int] = Option.when(e.contains(qty))(e.get[String](qty).toDouble.toInt)
    private def extractPizza(e: NCEntity): Pizza =
        Pizza(e.get[String]("ord:pizza:value"), e.getOpt[String]("ord:pizza:size").toScala, extractQty(e, "ord:pizza:qty"))
    private def extractDrink(e: NCEntity): Drink =
        Drink(e.get[String]("ord:drink:value"), extractQty(e, "ord:drink:qty"))

    private def getDescription(o: Order): String =
        if !o.isEmpty then
            val s1 = toStr("Pizza", o.getPizzas.values.map(p => s"${p.name} size: ${p.size.getOrElse("undefined")} count: ${p.qty.getOrElse(DFLT_QTY)}"))
            val s2 = toStr("Drinks", o.getDrinks.values.map(p => s"${p.name} count: ${p.qty.getOrElse(DFLT_QTY)}"))

            if s2.isEmpty then s1
            else if s1.isEmpty then s2 else s"$s1 $s2"
        else "Nothing ordered."


import org.apache.nlpcraft.examples.order.OrderModel.*

/**
  *
  */
class OrderModel extends NCModelAdapter (new NCModelConfig("nlpcraft.order.ex", "Order Example Model", "1.0"), StanfordPipeline.PIPELINE) with LazyLogging:
    private val userOrders = mutable.HashMap.empty[String, Order]

    private def withLog(im: NCIntentMatch, body: Order => NCResult): NCResult =
        val o = userOrders.getOrElseUpdate(im.getContext.getRequest.getUserId, new Order)
        def getState: String = o.getState.toString.toLowerCase
        val state = getState

        try body.apply(o)
        finally println(s"'${im.getIntentId}' called ($state -> $getState)")

    private def askIsReady(o: Order): NCResult =
        val res = NCResult(s"Is order ready?", ASK_DIALOG)
        o.setState(DIALOG_IS_READY)
        res

    private def askSpecify(o: Order) =
        require(!o.isValid)
        val res = o.findPizzaNoSize match
            case Some(p) => NCResult(s"Choose size (large, medium or small) for: '${p.name}'", ASK_DIALOG)
            case None =>
                require(o.isEmpty)
                NCResult(s"Please order something. Ask `menu` to look what you can order.", ASK_DIALOG)
        o.setState(DIALOG_SPECIFY)
        res

    private def askShouldStop(o: Order) =
        val res = NCResult(s"Should current order be canceled?", ASK_DIALOG)
        o.setState(DIALOG_SHOULD_CANCEL)
        res

    private def doShowMenu() =
        NCResult(
            "There are accessible for order: margherita, carbonara and marinara. Sizes: large, medium or small. " +
            "Also there are tea, green tea, coffee and cola.",
            ASK_RESULT
        )

    private def doShowStatus(o: Order, newState: State) =
        val res = NCResult(s"Current order state: ${getDescription(o)}", ASK_RESULT)
        o.setState(newState)
        res

    private def askConfirm(o: Order): NCResult =
        require(o.isValid)
        val res = NCResult(s"Let's specify your order. ${getDescription(o)} Is it correct?", ASK_DIALOG)
        o.setState(DIALOG_CONFIRM)
        res

    private def clear(im: NCIntentMatch, o: Order): Unit =
        userOrders.remove(im.getContext.getRequest.getUserId)
        val conv = im.getContext.getConversation
        conv.clearStm(_ => true)
        conv.clearDialog(_ => true)

    private def doExecute(im: NCIntentMatch, o: Order): NCResult =
        require(o.isValid)
        val res = NCResult(s"Executed: ${getDescription(o)}", ASK_RESULT)
        clear(im, o)
        res

    private def doStop(im: NCIntentMatch, o: Order): NCResult =
        val res =
            if !o.isEmpty then NCResult(s"Everything cancelled. Ask `menu` to look what you can order.", ASK_RESULT)
            else NCResult(s"Nothing to cancel. Ask `menu` to look what you can order.", ASK_RESULT)
        clear(im, o)
        res

    private def doContinue(o: Order): NCResult =
        val res = NCResult(s"OK, please continue.", ASK_RESULT)
        o.setState(NO_DIALOG)
        res

    private def askConfirmOrAskSpecify(o: Order): NCResult = if o.isValid then askConfirm(o) else askSpecify(o)
    private def askIsReadyOrAskSpecify(o: Order): NCResult = if o.isValid then askIsReady(o) else askSpecify(o)
    private def doExecuteOrAskSpecify(im: NCIntentMatch, o: Order): NCResult = if o.isValid then doExecute(im, o) else askSpecify(o)
    private def askStopOrDoStop(im: NCIntentMatch, o: Order): NCResult = if o.isValid then askShouldStop(o) else doStop(im, o)

    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=yes term(yes)={# == 'ord:yes'}")
    def onYes(im: NCIntentMatch): NCResult = withLog(
        im,
        (o: Order) => o.getState match
            case DIALOG_CONFIRM =>
                require(o.isValid);
                doExecute(im, o)
            case DIALOG_SHOULD_CANCEL => doStop(im, o)
            case DIALOG_IS_READY => askConfirmOrAskSpecify(o)
            case DIALOG_SPECIFY | NO_DIALOG => throw UNEXPECTED
    )

    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=no term(no)={# == 'ord:no'}")
    def onNo(im: NCIntentMatch): NCResult = withLog(
        im,
        (o: Order) => o.getState match
            case DIALOG_CONFIRM | DIALOG_IS_READY => doContinue(o)
            case DIALOG_SHOULD_CANCEL => askConfirmOrAskSpecify(o)
            case DIALOG_SPECIFY | NO_DIALOG => throw UNEXPECTED
        )
    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=stop term(stop)={# == 'ord:stop'}")
    def onStop(im: NCIntentMatch): NCResult = withLog(
        im,
        // It doesn't depend on order validity and dialog state.
        (o: Order) => askStopOrDoStop(im, o)
    )

    /**
      *
      * @param im
      * @param ps
      * @param ds
      * @return
      */
    @NCIntent("intent=order term(ps)={# == 'ord:pizza'}* term(ds)={# == 'ord:drink'}*")
    def onOrder(im: NCIntentMatch, @NCIntentTerm("ps") ps: List[NCEntity], @NCIntentTerm("ds") ds: List[NCEntity]): NCResult = withLog(
        im,
        (o: Order) =>
            require(ps.nonEmpty || ds.nonEmpty);
            o.add(ps.map(extractPizza), ds.map(extractDrink)); // It doesn't depend on order validity and dialog state.
            askIsReadyOrAskSpecify(o)
        )
    /**
      *
      * @param im
      * @param size
      * @return
      */
    @NCIntent("intent=orderPizzaSize term(size)={# == 'ord:pizza:size'}")
    def onOrderPizzaSize(im: NCIntentMatch, @NCIntentTerm("size") size: NCEntity): NCResult = withLog(
        im,
        (o: Order) => o.getState match
            case DIALOG_SPECIFY =>
                if o.setPizzaNoSize(extractPizzaSize(size)) then
                    o.setState(NO_DIALOG);
                    askIsReadyOrAskSpecify(o)
                else throw UNEXPECTED
            case DIALOG_CONFIRM | NO_DIALOG | DIALOG_IS_READY | DIALOG_SHOULD_CANCEL => throw UNEXPECTED
        )
    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=status term(status)={# == 'ord:status'}")
    def onStatus(im: NCIntentMatch): NCResult = withLog(
        im,
        (o: Order) => o.getState match
            case DIALOG_CONFIRM =>
                require(o.isValid);
                askConfirm(o) // Ignore `status`, confirm again.
            //case DIALOG_SPECIFY => askSpecify(o) // Ignore `status`, specify again.
            case DIALOG_SHOULD_CANCEL => doShowStatus(o, NO_DIALOG) // Changes state.
            case NO_DIALOG | DIALOG_IS_READY | DIALOG_SPECIFY => doShowStatus(o, o.getState)  // Keeps same state.
        )
    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=finish term(finish)={# == 'ord:finish'}")
    def onFinish(im: NCIntentMatch): NCResult = withLog(
        im,
        (o: Order) => o.getState match
            case DIALOG_CONFIRM => doExecuteOrAskSpecify(im, o) // Like YES  if valid.
            case DIALOG_SPECIFY => askSpecify(o) // Ignore `finish`, specify again.
            case NO_DIALOG | DIALOG_IS_READY | DIALOG_SHOULD_CANCEL => askConfirmOrAskSpecify(o)
        )
    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=menu term(menu)={# == 'ord:menu'}")
    def onMenu(im: NCIntentMatch): NCResult = withLog(
        im,
        // It doesn't depend and doesn't influence on order validity and dialog state.
        _ => doShowMenu()
    )