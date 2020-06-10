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

package org.apache.nlpcraft.server.nlp.enrichers.stopword

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.nlp.pos.NCPennTreebank
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote, NCNlpSentenceToken}
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher._

import scala.annotation.tailrec
import scala.collection.mutable

/**
 * Stop-word and stop-sentence enricher.
 */
object NCStopWordEnricher extends NCServerEnricher {
    // Condition types.
    type Wildcard = (String, String)
    type Word = String

    /** All POSes set. http://www.clips.ua.ac.be/pages/mbsp-tags */
    private final val POSES = Set(
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

    private final val STOP_BEFORE_STOP: Seq[Word] = Seq("DT", "PRP", "PRP$", "WDT", "WP", "WP$", "WRB")

    private final val PERCENTS = Set(
        "%",
        "pct",
        "pc",
        "percentage",
        "proportion",
        "interest",
        "rate",
        "percent"
    ).map(NCNlpCoreManager.stem)

    @volatile private var POSSESSIVE_WORDS: Set[String] = _
    @volatile private var FIRST_WORDS: Set[String] = _
    @volatile private var NOUN_WORDS: Set[String] = _

    @volatile private var STOP_WORDS: StopWordHolder = _
    @volatile private var EXCEPTIONS: StopWordHolder = _

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
    ) {
        def matches(s: String, posOpt: Option[String]): Boolean =
            posOpt match {
                case Some(pos) ⇒
                    !excludes.getOrElse(pos, Set.empty).contains(s) &&
                    (any.contains(s) || includes.getOrElse(pos, Set.empty).contains(s))
                case _ ⇒ any.contains(s)
            }
    }

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
    ) {
        require(!any.exists { case (begin, end) ⇒ begin.isEmpty && end.isEmpty })

        // Optimization for full wildcard cases (configurations like * | DT)
        private val inclPoses = filterPoses(includes)
        private val exclPoses = filterPoses(excludes)

        private def filterPoses(m: Map[String, Set[Wildcard]]): Set[String] =
            m.filter { case(_, pair) ⇒ pair.exists { case (begin, end) ⇒ begin.isEmpty && end.isEmpty } }.keySet

        private def matches(s: String, set: Set[Wildcard]): Boolean =
            set.exists { case (b, e) ⇒ (b.isEmpty || s.startsWith(b)) && (e.isEmpty || s.endsWith(e)) }

        def matches(s: String, posOpt: Option[String]): Boolean =
            if (s.contains(' '))
                false
            else
                posOpt match {
                    case Some(pos) ⇒
                        !exclPoses.contains(pos) &&
                        !matches(s, excludes.getOrElse(pos, Set.empty)) &&
                        (
                            inclPoses.contains(pos) ||
                            matches(s, any) ||
                            matches(s, includes.getOrElse(pos, Set.empty))
                        )
                    case _ ⇒ throw new AssertionError(s"Unexpected missed POS.")
                }
    }

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
    ) {
        def matches(toks: Seq[NCNlpSentenceToken]): Boolean = {
            val posOpt =
                toks.size match {
                    case 0 ⇒ throw new AssertionError(s"Unexpected empty tokens.")
                    case 1 ⇒ Some(toks.head.pos)
                    case _ ⇒ None
                }

            // Hash access.
            stems.matches(toStemKey(toks), posOpt) ||
            lemmas.matches(toLemmaKey(toks), posOpt) ||
            origins.matches(toOriginalKey(toks), posOpt) ||
            // Scan access.
            wildcardsLemmas.matches(toLemmaKey(toks), posOpt) ||
            wildcardsOrigins.matches(toOriginalKey(toks), posOpt)
        }
    }

    /**
     * Parses configuration template.
     *
     * @param lines Configuration file content.
     * @return Holder and `is-exception` flag.
     */
    @throws[NCE]
    private def readStopWords(lines: Seq[String]): Map[Boolean, StopWordHolder] = {
        // 1. Prepares accumulation data structure.
        object WordForm extends Enumeration {
            type WordForm = Value

            val STEM, LEM, ORIG = Value
        }

        import WordForm._

        class Condition[T] {
            val any: mutable.HashSet[T] = mutable.HashSet.empty[T]
            val includes: mutable.HashMap[String, mutable.HashSet[T]] = mutable.HashMap.empty[String, mutable.HashSet[T]]
            val excludes: mutable.HashMap[String, mutable.HashSet[T]] = mutable.HashMap.empty[String, mutable.HashSet[T]]

            def addCondition(cond: T, poses: Map[String, Boolean]): Any =
                if (poses.isEmpty)
                    any += cond
                else {
                    def add(m: mutable.HashMap[String, mutable.HashSet[T]], incl: Boolean): Unit =
                        poses.filter { case (_, isIncl) ⇒ isIncl == incl }.keys.foreach(pos ⇒
                            m.get(pos) match {
                                case Some(set) ⇒ set.add(cond)
                                case _ ⇒
                                    val set = mutable.HashSet.empty[T]

                                    set += cond

                                    m += pos → set
                            }
                        )

                    add(includes, incl = true)
                    add(excludes, incl = false)
                }
        }

        def mkEntry[T](f: WordForm, mkT: Unit ⇒ T, isExc: Boolean):((Boolean, WordForm), T) = (isExc, f) → mkT(())
        def mkMap[T](mkT: Unit ⇒ T): Map[(Boolean, WordForm), T] =
            WordForm.values.flatMap(f ⇒ Map(mkEntry(f, mkT, isExc = true), mkEntry(f, mkT, isExc = false))).toMap

        // Prepares collections.
        val mHash = mkMap(_ ⇒ new Condition[Word]())
        val mScan = mkMap(_ ⇒ new Condition[Wildcard]())

        // 2. Accumulates data of each parsed line.
        for (line ← lines) {
            @throws[NCE]
            def throwError(msg: String): Unit =
                throw new NCE(s"Invalid stop word configuration [line=$line, reason=$msg]")

            var s = line.trim

            // Word with size 1 word should contains letter only.
            if (s.length == 1 && !s.head.isLetter)
                throwError("Invalid stop word")

            @throws[NCE]
            def checkSingle(ch: Char): Unit = if (s.count(_ == ch) > 1) throwError(s"Unexpected symbols count: $ch")

            // Confusing special symbols.
            checkSingle('@')
            checkSingle('|')
            checkSingle('*')

            val isExc = line.head == '~'

            if (isExc)
                s = line.drop(1)

            val idxPos = s.indexOf("|")

            val poses: Map[String, Boolean] =
                if (idxPos > 0)
                    s.
                        drop(idxPos + 1).
                        trim.split(" ").
                        map(_.trim.toUpperCase).
                        filter(!_.isEmpty).
                        toSeq.
                        map(p ⇒ if (p.head == '~') p.drop(1).trim → false else p → true).
                        toMap
                else
                    Map.empty

            if (!poses.keys.forall(POSES.contains))
                throwError(s"Invalid POSes: ${poses.keys.mkString(", ")}")

            val hasPoses = poses.nonEmpty

            if (hasPoses)
                s = s.take(idxPos).trim

            val isMultiWord = s.contains(' ')

            // Confusing POSes.
            if (poses.nonEmpty && isMultiWord)
                throwError("POSes cannot be defined for multiple stop words.")

            var isCase = false

            if (s.head == '@') {
                s = s.drop(1)

                // Empty word.
                if (s.isEmpty)
                    throwError("Empty word")

                isCase = true
            }

            val idxWild = s.indexOf("*")

            if (idxWild >= 0 && isMultiWord)
                throwError("Wildcard cannot be defined for multiple stop words.")

            if (idxWild < 0) {
                val (word, form) =
                    if (isCase)
                        (s, ORIG)
                    else {
                        if (!hasPoses) (NCNlpCoreManager.stem(s), STEM) else (NCNlpCoreManager.stem(s), LEM)
                    }

                mHash((isExc, form)).addCondition(word, poses)
            }
            else {
                val b = s.take(idxWild)
                val e = s.drop(idxWild + 1)

                if (b.isEmpty && e.isEmpty && !hasPoses)
                    throwError("Too general wildcard definition.")

                mScan((isExc, if (isCase) ORIG else LEM)).addCondition((b, e), poses)
            }
        }

        // 3. Converts data to service format.
        def toImmutable[T](m: mutable.HashMap[String, mutable.HashSet[T]]): Map[String, Set[T]] = m.map(p ⇒ p._1 → p._2.toSet).toMap

        Seq(true, false).map(isExc ⇒ {
            def mkHolder[T, R](
                m: Map[(Boolean, WordForm), Condition[T]],
                form: WordForm,
                mkInstance: (Set[T], Map[String, Set[T]], Map[String, Set[T]]) ⇒ R): R = {

                val any = m((isExc, form)).any.toSet
                val incl = toImmutable(m((isExc, form)).includes)
                val excl = toImmutable(m((isExc, form)).excludes)

                mkInstance(any ++ excl.values.flatten, incl, excl)
            }

            def mkHash(form: WordForm): HashHolder = mkHolder(mHash, form, HashHolder)
            def mkScan(form: WordForm): ScanHolder = mkHolder(mScan, form, ScanHolder)

            isExc → StopWordHolder(mkHash(STEM), mkHash(LEM), mkHash(ORIG), mkScan(LEM), mkScan(ORIG))
        }).toMap
    }

    private def isVerb(pos: String): Boolean = pos.head == 'V'

    /**
     * Marks words before stop words.
     *
     * @param ns Sentence.
     * @param stopPoses Stop POSes.
     * @param lastIdx Last index.
     * @param isException Function which return `stop word exception` flag.
     */
    @tailrec
    private def markBefore(
        ns: NCNlpSentence, stopPoses: Seq[String], lastIdx: Int, isException: Seq[NCNlpSentenceToken] ⇒ Boolean
    ): Boolean = {
        var stop = true

        for (
            (tok, idx) ← ns.zipWithIndex
            if idx != lastIdx &&
                !tok.isStopWord &&
                !isException(Seq(tok)) &&
                stopPoses.contains(tok.pos) &&
                ns(idx + 1).isStopWord) {
            ns.fixNote(tok.getNlpNote, "stopWord" → true)

            stop = false
        }

        if (stop) true else markBefore(ns, stopPoses, lastIdx, isException)
    }

    /**
     * Checks value cached or not.
     *
     * @param toks Tokens.
     * @param cache Cache map.
     * @param get Calculation method based on given tokens.
     */
    private def exists(toks: Seq[NCNlpSentenceToken], cache: mutable.HashMap[Seq[NCNlpSentenceToken], Boolean], get: Seq[NCNlpSentenceToken] ⇒ Boolean): Boolean =
        cache.get(toks) match {
            case Some(b) ⇒ b
            case None ⇒
                val b = get(toks)

                cache += toks → b

                b
        }

    /**
      *
      * @param ns NLP sentence.
      */
    @throws[NCE]
    private def processBrackets(ns: NCNlpSentence): Unit = {
        val backup = ns.clone()

        def process(): Unit =
            ns.filter(!_.isQuoted).find(isBR) match {
                case Some(t) ⇒
                    // Invalid sentence if first bracket is right.
                    if (isRBR(t))
                        throw new NCE("Invalid left bracket")

                    // Clone input sentence.
                    val copy = ns.clone()

                    // Clear the tokens in the input sentence.
                    ns.clear()

                    var inBrackets = false
                    var level = 0

                    def getIndex = ns.size

                    val buf = mutable.Buffer.empty[NCNlpSentenceToken]

                    for (tok ← copy) {
                        if (isLBR(tok) && !tok.isQuoted) {
                            inBrackets = true

                            level += 1
                        }

                        if (inBrackets)
                            buf += tok
                        else {
                            val idx = getIndex
                            val newTok = tok.clone(idx)

                            def replace(nt: String): Unit =
                                newTok.getNotes(nt).map(n ⇒ (n, n.clone(Seq(idx), Seq(idx)))).foreach(p ⇒ {
                                    newTok.remove(p._1)
                                    newTok.add(p._2)
                                })

                            // They called before stopword enricher.
                            replace("nlpcraft:nlp")

                            // NLP note special case because has index field.
                            ns += newTok

                            ns.fixNote(newTok.getNlpNote, "index" → idx)
                        }

                        if (isRBR(tok) && !tok.isQuoted)
                            if (!inBrackets)
                                throw new NCE("Invalid right bracket")
                            else {
                                level -= 1

                                // It has to take into account correctness of nested brackets,
                                // because it needs to recognize suitable right bracket.
                                if (level == 0)
                                    inBrackets = false

                                if (!inBrackets) {
                                    val origText = mkSumString(buf, (t: NCNlpSentenceToken) ⇒ t.origText)

                                    val head = buf.head
                                    val last = buf.last
                                    val idx = getIndex

                                    require(buf.size >= 2)

                                    val body = buf.slice(1, buf.length - 2 + 1)
                                    val isBodyQuoted = body.nonEmpty && body.forall(_.isQuoted)

                                    val note = NCNlpSentenceNote(
                                        Seq(idx),
                                        "nlpcraft:nlp",
                                        "pos" → NCPennTreebank.SYNTH_POS,
                                        "posDesc" → NCPennTreebank.SYNTH_POS_DESC,
                                        "lemma" → mkSumString(buf, (t: NCNlpSentenceToken) ⇒ t.lemma),
                                        "origText" → origText,
                                        "normText" → mkSumString(buf, (t: NCNlpSentenceToken) ⇒ t.normText),
                                        "stem" → mkSumString(buf, (t: NCNlpSentenceToken) ⇒ t.stem),
                                        "start" → head.startCharIndex,
                                        "end" → last.endCharIndex,
                                        "charLength" → origText.length,
                                        "quoted" → isBodyQuoted,
                                        "stopWord" → false,
                                        "bracketed" → true,
                                        "direct" → buf.forall(_.isDirect)
                                    )

                                    val newTok = NCNlpSentenceToken(idx)

                                    newTok.add(note)

                                    ns += newTok

                                    buf.clear()
                                }
                            }
                    }

                    if (inBrackets)
                        throw new NCE("Missed right bracket")

                // Sentence doesn't contain any brackets.
                case None ⇒ // No-op.
            }

        try
            process()
        catch {
            case e: NCE ⇒
                logger.trace(s"Brackets processing error: ${e.getMessage}")

                ns.clear()
                ns ++= backup
        }
    }

    @throws[NCE]
    override def enrich(ns: NCNlpSentence, parent: Span = null) {
        // This stage must not be 1st enrichment stage.
        assume(ns.nonEmpty)
    
        startScopedSpan("enrich", parent, "srvReqId" → ns.srvReqId, "txt" → ns.text) { _ ⇒
            // +---------------------------------+
            // | Pass #1.                        |
            // | Brackets processing.            |
            // +---------------------------------+
            // Stop words notes are not set for each note yet.
            processBrackets(ns)
            // Stop words and exceptions caches for this sentence.
            val cacheSw = mutable.HashMap.empty[Seq[NCNlpSentenceToken], Boolean]
            val cacheEx = mutable.HashMap.empty[Seq[NCNlpSentenceToken], Boolean]
    
            def isStop(toks: Seq[NCNlpSentenceToken]): Boolean = exists(toks, cacheSw, STOP_WORDS.matches)
    
            def isException(toks: Seq[NCNlpSentenceToken]): Boolean = exists(toks, cacheEx, EXCEPTIONS.matches)
    
            for (p ← ns.zipWithIndex) {
                val tok = p._1
                val idx = p._2
                val pos = tok.pos
                val lemma = tok.lemma
                val stem = tok.stem
        
                def isFirst: Boolean = idx == 0
                def isLast: Boolean = idx == ns.length - 1
        
                def next(): NCNlpSentenceToken = ns(idx + 1)
                def prev(): NCNlpSentenceToken = ns(idx - 1)
        
                def isCommonVerbs(firstVerb: String, secondVerb: String): Boolean =
                    isVerb(pos) && lemma == secondVerb ||
                        (isVerb(pos) && lemma == firstVerb && !isLast && isVerb(next().pos) && next().lemma == secondVerb)
        
                // +---------------------------------+
                // | Pass #2.                        |
                // | POS tags and manual resolution. |
                // +---------------------------------+
                val stop =
                !tok.isQuoted && !isException(Seq(tok)) &&
                    (// Percents after numbers.
                        // 1. Word from 'percentage' list.
                        PERCENTS.contains(stem) &&
                            // 2. Number before.
                            !isFirst && prev().pos == "CD" &&
                            // 3. It's last word or any words after except numbers.
                            (isLast || next().pos != "CD")
                        ) ||
                    // be, was, is etc. or has been etc.
                    isCommonVerbs("have", "be") ||
                    // be, was, is etc. or have done etc.
                    isCommonVerbs("have", "do")
                if (stop)
                    ns.fixNote(tok.getNlpNote, "stopWord" → true)
            }
            // +--------------------------------------+
            // | Pass #3.                             |
            // | Find all words from predefined list. |
            // +--------------------------------------+
            val buf = mutable.Buffer.empty[Seq[NCNlpSentenceToken]]
            val mix = ns.tokenMixWithStopWords()
            
            for (toks ← mix if !buf.exists(_.containsSlice(toks)) && isStop(toks) && !isException(toks)) {
                toks.foreach(tok ⇒ ns.fixNote(tok.getNlpNote, "stopWord" → true))
                buf += toks
            }
            
            // Capture the token mix at this point minus the initial stop words found up to this point.
            val origToks: Seq[(Seq[NCNlpSentenceToken], String)] = (for (toks ← mix) yield toks).map(s ⇒ s → toStemKey(s))
    
            // +--------------------------------------------+
            // | Pass #4.                                   |
            // | Check external possessive stop-word file.  |
            // +--------------------------------------------+
            for (tup ← origToks; key = tup._2 if POSSESSIVE_WORDS.contains(key) && !isException(tup._1))
                tup._1.foreach(tok ⇒ ns.fixNote(tok.getNlpNote, "stopWord" → true))
            
            // +--------------------------------------------------+
            // | Pass #5.                                         |
            // | Check for sentence beginners from external file. |
            // +--------------------------------------------------+
            
            val foundKeys = new mutable.HashSet[String]()
            
            // All sentence first stop words + first non stop word.
            val startToks = ns.takeWhile(_.isStopWord) ++ ns.find(!_.isStopWord).map(p ⇒ p)
            for (startTok ← startToks; tup ← origToks.filter(_._1.head == startTok); key = tup._2
                if FIRST_WORDS.contains(key) && !isException(tup._1)) {
                tup._1.foreach(tok ⇒ ns.fixNote(tok.getNlpNote, "stopWord" → true))
                foundKeys += key
            }
    
            // +-------------------------------------------------+
            // | Pass #6.                                        |
            // | Check for sentence beginners with ending nouns. |
            // +-------------------------------------------------+
            for (tup ← origToks; key = tup._2 if !foundKeys.contains(key) && !isException(tup._1))
                foundKeys.find(key.startsWith) match {
                    case Some(s) ⇒
                        if (NOUN_WORDS.contains(key.substring(s.length).trim))
                            tup._1.foreach(tok ⇒ ns.fixNote(tok.getNlpNote, "stopWord" → true))
                    case None ⇒ ()
                }
    
            // +-------------------------------------------------+
            // | Pass #7.                                        |
            // | Mark words with POSes before stop-words.        |
            // +-------------------------------------------------+
            markBefore(ns, STOP_BEFORE_STOP, ns.size - 1, isException)
        }
    }

    @throws[NCE]
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        // Stemmatization is done already by generator.
        POSSESSIVE_WORDS = U.readTextGzipResource("stopwords/possessive_words.txt.gz", "UTF-8", logger).toSet
        FIRST_WORDS = U.readTextGzipResource("stopwords/first_words.txt.gz", "UTF-8", logger).toSet
        NOUN_WORDS = U.readTextGzipResource("stopwords/noun_words.txt.gz", "UTF-8", logger).toSet

        // Case sensitive.
        val m =
            readStopWords(
                U.readResource("stopwords/stop_words.txt", "UTF-8", logger).
                    map(_.trim).filter(s ⇒ !s.isEmpty && !s.startsWith("#")).toSeq
            )

        STOP_WORDS = m(false)
        EXCEPTIONS = m(true)

        super.start()
    }
    
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
}