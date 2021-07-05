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

package org.apache.nlpcraft.server.sugsyn

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.opencensus.trace.Span
import org.apache.http.HttpResponse
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.util.EntityUtils
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp.core.NCNlpPorterStemmer
import org.apache.nlpcraft.common.pool.NCThreadPoolManager
import org.apache.nlpcraft.server.probe.NCProbeManager

import java.util
import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

/**
 * Synonym suggestion manager.
 */
object NCSuggestSynonymManager extends NCService {
    // For context word server requests.
    private final val MAX_LIMIT: Int = 10000
    private final val BATCH_SIZE = 20
    private final val DFLT_MIN_SCORE = 0.0

    // For warnings.
    private final val MIN_CNT_INTENT = 5
    private final val MIN_CNT_MODEL = 20

    private final val GSON = new Gson
    private final val TYPE_RESP = new TypeToken[util.List[util.List[NCWordSuggestion]]]() {}.getType
    private final val SEPARATORS = Seq('?', ',', '.', '-', '!')

    private implicit final val ec: ExecutionContext = NCThreadPoolManager.getSystemContext

    private object Config extends NCConfigurable {
        val urlOpt: Option[String] = getStringOpt("nlpcraft.server.ctxword.url")
    }

    private final val HANDLER: ResponseHandler[Seq[Seq[NCWordSuggestion]]] =
        (resp: HttpResponse) => {
            val code = resp.getStatusLine.getStatusCode
            val e = resp.getEntity

            val js = if (e != null) EntityUtils.toString(e) else null

            if (js == null)
                throw new NCE(s"Unexpected empty HTTP response from `ctxword` server [code=$code]")

            code match {
                case 200 =>
                    val data: util.List[util.List[NCWordSuggestion]] = GSON.fromJson(js, TYPE_RESP)

                    data.asScala.map(p => if (p.isEmpty) Seq.empty else p.asScala.toSeq).toSeq

                case _ =>
                    throw new NCE(
                    s"Unexpected HTTP response from `ctxword` server [" +
                    s"code=$code, " +
                    s"response=$js" +
                    s"]"
                )
            }
        }


    case class RequestData(sentence: String, ex: String, elmId: String, index: Int)
    case class RestRequestSentence(text: String, indexes: util.List[Int])
    object RestRequestSentence {
        def apply(text: String, index: Int): RestRequestSentence = new RestRequestSentence(text, Seq(index).asJava)


    }
    case class RestRequest(sentences: util.List[RestRequestSentence], limit: Int, minScore: Double)
    case class Word(word: String, stem: String) {
        require(!word.contains(" "), s"Word cannot contains spaces: $word")
        require(
            word.forall(ch =>
                ch.isLetterOrDigit ||
                ch == '\'' ||
                SEPARATORS.contains(ch)
            ),
            s"Unsupported symbols: $word"
        )
    }
    case class SuggestionResult(synonym: String, score: Double)

    private def split(s: String): Seq[String] = U.splitTrimFilter(s, " ")
    private def toStem(s: String): String = split(s).map(NCNlpPorterStemmer.stem).mkString(" ")
    private def toStemWord(s: String): String = NCNlpPorterStemmer.stem(s)

    @throws[NCE]
    private def mkUrl = s"${Config.urlOpt.getOrElse(throw new NCE("Context word server is not configured."))}/suggestions"

    private def request(cli: CloseableHttpClient, post: HttpPost): Seq[Seq[NCWordSuggestion]] = {
        val resps: Seq[Seq[NCWordSuggestion]] =
            try
                cli.execute(post, HANDLER)
            finally
                post.releaseConnection()

        resps
    }

    /**
     *
     * @param seq1
     * @param seq2
     */
    private def getAllSlices(seq1: Seq[String], seq2: Seq[String]): Seq[Int] = {
        val seq = mutable.Buffer.empty[Int]

        var i = seq1.indexOfSlice(seq2)

        while (i >= 0) {
            seq += i

            i = seq1.indexOfSlice(seq2, i + 1)
        }

        seq
    }

