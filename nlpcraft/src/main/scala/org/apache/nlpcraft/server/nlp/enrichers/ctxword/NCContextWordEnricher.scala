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
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager.stem
import org.apache.nlpcraft.common.nlp.pos.NCPennTreebank._
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceToken}
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.model.NCContextWordElementConfig
import org.apache.nlpcraft.server.mdo.NCCtxWordConfigMdo
import org.apache.nlpcraft.server.nlp.core.{NCNlpParser, NCNlpServerManager, NCNlpWord}
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher
import org.apache.nlpcraft.server.sugsyn.{NCSuggestSynonymManager, NCSuggestionRequest, NCWordSuggestion}
import org.jibx.schema.codegen.extend.DefaultNameConverter

import java.text.DecimalFormat
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.SeqHasAsJava

/**
  * ContextWord enricher.
  * TODO: check plurals
  * TODO: check empty lemma
  */
object NCContextWordEnricher extends NCServerEnricher {
    private final val MAX_CTXWORD_SCORE = 2
    private final val INCL_MAX_SCORE = 1.0

    private final val DEBUG_MODE = true

    private final val CONVERTER = new DefaultNameConverter
    private final val FMT = new DecimalFormat("#0.00000")

    private case class Score(score: Double, reason: Option[String] = None) {
        override def toString: String = {
            val s =
                reason match {
                    case Some(v) => s"via:'$v'"
                    case None => "direct"
                }

            s"${FMT.format(score)}($s)}"
        }
    }
    private case class ModelProbeKey(probeId: String, modelId: String)
    private case class ElementScore(elementId: String, scores: Score*) {
        override def toString: String =
            s"Element [id=$elementId, scores=${scores.sortBy(p => -p.score).mkString("{ ", ", ", " }")}]"
    }

    // Key - word form (origin, stem). Value - Element IDs set.
    type ElementsByKey = Map[/** Key */ String, /** Element ID */ Set[String]]

    object ValuesHolder {
        def apply(normal: ElementsByKey, stems: ElementsByKey): ValuesHolder = new ValuesHolder(
            normal, stems.filter(p => !normal.keySet.contains(p._1))
        )
    }

    class ValuesHolder(val normal: ElementsByKey, val stems: ElementsByKey) {
        private def map2Str(m: Map[String, Set[String]]): String =
            m.toSeq.flatMap(p => p._2.toSeq.map(x => x -> p._1)).
                groupBy(_._1).map(p => p._1 -> p._2.map(_._2).
                mkString("{ ", ", ", " }")).mkString(", ")

        override def toString: String = s"Values [normal=${map2Str(normal)}, stems=${map2Str(stems)}]"
    }

    // Key - word form (origin, stem, lemma).
    // Scores list which extracted from suggestions for each example (direct or artificial)
    type ScoreFactors = Map[String, Seq[Double]]

    object ScoreHolder {
        def apply(normals: ScoreFactors, stems: ScoreFactors, lemmas: ScoreFactors): ScoreHolder =
            new ScoreHolder(normals, stems -- normals.keySet, lemmas -- normals.keySet -- stems.keySet)
    }

    class ScoreHolder(normals: ScoreFactors, stems: ScoreFactors, lemmas: ScoreFactors) {
        def get(m: ScoreFactors, key: String): Seq[Double] = m.getOrElse(key, Seq.empty)

        def get(norm: String, stem: String, lemma: String): Seq[Double] =
            get(normals, norm) ++ get(stems, stem) ++ get(lemmas, lemma)

        private def sort(m: ScoreFactors): String =
            m.toSeq.
                sortBy(p => (-p._2.max, -p._2.size)).map(
                    { case (k, factors) => s"$k=${factors.sortBy(-_).map(p => FMT.format(p)).mkString("{ ", ", ", " }")}" }
                ).mkString("{ ", ", ", " }")

        override def toString: String = s"Score: ${sort(normals)}"
    }

    @volatile private var valuesStems: mutable.HashMap[ModelProbeKey, ValuesHolder] = _
    @volatile private var corpuses: mutable.HashMap[ModelProbeKey, Map[/** Element ID */String, ScoreHolder]] = _

    @volatile private var parser: NCNlpParser = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        valuesStems = mutable.HashMap.empty
        corpuses = mutable.HashMap.empty
        parser = NCNlpServerManager.getParser

