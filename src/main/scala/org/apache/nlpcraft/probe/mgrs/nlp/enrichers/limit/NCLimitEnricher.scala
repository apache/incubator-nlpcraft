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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.limit

import java.io.Serializable

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.nlp.numeric.{NCNumeric, NCNumericManager}
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote, NCNlpSentenceToken}
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.probe.mgrs.NCModelDecorator
import org.apache.nlpcraft.probe.mgrs.nlp.NCProbeEnricher

import scala.collection.JavaConverters._
import scala.collection.{Map, Seq, mutable}

/**
  * Limit enricher.
  */
object NCLimitEnricher extends NCProbeEnricher {
    case class Match(
        limit: Double,
        asc: Option[Boolean],
        matched: Seq[NCNlpSentenceToken],
        refNotes: Set[String],
        refIndexes: java.util.List[Int]
    )

    private final val TOK_ID = "nlpcraft:limit"

    /**
      * Group of neighbouring tokens. All of them numbers or all of the not.
      *
      * @param tokens Tokens.
      * @param number Tokens numeric value. Optional.
      * @param isFuzzyNum Fuzzy value flag.
      */
    case class Group(tokens: Seq[NCNlpSentenceToken], number: Option[Int], isFuzzyNum: Boolean) {
        lazy val value: String = number match {
            case Some(_) ⇒ CD
            case None ⇒ tokens.map(_.stem).mkString(" ")
        }

        lazy val index: Int = tokens.head.index
    }

    /**
      * Neighbouring groups.
      *
      * @param groups Groups.
      */
    case class GroupsHolder(groups: Seq[Group]) {
        lazy val tokens: Seq[NCNlpSentenceToken] = groups.flatMap(_.tokens)

        lazy val limit: Int = {
            val numElems = groups.filter(_.number.isDefined)

            numElems.size match {
                case 0 ⇒ DFLT_LIMIT
                case 1 ⇒ numElems.head.number.get
                case _ ⇒ throw new AssertionError(s"Unexpected numeric count in template: ${numElems.size}")
            }
        }

        lazy val asc: Boolean = {
            val sorts: Seq[Boolean] = tokens.map(_.stem).flatMap(SORT_WORDS.get)

            sorts.size match {
                case 1 ⇒ sorts.head
                case _ ⇒ false
            }
        }

        lazy val value: String = groups.map(_.value).mkString(" ")
        lazy val isFuzzyNum: Boolean = groups.size == 1 && groups.head.isFuzzyNum
    }

    private final val DFLT_LIMIT = 10

    // Note that single words only supported now in code.
    private final val FUZZY_NUMS: Map[String, Int] = stemmatizeWords(Map(
        "few" → 3,
        "several" → 3,
        "handful" → 5,
        "single" → 1,
        "some" → 3,
        "couple" → 2
    ))

    // Note that single words only supported now in code.
    private final val SORT_WORDS: Map[String, Boolean] = stemmatizeWords(Map(
        "top" → false,
        "most" → false,
        "first" → false,
        "bottom" → true,
        "last" → true
    ))

    private final val TOP_WORDS: Seq[String] = Seq(
        "top",
        "most",
        "bottom",
        "first",
        "last"
    ).map(NCNlpCoreManager.stem)

    private final val POST_WORDS: Seq[String] = Seq(
        "total",
        "all together",
        "overall"
    ).map(NCNlpCoreManager.stem)

    // It designates:
    // - digits (like `25`),
    // - word numbers (like `twenty two`) or
    // - fuzzy numbers (like `few`).
    private final val CD = "[CD]"

    // Macros: SORT_WORDS, TOP_WORDS, POST_WORDS
    private final val MACROS: Map[String, Iterable[String]] = Map(
        "SORT_WORDS" → SORT_WORDS.keys,
        "TOP_WORDS" → TOP_WORDS,
        "POST_WORDS" → POST_WORDS
    )

