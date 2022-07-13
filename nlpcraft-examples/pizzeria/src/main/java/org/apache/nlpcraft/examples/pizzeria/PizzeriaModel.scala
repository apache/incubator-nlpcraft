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
import org.apache.nlpcraft.annotations.*
import org.apache.nlpcraft.examples.pizzeria.{PizzeriaOrder as Order, PizzeriaOrderState as State}
import org.apache.nlpcraft.examples.pizzeria.PizzeriaOrderState.*
import org.apache.nlpcraft.examples.pizzeria.components.PizzeriaModelPipeline
import org.apache.nlpcraft.nlp.*

/**
  * * Pizza model helper.
  */
object PizzeriaModel extends LazyLogging:
    type Result = (NCResult, State)
    private val UNEXPECTED_REQUEST = new NCRejection("Unexpected request for current dialog context.")

    private def extractPizzaSize(e: NCEntity): String = e.get[String]("ord:pizza:size:value")
    private def extractQty(e: NCEntity, qty: String): Option[Int] = Option.when(e.contains(qty))(e.get[String](qty).toDouble.toInt)
    private def extractPizza(e: NCEntity): Pizza =
        Pizza(e.get[String]("ord:pizza:value"), e.getOpt[String]("ord:pizza:size"), extractQty(e, "ord:pizza:qty"))
    private def extractDrink(e: NCEntity): Drink =
        Drink(e.get[String]("ord:drink:value"), extractQty(e, "ord:drink:qty"))

    private def getOrder(ctx: NCContext): Order =
        val data = ctx.getConversation.getData
        val usrId = ctx.getRequest.getUserId
        data.getOpt[Order](usrId) match
            case Some(ord) => ord
            case None =>
                val ord = new Order()
                data.put(usrId, ord)
                ord

    private def mkResult(msg: String): NCResult = NCResult(msg, ASK_RESULT)
    private def mkDialog(msg: String): NCResult = NCResult(msg, ASK_DIALOG)

    private def doRequest(body: Order => Result)(using ctx: NCContext, im: NCIntentMatch): NCResult =
        val o = getOrder(ctx)

        logger.info(s"Intent '${im.getIntentId}' activated for text: '${ctx.getRequest.getText}'.")
        logger.info(s"Before call [desc=${o.getState.toString}, resState: ${o.getDescription}.")

        val (res, resState) = body.apply(o)
        o.setState(resState)

        logger.info(s"After call [desc=${o.getDescription}, resState: $resState.")

        res

    private def askIsReady(): Result = mkDialog(s"Is order ready?") -> DIALOG_IS_READY

    private def askSpecify(o: Order): Result =
        require(!o.isValid)

        o.findPizzaWithoutSize match
            case Some(p) =>
                mkDialog(s"Choose size (large, medium or small) for: '${p.name}'") -> DIALOG_SPECIFY
            case None =>
                require(o.isEmpty)
                mkDialog(s"Please order something. Ask `menu` to look what you can order.") -> DIALOG_SPECIFY

    private def askShouldStop(): Result = mkDialog(s"Should current order be canceled?") -> DIALOG_SHOULD_CANCEL

    private def doShowMenuResult(): NCResult =
        mkResult(
            "There are accessible for order: margherita, carbonara and marinara. Sizes: large, medium or small. " +
            "Also there are tea, coffee and cola."
        )

    private def doShowMenu(state: State): Result = doShowMenuResult() -> state

    private def doShowStatus(o: Order, state: State): Result = mkResult(s"Current order state: ${o.getDescription}.") -> state

    private def askConfirm(o: Order): Result =
        require(o.isValid)
        mkDialog(s"Let's specify your order: ${o.getDescription}. Is it correct?") -> DIALOG_CONFIRM

    private def doResultWithClear(msg: String)(using ctx: NCContext, im: NCIntentMatch): Result =
        val conv = ctx.getConversation
        conv.getData.remove(ctx.getRequest.getUserId)
        conv.clearStm(_ => true)
        conv.clearDialog(_ => true)
        mkResult(msg) -> DIALOG_EMPTY

    private def doStop(o: Order)(using ctx: NCContext, im: NCIntentMatch): Result =
        doResultWithClear(
            if !o.isEmpty then s"Everything cancelled. Ask `menu` to look what you can order."
            else s"Nothing to cancel. Ask `menu` to look what you can order."
        )

    private def doContinue(): Result = mkResult("OK, please continue.") -> DIALOG_EMPTY
    private def askConfirmOrAskSpecify(o: Order): Result = if o.isValid then askConfirm(o) else askSpecify(o)
    private def askIsReadyOrAskSpecify(o: Order): Result = if o.isValid then askIsReady() else askSpecify(o)
    private def askStopOrDoStop(o: Order)(using ctx: NCContext, im: NCIntentMatch): Result = if o.isValid then askShouldStop() else doStop(o)

