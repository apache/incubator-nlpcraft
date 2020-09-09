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

package org.apache.nlpcraft.probe.mgrs.inspections.inspectors

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.inspections.impl.NCInspectionResultImpl
import org.apache.nlpcraft.common.inspections.{NCInspectionResult, NCInspectionService}
import org.apache.nlpcraft.model.NCModel
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager

import scala.collection.mutable
import scala.concurrent.Future

trait NCProbeInspection extends NCInspectionService {
    /**
     *
     * @param mdlId
     * @param inspName
     * @param args
     * @param parent
     * @return
     */
    override def inspect(mdlId: String, inspName: String, args: Option[String], parent: Span = null): Future[NCInspectionResult] = {
        val now = System.currentTimeMillis()

        startScopedSpan(
            "inspect",
            parent,
            "modelId" → mdlId,
            "inspName" → inspName,
            "args" → args.orNull) { _ ⇒
            Future {
                val errs = mutable.Buffer.empty[String]
                val warns = mutable.Buffer.empty[String]
                val suggs = mutable.Buffer.empty[String]

                NCModelManager.getModelWrapper(mdlId) match {
                    case Some(x) ⇒ body(x.proxy, args, suggs, warns, errs)
                    case None ⇒ errs += s"Model not found: $mdlId"
                }

                NCInspectionResultImpl(
                    inspectionId = inspName,
                    modelId = mdlId,
                    durationMs = System.currentTimeMillis() - now,
                    timestamp = now,
                    warnings = warns,
                    suggestions = suggs,
                    errors = errs
                )
            }(getExecutor)
        }
    }

    /**
     * Convenient adapter for the probe-side inspection implementation.
     *
     * @param mdl
     * @param args
     * @param suggs Mutable collector for suggestions.
     * @param warns Mutable collector for warnings.
     * @param errs Mutable collector for errors.
     */
    protected def body(
        mdl: NCModel,
        args: Option[String],
        suggs: mutable.Buffer[String],
        warns: mutable.Buffer[String],
        errs: mutable.Buffer[String]
    )
}
