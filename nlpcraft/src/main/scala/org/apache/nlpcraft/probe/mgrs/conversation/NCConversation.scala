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

package org.apache.nlpcraft.probe.mgrs.conversation

import java.util
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

import com.typesafe.scalalogging.LazyLogging
import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.opencensus.NCOpenCensusTrace
import org.apache.nlpcraft.model._

import scala.jdk.CollectionConverters._
import scala.collection.mutable

/**
  * An active conversation is an ordered set of utterances for the specific user and data model.
  */
case class NCConversation(
    usrId: Long,
    mdlId: String,
    timeoutMs: Long,
    maxDepth: Int
) extends LazyLogging with NCOpenCensusTrace {
    private final val data = new ConcurrentHashMap[String, Object]()

    /**
     *
     * @param token
     * @param tokenTypeUsageTime
     */
    case class TokenHolder(token: NCToken, var tokenTypeUsageTime: Long = 0)

    /**
     *
     * @param holders Tokens holders.
     * @param srvReqId Server request ID. Used just for logging.
     * @param tstamp Request timestamp. Used just for logging.
     */
    case class ConversationItem(holders: mutable.ArrayBuffer[TokenHolder], srvReqId: String, tstamp: Long)

    // Short-Term-Memory.
    private val stm = mutable.ArrayBuffer.empty[ConversationItem]
    private val lastToks = mutable.ArrayBuffer.empty[Iterable[NCToken]]

    @volatile private var ctx: util.List[NCToken] = new util.ArrayList[NCToken]()
    @volatile private var lastUpdateTstamp = U.nowUtcMs()
    @volatile private var depth = 0

    /**
      *
      */
    private def squeezeTokens(): Unit = {
        require(Thread.holdsLock(stm))

        stm --= stm.filter(_.holders.isEmpty)
    }

    /**
     * Gets called on each input request for given user and model.
     *
     * @param parent Optional parent span.
     */
    def updateTokens(parent: Span = null): Unit = startScopedSpan("updateTokens", parent) { _ =>
        val now = U.nowUtcMs()

        stm.synchronized {
            depth += 1

            // Conversation cleared by timeout or when there are too much unsuccessful requests.
            if (now - lastUpdateTstamp > timeoutMs) {
                stm.clear()

                logger.trace(s"Conversation is reset by timeout [" +
                    s"usrId=$usrId, " +
                    s"mdlId=$mdlId" +
                s"]")
            }
            else if (depth > maxDepth) {
                stm.clear()

                logger.trace(s"Conversation is reset after reaching max depth [" +
                    s"usrId=$usrId, " +
                    s"mdlId=$mdlId" +
                s"]")
            }
            else {
                val minUsageTime = now - timeoutMs
                val toks = lastToks.flatten

                for (item <- stm) {
                    val delHs =
                        // Deleted by timeout for tokens type or when token type used too many requests ago.
                        item.holders.filter(h => h.tokenTypeUsageTime < minUsageTime || !toks.contains(h.token))

                    if (delHs.nonEmpty) {
                        item.holders --= delHs

                        logger.trace(
                            s"Conversation overridden tokens removed [" +
                                s"usrId=$usrId, " +
                                s"mdlId=$mdlId, " +
                                s"srvReqId=${item.srvReqId}, " +
                                s"toks=${delHs.map(_.token).mkString("[", ", ", "]")}" +
                            s"]"
                        )
                    }
                }

                squeezeTokens()
            }

            lastUpdateTstamp = now

            ctx = new util.ArrayList[NCToken](stm.flatMap(_.holders.map(_.token)).asJava)

            ack()
        }
    }

    /**
      * Clears all tokens from this conversation satisfying given predicate.
      *
      * @param p Java-side predicate.
      * @param parent Optional parent span.
      */
    def clearTokens(p: Predicate[NCToken], parent: Span = null): Unit =
        startScopedSpan("clearTokens", parent) { _ =>
            stm.synchronized {
                for (item <- stm)
                    item.holders --= item.holders.filter(h => p.test(h.token))

                squeezeTokens()

                ctx = ctx.asScala.filter(tok => !p.test(tok)).asJava
            }

            logger.trace(s"Conversation is cleared [" +
                s"usrId=$usrId, " +
                s"mdlId=$mdlId" +
            s"]")
        }

    /**
      * Clears all tokens from this conversation satisfying given predicate.
      * 
      * @param p Scala-side predicate.
      */
    def clearTokens(p: NCToken => Boolean): Unit =
        clearTokens(new Predicate[NCToken] {
            override def test(t: NCToken): Boolean = p(t)
        })

    /**
      * Adds given tokens to the conversation.
      *
      * @param srvReqId Server request ID.
      * @param toks Tokens to add to the conversation STM.
      * @param parent Optional parent span.
      */
    def addTokens(srvReqId: String, toks: Seq[NCToken], parent: Span = null): Unit =
        startScopedSpan("addTokens", parent, "srvReqId" -> srvReqId) { _ =>
            stm.synchronized {
                depth = 0
    
                // Last used tokens processing.
                lastToks += toks
    
                val delCnt = lastToks.length - maxDepth
    
                if (delCnt > 0)
                    lastToks.remove(0, delCnt)
    
                val senToks = toks.
                        filter(_.getServerRequestId == srvReqId).
                        filter(t => !t.isFreeWord && !t.isStopWord)
    
                if (senToks.nonEmpty) {
                    // Adds new conversation element.
                    stm += ConversationItem(
                        mutable.ArrayBuffer.empty[TokenHolder] ++ senToks.map(TokenHolder(_)),
                        srvReqId,
                        lastUpdateTstamp
                    )
    
                    logger.trace(
                        s"Added new tokens to the conversation [" +
                            s"usrId=$usrId, " +
                            s"mdlId=$mdlId, " +
                            s"srvReqId=$srvReqId, " +
                            s"toks=${toks.mkString("[", ", ", "]")}" +
                        s"]"
                    )
    
                    val registered = mutable.HashSet.empty[Seq[String]]
    
                    for (
                        item <- stm.reverse;
                        (gs, hs) <- item.holders.groupBy(
                            t => if (t.token.getGroups != null) t.token.getGroups.asScala else Seq.empty
                        )
                    ) {
                        val grps = gs.sorted
    
                        // Reversed iteration.
                        // N : (A, B) -> registered.
                        // N-1 : (C) -> registered.
                        // N-2 : (A, B) or (A, B, X) etc -> deleted, because registered has less groups.
                        registered.find(grps.containsSlice) match {
                            case Some(_) =>
                                item.holders --= hs
    
                                logger.trace(
                                    "Conversation tokens are overridden [" +
                                        s"usrId=$usrId, " +
                                        s"mdlId=$mdlId, " +
                                        s"srvReqId=$srvReqId, " +
                                        s"groups=${grps.mkString(", ")}, " +
                                        s"toks=${hs.map(_.token).mkString("[", ", ", "]")}" +
                                    s"]"
                                )
                            case None => registered += grps
                        }
                    }
    
                    // Updates tokens usage time.
                    stm.foreach(_.holders.filter(h => toks.contains(h.token)).foreach(_.tokenTypeUsageTime = lastUpdateTstamp))
    
                    squeezeTokens()
                }
            }
        }

    /**
      * Prints out ASCII table for current STM.
      */
    private def ack(): Unit = {
        require(Thread.holdsLock(stm))

        if (ctx.isEmpty)
            logger.trace(s"Conversation context is empty for [" +
                s"mdlId=$mdlId, " +
                s"usrId=$usrId" +
            s"]")
        else {
            val tbl = NCAsciiTable("Token ID", "Groups", "Text", "Value", "From request")

            ctx.asScala.foreach(tok => tbl += (
                tok.getId,
                tok.getGroups.asScala.mkString(", "),
                tok.normText,
                tok.getValue,
                tok.getServerRequestId
            ))

            logger.trace(s"Conversation tokens [" +
                s"mdlId=$mdlId, " +
                s"usrId=$usrId" +
            s"]:\n${tbl.toString()}")
        }
    }

    /**
      *
      * @param parent Optional parent span.
      * @return
      */
    def getTokens(parent: Span = null): util.List[NCToken] = startScopedSpan("getTokens", parent) { _ =>
        stm.synchronized {
            val srvReqIds = ctx.asScala.map(_.getServerRequestId).distinct.zipWithIndex.toMap
            val toks = ctx.asScala.groupBy(_.getServerRequestId).toSeq.sortBy(p => srvReqIds(p._1)).reverse.flatMap(_._2)
     
            new util.ArrayList[NCToken](toks.asJava)
        }
    }

    /**
      *
      */
    def getUserData: util.Map[String, Object] = data
}