import org.apache.nlpcraft.examples.pizzeria.PizzeriaModel.*

/**
  * Pizza model.
  * It keep order state for each user.
  * Each order can in one of 5 state (org.apache.nlpcraft.examples.pizzeria.OrderState)
  * Note that there is used custom states logic instead of STM, because complex states flow.
  */
class PizzeriaModel extends NCModelAdapter(new NCModelConfig("nlpcraft.pizzeria.ex", "Pizzeria Example Model", "1.0"), PizzeriaModelPipeline.PIPELINE) with LazyLogging:
    // This method is defined in class scope and has package access level for tests reasons.
    private[pizzeria] def doExecute(o: Order)(using ctx: NCContext, im: NCIntentMatch): Result =
        require(o.isValid)
        doResultWithClear(s"Executed: ${o.getDescription}.")

    private def doExecuteOrAskSpecify(o: Order)(using ctx: NCContext, im: NCIntentMatch): Result = if o.isValid then doExecute(o) else askSpecify(o)

    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=yes term(yes)={# == 'ord:yes'}")
    def onYes(using ctx: NCContext, im: NCIntentMatch): NCResult = doRequest(
        o => o.getState match
            case DIALOG_CONFIRM => doExecute(o)
            case DIALOG_SHOULD_CANCEL => doStop(o)
            case DIALOG_IS_READY => askConfirmOrAskSpecify(o)
            case DIALOG_SPECIFY | DIALOG_EMPTY => throw UNEXPECTED_REQUEST
    )

    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=no term(no)={# == 'ord:no'}")
    def onNo(using ctx: NCContext, im: NCIntentMatch): NCResult = doRequest(
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
    def onStop(using ctx: NCContext, im: NCIntentMatch): NCResult = doRequest(askStopOrDoStop)

    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=status term(status)={# == 'ord:status'}")
    def onStatus(using ctx: NCContext, im: NCIntentMatch): NCResult = doRequest(
        o => o.getState match
            case DIALOG_CONFIRM => askConfirm(o) // Ignore `status`, confirm again.
            case DIALOG_SHOULD_CANCEL => doShowStatus(o, DIALOG_EMPTY)  // Changes state.
            case DIALOG_EMPTY | DIALOG_IS_READY | DIALOG_SPECIFY => doShowStatus(o, o.getState)  // Keeps same state.
    )
    /**
      *
      * @param im
      * @return
      */
    @NCIntent("intent=finish term(finish)={# == 'ord:finish'}")
    def onFinish(using ctx: NCContext, im: NCIntentMatch): NCResult = doRequest(
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
    def onMenu(using ctx: NCContext, im: NCIntentMatch): NCResult = doRequest(o => doShowMenu(o.getState))

    /**
      *
      * @param im
      * @param ps
      * @param ds
      * @return
      */
    @NCIntent("intent=order term(ps)={# == 'ord:pizza'}* term(ds)={# == 'ord:drink'}*")
    def onOrder(using ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("ps") ps: List[NCEntity], @NCIntentTerm("ds") ds: List[NCEntity]): NCResult = doRequest(
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
    def onOrderSpecify(using ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("size") size: NCEntity): NCResult = doRequest(
        // If order in progress and has pizza with unknown size, it doesn't depend on dialog state.
        o => if !o.isEmpty && o.fixPizzaWithoutSize(extractPizzaSize(size)) then askIsReadyOrAskSpecify(o) else throw UNEXPECTED_REQUEST
    )

    override def onRejection(ctx: NCContext, im: Option[NCIntentMatch], e: NCRejection): Option[NCResult] =
        if im.isEmpty || getOrder(ctx).isEmpty then throw e
        Option(doShowMenuResult())