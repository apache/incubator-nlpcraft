/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.server.nlp.enrichers.ctxword

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.nlp.NCNlpSentence
import org.apache.nlpcraft.common.nlp.core.NCNlpPorterStemmer
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.server.mdo.NCModelMLConfigMdo
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher
import org.apache.nlpcraft.server.sugsyn.{NCSuggestSynonymManager, NCSuggestionElement, NCWordSuggestion}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * ContextWord enricher.
  */
object NCContextWordEnricher extends NCServerEnricher {
    case class Key(probeId: String, modelId: String)
    case class Word(word: String, stem: String)

    object Word {
        def apply(word: String) = new Word(word, NCNlpPorterStemmer.stem(word))
    }

    @volatile private var samples: mutable.HashMap[Key, Map[/** Element ID */ String, Map[/** Stem */ String, /** Confidence */ Double]]] = _
    @volatile private var words: mutable.HashMap[Key, Map[/** Element ID */ String, Map[/** Stem */ String, /** Confidence */ Double]]] = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        samples = mutable.HashMap.empty
        words = mutable.HashMap.empty

        ackStarted()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        words = null
        samples = null

        ackStopped()
    }

    @throws[NCE]
    private def askSamples(cfg: NCModelMLConfigMdo): Map[String, Map[String, Double]] = {
        println("cfg=" + cfg)

        def parseSample(elemId: String, sample: String, synsStem: Map[String, String]): Seq[NCSuggestionElement] = {
            val pairs = sample.split(" ").map(_.strip()).filter(_.nonEmpty).zipWithIndex

            println("sample=" + sample)
            println("pairs=" + pairs)

            pairs.flatMap { case (sampleWord, idx) =>
                val sampleWordStem: String = NCNlpPorterStemmer.stem(sampleWord)

                synsStem.
                    filter(p => p._2.contains(sampleWordStem)).
                    map { case (_, synWord) =>
                        NCSuggestionElement(
                            elemId,
                            pairs.map { case (w, i) => if (i != idx) w else synWord}.mkString(" "),
                            Seq(idx)
                        )
                    }
            }
        }

        case class Record(sentence: NCSuggestionElement, elementName: String)

        val recs: Map[String, Seq[Record]] =
            (for (
                (elemId, map) <- cfg.values;
                (elemName, syns) <- map;
                synsStem = syns.map(p => NCNlpPorterStemmer.stem(p) -> p).toMap;
                sample <- cfg.samples;
                sugg <- parseSample(elemId, sample, synsStem)
            )
                yield (elemId, Record(sugg, elemName))).groupBy(_._1).map(p => p._1 -> p._2.values.toSeq)

        println("recs=" + recs)
        println("recs.size=" + recs.size)

        // TODO:
        val res: Map[String, Seq[NCWordSuggestion]] =
            if (recs.nonEmpty)
                Await.result(NCSuggestSynonymManager.suggestWords(recs.flatMap(_._2.map(_.sentence)).toSeq), Duration.Inf)
            else
                Map.empty

        // TODO: elemName
        res.map { case (elemId, suggs) =>
            elemId -> suggs.map(p => NCNlpPorterStemmer.stem(p.word) -> p.score).toMap
        }
    }

    override def enrich(ns: NCNlpSentence, parent: Span): Unit = {
        ns.mlConfig match {
            case Some(cfg) =>
                val key = Key(cfg.probeId, cfg.modelId)

                val ex =
                    samples.synchronized { samples.get(key) } match {
                        case Some(data) => data
                        case None =>
                            val data = askSamples(cfg)

                            samples.synchronized { samples += key -> data }

                            data
                    }

                println("ex="+ex)

                val ws: Map[String, Map[String, Double]] = words.getOrElse(key, Map.empty)

                val nouns = ns.tokens.filter(_.pos.startsWith("N"))

                for (n <- nouns; (elemId, stems) <- ex if stems.contains(n.stem))
                    println("EX FOUND elemId=" + elemId + ", n=" + n + ", stem=" + stems.toSeq.sortBy(-_._2))

                for (n <- nouns; (elemId, stems) <- ws if stems.contains(n.stem))
                    println("WS FOUND elemId=" + elemId + ", stem=" + stems)

            case None => // No-op.
        }
    }
}
