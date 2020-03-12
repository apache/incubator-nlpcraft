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
import scala.collection.mutable.ArrayBuffer
import scala.collection.{Map, Seq, mutable}

/**
  * Sort enricher.
  */
object NCSortEnricher extends NCProbeEnricher {
    private final val SORT =
        Seq("sort", "rank", "classify", "order", "arrange", "organize", "segment", "shuffle").map(NCNlpCoreManager.stem)

    private final val BY: Seq[String] = Seq("by", "on", "with").map(NCNlpCoreManager.stem)

    private final val ORDER = {
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

    private final val TOK_ID = "nlpcraft:sort"

    private final val SORT_TYPES = Seq(
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

    case class NoteData(note: String, indexes: Seq[Int])

    private case class Match(
        asc: Option[Boolean],
        main: Seq[NCNlpSentenceToken],
        stop: Seq[NCNlpSentenceToken],
        subjSeq: Seq[Seq[NoteData]],
        bySeq: Seq[Seq[NoteData]]
    ) {
        require(main.nonEmpty)
        require(subjSeq.nonEmpty)

        lazy val all: Seq[NCNlpSentenceToken] = main ++ stop

        // Added for debug reasons.
        override def toString: String = {
            def s1[T](seq: Seq[NCNlpSentenceToken]): String = s"[${seq.map(_.origText).mkString(", ")}]"
            def s2[T](seq: Seq[NoteData]): String =
                s"[${seq.map(p ⇒ s"${p.note}: [${p.indexes.mkString(", ")}]").mkString(", ")}]"
            def s3[T](seq: Seq[Seq[NoteData]]): String = s"[${seq.map(s2).mkString(", ")}]"

            s"Match [main=${s1(main)}, stop=${s1(stop)}, subjSeq=${s3(subjSeq)}, bySeq=${s3(bySeq)}]"
        }
    }

    /**
      *
      */
    private def validate() {
        require(SORT.size + BY.size + ORDER.size == (SORT ++ BY ++ ORDER.unzip._1).distinct.size)

        val seq1 = SORT.flatMap(_.split(" "))
        val seq2 = BY.flatMap(_.split(" "))
        val seq3 = ORDER.map(_._1).flatMap(_.split(" "))

        require(seq1.size == seq1.distinct.size)
        require(seq2.size == seq2.distinct.size)
        require(seq3.size == seq3.distinct.size)

        require(seq1.intersect(seq2).isEmpty)
        require(seq1.intersect(seq3).isEmpty)
        require(seq2.intersect(seq3).isEmpty)

        SEQS.map(_.split(" ")).foreach(seq ⇒ {
            require(seq.forall(p ⇒ p == "SORT" || p == "ORDER" || p == "BY" || p == "x"))

            seq.groupBy(p ⇒ p).foreach { case (key, group) ⇒
                key match {
                    case "x" ⇒ require(group.length <= 2)
                    case _ ⇒ require(group.length == 1)
                }
            }
        })
    }

    /**
      * Return flag which indicates are token contiguous or not.
      *
      * @param toks Tokens.
      * @param tok1Idx First token index.
      * @param tok2Idx Second token index.
      */
    private def contiguous(toks: Seq[NCNlpSentenceToken], tok1Idx: Int, tok2Idx: Int): Boolean = {
        val between = toks.filter(t ⇒ t.index > tok1Idx && t.index < tok2Idx)

        between.isEmpty || between.forall(_.isStopWord)
    }

    /**
      * [Token] -> [NoteData]
      * [Token(A, B), Token(A), Token(C, D), Token(C, D, X), Token(Z)] ⇒
      * [ [A (0, 1), C (2, 3), Z (4)], [A (0, 1), D (2, 3), Z (4) ] ]
      *
      * @param toks
      */
    private def split(toks: Seq[NCNlpSentenceToken]): Seq[Seq[NoteData]] = {
        val all =
            toks.flatten.filter(!_.isNlp).map(n ⇒ NoteData(n.noteType, n.tokenFrom to n.tokenTo)).sortBy(_.indexes.head).distinct

        if (all.nonEmpty) {
            val res = mutable.ArrayBuffer.empty[Seq[NoteData]]

            def fill(nd: NoteData, seq: mutable.ArrayBuffer[NoteData] = mutable.ArrayBuffer.empty[NoteData]): Unit = {
                seq += nd

                all.
                    filter(p ⇒ nd.indexes.last < p.indexes.head && contiguous(toks, nd.indexes.last, p.indexes.head)).
                    foreach(fill(_, mutable.ArrayBuffer.empty[NoteData] ++ seq.clone()))

                if (seq.nonEmpty &&
                    seq.head.indexes.head == all.head.indexes.head &&
                    seq.last.indexes.last == all.last.indexes.last
                )
                    res += seq
            }

            fill(all.head)

            res
        }
        else
            Seq.empty
    }

    /**
      *
      * @param toks
      */
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

                if (others.nonEmpty) {
                    val othersRefs = others.filter(t ⇒ t.exists(n ⇒ n.isUser || SORT_TYPES.contains(n.noteType)))

                    if (
                        othersRefs.nonEmpty &&
                        others.filter(p ⇒ !othersRefs.contains(p)).forall(_.isStopWord) &&
                        SEQS.contains(
                            // It removes duplicates (`SORT x x ORDER x x x` converts to `SORT x ORDER x`)
                            toks.map(t ⇒
                                h.getKeyWordType(t).getOrElse("x")).
                                foldLeft("")((x, y) ⇒ if (x.endsWith(y)) x else s"$x $y").trim
                        )
                    ) {
                        val sepIdxs = h.all.
                            map(_.index).
                            filter(i ⇒ others.exists(_.index > i) && others.exists(_.index < i)).
                            sorted

                        // Devides separated by keywords.
                        val (subj, by) =
                            if (sepIdxs.isEmpty)
                                (others, Seq.empty)
                            else
                                (others.filter(_.index < sepIdxs.head), others.filter(_.index > sepIdxs.last))

                        require(subj.nonEmpty)

                        Some(
                            Match(
                                asc = h.order match {
                                    case Some(order) ⇒ Some(ORDER(order.synonymIndex)._2)
                                    case None ⇒ None
                                },
                                main = h.sort.tokens,
                                stop = h.byTokens ++ h.orderTokens,
                                subjSeq = split(subj),
                                bySeq = split(by)
                            )
                        )
                    }
                    else
                        None
                }
                else
                    None
            case None ⇒ None
        }
    }

