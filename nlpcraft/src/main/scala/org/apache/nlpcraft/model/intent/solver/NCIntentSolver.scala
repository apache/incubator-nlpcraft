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

import scala.collection.JavaConverters._

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
                
                val intentMatch: NCIntentMatch = new NCMetadataAdapter with NCIntentMatch {
                    override val getContext: NCContext = ctx
                    override val getIntentTokens: java.util.List[java.util.List[NCToken]] = res.groups.map(_.tokens.asJava).asJava
                    override val getVariant: NCVariant = new NCVariantImpl(res.variant.tokens)
                    override val isAmbiguous: Boolean = !res.isExactMatch
                    override val getIntentId: String = res.intentId
                    override def getTermTokens(idx: Int): java.util.List[NCToken] = res.groups(idx).tokens.asJava
                    override def getTermTokens(termId: String): java.util.List[NCToken] = res.groups.find(_.termId === termId).flatMap(grp => Some(grp.tokens)).getOrElse(Nil).asJava
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
}
