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
import org.apache.nlpcraft.common.nlp.core.NCNlpPorterStemmer.stem
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
    private case class ModelProbeKey(probeId: String, modelId: String)
    private case class WordIndex(word: String, index: Int)
    private case class ElementValue(elementId: String, value: String)
    private case class ElementScore(elementId: String, score: Double)

    private type ElementStemScore = Map[/** Element ID */ String, Map[/** Stem */ String, /** Score */ Double]]

    @volatile private var samples: mutable.HashMap[ModelProbeKey, ElementStemScore] = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        samples = mutable.HashMap.empty

        ackStarted()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        samples = null

        ackStopped()
    }

    private def spaceTokenize(s: String): Seq[String] = s.split(" ").map(_.strip()).filter(_.nonEmpty)

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

    private def getSamples(cfg: NCModelMLConfigMdo, key: ModelProbeKey): ElementStemScore =
        samples.synchronized { samples.get(key) } match {
            case Some(cache) => cache
            case None =>
                val res = askSamples(cfg)

                samples.synchronized { samples += key -> res }

                res
        }

    @throws[NCE]
    private def askSamples(cfg: NCModelMLConfigMdo): ElementStemScore = {
        case class Record(request: NCSuggestionRequest, value: String)

        val recs: Map[String, Seq[Record]] =
            (for (
                (elemId, values) <- cfg.values;
                (value, syns) <- values;
                synsStem = syns.map(stem);
                sample <- cfg.samples;
                sampleWords = spaceTokenize(sample);
                samplesMap = sampleWords.zipWithIndex.map { case (w, idx) => stem(w) -> WordIndex(w, idx)}.toMap;
                sugg <- parseSample(sampleWords, samplesMap, synsStem)
            )
                yield (elemId, Record(sugg, value))).groupBy { case (elemId, _) => elemId }.
                map { case (elemId, map) => elemId -> map.values.toSeq }

        val map: Map[NCSuggestionRequest, ElementValue] =
            recs.flatMap { case (elemId, recs) => recs.map(p => p.request -> ElementValue(elemId, p.value)) }

        // TODO: sync
        val res =
            (
                if (recs.nonEmpty)
                    Await.result(NCSuggestSynonymManager.suggestWords(recs.flatMap(_._2.map(_.request)).toSeq), Duration.Inf)
                else
                    Map.empty
            ).map {
                case (req, suggs) =>
                    val d = map(req)

                    d.elementId -> suggs.groupBy(p =>stem(p.word)).map { case (stem, map) => stem -> map.map(_.score).max }
            }

        // TODO:
        println("!!!samples")
        res.foreach(s =>  {
            println(s"elemID=${s._1}")

            println(s._2.mkString("\n") + "\n")

        })

        res
    }

    @throws[NCE]
    private def askSentence(ns: NCNlpSentence, samples: ElementStemScore): Map[Int, Set[ElementScore]] = {
        val idxs = ns.tokens.flatMap(p => if (p.pos.startsWith("N")) Some(p.index) else None).toSeq
        val reqs = idxs.map(idx => NCSuggestionRequest(ns.text, idx))

        //

        // TODO: tokenization.
        // TODO: sync.
        val suggs: Map[NCWordSuggestion, NCSuggestionRequest] =
            Await.
                result(NCSuggestSynonymManager.suggestWords(reqs), Duration.Inf).
                flatMap { case (req, suggs) => suggs.map(_ -> req) }

        // TODO:
        println("suggsReq=" + reqs.mkString("|"))
        println("suggs="+suggs.keys.mkString("\n"))


        suggs.map { case(sugg, req) => (stem(sugg.word), sugg.score, req) }.
            flatMap { case (stem, suggScore, req) =>
                samples.map { case (elemId, map) =>
                    // TODO:  contains ? check key (and use score)

                    if (map.contains(stem)) {
                        // TODO:
                        println(s"!!!FOUND BY stem=$stem, elem=$elemId, map=$map")

                        map.map { case (_, score) => (ElementScore(elemId, score), req.index) }
                    }
                    else
                        Seq.empty
                }
            }.
            flatten.
            groupBy { case (_, idx) => idx }.
            map { case (idx, map) =>
                idx -> map.
                    map { case (score, _) => score }.
                    groupBy(_.elementId).
                    map { case (_, scores) => scores.toSeq.minBy(-_.score) }.toSet
            }
    }

    override def enrich(ns: NCNlpSentence, parent: Span): Unit = {
        ns.mlConfig match {
            case Some(cfg) =>
                val nouns = ns.tokens.filter(_.pos.startsWith("N"))

                if (nouns.nonEmpty) {
                    val key = ModelProbeKey(cfg.probeId, cfg.modelId)
                    val samples = getSamples(cfg, key)


                    for (n <- nouns; (elemId, stems) <- getSamples(cfg, key) if stems.contains(n.stem))
                        println("EX FOUND elemId=" + elemId + ", n=" + n.stem + ", stem=" + stems.toSeq.sortBy(-_._2))

                    val sens = askSentence(ns, samples)

                    println("!!!sens")
                    sens.foreach(s =>  {
                        println(s"INDEX=${s._1}")

                        println(s._2.mkString("\n") + "\n")

                    })

                }

            case None => // No-op.
        }
    }
}
