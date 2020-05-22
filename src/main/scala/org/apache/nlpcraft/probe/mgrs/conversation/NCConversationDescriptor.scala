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
import java.util.function.Predicate

import com.typesafe.scalalogging.LazyLogging
import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.opencensus.NCOpenCensusTrace
import org.apache.nlpcraft.model._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.duration._

/**
  * Conversation as an ordered set of utterances.
  */
case class NCConversationDescriptor(usrId: Long, mdlId: String) extends LazyLogging with NCOpenCensusTrace {
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
    
    // After 5 mins pause between questions we clear the STM.
    private final val CONV_CLEAR_DELAY = 5.minutes.toMillis

    // If token is not used in last 3 requests, it is removed from the conversation.
    private final val MAX_DEPTH = 3

    // Short-Term-Memory.
    private val stm = mutable.ArrayBuffer.empty[ConversationItem]
    private val lastToks = mutable.ArrayBuffer.empty[Iterable[NCToken]]

    @volatile private var ctx: util.List[NCToken] = new util.ArrayList[NCToken]()
    @volatile private var lastUpdateTstamp = U.nowUtcMs()
    @volatile private var attempt = 0

    /**
      *
      */
    private def squeezeTokens(): Unit = {
        require(Thread.holdsLock(stm))

        stm --= stm.filter(_.holders.isEmpty)
    }

    /**
      * @param parent Optional parent span.
      */
    def updateTokens(parent: Span = null): Unit = startScopedSpan("updateTokens", parent) { _ ⇒
        val now = U.nowUtcMs()

        stm.synchronized {
            attempt += 1

            // Conversation cleared by timeout or when there are too much unsuccessful requests.
            if (now - lastUpdateTstamp > CONV_CLEAR_DELAY) {
                stm.clear()

                logger.info(s"Conversation reset by timeout [" +
                    s"usrId=$usrId, " +
                    s"mdlId=$mdlId" +
                s"]")
            }
            else if (attempt > MAX_DEPTH) {
                stm.clear()
        
                logger.info(s"Conversation reset after too many unsuccessful requests [" +
                    s"usrId=$usrId, " +
                    s"mdlId=$mdlId" +
                s"]")
            }
            else {
                val minUsageTime = now - CONV_CLEAR_DELAY
                val toks = lastToks.flatten

                for (item ← stm) {
                    val delHs =
                        // Deleted by timeout for tokens type or when token type used too many requests ago.
                        item.holders.filter(h ⇒ h.tokenTypeUsageTime < minUsageTime || !toks.contains(h.token))

                    if (delHs.nonEmpty) {
                        item.holders --= delHs

                        logger.info(
                            s"Conversation stale tokens removed [" +
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
    def clearTokens(p: Predicate[NCToken], parent: Span = null): Unit = startScopedSpan("clearTokens", parent) { _ ⇒
        stm.synchronized {
            for (item ← stm)
                item.holders --= item.holders.filter(h ⇒ p.test(h.token))

            squeezeTokens()

            ctx = ctx.asScala.filter(tok ⇒ !p.test(tok)).asJava
        }

        logger.info(s"Manually cleared conversation using token predicate.")
    }

    /**
      * Clears all tokens from this conversation satisfying given predicate.
      * 
      * @param p Scala-side predicate.
      */
    def clearTokens(p: NCToken ⇒ Boolean): Unit =
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
        startScopedSpan("addTokens", parent, "srvReqId" → srvReqId) { _ ⇒
            stm.synchronized {
                attempt = 0
    
                // Last used tokens processing.
                lastToks += toks
    
                val delCnt = lastToks.length - MAX_DEPTH
    
                if (delCnt > 0)
                    lastToks.remove(0, delCnt)
    
                val senToks = toks.
                        filter(_.getServerRequestId == srvReqId).
                        filter(t ⇒ !t.isFreeWord && !t.isStopWord)
    
                if (senToks.nonEmpty) {
                    // Adds new conversation element.
                    stm += ConversationItem(
                        mutable.ArrayBuffer.empty[TokenHolder] ++ senToks.map(TokenHolder(_)),
                        srvReqId,
                        lastUpdateTstamp
                    )
    
                    logger.info(
                        s"Added new tokens to the conversation [" +
                            s"usrId=$usrId, " +
                            s"mdlId=$mdlId, " +
                            s"srvReqId=$srvReqId, " +
                            s"toks=${toks.mkString("[", ", ", "]")}" +
                        s"]"
                    )
    
                    val registered = mutable.HashSet.empty[Seq[String]]
    
                    for (
                        item ← stm.reverse;
                        (gs, hs) ← item.holders.groupBy(
                            t ⇒ if (t.token.getGroups != null) t.token.getGroups.asScala else Seq.empty
                        )
                    ) {
                        val grps = gs.sorted
    
                        // Reversed iteration.
                        // N : (A, B) -> registered.
                        // N-1 : (C) -> registered.
                        // N-2 : (A, B) or (A, B, X) etc -> deleted, because registered has less groups.
                        registered.find(grps.containsSlice) match {
                            case Some(_) ⇒
                                item.holders --= hs
    
                                logger.info(
                                    "Conversation tokens overridden by the \"group rule\" [" +
                                        s"usrId=$usrId, " +
                                        s"mdlId=$mdlId, " +
                                        s"srvReqId=$srvReqId, " +
                                        s"groups=${grps.mkString("[", ", ", "]")}, " +
                                        s"toks=${hs.map(_.token).mkString("[", ", ", "]")}" +
                                    s"]"
                                )
                            case None ⇒ registered += grps
                        }
                    }
    
                    // Updates tokens usage time.
                    stm.foreach(_.holders.filter(h ⇒ toks.contains(h.token)).foreach(_.tokenTypeUsageTime = lastUpdateTstamp))
    
                    squeezeTokens()
                }
            }
        }

    /**
      * Prints out ASCII table for current STM.
      */
    private def ack(): Unit = {
        require(Thread.holdsLock(stm))

        val tbl = NCAsciiTable("Token ID", "Groups", "Text", "Value", "From request")

        ctx.asScala.foreach(tok ⇒ tbl += (
            tok.getId,
            tok.getGroups,
            tok.normText,
            tok.getValue,
            tok.getServerRequestId
        ))

        logger.info(s"Conversation tokens [usrId=$usrId, mdlId=$mdlId]:\n${tbl.toString()}")
    }

    /**
      *
      * @param parent Optional parent span.
      * @return
      */
    def getTokens(parent: Span = null): util.List[NCToken] = startScopedSpan("getTokens", parent) { _ ⇒
        stm.synchronized {
            val srvReqIds = ctx.asScala.map(_.getServerRequestId).distinct.zipWithIndex.toMap
            val toks = ctx.asScala.groupBy(_.getServerRequestId).toSeq.sortBy(p ⇒ srvReqIds(p._1)).reverse.flatMap(_._2)
     
            new util.ArrayList[NCToken](toks.asJava)
        }
    }
}