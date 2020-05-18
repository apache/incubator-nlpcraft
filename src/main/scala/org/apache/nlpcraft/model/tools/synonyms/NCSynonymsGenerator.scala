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
package org.apache.nlpcraft.model.tools.synonyms

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CopyOnWriteArrayList, CountDownLatch, TimeUnit}

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.http.HttpResponse
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp.core.NCNlpPorterStemmer
import org.apache.nlpcraft.common.util.NCUtils
import org.apache.nlpcraft.model.NCModelFileAdapter

import scala.collection.JavaConverters._
import scala.collection._

case class NCSynonymsGeneratorData(
      url: String = "http://localhost:5000",
      modelPath: String,
      minScore: Double = 0,
      supportMultiple: Boolean = false,
      debugRequests: Boolean = false
)

object NCSynonymsGenerator {
    /**
      * Suggestion data holder.
      *
      * @param word Word
      * @param bert Bert factor.
      * @param normalized Normalized bert factor.
      * @param ftext FText factor.
      * @param `ftext-sentence` TODO:
      * @param score Calculated summary factor: normalized * weight1 + ftext * weight2 (weights values are 1 currently)
      */
    case class Suggestion(
        word: String, bert: Double, normalized: Double, ftext: Double, `ftext-sentence`: Double, score: Double
    )
    case class RequestData(sentence: String, example: String, elementId: String, lower: Int, upper: Int)
    case class RestRequest(sentence: String, simple: Boolean, lower: Int, upper: Int)
    case class RestResponse(data: java.util.ArrayList[Suggestion])

    private final val GSON = new Gson
    private final val TYPE_RESP = new TypeToken[RestResponse]() {}.getType
    private final val SEPARATORS = Seq('?', ',', '.', '-', '!')

    private def mkHandler(req: RequestData): ResponseHandler[Seq[Suggestion]] =
        (resp: HttpResponse) ⇒ {
            val code = resp.getStatusLine.getStatusCode
            val e = resp.getEntity

            val js = if (e != null) EntityUtils.toString(e) else null

            if (js == null)
                throw new RuntimeException(s"Unexpected empty response [req=$req, code=$code]")

            code match {
                case 200 ⇒
                    val data: RestResponse = GSON.fromJson(js, TYPE_RESP)

                    data.data.asScala

                case 400 ⇒ throw new RuntimeException(js)
                case _ ⇒ throw new RuntimeException(s"Unexpected response [req=$req, code=$code, response=$js]")
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

    def process(data: NCSynonymsGeneratorData): Unit = {
        val mdl = new NCModelFileAdapter(data.modelPath) {}

        val parser = new NCMacroParser()

        if (mdl.getMacros != null)
            mdl.getMacros.asScala.foreach { case (name, str) ⇒ parser.addMacro(name, str) }

        val client = HttpClients.createDefault

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

        val examples =
            mdl.getExamples.asScala.
                map(ex ⇒ SEPARATORS.foldLeft(ex)((s, ch) ⇒ s.replaceAll(s"\\$ch", s" $ch "))).
                map(ex ⇒ {
                    val seq = ex.split(" ")

                    seq → seq.map(toStemWord)
                }).
                toMap

        val elemSyns =
            mdl.getElements.asScala.map(e ⇒ e.getId → e.getSynonyms.asScala.flatMap(parser.expand)).
                map { case (id, seq) ⇒ id → seq.map(txt ⇒ split(txt).map(p ⇒ Word(p, toStemWord(p))))}.toMap

        val allReqs =
            elemSyns.map {
                case (elemId, syns) ⇒
                    val normSyns: Seq[Seq[Word]] =
                        if (data.supportMultiple) syns.filter(_.size <= 2) else syns.filter(_.size == 1)
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
                                    lower = idx,
                                    upper = idx + synStems.length - 1
                                )
                            }

                        (for (idx ← exampleIdxs; (synStems, i) ← synsStems.zipWithIndex) yield mkRequestData(idx, synStems, i)).
                            distinct
                    }

                    elemId → reqs.toSet
            }.filter(_._2.nonEmpty)

        println(s"Examples count: ${examples.size}")
        println(s"Synonyms count: ${elemSyns.map(_._2.size).sum}")
        println(s"Request prepared: ${allReqs.map(_._2.size).sum}")

        val allSuggs = new java.util.concurrent.ConcurrentHashMap[String, java.util.List[Suggestion]] ()
        val cdl = new CountDownLatch(allReqs.map { case (_, seq) ⇒ seq.size }.sum)
        val debugs = mutable.HashMap.empty[RequestData, Seq[Suggestion]]
        val cnt = new AtomicInteger(0)

