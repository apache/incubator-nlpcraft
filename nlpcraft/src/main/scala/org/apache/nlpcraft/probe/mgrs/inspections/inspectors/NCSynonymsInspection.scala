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

import org.apache.nlpcraft.common.inspections.NCInspectionService
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.model.NCModel

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Inspection for model's synonyms.
 */
object NCSynonymsInspection extends NCInspectionService {
    // Pretty arbitrary number...
    // TODO: make it part of inspection configuration.
    private final val TOO_MANY_SYNS = 10000

    override def getName: String = "synonyms"

    override def bodyOnProbe(
        mdl: NCModel,
        args: Option[String],
        suggs: mutable.Buffer[String],
        warns: mutable.Buffer[String],
        errs: mutable.Buffer[String]): Unit = {
        if (args.isDefined && args.get.nonEmpty)
            warns += s"Invalid inspection arguments (ignoring): ${args.get}"

        val parser = new NCMacroParser()

        mdl.getMacros.asScala.foreach { case (name, str) ⇒ parser.addMacro(name, str) }

        val mdlSyns = mdl.getElements.asScala.map(p ⇒ p.getId → p.getSynonyms.asScala.flatMap(parser.expand))

        mdlSyns.foreach { case (elemId, syns) ⇒
            val size = syns.size

            if (size == 0)
                warns += s"Element '$elemId' doesn't have synonyms."
            else if (size > TOO_MANY_SYNS)
                warns += s"Element '$elemId' has too many ($size) synonyms. Make sure this is truly necessary."

            val others = mdlSyns.filter {
                case (otherId, _) ⇒ otherId != elemId
            }

            val cross = others.filter {
                case (_, othSyns) ⇒ othSyns.intersect(syns).nonEmpty
            }.toMap.keys.mkString(",")

            if (cross.nonEmpty)
                errs += s"Element '$elemId' has duplicate synonyms with elements '$cross'."
        }
    }
}
