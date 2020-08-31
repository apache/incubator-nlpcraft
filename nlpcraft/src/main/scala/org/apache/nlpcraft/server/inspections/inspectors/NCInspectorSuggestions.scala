/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.server.inspections.inspectors

import java.util
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import java.util.concurrent.{ConcurrentHashMap, CopyOnWriteArrayList, CountDownLatch, ExecutorService, Executors, TimeUnit}

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.opencensus.trace.Span
import org.apache.http.HttpResponse
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.inspections.NCInspectionResult
import org.apache.nlpcraft.common.inspections.impl.NCInspectionResultImpl
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp.core.NCNlpPorterStemmer
import org.apache.nlpcraft.common.util.NCUtils
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.server.probe.NCProbeManager

import scala.collection.JavaConverters._
import scala.collection.{Seq, mutable}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future, Promise}

// TODO: Possible parameter 'minScore' (double 0 .. 1)
object NCInspectorSuggestions extends NCService with NCInspector {
    // For context word server requests.
    private final val MAX_LIMIT: Int = 10000
    private final val BATCH_SIZE = 20
    private final val DFLT_MIN_SCORE = 0.0

    // For warnings.
    private final val MIN_CNT_INTENT = 5
    private final val MIN_CNT_MODEL = 20

    private final val GSON = new Gson
    private final val TYPE_RESP = new TypeToken[util.List[util.List[Suggestion]]]() {}.getType
    private final val TYPE_ARGS = new TypeToken[util.HashMap[String, AnyRef]]() {}.getType
    private final val SEPARATORS = Seq('?', ',', '.', '-', '!')

    private object Config extends NCConfigurable {
        val urlOpt: Option[String] = getStringOpt("nlpcraft.server.ctxword.url")
    }

    @volatile private var pool: ExecutorService = _
    @volatile private var executor: ExecutionContextExecutor = _

    private final val HANDLER: ResponseHandler[Seq[Seq[Suggestion]]] =
        (resp: HttpResponse) ⇒ {
            val code = resp.getStatusLine.getStatusCode
            val e = resp.getEntity

            val js = if (e != null) EntityUtils.toString(e) else null

            if (js == null)
                throw new RuntimeException(s"Unexpected empty response [code=$code]")

            code match {
                case 200 ⇒
                    val data: util.List[util.List[Suggestion]] = GSON.fromJson(js, TYPE_RESP)

                    data.asScala.map(p ⇒ if (p.isEmpty) Seq.empty else p.asScala.tail)

                case 400 ⇒ throw new RuntimeException(js)
                case _ ⇒ throw new RuntimeException(s"Unexpected response [code=$code, response=$js]")
            }
        }

    case class Suggestion(word: String, score: Double)
    case class RequestData(sentence: String, example: String, elementId: String, index: Int)
    case class RestRequestSentence(text: String, indexes: util.List[Int])
    case class RestRequest(sentences: util.List[RestRequestSentence], limit: Int, min_score: Double)
    case class Word(word: String, stem: String) {
        require(!word.contains(" "), s"Word cannot contains spaces: $word")
        require(
            word.forall(ch ⇒
                ch.isLetterOrDigit ||
                    ch == '\'' ||
                    SEPARATORS.contains(ch)
            ),
            s"Unsupported symbols: $word"
        )
    }

    // TODO:
    case class SuggestionResult(
        synonym: String,
        ctxWorldServerScore: Double,
        suggestedCount: Int
    )

    private def split(s: String): Seq[String] = s.split(" ").toSeq.map(_.trim).filter(_.nonEmpty)
    private def toStem(s: String): String = split(s).map(NCNlpPorterStemmer.stem).mkString(" ")
    private def toStemWord(s: String): String = NCNlpPorterStemmer.stem(s)

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

