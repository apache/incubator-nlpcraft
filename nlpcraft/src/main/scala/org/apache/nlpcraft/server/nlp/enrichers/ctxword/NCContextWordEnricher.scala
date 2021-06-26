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
import org.apache.nlpcraft.common.{NCE, NCService, U}
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
    private final val MAX_CTXWORD_SCORE = 2
    private final val EXCLUSIVE_MIN_SCORE = -1.0

    private case class ModelProbeKey(probeId: String, modelId: String)
    private case class WordIndex(word: String, index: Int)
    private case class ElementValue(elementId: String, value: String)
    private case class ElementScore(elementId: String, score: Double)

    private type ElementStemScore = Map[/** Element ID */String, Map[/** Stem */String,/** Score */Double]]

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

    /**
      *
      * @param s
      * @return
      */
    private def spaceTokenize(s: String): Seq[String] = U.splitTrimFilter(s, " ")

    /**
      *
      * @param awaitable
      * @tparam T
      * @return
      */
    private def syncExec[T](awaitable : scala.concurrent.Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

    /**
      *
      * @param reqs
      * @return
      */
    private def getSentenceData(reqs: Seq[NCSuggestionRequest]): Map[NCWordSuggestion, Int] =
        syncExec(NCSuggestSynonymManager.suggestWords(reqs)).flatMap { case (req, suggs) => suggs.map(_ -> req.index) }

    /**
      *
      * @param sampleWords
      * @param sampleMap
      * @param synsStem
      * @return
      */
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
                case None => None
            }
        )
    }

    /**
      *
      * @param d
      * @return
      */
    private def normalizeScore(d: Double): Double = d / MAX_CTXWORD_SCORE

    /**
      *
      * @param cfg
      * @param key
      * @return
      */
    private def getSamplesData(cfg: NCModelMLConfigMdo, key: ModelProbeKey): ElementStemScore =
        samples.synchronized { samples.get(key) } match {
            case Some(cache) => cache
            case None =>
                val res = askSamples(cfg)

                samples.synchronized { samples += key -> res }

                res
        }

    /**
      *
      * @param cfg
      * @return
      */
    @throws[NCE]
    private def askSamples(cfg: NCModelMLConfigMdo): ElementStemScore = {
        case class Record(request: NCSuggestionRequest, value: String)

        val recs: Map[String, Seq[Record]] =
            (
                for (
                    (elemId, values) <- cfg.values;
                    (value, syns) <- values;
                    synsStem = syns.map(stem);
                    sample <- cfg.samples;
                    sampleWords = spaceTokenize(sample);
                    samplesMap = sampleWords.zipWithIndex.map { case (w, idx) => stem(w) -> WordIndex(w, idx) }.toMap;
                    sugg <- parseSample(sampleWords, samplesMap, synsStem
                )
            )
            yield (elemId, Record(sugg, value))).groupBy { case (elemId, _) => elemId }.
                map { case (elemId, map) => elemId -> map.values.toSeq }

        val map = recs.flatMap { case (elemId, recs) => recs.map(p => p.request -> ElementValue(elemId, p.value)) }

        if (recs.nonEmpty)
            syncExec(NCSuggestSynonymManager.suggestWords(recs.flatMap(_._2.map(_.request)).toSeq)).
            map { case (req, suggs) =>
                map(req).elementId -> suggs.groupBy(p => stem(p.word)).map { case (stem, suggs) =>
                    stem -> normalizeScore(suggs.map(_.score).max)
                }
            }
        else
            Map.empty[String, Map[String, Double]]
    }

    override def enrich(ns: NCNlpSentence, parent: Span): Unit =
        ns.mlConfig match {
            case Some(cfg) =>
                val detected = mutable.HashMap.empty[NCNlpSentenceToken, mutable.HashSet[ElementScore]]

                def add(nounTok: NCNlpSentenceToken, elemId: String, score: Double): Unit = {
                    val tokElems = detected.getOrElseUpdate(nounTok, mutable.HashSet.empty[ElementScore])

                    tokElems.find(_.elementId == elemId) match {
                        case Some(saved) =>
                            if (score > saved.score) {
                                tokElems -= saved
                                tokElems += ElementScore(elemId, score)
                            }
                        case None => tokElems += ElementScore(elemId, score)
                    }
                }

                val nounToks = ns.tokens.filter(_.pos.startsWith("N"))

                if (nounToks.nonEmpty) {
                    val key = ModelProbeKey(cfg.probeId, cfg.modelId)
                    val mdlSamples = getSamplesData(cfg, key)

                    for (
                        nounTok <- nounToks;
                        (elemId, suggs) <- mdlSamples;
                        score = suggs.getOrElse(nounTok.stem, EXCLUSIVE_MIN_SCORE)
                        if score >= cfg.levels(elemId)
                    )
                        add(nounTok, elemId, score)

                    val idxs = ns.tokens.flatMap(p => if (p.pos.startsWith("N")) Some(p.index) else None).toSeq

                    val reqs = idxs.map(idx => NCSuggestionRequest(ns.tokens.map(_.origText).mkString(" "), idx))

                    for (
                        // Token index (tokIdx) should be correct because request created from original words,
                        // separated by space, and Suggestion Manager uses space tokenizer.
                        (sugg, tokIdx) <- getSentenceData(reqs);
                        suggStem = stem(sugg.word);
                        senSuggScore = normalizeScore(sugg.score);
                        (elemId, mdlSamplesSuggs) <- mdlSamples
                        if mdlSamplesSuggs.contains(suggStem);
                        elemScore = cfg.levels(elemId);
                        sampleSuggScore = mdlSamplesSuggs(suggStem);
                        score = (sampleSuggScore + senSuggScore) / 2
                        if score >= elemScore
                    )
                        add(ns.tokens(tokIdx), elemId, score)
                }

                ns.mlData = detected.map {
                    case (tok, scores) => tok.index -> scores.map(p => p.elementId -> p.score).toMap
                }.toMap
            case None => // No-op.
        }
}
