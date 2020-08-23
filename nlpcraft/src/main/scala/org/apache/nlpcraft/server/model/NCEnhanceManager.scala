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

package org.apache.nlpcraft.server.model

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CopyOnWriteArrayList, CountDownLatch, TimeUnit}
import java.util.{List ⇒ JList}

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
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp.core.NCNlpPorterStemmer
import org.apache.nlpcraft.common.util.NCUtils
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.server.probe.NCProbeManager

import scala.collection.JavaConverters._
import scala.collection._

/**
  * TODO:
  */
object NCEnhanceManager extends NCService {
    // For context word server requests.
    private final val DFLT_LIMIT: Int = 20
    private final val MAX_LIMIT: Int = 10000
    private final val DFLT_MIN_SCORE: Double = 0
    private final val BATCH_SIZE = 20

    // For warnings.
    private final val MIN_CNT_INTENT = 5
    private final val MIN_CNT_MODEL = 20

    private object Config extends NCConfigurable {
        val urlOpt: Option[String] = getStringOpt("nlpcraft.server.ctxword.url")
    }

    case class Suggestion(word: String, score: Double)
    case class RequestData(sentence: String, example: String, elementId: String, index: Int)
    case class RestRequestSentence(text: String, indexes: JList[Int])
    case class RestRequest(sentences: JList[RestRequestSentence], limit: Int, min_score: Double)
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

    private final val GSON = new Gson
    private final val TYPE_RESP = new TypeToken[JList[JList[Suggestion]]]() {}.getType
    private final val SEPARATORS = Seq('?', ',', '.', '-', '!')

    private final val HANDLER: ResponseHandler[Seq[Seq[Suggestion]]] =
        (resp: HttpResponse) ⇒ {
            val code = resp.getStatusLine.getStatusCode
            val e = resp.getEntity

            val js = if (e != null) EntityUtils.toString(e) else null

            if (js == null)
                throw new RuntimeException(s"Unexpected empty response [code=$code]")

            code match {
                case 200 ⇒
                    val data: JList[JList[Suggestion]] = GSON.fromJson(js, TYPE_RESP)

                    data.asScala.map(p ⇒ if (p.isEmpty) Seq.empty else p.asScala.tail)

                case 400 ⇒ throw new RuntimeException(js)
                case _ ⇒ throw new RuntimeException(s"Unexpected response [code=$code, response=$js]")
            }
        }

