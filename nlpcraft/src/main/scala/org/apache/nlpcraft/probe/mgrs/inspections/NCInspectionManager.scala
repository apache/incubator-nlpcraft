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
import org.apache.nlpcraft.probe.mgrs.inspections.inspectors.{NCIntentsInspection, NCMacrosInspection, NCSynonymsInspection}

import scala.concurrent.Future

/**
 * Probe-side inspection manager.
 */
object NCInspectionManager extends NCService with NCOpenCensusModelStats {
    private final val PROBE_INSPECTIONS =
        Seq(
            NCMacrosInspection,
            NCSynonymsInspection,
            NCIntentsInspection
        )

    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ ⇒
        PROBE_INSPECTIONS.foreach(_.start(parent))

        super.start(parent)
    }

    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()

        PROBE_INSPECTIONS.foreach(_.stop(parent))
    }

    /**
     *
     * @param mdlId Model ID.
     * @param inspName Inspection ID.
     * @param args Inspection arguments.
     * @param parent Optional parent trace span.
     */
    def inspect(mdlId: String, inspName: String, args: Option[String], parent: Span = null): Future[NCInspectionResult] =
        startScopedSpan(
            "inspect",
            parent,
            "modelId" → mdlId,
            "inspName" → inspName,
            "args" -> args.orNull) { _ ⇒
            PROBE_INSPECTIONS.find(_.getName == inspName) match {
                case Some(insp) ⇒ insp.inspect(mdlId, inspName, args)
                case None ⇒ throw new NCE(s"Unsupported inspection: $inspName")
            }
        }
}
