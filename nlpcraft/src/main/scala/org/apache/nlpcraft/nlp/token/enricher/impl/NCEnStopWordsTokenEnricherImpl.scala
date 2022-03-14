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

package org.apache.nlpcraft.nlp.token.enricher.impl

import com.typesafe.scalalogging.LazyLogging
import opennlp.tools.stemmer.PorterStemmer
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticStemmer

import java.io.*
import java.util
import java.util.{List as JList, Set as JSet}
import scala.annotation.tailrec
import scala.collection.{IndexedSeq, Seq, mutable}
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*

/**
  *
  */
object NCEnStopWordsTokenEnricherImpl:
    // Condition types.
    type Wildcard = (String, String)
    type Word = String

    /** All POSes set. http://www.clips.ua.ac.be/pages/mbsp-tags */
    private val POSES = Set(
        "CC",
        "CD",
        "DT",
        "EX",
        "FW",
        "IN",
        "JJ",
        "JJR",
        "JJS",
        "LS",
        "MD",
        "NN",
        "NNS",
        "NNP",
        "NNPS",
        "PDT",
        "POS",
        "PRP",
        "PRP$",
        "RB",
        "RBR",
        "RBS",
        "RP",
        "SYM",
        "TO",
        "UH",
        "VB",
        "VBZ",
        "VBP",
        "VBD",
        "VBN",
        "VBG",
        "WDT",
        "WP",
        "WP$",
        "WRB",
        ".",
        ",",
        ":",
        "(",
        ")",
        "--" // Synthetic POS.
    )

    private val STOP_BEFORE_STOP: Seq[Word] = Seq("DT", "PRP", "PRP$", "WDT", "WP", "WP$", "WRB")
    private val Q_POS = Set("``", "''")
    private val PERCENTS = Set(
        "%",
        "pct",
        "pc",
        "percentage",
        "proportion",
        "interest",
        "rate",
        "percent"
    )

    private def getPos(t: NCToken): String = t.getOpt("pos").orElseThrow(() => throw new NCException(s"POS not found in token: ${t.keysSet()}"))
    private def getLemma(t: NCToken): String = t.getOpt("lemma").orElseThrow(() => throw new NCException(s"Lemma not found in token: ${t.keysSet()}"))
    private def isQuote(t: NCToken): Boolean = Q_POS.contains(getPos(t))
    private def toLemmaKey(toks: Seq[NCToken]): String = toks.map(getLemma).mkString(" ")
    private def toValueKey(toks: Seq[NCToken]): String = toks.map(_.getText.toLowerCase).mkString(" ")
    private def toOriginalKey(toks: Seq[NCToken]): String = toks.map(_.getText).mkString(" ")
    private def isStopWord(t: NCToken): Boolean = t.getOpt[Boolean]("stopword").orElse(false)

    /**
      * Gets all sequential permutations of tokens in this NLP sentence.
      * This method is like a 'tokenMix', but with all combinations of stop-words (with and without)
      *
      * @param tokens Tokens.
      * @param maxLen Maximum number of tokens in the sequence.
      */
    // TODO: private[impl]
    def tokenMixWithStopWords(tokens: Seq[NCToken], maxLen: Int = Integer.MAX_VALUE): Seq[Seq[NCToken]] =
        /**
          * Gets all combinations for sequence of mandatory tokens with stop-words and without.
          *
          * Example:
          * 'A (stop), B, C(stop) -> [A, B, C]; [A, B]; [B, C], [B]
          * 'A, B(stop), C(stop) -> [A, B, C]; [A, B]; [A, C], [A].
          *
          * @param toks Tokens.
          */
        def permutations(toks: Seq[NCToken]): Seq[Seq[NCToken]] =
            def multiple(seq: Seq[Seq[Option[NCToken]]], t: NCToken): Seq[Seq[Option[NCToken]]] =
                if seq.isEmpty then
                    if isStopWord(t) then IndexedSeq(IndexedSeq(Option(t)), IndexedSeq(None)) else IndexedSeq(IndexedSeq(Option(t)))
                else
                    (for (subSeq <- seq) yield subSeq :+ Option(t)) ++ (if isStopWord(t) then for (subSeq <- seq) yield subSeq :+ None else Seq.empty)

            var res: Seq[Seq[Option[NCToken]]] = Seq.empty
            for (t <- toks) res = multiple(res, t)
            res.map(_.flatten).filter(_.nonEmpty)

        tokenMix(tokens, maxLen).
            flatMap(permutations).
            filter(_.nonEmpty).
            distinct.
            sortBy(seq => (-seq.length, seq.head.getIndex))

    /**
      * Gets all sequential permutations of tokens in this NLP sentence.
      *
      * For example, if NLP sentence contains "a, b, c, d" tokens, then
      * this function will return the sequence of following token sequences in this order:
      * "a b c d"
      * "a b c"
      * "b c d"
      * "a b"
      * "b c"
      * "c d"
      * "a"
      * "b"
      * "c"
      * "d"
      *
      * NOTE: this method will not return any permutations with a quoted token.
      *
      * @param toks Tokens.
      * @param maxLen Maximum number of tokens in the sequence.
      */
    private def tokenMix(toks: Seq[NCToken], maxLen: Int = Integer.MAX_VALUE): Seq[Seq[NCToken]] =
        (for (n <- toks.length until 0 by -1 if n <= maxLen) yield toks.sliding(n)).flatten

