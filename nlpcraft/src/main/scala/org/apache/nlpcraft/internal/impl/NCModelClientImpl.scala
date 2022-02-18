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

package org.apache.nlpcraft.internal.impl

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
import java.util.function.*
import java.util.{ArrayList, Objects, UUID, Collections as JColls, List as JList, Map as JMap}
import scala.collection.{immutable, mutable}
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

/**
  *
  * @param mdl
  */
class NCModelClientImpl(mdl: NCModel) extends LazyLogging:
    verify()

    private val intents = NCModelScanner.scan(mdl)
    private val convMgr = NCConversationManager(mdl.getConfig)
    private val dlgMgr = NCDialogFlowManager(mdl.getConfig)
    private val plMgr = new NCModelPipelineManager(mdl.getConfig, mdl.getPipeline)
    private val intentsMgr = NCIntentsManager(dlgMgr, intents.map(p => p.intent -> p.function).toMap)

    init()

    /**
      *
      * @param cfg
      * @param pipeline
      */
    private def verify(): Unit =
        Objects.requireNonNull(mdl, "Model cannot be null.")

        val cfg = mdl.getConfig
        val pipeline = mdl.getPipeline

        Objects.requireNonNull(cfg.getId, "Model ID cannot be null.")
        Objects.requireNonNull(cfg.getName, "Model name cannot be null.")
        Objects.requireNonNull(cfg.getVersion, "Model version cannot be null.")
        Objects.requireNonNull(pipeline.getTokenParser, "Token parser cannot be null.")
        Objects.requireNonNull(pipeline.getEntityParsers, "List of entity parsers in the pipeline cannot be null.")

        if pipeline.getEntityParsers.isEmpty then E(s"At least one entity parser must be specified in the pipeline.")

    /**
      *
      */
    private def init(): Unit =
        convMgr.start()
        dlgMgr.start()
        plMgr.start()

     /*
      * @param txt
      * @param data
      * @param usrId
      * @return
      */
    def ask(txt: String, data: JMap[String, AnyRef], usrId: String): NCResult =
        val plData = plMgr.prepare(txt, data, usrId)

        val userId = plData.request.getUserId
        val convHldr = convMgr.getConversation(userId)
        val allEnts = plData.variants.flatMap(_.getEntities.asScala)

        val conv: NCConversation =
            new NCConversation:
                override val getSession: NCPropertyMap = convHldr.getUserData
                override val getStm: JList[NCEntity] = convHldr.getEntities.asJava
                override val getDialogFlow: JList[NCDialogFlowItem] = dlgMgr.getDialogFlow(userId).asJava
                override def clearStm(filter: Predicate[NCEntity]): Unit = convHldr.clear(filter)
                override def clearDialog(filter: Predicate[NCDialogFlowItem]): Unit = dlgMgr.clear(userId, (s: NCDialogFlowItem) => filter.test(s))

        val ctx: NCContext =
            new NCContext:
                override def isOwnerOf(ent: NCEntity): Boolean = allEnts.contains(ent)
                override val getModelConfig: NCModelConfig = mdl.getConfig
                override val getRequest: NCRequest = plData.request
                override val getConversation: NCConversation = conv
                override val getVariants: util.Collection[NCVariant] = plData.variants.asJava
                override val getTokens: JList[NCToken] = plData.tokens

        intentsMgr.solve(NCIntentSolverInput(ctx, mdl))

    /**
      *
      * @param usrId
      */
    def clearStm(usrId: String): Unit = convMgr.getConversation(usrId).clear(_ => true)

    /**
      *
      * @param usrId
      * @param filter
      */
    def clearStm(usrId: String, filter: Predicate[NCEntity]): Unit = convMgr.getConversation(usrId).clear(filter)

    /**
      *
      * @param usrId
      */
    def clearDialog(usrId: String): Unit = dlgMgr.clear(usrId)

    /**
      *
      * @param usrId
      */
    def clearDialog(usrId: String, filter: Predicate[NCDialogFlowItem]): Unit = dlgMgr.clear(usrId, (i: NCDialogFlowItem) => filter.test(i))

    /**
      *
      */
    def validateSamples(): Unit =
        case class Result(intentId: String, text: String, pass: Boolean, error: Option[String], time: Long)

        val userId = UUID.randomUUID().toString
        val res = mutable.ArrayBuffer.empty[Result]

        def now: Long = System.currentTimeMillis()

        for (i <- intents; samples <- i.samples)
            for (sample <- samples)
                val start = now

                val err: Option[String] =
                    try
                        ask(sample, null, userId)

                        None
                    catch case e: Throwable => Option(e.getMessage)

                res += Result(i.intent.id, sample, err.isEmpty, err, now - start)

            clearDialog(userId)
            clearStm(userId)

        val tbl = NCAsciiTable()

        tbl #= ("Intent ID", "+/-", "Text", "Error", "ms.")

        for (res <- res)
            tbl += (
                res.intentId,
                if res.pass then "OK" else "FAIL",
                res.text,
                res.error.getOrElse(""),
                res.time
            )

        val passCnt = res.count(_.pass)
        val failCnt = res.count(!_.pass)

        tbl.info(logger, Option(s"Model auto-validation results: OK $passCnt, FAIL $failCnt:"))

    /**
      *
      */
    def close(): Unit =
        plMgr.close()
        dlgMgr.close()
        convMgr.close()
