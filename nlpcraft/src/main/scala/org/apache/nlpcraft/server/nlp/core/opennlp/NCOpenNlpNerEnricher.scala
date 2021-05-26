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

package org.apache.nlpcraft.server.nlp.core.opennlp

import io.opencensus.trace.Span
import opennlp.tools.namefind.{NameFinderME, TokenNameFinderModel}
import org.apache.ignite.IgniteCache
import org.apache.nlpcraft.common.extcfg.NCExternalConfigManager
import org.apache.nlpcraft.common.extcfg.NCExternalConfigType.OPENNLP
import org.apache.nlpcraft.common.nlp.core.opennlp.NCOpenNlpTokenizer
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote}
import org.apache.nlpcraft.common.pool.NCThreadPoolManager
import org.apache.nlpcraft.common.{NCService, U}
import org.apache.nlpcraft.server.ignite.NCIgniteHelpers._
import org.apache.nlpcraft.server.ignite.NCIgniteInstance
import org.apache.nlpcraft.server.nlp.core.NCNlpNerEnricher
import scala.util.Using

import scala.concurrent.ExecutionContext
import scala.util.control.Exception.catching

/**
  * OpenNLP NER enricher.
  */
object NCOpenNlpNerEnricher extends NCService with NCNlpNerEnricher with NCIgniteInstance {
    private implicit final val ec: ExecutionContext = NCThreadPoolManager.getSystemContext

    @volatile private var nerFinders: Map[NameFinderME, String] = _
    @volatile private var cache: IgniteCache[String, Array[String]] = _

    /**
     *
     * @param parent Optional parent span.
     * @return
     */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span =>
        ackStarting()

        require(NCOpenNlpTokenizer.isStarted)

        val m = collection.mutable.HashMap.empty[NameFinderME, String]

        def add(typ: String, res: String): Unit = {
            val f =
                Using.resource(NCExternalConfigManager.getStream(OPENNLP, res, span)) { in =>
                    new NameFinderME(new TokenNameFinderModel(in))
                }

            m.synchronized {
                m += f -> typ
            }
        }

        U.executeParallel(
            () => add("location", "en-ner-location.bin"),
            () => add("money", "en-ner-money.bin"),
            () => add("person", "en-ner-person.bin"),
            () => add("organization", "en-ner-organization.bin"),
            () => add("date", "en-ner-date.bin"),
            () => add("time", "en-ner-time.bin"),
            () => add("percentage", "en-ner-percentage.bin")
        )

        nerFinders = m.toMap

        catching(wrapIE) {
            cache = ignite.cache[String, Array[String]]("opennlp-cache")
        }

        ackStarted()
    }

    /**
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        cache = null
    
        ackStopped()
    }

    /**
     *
     * @param ns
     * @param ebiTokens
     * @param parent Optional parent span.
     */
    override def enrich(ns: NCNlpSentence, ebiTokens: Set[String], parent: Span = null): Unit =
        startScopedSpan("enrich", parent, "srvReqId" -> ns.srvReqId, "txt" -> ns.text) { _ =>
            val normTxt = ns.text
    
            val words =
                catching(wrapIE) {
                    cache(normTxt) match {
                        case Some(ws) => ws
                        case None =>
                            val words = this.synchronized {
                                NCOpenNlpTokenizer.tokenize(normTxt).toArray.map(_.token)
                            }
    
                            cache += normTxt -> words
    
                            words
                    }
                }
    
            case class Holder(start: Int, end: Int, name: String, probability: Double)
    
            val hs =
                this.
                    synchronized {
                        val res = nerFinders.
                            filter { case (_, tokName) => ebiTokens.contains(tokName)}.
                            flatMap {
                                case (finder, name) =>
                                    finder.find(words).map(p => Holder(p.getStart, p.getEnd - 1, name, p.getProb))
                            }
    
                            nerFinders.keys.foreach(_.clearAdaptiveData())
    
                            res
                    }.toSeq
    
            hs.
                filter(h => ebiTokens.contains(h.name)).
                foreach(h => {
                    val t1 = ns.find(_.index == h.start)
                    val t2 = ns.find(_.index == h.end)
    
                    if (t1.nonEmpty && t2.nonEmpty) {
                        val i1 = t1.get.index
                        val i2 = t2.get.index
    
                        val toks = ns.filter(t => t.index >= i1 && t.index <= i2)
    
                        val note = NCNlpSentenceNote(
                            toks.map(_.index),
                            s"opennlp:${h.name}",
                            "probability" -> h.probability
                        )
    
                        toks.foreach(_.add(note))
                    }
                })
        }
}
