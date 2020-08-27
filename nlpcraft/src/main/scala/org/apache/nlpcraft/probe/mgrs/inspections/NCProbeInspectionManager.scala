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
import org.apache.nlpcraft.common.inspections.NCInspectionType._
import org.apache.nlpcraft.common.inspections.{NCInspection, NCInspectionType}
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.model.opencensus.stats.NCOpenCensusModelStats
import org.apache.nlpcraft.probe.mgrs.inspections.inspectors._

object NCProbeInspectionManager extends NCService with NCOpenCensusModelStats {
    private final val INSPECTORS =
        Map(
            SUGGEST_SYNONYMS → NCInspectorSynonymsSuggestions,
            INSPECTION_MACROS → NCInspectorMacros,
            INSPECTION_SYNONYMS → NCInspectorSynonyms,
            INSPECTION_INTENTS → NCInspectorIntents
        )

    require(NCInspectionType.values.forall(INSPECTORS.contains))

    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ ⇒
        INSPECTORS.values.foreach(_.start())

        super.start(parent)
    }

    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()

        INSPECTORS.values.foreach(_.stop())
    }

    @throws[NCE]
    def inspect(mdlId: String, types: Seq[NCInspectionType], parent: Span = null): Map[NCInspectionType, NCInspection] =
        startScopedSpan("inspect", parent) { _ ⇒
            types.map(t ⇒  t → INSPECTORS(t).inspect(mdlId, parent = parent)).toMap
        }
}