    // Possible elements:
    // - Any macros,
    // - Special symbol CD (which designates obvious number or fuzzy number word)
    // - Any simple word.
    // Note that `CD` is optional (DFLT_LIMIT will be used)
    private final val SYNONYMS = Seq(
        s"<TOP_WORDS> {of|*} {$CD|*} {<POST_WORDS>|*}",
        s"$CD of",
        s"$CD <POST_WORDS>",
        s"<POST_WORDS> $CD"
    )

    private final val LIMITS: Seq[String] = {
        // Few numbers cannot be in on template.
        require(SYNONYMS.forall(_.split(" ").map(_.trim).count(_ == CD) < 2))

        def toMacros(seq: Iterable[String]): String = seq.mkString("|")

        val parser = NCMacroParser(MACROS.map { case (name, seq) ⇒ s"<$name>" → s"{${toMacros(seq)}}" })

        // Duplicated elements is not a problem.
        SYNONYMS.flatMap(parser.expand).distinct
    }

    private final val TECH_WORDS = (SORT_WORDS.keys ++ TOP_WORDS ++ POST_WORDS ++ FUZZY_NUMS.keySet).toSet

    /**
      * Stemmatizes map's keys.
      *
      * @param m Map.
      */
    private def stemmatizeWords[T](m: Map[String, T]): Map[String, T] = m.map(p ⇒ NCNlpCoreManager.stem(p._1) → p._2)

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
      * Starts this component.
      */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
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
        def isImportant(t: NCNlpSentenceToken): Boolean = isUserNotValue(t) || TECH_WORDS.contains(t.stem)

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
            val notes = mutable.HashSet.empty[NCNlpSentenceNote]
            val numsMap = NCNumericManager.find(ns).filter(_.unit.isEmpty).map(p ⇒ p.tokens → p).toMap
            val groupsMap = groupNums(ns, numsMap.values)

