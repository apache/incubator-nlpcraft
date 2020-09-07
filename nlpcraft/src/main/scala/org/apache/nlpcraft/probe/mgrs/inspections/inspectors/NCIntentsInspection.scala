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
import org.apache.nlpcraft.common.nlp.core.NCNlpPorterStemmer
import org.apache.nlpcraft.model.NCModel
import org.apache.nlpcraft.model.intent.impl.NCIntentScanner

import scala.collection.JavaConverters._
import scala.collection._

/**
 * Inspection for model's intents.
 */
object NCIntentsInspection extends NCInspectionService {
    private final val SEPARATORS = Seq('?', ',', '.', '-', '!')

    override def getName: String = "intents"

    override def bodyOnProbe(
        mdl: NCModel,
        args: Option[String],
        suggs: mutable.Buffer[String],
        warns: mutable.Buffer[String],
        errs: mutable.Buffer[String]): Unit = {
        if (args.isDefined && args.get.nonEmpty)
            warns += s"Invalid inspection arguments (ignoring): ${args.get}"

        val res = NCIntentScanner.scanIntentsSamples(mdl)

        val parser = new NCMacroParser

        mdl.getMacros.asScala.foreach { case (name, str) ⇒ parser.addMacro(name, str) }

        val allSyns: Set[Seq[String]] =
            mdl.getElements.
                asScala.
                flatMap(_.getSynonyms.asScala.flatMap(parser.expand)).
                map(NCNlpPorterStemmer.stem).map(_.split(" ").toSeq).
                toSet

        res.samples.
            flatMap { case (_, samples) ⇒ samples.map(_.toLowerCase) }.
            map(s ⇒ s → SEPARATORS.foldLeft(s)((s, ch) ⇒ s.replaceAll(s"\\$ch", s" $ch "))).
            foreach {
                case (s, sNorm) ⇒
                    val seq: Seq[String] = sNorm.split(" ").map(NCNlpPorterStemmer.stem)

                    if (!allSyns.exists(_.intersect(seq).nonEmpty))
                        warns += s"Sample: '$s' doesn't contain synonyms"
            }
    }
}
