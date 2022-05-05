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

package org.apache.nlpcraft.examples.pizzeria

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.NCResultType.*
import org.apache.nlpcraft.examples.pizzeria.{PizzeriaOrder as Order, PizzeriaOrderState as State}
import org.apache.nlpcraft.examples.pizzeria.PizzeriaOrderState.*
import org.apache.nlpcraft.nlp.*

import java.util.Properties
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

/**
  * * Pizza model helper.
  */
object PizzeriaModel extends LazyLogging:
    private val UNEXPECTED_REQUEST = new NCRejection("Unexpected request for current dialog context.")

    private def extractPizzaSize(e: NCEntity): String = e.get[String]("ord:pizza:size:value")
    private def extractQty(e: NCEntity, qty: String): Option[Int] = Option.when(e.contains(qty))(e.get[String](qty).toDouble.toInt)
    private def extractPizza(e: NCEntity): Pizza =
        Pizza(e.get[String]("ord:pizza:value"), e.getOpt[String]("ord:pizza:size").toScala, extractQty(e, "ord:pizza:qty"))
    private def extractDrink(e: NCEntity): Drink =
        Drink(e.get[String]("ord:drink:value"), extractQty(e, "ord:drink:qty"))

    type Result = (NCResult, State)

import org.apache.nlpcraft.examples.pizzeria.PizzeriaModel.*

/**
  * Pizza model.
  * It keep order state for each user.
  * Each order can in one of 5 state (org.apache.nlpcraft.examples.pizzeria.OrderState)
  * Note that there is used custom states logic instead of STM, because complex states flow.
  */
