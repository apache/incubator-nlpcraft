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

import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.nlpcraft.*

import java.util
import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCRuStopWordsTokenEnricher extends NCTokenEnricher:
    private final val stops = RussianAnalyzer.getDefaultStopSet

    private def getPos(t: NCToken): String = t.getOpt("pos").orElseThrow(() => throw new NCException("POS not found in token."))
    private def getLemma(t: NCToken): String = t.getOpt("lemma").orElseThrow(() => throw new NCException("Lemma not found in token."))

    override def enrich(req: NCRequest, cfg: NCModelConfig, toks: util.List[NCToken]): Unit =
        for (t <- toks.asScala)
            val lemma = getLemma(t)
            lazy val pos = getPos(t)

            t.put(
                "stopword",
                lemma.length == 1 && !Character.isLetter(lemma.head) && !Character.isDigit(lemma.head) ||
                stops.contains(lemma.toLowerCase) ||
                pos.startsWith("PARTICLE") ||
                pos.startsWith("INTERJECTION") ||
                pos.startsWith("PREP")
            )