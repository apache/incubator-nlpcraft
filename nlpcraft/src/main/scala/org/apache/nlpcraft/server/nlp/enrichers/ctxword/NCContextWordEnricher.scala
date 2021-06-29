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
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.server.mdo.NCModelMLConfigMdo
import org.apache.nlpcraft.server.nlp.core.{NCNlpParser, NCNlpServerManager, NCNlpWord}
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher
import org.apache.nlpcraft.server.sugsyn.{NCSuggestSynonymManager, NCSuggestionRequest, NCWordSuggestion}
import org.jibx.schema.codegen.extend.DefaultNameConverter

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.SeqHasAsJava

/**
  * ContextWord enricher.
  */
object NCContextWordEnricher extends NCServerEnricher {
    private final val MAX_CTXWORD_SCORE = 2
    private final val EXCL_MIN_SCORE = -1.0
    private final val INCL_MAX_SCORE = 1.0

    private final val CONVERTER = new DefaultNameConverter

    private case class ModelProbeKey(probeId: String, modelId: String)
    private case class ElementScore(elementId: String, scores: Double*) {
        lazy val maxScore: Double = scores.max // TODO" logic
        override def toString: String = s"Element [id=$elementId, scores=${scores.sortBy(p => -p).mkString(",", "[", "]")}]"
    }
    private case class ValuesHolder(
        values: Map[/**  Value as is */ String, /** Element ID */ Set[String]],
        valuesStems: Map[/** Value's stem */ String, /** Element ID */ Set[String]]
    ) {
        override def toString: String = s"Values [values=$values, stems=$valuesStems]"
    }

    case class ScoreHolder(
        normal: Map[/** Normal value */ String, /** Score */ Double],
        stems: Map[/** Stem */ String, /** Score */ Double],
        lemma: Map[/** Lemma */ String, /** Score */ Double]
    ) {
        private def sort(m: Map[String, Double]): String = m.toSeq.sortBy(-_._2).map({ case (k, v) => s"$k=$v" }).mkString(",")
        override def toString: String = s"Score [normal=${sort(normal)}, stems=${sort(stems)}, lemma=${sort(lemma)}]"
    }

    @volatile private var valuesStems: mutable.HashMap[ModelProbeKey, ValuesHolder] = _
    @volatile private var samples: mutable.HashMap[ModelProbeKey, Map[/** Element ID */String, ScoreHolder]] = _

    @volatile private var parser: NCNlpParser = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        valuesStems = mutable.HashMap.empty
        samples = mutable.HashMap.empty
        parser = NCNlpServerManager.getParser

