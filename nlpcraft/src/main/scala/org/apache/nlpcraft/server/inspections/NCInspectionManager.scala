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
import org.apache.nlpcraft.common.inspections.impl.NCInspectionImpl
import org.apache.nlpcraft.common.inspections.{NCInspection, NCInspectionResult}
import org.apache.nlpcraft.server.inspections.inspectors.NCInspectorSuggestions
import org.apache.nlpcraft.server.probe.NCProbeManager

import scala.collection.Map
import scala.concurrent.Future

/**
 *
 */
object NCInspectionManager extends NCService {
    private final val INSPECTIONS: Seq[NCInspection] =
        Seq(
            NCInspectionImpl(
                id = "macros",
                name = "macros",
                synopsis = "macros",
                parameters = Seq.empty,
                description = "macros",
                isServerSide = false
            ),
            NCInspectionImpl(
                id = "elements",
                name = "elements",
                synopsis = "elements",
                parameters = Seq.empty,
                description = "elements",
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
                id = "synonyms_suggestions",
                name = "synonyms_suggestions",
                synopsis = "synonyms_suggestions",
                parameters = Seq.empty,
                description = "synonyms_suggestions",
                isServerSide = true
            )
        )

    private final val SRV_INSPECTORS =
        Map(
            "suggestions" → NCInspectorSuggestions
        )

    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ ⇒
        SRV_INSPECTORS.values.foreach(_.start())

        super.start(parent)
    }

    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()

        SRV_INSPECTORS.values.foreach(_.stop(parent))
    }

    /**
     *
     * @param mdlId Model ID.
     * @param inspId Inspection ID.
     * @param args Inspection arguments .
     * @param parent Optional parent trace span.
     */
    def inspect(mdlId: String, inspId: String, args: String, parent: Span = null): Future[NCInspectionResult] =
        SRV_INSPECTORS.get(inspId) match {
            case Some(inspector) ⇒ inspector.inspect(mdlId, inspId, args, parent)
            case None ⇒ NCProbeManager.getProbeInspection(mdlId, inspId, args, parent)
        }
    /**
     * Gets all supported server and probe inspections.
     *
     * @param parent
     * @return
     */
    def getInspections(parent: Span = null): Seq[NCInspection] = INSPECTIONS
}