    private def split(s: String): Seq[String] = s.split(" ").toSeq.map(_.trim).filter(_.nonEmpty)
    private def toStem(s: String): String = split(s).map(NCNlpPorterStemmer.stem).mkString(" ")
    private def toStemWord(s: String): String = NCNlpPorterStemmer.stem(s)

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
      * TODO:
      * @param mdlId Model ID.
      * @param minScore Context word server minimal suggestion score (default DFLT_MIN_SCORE).
      * Increase it for suggestions count increasing, decrease it to be more precise. Range 0 ... 1.
      *
      * @param parent Parent.
      */
    @throws[NCE]
    def enhance(mdlId: String, minScore: Option[Double], parent: Span = null): Map[String, Seq[NCEnhanceSuggestion]] =
        startScopedSpan(
            "suggest", parent, "modelId" → mdlId, "minScore" → minScore.getOrElse(() ⇒ null)
        ) { _ ⇒
            val minScoreVal = minScore.getOrElse(DFLT_MIN_SCORE)

            require(minScoreVal >= 0 && minScoreVal <= 1)

            val url = s"${Config.urlOpt.getOrElse(throw new NCE("Context word server is not configured"))}/suggestions"

            val mdl = NCProbeManager.getModel(mdlId)

            require(mdl.intentsSamples != null, "Samples cannot be null")
            require(mdl.elementsSynonyms != null, "Element synonyms cannot be null")
            require(mdl.macros != null, "Macros cannot be null")

            val allSamplesCnt = mdl.intentsSamples.map { case (_, samples) ⇒ samples.size }.sum

            if (allSamplesCnt < MIN_CNT_MODEL) {
                // TODO: text
                logger.warn(
                    s"Model: '$mdlId' has too small intents samples count: $allSamplesCnt. " +
                    s"Potentially is can be not enough for suggestions service high quality work. " +
                    s"Try to increase their count at least to $MIN_CNT_MODEL."
                )
            }
            else {
                val ids =
                    mdl.intentsSamples.
                        filter { case (_, samples) ⇒ samples.size < MIN_CNT_INTENT }.
                        map { case (intentId, _) ⇒ intentId }

                if (ids.nonEmpty)
                    // TODO: text
                    logger.warn(s"Models '$mdlId' has intents: [${ids.mkString(", ")}] with too small intents samples count." +
                        s"Potentially it can be not enough for suggestions service high quality work. " +
                        s"Try to increase their count at least to $MIN_CNT_INTENT."
                    )
            }

            val parser = new NCMacroParser()

            mdl.macros.foreach { case (name, str) ⇒ parser.addMacro(name, str) }

            // Note that we don't use system tokenizer, because ContextWordServer doesn't have this tokenizer.
            // We just split examples words with spaces. Also we divide SEPARATORS as separated words.
            val examples =
                mdl.
                    intentsSamples.
                    flatMap { case (_, samples) ⇒ samples }.
                    map(ex ⇒ SEPARATORS.foldLeft(ex)((s, ch) ⇒ s.replaceAll(s"\\$ch", s" $ch "))).
                    map(ex ⇒ {
                        val seq = ex.split(" ")

                        seq → seq.map(toStemWord)
                    }).
                    toMap

            val elemSyns =
                mdl.elementsSynonyms.map { case (elemId, syns) ⇒ elemId → syns.flatMap(parser.expand) }.
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
                mdl.elementsSynonyms.
                    filter { case (elemId, syns) ⇒ syns.nonEmpty && !allReqs.contains(elemId) }.
                    map { case (elemId, _) ⇒ elemId }

            if (noExElems.nonEmpty)
                // TODO: text
                logger.warn(
                    "Some elements don't have synonyms in intent samples, " +
                    s"so the service can't suggest any new synonyms for such elements: [${noExElems.mkString(", ")}]"
            )

            val allReqsCnt = allReqs.map(_._2.size).sum
            val allSynsCnt = elemSyns.map(_._2.size).sum

            logger.info(s"Data prepared [examples=${examples.size}, synonyms=$allSynsCnt, requests=$allReqsCnt]")

            val allSuggs = new java.util.concurrent.ConcurrentHashMap[String, JList[Suggestion]]()
            val cdl = new CountDownLatch(1)
            val debugs = mutable.HashMap.empty[RequestData, Seq[Suggestion]]
            val cnt = new AtomicInteger(0)

            val client = HttpClients.createDefault

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
                                        min_score = minScoreVal * 2,
                                        // If minScore defined, we set big limit value and in fact only minimal score
                                        // is taken into account. Otherwise - default value.
                                        limit = if (minScore.isDefined) MAX_LIMIT else DFLT_LIMIT
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

                        logger.info(s"Executed: $i requests...")

                        allSuggs.
                            computeIfAbsent(elemId, (_: String) ⇒ new CopyOnWriteArrayList[Suggestion]()).
                            addAll(resps.flatten.asJava)

                        if (i == allReqsCnt)
                            cdl.countDown()
                    },
                    (e: Throwable) ⇒ {
                        logger.error("Error execution request", e)

                        cdl.countDown()
                    },
                    (_: Unit) ⇒ ()
                )
            }

            cdl.await(Long.MaxValue, TimeUnit.MILLISECONDS)

            val allSynsStems = elemSyns.flatMap(_._2).flatten.map(_.stem).toSet

            val nonEmptySuggs = allSuggs.asScala.map(p ⇒ p._1 → p._2.asScala).filter(_._2.nonEmpty)

            val res = mutable.HashMap.empty[String, mutable.ArrayBuffer[NCEnhanceSuggestion]]

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
                        foreach { case ((sugg, cnt, sumFactor), _) ⇒
                            val seq =
                                res.get(elemId) match {
                                    case Some(seq) ⇒ seq
                                    case None ⇒
                                        val buf = mutable.ArrayBuffer.empty[NCEnhanceSuggestion]

                                        res += elemId → buf

                                        buf
                                }

                            seq += NCEnhanceSuggestion(sugg.word, sugg.score, cnt, sumFactor)
                        }
                }

            logger.whenInfoEnabled({
                var i = 1

                debugs.groupBy(_._1.example).foreach { case (_, m) ⇒
                    m.toSeq.sortBy(_._1.sentence).foreach { case (req, suggs) ⇒
                        val s =
                            split(req.sentence).
                                zipWithIndex.map { case (w, i) ⇒ if (i == req.index) s"<<<$w>>>" else w }.
                                mkString(" ")

                        logger.info(
                            s"$i. " +
                                s"Request=$s, " +
                                s"suggestions=[${suggs.map(_.word).mkString(", ")}], " +
                                s"element=${req.elementId}"
                        )

                        i = i + 1
                    }
                }
            })

            res
        }
}
