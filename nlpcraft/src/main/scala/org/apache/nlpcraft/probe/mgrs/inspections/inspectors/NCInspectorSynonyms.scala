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
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager

import scala.collection.JavaConverters._
import scala.collection.mutable

// TODO:
object NCInspectorSynonyms extends NCService with NCInspector {
    private final val TOO_MANY_SYNS = 10000

    override def inspect(mdlId: String, prevLayerInspection: Option[NCInspection], parent: Span = null): NCInspection =
        startScopedSpan("inspect", parent, "modelId" → mdlId) { _ ⇒
            val mdl = NCModelManager.getModel(mdlId).getOrElse(throw new NCE(s"Model not found: '$mdlId'")).model

            val warns = mutable.ArrayBuffer.empty[String]

            val parser = new NCMacroParser()

            mdl.getMacros.asScala.foreach { case (name, str) ⇒ parser.addMacro(name, str) }

            val mdlSyns = mdl.getElements.asScala.map(p ⇒ p.getId → p.getSynonyms.asScala.flatMap(parser.expand))

            mdlSyns.foreach { case (elemId, syns) ⇒
                val size = syns.size

                if (size == 0)
                    warns += s"Element: '$elemId' doesn't have synonyms"
                else if (size > TOO_MANY_SYNS)
                    warns += s"Element: '$elemId' has too many synonyms: $size"

                val others = mdlSyns.filter { case (othId, _) ⇒ othId != elemId}

                val intersects =
                    others.filter { case (_, othSyns) ⇒ othSyns.intersect(syns).nonEmpty }.toMap.keys.mkString(",")

                if (intersects.nonEmpty)
                    warns += s"Element: '$elemId' has same synonyms with '$intersects'"
            }

            NCInspection(errors = None, warnings = if (warns.isEmpty) None else Some(warns), suggestions = None)
        }
}