        for ((elemId, reqs) ← allReqs; req ← reqs) {
            NCUtils.asFuture(
                _ ⇒ {
                    val post = new HttpPost(data.url)

                    post.setHeader("Content-Type", "application/json")
                    post.setEntity(
                        new StringEntity(
                            GSON.toJson(
                                RestRequest(
                                    sentence = req.sentence,
                                    simple = false,
                                    lower = req.lower,
                                    upper = req.upper
                                )
                            ),
                            "UTF-8"
                        )
                    )

                    val resp: Seq[Suggestion] =
                        try
                            client.execute(post, mkHandler(req))
                        finally
                            post.releaseConnection()

                    if (data.debugRequests)
                        debugs += req → resp

                    val i = cnt.incrementAndGet()

                    if (i % 10 == 0)
                        println(s"Executed: $i requests.")

                    allSuggs.
                        computeIfAbsent(elemId, (_: String) ⇒ new CopyOnWriteArrayList[Suggestion]()).
                        addAll(resp.asJava)
                },
                (e: Throwable) ⇒ {
                    e.printStackTrace()

                    cdl.countDown()
                },
                (_: Boolean) ⇒ cdl.countDown()
            )
        }

        cdl.await(Long.MaxValue, TimeUnit.MILLISECONDS)

        println("All requests executed.")

        val allSynsStems = elemSyns.flatMap(_._2).flatten.map(_.stem).toSet

        val filteredSuggs =
            allSuggs.asScala.map {
                case (elemId, elemSuggs) ⇒ elemId → elemSuggs.asScala.filter(_.score >= data.minScore)
            }.filter(_._2.nonEmpty)

        val avgScores = filteredSuggs.map { case (elemId, suggs) ⇒ elemId → (suggs.map(_.score).sum / suggs.size) }
        val counts = filteredSuggs.map { case (elemId, suggs) ⇒ elemId → suggs.size }

        val tbl = NCAsciiTable()

        val headers = Seq("Element", "Suggestion", "Summary factor", "Count", "Bert/Ftext score", "Bert", "Bert norm", "Ftext")

        tbl #= ((if (data.supportMultiple) headers ++ Seq("Ftext-Sentence") else headers) :_*)

        filteredSuggs.
            foreach { case (elemId, elemSuggs) ⇒
                val seq: Seq[(Suggestion, Int)] = elemSuggs.
                    map(sugg ⇒ (sugg, toStem(sugg.word))).
                    groupBy { case (_, stem) ⇒ stem }.
                    filter { case (stem, _) ⇒ !allSynsStems.contains(stem) }.
                    map { case (_, group) ⇒
                        val seq = group.map { case (sugg, _) ⇒ sugg }.sortBy(-_.score)

                        // Drops repeated.
                        (seq.head, seq.length)
                    }.
                    toSeq

                val normFactor = seq.map(_._2).sum.toDouble / seq.size  / avgScores(elemId)

                seq.
                    map { case (sugg, cnt) ⇒ (sugg, cnt, sugg.score * normFactor * cnt.toDouble / counts(elemId)) }.
                    sortBy { case (_, _, cumFactor) ⇒  -cumFactor }.
                    zipWithIndex.
                    foreach { case ((sugg, cnt, cumFactor), sugIdx) ⇒
                        def f(d: Double): String = "%1.3f" format d

                        val vals = Seq(
                            if (sugIdx == 0) elemId else " ",
                            sugg.word,
                            f(cumFactor),
                            cnt,
                            f(sugg.score),
                            f(sugg.bert),
                            f(sugg.normalized),
                            f(sugg.ftext)
                        )

                        tbl += ((if (data.supportMultiple) vals ++ Seq(f(sugg.`ftext-sentence`)) else vals)  :_*)
                    }
            }

        if (data.debugRequests) {
            var i = 1

            debugs.groupBy(_._1.example).foreach { case (_, m) ⇒
                m.toSeq.sortBy(_._1.sentence).foreach { case (req, suggs) ⇒
                    val s =
                        split(req.sentence).zipWithIndex.map { case (w, i) ⇒
                            i match {
                                case x if x == req.lower && x == req.upper ⇒ s"<<<$w>>>"
                                case x if x == req.lower ⇒ s"<<<$w"
                                case x if x == req.upper ⇒ s"$w>>>"
                                case _ ⇒ w
                            }
                        }.mkString(" ")

                    println(
                        s"$i. " +
                            s"Request=$s, " +
                            s"suggestions=[${suggs.map(_.word).mkString(", ")}], " +
                            s"element=${req.elementId}"
                    )

                    i = i + 1
                }
            }
        }

        println("Suggestions:")

        tbl.render()
    }
}

object NCSynonymsGeneratorRunner extends App {
    NCSynonymsGenerator.process(
        NCSynonymsGeneratorData(
            url = "http://localhost:5000",
            modelPath = "src/main/scala/org/apache/nlpcraft/examples/weather/weather_model.json",
            minScore = 0,
            supportMultiple = false, // TODO: change it to words count.
            debugRequests = true
        )
    )
}