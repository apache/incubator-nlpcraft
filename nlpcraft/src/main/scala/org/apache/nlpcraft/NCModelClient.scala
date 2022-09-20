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

package org.apache.nlpcraft

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.*
import org.apache.nlpcraft.internal.ascii.NCAsciiTable
import org.apache.nlpcraft.internal.conversation.*
import org.apache.nlpcraft.internal.dialogflow.NCDialogFlowManager
import org.apache.nlpcraft.internal.impl.*
import org.apache.nlpcraft.internal.intent.matcher.*
import org.apache.nlpcraft.internal.util.*

import java.util
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import java.util.{Objects, UUID}
import scala.concurrent.ExecutionContext

/**
  * Client API to issue requests again given model. This the primary method of interacting with NLPCraft
  * from the user perspective.
  *
  * @param mdl A data model to issue requests against.
  */
class NCModelClient(mdl: NCModel) extends LazyLogging, AutoCloseable:
    verify()

    private val intents = NCModelScanner.scan(mdl)
    private val convMgr = NCConversationManager(mdl.getConfig)
    private val dlgMgr = NCDialogFlowManager(mdl.getConfig)
    private val plMgr = NCModelPipelineManager(mdl.getConfig, mdl.getPipeline)
    private val intentsMgr = NCIntentSolverManager(dlgMgr, convMgr, intents.map(p => p.intent -> p.function).toMap)

    private var closed = false

    init()

    /**
      *
      */
    private def verify(): Unit =
        require(mdl != null, "Model cannot be null.")

        val cfg = mdl.getConfig
        val pipeline = mdl.getPipeline

        require(cfg.getId != null, "Model ID cannot be null.")
        require(cfg.getName != null, "Model name cannot be null.")
        require(cfg.getVersion != null, "Model version cannot be null.")
        require(pipeline.getTokenParser != null, "Token parser cannot be null.")
        require(pipeline.getEntityParsers != null && pipeline.getEntityParsers.nonEmpty, "List of entity parsers in the pipeline cannot be null or empty.")
    /**
      *
      */
    private def init(): Unit =
        convMgr.start()
        dlgMgr.start()
        plMgr.start()

    /**
      *
      */
    private def checkClosed(): Unit = if closed then throw new IllegalStateException("Client is already closed.")

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      * @param typ
      */
    private def ask0(txt: String, data: Map[String, Any], usrId: String, typ: NCIntentSolveType): Either[NCResult, NCMatchedCallback] =
        require(txt != null, "Input text cannot be null.")
        require(data != null, "Data cannot be null.")
        require(usrId != null, "User id cannot be null.")

        checkClosed()

        val plData = plMgr.prepare(txt, data, usrId)

        val userId = plData.request.getUserId
        val convHldr = convMgr.getConversation(userId)
        val allEnts = plData.variants.flatMap(_.getEntities)

        convHldr.updateEntities()

        val conv: NCConversation =
            new NCConversation:
                override val getData: NCPropertyMap = convHldr.getUserData
                override val getStm: List[NCEntity] = convHldr.getEntities
                override val getDialogFlow: List[NCDialogFlowItem] = dlgMgr.getDialogFlow(userId)
                override def clearStm(filter: NCEntity => Boolean): Unit = convHldr.clear(filter)
                override def clearDialog(filter: NCDialogFlowItem => Boolean): Unit = dlgMgr.clear(userId, (s: NCDialogFlowItem) => filter(s))

        val ctx: NCContext =
            new NCContext:
                override def isOwnerOf(ent: NCEntity): Boolean = allEnts.contains(ent)
                override val getModelConfig: NCModelConfig = mdl.getConfig
                override val getRequest: NCRequest = plData.request
                override val getConversation: NCConversation = conv
                override val getVariants: List[NCVariant] = plData.variants
                override val getTokens: List[NCToken] = plData.tokens

        intentsMgr.solve(mdl, ctx, typ)

    /**
      * Passes given input text to the model's pipeline for processing.
      *
      * This method takes given text, passes it to the pipeline for parsing and enrichment,
      * then tries to match it against declared intents. If the winning intent match is found,
      * its callback is called and result is returned here.
      *
      * @param txt Text of the request.
      * @param usrId ID of the user to associate with this request.
      * @param data Optional data container that will be available to the intent matching IDL.
      * @return Callback result from the winning intent match. This method never returns `null`.
      * @throws NCRejection An exception indicating a rejection of the user input. This exception is thrown
      *     automatically by the processing logic as well as can be thrown by the user from the intent callback.
      * @throws NCException Thrown in case of any internal errors processing the user input.
      */
    def ask(txt: String, usrId: String, data: Map[String, AnyRef] = Map.empty): NCResult =
        ask0(txt, data, usrId, NCIntentSolveType.REGULAR).swap.toOption.get

    /**
      * Removes all entities from the short-term-memory (STM) associated with given user ID.
      *
      * @param usrId User ID for which to clear STM.
      */
    def clearStm(usrId: String): Unit =
        require(usrId != null, "User id cannot be null.")
        checkClosed()
        convMgr.getConversation(usrId).clear(_ => true)

    /**
      * Removes entities satisfying given filter from the short-term-memory (STM) associated with given user ID.
      *
      * @param usrId User ID for which to clear STM.
      * @param filter Entity filter.
      */
    def clearStm(usrId: String, filter: NCEntity => Boolean): Unit =
        require(usrId != null, "User id cannot be null.")
        require(filter != null, "Filter cannot be null.")
        checkClosed()
        convMgr.getConversation(usrId).clear(filter)

    /**
      * Removes all previously matched intents from the memory associated with given user ID.
      *
      * @param usrId User ID for which to clear dialog history.
      */
    def clearDialog(usrId: String): Unit =
        require(usrId != null, "User id cannot be null.")
        checkClosed()
        dlgMgr.clear(usrId)

    /**
      * Removes previously matched intents satisfying given filter from the memory associated with given user ID.
      *
      * @param usrId User ID for which to clear dialog history.
      * @param filter Dialog flow item filter.
      */
    def clearDialog(usrId: String, filter: NCDialogFlowItem => Boolean): Unit =
        require(usrId != null, "User ID cannot be null.")
        require(usrId != null, "Filter cannot be null.")
        checkClosed()
        dlgMgr.clear(usrId, (i: NCDialogFlowItem) => filter(i))

    /**
      * Closes this client releasing its associated resources.
      */
    override def close(): Unit =
        checkClosed()
        closed = true
        plMgr.close()
        dlgMgr.close()
        convMgr.close()
        intentsMgr.close()

    /**
      * Passes given input text to the model's pipeline for processing.
      *
      * This method differs from [[NCModelClient.ask()]] method in a way that instead of calling a callback
      * of the winning intent this method returns the descriptor of that callback without actually calling it.
      * This method is well suited for testing the model's intent matching logic without automatically
      * executing the actual intent's callbacks.
      *
      * @param txt Text of the request.
      * @param usrId ID of the user to associate with this request.
      * @param saveHist Whether or not to store matched intent in the dialog history.
      * @param data Optional data container that will be available to the intent matching IDL.
      * @return Processing result. This method never returns `null`.
      * @throws NCRejection An exception indicating a rejection of the user input. This exception is thrown
      *     automatically by the processing logic as well as can be thrown by the user from the intent callback.
      * @throws NCException Thrown in case of any internal errors processing the user input.
      */
    def debugAsk(txt: String, usrId: String, saveHist: Boolean, data: Map[String, AnyRef] = Map.empty): NCMatchedCallback =
        import NCIntentSolveType.*
        ask0(txt, data, usrId, if saveHist then SEARCH else SEARCH_NO_HISTORY).toOption.get