    override def enrich(mdl: NCModelDecorator, ns: NCNlpSentence, meta: Map[String, Serializable], parent: Span): Boolean =
        startScopedSpan("enrich", parent,
            "srvReqId" → ns.srvReqId,
            "modelId" → mdl.model.getId,
            "txt" → ns.text) { _ ⇒
            val buf = mutable.Buffer.empty[Set[NCNlpSentenceToken]]
            var changed: Boolean = false

            for (toks ← ns.tokenMixWithStopWords() if areSuitableTokens(buf, toks))
                tryToMatch(toks) match {
                    case Some(m) ⇒
                        for (subj ← m.subjSeq if !hasReferences(TOK_ID, "subjNotes", subj.map(_.note), m.main)) {
                            def addNotes(
                                params: ArrayBuffer[(String, Any)],
                                seq: Seq[NoteData],
                                notesName: String,
                                idxsName: String
                            ): ArrayBuffer[(String, Any)] = {
                                params += notesName → seq.map(_.note).asJava
                                params += idxsName → seq.map(_.indexes.asJava).asJava

                                params
                            }

                            def mkParams(): ArrayBuffer[(String, Any)] = {
                                val params = mutable.ArrayBuffer.empty[(String, Any)]

                                if (m.asc.isDefined)
                                    params += "asc" → m.asc.get

                                addNotes(params, subj, "subjNotes", "subjIndexes")
                            }

                            def mkNote(params: ArrayBuffer[(String, Any)]): Unit = {
                                val note = NCNlpSentenceNote(m.main.map(_.index), TOK_ID, params: _*)

                                m.main.foreach(_.add(note))
                                m.stop.foreach(_.addStopReason(note))

                                changed = true
                            }

                            if (m.bySeq.nonEmpty)
                                for (by ← m.bySeq)
                                    mkNote(addNotes(mkParams(), by, "byNotes", "byIndexes"))
                            else
                                mkNote(mkParams())
                        }

                        if (changed)
                            buf += toks.toSet
                    case None ⇒ // No-op.
                }

            changed
        }

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        validate()

        super.start()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
}