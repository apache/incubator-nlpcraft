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
  *
  * @param mdl
  */
class NCModelClient(mdl: NCModel) extends LazyLogging, AutoCloseable:
    verify()

    private val intents = NCModelScanner.scan(mdl)
    private val convMgr = NCConversationManager(mdl.getConfig)
    private val dlgMgr = NCDialogFlowManager(mdl.getConfig)
    private val plMgr = NCModelPipelineManager(mdl.getConfig, mdl.getPipeline)
    private val intentsMgr = NCIntentSolverManager(dlgMgr, convMgr, intents.map(p => p.intent -> p.function).toMap)

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
        require(pipeline.getEntityParsers != null, "List of entity parsers in the pipeline cannot be null.")

    /**
      *
      */
    private def init(): Unit =
        convMgr.start()
        dlgMgr.start()
        plMgr.start()

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      * @param typ
      * @return
      */
    private def ask0(txt: String, data: Map[String, Any], usrId: String, typ: NCIntentSolveType): Either[NCResult, NCMatchedCallback] =
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

     /*
      * @param txt
      * @param data
      * @param usrId
      * @return
      */
    def ask(txt: String, usrId: String, data: Map[String, AnyRef] = Map.empty): NCResult =
        require(txt != null, "Input text cannot be null.")
        require(data != null, "Data cannot be null.")
        require(usrId != null, "User id cannot be null.")
        ask0(txt, data, usrId, NCIntentSolveType.REGULAR).swap.toOption.get

    /**
      *
      * @param usrId
      */
    def clearStm(usrId: String): Unit =
        require(usrId != null, "User id cannot be null.")
        convMgr.getConversation(usrId).clear(_ => true)

    /**
      *
      * @param usrId
      * @param filter
      */
    def clearStm(usrId: String, filter: NCEntity => Boolean): Unit =
        require(usrId != null, "User id cannot be null.")
        require(filter != null, "Filter cannot be null.")
        convMgr.getConversation(usrId).clear(filter)

    /**
      *
      * @param usrId
      */
    def clearDialog(usrId: String): Unit =
        require(usrId != null, "User id cannot be null.")
        dlgMgr.clear(usrId)

    /**
      *
      * @param usrId
      */
    def clearDialog(usrId: String, filter: NCDialogFlowItem => Boolean): Unit =
        require(usrId != null, "User id cannot be null.")
        require(usrId != null, "Filter cannot be null.")
        dlgMgr.clear(usrId, (i: NCDialogFlowItem) => filter(i))

    /**
      *
      */
    override def close(): Unit =
        plMgr.close()
        dlgMgr.close()
        convMgr.close()
        intentsMgr.close()

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      * @param saveHist
      * @return
      */
    def debugAsk(txt: String, usrId: String, saveHist: Boolean, data: Map[String, AnyRef] = Map.empty): NCMatchedCallback =
        require(txt != null, "Input text cannot be null.")
        require(data != null, "Data cannot be null.")
        require(usrId != null, "User id cannot be null.")
        import NCIntentSolveType.*
        ask0(txt, data, usrId, if saveHist then SEARCH else SEARCH_NO_HISTORY).toOption.get