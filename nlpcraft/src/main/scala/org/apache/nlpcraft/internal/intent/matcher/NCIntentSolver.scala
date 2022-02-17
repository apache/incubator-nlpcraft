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

package org.apache.nlpcraft.internal.intent.matcher

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.dialogflow.NCDialogFlowManager
import org.apache.nlpcraft.internal.intent.NCIDLIntent

import java.util.{Collections, List as JList}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/**
 * Front-end for intent solver.
 */
class NCIntentSolver(
    engine: NCIntentSolverEngine,
    dialog: NCDialogFlowManager,
    // TODO: order.
    // TODO: NCIntentSolverInput contains model.
    // TODO: logic with RedoSolver.
    // TODO: NCIntentMatcher API.
    // TODO: why 2 classes NCIntentSolver and NCIntentSolverEngine.
    intents: Map[NCIDLIntent, NCIntentMatch => NCResult]
) extends LazyLogging:
    /**
     *
     * @param in
     * @param span
     * @return
     */
    def solve(in: NCIntentSolverInput): NCResult =
        var res: NCResult = null

        while (res != null)
          solve0(in) match
              case Some(solverRes) => res = solverRes
              case None => // No-op.

        res

    /**
     *
     * @param in Intent solver input.
     * @param span Parent span.
     * @return
     * @throws NCRejection
     */
    private def solve0(in: NCIntentSolverInput): Option[NCResult] =
        // Should it be an assertion?
        if intents.isEmpty then throw new NCRejection("Intent solver has no registered intents.")
        
        val ctx = in.context
        val req = ctx.getRequest

        val results =
            try engine.solve(ctx, intents)
            catch case e: Exception => throw new NCRejection("Processing failed due to unexpected error.", e)

        if results.isEmpty then throw new NCRejection("No matching intent found.")

        var i = -1

        for (res <- results if res != null)
            try
                i += 1

                val intentMatch: NCIntentMatch =
                    new NCIntentMatch:
                        override val getIntentId: String = res.intentId
                        override val getIntentEntities: JList[JList[NCEntity]] = res.groups.map(_.entities).map(_.asJava).asJava
                        override def getTermEntities(idx: Int): JList[NCEntity] = res.groups(idx).entities.asJava
                        override def getTermEntities(termId: String): JList[NCEntity] =
                            res.groups.find(_.termId === termId) match
                                case Some(g) => g.entities.asJava
                                case None => Collections.emptyList()
                        override val getVariant: NCVariant =
                            new NCVariant:
                                override def getEntities: JList[NCEntity] = res.variant.entities.asJava

                if !in.model.onMatchedIntent(intentMatch) then
                    logger.info(
                        s"Model '${ctx.getModelConfig.getId}' triggered rematching of intents by intent '${res.intentId}' on variant #${res.variantIdx + 1}."
                    )

                    return None

                // This can throw NCIntentSkip exception.
                val cbRes = res.fn(intentMatch)
    
                // Store won intent match in the input.
                in.intentMatch = intentMatch

                if cbRes.getIntentId == null then
                    cbRes.setIntentId(res.intentId)
                    
                logger.info(s"Intent '${res.intentId}' for variant #${res.variantIdx + 1} selected as the <|best match|>")

                dialog.addMatchedIntent(intentMatch, cbRes, ctx)
                
                return Option(cbRes)
            catch
                case e: NCIntentSkip =>
                    // No-op - just skipping this result.
                    e.getMessage match
                        case s if s != null => logger.info(s"Selected intent '${res.intentId}' skipped: $s")
                        case _ => logger.info(s"Selected intent '${res.intentId}' skipped.")
        
        throw new NCRejection("No matching intent found - all intents were skipped.")
