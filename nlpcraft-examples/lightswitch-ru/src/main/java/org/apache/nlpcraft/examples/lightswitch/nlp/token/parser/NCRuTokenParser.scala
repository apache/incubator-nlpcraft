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

package org.apache.nlpcraft.examples.lightswitch.nlp.token.parser

import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.nlpcraft.*
import org.languagetool.AnalyzedToken
import org.languagetool.language.Russian
import org.languagetool.rules.ngrams.*
import org.languagetool.tagging.ru.*
import org.languagetool.tokenizers.WordTokenizer

import java.util
import scala.jdk.CollectionConverters.*

object NCRuTokenParser:
    private val tokenizer = new WordTokenizer
    private case class Span(word: String, start: Int, end: Int)
    private def nvl(v: String, dflt : => String): String = if v != null then v else dflt

    private def split(text: String): Seq[Span] =
        val spans = collection.mutable.ArrayBuffer.empty[Span]
        var sumLen = 0

        for (((word, len), idx) <- tokenizer.tokenize(text).asScala.map(p => p -> p.length).zipWithIndex)
            if word.strip.nonEmpty then spans += Span(word, sumLen, sumLen + word.length)
            sumLen += word.length

        spans.toSeq

import NCRuTokenParser.*

class NCRuTokenParser extends NCTokenParser:
    override def tokenize(text: String): util.List[NCToken] =
        val spans = split(text)
        val tags = RussianTagger.INSTANCE.tag(spans.map(_.word).asJava).asScala

        require(spans.size == tags.size)

        spans.zip(tags).zipWithIndex.map { case ((span, tag), idx) =>
            val readings = tag.getReadings.asScala

            val (lemma, pos) =
                readings.size match
                    // No data. Lemma is word as is, POS is undefined.
                    case 0 => (span.word, "")
                    // Takes first. Other variants ignored.
                    case _ =>
                        val aTok: AnalyzedToken = readings.head
                        (nvl(aTok.getLemma, span.word), nvl(aTok.getPOSTag, ""))

            val tok: NCToken =
                new NCPropertyMapAdapter with NCToken:
                    override val getText: String = span.word
                    override val getIndex: Int = idx
                    override val getStartCharIndex: Int = span.start
                    override val getEndCharIndex: Int = span.end
                    override val getLemma: String = lemma
                    override val getPos: String = pos
            tok
        }.asJava