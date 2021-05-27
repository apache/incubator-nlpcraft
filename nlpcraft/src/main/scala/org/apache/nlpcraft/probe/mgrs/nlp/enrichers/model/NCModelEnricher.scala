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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.model

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.nlp.{NCNlpSentence ⇒ Sentence, NCNlpSentenceNote ⇒ NlpNote, NCNlpSentenceToken ⇒ NlpToken}
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.probe.mgrs.NCProbeSynonym.NCIdlContent
import org.apache.nlpcraft.probe.mgrs.NCProbeSynonymChunkKind.{NCSynonymChunkKind, _}
import org.apache.nlpcraft.probe.mgrs.nlp.NCProbeEnricher
import org.apache.nlpcraft.probe.mgrs.nlp.impl.NCRequestImpl
import org.apache.nlpcraft.probe.mgrs.sentence.NCSentenceManager
import org.apache.nlpcraft.probe.mgrs.{NCProbeModel, NCProbeVariants, NCProbeSynonym ⇒ Synonym}

import java.io.Serializable
import java.util
import java.util.{List ⇒ JList}
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsJava, MapHasAsScala, SeqHasAsJava}
import scala.collection.parallel.CollectionConverters._

/**
  * Model elements enricher.
  */
object NCModelEnricher extends NCProbeEnricher {
    type TokType = (NCToken, NCSynonymChunkKind)
    type Cache = mutable.Map[String, ArrayBuffer[Seq[Int]]]

    object Complex {
        def apply(t: NCToken): Complex =
            Complex(
                data = Left(t),
                isToken = true,
                isWord = false,
                token = t,
                word = null,
                origText = t.origText,
                wordIndexes = t.wordIndexes.toSet,
                minIndex = t.wordIndexes.head,
                maxIndex = t.wordIndexes.last
            )

        def apply(t: NlpToken): Complex =
            Complex(
                data = Right(t),
                isToken = false,
                isWord = true,
                token = null,
                word = t,
                origText = t.origText,
                wordIndexes = t.wordIndexes.toSet,
                minIndex = t.wordIndexes.head,
                maxIndex = t.wordIndexes.last
            )
    }

    case class Complex(
        data: NCIdlContent,
        isToken: Boolean,
        isWord: Boolean,
        token: NCToken,
        word: NlpToken,
        origText: String,
        wordIndexes: Set[Int],
        minIndex: Int,
        maxIndex: Int
    ) {
        private final val hash = if (isToken) Seq(wordIndexes, token.getId).hashCode() else wordIndexes.hashCode()

        override def hashCode(): Int = hash

        def isSubsetOf(minIndex: Int, maxIndex: Int, indexes: Set[Int]): Boolean =
            if (this.minIndex > maxIndex || this.maxIndex < minIndex)
                false
            else
                wordIndexes.subsetOf(indexes)

        override def equals(obj: Any): Boolean = obj match {
            case x: Complex =>
                hash == x.hash && (isToken && x.isToken && token == x.token || isWord && x.isWord && word == x.word)
            case _ => false
        }

        // Added for debug reasons.
        override def toString: String = {
            val idxs = wordIndexes.mkString(",")

            if (isToken && token.getId != "nlpcraft:nlp") s"'$origText' (${token.getId}) [$idxs]]" else s"'$origText' [$idxs]"
        }
    }

    object ComplexSeq {
        def apply(all: Seq[Complex]): ComplexSeq = ComplexSeq(all.filter(_.isToken), all.flatMap(_.wordIndexes).toSet)
    }

    case class ComplexSeq(tokensComplexes: Seq[Complex], wordsIndexes: Set[Int]) {
        private val (idxsSet: Set[Int], minIndex: Int, maxIndex: Int) = {
            val seq = tokensComplexes.flatMap(_.wordIndexes).distinct.sorted

            (seq.toSet, seq.head, seq.last)
        }

        def isIntersect(minIndex: Int, maxIndex: Int, idxsSet: Set[Int]): Boolean =
            if (this.minIndex > maxIndex || this.maxIndex < minIndex)
                false
            else
                this.idxsSet.exists(idxsSet.contains)

        override def toString: String = tokensComplexes.mkString(" | ")
    }

