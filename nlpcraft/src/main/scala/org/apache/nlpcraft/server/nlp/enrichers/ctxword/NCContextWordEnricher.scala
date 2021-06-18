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
import org.apache.nlpcraft.common.nlp.core.NCNlpPorterStemmer.stem
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceToken}
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.server.mdo.NCModelMLConfigMdo
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher
import org.apache.nlpcraft.server.sugsyn.{NCSuggestSynonymManager, NCSuggestionRequest, NCWordSuggestion}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * ContextWord enricher.
  */
object NCContextWordEnricher extends NCServerEnricher {
    case class ModelProbeKey(probeId: String, modelId: String)
    case class WordIndex(word: String, index: Int)
    case class ValueScore(sourceValue: String, score: Double)
    case class ElementValue(elementId: String, value: String)

    @volatile private var values: mutable.HashMap[ModelProbeKey, Map[/** Stem */ String, Set[ElementValue]]] = _
    @volatile private var samples: mutable.HashMap[ModelProbeKey, Map[/** Element ID */ String, Map[/** Stem */ String, /** Value */ ValueScore]]] = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        values = mutable.HashMap.empty
        samples = mutable.HashMap.empty

        ackStarted()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        samples = null
        values = null

        ackStopped()
    }

    private def toWords(s: String): Seq[String] = s.split(" ").map(_.strip()).filter(_.nonEmpty)

    private def parseSample(
        sampleWords: Seq[String],
        sampleMap: Map[String, WordIndex],
        synsStem: Set[String]
    ): Iterable[NCSuggestionRequest] = {
        lazy val sampleWordsIdxs = sampleWords.zipWithIndex

        synsStem.flatMap(synStem =>
            sampleMap.get(synStem) match {
                case Some(syn) =>
                    Some(
                        NCSuggestionRequest(
                            sampleWordsIdxs.map { case (w, i) => if (i != syn.index) w else syn.word}.mkString(" "),
                            syn.index
                        )
                    )
                case None =>
                    None
            }
        )
    }

    private def getSamples(cfg: NCModelMLConfigMdo, key: ModelProbeKey): Map[String, Map[String, ValueScore]] =
        samples.synchronized { samples.get(key) } match {
            case Some(cache) => cache
            case None =>
                val res = askSamples(cfg)

                samples.synchronized { samples += key -> res }

                res
        }

    private def getValues(key: ModelProbeKey, cfg: NCModelMLConfigMdo): Map[/** Stem */ String, Set[ElementValue]] =
        values.synchronized { values.get(key) } match {
            case Some(cache) => cache
            case None =>
                val res =
                    cfg.values.flatMap { case (elemId, map) =>
                        map.map { case (value, syns) => syns.map(syn => (stem(syn), ElementValue(elemId, value))) }
                    }.flatten.groupBy(_._1).map { case (elemId, vals) => elemId -> vals.map(_._2).toSet }

                values.synchronized { values += key -> res }

                res
        }


    @throws[NCE]
    private def askSamples(cfg: NCModelMLConfigMdo): Map[/** Element ID */String, Map[/** Stem */String, ValueScore]] = {
        case class Record(sentence: NCSuggestionRequest, value: String)

        val recs: Map[String, Seq[Record]] =
            (for (
                (elemId, values) <- cfg.values;
                (value, syns) <- values;
                synsStem = syns.map(stem);
                sample <- cfg.samples;
                sampleWords = toWords(sample);
                samplesMap = sampleWords.zipWithIndex.map { case (w, idx) => stem(w) -> WordIndex(w, idx)}.toMap;
                sugg <- parseSample(sampleWords, samplesMap, synsStem)
            )
                yield (elemId, Record(sugg, value))).groupBy(_._1).map(p => p._1 -> p._2.values.toSeq)

        val data = recs.flatMap(p => p._2.map(x => x.sentence -> ElementValue(p._1, x.value)))

        // TODO:
        val res: Map[NCSuggestionRequest, Seq[NCWordSuggestion]] =
            if (recs.nonEmpty)
                Await.result(NCSuggestSynonymManager.suggestWords(recs.flatMap(_._2.map(_.sentence)).toSeq), Duration.Inf)
            else
                Map.empty

        res.map {
            case (req, suggs) =>
                val d = data(req)

                d.elementId -> suggs.map(p => stem(p.word) -> ValueScore(d.value, p.score)).toMap
        }
    }

    @throws[NCE]
    private def askSentence(
        ns: NCNlpSentence,
        values: Map[/** Stem */ String, Set[ElementValue]],
        samples: Map[/** Element ID */String, Map[/** Stem */String, ValueScore]]
    ): Map[/** Element ID */ String, Map[/** Stem */String, ValueScore]] = {
        val idxs = ns.tokens.flatMap(p => if (p.pos.startsWith("N")) Some(p.index) else None).toSeq

        if (idxs.nonEmpty) {
            // TODO: tokenization.
            // TODO: sync.
            val suggs: Iterable[NCWordSuggestion] =
                Await.result(
                    NCSuggestSynonymManager.suggestWords(idxs.map(idx => NCSuggestionRequest(ns.text, idx))),
                    Duration.Inf
                ).flatMap(p => p._2)

            suggs.map(sugg => (stem(sugg.word), sugg)).flatMap { case (stem, sugg) =>
                values.get(stem) match {
                    case Some(data) => data.map(d => (d.elementId, stem, ValueScore(d.value, sugg.score)))
                    case None =>
                        samples.flatMap { case (elemId, map) =>
                            map.get(stem) match {
                                case Some(vs) => Some((elemId, stem, vs))
                                case None => None
                            }
                        }
                }
            }.groupBy(_._1).map { case (elemId, map) => elemId -> map.map { case(_, stem, vs) => stem -> vs }.toMap }
        }
        else
            Map.empty
    }

    override def enrich(ns: NCNlpSentence, parent: Span): Unit = {
        ns.mlConfig match {
            case Some(cfg) =>
                val key = ModelProbeKey(cfg.probeId, cfg.modelId)
                val samples: Map[String, Map[String, ValueScore]] = getSamples(cfg, key)

                val nouns = ns.tokens.filter(_.pos.startsWith("N"))
                val marked = mutable.HashMap.empty[NCNlpSentenceToken, String]

                for (n <- nouns; (elemId, stems) <- getSamples(cfg, key) if stems.contains(n.stem))
                    println("EX FOUND elemId=" + elemId + ", n=" + n.stem + ", stem=" + stems.toSeq.sortBy(-_._2.score))

                for (n <- nouns; (elemId, stems) <- askSentence(ns, getValues(key, cfg), samples) if stems.contains(n.stem))
                    println("WS FOUND elemId=" + elemId + ", n=" + n.stem + ", stem=" + stems.toSeq.sortBy(-_._2.score))

            case None => // No-op.
        }
    }
}