class PizzeriaModel extends NCModelAdapter(new NCModelConfig("nlpcraft.pizzeria.ex", "Pizzeria Example Model", "1.0"), PizzeriaModelPipeline.PIPELINE) with LazyLogging:
    private def getOrder(ctx: NCContext): Order =
        val data = ctx.getConversation.getData
        val usrId = ctx.getRequest.getUserId
        val o: Order = data.get(usrId)

        if o != null then o
        else
            val o = new Order()
            data.put(usrId, o)
            o

    private def doRequest(body: Order => Result)(using im: NCIntentMatch): NCResult =
        val o = getOrder(im.getContext)

        logger.info(s"Intent '${im.getIntentId}' activated for text: '${im.getContext.getRequest.getText}'.")
        logger.info(s"Before call [desc=${o.getState.toString}, resState: ${o.getDescription}.")

        val (res, resState) = body.apply(o)
        o.setState(resState)

        logger.info(s"After call [desc=${o.getDescription}, resState: $resState.")

        res

    private def askIsReady(): Result = NCResult(s"Is order ready?", ASK_DIALOG) -> DIALOG_IS_READY

    private def askSpecify(o: Order): Result =
        require(!o.isValid)

        o.findPizzaWithoutSize match
            case Some(p) =>
                NCResult(s"Choose size (large, medium or small) for: '${p.name}'", ASK_DIALOG) -> DIALOG_SPECIFY
            case None =>
                require(o.isEmpty)
                NCResult(s"Please order something. Ask `menu` to look what you can order.", ASK_DIALOG) -> DIALOG_SPECIFY

    private def askShouldStop(): Result =
        NCResult(s"Should current order be canceled?", ASK_DIALOG) -> DIALOG_SHOULD_CANCEL

    private def doShowMenu(): NCResult =
        NCResult(
            "There are accessible for order: margherita, carbonara and marinara. Sizes: large, medium or small. " +
            "Also there are tea, coffee and cola.",
            ASK_RESULT
        )

    private def doShowStatus(o: Order): NCResult = NCResult(s"Current order state: ${o.getDescription}.", ASK_RESULT)

    private def askConfirm(o: Order): Result =
        require(o.isValid)
        NCResult(s"Let's specify your order: ${o.getDescription}. Is it correct?", ASK_DIALOG) -> DIALOG_CONFIRM

    private def withClear(res: NCResult, newState: State, im: NCIntentMatch): Result =
        val ctx = im.getContext
        val conv = ctx.getConversation
        conv.getData.remove(ctx.getRequest.getUserId)
        conv.clearStm(_ => true)
        conv.clearDialog(_ => true)
        res -> newState

    // Access level set for tests reasons.
    private[pizzeria] def doExecute(o: Order)(using im: NCIntentMatch): Result =
        require(o.isValid)
        withClear(NCResult(s"Executed: ${o.getDescription}.", ASK_RESULT), DIALOG_EMPTY, im)

    private def doStop(o: Order)(using im: NCIntentMatch): Result =
        withClear(
            NCResult(
                if !o.isEmpty then s"Everything cancelled. Ask `menu` to look what you can order."
                else s"Nothing to cancel. Ask `menu` to look what you can order.",
                ASK_RESULT
            ),
            DIALOG_EMPTY,
            im
        )

    private def doContinue(): Result = NCResult(s"OK, please continue.", ASK_RESULT) -> DIALOG_EMPTY
    private def askConfirmOrAskSpecify(o: Order): Result = if o.isValid then askConfirm(o) else askSpecify(o)
    private def askIsReadyOrAskSpecify(o: Order): Result = if o.isValid then askIsReady() else askSpecify(o)
    private def doExecuteOrAskSpecify(o: Order)(using im: NCIntentMatch): Result = if o.isValid then doExecute(o) else askSpecify(o)
    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=yes term(yes)={# == 'ord:yes'}")
    def onYes(using im: NCIntentMatch): NCResult = doRequest(
        o => o.getState match
            case DIALOG_CONFIRM => doExecute(o)
            case DIALOG_SHOULD_CANCEL => doStop(o)
            case DIALOG_IS_READY => askConfirmOrAskSpecify(o)
            case DIALOG_SPECIFY | DIALOG_EMPTY => throw UNEXPECTED_REQUEST
    )

    private def askStopOrDoStop(o: Order)(using im: NCIntentMatch): Result = if o.isValid then askShouldStop() else doStop(o)

    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=no term(no)={# == 'ord:no'}")
    def onNo(using im: NCIntentMatch): NCResult = doRequest(
        o => o.getState match
            case DIALOG_CONFIRM | DIALOG_IS_READY => doContinue()
            case DIALOG_SHOULD_CANCEL => askConfirmOrAskSpecify(o)
            case DIALOG_SPECIFY | DIALOG_EMPTY => throw UNEXPECTED_REQUEST
    )
    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=stop term(stop)={# == 'ord:stop'}")
    // It doesn't depend on order validity and dialog state.
    def onStop(using im: NCIntentMatch): NCResult = doRequest(askStopOrDoStop)

    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=status term(status)={# == 'ord:status'}")
    def onStatus(using im: NCIntentMatch): NCResult = doRequest(
        o => o.getState match
            case DIALOG_CONFIRM => askConfirm(o) // Ignore `status`, confirm again.
            case DIALOG_SHOULD_CANCEL => doShowStatus(o) -> DIALOG_EMPTY // Changes state.
            case DIALOG_EMPTY | DIALOG_IS_READY | DIALOG_SPECIFY => doShowStatus(o) -> o.getState  // Keeps same state.
    )
    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=finish term(finish)={# == 'ord:finish'}")
    def onFinish(using im: NCIntentMatch): NCResult = doRequest(
        o => o.getState match
            case DIALOG_CONFIRM => doExecuteOrAskSpecify(o) // Like YES if valid.
            case DIALOG_SPECIFY => askSpecify(o) // Ignore `finish`, specify again.
            case DIALOG_EMPTY | DIALOG_IS_READY | DIALOG_SHOULD_CANCEL => askConfirmOrAskSpecify(o)
    )
    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=menu term(menu)={# == 'ord:menu'}")
    // It doesn't depend and doesn't influence on order validity and dialog state.
    def onMenu(using im: NCIntentMatch): NCResult = doRequest(o => doShowMenu() -> o.getState)

    /**
      *
      * @param im
      * @param ps
      * @param ds
      * @return
      */
    @NCIntent("intent=order term(ps)={# == 'ord:pizza'}* term(ds)={# == 'ord:drink'}*")
    def onOrder(using im: NCIntentMatch, @NCIntentTerm("ps") ps: List[NCEntity], @NCIntentTerm("ds") ds: List[NCEntity]): NCResult = doRequest(
        o =>
            require(ps.nonEmpty || ds.nonEmpty);
            // It doesn't depend on order validity and dialog state.
            o.add(ps.map(extractPizza), ds.map(extractDrink));
            askIsReadyOrAskSpecify(o)
    )

    /**
      *
      * @param im
      * @param size
      * @return
      */
    @NCIntent("intent=orderSpecify term(size)={# == 'ord:pizza:size'}")
    def onOrderSpecify(using im: NCIntentMatch, @NCIntentTerm("size") size: NCEntity): NCResult = doRequest(
        // If order in progress and has pizza with unknown size, it doesn't depend on dialog state.
        o => if !o.isEmpty && o.fixPizzaWithoutSize(extractPizzaSize(size)) then askIsReadyOrAskSpecify(o) else throw UNEXPECTED_REQUEST
    )

    override def onRejection(im: NCIntentMatch, e: NCRejection): NCResult =
        // TODO: improve logic after https://issues.apache.org/jira/browse/NLPCRAFT-495 ticket resolving.
        if im == null || getOrder(im.getContext).isEmpty then doShowMenu() else throw e