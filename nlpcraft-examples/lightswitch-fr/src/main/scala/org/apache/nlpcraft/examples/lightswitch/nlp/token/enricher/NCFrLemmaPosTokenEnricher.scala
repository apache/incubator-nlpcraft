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

package org.apache.nlpcraft.examples.lightswitch.nlp.token.enricher

import org.apache.nlpcraft.*
import org.languagetool.AnalyzedToken
import org.languagetool.tagging.fr.FrenchTagger

import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCFrLemmaPosTokenEnricher extends NCTokenEnricher:
    private def nvl(v: String, dflt : => String): String = if v != null then v else dflt

    override def enrich(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): Unit =
        val tags = FrenchTagger.INSTANCE.tag(toks.map(_.getText).asJava).asScala

        require(toks.size == tags.size)

        toks.zip(tags).foreach { case (tok, tag) =>
            val readings = tag.getReadings.asScala

            val (lemma, pos) = readings.size match
                // No data. Lemma is word as is, POS is undefined.
                case 0 => (tok.getText, "")
                // Takes first. Other variants ignored.
                case _ =>
                    val aTok: AnalyzedToken = readings.head
                    (nvl(aTok.getLemma, tok.getText), nvl(aTok.getPOSTag, ""))

            tok.put("pos", pos)
            tok.put("lemma", lemma)

            () // Otherwise NPE.
        }