    case class ComplexHolder(complexesWords: Seq[Complex], complexes: Seq[ComplexSeq])

    /**
      *
      * @param parent Optional parent span.
      * @return
      */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()
        ackStarted()
    }

    /**
      *
      * @param parent Optional parent span.
      */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()
        ackStopped()
    }

    def isComplex(mdl: NCProbeModel): Boolean = mdl.hasIdlSynonyms || !mdl.model.getParsers.isEmpty

    /**
      *
      * @param ns
      * @param elem
      * @param toks
      * @param direct
      * @param syn
      * @param parts
      * @param metaOpt
      */
    private def mark(
        ns: Sentence,
        elem: NCElement,
        toks: Seq[NlpToken],
        direct: Boolean,
        syn: Option[Synonym] = None,
        parts: Seq[TokType] = Seq.empty,
        metaOpt: Option[Map[String, Object]] = None
    ): Unit = {
        val params = mutable.ArrayBuffer.empty[(String, AnyRef)]

        // For system elements.
        params += "direct" -> direct.asInstanceOf[AnyRef]

        syn match {
            case Some(s) =>
                if (s.isValueSynonym)
                    params += "value" -> s.value
            case None => // No-op.
        }

        metaOpt match {
            case Some(meta) => params += "meta" -> meta
            case None => // No-op.
        }

        if (parts.nonEmpty) {
            val partsData: Seq[util.HashMap[String, Any]] =
                parts.map { case (part, kind) =>
                    val m = new util.HashMap[String, Any]()

                    m.put("id", if (kind == TEXT) "nlpcraft:nlp" else part.getId)
                    m.put("startcharindex", part.getStartCharIndex)
                    m.put("endcharindex", part.getEndCharIndex)
                    m.put(TOK_META_ALIASES_KEY, part.getMetadata.get(TOK_META_ALIASES_KEY))

                    m
                }

            params += "parts" -> partsData.asJava
        }

        val idxs = toks.map(_.index).sorted

        val note = NlpNote(idxs, elem.getId, params: _*)

        toks.foreach(_.add(note))

        // For NLP elements.
        toks.foreach(t => ns.fixNote(t.getNlpNote, "direct" -> direct))
    }

    /**
      *
      * @param mdl
      * @param ns
      * @param span
      * @param req
      */
    private def processParsers(mdl: NCProbeModel, ns: Sentence, span: Span, req: NCRequestImpl): Unit = {
        for (parser <- mdl.model.getParsers.asScala) {
            parser.onInit()

            startScopedSpan("customParser", span,
                "srvReqId" -> ns.srvReqId,
                "mdlId" -> mdl.model.getId,
                "txt" -> ns.text
            ) { _ =>
                def to(t: NlpToken): NCCustomWord =
                    new NCCustomWord {
                        override def getNormalizedText: String = t.normText
                        override def getOriginalText: String = t.origText
                        override def getStartCharIndex: Int = t.startCharIndex
                        override def getEndCharIndex: Int = t.endCharIndex
                        override def getPos: String = t.pos
                        override def getPosDescription: String = t.posDesc
                        override def getLemma: String = t.lemma
                        override def getStem: String = t.stem
                        override def isStopWord: Boolean = t.isStopWord
                        override def isBracketed: Boolean = t.isBracketed
                        override def isQuoted: Boolean = t.isQuoted
                        override def isKnownWord: Boolean = t.isKnownWord
                        override def isSwearWord: Boolean = t.isSwearWord
                        override def isEnglish: Boolean = t.isEnglish
                    }

                val res = parser.parse(
                    req,
                    mdl.model,
                    ns.map(to).asJava,
                    ns.flatten.distinct.filter(!_.isNlp).map(n => {
                        val noteId = n.noteType
                        val words = ns.filter(t => n.tokenIndexes.contains(t.index)).map(to).asJava
                        val md = n.asMetadata()

                        new NCCustomElement() {
                            override def getElementId: String = noteId
                            override def getWords: JList[NCCustomWord] = words
                            override def getMetadata: JavaMeta = md.map(p => p._1 -> p._2.asInstanceOf[AnyRef]).asJava
                        }
                    }).asJava
                )

                if (res != null)
                    res.asScala.foreach(e => {
                        val elemId = e.getElementId
                        val words = e.getWords

                        if (elemId == null)
                            throw new NCE(s"Custom model parser cannot return 'null' element ID.")

                        if (words == null || words.isEmpty)
                            throw new NCE(s"Custom model parser cannot return empty custom tokens [elementId=$elemId]")

                        val matchedToks = words.asScala.map(w =>
                            ns.find(t =>
                                t.startCharIndex == w.getStartCharIndex && t.endCharIndex == w.getEndCharIndex
                            ).getOrElse(throw new AssertionError(s"Custom model parser returned an invalid custom token: $w"))
                        )

                        // Checks element's tokens.
                        if (!alreadyMarked(ns, elemId, matchedToks, matchedToks.map(_.index).sorted))
                            mark(
                                ns,
                                elem = mdl.elements.getOrElse(elemId, throw new NCE(s"Custom model parser returned unknown element ID: $elemId")),
                                toks = matchedToks,
                                direct = true,
                                metaOpt = Some(e.getMetadata.asScala)
                            )
                    })
            }

            parser.onDiscard()
        }
    }

    /**
      * Gets all sequential permutations of given tokens.
      *
      * For example, if buffer contains "a b c d" tokens, then this function will return the
      * sequence of following token sequences in this order:
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
      * @param toks
      * @return
      */
    private def combos[T](toks: Seq[T]): Seq[Seq[T]] =
        (for (n <- toks.size until 0 by -1) yield toks.sliding(n)).flatten.map(p => p)

    /**
      *
      * @param seq
      * @param s
      */
    private def toParts(seq: Seq[NCIdlContent], s: Synonym): Seq[TokType] =
        seq.zip(s.map(_.kind)).flatMap {
            case (complex, kind) => if (complex.isLeft) Some(complex.left.get -> kind) else None
        }

    /**
      *
      * @param tows
      * @param ns
      */
    private def toTokens(tows: Seq[NCIdlContent], ns: Sentence): Seq[NlpToken] =
        (
            tows.filter(_.isRight).map(_.right.get) ++
                tows.filter(_.isLeft).map(_.left.get).
                    flatMap(w => ns.filter(t => t.wordIndexes.intersect(w.wordIndexes).nonEmpty))
        ).sortBy(_.startCharIndex)

    /**
      *
      * @param m
      * @param id
      * @return
      */
    private def get(m: Map[String , Seq[Synonym]], id: String): Seq[Synonym] = m.getOrElse(id, Seq.empty)

    /**
      * Gets synonyms sorted in descending order by their weight (already prepared),
      * i.e. first synonym in the sequence is the most important one.
      *
      * @param fastMap {Element ID -> {Synonym length -> T}}
      * @param elmId
      * @param len
      */
    private def fastAccess[T](fastMap: Map[String, Map[Int, T]], elmId: String, len: Int): Option[T] =
        fastMap.getOrElse(elmId, Map.empty[Int, T]).get(len)

    /**
      *
      * @param mdl
      * @param ns
      */
    private def mkComplexes(mdl: NCProbeModel, ns: Sentence): ComplexHolder = {
        val complexesWords = ns.map(Complex(_))

        val complexes =
            NCProbeVariants.convert(ns.srvReqId, mdl, NCSentenceManager.collapse(mdl.model, ns.clone())).
                map(_.asScala).
                par.
                flatMap(sen =>
                    // Tokens splitting.
                    // For example sentence "A B С D E" (5 words) processed as 3 tokens on first phase after collapsing
                    //  'A B' (2 words), 'C D' (2 words) and 'E' (1 word)
                    //  So, result combinations will be:
                    //  Token(AB) + Token(CD) + Token(E)
                    //  Token(AB) + Word(C) + Word(D) + Token(E)
                    //  Word(A) + Word(B) + Token(CD) + Token(E)
                    //  Word(A) + Word(B) + Word(C) + Word(D) + Token(E)
                    combos(sen).
                        map(senPartComb => {
                            sen.flatMap(t =>
                                // Single word token is not split as words - token.
                                // Partly (not strict in) token - word.
                                if (t.wordIndexes.length == 1 || senPartComb.contains(t))
                                    Seq(Complex(t))
                                else
                                    t.wordIndexes.map(complexesWords)
                            )
                            // Drops without tokens (IDL part works with tokens).
                        }).filter(_.exists(_.isToken)).map(ComplexSeq(_)).distinct
                ).seq

        ComplexHolder(complexesWords, complexes)
    }

    /**
      *
      * @param h
      * @param toks
      */
    private def mkCombinations(h: ComplexHolder, toks: Seq[NlpToken], cache: Set[Seq[Complex]]): Seq[Seq[Complex]] = {
        val idxs = toks.flatMap(_.wordIndexes).toSet

        h.complexes.par.
            flatMap(complexSeq => {
                val rec = complexSeq.tokensComplexes.filter(_.wordIndexes.exists(idxs.contains))

                // Drops without tokens (IDL part works with tokens).
                if (rec.nonEmpty) {
                    val data = rec ++
                        (complexSeq.wordsIndexes.intersect(idxs) -- rec.flatMap(_.wordIndexes)).map(h.complexesWords)

                    if (!cache.contains(data)) Some(data) else None
                }
                else
                    None
            }).seq
    }

    private def add(
        dbgType: String,
        ns: Sentence,
        contCache: Cache,
        elem: NCElement,
        elemToks: Seq[NlpToken],
        sliceToksIdxs: Seq[Int],
        syn: Synonym,
        parts: Seq[TokType] = Seq.empty)
    : Unit = {
        val resIdxs = elemToks.map(_.index)
        val resIdxsSorted = resIdxs.sorted

        if (resIdxsSorted == sliceToksIdxs && U.isContinuous(resIdxsSorted))
            contCache(elem.getId) += sliceToksIdxs

        val ok = !alreadyMarked(ns, elem.getId, elemToks, sliceToksIdxs)

        if (ok)
            mark(ns, elem, elemToks, direct = syn.isDirect && U.isIncreased(resIdxs), syn = Some(syn), parts = parts)

        if (DEEP_DEBUG)
            logger.trace(
                s"${if (ok) "Added" else "Skipped"} element [" +
                    s"id=${elem.getId}, " +
                    s"type=$dbgType, " +
                    s"text='${elemToks.map(_.origText).mkString(" ")}', " +
                    s"indexes=${resIdxs.mkString("[", ",", "]")}, " +
                    s"allTokensIndexes=${sliceToksIdxs.mkString("[", ",", "]")}, " +
                    s"synonym=$syn" +
                    s"]"
            )
    }

    @throws[NCE]
    override def enrich(mdl: NCProbeModel, ns: Sentence, senMeta: Map[String, Serializable], parent: Span = null): Unit = {
        require(isStarted)

        startScopedSpan(
            "enrich", parent, "srvReqId" -> ns.srvReqId, "mdlId" -> mdl.model.getId, "txt" -> ns.text
        ) { span =>
            val req = NCRequestImpl(senMeta, ns.srvReqId)
            val combToks = combos(ns.toSeq)
            lazy val ch = mkComplexes(mdl, ns)

            def execute(simpleEnabled: Boolean, idlEnabled: Boolean): Unit =
                startScopedSpan(
                    "execute", span, "srvReqId" -> ns.srvReqId, "mdlId" -> mdl.model.getId, "txt" -> ns.text
                ) { _ =>
                    if (DEEP_DEBUG)
                        logger.trace(s"Execution started [simpleEnabled=$simpleEnabled, idlEnabled=$idlEnabled]")

                    val contCache = mutable.HashMap.empty ++
                        mdl.elements.keys.map(k => k -> mutable.ArrayBuffer.empty[Seq[Int]])
                    lazy val idlCache = mutable.HashSet.empty[Seq[Complex]]

                    for (
                        toks <- combToks;
                        idxs = toks.map(_.index);
                        e <- mdl.elements.values;
                        eId = e.getId
                        if
                            !contCache(eId).exists(_.containsSlice(idxs)) &&
                            !alreadyMarked(ns, eId, toks, idxs)
                    ) {
                        // 1. SIMPLE.
                        if (simpleEnabled && (if (idlEnabled) mdl.hasIdlSynonyms(eId) else !mdl.hasIdlSynonyms(eId))) {
                            lazy val tokStems = toks.map(_.stem).mkString(" ")

                            // 1.1 Continuous.
                            var found = false

                            if (mdl.hasContinuousSynonyms)
                                fastAccess(mdl.continuousSynonyms, eId, toks.length) match {
                                    case Some(h) =>
                                        def tryMap(syns: Map[String, Synonym], notFound: () => Unit): Unit =
                                            syns.get(tokStems) match {
                                                case Some(s) =>
                                                    found = true
                                                    add("simple continuous", ns, contCache, e, toks, idxs, s)
                                                case None => notFound()
                                            }

                                        def tryScan(syns: Seq[Synonym]): Unit =
                                            for (s <- syns if !found)
                                                if (s.isMatch(toks)) {
                                                    found = true
                                                    add("simple continuous scan", ns, contCache, e, toks, idxs, s)
                                                }

                                        tryMap(
                                            h.txtDirectSynonyms,
                                            () => {
                                                tryScan(h.notTxtDirectSynonyms)

                                                if (!found)
                                                    tryMap(h.txtNotDirectSynonyms, () => tryScan(h.notTxtNotDirectSynonyms))
                                            }
                                        )
                                    case None => // No-op.
                                }

                            // 1.2 Sparse.
                            if (!found && mdl.hasSparseSynonyms)
                                for (s <- get(mdl.sparseSynonyms, eId))
                                    s.sparseMatch(toks) match {
                                        case Some(res) => add("simple sparse", ns, contCache, e, res, idxs, s)
                                        case None => // No-op.
                                    }
                        }

                        // 2. IDL.
                        if (idlEnabled) {
                            val allSyns = get(mdl.idlSynonyms, eId)
                            lazy val allCombs = mkCombinations(ch, toks, idlCache.toSet)

                            // 2.1 Continuous.

                            if (!mdl.hasSparseSynonyms) {
                                var found = false

                                for (
                                    s <- allSyns;
                                    comb <- allCombs
                                    if !found;
                                    data = comb.map(_.data)
                                )
                                    if (s.isMatch(data, req)) {
                                        add("IDL continuous", ns, contCache, e, toks, idxs, s, toParts(data, s))

                                        idlCache += comb

                                        found = true
                                    }
                            }
                            else
                                // 2.2 Sparse.
                                for (
                                    s <- allSyns;
                                    comb <- allCombs
                                )
                                    s.sparseMatch(comb.map(_.data), req) match {
                                        case Some(res) =>
                                            val typ = if (s.sparse) "IDL sparse" else "IDL continuous"

                                            add(typ, ns, contCache, e, toTokens(res, ns), idxs, s, toParts(res, s))

                                            idlCache += comb
                                        case None => // No-op.
                                    }
                        }
                    }
                }

            if (ns.firstProbePhase) {
                ns.firstProbePhase = false

                if (mdl.hasNoIdlSynonyms)
                    execute(simpleEnabled = true, idlEnabled = false)
                execute(simpleEnabled = mdl.hasNoIdlSynonyms, idlEnabled = mdl.hasIdlSynonyms)
            }
            else if (mdl.hasIdlSynonyms)
                execute(simpleEnabled = false, idlEnabled = true)

            processParsers(mdl, ns, span, req)
        }
    }

    // TODO: simplify, add tests, check model properties (sparse etc) for optimization.
    /**
      *
      * @param elemId
      * @param toks
      * @param sliceToksIdxsSorted
      */
    private def alreadyMarked(ns: Sentence, elemId: String, toks: Seq[NlpToken], sliceToksIdxsSorted: Seq[Int]): Boolean = {
        lazy val toksIdxsSorted = toks.map(_.index).sorted

        sliceToksIdxsSorted.map(ns).forall(_.exists(n => n.noteType == elemId && n.sparsity == 0)) ||
        toks.exists(_.exists(n =>
            n.noteType == elemId &&
            (
                (n.sparsity == 0 &&
                    (sliceToksIdxsSorted.containsSlice(n.tokenIndexes) || n.tokenIndexes.containsSlice(toksIdxsSorted))
                )
                    ||
                (
                    n.tokenIndexes == toksIdxsSorted ||
                        n.tokenIndexes.containsSlice(toksIdxsSorted) &&
                        U.isContinuous(toksIdxsSorted) &&
                        U.isContinuous(n.tokenIndexes)
                )
            )
        ))
    }
}