import org.apache.nlpcraft.nlp.token.enricher.impl.NCEnStopWordsTokenEnricherImpl.*

/**
  *
  * @param addStopsSet
  * @param exclStopsSet
  */
class NCEnStopWordsTokenEnricherImpl(addStopsSet: JSet[String], exclStopsSet: JSet[String]) extends NCTokenEnricher with LazyLogging:
    private final val stemmer = new PorterStemmer

    private var addStems: Set[String] = _
    private var exclStems: Set[String] = _
    private var percents: Set[String] = _
    private var firstWords: Set[String] = _
    private var nounWords: Set[String] = _
    private var stopWords: StopWordHolder = _
    private var exceptions: StopWordHolder = _

    init()

    private def read(path: String): Set[String] = NCUtils.readTextGzipResource(path, "UTF-8", logger).toSet
    private def stem(s: String): String = stemmer.stem(s.toLowerCase)
    private def toStemKey(toks: Seq[NCToken]): String = toks.map(_.getText).map(stem).mkString(" ")

    /**
      * Stop words holder, used for hash search.
      *
      * @param any Any POSes container.
      * @param includes Included by POS container.
      * @param excludes Excluded by POS container.
      */
    private case class HashHolder(
        any: Set[Word],
        includes: Map[String, Set[Word]],
        excludes: Map[String, Set[Word]]
    ):
        def matches(s: String, posOpt: Option[String]): Boolean =
            posOpt match
                case Some(pos) =>
                    !excludes.getOrElse(pos, Set.empty).contains(s) &&
                        (any.contains(s) || includes.getOrElse(pos, Set.empty).contains(s))
                case _ => any.contains(s)

    /**
      * Stop words holder, used for scanning.
      *
      * @param any Any POSes container.
      * @param includes Included by POS container.
      * @param excludes Excluded by POS container.
      */
    private case class ScanHolder(
        any: Set[Wildcard],
        includes: Map[String, Set[Wildcard]],
        excludes: Map[String, Set[Wildcard]]
    ):
        require(!any.exists { (begin, end) => begin.isEmpty && end.isEmpty })

        // Optimization for full wildcard cases (configurations like * | DT)
        private val inclPoses = filterPoses(includes)
        private val exclPoses = filterPoses(excludes)

        private def filterPoses(m: Map[String, Set[Wildcard]]): Set[String] =
            m.filter { (_, pair) => pair.exists { (begin, end) => begin.isEmpty && end.isEmpty } }.keySet

        private def matches(s: String, set: Set[Wildcard]): Boolean =
            set.exists { (b, e) => (b.isEmpty || s.startsWith(b)) && (e.isEmpty || s.endsWith(e)) }

        def matches(s: String, posOpt: Option[String]): Boolean =
            if s.contains(' ') then
                false
            else
                posOpt match
                    case Some(pos) =>
                        !exclPoses.contains(pos) &&
                            !matches(s, excludes.getOrElse(pos, Set.empty)) &&
                            (
                                inclPoses.contains(pos) ||
                                    matches(s, any) ||
                                    matches(s, includes.getOrElse(pos, Set.empty))
                                )
                    case _ => throw new AssertionError("Unexpected POS.")

    /**
      * Stop words data holder.
      *
      * @param stems Stems data holder.
      * @param lemmas Lemmas data holder.
      * @param origins Origins data holder.
      * @param wildcardsLemmas Wildcards lemmas data holder.
      * @param wildcardsOrigins Wildcards origins data holder.
      */
    private case class StopWordHolder(
        stems: HashHolder,
        lemmas: HashHolder,
        origins: HashHolder,
        wildcardsLemmas: ScanHolder,
        wildcardsOrigins: ScanHolder
    ):
        def matches(toks: Seq[NCToken]): Boolean =
            val posOpt = toks.size match
                case 0 => throw new AssertionError(s"Unexpected empty tokens.")
                case 1 => Option(getPos(toks.head))
                case _ => None

            // Hash access.
            stems.matches(toStemKey(toks), posOpt) ||
                lemmas.matches(toLemmaKey(toks), posOpt) ||
                origins.matches(toOriginalKey(toks), posOpt) ||
                // Scan access.
                wildcardsLemmas.matches(toLemmaKey(toks), posOpt) ||
                wildcardsOrigins.matches(toOriginalKey(toks), posOpt)

    /**
      * 
      */
    private def init(): Unit =
        addStems = if addStopsSet == null then Set.empty else addStopsSet.asScala.toSet.map(stem)
        exclStems = if exclStopsSet == null then Set.empty else exclStopsSet.asScala.toSet.map(stem)

        def check(name: String, set: Set[String]): Unit =
            if set.exists(_.exists(_.isWhitespace)) then E(s"$name contain a string with whitespaces.")

        check("Additional synonyms", addStems)
        check("Excluded synonyms", exclStems)

        val dups = addStems.intersect(exclStems)
        if dups.nonEmpty then E(s"Duplicate stems detected between additional and excluded stopwords [dups=${dups.mkString(",")}]")

        percents = PERCENTS.map(stem)

        // Stemmatization is done already by generator.
        NCUtils.execPar(
            () => firstWords = read("stopwords/first_words.txt.gz"),
            () => nounWords = read("stopwords/noun_words.txt.gz")
        )(ExecutionContext.Implicits.global)

        // Case sensitive.
        val m = readStopWords(
            NCUtils.readResource("stopwords/stop_words.txt", "UTF-8", logger)
            .map(_.strip).filter(s => s.nonEmpty && !s.startsWith("#"))
        )

        stopWords = m(false)
        exceptions = m(true)

    /**
      * Parses configuration template.
      *
      * @param lines Configuration file content.
      * @return Holder and `is-exception` flag.
      */
    private def readStopWords(lines: Seq[String]): Map[Boolean, StopWordHolder] =
        // 1. Prepares accumulation data structure.
        enum WordForm:
            case STEM, LEM, ORIG

        import WordForm.*

        class Condition[T]:
            val any = mutable.HashSet.empty[T]
            val incls = mutable.HashMap.empty[String, mutable.HashSet[T]]
            val excls = mutable.HashMap.empty[String, mutable.HashSet[T]]

            def addCondition(cond: T, poses: Map[String, Boolean]): Any =
                if poses.isEmpty then
                    any += cond
                else
                    def add(m: mutable.HashMap[String, mutable.HashSet[T]], incl: Boolean): Unit =
                        poses.filter { (_, isIncl) => isIncl == incl }.keys.foreach(pos =>
                            m.get(pos) match
                                case Some(set) => set.add(cond)
                                case _ =>
                                    val set = mutable.HashSet.empty[T]
                                        set += cond
                                        m += pos -> set
                        )

                    add(incls, incl = true)
                    add(excls, incl = false)

        type Key = (Boolean, WordForm)
        def mkMap[T](mkT: Unit => T): Map[Key, T] =
            val m = mutable.Map.empty[Key, T]
            def add(f: WordForm, mkT: Unit => T, isExc: Boolean): Unit =
                val tuple: (Key, T) = (isExc, f) -> mkT(())
                m += tuple._1 -> tuple._2
            WordForm.values.foreach(f =>
                add(f, mkT, isExc = true)
                    add(f, mkT, isExc = false)
            )
            m.toMap

        // Prepares collections.
        val mHash = mkMap(_ => new Condition[Word]())
        val mScan = mkMap(_ => new Condition[Wildcard]())

        // 2. Accumulates data of each parsed line.
        for (line <- lines)
            def throwError(msg: String): Unit = E(s"Invalid stop word configuration [line=$line, reason=$msg]") 

            var s = line.trim

            // Word with size 1 word should contains letter only.
            if s.length == 1 && !s.head.isLetter then throwError("Invalid stop word")

            def checkSingle(ch: Char): Unit = if s.count(_ == ch) > 1 then throwError(s"Unexpected symbols count: $ch")

            // Confusing special symbols.
            checkSingle('@')
            checkSingle('|')
            checkSingle('*')

            val isExc = line.head == '~'
            if isExc then s = line.drop(1)
            val idxPos = s.indexOf("|")
            val poses: Map[String, Boolean] =
                if idxPos > 0 then
                    s.
                        drop(idxPos + 1).
                        trim.split(" ").
                        map(_.trim.toUpperCase).
                        filter(_.nonEmpty).
                        toSeq.
                        map(p => if p.head == '~' then p.drop(1).strip -> false else p -> true).
                        toMap
                else
                    Map.empty

            if !poses.keys.forall(POSES.contains) then throwError(s"Invalid POSes: ${poses.keys.mkString(", ")}")
            val hasPoses = poses.nonEmpty
            if hasPoses then s = s.take(idxPos).trim
            val isMultiWord = s.contains(' ')

            // Confusing POSes.
            if poses.nonEmpty && isMultiWord then throwError("POSes cannot be defined for multiple stop words.")
            var isCase = false
            if s.head == '@' then
                s = s.drop(1)
                // Empty word.
                if s.isEmpty then throwError("Empty word.")
                isCase = true
            val idxWild = s.indexOf("*")
            if idxWild >= 0 && isMultiWord then throwError("Wildcard cannot be defined for multiple stop words.")
            if idxWild < 0 then
                val (word, form) =
                    if isCase then (s, ORIG)
                    else
                        if !hasPoses then (stem(s), STEM) else (stem(s), LEM)
                mHash((isExc, form)).addCondition(word, poses)
            else
                val b = s.take(idxWild)
                val e = s.drop(idxWild + 1)

                if b.isEmpty && e.isEmpty && !hasPoses then throwError("Too general wildcard definition.")
                mScan((isExc, if isCase then ORIG else LEM)).addCondition((b, e), poses)

        // 3. Converts data to service format.
        def toImmutable[T](m: mutable.HashMap[String, mutable.HashSet[T]]): Map[String, Set[T]] = m.map(p => p._1 -> p._2.toSet).toMap

        Seq(true, false).map(isExc =>
            def mkHolder[T, R](
                m: Map[(Boolean, WordForm), Condition[T]],
                form: WordForm,
                mkInstance: (Set[T], Map[String, Set[T]], Map[String, Set[T]]) => R
            ): R =
                val any = m((isExc, form)).any.toSet
                val incl = toImmutable(m((isExc, form)).incls)
                val excl = toImmutable(m((isExc, form)).excls)

                    mkInstance(any ++ excl.values.flatten, incl, excl)
            end mkHolder
            def mkHash(form: WordForm): HashHolder = mkHolder(mHash, form, HashHolder.apply)
            def mkScan(form: WordForm):
            ScanHolder = mkHolder(mScan, form, ScanHolder.apply)

                isExc -> StopWordHolder(mkHash(STEM), mkHash(LEM), mkHash(ORIG), mkScan(LEM), mkScan(ORIG))
        ).toMap

    private def isVerb(pos: String): Boolean = pos.head == 'V'

    /**
      * Marks words before stop words.
      *
      * @param ns Sentence.
      * @param stopPoses Stop POSes.
      * @param lastIdx Last index.
      * @param isException Function which return `stop word exception` flag.
      * @param stops Stopwords tokens.
      */
    @tailrec
    private def markBefore(
        ns: Seq[NCToken],
        stopPoses: Seq[String],
        lastIdx: Int,
        isException: Seq[NCToken] => Boolean,
        stops: mutable.HashSet[NCToken]
    ): Boolean =
        var stop = true

        for ((tok, idx) <- ns.zipWithIndex if idx != lastIdx && !isStopWord(tok) && !isException(Seq(tok)) &&
            stopPoses.contains(getPos(tok)) && isStopWord(ns(idx + 1)))
            stops += tok
            stop = false

        if stop then true else markBefore(ns, stopPoses, lastIdx, isException, stops)

    /**
      * Checks value cached or not.
      *
      * @param toks Tokens.
      * @param cache Cache map.
      * @param get Calculation method based on given tokens.
      */
    private def exists(toks: Seq[NCToken], cache: mutable.HashMap[Seq[NCToken], Boolean], get: Seq[NCToken] => Boolean): Boolean =
        cache.get(toks) match
            case Some(b) => b
            case None =>
                val b = get(toks)
                cache += toks -> b
                b

    /**
      * Marks as stopwords, words with POS from configured list, which also placed before another stop words.
      */
    private def processCommonStops(ns: Seq[NCToken], stops: mutable.HashSet[NCToken]): Unit =
        /**
          * Marks as stopwords, words with POS from configured list, which also placed before another stop words.
          */
        @tailrec
        def processCommonStops0(ns: Seq[NCToken]): Unit =
            val max = ns.size - 1
            var stop = true

            for ((tok, idx) <- ns.zipWithIndex if idx != max && !isStopWord(tok) && !exclStems.contains(stem(tok.getText)) &&
                POSES.contains(getPos(tok)) && isStopWord(ns(idx + 1)))
                stops += tok
                stop = false

            if !stop then processCommonStops0(ns)

        processCommonStops0(ns)

    override def enrich(req: NCRequest, cfg: NCModelConfig, toksList: JList[NCToken]): Unit =
        val toks = toksList.asScala

        // Stop words and exceptions caches for this sentence.
        val cacheSw = mutable.HashMap.empty[Seq[NCToken], Boolean]
        val cacheEx = mutable.HashMap.empty[Seq[NCToken], Boolean]

        def isStop(toks: Seq[NCToken]): Boolean = exists(toks, cacheSw, stopWords.matches)
        def isException(toks: Seq[NCToken]): Boolean = exists(toks, cacheEx, exceptions.matches)

        val stops = mutable.HashSet.empty[NCToken]

        for (tok <- toks)
            val idx = tok.getIndex
            val pos = getPos(tok)
            val lemma = getLemma(tok)
            val st = stem(tok.getText)

            def isFirst: Boolean = idx == 0
            def isLast: Boolean = idx == toks.length - 1
            def next(): NCToken = toks(idx + 1)
            def prev(): NCToken = toks(idx - 1)
            def isCommonVerbs(firstVerb: String, secondVerb: String): Boolean =
                isVerb(pos) && lemma == secondVerb ||
                    (isVerb(pos) && lemma == firstVerb && !isLast && isVerb(getPos(next())) && getLemma(next()) == secondVerb)

            // +---------------------------------+
            // | Pass #1.                        |
            // | POS tags and manual resolution. |
            // +---------------------------------+
            val stop = !isException(Seq(tok)) &&
                (// Percents after numbers.
                    // 1. Word from 'percentage' list.
                    percents.contains(st) &&
                        // 2. Number before.
                        !isFirst && getPos(prev()) == "CD" &&
                        // 3. It's last word or any words after except numbers.
                        (isLast || getPos(next()) != "CD")
                    ) ||
                // be, was, is etc. or has been etc.
                isCommonVerbs("have", "be") ||
                // be, was, is etc. or have done etc.
                isCommonVerbs("have", "do")
            if stop then stops += tok

        // +--------------------------------------+
        // | Pass #2.                             |
        // | Find all words from predefined list. |
        // +--------------------------------------+
        val buf = mutable.Buffer.empty[Seq[NCToken]]
        val mix = tokenMixWithStopWords(toks)

        for (toks <- mix if !buf.exists(_.containsSlice(toks)) && isStop(toks) && !isException(toks))
            toks.foreach(tok => stops += tok)
            buf += toks

        // Capture the token mix at this point minus the initial stop words found up to this point.
        val origToks: Seq[(Seq[NCToken], String)] =
            (for (toks <- mix) yield toks.toSeq).map(s => s -> toStemKey(s)).toSeq

        // +--------------------------------------------------+
        // | Pass #3.                                         |
        // | Check for sentence beginners from external file. |
        // +--------------------------------------------------+

        val foundKeys = new mutable.HashSet[String]()

        // All sentence first stop words + first non stop word.
        val startToks = toks.takeWhile(isStopWord) ++ toks.find(p => !isStopWord(p)).map(p => p)
        for (startTok <- startToks; tup <- origToks.filter(_._1.head == startTok); key = tup._2 if firstWords.contains(key) && !isException(tup._1))
            tup._1.foreach(tok => stops += tok)
            foundKeys += key

        // +-------------------------------------------------+
        // | Pass #4.                                        |
        // | Check for sentence beginners with ending nouns. |
        // +-------------------------------------------------+
        for (tup <- origToks; key = tup._2 if !foundKeys.contains(key) && !isException(tup._1))
            foundKeys.find(key.startsWith) match
                case Some(s) => if nounWords.contains(key.substring(s.length).strip) then tup._1.foreach(tok => stops += tok)
                case None => ()

        // +-------------------------------------------------+
        // | Pass #5.                                        |
        // | Mark words with POSes before stop-words.        |
        // +-------------------------------------------------+
        markBefore(toks, STOP_BEFORE_STOP, toks.size - 1, isException, stops)

        // +-------------------------------------------------+
        // | Pass #6.                                        |
        // | Processing additional and excluded stop words.  |
        // +-------------------------------------------------+
        for (t <- toks if addStems.contains(stem(t.getText)))
            stops += t

        for (t <- stops.filter(t => exclStems.contains(stem(t.getText))))
            stops -= t

        // +-------------------------------------------------+
        // | Pass #7.                                        |
        // | Marks as stopwords, words with POS from         |
        // | configured list, which also placed before       |
        // | another stop words.                             |
        // +-------------------------------------------------+
        processCommonStops(toks, stops)

        // +-------------------------------------------------+
        // | Pass #8.                                        |
        // | Deletes stop words if they are marked as quoted.|
        // +-------------------------------------------------+
        var quotes = toks.filter(isQuote)

        // Just ignore last odd quote.
        if quotes.size % 2 != 0 then quotes = quotes.reverse.drop(1).reverse

        // Start and end quote mustn't be same ("a` processed as valid)
        if quotes.nonEmpty then
            val m = toks.zipWithIndex.toMap
            val pairs = quotes.zipWithIndex.drop(1).flatMap {
                (t, idx) => Option.when(idx % 2 != 0)(m(t) -> m(quotes(idx - 1)))
            }
            stops --= stops.filter(t => pairs.exists { (from, to) =>
                val idx = m(t)
                from > idx && to < idx
            })

        // +-------------------------------------------------+
        // | Pass #9.                                        |
        // | Deletes stop words if they are brackets.        |
        // +-------------------------------------------------+
        val stack = new java.util.Stack[String]()
        val set = mutable.HashSet.empty[NCToken]
        var ok = true

        def check(expected: String): Unit = if stack.empty() || stack.pop() != expected then ok = false
        def mark(t: NCToken): Unit = if !stack.isEmpty then set += t

        for (t <- toks if ok)
            t.getText match
                case "(" | "{" | "[" | "<" => mark(t); stack.push(t.getText)
                case ")" => check("("); mark(t)
                case "}" => check("{"); mark(t)
                case "]" => check("["); mark(t)
                case ">" => check("<"); mark(t)
                case _ => mark(t)

        // Just ignore invalid brackets.
        if ok && stack.isEmpty then
            stops --= stops.intersect(set)

        toks.foreach(t => t.put("stopword", stops.contains(t)))