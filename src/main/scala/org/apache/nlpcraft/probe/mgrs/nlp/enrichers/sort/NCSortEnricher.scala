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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.sort

import java.io.Serializable

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote, NCNlpSentenceToken}
import org.apache.nlpcraft.probe.mgrs.NCModelDecorator
import org.apache.nlpcraft.probe.mgrs.nlp.NCProbeEnricher

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.collection.{Map, Seq, mutable}

/**
  * Sort enricher.
  */
object NCSortEnricher extends NCProbeEnricher {
    // Single words.
    private final val SORT =
        Seq("sort", "rank", "classify", "order", "arrange", "organize", "segment", "shuffle").map(NCNlpCoreManager.stem)

    // Single words.
    // Cannot be same as in SORT.
    private final val BY: Seq[String] = Seq("by", "on", "with").map(NCNlpCoreManager.stem)

    // Multiple words.
    // Cannot be same as in SORT and BY.
    // Some words from chunks can be the same as SORT but cannot be same as BY.
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
        ).flatMap { case (txt, asc) ⇒ p.expand(txt).map(p ⇒ NCNlpCoreManager.stem(p) → asc ) }
    }

    private final val STEM_AND = NCNlpCoreManager.stem("and")

    private final val MASK_WORDS =
        (SORT ++ BY ++ ORDER.map(_._1)).flatMap(_.split(" ")).map(_.trim).filter(_.nonEmpty).distinct

    private final val TOK_ID = "nlpcraft:sort"

    object Type extends Enumeration {
        type Type = Value
        val TYPE_SUBJ_BY, TYPE_SUBJ, TYPE_BY = Value
    }

    import Type._

    // Elements: SORT, BY, ORDER, x.
    // Note that SORT, BY, ORDER - are sets of words (not single words)
    // x - means one or multiple words. x must be at least one for each line, maximum two.
    private final val MASKS: Map[String, Type] =
        Map(
            "x SORT BY x" → TYPE_SUBJ_BY,
            "x SORT BY x ORDER" → TYPE_SUBJ_BY,

            "SORT x BY x" → TYPE_SUBJ_BY,
            "SORT x BY x ORDER" → TYPE_SUBJ_BY,

            "SORT x ORDER BY x" → TYPE_SUBJ_BY,
            "x SORT ORDER BY x" → TYPE_SUBJ_BY,

            "ORDER SORT x BY x" → TYPE_SUBJ_BY,

            "SORT x ORDER" → TYPE_SUBJ,
            "SORT x BY ORDER" → TYPE_SUBJ,
            "ORDER SORT x" → TYPE_SUBJ,
            "SORT x" → TYPE_SUBJ,
            "x SORT" → TYPE_SUBJ,

            "SORT BY x ORDER" → TYPE_BY,
            "SORT BY x" → TYPE_BY,
            "ORDER SORT BY x" → TYPE_BY
        )

    case class NoteData(note: String, indexes: Seq[Int]) {
        // Added for debug reasons.
        override def toString: String = s"NoteData [note=$note, indexes=[${indexes.mkString(",")}]]"
    }

    private case class Match(
        asc: Option[Boolean],
        main: Seq[NCNlpSentenceToken],
        stop: Seq[NCNlpSentenceToken],
        subjSeq: Seq[Seq[NoteData]],
        bySeq: Seq[Seq[NoteData]]
    ) {
        require(main.nonEmpty)
        require(subjSeq.nonEmpty || bySeq.nonEmpty)

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
        // Not duplicated.
        require(SORT.size + BY.size + ORDER.size == (SORT ++ BY ++ ORDER.map(_._1)).distinct.size)

        // Single words.
        require(!SORT.exists(_.contains(" ")))
        require(!BY.exists(_.contains(" ")))

        // Different words.
        require(SORT.intersect(BY).isEmpty)
        require(SORT.intersect(ORDER.map(_._1)).isEmpty)
        require(BY.intersect(ORDER.map(_._1)).isEmpty)

        // `Sort by` as one element.
        require(MASKS.filter(_._2 == TYPE_BY).keys.forall(_.contains("SORT BY")))

        val ordersSeq: Seq[Seq[String]] = ORDER.map(_._1).map(_.split(" ").toSeq)

        // ORDER doesn't contain words from BY (it can contain words from SORT).
        require(!BY.exists(ordersSeq.contains))

        // Right order of keywords and references.
        MASKS.keys.map(_.split(" ")).foreach(seq ⇒ {
            require(seq.forall(p ⇒ p == "SORT" || p == "ORDER" || p == "BY" || p == "x"))

            seq.groupBy(p ⇒ p).foreach { case (key, group) ⇒
                val n = group.length

                key match {
                    case "x" ⇒ require(n == 1 || n == 2)
                    case _ ⇒ require(n == 1)
                }
            }
        })
    }

    private def toNoteData(toks: Seq[NCNlpSentenceToken]): Seq[NoteData] = {
        require(toks.nonEmpty)

        val min = toks.head.index
        val max = toks.last.index

        toks.flatten.
            filter(!_.isNlp).
            filter(n ⇒ n.tokenIndexes.head >= min && n.tokenIndexes.last <= max).
            map(n ⇒ NoteData(n.noteType, n.tokenFrom to n.tokenTo)).
            sortBy(_.indexes.head).distinct
    }

    /**
      * [Token] -> [NoteData]
      * [Token(A, B), Token(A), Token(C, D), Token(C, D, X), Token(Z)] ⇒
      * [ [A (0, 1), C (2, 3), Z (4)], [A (0, 1), D (2, 3), Z (4) ] ]
      *
      * @param toksNoteData
      */
    private def split(toks: Seq[NCNlpSentenceToken], toksNoteData: Seq[NoteData], nullable: Boolean): Seq[Seq[NoteData]] = {
        val res =
            if (toksNoteData.nonEmpty) {
                val res = mutable.ArrayBuffer.empty[Seq[NoteData]]

                /**
                  * Returns flag which indicates are token contiguous or not.
                  *
                  * @param tok1Idx First token index.
                  * @param tok2Idx Second token index.
                  */
                def contiguous(tok1Idx: Int, tok2Idx: Int): Boolean = {
                    val between = toks.filter(t ⇒ t.index > tok1Idx && t.index < tok2Idx)

                    between.isEmpty || between.forall(p ⇒ p.isStopWord || p.stem == STEM_AND)
                }

                val minIdx = toks.dropWhile(t ⇒ !isUserNotValue(t)).head.index
                val maxIdx = toks.reverse.dropWhile(t ⇒ !isUserNotValue(t)).head.index

                require(minIdx <= maxIdx)

                def fill(nd: NoteData, seq: mutable.ArrayBuffer[NoteData] = mutable.ArrayBuffer.empty[NoteData]): Unit = {
                    seq += nd

                    toksNoteData.
                        filter(p ⇒ nd.indexes.last < p.indexes.head && contiguous(nd.indexes.last, p.indexes.head)).
                        foreach(fill(_, mutable.ArrayBuffer.empty[NoteData] ++ seq.clone()))

                    if (seq.nonEmpty && seq.head.indexes.head == minIdx && seq.last.indexes.last == maxIdx)
                        res += seq
                }

                toksNoteData.filter(_.indexes.head == minIdx).foreach(p ⇒ fill(p))

                res
            }
            else
                Seq.empty

        if (res.isEmpty && !nullable)
            throw new AssertionError(s"Invalid empty result " +
                s"[tokensTexts=[${toks.map(_.origText).mkString("|")}]" +
                s", notes=[${toks.flatten.map(n ⇒ s"${n.noteType}:[${n.tokenIndexes.mkString(",")}]").mkString("|")}]" +
                s", tokensIndexes=[${toks.map(_.index).mkString("|")}]" +
                s", allData=[${toksNoteData.mkString("|")}]" +
                s"]"
            )

        res
    }

    /**
      *
      * @param t
      */
    private def isUserNotValue(t: NCNlpSentenceToken): Boolean =
        t.find(_.isUser) match {
            case Some(n) ⇒ !n.contains("value")
            case None ⇒ false
        }

    /**
      *
      * @param n
      */
    private def isUserNotValue(n: NCNlpSentenceNote): Boolean = n.isUser && !n.contains("value")

    /**
      *
      * @param toks
      */
    private def tryToMatch(toks: Seq[NCNlpSentenceToken]): Option[Match] = {
        require(toks.nonEmpty)

        case class KeyWord(tokens: Seq[NCNlpSentenceToken], synonymIndex: Int) {
            require(tokens.nonEmpty)
        }

        def extract(keyStems: Seq[String], used: Seq[NCNlpSentenceToken]): Option[KeyWord] = {
            require(keyStems.nonEmpty)

            val maxWords = keyStems.map(_.count(_ == ' ')).max + 1

            (1 to maxWords).reverse.flatMap(i ⇒
                toks.sliding(i).filter(toks ⇒ used.intersect(toks).isEmpty).
                    map(toks ⇒ toks.map(_.stem).mkString(" ") → toks).toMap.
                    flatMap { case (stem, stemToks) ⇒
                        if (keyStems.contains(stem)) Some(KeyWord(stemToks, keyStems.indexOf(stem))) else None
                    }.toStream.headOption
            ).toStream.headOption
        }

        var res: Option[Match] = None

        // Order is important.
        // SORT and ORDER don't have same words (validated)
        val orderOpt = extract(ORDER.map(_._1), used = Seq.empty)
        val byOpt = extract(BY, used = orderOpt.toSeq.flatMap(_.tokens))
        val sortOpt = extract(SORT, used = orderOpt.toSeq.flatMap(_.tokens) ++ byOpt.toSeq.flatMap(_.tokens))

        if (sortOpt.nonEmpty || orderOpt.nonEmpty) {
            val sortToks = sortOpt.toSeq.flatMap(_.tokens)
            val byToks = byOpt.toSeq.flatMap(_.tokens)
            val orderToks = orderOpt.toSeq.flatMap(_.tokens)

            val all = sortToks ++ byToks ++ orderToks

            def getKeyWordType(t: NCNlpSentenceToken): String =
                if (sortToks.contains(t))
                    "SORT"
                else if (byToks.contains(t))
                    "BY"
                else if (orderToks.contains(t))
                    "ORDER"
                else if (isUserNotValue(t))
                    "x"
                else
                    "-"

            val others = toks.filter(t ⇒ !all.contains(t))

            if (others.nonEmpty) {
                val i1 = others.head.index
                val i2 = others.last.index

                val othersRefs = others.filter(
                    t ⇒ t.exists(n ⇒ isUserNotValue(n) && n.tokenIndexes.head >= i1 && n.tokenIndexes.last <= i2)
                )

                if (
                    othersRefs.nonEmpty &&
                    others.filter(p ⇒ !othersRefs.contains(p)).
                        forall(p ⇒ (p.isStopWord || p.stem == STEM_AND) && !MASK_WORDS.contains(p.stem))
                ) {
                    // It removes duplicates (`SORT x x ORDER x x x` converts to `SORT x ORDER x`)
                    val mask = toks.map(getKeyWordType).
                        foldLeft("")((x, y) ⇒ if (x.endsWith(y)) x else s"$x $y").trim

                    MASKS.get(mask) match {
                        case Some(typ) ⇒
                            val sepIdxs = all.
                                map(_.index).
                                filter(i ⇒ others.exists(_.index > i) && others.exists(_.index < i)).
                                sorted

                            // Divides separated by keywords.
                            val (part1, part2) =
                                if (sepIdxs.isEmpty)
                                    (others, Seq.empty)
                                else
                                    (others.filter(_.index < sepIdxs.head), others.filter(_.index > sepIdxs.last))

                            require(part1.nonEmpty)

                            val data1 = toNoteData(part1)
                            val data2 = if (part2.isEmpty) Seq.empty else toNoteData(part2)

                            if (data1.nonEmpty || data2.nonEmpty) {
                                val seq1 =
                                    if (data1.nonEmpty)
                                        split(part1, data1, nullable = false)
                                    else
                                        split(part2, data2, nullable = false)
                                val seq2 =
                                    if (data1.nonEmpty && data2.nonEmpty)
                                        split(part2, data2, nullable = true)
                                    else
                                        Seq.empty
                                val asc = orderOpt.flatMap(order ⇒ Some(ORDER(order.synonymIndex)._2))

                                typ match {
                                    case TYPE_SUBJ ⇒
                                        require(seq1.nonEmpty)
                                        require(seq2.isEmpty)
                                        require(sortToks.nonEmpty)

                                        // Ignores invalid cases.
                                        if (byToks.isEmpty)
                                            res =
                                                Some(
                                                    Match(
                                                        asc = asc,
                                                        main = sortToks,
                                                        stop = orderToks,
                                                        subjSeq = seq1,
                                                        bySeq = Seq.empty
                                                    )
                                                )

                                    case TYPE_SUBJ_BY ⇒
                                        require(seq1.nonEmpty)
                                        require(seq2.nonEmpty)
                                        require(sortToks.nonEmpty)
                                        require(byToks.nonEmpty)

                                        res = Some(
                                            Match(
                                                asc = asc,
                                                main = sortToks,
                                                stop = byToks ++ orderToks,
                                                subjSeq = seq1,
                                                bySeq = seq2
                                            )
                                        )

                                    case TYPE_BY ⇒
                                        require(seq1.nonEmpty)
                                        require(seq2.isEmpty)
                                        require(sortToks.nonEmpty)
                                        require(byToks.nonEmpty)

                                        // `Sort by` as one element, see validation.
                                        res = Some(
                                            Match(
                                                asc = asc,
                                                main = sortToks ++ byToks,
                                                stop = orderToks,
                                                subjSeq = Seq.empty,
                                                bySeq = seq1
                                            )
                                        )

                                    case _ ⇒ throw new AssertionError(s"Unexpected type: $typ")
                                }
                            }
                        case None ⇒ // No-op.
                    }
                }
            }
        }

        res
    }

    /**
      * Checks whether important tokens deleted as stopwords or not.
      *
      * @param ns Sentence.
      * @param toks Tokens in which some stopwords can be deleted.
      */
    private def validImportant(ns: NCNlpSentence, toks: Seq[NCNlpSentenceToken]): Boolean = {
        def isImportant(t: NCNlpSentenceToken): Boolean = isUserNotValue(t) || MASK_WORDS.contains(t.stem)

        val idxs = toks.map(_.index)

        require(idxs == idxs.sorted)

        val toks2 = ns.slice(idxs.head, idxs.last + 1)

        toks.length == toks2.length || toks.count(isImportant) == toks2.count(isImportant)
    }

    override def enrich(mdl: NCModelDecorator, ns: NCNlpSentence, meta: Map[String, Serializable], parent: Span): Unit =
        startScopedSpan("enrich", parent,
            "srvReqId" → ns.srvReqId,
            "modelId" → mdl.model.getId,
            "txt" → ns.text) { _ ⇒
            val notes = mutable.HashSet.empty[NCNlpSentenceNote]

            for (toks ← ns.tokenMixWithStopWords() if validImportant(ns, toks)) {
                tryToMatch(toks) match {
                    case Some(m) ⇒
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

                        def mkNote(params: ArrayBuffer[(String, Any)]): Unit = {
                            val note = NCNlpSentenceNote(m.main.map(_.index), TOK_ID, params: _*)

                            if (!notes.exists(n ⇒ ns.notesEqualOrSimilar(n, note))) {
                                notes += note

                                m.main.foreach(_.add(note))
                                m.stop.foreach(_.addStopReason(note))
                            }
                        }

                        def mkParams(): mutable.ArrayBuffer[(String, Any)] = {
                            val params = mutable.ArrayBuffer.empty[(String, Any)]

                            if (m.asc.isDefined)
                                params += "asc" → m.asc.get

                            params
                        }

                        if (m.subjSeq.nonEmpty)
                            for (subj ← m.subjSeq) {
                                def addSubj(): ArrayBuffer[(String, Any)] =
                                    addNotes(mkParams(), subj, "subjnotes", "subjindexes")

                                if (m.bySeq.nonEmpty)
                                    for (by ← m.bySeq)
                                        mkNote(addNotes(addSubj(), by, "bynotes", "byindexes"))
                                else
                                    mkNote(addSubj())
                            }
                        else {
                            require(m.bySeq.nonEmpty)

                            for (by ← m.bySeq)
                                mkNote(addNotes(mkParams(), by, "bynotes", "byindexes"))
                        }

                    case None ⇒ // No-op.
                }
            }
        }

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        validate()

        super.start()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
}