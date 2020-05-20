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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.relation

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
  * Relation enricher.
  */
object NCRelationEnricher extends NCProbeEnricher {
    case class Holder(funcType: String, headStem: String, allStems: Set[String])
    case class Match(
        funcType: String,
        matched: Seq[NCNlpSentenceToken],
        matchedHead: NCNlpSentenceToken,
        refNotes: Set[String],
        refIndexes: java.util.List[Int]
    )
    case class Reference(tokens: Seq[NCNlpSentenceToken], types: Set[String]) {
        require(tokens.nonEmpty)
        require(types.nonEmpty)
    }

    private final val TOK_ID = "nlpcraft:relation"

    private final val REL_TYPES = Seq(
        "nlpcraft:continent",
        "nlpcraft:subcontinent",
        "nlpcraft:country",
        "nlpcraft:metro",
        "nlpcraft:region",
        "nlpcraft:city",
        "nlpcraft:date"
    )

    private var FUNCS: Seq[Holder] = _
    private var ALL_FUNC_STEMS: Set[String] = _

    /**
      * Starts this component.
      */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        val macros = NCMacroParser()

        FUNCS = {
            val seq = mutable.ArrayBuffer.empty[Holder]

            /**
              *
              * @param f Function ID.
              * @param head Mandatory "start" word(s).
              * @param tails All possible "connecting" words (zero or more can be in the sentence).
              */
            def add(f: String, head: String, tails: Seq[String]): Unit = {
                require(!head.contains(" "))

                val tailsSeq = tails.toSet.map(NCNlpCoreManager.stem)

                macros.expand(head).map(NCNlpCoreManager.stem).foreach(s ⇒ seq += Holder(f, s, tailsSeq + s))
            }

            val tails = Seq(
                "to",
                "and",
                "for",
                "with",
                "in",
                "of",
                "between",
                "statistic",
                "stats",
                "info",
                "metrics",
                "information",
                "data",
                "vs",
                "versus"
            )

            add("compare", "{compare|comparing|contrast|difference|match|vs|versus}", tails)
            add("correlate", "{correlate|correlation|relation|relationship}", tails)

            seq.sortBy(-_.allStems.size)
        }

        ALL_FUNC_STEMS = FUNCS.flatMap(_.allStems).toSet

        super.start()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }

    /**
      * Checks whether important tokens deleted as stopwords or not.
      *
      * @param ns Sentence.
      * @param toks Tokens in which some stopwords can be deleted.
      */
    private def validImportant(ns: NCNlpSentence, toks: Seq[NCNlpSentenceToken]): Boolean = {
        def isImportant(t: NCNlpSentenceToken): Boolean =
            t.exists(n ⇒ n.isUser || REL_TYPES.contains(n.noteType)) || ALL_FUNC_STEMS.contains(t.stem)

        val idxs = toks.map(_.index)

        require(idxs == idxs.sorted)

        val toks2 = ns.slice(idxs.head, idxs.last + 1)

        toks.length == toks2.length || toks.count(isImportant) == toks2.count(isImportant)
    }

    @throws[NCE]
    override def enrich(mdl: NCModelDecorator, ns: NCNlpSentence, senMeta: Map[String, Serializable], parent: Span = null): Unit =
        startScopedSpan("enrich", parent,
            "srvReqId" → ns.srvReqId,
            "modelId" → mdl.model.getId,
            "txt" → ns.text) { _ ⇒
            // Tries to grab tokens direct way.
            // Example: A, B, C ⇒ ABC, AB, BC .. (AB will be processed first)
            val notes = mutable.HashSet.empty[NCNlpSentenceNote]

            for (toks ← ns.tokenMixWithStopWords() if validImportant(ns, toks))
                tryToMatch(toks) match {
                    case Some(m) ⇒
                        for (refNote ← m.refNotes) {
                            val note = NCNlpSentenceNote(
                                Seq(m.matchedHead.index),
                                TOK_ID,
                                "type" → m.funcType,
                                "indexes" → m.refIndexes,
                                "note" → refNote
                            )

                            if (!notes.exists(n ⇒ ns.notesEqualOrSimilar(n, note))) {
                                notes += note

                                m.matched.filter(_ != m.matchedHead).foreach(_.addStopReason(note))

                                m.matchedHead.add(note)
                            }
                        }
                    case None ⇒ // No-op.
                }
        }

    /**
      *
      * @param toks
      */
    private def getReference(toks: Seq[NCNlpSentenceToken]): Option[Reference] =
        if (toks.isEmpty)
            None
        else {
            val sortedToks = toks.sortBy(_.index)

            val i1 = sortedToks.head.index
            val i2 = sortedToks.last.index

            val notes =
                sortedToks.flatMap(_.filter(n ⇒ n.isUser || REL_TYPES.contains(n.noteType))).
                // Finds notes for tokens related to only given tokens.
                filter(n ⇒ n.tokenFrom >= i1 && n.tokenTo <= i2)

            val suitNotes =
                notes.
                    filter(n1 ⇒ notes.exists(
                        n2 ⇒ n2.tokenFrom > n1.tokenTo || n2.tokenTo < n1.tokenFrom)
                    ).
                    groupBy(_.noteType).
                    flatMap { case (_, ns) ⇒ if (ns.size > 1) ns else Seq.empty }

            if (suitNotes.nonEmpty)
                Some(
                    Reference(
                        toks.filter(t ⇒ suitNotes.exists(t.contains)),
                        suitNotes.map(_.noteType).toSet
                    )
                )
            else
                None
        }

    /**
      *
      * @param toks
      */
    private def tryToMatch(toks: Seq[NCNlpSentenceToken]): Option[Match] = {
        val i1 = toks.head.index
        val i2 = toks.last.index

        var refOpts = toks.
            filter(t ⇒
                t.exists(n ⇒ (
                    n.isUser || REL_TYPES.contains(n.noteType)) &&
                    n.tokenIndexes.head >= i1 &&
                    n.tokenIndexes.last <= i2
                )
            )
        val matchOpts = toks.diff(refOpts)

        if (refOpts.nonEmpty && matchOpts.nonEmpty)
            getReference(refOpts) match {
                case Some(r) ⇒
                    refOpts = r.tokens

                    def try0(stems: Set[String]): Option[Match] =
                        FUNCS.
                            flatMap(h ⇒
                                if (stems.subsetOf(h.allStems) && stems.contains(h.headStem)) Some(h) else None
                            ).
                            headOption match {
                            case Some(h) ⇒
                                Some(
                                    Match(
                                        h.funcType,
                                        matchOpts,
                                        matchOpts.
                                            find(_.stem == h.headStem).
                                            getOrElse(throw new AssertionError("Missed token.")),
                                        r.types,
                                        refOpts.map(_.index).asJava
                                    )
                                )
                            case None ⇒ None
                        }

                    try0(matchOpts.map(_.stem).toSet) match {
                        case Some(m) ⇒ Some(m)
                        case None ⇒ try0(matchOpts.filter(!_.isStopWord).map(_.stem).toSet)
                    }

                case None ⇒ None
            }
        else
            None
    }
}
