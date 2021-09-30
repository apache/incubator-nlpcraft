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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.function

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote, NCNlpSentenceToken}
import org.apache.nlpcraft.probe.mgrs.NCProbeModel
import org.apache.nlpcraft.probe.mgrs.nlp.NCProbeEnricher

import java.util.Collections
import scala.jdk.CollectionConverters.{MapHasAsScala, SetHasAsScala}

/**
  *
  */
object NCFunctionEnricher extends NCProbeEnricher {
    private final val TOK_ID = "nlpcraft:function"

    private case class SingeFunc(name: String, synonyms: Seq[String])

    private object SingeFunc {
        def apply(name: String, syns:String*): SingeFunc = SingeFunc(name, syns)
    }

    private final val FUNC_NUM_SINGLE =
        Set(
            SingeFunc("sin", "sine"),
            SingeFunc("cos", "cosine"),
            SingeFunc("tan", "tangent"),
            SingeFunc("cot", "cotangent"),
            SingeFunc("round"),
            SingeFunc("floor"),
            SingeFunc("max", "maximum"),
            SingeFunc("min", "minimum"),
            SingeFunc("avg", "average"),
            SingeFunc("sum", "summary")
        )

    @volatile private var funcNumSingleData: Map[String, String] = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        funcNumSingleData =
            FUNC_NUM_SINGLE.flatMap(p => (p.synonyms :+ p.name).toSet.map(NCNlpCoreManager.stem).map(_ -> p.name).toMap).toMap

        ackStarted()
    }

    /**
      *
      * @param parent Optional parent span.
      */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        funcNumSingleData = null

        ackStopped()
    }

    override def enrich(mdl: NCProbeModel, ns: NCNlpSentence, senMeta: Map[String, Serializable], parent: Span): Unit = {
        require(isStarted)

        val restricted =
            mdl.model.getRestrictedCombinations.asScala.getOrElse(TOK_ID, java.util.Collections.emptySet()).
                asScala

        startScopedSpan(
            "enrich", parent, "srvReqId" -> ns.srvReqId, "mdlId" -> mdl.model.getId, "txt" -> ns.text
        ) { _ =>
            val buf = collection.mutable.ArrayBuffer.empty[Seq[NCNlpSentenceToken]]

            for (toks <- ns.tokenMixWithStopWords() if toks.size > 1 && !buf.exists(_.containsSlice(toks))) {
                funcNumSingleData.get(toks.head.stem) match {
                    case Some(f) =>
                        val users = toks.tail.filter(_.isUser)

                        if (users.size == 1 && toks.tail.forall(t => users.contains(t) || t.isStopWord)) {
                            for (typ <- users.head.filter(_.isUser).map(_.noteType) if !restricted.contains(typ))
                                toks.head.add(
                                    NCNlpSentenceNote(
                                        Seq(toks.head.index),
                                        TOK_ID,
                                        "type" -> f,
                                        "indexes" -> Collections.singleton(users.head.index),
                                        "note" -> typ
                                    )
                                )
                        }

                    case None => // No-op.
                }
            }
        }
    }
}
