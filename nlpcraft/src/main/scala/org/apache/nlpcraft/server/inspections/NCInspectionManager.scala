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

package org.apache.nlpcraft.server.inspections

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.inspections.impl.{NCInspectionImpl, NCInspectionParameterImpl}
import org.apache.nlpcraft.common.inspections.{NCInspection, NCInspectionResult, NCInspectionService}
import org.apache.nlpcraft.server.inspections.inspectors.NCInspectorSuggestions
import org.apache.nlpcraft.server.probe.NCProbeManager

import scala.collection.Map
import scala.concurrent.Future

/**
 * Model inspection manager.
 */
object NCInspectionManager extends NCService {
    private final val ALL_INSPECTIONS: Seq[NCInspection] = Seq(
        NCInspectionImpl(
            id = "macros",
            name = "macros",
            synopsis = "macros",
            parameters = Seq.empty,
            description = "macros",
            isServerSide = false
        ),
        NCInspectionImpl(
            id = "intents",
            name = "intents",
            synopsis = "intents",
            parameters = Seq.empty,
            description = "intents",
            isServerSide = false
        ),
        NCInspectionImpl(
            id = "synonyms",
            name = "synonyms",
            synopsis = "synonyms",
            parameters = Seq.empty,
            description = "synonyms",
            isServerSide = false
        ),
        NCInspectionImpl(
            id = "suggestions",
            name = "suggestions",
            synopsis = "suggestions",
            parameters = Seq(
                NCInspectionParameterImpl(
                    id = "minScore",
                    name = "minScore",
                    value = "minScore",
                    valueType = "double",
                    synopsis = "minScore, range  between 0 and 1",
                    description = "minScore"
                )
            ),
            description = "suggestions",
            isServerSide = true
        )
    )

    private final val SRV_INSPECTIONS = Map[String, NCInspectionService](
        "suggestions" → NCInspectorSuggestions
    )

    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ ⇒
        SRV_INSPECTIONS.values.foreach(_.start())

        super.start(parent)
    }

    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()

        SRV_INSPECTIONS.values.foreach(_.stop(parent))
    }

    /**
     *
     * @param mdlId Model ID.
     * @param inspId Inspection ID.
     * @param args Inspection arguments .
     * @param parent Optional parent trace span.
     */
    def inspect(mdlId: String, inspId: String, args: Option[String], parent: Span = null): Future[NCInspectionResult] =
        SRV_INSPECTIONS.get(inspId) match {
            case Some(insp) ⇒ insp.inspect(mdlId, inspId, args, parent)
            case None ⇒ NCProbeManager.runInspection(mdlId, inspId, args, parent)
        }

    /**
     * Gets all supported server and probe inspections.
     *
     * @param parent
     * @return
     */
    def allInspections(parent: Span = null): Seq[NCInspection] = ALL_INSPECTIONS
}
