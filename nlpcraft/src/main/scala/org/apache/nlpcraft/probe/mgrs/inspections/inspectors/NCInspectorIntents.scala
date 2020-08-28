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
import org.apache.nlpcraft.common.inspections.{NCInspection, NCInspector}
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.model.intent.impl.NCIntentScanner
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager

object NCInspectorIntents extends NCService with NCInspector {
    override def inspect(mdlId: String, prevLayerInspection: Option[NCInspection], parent: Span = null): NCInspection =
        startScopedSpan("inspect", parent) { _ ⇒
            val warns = NCIntentScanner.validateIntentsSamples(
                NCModelManager.getModel(mdlId).getOrElse(throw new NCE(s"Model not found: '$mdlId'")).model.proxy
            )

            NCInspection(errors = None, warnings = if (warns.isEmpty) None else Some(warns), suggestions = None, data = None)
        }
}
