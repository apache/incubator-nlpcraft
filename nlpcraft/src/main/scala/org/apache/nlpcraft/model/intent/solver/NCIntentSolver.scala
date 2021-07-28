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
                val nonConvToks = res.groups.flatMap(_.tokens).filterNot(allConvToks.contains)

                val intentToks =
                    res.groups.map(_.tokens).map(toks => {
                        toks.filter(allConvToks.contains).foreach(convTok =>
                            fixBuiltTokensMeta(convTok, nonConvToks, allConvToks))

                        toks.asJava
                    }).asJava

                ctx.getConversation.getTokens

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
                    
                logger.info(s"Intent '${res.intentId}' for variant #${res.variantIdx + 1} selected as the ${g(bo("'best match'"))}.")

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
      * @param convTok
      * @param nonConvToks
      * @param allConvToks
      */
    @throws[NCE]
    private def fixBuiltTokensMeta(convTok: NCToken, nonConvToks: Seq[NCToken], allConvToks: Seq[NCToken]): Unit = {
        def isReference(tok: NCToken, id: String, idx: Int): Boolean = tok.getId == id && tok.getIndex == idx

        /**
          * Gets new references candidates.
          *
          * Initially, it finds common group for all conversation's references.
          * Next, for found group, it tries to find tokens with this group among non-conversation tokens.
          * If these non-conversation tokens found, they should be validated.
          *
          * @param complexTokId Token id, which has references.
          * @param convRefs Conversation references. Valid references which found in conversation.
          * @param nonConvToks Non conversation tokens.
          * @param validate Validate predicate.
          */
        @throws[NCE]
        def getForRecalc(
            complexTokId: String, convRefs: Seq[NCToken], nonConvToks: Seq[NCToken], validate: Seq[NCToken] => Boolean
        ): Seq[NCToken] = {
            val convGs = convRefs.map(_.getGroups.asScala)
            val commonConvGs = convGs.foldLeft(convGs.head)((g1, g2) => g1.intersect(g2))

            if (commonConvGs.isEmpty)
                throw new NCE(s"Conversation references don't have common group [id=$complexTokId]")

            val actualRefs = nonConvToks.filter(_.getGroups.asScala.exists(commonConvGs.contains))

            if (actualRefs.nonEmpty) {
                if (!validate(actualRefs))
                    throw new NCE(s"Variant references are not found for recalculation [tokenId=$complexTokId]")

                actualRefs
            }
            else
                convRefs
        }

        convTok.getId match {
            case "nlpcraft:sort" =>
                def getNotNullSeq[T](tok: NCToken, name: String): Seq[T] = {
                    val list = tok.meta[JList[T]](name)

                    if (list == null) Seq.empty else list.asScala
                }

                def process(notesName: String, idxsName: String): Unit = {
                    val refIds: Seq[String] = getNotNullSeq(convTok, s"nlpcraft:sort:$notesName")
                    val refIdxs: Seq[Int] = getNotNullSeq(convTok, s"nlpcraft:sort:$idxsName")

                    require(refIds.length == refIdxs.length)

                    // Can be empty section.
                    if (refIds.nonEmpty) {
                        var data = mutable.ArrayBuffer.empty[(String, Int)]
                        val notFound = mutable.ArrayBuffer.empty[(String, Int)]

                        // Sort references can be different types.
                        // Part of them can be in conversation, part of them - in actual variant.
                        refIds.zip(refIdxs).map { case (refId, refIdx) =>
                            val seq =
                                nonConvToks.find(isReference(_, refId, refIdx)) match {
                                    case Some(_) => data
                                    case None => notFound
                                }

                            seq += refId -> refIdx
                        }

                        if (notFound.nonEmpty) {
                            notFound.
                                groupBy { case (refId, _) => refId }.
                                map { case (refId, data) =>  refId -> data.map(_._2).sorted }.
                                foreach { case (refId, refIdxs) =>
                                    val convRefs = allConvToks.filter(_.getId == refId)

                                    if (convRefs.map(_.getIndex).sorted != refIdxs)
                                        throw new NCE(
                                            s"Conversation references are not found [id=$refId, " +
                                            s"indexes=${refIdxs.mkString(", ")}]"
                                        )

                                    getForRecalc(refId, convRefs, nonConvToks, _.size != refIdxs.size).
                                        foreach(t => data += t.getId -> t.getIndex)
                                }

                            data = data.sortBy(_._2)

                            convTok.getMetadata.put(s"nlpcraft:sort:$notesName", data.map(_._1).asJava)
                            convTok.getMetadata.put(s"nlpcraft:sort:$idxsName", data.map(_._2).asJava)
                        }
                    }
                }

                process("bynotes", "byindexes")
                process("subjnotes", "subjindexes")
            case "nlpcraft:limit" =>
                val refId = convTok.meta[String]("nlpcraft:limit:note")
                val refIdxs = convTok.meta[JList[Int]]("nlpcraft:limit:indexes").asScala

                require(refIdxs.size == 1)

                val refIdx = refIdxs.head

                if (!nonConvToks.exists(isReference(_, refId, refIdx))) {
                    val convRefs = allConvToks.filter(_.getId == refId)

                    if (convRefs.size != 1 || convRefs.head.getIndex != refIdx)
                        throw new NCE(s"Conversation reference is not found [id=$refId, index=$refIdx]")

                    val ref = getForRecalc(refId, convRefs, nonConvToks, _.size == 1).head

                    convTok.getMetadata.put(s"nlpcraft:limit:note", ref.getId)
                    convTok.getMetadata.put(s"nlpcraft:limit:indexes", Collections.singleton(ref.getIndex))
                }

            case "nlpcraft:relation" =>
                val refId = convTok.meta[String]("nlpcraft:relation:note")
                val refIdxs = convTok.meta[JList[Int]]("nlpcraft:relation:indexes").asScala.sorted

                val nonConvRefs = nonConvToks.filter(t => t.getId == refId && refIdxs.contains(t.getIndex))

                if (nonConvRefs.nonEmpty && nonConvRefs.size != refIdxs.size)
                    throw new NCE(s"References are not found [id=$refId, indexes=${refIdxs.mkString(", ")}]")

                if (nonConvRefs.isEmpty) {
                    val convRefs = allConvToks.filter(t => t.getId == refId  && refIdxs.contains(t.getIndex))

                    if (convRefs.size != refIdxs.size)
                        throw new NCE(
                            s"Conversation references are not found [id=$refId, " +
                                s"indexes=${refIdxs.mkString(", ")}]"
                        )

                    val refs = getForRecalc(refId, convRefs, nonConvToks, _.size == refIdxs.size)

                    val refsIds = refs.map(_.getId).distinct

                    if (refsIds.size != 1)
                        throw new NCE(s"Variant references are not found [id=$refId, count=${refIdxs.size}]")

                    convTok.getMetadata.put(s"nlpcraft:relation:note", refsIds.head)
                    convTok.getMetadata.put(s"nlpcraft:relation:indexes", refs.map(_.getIndex).asJava)
                }

            case _ => // No-op.
        }
    }
}
