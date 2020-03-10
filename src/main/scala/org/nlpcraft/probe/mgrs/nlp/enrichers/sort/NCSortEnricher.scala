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

package org.nlpcraft.probe.mgrs.nlp.enrichers.sort

import java.io.Serializable

import io.opencensus.trace.Span
import org.nlpcraft.common.NCService
import org.nlpcraft.common.makro.NCMacroParser
import org.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote, NCNlpSentenceToken}
import org.nlpcraft.probe.mgrs.NCModelDecorator
import org.nlpcraft.probe.mgrs.nlp.NCProbeEnricher

import scala.collection.JavaConverters._
import scala.collection.{Map, Seq, mutable}

/**
  * Sort enricher.
  */
object NCSortEnricher extends NCProbeEnricher {
    private final val SORT: Seq[String] =
        Seq("sort", "rank", "classify", "order", "arrange", "organize", "segment", "shuffle").map(NCNlpCoreManager.stem)

    private final val BY: Seq[String] =
        Seq("by", "on", "with").map(NCNlpCoreManager.stem)

    private final val ORDER: Seq[(String, Boolean)] = {
        val p = NCMacroParser()

        Seq(
            "top down" → false,
            "bottom up" → true,
            "ascending" → true,
            "asc" → true,
            "descending" → false,
            "desc" → false,
            "{in|by|from} {top down|descending} {order|way|fashion|*}" → false,
            "{in|by|from} {bottom up|ascending} {order|way|fashion|*}" → true
        ).flatMap { case (txt, asc) ⇒ p.expand(txt).map(p ⇒ NCNlpCoreManager.stem(p) → asc) }
    }

    require(SORT.size + BY.size + ORDER.size == (SORT ++ BY ++ ORDER.unzip._1).distinct.size)

    private final val TOK_ID: String = "nlpcraft:sort"

    private final val SORT_TYPES: Seq[String] = Seq(
        "nlpcraft:continent",
        "nlpcraft:subcontinent",
        "nlpcraft:country",
        "nlpcraft:metro",
        "nlpcraft:region",
        "nlpcraft:city",
        "nlpcraft:coordinate",
        "nlpcraft:date",
        "nlpcraft:num"
    )

    // Note that SORT, BY, ORDER - are sets of words (not single words)
    private final val SEQS =
        Seq(
            s"x SORT BY x ORDER",
            s"x SORT BY x",
            s"SORT x BY x",
            s"x SORT",
            s"SORT x ORDER BY x",
            s"SORT x",
            s"ORDER SORT x BY x",
            s"ORDER SORT x",
            s"SORT x BY ORDER"
        )

    // Validation.
    SEQS.map(_.split(" ")).foreach(seq ⇒ {
        require(seq.forall(p ⇒ p == "SORT" || p == "ORDER" || p == "BY" || p == "x"))

        seq.groupBy(p ⇒ p).foreach { case (key, group) ⇒
            key match {
                case "x" ⇒ require(group.length <= 2)
                case _ ⇒ require(group.length == 1)
            }
        }
    })

    case class NoteData(note: String, indexes: Seq[Int])