            // Tries to grab tokens reverse way.
            // Example: A, B, C ⇒ ABC, BC, AB .. (BC will be processed first)
            for (toks ← ns.tokenMixWithStopWords().sortBy(p ⇒ (-p.size, -p.head.index)) if validImportant(ns, toks))
                tryToMatch(numsMap, groupsMap, toks) match {
                    case Some(m) ⇒
                        for (refNote ← m.refNotes) {
                            val params = mutable.ArrayBuffer.empty[(String, Any)]

                            params += "limit" → m.limit
                            params += "indexes" → m.refIndexes
                            params += "note" → refNote

                            if (m.asc.isDefined)
                                params += "asc" → m.asc.get

                            val note = NCNlpSentenceNote(m.matched.map(_.index), TOK_ID, params: _*)

                            if (!notes.exists(n ⇒ ns.notesEqualOrSimilar(n, note))) {
                                notes += note

                                m.matched.foreach(_.add(note))
                            }
                        }
                    case None ⇒ // No-op.
                }
        }

    /**
      *
      * @param toks
      */
    private def getCommonNotes(toks: Seq[NCNlpSentenceToken]): Set[String] =
        if (toks.isEmpty)
            Set.empty
        else {
            def getCommon(sortedToks: Seq[NCNlpSentenceToken]): Set[String] = {
                require(sortedToks.nonEmpty)

                val h = sortedToks.head
                val l = sortedToks.last

                h.filter(!_.isNlp).filter(n ⇒ h.index == n.tokenFrom && l.index == n.tokenTo).map(_.noteType).toSet
            }

            var sortedToks = toks.sortBy(_.index)

            var res = getCommon(sortedToks)

            if (res.isEmpty) {
                sortedToks = sortedToks.filter(!_.isStopWord)

                if (sortedToks.nonEmpty)
                    res = getCommon(sortedToks)
            }

            if (res.isEmpty) Set.empty else res
        }

    /**
      *
      * @param numsMap
      * @param groupsMap
      * @param toks
      */
    private def tryToMatch(
        numsMap: Map[Seq[NCNlpSentenceToken], NCNumeric],
        groupsMap: Map[Seq[NCNlpSentenceToken], GroupsHolder],
        toks: Seq[NCNlpSentenceToken]
    ): Option[Match] = {
        val i1 = toks.head.index
        val i2 = toks.last.index

        val refCands = toks.filter(_.exists(n ⇒ isUserNotValue(n) && n.tokenIndexes.head >= i1 && n.tokenIndexes.last <= i2))

        // Reference should be last.
        if (refCands.nonEmpty && refCands.last.index == toks.last.index) {
            val commonRefNotes = getCommonNotes(refCands)

            if (commonRefNotes.nonEmpty) {
                val matchCands = toks.diff(refCands)
                val idxs = refCands.map(_.index)

                def try0(group: Seq[NCNlpSentenceToken]): Option[Match] =
                    groupsMap.get(group) match {
                        case Some(h) ⇒
                            if (LIMITS.contains(h.value) || h.isFuzzyNum)
                                Some(Match(h.limit, Some(h.asc), matchCands, commonRefNotes, idxs.asJava))
                            else
                                numsMap.get(group) match {
                                    case Some(num) ⇒ Some(Match(num.value, None, matchCands, commonRefNotes, idxs.asJava))
                                    case None ⇒ None
                                }
                        case None ⇒ None
                    }

                try0(matchCands) match {
                    case Some(m) ⇒ Some(m)
                    case None ⇒ try0(matchCands.filter(!_.isStopWord))
                }
            }
            else
                None
        }
        else
            None
    }

    /**
      *
      * @param ns
      * @param nums
      * @return
      */
    private def groupNums(ns: NCNlpSentence, nums: Iterable[NCNumeric]): Map[Seq[NCNlpSentenceToken], GroupsHolder] = {
        val numsMap = nums.map(n ⇒ n.tokens → n).toMap

        // All groups combinations.
        val tks2Nums: Seq[(NCNlpSentenceToken, Option[Int])] = ns.filter(!_.isStopWord).map(t ⇒ t → FUZZY_NUMS.get(t.stem))

        // Tokens: A;  B;  20;  C;  twenty; two, D
        // NERs  : -;  -;  20;  -;  22;     22;  -
        // Groups: (A) → -; (B) → -; (20) → 20; (C) → -; (twenty, two) → 22; (D) → -;
        val groups: Seq[Group] = tks2Nums.zipWithIndex.groupBy { case ((_, numOpt), idx) ⇒
            // Groups by artificial flag.
            // Flag is first index of independent token.
            // Tokens:  A;  B;  20;  C;  twenty; two, D
            // Indexes  0;  1;  2;   3;  4;      4;   6
            if (idx == 0)
                0
            else {
                // Finds last another.
                var i = idx

                while (i > 0 && numOpt.isDefined && tks2Nums(i - 1)._2 == numOpt)
                    i = i - 1

                i
            }
        }.
            // Converts from artificial group to tokens groups (Seq[Token], Option[Int])
            map { case (_, gs) ⇒ gs.map { case (seq, _) ⇒ seq } }.
            map(seq ⇒ {
                val toks = seq.map { case (t, _) ⇒ t }
                var numOpt = seq.head._2
                val isFuzzyNum = numOpt.nonEmpty

                if (numOpt.isEmpty)
                    numOpt = numsMap.get(toks) match {
                        case Some(num) ⇒ Some(num.value.intValue())
                        case None ⇒ None
                    }

                Group(toks, numOpt, isFuzzyNum)
            }).
            // Converts to sequence and sorts.
            toSeq.sortBy(_.index)

        (for (n ← groups.length until 0 by -1) yield groups.sliding(n).map(GroupsHolder)).
            flatten.
            map(p ⇒ p.tokens → p).
            toMap
    }
}
