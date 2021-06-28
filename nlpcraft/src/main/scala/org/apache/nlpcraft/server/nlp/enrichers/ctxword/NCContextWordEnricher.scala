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
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager.stem
import org.apache.nlpcraft.common.nlp.pos.NCPennTreebank._
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceToken}
import org.apache.nlpcraft.common.{NCE, NCService, U}
import org.apache.nlpcraft.server.mdo.NCModelMLConfigMdo
import org.apache.nlpcraft.server.nlp.core.{NCNlpParser, NCNlpServerManager, NCNlpWord}
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher
import org.apache.nlpcraft.server.sugsyn.{NCSuggestSynonymManager, NCSuggestionRequest, NCWordSuggestion}
import org.jibx.schema.codegen.extend.DefaultNameConverter

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * ContextWord enricher.
  */
object NCContextWordEnricher extends NCServerEnricher {
    private final val MAX_CTXWORD_SCORE = 2
    private final val EXCLUSIVE_MIN_SCORE = -1.0

    private final val CONVERTER = new DefaultNameConverter

    private case class ModelProbeKey(probeId: String, modelId: String)
    private case class ElementScore(elementId: String, averageScore: Double, senScore: Double, sampleScore: Double)
    private case class ValuesHolder(
        values: Map[/** Stem */String, /** Element ID */Set[String]],
        valuesStems: Map[/** Value */String, /** Element ID */Set[String]],
    )

    private type ElementStemScore = Map[/** Element ID */String, Map[/** Stem */String,/** Score */Double]]

    @volatile private var valuesStems: mutable.HashMap[ModelProbeKey, ValuesHolder] = _
    @volatile private var samples: mutable.HashMap[ModelProbeKey, ElementStemScore] = _

    @volatile private var parser: NCNlpParser = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        valuesStems = mutable.HashMap.empty
        samples = mutable.HashMap.empty
        parser = NCNlpServerManager.getParser