    override def inspect(mdlId: String, inspId: String, args: Option[String], parent: Span = null): Future[NCInspectionResult] =
        startScopedSpan("inspect", parent, "modelId" → mdlId) { _ ⇒
            val now = System.currentTimeMillis()

            val promise = Promise[NCInspectionResult]()

            NCProbeManager.getModelInfo(mdlId, parent).collect {
                case m ⇒
                    try {
                        require(
                            m.containsKey("macros") && m.containsKey("elementsSynonyms") && m.containsKey("intentsSamples")
                        )

                        val macros = m.get("macros").
                            asInstanceOf[util.Map[String, String]].asScala
                        val elementsSynonyms = m.get("elementsSynonyms").
                            asInstanceOf[util.Map[String, util.List[String]]].asScala.map(p ⇒ p._1 → p._2.asScala)
                        val intentsSamples = m.get("intentsSamples").
                            asInstanceOf[util.Map[String, util.List[String]]].asScala.map(p ⇒ p._1 → p._2.asScala)

                        val minScore =
                            args match {
                                case Some(a) ⇒
                                    val v =
                                        try {
                                            val m: util.Map[String, AnyRef] = GSON.fromJson(a, TYPE_ARGS)

                                            val v = m.get("minScore")

                                            if (v == null)
                                                throw new NCE("Missed parameter: 'minScore'")

                                            v.asInstanceOf[Double]
                                        }
                                        catch {
                                            case e: Exception ⇒ throw new NCE("Invalid 'minScore' parameter", e)
                                        }

                                    if (v < 0 || v > 1)
                                        throw new NCE("'minScore' parameter value must be between 0 and 1")

                                    v
                                case None ⇒ DFLT_MIN_SCORE
                            }

                        def onError(err: String): Unit =
                            promise.success(
                                NCInspectionResultImpl(
                                    inspectionId = inspId,
                                    modelId = mdlId,
                                    inspectionArguments = None,
                                    durationMs = System.currentTimeMillis() - now,
                                    timestamp = now,
                                    errors = Seq(err),
                                    warnings = Seq.empty,
                                    suggestions = Seq.empty
                                )
                            )

                        if (intentsSamples.isEmpty)
                            onError(s"Missed intents samples for: '$mdlId'")
                        else {
                            val url = s"${Config.urlOpt.getOrElse(throw new NCE("Context word server is not configured"))}/suggestions"

                            val allSamplesCnt = intentsSamples.map { case (_, samples) ⇒ samples.size }.sum

                            val warns = mutable.ArrayBuffer.empty[String]

                            if (allSamplesCnt < MIN_CNT_MODEL)
                                warns +=
                                    s"Model: '$mdlId' has too small intents samples count: $allSamplesCnt. " +
                                        s"Potentially is can be not enough for suggestions service high quality work. " +
                                        s"Try to increase their count at least to $MIN_CNT_MODEL."

                            else {
                                val ids =
                                    intentsSamples.
                                        filter { case (_, samples) ⇒ samples.size < MIN_CNT_INTENT }.
                                        map { case (intentId, _) ⇒ intentId }

                                if (ids.nonEmpty)
                                    warns +=
                                        s"Models '$mdlId' has intents: [${ids.mkString(", ")}] with too small intents samples count." +
                                            s"Potentially it can be not enough for suggestions service high quality work. " +
                                            s"Try to increase their count at least to $MIN_CNT_INTENT."
                            }

                            val parser = new NCMacroParser()

                            macros.foreach { case (name, str) ⇒ parser.addMacro(name, str) }

                            // Note that we don't use system tokenizer, because ContextWordServer doesn't have this tokenizer.
                            // We just split examples words with spaces. Also we divide SEPARATORS as separated words.
                            val examples =
                            intentsSamples.
                                flatMap { case (_, samples) ⇒ samples }.
                                map(ex ⇒ SEPARATORS.foldLeft(ex)((s, ch) ⇒ s.replaceAll(s"\\$ch", s" $ch "))).
                                map(ex ⇒ {
                                    val seq = ex.split(" ")

                                    seq → seq.map(toStemWord)
                                }).
                                toMap

                            val elemSyns =
                                elementsSynonyms.map { case (elemId, syns) ⇒ elemId → syns.flatMap(parser.expand) }.
                                    map { case (id, seq) ⇒ id → seq.map(txt ⇒ split(txt).map(p ⇒ Word(p, toStemWord(p)))) }

                            val allReqs =
                                elemSyns.map {
                                    case (elemId, syns) ⇒
                                        val normSyns: Seq[Seq[Word]] = syns.filter(_.size == 1)
                                        val synsStems = normSyns.map(_.map(_.stem))
                                        val synsWords = normSyns.map(_.map(_.word))

                                        val reqs =
                                            examples.flatMap { case (exampleWords, exampleStems) ⇒
                                                val exampleIdxs = synsStems.flatMap(synStems ⇒ getAllSlices(exampleStems, synStems))

                                                def mkRequestData(idx: Int, synStems: Seq[String], synStemsIdx: Int): RequestData = {
                                                    val fromIncl = idx
                                                    val toExcl = idx + synStems.length

                                                    RequestData(
                                                        sentence = exampleWords.zipWithIndex.flatMap {
                                                            case (exampleWord, i) ⇒
                                                                i match {
                                                                    case x if x == fromIncl ⇒ synsWords(synStemsIdx)
                                                                    case x if x > fromIncl && x < toExcl ⇒ Seq.empty
                                                                    case _ ⇒ Seq(exampleWord)
                                                                }
                                                        }.mkString(" "),
                                                        example = exampleWords.mkString(" "),
                                                        elementId = elemId,
                                                        index = idx
                                                    )
                                                }

                                                (for (idx ← exampleIdxs; (synStems, i) ← synsStems.zipWithIndex)
                                                    yield mkRequestData(idx, synStems, i)).distinct
                                            }

                                        elemId → reqs.toSet
                                }.filter(_._2.nonEmpty)

                            val noExElems =
                                elementsSynonyms.
                                    filter { case (elemId, syns) ⇒ syns.nonEmpty && !allReqs.contains(elemId) }.
                                    map { case (elemId, _) ⇒ elemId }

                            if (noExElems.nonEmpty)
                                warns +=
                                    "Some elements don't have synonyms in their intent samples, " +
                                        s"so the service can't suggest any new synonyms for such elements: [${noExElems.mkString(", ")}]"

                            val allReqsCnt = allReqs.map(_._2.size).sum
                            val allSynsCnt = elemSyns.map(_._2.size).sum

                            logger.info(s"Data prepared. Request is going to execute on ContextWord Server " +
                                s"[examples=${examples.size}, " +
                                s"synonyms=$allSynsCnt, " +
                                s"requests=$allReqsCnt]"
                            )

                            if (allReqsCnt == 0)
                                onError(s"Suggestions cannot be prepared: '$mdlId'. Samples don't contain synonyms")
                            else {
                                val allSuggs = new ConcurrentHashMap[String, util.List[Suggestion]]()
                                val cdl = new CountDownLatch(1)
                                val debugs = mutable.HashMap.empty[RequestData, Seq[Suggestion]]
                                val cnt = new AtomicInteger(0)

                                val client = HttpClients.createDefault
                                val err = new AtomicReference[Throwable]()

                                for ((elemId, reqs) ← allReqs; batch ← reqs.sliding(BATCH_SIZE, BATCH_SIZE).map(_.toSeq)) {
                                    NCUtils.asFuture(
                                        _ ⇒ {
                                            val post = new HttpPost(url)

                                            post.setHeader("Content-Type", "application/json")

                                            post.setEntity(
                                                new StringEntity(
                                                    GSON.toJson(
                                                        RestRequest(
                                                            sentences = batch.map(p ⇒ RestRequestSentence(p.sentence, Seq(p.index).asJava)).asJava,
                                                            // ContextWord server range is (0, 2), input range is (0, 1)
                                                            min_score = minScore * 2,
                                                            // We set big limit value and in fact only minimal score is taken into account.
                                                            limit = MAX_LIMIT
                                                        )
                                                    ),
                                                    "UTF-8"
                                                )
                                            )

                                            val resps: Seq[Seq[Suggestion]] =
                                                try
                                                    client.execute(post, HANDLER)
                                                finally
                                                    post.releaseConnection()

                                            require(batch.size == resps.size, s"Batch: ${batch.size}, responses: ${resps.size}")

                                            batch.zip(resps).foreach { case (req, resp) ⇒ debugs += req → resp }

                                            val i = cnt.addAndGet(batch.size)

                                            logger.debug(s"Executed: $i requests...")

                                            allSuggs.
                                                computeIfAbsent(elemId, (_: String) ⇒ new CopyOnWriteArrayList[Suggestion]()).
                                                addAll(resps.flatten.asJava)

                                            if (i == allReqsCnt)
                                                cdl.countDown()
                                        },
                                        (e: Throwable) ⇒ {
                                            err.compareAndSet(null, e)

                                            cdl.countDown()
                                        },
                                        (_: Unit) ⇒ ()
                                    )
                                }

                                cdl.await(Long.MaxValue, TimeUnit.MILLISECONDS)

                                if (err.get() != null)
                                    throw new NCE("Error during work with ContextWordServer", err.get())

                                val allSynsStems = elemSyns.flatMap(_._2).flatten.map(_.stem).toSet

                                val nonEmptySuggs = allSuggs.asScala.map(p ⇒ p._1 → p._2.asScala).filter(_._2.nonEmpty)

                                val res = mutable.HashMap.empty[String, mutable.ArrayBuffer[SuggestionResult]]

                                nonEmptySuggs.
                                    foreach { case (elemId, elemSuggs) ⇒
                                        elemSuggs.
                                            map(sugg ⇒ (sugg, toStem(sugg.word))).
                                            groupBy { case (_, stem) ⇒ stem }.
                                            // Drops already defined.
                                            filter { case (stem, _) ⇒ !allSynsStems.contains(stem) }.
                                            map { case (_, group) ⇒
                                                val seq = group.map { case (sugg, _) ⇒ sugg }.sortBy(-_.score)

                                                // Drops repeated.
                                                (seq.head, seq.length)
                                            }.
                                            toSeq.
                                            map { case (sugg, cnt) ⇒ (sugg, cnt, sugg.score * cnt / elemSuggs.size) }.
                                            sortBy { case (_, _, sumFactor) ⇒ -sumFactor }.
                                            zipWithIndex.
                                            foreach { case ((sugg, cnt, _), _) ⇒
                                                val seq =
                                                    res.get(elemId) match {
                                                        case Some(seq) ⇒ seq
                                                        case None ⇒
                                                            val buf = mutable.ArrayBuffer.empty[SuggestionResult]

                                                            res += elemId → buf

                                                            buf
                                                    }

                                                seq += SuggestionResult(sugg.word, sugg.score, cnt)
                                            }
                                    }

                                logger.whenDebugEnabled({
                                    logger.debug("Request information:")

                                    var i = 1

                                    debugs.groupBy(_._1.example).foreach { case (_, m) ⇒
                                        m.toSeq.sortBy(_._1.sentence).foreach { case (req, suggs) ⇒
                                            val s =
                                                split(req.sentence).
                                                    zipWithIndex.map { case (w, i) ⇒ if (i == req.index) s"<<<$w>>>" else w }.
                                                    mkString(" ")

                                            logger.debug(
                                                s"$i. " +
                                                    s"Request=$s, " +
                                                    s"suggestions=[${suggs.map(_.word).mkString(", ")}], " +
                                                    s"element=${req.elementId}"
                                            )

                                            i = i + 1
                                        }
                                    }
                                })

                                val resJ: util.Map[String, util.List[util.HashMap[String, Any]]] =
                                    res.map { case (id, data) ⇒
                                        id → data.map(d ⇒ {
                                            val m = new util.HashMap[String, Any]()

                                            m.put("synonym", d.synonym)
                                            // ContextWord server range is (0, 2)
                                            m.put("ctxWorldServerScore", d.ctxWorldServerScore / 2)
                                            m.put("suggestedCount", d.suggestedCount)

                                            m
                                        }).asJava
                                    }.asJava

                                promise.success(
                                    NCInspectionResultImpl(
                                        inspectionId = inspId,
                                        modelId = mdlId,
                                        inspectionArguments = None,
                                        durationMs = System.currentTimeMillis() - now,
                                        timestamp = now,
                                        errors = Seq.empty,
                                        warnings = warns,
                                        suggestions = Seq(resJ)
                                    )
                                )
                            }
                    }
                }
                catch {
                    case e: NCE ⇒ promise.failure(e)
                    case e: Throwable ⇒
                        logger.warn("Unexpected error.", e)

                        promise.failure(e)
                }
                case e: Throwable ⇒
                    logger.warn(s"Error getting model information: $mdlId", e)

                    promise.failure(e)

            }(executor)

            promise.future
        }

    override def start(parent: Span): NCService =
        startScopedSpan("start", parent) { _ ⇒
            pool = Executors.newCachedThreadPool()
            executor = ExecutionContext.fromExecutor(pool)

            super.start(parent)
        }

    override def stop(parent: Span): Unit =
        startScopedSpan("stop", parent) { _ ⇒
            super.stop(parent)

            NCUtils.shutdownPools(pool)
            executor = null
        }
}
