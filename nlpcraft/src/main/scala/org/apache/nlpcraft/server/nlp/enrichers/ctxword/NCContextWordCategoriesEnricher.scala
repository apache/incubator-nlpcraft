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
import org.apache.nlpcraft.server.mdo.NCCtxWordCategoriesConfigMdo
import org.apache.nlpcraft.server.nlp.core.{NCNlpParser, NCNlpServerManager, NCNlpWord}
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher
import org.apache.nlpcraft.server.sugsyn.{NCSuggestSynonymManager, NCSuggestionRequest, NCWordSuggestion}
import org.jibx.schema.codegen.extend.DefaultNameConverter

import java.text.DecimalFormat
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * ContextWord enricher.
  */
object NCContextWordCategoriesEnricher extends NCServerEnricher {
    private final val MAX_CTXWORD_SCORE = 2
    private final val INCL_MAX_CONFIDENCE = 1.0

    // TODO: use standard DEEP_DEBUG flag and change debug level.
    private final val DEBUG_MODE = true

    private final val CONVERTER = new DefaultNameConverter
    private final val FMT = new DecimalFormat("#0.00000")

    private case class Reason(word: String, suggestionConfidence: Double, corpusConfidence: Double) {
        override def toString: String =
            s"Word: $word, suggestionConf=${FMT.format(suggestionConfidence)}, corpusConf=${FMT.format(corpusConfidence)}"
    }

    private case class Confidence(value: Double, reason: Option[Reason] = None) {
        override def toString: String =
            s"${FMT.format(value)}(${if (reason.isDefined) s"via:'${reason.get}'" else "direct"})}"
    }

    private case class ModelProbeKey(probeId: String, modelId: String)

    private case class ElementConfidence(elementId: String, confidence: Confidence) {
        override def toString: String = s"Element [id=$elementId, confidence=$confidence]]"
    }

    private case class ValuesHolder(normal: Map[String, Set[String]], stems: Map[String, Set[String]]) {
        private def map2Str(m: Map[String, Set[String]]): String =
            m.toSeq.flatMap { case (v, elems) =>
                elems.toSeq.map(_ -> v) }.groupBy { case (v, _) => v }.map { case (v, seq) => v -> toStr(seq.map(_._2))
            }.mkString(", ")

        override def toString: String = s"Values [normal=${map2Str(normal)}, stems=${map2Str(stems)}]"
    }

    private case class ElementData(normals: Map[String, Double], stems: Map[String, Double], lemmas: Map[String, Double]) {
        def get(norm: String, stem: String, lemma: String): Option[Double] =
            normals.get(norm) match {
                case Some(v) => Some(v)
                case None =>
                    stems.get(stem) match {
                        case Some(v) => Some(v)
                        case None => lemmas.get(lemma)
                    }
            }
    }

    // Service which responsible for all confidences calculations.
    private object ConfMath {
        /**
          * Squeeze confidences values of some words to one value.
          *
          * @param confs Confidences values for some word. It is result of corpus processing.
          */
        def squeeze(confs: Seq[Double]): Option[Double] =
             // Drops if there is not enough data.
            if (confs.length < 3)
                None
            else {
                // Takes 50% of most important (or first 2 at least) and calculates average value.
                val n = Math.max((confs.length * 0.5).intValue(), 2)

                val maxN = confs.sortBy(-_).take(n)

                Some(maxN.sum / maxN.length)
            }

        /**
          * Calculates confidence values based on suggested confidence and corpus confidence.
          *
          * @param suggConf Suggestion confidence for noun of given sentence.
          * @param corpusConf Corpus confidence which found via suggestion, co-reference.
          */
        def calculate(suggConf: Double, corpusConf: Double): Double =
            // Corpus data is more important. Empirical factors configured.
            calcWeightedGeoMean(Map(suggConf -> 1, corpusConf -> 3))

        /**
          * Calculates weighted geometrical mean value.
          *
          * @param vals2Weights Values with their weights.
          */
        private def calcWeightedGeoMean(vals2Weights: Map[Double, Double]): Double =
            Math.pow(vals2Weights.map { case (v, weight) => Math.pow(v, weight) }.product, 1.0 / vals2Weights.values.sum)
    }