    /**
     * TODO: refactor async call (waiting should be dropped.)
     * @param mdlId
     * @param minScoreOpt
     * @param parent
     * @return
     */
    def suggestModel(mdlId: String, minScoreOpt: Option[Double], parent: Span = null): Future[NCSuggestSynonymResult] =
        startScopedSpan("suggestModel", parent, "mdlId" -> mdlId) { _ =>
            val now = U.now()

            val promise = Promise[NCSuggestSynonymResult]()

            NCProbeManager.getModelInfo(mdlId, parent).onComplete {
                case Success(m) =>
                    try {
                        require(
                            m.containsKey("macros") &&
                            m.containsKey("synonyms") &&
                            m.containsKey("samples")
                        )

                        val mdlMacros = m.get("macros").
                            asInstanceOf[util.Map[String, String]].asScala
                        val mdlSyns = m.get("synonyms").
                            asInstanceOf[util.Map[String, util.List[String]]].asScala.map(p => p._1 -> p._2.asScala)
                        val mdlExs = m.get("samples").
                            asInstanceOf[util.Map[String, util.List[util.List[String]]]].asScala.
                            map(p => p._1 -> p._2.asScala.flatMap(_.asScala.toSeq).distinct)

                        val minScore = minScoreOpt.getOrElse(DFLT_MIN_SCORE)

                        def onError(err: String): Unit =
                            promise.success(
                                NCSuggestSynonymResult(
                                    modelId = mdlId,
                                    minScore = minScore,
                                    durationMs = U.now() - now,
                                    timestamp = now,
                                    error = err,
                                    suggestions = Seq.empty.asJava,
                                    warnings = Seq.empty.asJava
                                )
                            )

                        if (mdlExs.isEmpty)
                            onError(s"Missed intents samples for: `$mdlId``")
                        else {
                            val url = mkUrl

                            val allSamplesCnt = mdlExs.map { case (_, samples) => samples.size }.sum

                            val warns = mutable.ArrayBuffer.empty[String]

                            if (allSamplesCnt < MIN_CNT_MODEL)
                                warns +=
                                    s"Model has too few ($allSamplesCnt) intents samples. " +
                                    s"Try to increase overall sample count to at least $MIN_CNT_MODEL."

                            else {
                                val ids =
                                    mdlExs.
                                        filter { case (_, samples) => samples.size < MIN_CNT_INTENT }.
                                        map { case (intentId, _) => intentId }

                                if (ids.nonEmpty)
                                    warns +=
                                        s"Following model intent have too few samples (${ids.mkString(", ")}). " +
                                        s"Try to increase overall sample count to at least $MIN_CNT_INTENT."
                            }

                            val parser = new NCMacroParser()

                            mdlMacros.foreach { case (name, str) => parser.addMacro(name, str) }

                            // Note that we don't use system tokenizer, because 'ctxword' module' doesn't have this tokenizer.
                            // We split examples words by spaces. We also treat separator as separate words.

                            val exs = mdlExs.
                                flatMap { case (_, samples) => samples }.
                                map(ex => SEPARATORS.foldLeft(ex)((s, ch) => s.replaceAll(s"\\$ch", s" $ch "))).
                                map(ex => {
                                    val seq = ex.split(" ")

                                    seq -> seq.map(toStemWord)
                                }).
                                toMap

                            val elemSyns =
                                mdlSyns.map { case (elemId, syns) => elemId -> syns.flatMap(parser.expand) }.
                                    map { case (id, seq) => id -> seq.map(txt => split(txt).map(p => Word(p, toStemWord(p)))) }

                            val allReqs =
                                elemSyns.map {
                                    case (elemId, syns) =>
                                        // Current implementation supports suggestions only for single words synonyms.
                                        val normSyns: Seq[Seq[Word]] = syns.filter(_.size == 1)
                                        val synsStems = normSyns.map(_.map(_.stem))
                                        val synsWords = normSyns.map(_.map(_.word))

                                        val reqs =
                                            exs.flatMap { case (exWords, exampleStems) =>
                                                val exIdxs = synsStems.flatMap(synStems => getAllSlices(exampleStems.toIndexedSeq, synStems))

                                                def mkRequestData(idx: Int, synStems: Seq[String], synStemsIdx: Int): RequestData = {
                                                    val fromIncl = idx
                                                    val toExcl = idx + synStems.length

                                                    RequestData(
                                                        sentence = exWords.zipWithIndex.flatMap {
                                                            case (exWord, i) =>
                                                                i match {
                                                                    case x if x == fromIncl => synsWords(synStemsIdx)
                                                                    case x if x > fromIncl && x < toExcl => Seq.empty
                                                                    case _ => Seq(exWord)
                                                                }
                                                        }.mkString(" "),
                                                        ex = exWords.mkString(" "),
                                                        elmId = elemId,
                                                        index = idx
                                                    )
                                                }

                                                (for (idx <- exIdxs; (synStems, i) <- synsStems.zipWithIndex)
                                                    yield mkRequestData(idx, synStems, i)).distinct
                                            }

                                        elemId -> reqs.toSet
                                }.filter(_._2.nonEmpty)

                            val noExElems =
                                mdlSyns.
                                    filter { case (elemId, syns) => syns.nonEmpty && !allReqs.contains(elemId) }.
                                    map { case (elemId, _) => elemId }

                            if (noExElems.nonEmpty)
                                warns += s"Elements do not have *single word* synonyms in their @NCIntentSample - " +
                                    s"no suggestion can be made: ${noExElems.mkString(", ")}"

                            val allReqsCnt = allReqs.map(_._2.size).sum
                            val allSynsCnt = elemSyns.map(_._2.size).sum

                            logger.trace(s"Request is going to execute on 'ctxword' server [" +
                                s"exs=${exs.size}, " +
                                s"syns=$allSynsCnt, " +
                                s"reqs=$allReqsCnt" +
                            s"]")

                            if (allReqsCnt == 0)
                                onError(s"Suggestions cannot be generated for model: '$mdlId'")
                            else {
                                val allSgsts = new ConcurrentHashMap[String, util.List[NCWordSuggestion]]()
                                val cdl = new CountDownLatch(1)
                                val debugs = mutable.HashMap.empty[RequestData, Seq[NCWordSuggestion]]
                                val cnt = new AtomicInteger(0)

                                val cli = HttpClients.createDefault
                                val err = new AtomicReference[Throwable]()

                                for ((elemId, reqs) <- allReqs; batch <- reqs.sliding(BATCH_SIZE, BATCH_SIZE).map(_.toSeq)) {
                                    U.asFuture(
                                        _ => {
                                            val post = new HttpPost(url)

                                            post.setHeader("Content-Type", "application/json")
                                            post.setEntity(
                                                new StringEntity(
                                                    GSON.toJson(
                                                        RestRequest(
                                                            sentences = batch.map(p => RestRequestSentence(p.sentence, p.index)).asJava,
                                                            minScore = 0,
                                                            limit = MAX_LIMIT
                                                        )
                                                    ),
                                                    "UTF-8"
                                                )
                                            )

                                            val resps = request(cli, post)

                                            require(batch.size == resps.size, s"Batch: ${batch.size}, responses: ${resps.size}")

                                            batch.zip(resps).foreach { case (req, resp) => debugs += req -> resp }

                                            val i = cnt.addAndGet(batch.size)

                                            logger.debug(s"Executed: $i requests...")

                                            allSgsts.
                                                computeIfAbsent(elemId, (_: String) => new CopyOnWriteArrayList[NCWordSuggestion]()).
                                                addAll(resps.flatten.asJava)

                                            if (i == allReqsCnt)
                                                cdl.countDown()
                                        },
                                        (e: Throwable) => {
                                            err.compareAndSet(null, e)

                                            cdl.countDown()
                                        },
                                        (_: Unit) => ()
                                    )
                                }

                                cdl.await(Long.MaxValue, TimeUnit.MILLISECONDS)

                                if (err.get() != null)
                                    throw new NCE("Error while working with 'ctxword' server.", err.get())

                                val allSynsStems = elemSyns.flatMap(_._2).toSeq.flatten.map(_.stem).toSet

                                val nonEmptySgsts = allSgsts.asScala.map(p => p._1 -> p._2.asScala).filter(_._2.nonEmpty)

                                val res = mutable.HashMap.empty[String, mutable.ArrayBuffer[SuggestionResult]]

                                nonEmptySgsts.foreach { case (elemId, elemSgsts) =>
                                    elemSgsts.
                                        map(sgst => (sgst, toStem(sgst.word))).
                                        groupBy { case (_, stem) => stem }.
                                        // Drops already defined.
                                        filter { case (stem, _) => !allSynsStems.contains(stem) }.
                                        map { case (_, group) =>
                                            val seq = group.map { case (sgst, _) => sgst }.sortBy(-_.score)

                                            // Drops repeated.
                                            (seq.head.word, seq.length, seq.map(_.score).sum / seq.size)
                                        }.
                                        toSeq.
                                        map { case (sgst, cnt, score) => (sgst, cnt, score * cnt / elemSgsts.size) }.
                                        sortBy { case (_, _, sumFactor) => -sumFactor }.
                                        zipWithIndex.
                                        foreach { case ((word, _, sumFactor), _) =>
                                            val seq =
                                                res.get(elemId) match {
                                                    case Some(seq) => seq
                                                    case None =>
                                                        val buf = mutable.ArrayBuffer.empty[SuggestionResult]

                                                        res += elemId -> buf

                                                        buf
                                                }

                                            seq += SuggestionResult(word, sumFactor)
                                        }
                                }

                                val resJ: util.Map[String, util.List[util.HashMap[String, Any]]] =
                                    res.map {
                                        case (id, data) =>
                                            val norm =
                                                if (data.nonEmpty) {
                                                    val factors = data.map(_.score)

                                                    val min = factors.min
                                                    val max = factors.max
                                                    var delta = max - min

                                                    if (delta == 0)
                                                        delta = max

                                                    def normalize(v: Double): Double = (v - min) / delta

                                                    data.
                                                        map(s => SuggestionResult(s.synonym, normalize(s.score))).
                                                        filter(_.score >= minScore)
                                                }
                                                else
                                                    Seq.empty

                                            id -> norm.map(d => {
                                                val m = new util.HashMap[String, Any]()

                                                m.put("synonym", d.synonym.toLowerCase)
                                                m.put("score", d.score)

                                                m
                                            }).asJava
                                    }.toMap.asJava

                                promise.success(
                                    NCSuggestSynonymResult(
                                        modelId = mdlId,
                                        minScore = minScore,
                                        durationMs = U.now() - now,
                                        timestamp = now,
                                        error = null,
                                        suggestions = Seq(resJ.asInstanceOf[AnyRef]).asJava,
                                        warnings = warns.asJava
                                    )
                                )
                            }
                        }
                    }
                    catch {
                        case e: NCE => promise.failure(e)
                        case e: Throwable =>
                            U.prettyError(logger, "Unexpected error:", e)

                            promise.failure(e)
                    }
                case Failure(e) => promise.failure(e)
            }

            promise.future
        }