    private case class Match(
        asc: Option[Boolean],
        main: Seq[NCNlpSentenceToken],
        stop: Seq[NCNlpSentenceToken],
        subj: Seq[Seq[NoteData]],
        by: Seq[Seq[NoteData]]
    ) {
        lazy val all = main ++ stop
    }

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        super.start()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }

    // [Token] -> [NoteData]
    // [Token(A, B), Token(A), Token(C, D), Token(C, D, X), Token(Z)] ⇒
    // [[A (0, 1), C (2, 3), Z (4)], [A (0, 1), D (2, 3), Z (4)]]
    private def split(toks: Seq[NCNlpSentenceToken]): Seq[Seq[NoteData]] = {
        val all = toks.
            flatten.
            filter(n ⇒ !n.isNlp).
            map(n ⇒ NoteData(n.noteType, n.tokenFrom to n.tokenTo)).
            sortBy(_.indexes.head)

        val res = mutable.ArrayBuffer.empty[Seq[NoteData]]
        val used = mutable.ArrayBuffer.empty[NoteData]

        def go(seq: mutable.ArrayBuffer[NoteData], nd: NoteData): Boolean =
            if (!used.contains(nd)) {
                if (seq.isEmpty) {
                    if (nd.indexes.head == 0) {
                        seq += nd
                        used += nd

                        all.find(nd ⇒ !used.contains(nd)) match {
                            case Some(next) ⇒ go(seq, next)
                            case None ⇒ false
                        }
                    }
                    else
                        false
                }
                else {
                    false
                }
            }
            else
                false

        res
    }

    private def tryToMatch(toks: Seq[NCNlpSentenceToken]): Option[Match] = {
        case class KeyWord(tokens: Seq[NCNlpSentenceToken], synonymIndex: Int) {
            // Added for tests reasons.
            override def toString = tokens.map(_.origText).mkString(" ")
        }

        def get0(keyStems: Seq[String], toks: Seq[NCNlpSentenceToken]): Option[KeyWord] = {
            require(keyStems.nonEmpty)

            val maxWords = keyStems.map(_.count(_ == ' ')).max + 1

            (1 to maxWords).reverse.flatMap(i ⇒
                toks.sliding(i).
                    map(toks ⇒ toks.map(_.stem).mkString(" ") → toks).toMap.
                    flatMap { case (stem, stemToks) ⇒
                        if (keyStems.contains(stem)) Some(KeyWord(stemToks, keyStems.indexOf(stem))) else None
                    }.toStream.headOption
            ).toStream.headOption
        }

        case class KeyWordsHolder(sort: KeyWord, by: Option[KeyWord], order: Option[KeyWord]) {
            lazy val byTokens = by.toSeq.flatMap(_.tokens)
            lazy val orderTokens = order.toSeq.flatMap(_.tokens)
            lazy val all = sort.tokens ++ byTokens ++ orderTokens

            def getKeyWordType(t: NCNlpSentenceToken): Option[String] =
                if (sort.tokens.contains(t))
                    Some("SORT")
                else if (byTokens.contains(t))
                    Some("BY")
                else if (orderTokens.contains(t))
                    Some("ORDER")
                else
                    None

            // Added for tests reasons.
            override def toString = s"Sort: $sort, by: ${by.toSeq.mkString(",")}, order: ${order.toSeq.mkString(",")}"
        }

        val hOpt: Option[KeyWordsHolder] =
            get0(SORT, toks) match {
                case Some(sort) ⇒ Some(KeyWordsHolder(sort, get0(BY, toks), get0(ORDER.unzip._1, toks)))
                case None ⇒ None
            }

        hOpt match {
            case Some(h) ⇒
                val others = toks.filter(t ⇒ !h.all.contains(t))
                val othersWithoutStops = others.filter(!_.isStopWord)

                if (
                    othersWithoutStops.nonEmpty &&
                    othersWithoutStops.forall(t ⇒ t.exists(n ⇒ n.isUser || SORT_TYPES.contains(n.noteType))) &&
                    SEQS.contains(
                        // It removes duplicates (`SORT x x ORDER x x x` converts to `SORT x ORDER x`)
                        toks.map(t ⇒
                            h.getKeyWordType(t).getOrElse("x")).
                            foldLeft("")((x, y) ⇒ if (x.endsWith(y)) x else s"$x $y").trim
                    )
                ) {
                    val subj = mutable.ArrayBuffer.empty[NCNlpSentenceToken]
                    val by = mutable.ArrayBuffer.empty[NCNlpSentenceToken]

                    others.foreach(t ⇒ (if (subj.isEmpty || subj.last.index + 1 == t.index) subj else by) += t)

                    require(subj.nonEmpty)

                    val asc =
                        h.order match {
                            case Some(order) ⇒ Some(ORDER(order.synonymIndex)._2)
                            case None ⇒ None
                        }

                    Some(
                        Match(
                            asc = asc,
                            main = h.sort.tokens,
                            stop = h.byTokens ++ h.orderTokens,
                            subj = split(subj),
                            by = split(by)
                        )
                    )
                }
                else
                    None
            case None ⇒ None
        }
    }

//    def suitable(m: Match, notes: Seq[String], refName: String): Boolean =
//        notes.forall(note ⇒ !isReference(TOK_ID, refName, note, m.all))
//

    override def enrich(mdl: NCModelDecorator, ns: NCNlpSentence, senMeta: Map[String, Serializable], parent: Span): Boolean =
        startScopedSpan("enrich", parent,
            "srvReqId" → ns.srvReqId,
            "modelId" → mdl.model.getId,
            "txt" → ns.text) { _ ⇒
            val buf = mutable.Buffer.empty[Set[NCNlpSentenceToken]]
            var changed: Boolean = false

//            for (toks ← ns.tokenMixWithStopWords() if areSuitableTokens(buf, toks))
//                tryToMatch(toks) match {
//                    case Some(m)
////                        if suitable(m, m.subj.map(_.note), "subjNotes") &&
////                        (m.by.isEmpty || suitable(m, m.by.map(_.note), "byNotes")) ⇒
//                        ⇒
//                        val params = mutable.ArrayBuffer.empty[(String, Any)]
//
//                        m.asc match {
//                            case Some(asc) ⇒ params += "asc" → asc
//                            case None ⇒ // No-op.
//                        }
//
//                        def addNotes(seq: Seq[NoteData], notesName: String, idxsName: String): Unit = {
//                            params += notesName → seq.map(_.note).asJava
//                            params += idxsName → seq.map(_.indexes.asJava).asJava
//                        }
//
//                        addNotes(m.subj, "subjNotes", "subjIndexes")
//
//                        if (m.by.nonEmpty)
//                            addNotes(m.by, "byNotes", "byIndexes")
//
//                        val note = NCNlpSentenceNote(m.main.map(_.index), TOK_ID, params :_*)
//
//                        m.main.foreach(_.add(note))
//                        m.stop.foreach(_.addStopReason(note))
//
//                        changed = true
//
//                    case None ⇒ // No-op.
//
//                if (changed)
//                    buf += toks.toSet
//            }

            changed
        }
}