    @volatile private var valuesStems: mutable.HashMap[ModelProbeKey, ValuesHolder] = _
    @volatile private var elemsCorpuses: mutable.HashMap[ModelProbeKey, Map[String, ElementData]] = _
    @volatile private var parser: NCNlpParser = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        valuesStems = mutable.HashMap.empty
        elemsCorpuses = mutable.HashMap.empty
        parser = NCNlpServerManager.getParser

        ackStarted()
    }

    override def stop(parent: Span = null): Unit =
        startScopedSpan("stop", parent) { _ =>
            ackStopping()

            parser = null
            elemsCorpuses = null
            valuesStems = null

            ackStopped()
        }

    /**
      *
      * @param seq
      * @return
      */
    private def toStr(seq: Seq[String]): String = seq.mkString("{ ", ", ", " }")

    /**
      *
      * @param s
      * @return
      */
    private def norm(s: String): String = s.toLowerCase

    /**
      *
      * @param awaitable
      * @tparam T
      * @return
      */
    private def syncExec[T](awaitable: scala.concurrent.Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

    /**
      *
      * @param corpusNlpSeq
      * @param elemSingleVals
      * @return
      */
    private def mkRequests(corpusNlpSeq: Seq[Seq[NCNlpWord]], elemSingleVals: Set[String]): Iterable[NCSuggestionRequest] =
        corpusNlpSeq.
            flatMap {
                corpusNlp =>
                    lazy val corpusWords = corpusNlp.map(_.word)

                    def getIndexes(corpVals: Seq[String], vals: Set[String]): Set[Int] =
                        vals.flatMap(v => {
                            val i = corpVals.indexOf(v)

                            if (i >= 0) Some(i) else None
                        })

                    val elemSingleValsNorm = elemSingleVals.map(norm)
                    val elemSingleValsStem = elemSingleVals.map(stem)

                    val idxs =
                        getIndexes(corpusNlp.map(_.normalWord), elemSingleValsNorm) ++
                        getIndexes(corpusNlp.map(_.stem), elemSingleValsStem) ++
                        // Sample can have word in plural forms. We can compare them with synonyms values.
                        getIndexes(corpusNlp.map(p => norm(p.lemma)), elemSingleValsNorm)

                    def mkRequest(idx: Int, syn: String): NCSuggestionRequest = {
                        var newSen = substitute(corpusWords, syn, idx)

                        val nlpWordsNew = parser.parse(newSen.mkString(" "))

                        require(corpusWords.size == nlpWordsNew.size)

                        val pos = corpusNlp(idx).pos
                        val posNew = nlpWordsNew(idx).pos

                        if (NOUNS_POS_SINGULAR.contains(pos) && NOUNS_POS_PLURALS.contains(posNew))
                            newSen = substitute(corpusWords, CONVERTER.depluralize(syn), idx)
                        else if (NOUNS_POS_PLURALS.contains(pos) && NOUNS_POS_SINGULAR.contains(posNew))
                            newSen = substitute(corpusWords, CONVERTER.pluralize(syn), idx)

                        NCSuggestionRequest(newSen, idx)
                    }

                    for (idx <- idxs; syn <- elemSingleVals)
                        yield mkRequest(idx, syn)
            }

    /**
      *
      * @param conf
      * @return
      */
    private def normalizeConf(conf: Double): Double = conf / MAX_CTXWORD_SCORE

    /**
      *
      * @param cfg
      * @param key
      * @return
      */
    private def getCorpusData(cfg: NCCtxWordCategoriesConfigMdo, key: ModelProbeKey, parent: Span = null):
        Map[/** Element ID */String, ElementData] =
        elemsCorpuses.synchronized { elemsCorpuses.get(key) } match {
            case Some(cache) => cache
            case None =>
                val res = askSamples(cfg, parent)

                elemsCorpuses.synchronized { elemsCorpuses += key -> res }

                res
        }

    /**
      *
      * @param cfg
      * @param key
      * @return
      */
    private def getValuesData(cfg: NCCtxWordCategoriesConfigMdo, key: ModelProbeKey): ValuesHolder =
        valuesStems.synchronized { valuesStems.get(key) } match {
            case Some(cache) => cache
            case None =>
                def mkMap(convert: String => String): Map[String, Set[String]] =
                    cfg.singleValues.
                        flatMap { case (elemId, vals) => vals.map { case (_, vals) => vals.map(convert(_) -> elemId) } }.
                        flatten.
                        groupBy { case (converted, _) => converted }.
                        map { case (converted, map) => converted -> map.map { case (_, elemId) => elemId }.toSet }

                val normsMap = mkMap(norm)
                val stemsMap = mkMap(stem)

                val h = ValuesHolder(normal = normsMap, stems = stemsMap.filter(p => !normsMap.keySet.contains(p._1)))

                valuesStems.synchronized { valuesStems += key -> h }

                h
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

        words.zipWithIndex.map { case (w, i) => if (i != index) w
        else word
        }
    }

    /**
      *
      * @param req
      * @param sugg
      * @return
      */
    private def getLemma(req: NCSuggestionRequest, sugg: NCWordSuggestion): String =
        parser.parse(substitute(req.words, sugg.word, req.index).mkString(" "))(req.index).lemma

    /**
      *
      * @param cfg
      * @return
      */
    @throws[NCE]
    private def askSamples(cfg: NCCtxWordCategoriesConfigMdo, parent: Span = null):
        Map[/** Element ID */String, ElementData] = {
        val corpusNlp = cfg.corpus.toSeq.map(s => parser.parse(s))

        val recs: Map[String, Seq[NCSuggestionRequest]] =
            (
                for (
                    (elemId, elemSingleVals) <- cfg.singleValues.toSeq;
                    elemSingleValsSet = elemSingleVals.flatMap(_._2).toSet;
                    suggReq <- mkRequests(corpusNlp, elemSingleValsSet)
                ) yield (elemId, suggReq)
            ).
                groupBy { case (elemId, _) => elemId }.
                map { case (elemId, m) => elemId -> m.map(_._2) }

        if (recs.nonEmpty) {
            val respsSeq: Seq[(NCSuggestionRequest, Seq[NCWordSuggestion])] =
                syncExec(NCSuggestSynonymManager.suggestWords(recs.flatMap(_._2).toSeq, parent = parent)).
                    toSeq.sortBy(p => (p._1.words.mkString, p._1.index))

            if (DEBUG_MODE) {
                val t = NCAsciiTable()

                t #= ("Request", "Responses")

                for ((req, resp) <- respsSeq)
                    t += (
                        req,
                        s"${resp.map(p => s"${p.word}=${FMT.format(normalizeConf(p.score))}").mkString(", ")}"
                    )

                t.info(logger, Some("Corpus requests:"))
            }

            val req2Elem = recs.flatMap { case (elemId, recs) => recs.map(p => p -> elemId) }

            def mkMap(convert: (NCSuggestionRequest, NCWordSuggestion) => String):
                Map[/** Element ID */ String, /** Word key */ Map[String, /** Confidences */ Seq[Double]]] = {
                val seq: Seq[(String, Map[String, Double])] =
                    respsSeq.
                        map { case (req, suggs) =>
                            (
                                req2Elem(req),
                                suggs.groupBy(sygg => convert(req, sygg)).
                                    // If different word forms have different confidence (`Abc`- 0.9, `abc`- 0.7),
                                    // we use maximum (0.9).
                                    map { case (key, suggs) => key -> suggs.map(p => normalizeConf(p.score)).max }
                            )
                        }
                seq.
                    groupBy { case (elemId, _) => elemId }.
                    map { case (elemId, data) =>
                        elemId ->
                            data.flatMap(_._2).
                                groupBy { case (word, _) => word }.
                                map { case (word, data) => word -> data.map { case (_, confs) => confs } }
                    }
            }

            val normals = mkMap { (_, sugg) => norm(sugg.word) }
            val stems = mkMap { (_, sugg) => stem(sugg.word) }
            val lemmas = mkMap { (req, sugg) => getLemma(req, sugg) }

            def mkTable(): NCAsciiTable =
                if (DEBUG_MODE) {
                    val t = NCAsciiTable()

                    t #= ("Element", "Confidences for normal forms")

                    t
                }
                else
                    null

            val (tabAll, tabNorm) = (mkTable(), mkTable())

            val res =
                (normals.keySet ++ stems.keySet ++ lemmas.keySet).map(elemId =>
                    elemId -> {
                        def get[T, K](m: Map[String, Map[T, K]]): Map[T, K] = m.getOrElse(elemId, Map.empty)

                        (get(normals), get(stems), get(lemmas))
                    }
                ).
                    toMap.
                    map { case (elemId, (normals, stems, lemmas)) =>
                        val normalsAll = normals
                        val stemsAll = stems -- normals.keySet
                        val lemmasAll = lemmas -- normals.keySet -- stems.keySet

                        if (DEBUG_MODE)
                            tabAll += (
                                s"Element: $elemId [" +
                                s"normals=${normalsAll.size}, " +
                                s"stems=${stemsAll.size}, " +
                                s"lemmas=${lemmasAll.size}" +
                                s"]",
                                toStr(
                                    normalsAll.toSeq.
                                        sortBy(p => (-p._2.max, -p._2.size)).map(
                                        { case (k, confs) => s"$k=${toStr(confs.sortBy(-_).map(p => FMT.format(p)))}" }
                                    )
                                )
                            )

                        def squeeze(map: Map[String, Seq[Double]]): Map[String, Double] =
                            map.flatMap { case (wordKey, confs) =>
                                ConfMath.squeeze(confs) match {
                                    case Some(conf) => Some(wordKey -> conf)
                                    case None => None
                                }
                            }

                        val normalsNorm = squeeze(normalsAll)
                        val stemsNorm = squeeze(stemsAll)
                        val lemmasNorm = squeeze(lemmasAll)

                        if (DEBUG_MODE)
                            tabNorm += (
                                s"Element: $elemId [" +
                                s"normals=${normalsNorm.size}, " +
                                s"stems=${stemsNorm.size}, " +
                                s"lemmas=${lemmasNorm.size}",
                                toStr(
                                    normalsNorm.toSeq.sortBy(-_._2).map(
                                        { case (k, factor) => s"$k=${FMT.format(factor)}" }
                                    )
                                )
                            )

                        elemId -> ElementData(normalsNorm, stemsNorm, lemmasNorm)
                    }

            if (DEBUG_MODE) {
                tabAll.info(logger, Some("Model corpus all confidences:"))
                tabNorm.info(logger, Some("Model corpus normalized confidences:"))
            }

            res
        }
        else
            Map.empty[String, ElementData]
    }

    override def enrich(ns: NCNlpSentence, parent: Span): Unit =
        startScopedSpan("stop", parent) { _ =>
            ns.ctxWordConfig match {
                case Some(cfg) =>
                    val detected = mutable.HashMap.empty[NCNlpSentenceToken, mutable.HashSet[ElementConfidence]]

                    def add(nounTok: NCNlpSentenceToken, elemId: String, conf: Confidence): Unit = {
                        val tokElems = detected.getOrElseUpdate(nounTok, mutable.HashSet.empty[ElementConfidence])

                        tokElems.find(_.elementId == elemId) match {
                            case Some(exConf) =>
                                if (conf.value > exConf.confidence.value) {
                                    tokElems += ElementConfidence(elemId, conf)
                                    tokElems -= exConf
                                }
                            case None =>
                                tokElems += ElementConfidence(elemId, conf)
                        }
                    }

                    val nouns = ns.tokens.filter(t => NOUNS_POS.contains(t.pos))

                    if (nouns.nonEmpty) {
                        val key = ModelProbeKey(cfg.probeId, cfg.modelId)

                        // 1. Values. Direct.
                        val vd = getValuesData(cfg, key)

                        val (vNorms, vStems) = (vd.normal, vd.stems)

                        if (DEBUG_MODE)
                            logger.info(
                                s"Model loaded [" +
                                s"key=$key, elements: " +
                                s"${cfg.elements.mkString(", ")}, " +
                                s"values data=$vd]"
                            )

                        def get(m: Map[String, Set[String]], key: String): Set[String] = m.getOrElse(key, Set.empty)

                        for (
                            n <- nouns;
                            elemId <- get(vNorms, n.normText) ++ get(vNorms, norm(n.lemma)) ++ get(vStems, n.stem)
                        )
                            add(n, elemId, Confidence(INCL_MAX_CONFIDENCE))

                        // 2. Via corpus.
                        val corpusData = getCorpusData(cfg, key, parent)

                        for (
                            nounTok <- nouns;
                            (elemId, elemData) <- corpusData;
                            confOpt = elemData.get(nounTok.normText, nounTok.stem, nounTok.lemma)
                            if confOpt.isDefined && confOpt.get >= cfg.elements(elemId)
                        )
                            add(nounTok, elemId, Confidence(confOpt.get))

                        // 3. Ask for sentence (via co-references)
                        val idxs = ns.tokens.flatMap(p => if (p.pos.startsWith("N")) Some(p.index) else None).toSeq
                        val reqs = idxs.map(idx => NCSuggestionRequest(ns.tokens.map(_.origText).toSeq, idx))

                        val resps: Map[NCWordSuggestion, NCSuggestionRequest] =
                            syncExec(
                                NCSuggestSynonymManager.suggestWords(reqs, parent = parent)
                            ).flatMap { case (req, suggs) => suggs.map(_ -> req) }

                        if (DEBUG_MODE) {
                            val t = NCAsciiTable()

                            t #= ("Request", "Responses")

                            resps.toSeq.groupBy(_._2.index).foreach { case (_, seq) =>
                                val sorted = seq.sortBy(-_._1.score)

                                t += (
                                    sorted.head._2,
                                    s"${
                                        sorted.map(_._1).
                                            map(p => s"${p.word}=${FMT.format(normalizeConf(p.score))}").
                                            mkString(", ")
                                    }"
                                )
                            }

                            t.info(logger, Some(s"Sentence requests processing [key=$key, sentence=${ns.text}]"))
                        }

                        case class Key(elementId: String, token: NCNlpSentenceToken)

                        val missed = if (DEBUG_MODE) mutable.HashMap.empty[Key, ArrayBuffer[Confidence]] else null

                        for (
                            // Token index (tokIdx) should be correct because request created from original words,
                            // separated by space, and Suggestion Manager uses space tokenizer.
                            (sugg, req) <- resps.toSeq.sortBy(_._2.index);
                            suggConf = normalizeConf(sugg.score);
                            (elemId, elemData) <- corpusData;
                            elemConf = cfg.elements(elemId);
                            corpConfOpt = elemData.get(norm(sugg.word), stem(sugg.word), getLemma(req, sugg))
                            if corpConfOpt.isDefined;
                            corpConf = corpConfOpt.get;
                            normConf = ConfMath.calculate(suggConf, corpConf)
                        ) {
                            def mkConf(): Confidence = Confidence(normConf, Some(Reason(sugg.word, suggConf, corpConf)))
                            def getToken: NCNlpSentenceToken = ns.tokens(req.index)

                            if (normConf >= elemConf)
                                add(getToken, elemId, mkConf())
                            else if (DEBUG_MODE)
                                missed.getOrElseUpdate(Key(elemId, getToken), mutable.ArrayBuffer.empty) += mkConf()
                        }

                        ns.ctxWordCategories = detected.map {
                            case (tok, confs) => tok.index -> confs.map(p => p.elementId -> p.confidence.value).toMap
                        }.toMap

                        if (DEBUG_MODE) {
                            require(missed != null)

                            missed.filter { case (key, _) =>
                                !detected.exists {
                                    case (tok, confs) => confs.exists(conf => Key(conf.elementId, tok) == key)
                                }
                            }.sortBy { case (key, _) => (key.token.index, key.elementId) }.
                                foreach { case (key, confs) =>
                                    logger.info(
                                        s"Unsuccessful attempt [" +
                                        s"elementId=${key.elementId}, " +
                                        s"tokenWordIndexes=${key.token.wordIndexes.mkString(",")}, " +
                                        s"confidences=${confs.sortBy(-_.value).mkString(", ")}" +
                                        s"]"
                                    )
                                }

                            logger.info("Sentence detected elements:")

                            for ((tok, elems) <- detected)
                                logger.info(s"${tok.origText}: ${elems.mkString(", ")}")
                        }
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
            elemsCorpuses.synchronized { elemsCorpuses --= elemsCorpuses.keySet.filter(_.probeId == probeId) }
        }
}