    /**
      *
      * @param reqs
      * @param minScoreOpt
      * @param parent
      * @return
      */
    def suggestWords(reqs: Seq[NCSuggestionRequest], minScoreOpt: Option[Double] = None, parent: Span = null):
        Future[Map[NCSuggestionRequest, Seq[NCWordSuggestion]]] =
        startScopedSpan("suggestWords", parent) { _ =>
            val promise = Promise[Map[NCSuggestionRequest, Seq[NCWordSuggestion]]]()

            case class Result(request: NCSuggestionRequest, suggestions: Seq[NCWordSuggestion])

            val data = new CopyOnWriteArrayList[Result]()
            val cli = HttpClients.createDefault
            val batches = reqs.sliding(BATCH_SIZE, BATCH_SIZE).map(_.toSeq).toSeq
            val cnt = new AtomicInteger(0)

            for (batch <- batches)
                U.asFuture(
                    _ => {
                        val post = new HttpPost(mkUrl)

                        post.setHeader("Content-Type", "application/json")
                        post.setEntity(
                            new StringEntity(
                                GSON.toJson(
                                    RestRequest(
                                        sentences = batch.map(p => RestRequestSentence(p.words.mkString(" "), p.index)).asJava,
                                        minScore = 0,
                                        limit = MAX_LIMIT
                                    )
                                ),
                                "UTF-8"
                            )
                        )

                        val resps = request(cli, post)

                        require(batch.size == resps.size, s"Batch: ${batch.size}, responses: ${resps.size}")

                        data.addAll(batch.zip(resps).map { case (req, resp) => Result(req, resp) }.asJava )

                        if (cnt.incrementAndGet() == batches.size) {
                            val min = minScoreOpt.getOrElse(DFLT_MIN_SCORE)

                            val map: Map[NCSuggestionRequest, Seq[NCWordSuggestion]] =
                                data.asScala.groupBy(_.request).map {
                                    case (req, ress) =>
                                        req -> ress.flatMap(_.suggestions.filter(_.score >= min).toSeq).sortBy(-_.score)
                            }

                            // TODO ? logic?
//                            val map: Map[NCSuggestionRequest, Seq[NCWordSuggestion]] = data.asScala.groupBy(_.request).map(p =>
//                                p._1 ->
//                                p._2.
//                                    map(_.suggestions.map(p => (toStem(p.word), p.score))).
//                                    map(_.groupBy(_._1)).
//                                    flatMap(p => p.map(p => p._1 -> p._1 -> p._2.map(_._2).sum / p._2.size).
//                                    filter(_._2 >= min).
//                                    map(p => NCWordSuggestion(p._1._2, p._2)).toSeq).toSeq)

                            promise.success(map)
                        }
                        ()
                    },
                    (e: Throwable) => {
                        U.prettyError(logger, "Unexpected error:", e)

                        promise.failure(e)

                    },
                    (_: Unit) => ()
                )

            promise.future
        }

    /**
     *
     * @param parent Optional parent span.
     * @return
     */
    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()
        ackStarted()
    }

    /**
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()
        ackStopped()
    }
}