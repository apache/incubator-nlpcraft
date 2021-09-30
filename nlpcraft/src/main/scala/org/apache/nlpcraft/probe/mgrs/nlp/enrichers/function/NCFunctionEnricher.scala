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
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote, NCNlpSentenceToken}
import org.apache.nlpcraft.probe.mgrs.NCProbeModel
import org.apache.nlpcraft.probe.mgrs.nlp.NCProbeEnricher

import java.util.Collections
import scala.collection.mutable
import scala.jdk.CollectionConverters.{MapHasAsScala, SetHasAsScala}

/**
  *
  */
object NCFunctionEnricher extends NCProbeEnricher {
    private final val TOK_ID = "nlpcraft:function"

    private case class SingleFuncDef(name: String, synonyms: String*)

    private final val FUNC_NUM_SINGLE = {
        import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.function.NCFunctionEnricher.{SingleFuncDef => F}

        Set(
            F("sin", "sine"),
            F("cos", "cosine"),
            F("tan", "tangent"),
            F("cot", "cotangent"),

            F("round"),
            F("floor"),

            F("max", "{maximum|max} {of|_}"),
            F("min", "{minimum|min} {of|_}"),
            F("avg", "{average|avg} {of|_}"),
            F("sum", "{summary|sum} {of|_}"),
            F("count", "count {of|_}"),
            F("first", "first {of|_}"),
            F("last", "last {of|_}")
        )
    }


    @volatile private var funcSingle: Map[String, String] = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        val parser = new NCMacroParser

        funcSingle =
            FUNC_NUM_SINGLE.flatMap(
                func =>
                    (func.synonyms :+ func.name).
                        toSet.flatMap(parser.expand).
                        map(_.split(" ").map(_.strip).filter(_.nonEmpty).map(NCNlpCoreManager.stem)).
                        map(stems => stems.mkString(" ")).
                        map { syn => syn -> func.name }.
                    toMap
            ).toMap

        ackStarted()
    }

    /**
      *
      * @param parent Optional parent span.
      */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        funcSingle = null

        ackStopped()
    }

    override def enrich(mdl: NCProbeModel, ns: NCNlpSentence, senMeta: Map[String, Serializable], parent: Span): Unit = {
        require(isStarted)

        val restricted =
            mdl.model.getRestrictedCombinations.asScala.getOrElse(TOK_ID, java.util.Collections.emptySet()).
                asScala.toSet

        startScopedSpan(
            "enrich", parent, "srvReqId" -> ns.srvReqId, "mdlId" -> mdl.model.getId, "txt" -> ns.text
        ) { _ => processSingleFunctions(ns, restricted)
        }
    }

    /**
      *
      * @param ns
      * @param restricted
      */
    private def processSingleFunctions(ns: NCNlpSentence, restricted: Set[String]): Unit = {
        val buf = mutable.ArrayBuffer.empty[Seq[NCNlpSentenceToken]]

        for (toks <- ns.tokenMixWithStopWords() if !buf.exists(_.exists(toks.contains))) {
            val stops = toks.filter(_.isStopWord)

            val toksAllCombs =
                (0 to stops.size).
                    flatMap(i => stops.combinations(i).map(comb => toks.filter(t => !comb.contains(t)))).
                    filter(_.nonEmpty)

            toksAllCombs.to(LazyList).
                flatMap(comb =>
                    funcSingle.get(comb.map(_.stem).mkString(" ")) match {
                        case Some(funName) => Some(comb -> funName)
                        case None => None
                    }
                ).headOption match {
                    case Some((comb, funName)) =>
                        buf += toks

                        val after = ns.tokens.drop(comb.last.index + 1)

                        after.find(_.isUser) match {
                            case Some(userTok) =>
                                val betweenFuncAndUser = after.takeWhile(_ != userTok)

                                if (betweenFuncAndUser.isEmpty || betweenFuncAndUser.forall(_.isStopWord)) {
                                    val usrNoteTypes =
                                        userTok.flatMap(n =>
                                            if (n.isUser && !restricted.contains(n.noteType)) Some(n.noteType) else None
                                        )

                                    for (usrNoteType <- usrNoteTypes) {
                                        val note =
                                            NCNlpSentenceNote(
                                                comb.map(_.index).toSeq,
                                                TOK_ID,
                                                "type" -> funName,
                                                "indexes" -> Collections.singletonList(userTok.index),
                                                "note" -> usrNoteType
                                            )
                                        comb.foreach(_.add(note))
                                    }
                                }

                            case None => // No-op.
                        }
                    case None => // No-op.
                }
        }
    }
}
