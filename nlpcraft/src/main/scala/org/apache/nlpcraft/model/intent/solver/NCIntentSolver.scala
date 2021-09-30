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

package org.apache.nlpcraft.model.intent.solver

import com.typesafe.scalalogging.LazyLogging
import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ansi.NCAnsi._
import org.apache.nlpcraft.common.debug.NCLogHolder
import org.apache.nlpcraft.common.opencensus.NCOpenCensusTrace
import org.apache.nlpcraft.common.util.NCUtils
import org.apache.nlpcraft.model.impl.{NCMetadataAdapter, NCVariantImpl}
import org.apache.nlpcraft.model.intent.NCIdlIntent
import org.apache.nlpcraft.model.{NCContext, NCIntentMatch, NCIntentSkip, NCModel, NCRejection, NCResult, NCToken, NCVariant}
import org.apache.nlpcraft.probe.mgrs.dialogflow.NCDialogFlowManager

import java.util.{Collections, List => JList}
import scala.collection.mutable
import scala.jdk.CollectionConverters.{ListHasAsScala, SeqHasAsJava}

/**
 * Front-end for intent solver.
 */
class NCIntentSolver(intents: List[(NCIdlIntent/*Intent*/, NCIntentMatch => NCResult/*Callback*/)])
    extends LazyLogging with NCOpenCensusTrace {
    class RedoSolver extends RuntimeException

    /**
     *
     * @param in
     * @param span
     * @return
     */
    def solve(in: NCIntentSolverInput, span: Span): NCResult = {
        var doIt = true
        var res: NCResult = null
        
        while (doIt)
            try {
                res = solve0(in, span)
                
                doIt = false
            }
            catch {
                case _: RedoSolver => ()
            }
        
        res
    }
    
    /**
     *
     * @param in Intent solver input.
     * @param span Parent span.
     * @return
     * @throws NCRejection
     */
    def solve0(in: NCIntentSolverInput, span: Span): NCResult = {
        if (intents.isEmpty)
            // Should it be an assertion?
            throw new NCRejection("Intent solver has no registered intents.")
        
        val ctx = in.context
        
        val req = ctx.getRequest
        val meta = ctx.getModel.getMetadata
        
        val logHldr = meta synchronized {
            meta.get(NCUtils.mkLogHolderKey(req.getServerRequestId)).asInstanceOf[NCLogHolder]
        }
    
        val results = try NCIntentSolverEngine.solve(ctx, intents, logHldr) catch {
            case e: Exception => throw new NCRejection("Processing failed due to unexpected error.", e)
        }
        
        if (results.isEmpty)
            throw new NCRejection("No matching intent found.")

        var i = -1
    
        for (res <- results if res != null) {
            try {
                i += 1

                val allConvToks = ctx.getConversation.getTokens.asScala
                val vrntNotConvToks = res.variant.tokens.asScala.filterNot(allConvToks.contains)

                val intentToks =
                    res.groups.map(_.tokens).map(toks => {
                        toks.filter(allConvToks.contains).foreach(convTok =>
                            fixBuiltTokenMeta(convTok, vrntNotConvToks, allConvToks))

                        toks.asJava
                    }).asJava

                val intentMatch: NCIntentMatch = new NCMetadataAdapter with NCIntentMatch {
                    override val getContext: NCContext = ctx
                    override val getIntentTokens: JList[JList[NCToken]] = intentToks
                    override val getVariant: NCVariant = new NCVariantImpl(res.variant.tokens)
                    override val getIntentId: String = res.intentId
                    override def getTermTokens(idx: Int): JList[NCToken] = res.groups(idx).tokens.asJava
                    override def getTermTokens(termId: String): JList[NCToken] = res.groups.find(_.termId === termId).flatMap(grp => Some(grp.tokens)).getOrElse(Nil).asJava
                }
                
                if (!in.context.getModel.asInstanceOf[NCModel].onMatchedIntent(intentMatch)) {
                    logger.info(
                        s"Model '${ctx.getModel.getId}' triggered rematching of intents " +
                        s"by intent '${res.intentId}' on variant #${res.variantIdx + 1}."
                    )
                    
                    throw new RedoSolver
                }
                
                var cbRes: NCResult = null
    
                startScopedSpan("intentCallback", span) { _ =>
                    /*
                     * This can throw NCIntentSkip exception.
                     * ======================================
                     */
                    cbRes = res.fn.apply(intentMatch)
                }
                
                // Store won intent match in the input.
                in.intentMatch = intentMatch
                
                // Don't override if user already set it.
                if (cbRes.getTokens == null)
                    cbRes.setTokens(res.groups.flatMap(_.tokens).asJava)

                if (cbRes.getIntentId == null)
                    cbRes.setIntentId(res.intentId)
                    
                logger.info(s"Intent ${ansi256Fg(183)}'${res.intentId}'$ansiReset for variant #${res.variantIdx + 1} selected as the ${g(bo("<|best match|>"))}.")

                NCDialogFlowManager.addMatchedIntent(
                    intentMatch,
                    res,
                    cbRes,
                    ctx,
                    span
                )
                
                if (logHldr != null)
                    logHldr.setMatchedIntentIndex(i)
                    
                return cbRes
            }
            catch {
                case e: NCIntentSkip =>
                    // No-op - just skipping this result.
                    e.getMessage match {
                        case s if s != null => logger.info(s"Selected intent '${res.intentId}' skipped: $s")
                        case _ => logger.info(s"Selected intent '${res.intentId}' skipped.")
                    }
            }
        }
        
        throw new NCRejection("No matching intent found - all intents were skipped.")
    }

    /**
      *
      * @param convTokToFix
      * @param vrntNotConvToks
      * @param allConvToks
      */
    @throws[NCE]
    @throws[NCIntentSkip]
    private def fixBuiltTokenMeta(convTokToFix: NCToken, vrntNotConvToks: Seq[NCToken], allConvToks: Seq[NCToken]): Unit = {
        def isReference(tok: NCToken, id: String, idx: Int): Boolean = tok.getId == id && tok.getIndex == idx

        /**
         * Gets new references candidates.
         *
         * 1. It attempts to find references in the conversation. They should be here because they were not found
         *    among non conversation tokens.
         * 2. Next, it finds common group for all found conversation's references.
         * 3. Next, for the found group, it tries to find actual tokens with this group among non-conversation tokens.
         *    If these non-conversation tokens found, they should be validated and returned. If not found -
         *    conversation tokens returned.
         *
         * @param refId ID of the token being referenced.
         * @param refIdxs Reference indexes.
         * @param validate Validate predicate.
         * @throws NCE It means that we sentence is invalid, internal error.
         * @throws NCIntentSkip It means that we are trying to process an invalid variant and the intent should be skipped.
         */
        @throws[NCE]
        @throws[NCIntentSkip]
        def getNewReferences(refId: String, refIdxs: Seq[Int], validate: Seq[NCToken] => Boolean): Seq[NCToken] = {
            val convRefs = allConvToks.filter(_.getId == refId)

            if (convRefs.map(_.getIndex).sorted != refIdxs.sorted)
                throw new NCE(s"Conversation references are not found [id=$refId, indexes=${refIdxs.mkString(", ")}]")

            val convGrps = convRefs.map(_.getGroups.asScala)
            val commonConvGrps = convGrps.foldLeft(convGrps.head)((g1, g2) => g1.intersect(g2))

            if (commonConvGrps.isEmpty)
                throw new NCE(s"Conversation references don't have common group [id=$refId]")

            val actNonConvRefs = vrntNotConvToks.filter(_.getGroups.asScala.exists(commonConvGrps.contains))

            if (actNonConvRefs.nonEmpty) {
                if (!validate(actNonConvRefs))
                    throw new NCIntentSkip(
                        s"Actual valid variant references are not found for recalculation [" +
                            s"id=$refId, " +
                            s"actualNonConvRefs=${actNonConvRefs.mkString(",")}" +
                        s"]"
                    )

                actNonConvRefs
            }
            else
                convRefs
        }

        convTokToFix.getId match {
            case "nlpcraft:sort" =>
                def getNotNullSeq[T](tok: NCToken, name: String): Seq[T] = {
                    val list = tok.meta[JList[T]](name)

                    if (list == null) Seq.empty else list.asScala
                }

                def process(notesName: String, idxsName: String): Unit = {
                    val refIds: Seq[String] = getNotNullSeq(convTokToFix, s"nlpcraft:sort:$notesName")
                    val refIdxs: Seq[Int] = getNotNullSeq(convTokToFix, s"nlpcraft:sort:$idxsName")

                    require(refIds.length == refIdxs.length)

                    // Can be empty section.
                    if (refIds.nonEmpty) {
                        var data = mutable.ArrayBuffer.empty[(String, Int)]
                        val notFound = mutable.ArrayBuffer.empty[(String, Int)]

                        // Sort references can be of different types.
                        // Part of them can be in conversation, part of them - in the actual variant.
                        refIds.zip(refIdxs).map { case (refId, refIdx) =>
                            val seq =
                                vrntNotConvToks.find(isReference(_, refId, refIdx)) match {
                                    case Some(_) => data
                                    case None => notFound
                                }

                            seq += refId -> refIdx
                        }

                        if (notFound.nonEmpty) {
                            notFound.
                                groupBy { case (refId, _) => refId }.
                                map { case (refId, data) =>  refId -> data.map(_._2) }.
                                foreach { case (refId, refIdxs) =>
                                    getNewReferences(refId, refIdxs, _.size == refIdxs.size).
                                        foreach(t => data += t.getId -> t.getIndex)
                                }

                            data = data.sortBy(_._2)

                            convTokToFix.getMetadata.put(s"nlpcraft:sort:$notesName", data.map(_._1).asJava)
                            convTokToFix.getMetadata.put(s"nlpcraft:sort:$idxsName", data.map(_._2).asJava)
                        }
                    }
                }

                process("bynotes", "byindexes")
                process("subjnotes", "subjindexes")

            case "nlpcraft:limit" =>
                val refId = convTokToFix.meta[String]("nlpcraft:limit:note")
                val refIdxs = convTokToFix.meta[JList[Int]]("nlpcraft:limit:indexes").asScala

                require(refIdxs.size == 1)

                val refIdx = refIdxs.head

                if (!vrntNotConvToks.exists(isReference(_, refId, refIdx))) {
                    val newRef = getNewReferences(refId, Seq(refIdx), _.size == 1).head

                    convTokToFix.getMetadata.put(s"nlpcraft:limit:note", newRef.getId)
                    convTokToFix.getMetadata.put(s"nlpcraft:limit:indexes", Collections.singletonList(newRef.getIndex))
                }

            case "nlpcraft:function" =>
                val refId = convTokToFix.meta[String]("nlpcraft:function:note")
                val refIdxs = convTokToFix.meta[JList[Int]]("nlpcraft:function:indexes").asScala

                require(refIdxs.size == 1)

                val refIdx = refIdxs.head

                if (!vrntNotConvToks.exists(isReference(_, refId, refIdx))) {
                    val newRef = getNewReferences(refId, Seq(refIdx), _.size == 1).head

                    convTokToFix.getMetadata.put(s"nlpcraft:function:note", newRef.getId)
                    convTokToFix.getMetadata.put(s"nlpcraft:function:indexes", Collections.singletonList(newRef.getIndex))
                }

            case "nlpcraft:relation" =>
                val refId = convTokToFix.meta[String]("nlpcraft:relation:note")
                val refIdxs = convTokToFix.meta[JList[Int]]("nlpcraft:relation:indexes").asScala.sorted

                val nonConvRefs = vrntNotConvToks.filter(t => t.getId == refId && refIdxs.contains(t.getIndex))

                if (nonConvRefs.nonEmpty && nonConvRefs.size != refIdxs.size)
                    throw new NCE(s"References are not found [id=$refId, indexes=${refIdxs.mkString(", ")}]")

                if (nonConvRefs.isEmpty) {
                    val newRefs = getNewReferences(refId, refIdxs, _.size == refIdxs.size)
                    val newRefsIds = newRefs.map(_.getId).distinct

                    if (newRefsIds.size != 1)
                        throw new NCE(s"Valid variant references are not found [id=$refId]")

                    convTokToFix.getMetadata.put(s"nlpcraft:relation:note", newRefsIds.head)
                    convTokToFix.getMetadata.put(s"nlpcraft:relation:indexes", newRefs.map(_.getIndex).asJava)
                }

            case _ => // No-op.
        }
    }
}