        ackStarted()
    }

    override def stop(parent: Span = null): Unit =
        startScopedSpan("stop", parent) { _ =>
            ackStopping()

            parser = null
            corpuses = null
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
      * @param corpusWords
      * @param corpusWordsStems
      * @param corpusWordsNorm
      * @param elemValuesSyns
      * @param elemValuesSynsStems
      * @param elemValuesSynsNorm
      * @return
      */
    private def mkRequests(
        nlpWords: Seq[Seq[NCNlpWord]],
        corpusWords: Seq[Seq[String]],
        corpusWordsStems: Seq[Seq[String]],
        corpusWordsNorm: Seq[Seq[String]],
        elemValuesSyns: Set[String],
        elemValuesSynsStems: Set[String],
        elemValuesSynsNorm: Set[String]
    ): Iterable[NCSuggestionRequest] = {
        require(nlpWords.size == corpusWords.size)
        require(corpusWords.size == corpusWordsStems.size)
        require(corpusWords.size == corpusWordsNorm.size)
        require(elemValuesSyns.size == elemValuesSynsStems.size)
        require(elemValuesSyns.size == elemValuesSynsNorm.size)

        corpusWordsStems.
            zip(corpusWords).
            zip(corpusWordsNorm).
            zip(nlpWords).
            flatMap {
                case (((corpusWordsStem, corpusWords), corpusWordsNorm), nlpWords) =>
                def getIndexes(elemValuesData: Set[String], corpusData: Seq[String]): Set[Int] =
                    elemValuesData.flatMap(vd => {
                        val i = corpusData.indexOf(vd)

                        if (i >= 0) Some(i) else None
                    })

                val idxs =
                    getIndexes(elemValuesSynsStems, corpusWordsStem) ++ getIndexes(elemValuesSynsNorm, corpusWordsNorm)

                def mkRequest(idx: Int, syn: String): NCSuggestionRequest = {
                    var newSen = substitute(corpusWords, syn, idx)

                    val nlpWordsNew = parser.parse(newSen.mkString(" "))

                    require(nlpWords.size == nlpWordsNew.size)

                    val pos = nlpWords(idx).pos
                    val posNew = nlpWordsNew(idx).pos

                    if (NOUNS_POS_SINGULAR.contains(pos) && NOUNS_POS_PLURALS.contains(posNew)) {
                        println(s"newSen1=$newSen")

                        newSen = substitute(corpusWords, CONVERTER.depluralize(syn), idx)

                        println(s"newSen2=$newSen")
                    }
                    else if (NOUNS_POS_PLURALS.contains(pos) && NOUNS_POS_SINGULAR.contains(posNew)) {
                        println(s"newSen1=$newSen")

                        newSen = substitute(corpusWords, CONVERTER.pluralize(syn), idx)

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
      * @param score
      * @return
      */
    private def normalize(score: Double): Double = score / MAX_CTXWORD_SCORE

    /**
      *
      * @param cfg
      * @param key
      * @return
      */
    private def getCorpusData(cfg: NCCtxWordConfigMdo, key: ModelProbeKey, parent: Span = null):
        Map[/** Element ID */String, ScoreHolder] =
        corpuses.synchronized { corpuses.get(key) } match {
            case Some(cache) => cache
            case None =>
                val res = askSamples(cfg, parent)

                corpuses.synchronized { corpuses += key -> res }

                res
        }

    /**
      *
      * @param cfg
      * @param key
      * @return
      */
    private def getValuesData(cfg: NCCtxWordConfigMdo, key: ModelProbeKey): ValuesHolder =
        valuesStems.synchronized { valuesStems.get(key) } match {
            case Some(cache) => cache
            case None =>
                def mkMap(convert: String => String): Map[String, Set[String]] =
                    cfg.values.
                        flatMap { case (elemId, vals) => vals.map { case (_, vals) => vals.map(convert(_) -> elemId) } }.
                        flatten.
                        groupBy { case (converted, _) => converted }.
                        map { case (converted, map) => converted -> map.map {case (_, elemId) => elemId }.toSet }

                val res = ValuesHolder(normal = mkMap(_.toLowerCase), stems = mkMap(stem))

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
    private def askSamples(cfg: NCCtxWordConfigMdo, parent: Span = null): Map[/** Element ID */String, ScoreHolder] = {
        val corpusSeq = cfg.corpus.toSeq
        val corpusWords = corpusSeq.map(parser.parse(_).map(_.word))
        val nlpWords = corpusSeq.map(s => parser.parse(s))

        val corpusWordsStems = corpusWords.map(_.map(stem))
        val corpusWordsNorm = corpusWords.map(_.map(_.toLowerCase))

        val recs: Map[String, Seq[NCSuggestionRequest]] =
            (
                for (
                    (elemId, elemValues) <- cfg.values.toSeq;
                    // Uses single words synonyms only.
                    elemValuesSyns = elemValues.flatMap(_._2).toSet.filter(!_.contains(' '));
                    suggReq <- mkRequests(
                        nlpWords = nlpWords,
                        corpusWords = corpusWords,
                        corpusWordsStems = corpusWordsStems,
                        corpusWordsNorm = corpusWordsNorm,
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
            val resps = syncExec(NCSuggestSynonymManager.suggestWords(recs.flatMap(_._2).toSeq, parent = parent))

            if (DEBUG_MODE) {
                val t = NCAsciiTable()

                t #= ("Request", "Responses")

                for ((req, resp) <- resps) {
                    t += (
                        req,
                        s"${resp.map(p => s"${p.word}=${FMT.format(normalize(p.score))}").mkString(", ")}"
                    )
                }

                t.info(logger, Some("Corpus requests:"))
            }

            val respsSeq = resps.toSeq

            val req2Elem = recs.flatMap { case (elemId, recs) => recs.map(p => p -> elemId) }

            def mkMap(convert: (NCSuggestionRequest, NCWordSuggestion) => String) = {
                val seq: Seq[(String, Map[String, Seq[Double]])] = respsSeq.
                    map { case (req, suggs) =>
                        (
                            req2Elem(req),
                            suggs.groupBy(sygg => convert(req, sygg)).
                                map { case (key, suggs) => key -> suggs.map(p => normalize(p.score)) }
                        )
                    }

                seq.
                    groupBy { case (elemId, _) => elemId }.
                    map { case (elemId, data) => elemId -> {
                        val factors: Seq[(String, Seq[Double])] = data.flatMap(_._2)

                        factors.
                            groupBy{ case (word, _) => word }.
                            map { case (word, factors) => word -> factors.flatMap { case (_, factor) => factor } }
                    } }
            }

            val normalMap: Map[String, Map[String, Seq[Double]]] = mkMap { (_, sugg ) => sugg.word.toLowerCase }
            val stemMap = mkMap { (_, sugg ) => stem(sugg.word) }
            val lemmaMap = mkMap { (req, sugg ) => getSuggestionLemma(req, sugg) }

            (normalMap.keySet ++ stemMap.keySet ++ lemmaMap.keySet).map(elemId =>
                elemId ->
                    ScoreHolder(
                        normals = normalMap.getOrElse(elemId, Map.empty),
                        stems = stemMap.getOrElse(elemId, Map.empty),
                        lemmas = lemmaMap.getOrElse(elemId, Map.empty)
                    )
            ).toMap
        }
        else
            Map.empty[String, ScoreHolder]
    }

    /**
      *
      * @param elemScore
      * @param scores
      * @return
      */
    private def isMatched(elemScore: NCContextWordElementConfig, scores: Double*): Boolean =
        if (scores.nonEmpty) {
            import NCContextWordElementConfig.NCContextWordElementPolicy._

            val policy = elemScore.getPolicy
            val elemScoreVal = elemScore.getScore

            policy match {
                case MEDIAN =>
                    val sorted = scores.sorted
                    val mid = sorted.length / 2
                    val median = if (sorted.length % 2 == 0) (sorted(mid) + sorted(mid - 1)) / 2
                    else sorted(mid)

                    median >= elemScoreVal
                case ALL => scores.forall(_ >= elemScoreVal)
                case AVERAGE => scores.sum / scores.size >= elemScoreVal
                case ANY => scores.exists(_ >= elemScoreVal)

                case _ => throw new AssertionError(s"Unexpected policy: $policy")
            }
        }
        else
            false

    override def enrich(ns: NCNlpSentence, parent: Span): Unit =
        startScopedSpan("stop", parent) { _ =>
            ns.ctxWordConfig match {
                case Some(cfg) =>
                    val detected = mutable.HashMap.empty[NCNlpSentenceToken, mutable.HashSet[ElementScore]]

                    def add(
                        nounTok: NCNlpSentenceToken, elemId: String, scores: Score*
                    ): Unit = {
                        val tokElems = detected.getOrElseUpdate(nounTok, mutable.HashSet.empty[ElementScore])

                        tokElems.find(_.elementId == elemId) match {
                            case Some(ex) =>
                                tokElems += ElementScore(elemId, scores ++ ex.scores:_*)
                                tokElems -= ex
                            case None =>
                                tokElems += ElementScore(elemId, scores:_*)
                        }
                    }

                    val nounToks = ns.tokens.filter(t => NOUNS_POS.contains(t.pos))

                    if (nounToks.nonEmpty) {
                        val key = ModelProbeKey(cfg.probeId, cfg.modelId)

                        // 1. Values. Direct.
                        val valsData = getValuesData(cfg, key)

                        if (DEBUG_MODE)
                            logger.info(s"Values loaded [key=$key, data=$valsData]")

                        def get(m: Map[String, Set[String]], key: String): Set[String] = m.getOrElse(key, Set.empty)

                        for (
                            nounTok <- nounToks;
                                elemId <-
                                get(valsData.normal, nounTok.normText) ++
                                get(valsData.normal, nounTok.lemma.toLowerCase) ++
                                get(valsData.stems, nounTok.stem)
                        )
                            add(nounTok, elemId, Score(INCL_MAX_SCORE))

                        // 2. Via examples.
                        val mdlCorpusData: Map[String, ScoreHolder] = getCorpusData(cfg, key, parent)

                        if (DEBUG_MODE) {
                            val t = NCAsciiTable()

                            t #= ("Element", "Detailed")

                            for ((elemId, sh) <- mdlCorpusData)
                                t += (elemId, sh)

                            t.info(logger, Some(s"Model corpus processed [key=$key]"))
                        }

                        for (
                            nounTok <- nounToks;
                            (elemId, suggs) <- mdlCorpusData;
                            scores = suggs.get(nounTok.normText, nounTok.stem, nounTok.lemma)
                            if isMatched(cfg.elements(elemId), scores :_*);
                            score <- scores
                        )
                            add(nounTok, elemId, Score(score))

                        // 3. Ask for sentence.
                        val idxs = ns.tokens.flatMap(p => if (p.pos.startsWith("N")) Some(p.index)else None).toSeq
                        val reqs = idxs.map(idx => NCSuggestionRequest(ns.tokens.map(_.origText).toSeq, idx))

                        val resps: Map[NCWordSuggestion, NCSuggestionRequest] =
                            syncExec(
                                NCSuggestSynonymManager.suggestWords(reqs, parent = parent)).
                                flatMap { case (req, suggs) => suggs.map(_ -> req)
                            }

                        if (DEBUG_MODE) {
                            val t = NCAsciiTable()

                            t #= ("Request", "Responses")

                            resps.toSeq.groupBy(_._2.index).foreach { case (_, seq) =>
                                val sorted = seq.sortBy(-_._1.score)

                                t += (
                                    sorted.head._2,
                                    s"${sorted.map(_._1).
                                    map(p => s"${p.word}=${FMT.format(normalize(p.score))}").
                                    mkString(", ")}"
                                )
                            }

                            t.info(logger, Some(s"Sentence requests processing [key=$key, sentence=${ns.text}]"))
                        }

                        for (
                            // Token index (tokIdx) should be correct because request created from original words,
                            // separated by space, and Suggestion Manager uses space tokenizer.
                            (sugg, req) <- resps;
                                senScore = normalize(sugg.score);
                                (elemId, mdlCorpusSuggs) <- mdlCorpusData;
                                elemCfg = cfg.elements(elemId);
                                corpusScores =
                                    mdlCorpusSuggs.get(
                                        sugg.word.toLowerCase, stem(sugg.word), getSuggestionLemma(req, sugg)
                                    )
                                // TODO:
                                if isMatched(elemCfg, senScore) && isMatched(elemCfg, corpusScores :_*)
                        ) {
                            add(ns.tokens(req.index), elemId, Score(senScore, Some(sugg.word)))
//
//                            for (corpusScore <- corpusScores)
//                                add(ns.tokens(req.index), elemId, Score(corpusScore, Some(sugg.word)))
                        }
                    }

                    ns.ctxWordData = detected.map {
                        case (tok, scores) => tok.index -> scores.map(p => p.elementId -> p.scores.map(_.score).asJava).toMap
                    }.toMap

                    if (DEBUG_MODE) {
                        logger.info("Sentence detected elements:")

                        for ((tok, elems) <- detected)
                            logger.info(s"${tok.origText}: ${elems.sortBy(-_.scores.map(_.score).max).mkString(", ")}")
                    }
                case None => // No-op.
            }
        }

    /**
      *
      * @param probeId
      * @param parent
      */
    def onDisconnectProbe(probeId: String, parent: Span = null): Unit =
        startScopedSpan("onDisconnectProbe", parent) { _ =>
            valuesStems.synchronized { valuesStems --= valuesStems.keySet.filter(_.probeId == probeId) }
            corpuses.synchronized { corpuses --= corpuses.keySet.filter(_.probeId == probeId) }
        }
}