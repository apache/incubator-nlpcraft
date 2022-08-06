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

package org.apache.nlpcraft.internal.dialogflow

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.ascii.NCAsciiTable
import org.apache.nlpcraft.internal.util.NCUtils

import java.text.DateFormat
import java.time.format.DateTimeFormatter
import scala.collection.*

/**
 * Dialog flow manager.
 */
class NCDialogFlowManager(cfg: NCModelConfig) extends LazyLogging:
    private final val flow = mutable.HashMap.empty[String, mutable.ArrayBuffer[NCDialogFlowItem]]

    @volatile private var gc: Thread = _

    /**
      *  Gets next clearing time.
      */
    private def clearForTimeout(): Long =
        require(Thread.holdsLock(flow))

        val timeout = cfg.conversationTimeout
        val bound = NCUtils.now() - timeout
        var next = Long.MaxValue

        val delKeys = mutable.ArrayBuffer.empty[String]

        for ((usrId, values) <- flow)
            values --= values.filter(_.getRequest.getReceiveTimestamp < bound)

            if values.nonEmpty then
                val candidate = values.map(_.getRequest.getReceiveTimestamp).min + timeout
                if next > candidate then next = candidate
                else
                    delKeys += usrId

        if delKeys.nonEmpty then flow --= delKeys

        next

    /**
      *
      * @param intentMatch
      * @param res
      * @param ctx
      * @return
      */
    private def mkItem(intentMatch: NCIntentMatch, res: Option[NCResult], ctx: NCContext): NCDialogFlowItem =
        new NCDialogFlowItem:
            override val getIntentMatch: NCIntentMatch = intentMatch
            override val getRequest: NCRequest = ctx.getRequest
            override val getResult: Option[NCResult] = res

    /**
      *
      * @return
      */
    def start(): Unit =
        gc = NCUtils.mkThread("dialog-mgr-gc", cfg.id) { t =>
            while (!t.isInterrupted)
                try
                    flow.synchronized {
                        val sleepTime = clearForTimeout() - NCUtils.now()

                        if sleepTime > 0 then
                            logger.trace(s"${t.getName} waits for $sleepTime ms.")
                            flow.wait(sleepTime)
                    }
                catch
                    case _: InterruptedException => // No-op.
                    case e: Throwable => logger.error(s"Unexpected error for thread: ${t.getName}", e)
        }

        gc.start()
    /**
      *
      */
    def close(): Unit =
        NCUtils.stopThread(gc)
        gc = null
        flow.clear()

    /**
      * Adds matched (winning) intent to the dialog flow.
      *
      * @param intentMatch
      * @param res Intent callback result. // TODO: Option if debugAsk.
      * @param ctx Original query context.
      */
    def addMatchedIntent(intentMatch: NCIntentMatch, res: Option[NCResult], ctx: NCContext): Unit =
        val item = mkItem(intentMatch, res, ctx)

        flow.synchronized {
            flow.getOrElseUpdate(ctx.getRequest.getUserId, mutable.ArrayBuffer.empty[NCDialogFlowItem]).append(item)
            flow.notifyAll()
        }

    /**
      *
      * @param intentMatch
      * @param res
      * @param ctx
      */
    def replaceLastItem(intentMatch: NCIntentMatch, res: NCResult, ctx: NCContext): Unit =
        val item = mkItem(intentMatch, Option(res), ctx)

        flow.synchronized {
            val buf = flow.getOrElseUpdate(ctx.getRequest.getUserId, mutable.ArrayBuffer.empty[NCDialogFlowItem])

            // If buf is empty - it cleared by timer, so there is nothing to replace.     
            if buf.nonEmpty then
                buf.remove(buf.size - 1)
                buf.append(item)

            flow.notifyAll()
        }

    /**
      * Gets sequence of dialog flow items sorted from oldest to newest (i.e. dialog flow) for given user ID.
      *
      * @param usrId User ID.
      * @return Dialog flow.
      */
    def getDialogFlow(usrId: String): List[NCDialogFlowItem] =
        flow.synchronized { flow.get(usrId) } match
            case Some(buf) => buf.toList
            case None => List.empty

    /**
      * Prints out ASCII table for current dialog flow.
      *
      * @param usrId User ID.
      */
    def ack(usrId: String): Unit =
        val tbl = NCAsciiTable(
            "#",
            "Intent ID",
            "Request ID",
            "Text",
            "Received"
        )

        getDialogFlow(usrId).zipWithIndex.foreach { (itm, idx) =>
            tbl += (
                idx + 1,
                itm.getIntentMatch.getIntentId,
                itm.getRequest.getRequestId,
                itm.getRequest.getText,
                DateFormat.getDateTimeInstance.format(new java.util.Date(itm.getRequest.getReceiveTimestamp))
            )
        }

        logger.info(s"""Current dialog flow (oldest first) for [mdlId=${cfg.id}, usrId=$usrId]\n${tbl.toString()}""")

    /**
      * Clears dialog history for given user ID.
      *
      * @param usrId User ID.
      */
    def clear(usrId: String): Unit =
        flow.synchronized {
            flow -= usrId
            flow.notifyAll()
        }

    /**
      * Clears dialog history for given user ID and predicate.
      *
      * @param usrId User ID.
      * @param pred Intent ID predicate.
      */
    def clear(usrId: String, pred: NCDialogFlowItem => Boolean): Unit =
        flow.synchronized {
            flow.get(usrId) match
                case Some(fu) =>
                    fu --= fu.filter(pred)
                    if fu.isEmpty then flow -= usrId
                case None => // No-op.
            flow.notifyAll()
        }
