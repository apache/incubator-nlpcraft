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

    private val STEM_AND = NCNlpCoreManager.stem("and")

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

                val minIdx = toks.dropWhile(_.isNlp).head.index
                val maxIdx = toks.reverse.dropWhile(_.isNlp).head.index

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
            throw new AssertionError(s"Invalid null result " +
                s"[tokensTexts=[${toks.map(_.origText).mkString(", ")}]" +
                s", tokensIndexes=[${toks.map(_.index).mkString(", ")}]" +
                s", allData=[${toksNoteData.mkString(", ")}]" +
                s"]"
            )

        res
    }

    /**
      *
      * @param toks
      */
    private def tryToMatch(toks: Seq[NCNlpSentenceToken]): Option[Match] = {
        case class KeyWord(tokens: Seq[NCNlpSentenceToken], synonymIndex: Int) {
            require(tokens.nonEmpty)

            // Added for debug reasons.
            override def toString = s"${tokens.map(_.origText).mkString(" ")} [${tokens.map(_.index).mkString(",")}]"
        }

        def extract(
            keyStems: Seq[String], toks: Seq[NCNlpSentenceToken], used: Seq[NCNlpSentenceToken] = Seq.empty
        ): Option[KeyWord] = {
            require(keyStems.nonEmpty)

            if (toks.nonEmpty) {
                val maxWords = keyStems.map(_.count(_ == ' ')).max + 1

                (1 to maxWords).reverse.flatMap(i ⇒
                    toks.sliding(i).filter(toks ⇒ used.intersect(toks).isEmpty).
                        map(toks ⇒ toks.map(_.stem).mkString(" ") → toks).toMap.
                        flatMap { case (stem, stemToks) ⇒
                            if (keyStems.contains(stem)) Some(KeyWord(stemToks, keyStems.indexOf(stem))) else None
                        }.toStream.headOption
                ).toStream.headOption
            }
            else
                None
        }

        var res: Option[Match] = None

        // SORT and ORDER don't have same words (validated.)
        val subjOpt = extract(SORT, toks)
        val orderOpt = extract(ORDER.map(_._1), toks)

        if (subjOpt.nonEmpty || orderOpt.nonEmpty) {
            val byOpt = extract(BY, toks, used = subjOpt.toSeq.flatMap(_.tokens) ++ orderOpt.toSeq.flatMap(_.tokens))

            val subjToks = subjOpt.toSeq.flatMap(_.tokens)
            val byToks = byOpt.toSeq.flatMap(_.tokens)
            val orderToks = orderOpt.toSeq.flatMap(_.tokens)

            val all = subjToks ++ byToks ++ orderToks

            def getKeyWordType(t: NCNlpSentenceToken): Option[String] =
                if (subjToks.contains(t))
                    Some("SORT")
                else if (byToks.contains(t))
                    Some("BY")
                else if (orderToks.contains(t))
                    Some("ORDER")
                else
                    None

            val others = toks.filter(t ⇒ !all.contains(t))

            if (others.nonEmpty) {
                val othersRefs = others.filter(_.exists(_.isUser))

                if (
                    othersRefs.nonEmpty &&
                    others.filter(p ⇒ !othersRefs.contains(p)).forall(p ⇒ p.isStopWord || p.stem == STEM_AND)
                ) {
                    // It removes duplicates (`SORT x x ORDER x x x` converts to `SORT x ORDER x`)
                    val mask = toks.map(t ⇒
                        getKeyWordType(t).getOrElse("x")).
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

                            val noteData1 = toNoteData(part1)
                            val noteData2 = if (part2.isEmpty) Seq.empty else toNoteData(part2)

                            if (noteData1.nonEmpty || noteData2.nonEmpty) {
                                val seq1 = split(part1, noteData1, nullable = false)
                                val seq2 = if (part2.isEmpty) Seq.empty else split(part2, noteData2, nullable = true)
                                val asc = orderOpt.flatMap(order ⇒ Some(ORDER(order.synonymIndex)._2))

                                typ match {
                                    case TYPE_SUBJ ⇒
                                        require(seq1.nonEmpty)
                                        require(seq2.isEmpty)
                                        require(subjToks.nonEmpty)

                                        // Ignores invalid cases.
                                        if (byToks.isEmpty)
                                            res =
                                                Some(
                                                    Match(
                                                        asc = asc,
                                                        main = subjToks,
                                                        stop = orderToks,
                                                        subjSeq = seq1,
                                                        bySeq = Seq.empty
                                                    )
                                                )

                                    case TYPE_SUBJ_BY ⇒
                                        require(seq1.nonEmpty)
                                        require(seq2.nonEmpty)
                                        require(subjToks.nonEmpty)
                                        require(byToks.nonEmpty)

                                        Match(
                                            asc = asc,
                                            main = subjToks,
                                            stop = byToks ++ orderToks,
                                            subjSeq = seq1,
                                            bySeq = seq2
                                        )

                                    case TYPE_BY ⇒
                                        require(seq1.nonEmpty)
                                        require(seq2.isEmpty)
                                        require(byToks.nonEmpty)

                                        // Ignores invalid cases.
                                        if (subjToks.isEmpty)
                                            res = Some(
                                                Match(
                                                    asc = asc,
                                                    main = byToks,
                                                    stop = byToks ++ orderToks,
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

    override def enrich(mdl: NCModelDecorator, ns: NCNlpSentence, meta: Map[String, Serializable], parent: Span): Unit =
        startScopedSpan("enrich", parent,
            "srvReqId" → ns.srvReqId,
            "modelId" → mdl.model.getId,
            "txt" → ns.text) { _ ⇒
            val buf = mutable.Buffer.empty[Set[NCNlpSentenceToken]]

            for (toks ← ns.tokenMixWithStopWords() if areSuitableTokens(buf, toks))
                tryToMatch(toks) match {
                    case Some(m) ⇒
                        for (subj ← m.subjSeq) {
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

                                addNotes(params, subj, "subjnotes", "subjindexes")
                            }

                            def mkNote(params: ArrayBuffer[(String, Any)]): Unit = {
                                val note = NCNlpSentenceNote(m.main.map(_.index), TOK_ID, params: _*)

                                m.main.foreach(_.add(note))
                                m.stop.foreach(_.addStopReason(note))

                                buf += toks.toSet
                            }

                            if (m.bySeq.nonEmpty)
                                for (by ← m.bySeq)
                                    mkNote(addNotes(mkParams(), by, "bynotes", "byindexes"))
                            else
                                mkNote(mkParams())
                        }
                    case None ⇒ // No-op.
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