        ackStarted()
    }

    override def stop(parent: Span = null): Unit =
        startScopedSpan("stop", parent) { _ =>
            ackStopping()

            // TODO: clear model cache
            parser = null
            samples = null
            valuesStems = null

            ackStopped()
        }

    /**
      *
      * @param awaitable
      * @tparam T
      * @return
      */
    private def syncExec[T](awaitable : scala.concurrent.Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

    /**
      *
      * @param nlpWords
      * @param sampleWords
      * @param sampleWordsStems
      * @param sampleWordsNorm
      * @param elemValuesSyns
      * @param elemValuesSynsStems
      * @param elemValuesSynsNorm
      * @return
      */
    private def parseSample(
        nlpWords: Seq[Seq[NCNlpWord]],
        sampleWords: Seq[Seq[String]],
        sampleWordsStems: Seq[Seq[String]],
        sampleWordsNorm: Seq[Seq[String]],
        elemValuesSyns: Set[String],
        elemValuesSynsStems: Set[String],
        elemValuesSynsNorm: Set[String]
    ): Iterable[NCSuggestionRequest] = {
        require(nlpWords.size == sampleWords.size)
        require(sampleWords.size == sampleWordsStems.size)
        require(sampleWords.size == sampleWordsNorm.size)
        require(elemValuesSyns.size == elemValuesSynsStems.size)
        require(elemValuesSyns.size == elemValuesSynsNorm.size)

        sampleWordsStems.
            zip(sampleWords).
            zip(sampleWordsNorm).
            zip(nlpWords).
            flatMap {
                case (((sampleWordsStem, sampleWords), sampleWordsNorm), nlpWords) =>
                def getIndexes(elemValuesData: Set[String], sampleData: Seq[String]): Set[Int] =
                    elemValuesData.flatMap(vd => {
                        val i = sampleData.indexOf(vd)

                        if (i >= 0) Some(i) else None
                    })

                val idxs = getIndexes(elemValuesSynsStems, sampleWordsStem) ++ getIndexes(elemValuesSynsNorm, sampleWordsNorm)

                def mkRequest(idx: Int, syn: String): NCSuggestionRequest = {
                    var newSen = substitute(sampleWords, syn, idx)

                    val nlpWordsNew = parser.parse(newSen.mkString(" "))

                    require(nlpWords.size == nlpWordsNew.size)

                    val pos = nlpWords(idx).pos
                    val posNew = nlpWordsNew(idx).pos

                    if (NOUNS_POS_SINGULAR.contains(pos) && NOUNS_POS_PLURALS.contains(posNew)) {
                        println(s"newSen1=$newSen")

                        newSen = substitute(sampleWords, CONVERTER.depluralize(syn), idx)

                        println(s"newSen2=$newSen")
                    }
                    else if (NOUNS_POS_PLURALS.contains(pos) && NOUNS_POS_SINGULAR.contains(posNew)) {
                        println(s"newSen1=$newSen")

                        newSen = substitute(sampleWords, CONVERTER.pluralize(syn), idx)

                        println(s"newSen3=$newSen")
                    }

                    // TODO: check newSen
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
    private def getSamplesData(cfg: NCModelMLConfigMdo, key: ModelProbeKey): Map[/** Element ID */String, ScoreHolder] =
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
      * @param words
      * @param word
      * @param index
      * @return
      */
    private def substitute(words: Seq[String], word: String, index: Int): Seq[String] = {
        require(index < words.length)

        words.zipWithIndex.map { case (w, i) => if (i != index) w else word }
    }

    /**
      *
      * @param req
      * @param sugg
      * @return
      */
    private def getSuggestionLemma(req: NCSuggestionRequest, sugg: NCWordSuggestion): String =
        parser.parse(substitute(req.words, sugg.word, req.index).mkString(" "))(req.index).lemma

    /**
      *
      * @param cfg
      * @return
      */
    @throws[NCE]
    private def askSamples(cfg: NCModelMLConfigMdo): Map[/** Element ID */String, ScoreHolder] = {
        val samplesSeq = cfg.samples.toSeq
        val sampleWords = samplesSeq.map(parser.parse(_).map(_.word))
        val nlpWords = samplesSeq.map(s => parser.parse(s))

        val sampleWordsStems = sampleWords.map(_.map(stem))
        val sampleWordsNorm = sampleWords.map(_.map(_.toLowerCase))

        val recs: Map[/** Element ID */String, Seq[NCSuggestionRequest]] =
            (
                for (
                    (elemId, elemValues) <- cfg.values.toSeq;
                    // Uses single words synonyms only.
                    elemValuesSyns = elemValues.flatMap(_._2).toSet.filter(!_.contains(' '));
                    suggReq <- parseSample(
                        nlpWords = nlpWords,
                        sampleWords = sampleWords,
                        sampleWordsStems = sampleWordsStems,
                        sampleWordsNorm = sampleWordsNorm,
                        elemValuesSyns = elemValuesSyns,
                        elemValuesSynsStems = elemValuesSyns.map(stem),
                        elemValuesSynsNorm = elemValuesSyns.map(_.toLowerCase)
                    )
                )
                    yield (elemId, suggReq)
            ).
                groupBy { case (elemId, _) => elemId }.
                map { case (elemId, m) => elemId -> m.map(_._2) }

        if (recs.nonEmpty) {
            val resps = syncExec(NCSuggestSynonymManager.suggestWords(recs.flatMap(_._2).toSeq))
            val respsSeq = resps.toSeq

            val req2Elem = recs.flatMap { case (elemId, recs) => recs.map(p => p -> elemId) }

            def mkMap(convert: (NCSuggestionRequest, NCWordSuggestion) => String): Map[String, Map[String, Double]] =
                respsSeq.
                    map { case (req, suggs) =>
                        (req2Elem(req),
                            suggs.groupBy(sygg => convert(req, sygg)).
                                map { case (converted, stemSuggs) =>
                                converted -> normalizeScore(stemSuggs.map(_.score).max)
                            }
                        )
                    }.
                    groupBy { case (elemId, _) => elemId }.
                    map { case (elemId, data) => elemId -> data.flatMap(_._2).toMap }

            val normalMap = mkMap { (_, sugg ) => sugg.word.toLowerCase }
            val stemMap = mkMap { (_, sugg ) => stem(sugg.word) }
            val lemmaMap = mkMap { (req, sugg ) => getSuggestionLemma(req, sugg) }

            (normalMap.keySet ++ stemMap.keySet ++ lemmaMap.keySet).map(elemId =>
                elemId ->
                    ScoreHolder(
                        normal = normalMap.getOrElse(elemId, Map.empty),
                        stems = stemMap.getOrElse(elemId, Map.empty),
                        lemma = lemmaMap.getOrElse(elemId, Map.empty)
                    )
            ).toMap
        }
        else
            Map.empty[String, ScoreHolder]
    }

    override def enrich(ns: NCNlpSentence, parent: Span): Unit =
        ns.mlConfig match {
            case Some(cfg) =>
                val detected = mutable.HashMap.empty[NCNlpSentenceToken, mutable.HashSet[ElementScore]]

                def add(
                    nounTok: NCNlpSentenceToken, elemId: String, senScore: Double, sampleScore: Double
                ): Unit = {
                    val maxScore = Math.max(senScore, sampleScore)
                    val tokElems = detected.getOrElseUpdate(nounTok, mutable.HashSet.empty[ElementScore])

                    def mkNew(): ElementScore = ElementScore(elemId, senScore, sampleScore)

                    tokElems.find(_.elementId == elemId) match {
                        case Some(saved) =>
                            if (maxScore > saved.maxScore) {
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

                    //println("valuesData="+valuesData)

                    for (
                        nounTok <- nounToks;
                        elemId <-
                            valuesData.values.getOrElse(nounTok.lemma.toLowerCase, Set.empty) ++
                            valuesData.values.getOrElse(nounTok.normText, Set.empty) ++
                            valuesData.valuesStems.getOrElse(nounTok.stem, Set.empty)
                    )
                        add(nounTok, elemId, INCL_MAX_SCORE, INCL_MAX_SCORE)

                    // 2. Via examples.
                    val mdlSamples = getSamplesData(cfg, key)

                    //println("mdlSamples="+mdlSamples.mkString("\n"))

                    for (
                        nounTok <- nounToks;
                        (elemId, suggs) <- mdlSamples;
                        score = Seq(
                            suggs.stems.getOrElse(nounTok.stem, EXCL_MIN_SCORE),
                            suggs.lemma.getOrElse(nounTok.lemma, EXCL_MIN_SCORE),
                            suggs.normal.getOrElse(nounTok.normText, EXCL_MIN_SCORE)
                        ).max
                        if score >= cfg.levels(elemId)
                    )
                        add(nounTok, elemId, score, score)

                    // 3. Ask for sentence.
                    val idxs = ns.tokens.flatMap(p => if (p.pos.startsWith("N")) Some(p.index) else None).toSeq
                    val reqs = idxs.map(idx => NCSuggestionRequest(ns.tokens.map(_.origText).toSeq, idx))

                    val resps =
                        syncExec(
                            NCSuggestSynonymManager.suggestWords(reqs)).flatMap { case (req, suggs) => suggs.map(_ -> req)
                        }

//                    resps.toSeq.groupBy(_._2.index).foreach { case (_, seq) =>
//                        val sorted = seq.sortBy(-_._1.score)
//
//                        println("REQ=" + sorted.head._2)
//                        println("Resps=" + sorted.map(_._1))
//                        println()
//                    }



                    for (
                        // Token index (tokIdx) should be correct because request created from original words,
                        // separated by space, and Suggestion Manager uses space tokenizer.
                        (sugg, req) <- resps;
                        senScore = normalizeScore(sugg.score);
                        (elemId, mdlSamplesSuggs) <- mdlSamples;
                        elemScore = cfg.levels(elemId);
                        sampleScore =
                            Seq(
                                mdlSamplesSuggs.stems.getOrElse(stem(sugg.word), EXCL_MIN_SCORE),
                                mdlSamplesSuggs.normal.getOrElse(sugg.word.toLowerCase, EXCL_MIN_SCORE),
                                mdlSamplesSuggs.lemma.getOrElse(getSuggestionLemma(req, sugg), EXCL_MIN_SCORE)
                            ).max
                        if sampleScore >= elemScore && senScore >= elemScore // TODO: logic
                    )
                        add(ns.tokens(req.index), elemId, senScore, sampleScore)
                }

                ns.mlData = detected.map {
                    case (tok, scores) => tok.index -> scores.map(p => p.elementId -> p.scores.asJava).toMap
                }.toMap

                println("detected="+detected.map(p => p._1.lemma -> p._2))
            case None => // No-op.
        }
}
