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

package org.apache.nlpcraft.server.nlp.core.spacy

import java.net.URLEncoder
import java.util
import java.util.concurrent.TimeUnit._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import io.opencensus.trace.Span
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote}
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.server.ignite.NCIgniteInstance
import org.apache.nlpcraft.server.nlp.core.NCNlpNerEnricher
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, ExecutionContextExecutor, TimeoutException}

/**
  * spaCy REST proxy server NER enricher.
  */
object NCSpaCyNerEnricher extends NCService with NCNlpNerEnricher with NCIgniteInstance {
    private final val TIMEOUT_SECS: Int = 5

    private implicit val actSys: ActorSystem = ActorSystem()
    private implicit val materializer: ActorMaterializer = ActorMaterializer()
    private implicit val execCtx: ExecutionContextExecutor = actSys.dispatcher
    private implicit val fmt: RootJsonFormat[SpacySpan] = jsonFormat7(SpacySpan)
    
    private object Config extends NCConfigurable {
        def proxyUrl = getStringOrElse("nlpcraft.server.spacy.proxy.url", "http://localhost:5002")
    }

    // NOTE: property 'vector' represented as string because Python JSON serialization requirements.
    case class SpacySpan(
        text: String,
        from: Int,
        to: Int,
        ner: String,
        vector: String,
        sentiment: String,
        meta: Map[String, String]
    )

    @volatile private var url: String = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span ⇒
        url = Config.proxyUrl

        if (url.last == '/')
            url = url.dropRight(1)

        if (!url.endsWith("/spacy"))
            url = s"$url/spacy"
        
        addTags(
            span,
            "spacyProxyUrl" → url
        )

        // Tries to access spaCy proxy server.
        val status =
            try
                getSync(Http().singleRequest(HttpRequest(uri = s"$url?text=Hi"))).status
            catch {
                case e: Exception ⇒ throw new NCE(s"Failed to connect to spaCy proxy at $url.", e)
            }

        if (status != OK)
            throw new NCE(s"spaCy proxy unexpected response status: $status")

        logger.info(s"spaCy proxy connected: $url")

        super.start()
    }
    
    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
    
    /**
      *
      * @param ns
      * @param enabledBuiltInToks Set of enabled built-in token IDs.
      */
    override def enrich(ns: NCNlpSentence, enabledBuiltInToks: Set[String], parent: Span = null): Unit =
        startScopedSpan("enrich", parent, "srvReqId" → ns.srvReqId, "txt" → ns.text) { _ ⇒
            val resp = getSync(Http().singleRequest(HttpRequest(uri = s"$url?text=${URLEncoder.encode(ns.text, "UTF-8")}")))
    
            val status = resp.status
    
            status match {
                case OK ⇒
                    val resType = resp.entity.contentType
    
                    resType match {
                        case ContentTypes.`application/json` ⇒
                            val spans = getSync(Unmarshal(resp.entity).to[List[SpacySpan]])
    
                            spans.foreach(span ⇒ {
                                val nerLc = span.ner.toLowerCase
    
                                if (enabledBuiltInToks.contains(nerLc)) {
                                    val t1Opt = ns.find(_.startCharIndex == span.from)
                                    val t2Opt = ns.find(_.endCharIndex == span.from + span.text.length)
    
                                    if (t1Opt.nonEmpty && t2Opt.nonEmpty) {
                                        val vector = extractDouble(span, span.vector, "vector")
                                        val sentiment = extractDouble(span, span.sentiment, "sentiment")
                                        val from = t1Opt.get.startCharIndex
                                        val to = t2Opt.get.endCharIndex
    
                                        val meta = new util.HashMap[String, String]()
    
                                        span.meta.foreach { case (k, v) ⇒ meta.put(k, v) }
    
                                        val toks = ns.filter(t ⇒ t.startCharIndex >= from && t.endCharIndex <= to)
    
                                        val note = NCNlpSentenceNote(
                                            toks.map(_.index),
                                            s"spacy:$nerLc",
                                            "vector" → vector,
                                            "sentiment" → sentiment,
                                            "meta" → meta
                                        )
    
                                        toks.foreach(_.add(note))
                                    }
                                }
                            })
                        case _ ⇒ throw new NCE(s"spaCy proxy unexpected response type: $resType")
                    }
                    
                case _ ⇒ throw new NCE(s"spaCy proxy unexpected response status: $status")
            }
        }

    /**
      *
      * @param span
      * @param v
      * @param name
      */
    @throws[NCE]
    private def extractDouble(span: SpacySpan, v: String, name: String): Double =
        try
            v.toDouble
        catch {
            case e: NumberFormatException ⇒ throw new NCE(s"Invalid spaCy '$name' value: $span", e)
        }

    /**
      *
      * @param a
      */
    @throws[NCE]
    private def getSync[T](a : Awaitable[T]): T =
        try
            Await.result(a, Duration(TIMEOUT_SECS, SECONDS))
        catch {
            case _: TimeoutException ⇒ throw new NCE("spaCy proxy operation timed out.")
        }
}
