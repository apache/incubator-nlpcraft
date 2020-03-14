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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.aggregation

import java.io.Serializable

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote, NCNlpSentenceToken}
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.probe.mgrs.NCModelDecorator
import org.apache.nlpcraft.probe.mgrs.nlp.NCProbeEnricher

import scala.collection.JavaConverters._
import scala.collection.{Map, Seq, mutable}

/**
  * Aggregation enricher.
  */
object NCAggregationEnricher extends NCProbeEnricher {
    case class Match(
        funcType: String,
        matched: Seq[NCNlpSentenceToken],
        refNotes: Set[String],
        refIndexes: java.util.List[Int]
    )
    private final val TOK_ID = "nlpcraft:aggregation"

    @volatile private var FUNCS: Map[String, String] = _

    /**
      * Starts this component.
      */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        val macros = NCMacroParser()

        FUNCS = {
            val m = mutable.HashMap.empty[String, String]

            def add(f: String, syns: String*): Unit =
                syns.
                    flatMap(macros.expand).
                    map(p ⇒ p.split(" ").map(_.trim).map(NCNlpCoreManager.stem).mkString(" ")).
                    foreach(s ⇒ m += s → f)

            add("sum", "{sum|summary} {of|*} {data|value|*}")
            add("max", "{max|maximum} {of|*} {data|value|*}")
            add("min", "{min|minimum|minimal} {of|*} {data|value|*}")
            add("avg", "{avg|average} {of|*} {data|value|*}")
            add("group", "{group|grouped} {of|by|with|for}")

            m.toMap
        }

        super.start()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }

    @throws[NCE]
    override def enrich(mdl: NCModelDecorator, ns: NCNlpSentence, senMeta: Map[String, Serializable], parent: Span = null): Boolean =
        startScopedSpan("enrich", parent,
            "srvReqId" → ns.srvReqId,
            "modelId" → mdl.model.getId,
            "txt" → ns.text) { _ ⇒
            val buf = mutable.Buffer.empty[Set[NCNlpSentenceToken]]
            var changed: Boolean = false

            for (toks ← ns.tokenMixWithStopWords() if areSuitableTokens(buf, toks))
                tryToMatch(toks) match {
                    case Some(m) ⇒
                        for (refNote ← m.refNotes if !hasReference(TOK_ID, "note", refNote, m.matched)) {
                            val note = NCNlpSentenceNote(
                                m.matched.map(_.index),
                                TOK_ID,
                                "type" → m.funcType,
                                "indexes" → m.refIndexes,
                                "note" → refNote
                            )

                            m.matched.foreach(_.add(note))

                            changed = true
                        }

                        if (changed)
                            buf += toks.toSet
                    case None ⇒ // No-op.
                }

            changed
        }

    /**
      *
      * @param toks
      */
    private def tryToMatch(toks: Seq[NCNlpSentenceToken]): Option[Match] = {
        val matchedCands = toks.takeWhile(!_.exists(_.isUser))

        def try0(stem: String): Option[Match] =
            FUNCS.get(stem) match {
                case Some(funcType) ⇒
                    val afterMatched = toks.drop(matchedCands.length)
                    val refCands = afterMatched.filter(_.exists(_.isUser))
                    val commonNotes = getCommonNotes(refCands, Some((n: NCNlpSentenceNote) ⇒ n.isUser))

                    val ok =
                        commonNotes.nonEmpty &&
                        afterMatched.diff(refCands).forall(t ⇒ !t.isQuoted && (t.isStopWord || t.pos == "IN"))

                    if (ok) Some(Match(funcType, matchedCands, commonNotes, refCands.map(_.index).asJava)) else None
                case None ⇒ None
            }

        try0(matchedCands.map(_.stem).mkString(" ")) match {
            case Some(m) ⇒ Some(m)
            case None ⇒ try0(matchedCands.filter(!_.isStopWord).map(_.stem).mkString(" "))
        }
    }
}