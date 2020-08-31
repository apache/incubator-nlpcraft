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

package org.apache.nlpcraft.probe.mgrs.inspections

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.inspections.NCInspectionResult
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.model.opencensus.stats.NCOpenCensusModelStats
import org.apache.nlpcraft.probe.mgrs.inspections.inspectors.{NCInspectorIntents, NCInspectorMacros, NCInspectorSynonyms}

import scala.concurrent.Future

/**
 * TODO:
 */
object NCInspectionManager extends NCService with NCOpenCensusModelStats {
    private final val INSPECTORS =
        Map(
            "macros" → NCInspectorMacros,
            "synonyms" → NCInspectorSynonyms,
            "intents" → NCInspectorIntents
        )

    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ ⇒
        INSPECTORS.values.foreach(_.start(parent))

        super.start(parent)
    }

    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()

        INSPECTORS.values.foreach(_.stop(parent))
    }

    /**
     *
     * @param mdlId Model ID.
     * @param inspId Inspection ID.
     * @param args Inspection arguments.
     * @param parent Optional parent trace span.
     */
    def inspect(mdlId: String, inspId: String, args: Option[String], parent: Span = null): Future[NCInspectionResult] =
        startScopedSpan("inspect", parent, "modelId" → mdlId, "inspectionId" → inspId) { _ ⇒
            INSPECTORS.get(inspId) match {
                case Some(inspector) ⇒ inspector.inspect(mdlId, inspId, args)
                case None ⇒ throw new NCE(s"Unsupported inspection: $inspId")
            }
        }
}
