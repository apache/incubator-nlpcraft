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

package org.apache.nlpcraft.internal.conversation

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.ascii.*
import org.apache.nlpcraft.internal.util.*

import java.util
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/**
  * An active conversation is an ordered set of utterances for the specific user and data model.
  */
case class NCConversationData(
    usrId: String,
    mdlId: String,
    timeoutMs: Long,
    maxDepth: Int
) extends LazyLogging:
    private final val data = new NCPropertyMapAdapter()

    case class EntityHolder(entity: NCEntity, var entityTypeUsageTime: Long = 0)
    case class ConversationItem(holders: mutable.ArrayBuffer[EntityHolder], reqId: String, tstamp: Long)

    // Short-Term-Memory.
    private val stm = mutable.ArrayBuffer.empty[ConversationItem]
    private val lastEnts = mutable.ArrayBuffer.empty[Iterable[NCEntity]]
    private val ctx = mutable.ArrayBuffer.empty[NCEntity]

    /**
      *
      */
    val getUserData: NCPropertyMap = data


    @volatile private var lastUpdateTstamp = NCUtils.nowUtcMs()
    @volatile private var depth = 0

    /**
      *
      * @param newCtx
      */
    private def replaceContext(newCtx: mutable.ArrayBuffer[NCEntity]): Unit =
        require(Thread.holdsLock(stm))
        ctx.clear()
        ctx ++= newCtx

    /**
      *
      */
    private def squeezeEntities(): Unit =
        require(Thread.holdsLock(stm))
        stm --= stm.filter(_.holders.isEmpty)

    /**
      * Gets called on each input request for given user and model.
      */
    def updateEntities(): Unit =
        val now = NCUtils.nowUtcMs()

        stm.synchronized {
            depth += 1

            lazy val z = s"usrId=$usrId, mdlId=$mdlId"

            // Conversation cleared by timeout or when there are too much unsuccessful requests.
            if now - lastUpdateTstamp > timeoutMs then
                stm.clear()
                logger.trace(s"STM is reset by timeout [$z]")
            else if depth > maxDepth then
                stm.clear()
                logger.trace(s"STM is reset after reaching max depth [$z]")
            else
                val minUsageTime = now - timeoutMs
                val ents = lastEnts.flatten

                for (item <- stm)
                    val delHs =
                        // Deleted by timeout for entity type or when an entity type used too many requests ago.
                        item.holders.filter(h => h.entityTypeUsageTime < minUsageTime || !ents.contains(h.entity))

                    if delHs.nonEmpty then
                        item.holders --= delHs
                        logger.trace(s"STM entity removed [$z, reqId=${item.reqId}]")
                        stepLogEntity(delHs.toSeq.map(_.entity))

                squeezeEntities()

            lastUpdateTstamp = now
            replaceContext(stm.flatMap(_.holders.map(_.entity)))
            ack()
        }

    /**
      * Clears all entities from this conversation satisfying given predicate.
      *
      * @param p Java-side predicate.
      */
    def clear(p: Predicate[NCEntity]): Unit =
        stm.synchronized {
            for (item <- stm) item.holders --= item.holders.filter(h => p.test(h.entity))
            squeezeEntities()
            replaceContext(ctx.filter(ent => !p.test(ent)))
        }

        logger.trace(s"STM is cleared [usrId=$usrId, mdlId=$mdlId]")

    /**
      *
      * @param ents
      */
    private def stepLogEntity(ents: Seq[NCEntity]): Unit =
        for (ent <- ents) logger.trace(s"  +-- $ent")

    /**
      * Adds given entities to the conversation.
      *
      * @param reqId Server request ID.
      * @param ents Entities to add to the conversation STM.
      */
    def addEntities(reqId: String, ents: Seq[NCEntity]): Unit =
        stm.synchronized {
            depth = 0
            lastEnts += ents // Last used entities processing.

            val delCnt = lastEnts.length - maxDepth
            if delCnt > 0 then lastEnts.remove(0, delCnt)

            val senEnts = ents.filter(_.getRequestId == reqId)
            if senEnts.nonEmpty then
                // Adds new conversation element.
                stm += ConversationItem(
                    mutable.ArrayBuffer.empty[EntityHolder] ++ senEnts.map(EntityHolder(_)),
                    reqId,
                    lastUpdateTstamp
                )

                logger.trace(s"Added new entities to STM [usrId=$usrId, mdlId=$mdlId, reqId=$reqId]")
                stepLogEntity(ents)

                val registered = mutable.HashSet.empty[Seq[String]]
                for (item <- stm.reverse; (gs, hs) <- item.holders.groupBy(t => if (t.entity.getGroups != null) t.entity.getGroups.asScala else Seq.empty))
                    val grps = gs.toSeq.sorted

                    // Reversed iteration.
                    // N : (A, B) -> registered.
                    // N-1 : (C) -> registered.
                    // N-2 : (A, B) or (A, B, X) etc -> deleted, because registered has less groups.
                    registered.find(grps.containsSlice) match
                        case Some(_) =>
                            item.holders --= hs
                            for (ent <- hs.map(_.entity)) logger.trace(s"STM entity overridden: $ent")

                        case None => registered += grps

                // Updates entity usage time.
                stm.foreach(_.holders.filter(h => ents.contains(h.entity)).foreach(_.entityTypeUsageTime = lastUpdateTstamp))

                squeezeEntities()
        }

    /**
      * Prints out ASCII table for current STM.
      */
    private def ack(): Unit =
        require(Thread.holdsLock(stm))

        val z = s"mdlId=$mdlId, usrId=$usrId"

        if ctx.isEmpty then logger.trace(s"STM is empty for [$z]")
        else
            val tbl = NCAsciiTable("Entity ID", "Groups", "Request ID")
            ctx.foreach(ent => tbl += (
                ent.getId,
                ent.getGroups.asScala.mkString(", "),
                ent.getRequestId
            ))
            logger.info(s"Current STM for [$z]:\n${tbl.toString()}")

    /**
      *
      * @return
      */
    def getEntities: Seq[NCEntity] = stm.synchronized {
        val reqIds = ctx.map(_.getRequestId).distinct.zipWithIndex.toMap
        ctx.groupBy(_.getRequestId).toSeq.sortBy(p => reqIds(p._1)).reverse.flatMap(_._2)
    }

    /**
      *
      */
    def clear(): Unit = stm.synchronized {
        ctx.clear()
        stm.clear()
        lastEnts.clear()
        data.clear()
    }
