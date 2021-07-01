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

    private final val FN = new DecimalFormat("#0.00000")

    private case class ModelProbeKey(probeId: String, modelId: String)
    private case class ElementScore(elementId: String, scores: Double*) {
        override def toString: String =
            s"Element [id=$elementId, scores=${scores.sortBy(p => -p).map(FN.format).mkString("{ ", ", ", " }")}]"
    }

    object ValuesHolder {
        def apply(
            normal: Map[/**  Normal value */ String, /** Element ID */ Set[String]],
            stems: Map[/** Value's stem */ String, /** Element ID */ Set[String]]
        ): ValuesHolder = new ValuesHolder(
            normal,
            stems.filter(p => !normal.keySet.contains(p._1))
        )
    }

    class ValuesHolder(
        val normal: Map[/**  Normal value */ String, /** Element ID */ Set[String]],
        val stems: Map[/** Value's stem */ String, /** Element ID */ Set[String]]
    ) {
        private def map2Str(m: Map[String, Set[String]]): String =
            m.toSeq.flatMap(p => p._2.toSeq.map(x => x -> p._1)).
                groupBy(_._1).map(p => p._1 -> p._2.map(_._2).
                mkString("{ ", ", ", " }")).mkString(", ")

        override def toString: String = s"Values [normal=${map2Str(normal)}, stems=${map2Str(stems)}]"
    }

    object ScoreHolder {
        private final val EXCL_MIN_SCORE = -1.0

        def apply(
            normals: Map[/** Normal value */ String, /** Score */ Double],
            stems: Map[/** Stem */ String, /** Score */ Double],
            lemmas: Map[/** Lemma */ String, /** Score */ Double]
        ): ScoreHolder =
            new ScoreHolder(
                normals,
                stems.filter(p => !normals.keySet.contains(p._1)),
                lemmas.filter(p => !normals.keySet.contains(p._1) && !stems.keySet.contains(p._1))
            )
    }

    import ScoreHolder._

    class ScoreHolder(
        normals: Map[/** Normal value */ String, /** Score */ Double],
        stems: Map[/** Stem */ String, /** Score */ Double],
        lemmas: Map[/** Lemma */ String, /** Score */ Double]
    ) {
        def get(norm: String, stem: String, lemma: String): Option[Double] = {
            val max =
                Seq(
                    normals.getOrElse(norm, EXCL_MIN_SCORE),
                    stems.getOrElse(stem, EXCL_MIN_SCORE),
                    lemmas.getOrElse(lemma, EXCL_MIN_SCORE)
                ).max

            if (max == EXCL_MIN_SCORE) None else Some(max)
        }

        private def sort(m: Map[String, Double]): String =
            m.toSeq.sortBy(-_._2).map({ case (k, v) => s"$k=${FN.format(v)}" }).mkString(", ")
        override def toString: String = s"Score [normal: ${sort(normals)}, stems: ${sort(stems)}, lemma: ${sort(lemmas)}]"
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

        val recs: Map[/** Element ID */String, Seq[NCSuggestionRequest]] =
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
                        s"${resp.sortBy(-_.score).map(p => s"${p.word}=${FN.format(normalize(p.score))}").mkString(", ")}"
                    )
                }

                t.info(logger, Some("Corpus requests:"))
            }

            val respsSeq = resps.toSeq

            val req2Elem = recs.flatMap { case (elemId, recs) => recs.map(p => p -> elemId) }

            def mkMap(convert: (NCSuggestionRequest, NCWordSuggestion) => String): Map[String, Map[String, Double]] =
                respsSeq.
                    map { case (req, suggs) =>
                        (req2Elem(req),
                            suggs.groupBy(sygg => convert(req, sygg)).
                                map { case (conv, suggs) =>
                                conv -> normalize(suggs.map(_.score).max)
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
    private def isMatched(elemScore: NCContextWordElementConfig, scores: Double*): Boolean = {
        require(scores.nonEmpty)

        import NCContextWordElementConfig.NCContextWordElementPolicy._

        val policy = elemScore.getPolicy
        val elemScoreVal = elemScore.getScore

        policy match {
            case MEDIAN =>
                val sorted = scores.sorted
                val mid = sorted.length / 2
                val median = if (sorted.length % 2 == 0) (sorted(mid) + sorted(mid - 1)) / 2 else sorted(mid)

                median >= elemScoreVal
            case ALL => scores.forall(_ >= elemScoreVal)
            case AVERAGE => scores.sum / scores.size >= elemScoreVal
            case ANY => scores.exists(_ >= elemScoreVal)

            case _ => throw new AssertionError(s"Unexpected policy: $policy")
        }
    }

    override def enrich(ns: NCNlpSentence, parent: Span): Unit =
        startScopedSpan("stop", parent) { _ =>
            ns.ctxWordConfig match {
                case Some(cfg) =>
                    val detected = mutable.HashMap.empty[NCNlpSentenceToken, mutable.HashSet[ElementScore]]

                    def add(
                        nounTok: NCNlpSentenceToken, elemId: String, scores: Double*
                    ): Unit = {
                        val tokElems = detected.getOrElseUpdate(nounTok, mutable.HashSet.empty[ElementScore])

                        tokElems += (
                            tokElems.find(_.elementId == elemId) match {
                                case Some(ex) => ElementScore(elemId, scores ++ ex.scores:_*)
                                case None => ElementScore(elemId, scores:_*)
                            }
                        )
                    }

                    val nounToks = ns.tokens.filter(t => NOUNS_POS.contains(t.pos))

                    if (nounToks.nonEmpty) {
                        val key = ModelProbeKey(cfg.probeId, cfg.modelId)

                        // 1. Values. Direct.
                        val valuesData = getValuesData(cfg, key)

                        if (DEBUG_MODE)
                            logger.info(s"Values loaded [probeKey=$key, data=$valuesData]")

                        for (
                            nounTok <- nounToks;
                                elemId <-
                                    valuesData.normal.getOrElse(nounTok.normText, Set.empty) ++
                                    valuesData.normal.getOrElse(nounTok.lemma.toLowerCase, Set.empty) ++
                                    valuesData.stems.getOrElse(nounTok.stem, Set.empty)
                        )
                            add(nounTok, elemId, INCL_MAX_SCORE)

                        // 2. Via examples.
                        val mdlCorpusData = getCorpusData(cfg, key, parent)

                        if (DEBUG_MODE) {
                            val t = NCAsciiTable()

                            t #= ("Element", "Scores")

                            for (entry <- mdlCorpusData)
                                t += (entry._1, entry._2)

                            t.info(logger, Some(s"Model corpus processed [probeKey=$key]"))
                        }

                        for (
                            nounTok <- nounToks;
                            (elemId, suggs) <- mdlCorpusData;
                            scoreOpt = suggs.get(nounTok.normText, nounTok.stem, nounTok.lemma)
                            if scoreOpt.isDefined && isMatched(cfg.elements(elemId), scoreOpt.get)
                        )
                            add(nounTok, elemId, scoreOpt.get)

                        // 3. Ask for sentence.
                        val idxs = ns.tokens.flatMap(p => if (p.pos.startsWith("N")) Some(p.index)
                        else None).toSeq
                        val reqs = idxs.map(idx => NCSuggestionRequest(ns.tokens.map(_.origText).toSeq, idx))

                        val resps =
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
                                        map(p => s"${p.word}=${FN.format(normalize(p.score))}").
                                        mkString(", ")}"
                                )
                            }

                            t.info(logger, Some(s"Sentence requests processing [probeKey=$key, sentence=${ns.text}]"))
                        }

                        for (
                            // Token index (tokIdx) should be correct because request created from original words,
                            // separated by space, and Suggestion Manager uses space tokenizer.
                            (sugg, req) <- resps;
                                senScore = normalize(sugg.score);
                                (elemId, mdlCorpusSuggs) <- mdlCorpusData;
                                elemScore = cfg.elements(elemId);
                                corpusScoreOpt =
                                    mdlCorpusSuggs.get(
                                        sugg.word.toLowerCase, stem(sugg.word), getSuggestionLemma(req, sugg)
                                    )
                                if corpusScoreOpt.isDefined && isMatched(elemScore, corpusScoreOpt.get, senScore)
                        )
                            add(ns.tokens(req.index), elemId, senScore, corpusScoreOpt.get)
                    }

                    ns.ctxWordData = detected.map {
                        case (tok, scores) => tok.index -> scores.map(p => p.elementId -> p.scores.asJava).toMap
                    }.toMap

                    if (DEBUG_MODE)
                        logger.info(
                            s"Sentence detected elements: " +
                            s"${detected.map { case (tok, scores)  => tok.origText -> scores.mkString(", ") }}"
                        )
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