        ackStarted()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        parser = null
        samples = null
        valuesStems = null

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
      * @param nlpWords
      * @param sampleWords
      * @param sampleWordsStems
      * @param elemValuesSyns
      * @param elemValuesSynsStems
      * @return
      */
    private def parseSample(
        nlpWords: Seq[Seq[NCNlpWord]],
        sampleWords: Seq[Seq[String]],
        sampleWordsStems: Seq[Seq[String]],
        elemValuesSyns: Set[String],
        elemValuesSynsStems: Set[String]
    ): Iterable[NCSuggestionRequest] = {
        require(nlpWords.size == sampleWords.size)
        require(sampleWords.size == sampleWordsStems.size)
        require(elemValuesSyns.size == elemValuesSynsStems.size)

        sampleWordsStems.zip(sampleWords).zip(nlpWords).flatMap { case ((sampleWordsStem, sampleWords), nlpWords) =>
            val idxs = elemValuesSynsStems.flatMap(valSynsStem => {
                val i = sampleWordsStem.indexOf(valSynsStem)

                if (i >= 0) Some(i) else None
            })

            def mkRequest(idx: Int, syn: String): NCSuggestionRequest = {
                def mkSentence(syn: String): String =
                    sampleWords.zipWithIndex.map { case (w, i) => if (i != idx) w else syn }.mkString(" ")

                var newSen = mkSentence(syn)

                val nlpWordsNew = parser.parse(newSen)

                require(nlpWords.size == nlpWordsNew.size)

                val pos = nlpWords(idx).pos
                val posNew = nlpWordsNew(idx).pos

                if (NOUNS_POS_SINGULAR.contains(pos) && NOUNS_POS_PLURALS.contains(posNew)) {
                    println(s"newSen1=$newSen")

                    newSen = mkSentence(CONVERTER.depluralize(syn))

                    println(s"newSen2=$newSen")
                }
                else if (NOUNS_POS_PLURALS.contains(pos) && NOUNS_POS_SINGULAR.contains(posNew)) {
                    println(s"newSen1=$newSen")

                    newSen = mkSentence(CONVERTER.pluralize(syn))

                    println(s"newSen3=$newSen")
                }

                NCSuggestionRequest(newSen, idx)
            }

            for (idx <- idxs; syn <- elemValuesSyns)
                yield mkRequest(idx, syn)
        }
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
      * @param key
      * @return
      */
    private def getValuesData(cfg: NCModelMLConfigMdo, key: ModelProbeKey): ValuesHolder =
        valuesStems.synchronized { valuesStems.get(key) } match {
            case Some(cache) => cache
            case None =>
                def mkMap(convert: String => String): Map[String, Set[String]] =
                    cfg.values.
                        flatMap { case (elemId, vals) => vals.map { case (_, vals) => vals.map(convert(_) -> elemId) } }.
                        flatten.
                        groupBy { case (converted, _) => converted }.
                        map { case (converted, map) => converted -> map.map {case (_, elemId) => elemId }.toSet }

                val res = ValuesHolder(mkMap(stem), mkMap(_.toLowerCase))

                valuesStems.synchronized { valuesStems += key -> res }

                res
        }

    /**
      *
      * @param cfg
      * @return
      */
    @throws[NCE]
    private def askSamples(cfg: NCModelMLConfigMdo): ElementStemScore = {
        val samplesSeq = cfg.samples.toSeq
        val sampleWords = samplesSeq.map(spaceTokenize)
        val nlpWords = samplesSeq.map(s => parser.parse(s))

        val sampleWordsStems = sampleWords.map(_.map(stem))

        val recs =
            (
                for (
                    (elemId, elemValues) <- cfg.values;
                    // Uses single words synonyms only.
                    elemValuesSyns = elemValues.flatMap(_._2).toSet.filter(!_.contains(' '));
                    elemValuesSynsStems = elemValuesSyns.map(stem);
                    suggReq <- parseSample(nlpWords, sampleWords, sampleWordsStems, elemValuesSyns, elemValuesSynsStems)
                )
                    yield (elemId, suggReq)
            ).
                groupBy { case (elemId, _) => elemId }.
                map { case (elemId, m) => elemId -> m.toSeq.map(_._2) }

        if (recs.nonEmpty) {
            val req2Elem = recs.flatMap { case (elemId, recs) => recs.map(p => p -> elemId) }

            syncExec(NCSuggestSynonymManager.suggestWords(recs.flatMap(_._2).toSeq)).
                toSeq.
                map { case (req, suggs) =>
                    (req2Elem(req),
                        suggs.groupBy(sygg => stem(sygg.word)).map { case (stem, stemSuggs) =>
                            stem -> normalizeScore(stemSuggs.map(_.score).max)
                        }
                    )
                }.
                groupBy { case (elemId, _) => elemId }.
                map { case (elemId, data) => elemId -> data.flatMap(_._2).toMap }
        }
        else
            Map.empty[String, Map[String, Double]]
    }

    override def enrich(ns: NCNlpSentence, parent: Span): Unit =
        ns.mlConfig match {
            case Some(cfg) =>
                val detected = mutable.HashMap.empty[NCNlpSentenceToken, mutable.HashSet[ElementScore]]

                def add(
                    nounTok: NCNlpSentenceToken, elemId: String, averageScore: Double, senScore: Double, sampleScore: Double
                ): Unit = {
                    val tokElems = detected.getOrElseUpdate(nounTok, mutable.HashSet.empty[ElementScore])

                    def mkNew(): ElementScore = ElementScore(elemId, averageScore, senScore, sampleScore)

                    tokElems.find(_.elementId == elemId) match {
                        case Some(saved) =>
                            if (averageScore > saved.averageScore) {
                                tokElems -= saved
                                tokElems += mkNew()
                            }
                        case None => tokElems += mkNew()
                    }
                }

                val nounToks = ns.tokens.filter(t => NOUNS_POS.contains(t.pos))

                if (nounToks.nonEmpty) {
                    val key = ModelProbeKey(cfg.probeId, cfg.modelId)

                    // 1. Values. Direct.
                    val valuesData = getValuesData(cfg, key)

                    for (nounTok <- nounToks; elemId <- valuesData.values.getOrElse(nounTok.lemma.toLowerCase, Set.empty))
                        add(nounTok, elemId, 1, 1, 1)
                    for (nounTok <- nounToks; elemId <- valuesData.values.getOrElse(nounTok.normText, Set.empty))
                        add(nounTok, elemId, 1, 1, 1)
                    for (nounTok <- nounToks; elemId <- valuesData.valuesStems.getOrElse(nounTok.stem, Set.empty))
                        add(nounTok, elemId, 1, 1, 1)

                    // 2. Via examples.
                    val mdlSamples = getSamplesData(cfg, key)

                    for (
                        nounTok <- nounToks;
                        (elemId, suggs) <- mdlSamples;
                        score = suggs.getOrElse(nounTok.stem, EXCLUSIVE_MIN_SCORE)
                        if score >= cfg.levels(elemId)
                    )
                        add(nounTok, elemId, score, score, score)

                    // 3. Ask for sentence.
                    val idxs = ns.tokens.flatMap(p => if (p.pos.startsWith("N")) Some(p.index) else None).toSeq
                    val reqs = idxs.map(idx => NCSuggestionRequest(ns.tokens.map(_.origText).mkString(" "), idx))

                    for (
                        // Token index (tokIdx) should be correct because request created from original words,
                        // separated by space, and Suggestion Manager uses space tokenizer.
                        (sugg, tokIdx) <- getSentenceData(reqs);
                        suggStem = stem(sugg.word);
                        senScore = normalizeScore(sugg.score);
                        (elemId, mdlSamplesSuggs) <- mdlSamples
                        if mdlSamplesSuggs.contains(suggStem);
                        elemScore = cfg.levels(elemId);
                        sampleScore = mdlSamplesSuggs(suggStem);
                        averageScore = (sampleScore + senScore) / 2
                        if averageScore >= elemScore
                    )
                        add(ns.tokens(tokIdx), elemId, averageScore, senScore, sampleScore)
                }

                ns.mlData = detected.map {
                    case (tok, scores) => tok.index -> scores.map(p => p.elementId -> p.averageScore).toMap
                }.toMap

                println("ns.mlData="+ns.mlData)
            case None => // No-op.
        }
}
