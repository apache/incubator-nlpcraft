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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.model

import java.io.Serializable
import java.util

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.nlp.{NCNlpSentenceToken, _}
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.probe.mgrs.nlp.NCProbeEnricher
import org.apache.nlpcraft.probe.mgrs.nlp.impl.NCRequestImpl
import org.apache.nlpcraft.probe.mgrs.{NCModelDecorator, NCSynonym}

import scala.collection.JavaConverters._
import scala.collection.convert.DecorateAsScala
import scala.collection.mutable.ArrayBuffer
import scala.collection.{Map, Seq, mutable}

/**
  * Model elements enricher.
  */
object NCModelEnricher extends NCProbeEnricher with DecorateAsScala {
    case class Complex(data: Either[NCToken, NCNlpSentenceToken]) {
        lazy val isToken: Boolean = data.isLeft
        lazy val isWord: Boolean = data.isRight
        lazy val token: NCToken = data.left.get
        lazy val word: NCNlpSentenceToken = data.right.get
        lazy val origText: String = if (isToken) token.origText else word.origText
        lazy val wordIndexes: Seq[Int] = if (isToken) token.wordIndexes else word.wordIndexes

        private lazy val hash = if (isToken) token.hashCode() else word.hashCode()

        override def hashCode(): Int = hash
        override def equals(obj: Any): Boolean = obj match {
            case x: Complex ⇒ isToken && x.isToken && token == x.token || isWord && x.isWord && word == x.word
            case _ ⇒ false
        }

        // Added for debug reasons.
        override def toString: String =
            if (isToken) s"Token: '${token.origText} (${token.getId})'" else s"Word: '${word.origText}'"
    }

    // Found-by-synonym model element.
    case class ElementMatch(
        element: NCElement,
        tokens: Seq[NCNlpSentenceToken],
        synonym: NCSynonym,
        parts: Seq[NCToken]
    ) extends Ordered[ElementMatch] {
        // Tokens sparsity.
        lazy val sparsity: Int = tokens.zipWithIndex.tail.map {
            case (tok, idx) ⇒ Math.abs(tok.index - tokens(idx - 1).index)
        }.sum - tokens.length + 1

        // Number of tokens.
        lazy val length: Int = tokens.size

        private lazy val tokensSet = tokens.toSet

        def isSubSet(toks: Set[NCNlpSentenceToken]): Boolean = toks.subsetOf(tokensSet)

        override def compare(that: ElementMatch): Int = {
            // Check synonym first, then length and then sparsity.
            // Note that less sparsity means more certainty in a match.

            if (that == null)
                1
            else if (synonym < that.synonym)
                -1
            else if (synonym > that.synonym)
                1
            else if (length < that.length)
                -1
            else if (length > that.length)
                1
            else if (sparsity < that.sparsity)
                1
            else if (sparsity > that.sparsity)
                -1
            else
                0
        }
    }

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        super.start()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }

    /**
      * Returns an iterator of tokens arrays where each token is jiggled left and right by given factor.
      * Note that only one token is jiggled at a time.
      *
      * @param ns NLP sentence to jiggle.
      * @param factor Distance of left or right jiggle, i.e. how far can an individual token move
      *         left or right in the sentence.
      */
    private def jiggle(ns: NCNlpSentenceTokenBuffer, factor: Int): Iterator[NCNlpSentenceTokenBuffer] = {
        require(factor >= 0)
        if (ns.isEmpty)
            Iterator.empty
        else if (factor == 0)
            Iterator.apply(ns)
        else
            new Iterator[NCNlpSentenceTokenBuffer] {
                private val min = -factor

                private val max = factor

                private val sz = ns.size

                private var i = 0 // Token index.
                private var d = 0 // Jiggle amount [min, max].

                private var isNext = sz > 0

                private def calcNext(): Unit = {
                    isNext = false
                    d += 1
                    while (i < sz && !isNext) {
                        while (d <= max && !isNext) {
                            val p = i + d
                            if (p >= 0 && p < sz) // Valid new position?
                                isNext = true
                            else
                                d += 1
                        }
                        if (!isNext) {
                            d = min
                            i += 1
                        }
                    }
                }

                override def hasNext: Boolean = isNext

                override def next(): NCNlpSentenceTokenBuffer = {
                    require(isNext)
                    val buf = NCNlpSentenceTokenBuffer(ns)
                    if (d != 0)
                        buf.insert(i + d, buf.remove(i)) // Jiggle.

                    calcNext()
                    buf
                }
            }
    }

    /**
      *
      * @param ns
      * @param elem
      * @param toks
      * @param direct
      * @param syn
      * @param metaOpt
      * @param parts
      */
    private def mark(
        ns: NCNlpSentence,
        elem: NCElement,
        toks: Seq[NCNlpSentenceToken],
        direct: Boolean,
        syn: Option[NCSynonym],
        metaOpt: Option[Map[String, Object]],
        parts: Seq[NCToken]
    ): Unit = {
        val params = mutable.ArrayBuffer.empty[(String, AnyRef)]

        // For system elements.
        params += "direct" → direct.asInstanceOf[AnyRef]

        syn match {
            case Some(s) ⇒
                if (s.isValueSynonym)
                    params += "value" → s.value
            case None ⇒ // No-op.
        }

        metaOpt match {
            case Some(meta) ⇒ params += "meta" → meta
            case None ⇒ // No-op.
        }

        if (parts.nonEmpty) {
            val partsData: Seq[util.HashMap[String, Any]] =
                parts.map(part ⇒ {
                    val m = new util.HashMap[String, Any]()

                    m.put("id", part.getId)
                    m.put("startcharindex", part.getStartCharIndex)
                    m.put("endcharindex", part.getEndCharIndex)
                    m.put(TOK_META_ALIASES_KEY, part.getMetadata.get(TOK_META_ALIASES_KEY))

                    m
                })

            params += "parts" → partsData.asJava
        }

        val idxs = toks.map(_.index).sorted

        val note = NCNlpSentenceNote(idxs, elem.getId, params: _*)

        toks.foreach(_.add(note))

        // For NLP elements.
        toks.foreach(t ⇒ ns.fixNote(t.getNlpNote, "direct" → direct))
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
    protected def combos[T](toks: Seq[T]): Seq[Seq[T]] =
        (for (n ← toks.size until 0 by -1) yield toks.sliding(n)).flatten.map(p ⇒ p)

    /**
      *
      * @param initialSen
      * @param collapsedSen
      * @param nlpToks
      */
    private def convert(
        initialSen: NCNlpSentence, collapsedSen: Seq[Seq[NCToken]], nlpToks: Seq[NCNlpSentenceToken]
    ): Seq[Seq[Complex]] = {
        val nlpWordIdxs = nlpToks.flatMap(_.wordIndexes)

        def in(t: NCToken): Boolean = t.wordIndexes.exists(nlpWordIdxs.contains)
        def inStrict(t: NCToken): Boolean = t.wordIndexes.forall(nlpWordIdxs.contains)
        def isSingleWord(t: NCToken): Boolean = t.wordIndexes.length == 1

        collapsedSen.
            map(_.filter(in)).
            filter(_.nonEmpty).flatMap(varToks ⇒
                // Tokens splitting.
                // For example sentence "A B С D E" (5 words) processed as 3 tokens on first phase after collapsing
                //  'A B' (2 words), 'C D' (2 words) and 'E' (1 word)
                //  So, result combinations will be:
                //  Token(AB) + Token(CD) + Token(E)
                //  Token(AB) + Word(C) + Word(D) + Token(E)
                //  Word(A) + Word(B) + Token(CD) + Token(E)
                //  Word(A) + Word(B) + Word(C) + Word(D) + Token(E)
                combos(varToks).map(toksComb ⇒
                    varToks.flatMap(t ⇒
                        // Single word token is not split as words - token.
                        // Partly (not strict in) token - word.
                        if ((toksComb.contains(t) || isSingleWord(t)) && inStrict(t))
                            Seq(Complex(Left(t)))
                        else
                            t.wordIndexes.filter(nlpWordIdxs.contains).map(i ⇒ Complex(Right(initialSen(i))))
                    )
                ).filter(_.exists(_.isToken)) // Drops without tokens (DSL part works with tokens).
        ).distinct
    }

    /**
      *
      * @param toks
      * @param elemId
      */
    private def alreadyMarked(toks: Seq[NCNlpSentenceToken], elemId: String): Boolean = toks.forall(_.isTypeOf(elemId))

    def isComplex(mdl: NCModelDecorator): Boolean =
        mdl.synonymsDsl.nonEmpty || (mdl.model.getParsers != null && !mdl.model.getParsers.isEmpty)

    @throws[NCE]
    override def enrich(mdl: NCModelDecorator, ns: NCNlpSentence, senMeta: Map[String, Serializable], parent: Span = null): Unit =
        startScopedSpan("enrich", parent,
            "srvReqId" → ns.srvReqId,
            "modelId" → mdl.model.getId,
            "txt" → ns.text) { span ⇒
            val jiggleFactor = mdl.model.getJiggleFactor
            val cache = mutable.HashSet.empty[Seq[Int]]
            val matches = ArrayBuffer.empty[ElementMatch]

            /**
              * Gets sequence of synonyms sorted in descending order by their weight, i.e. first synonym in
              * the sequence is the most important one.
              *
              * @param fastMap
              * @param elmId
              * @param len
              * @return
              */
            def fastAccess(
                fastMap: Map[String /*Element ID*/ , Map[Int /*Synonym length*/ , Seq[NCSynonym]]],
                elmId: String,
                len: Int): Seq[NCSynonym] =
                fastMap.get(elmId).flatMap(_.get(len)) match {
                    case Some(seq) ⇒ seq
                    case None ⇒ Seq.empty[NCSynonym]
                }

            /**
              *
              * @param toks
              * @return
              */
            def tokString(toks: Seq[NCNlpSentenceToken]): String =
                toks.map(t ⇒ (t.origText, t.index)).mkString(" ")

            var permCnt = 0
            var collapsedSens: Seq[Seq[NCToken]] = null

            /**
              *
              * @param perm Permutation to process.
              */
            def procPerm(perm: NCNlpSentenceTokenBuffer): Unit = {
                permCnt += 1

                for (toks ← combos(perm)) {
                    val key = toks.map(_.index).sorted

                    if (!cache.contains(key)) {
                        var seq: Seq[Seq[Complex]] = null

                        // Attempt to match each element.
                        for (elm ← mdl.elements.values if !alreadyMarked(toks, elm.getId)) {
                            var found = false

                            def addMatch(
                                elm: NCElement, toks: Seq[NCNlpSentenceToken], syn: NCSynonym, parts: Seq[NCToken]
                            ): Unit =
                                if (!matches.exists(m ⇒ m.element == elm && m.isSubSet(toks.toSet))) {
                                    found = true

                                    matches += ElementMatch(elm, toks, syn, parts)
                                }

                            // Optimization - plain synonyms can be used only on first iteration
                            if (mdl.synonyms.nonEmpty && !ns.exists(_.isUser))
                                for (syn ← fastAccess(mdl.synonyms, elm.getId, toks.length) if !found)
                                    if (syn.isMatch(toks))
                                        addMatch(elm, toks, syn, Seq.empty)

                            if (mdl.synonymsDsl.nonEmpty) {
                                found = false

                                if (collapsedSens == null)
                                    collapsedSens = mdl.makeVariants(ns.srvReqId, ns.clone().collapse()).map(_.asScala)

                                if (seq == null)
                                    seq = convert(ns, collapsedSens, toks)

                                for (comb ← seq; syn ← fastAccess(mdl.synonymsDsl, elm.getId, comb.length) if !found)
                                    if (syn.isMatch(comb.map(_.data)))
                                        addMatch(elm, toks, syn, comb.filter(_.isToken).map(_.token))
                            }
                        }

                        cache += key
                    }
                }
            }

            startScopedSpan("jiggleProc", span,
                "srvReqId" → ns.srvReqId,
                "modelId" → mdl.model.getId,
                "txt" → ns.text) { _ ⇒
                // Iterate over depth-limited permutations of the original sentence with and without stopwords.
                jiggle(ns, jiggleFactor).foreach(procPerm)
                jiggle(NCNlpSentenceTokenBuffer(ns.filter(!_.isStopWord)), jiggleFactor).foreach(procPerm)
            }

            if (DEEP_DEBUG)
                logger.trace(s"Total jiggled permutations processed: $permCnt")

            addTags(
                span,
                "totalJiggledPerms" → permCnt
            )

            val matchCnt = matches.size

            // Add notes for all remaining (non-intersecting) matches.
            for ((m, idx) ← matches.zipWithIndex) {
                if (DEEP_DEBUG)
                    logger.trace(
                        s"Model '${mdl.model.getId}' element found (${idx + 1} of $matchCnt) [" +
                            s"elementId=${m.element.getId}, " +
                            s"synonym=${m.synonym}, " +
                            s"tokens=${tokString(m.tokens)}" +
                            s"]"
                    )
    
                val elm = m.element
                val syn = m.synonym
    
                val tokIdxs = m.tokens.map(_.index)
                val direct = syn.isDirect && (tokIdxs == tokIdxs.sorted)

                mark(ns, elem = elm, toks = m.tokens, direct = direct, syn = Some(syn), metaOpt = None, parts = m.parts)
            }

            val parsers = mdl.model.getParsers

            if (parsers != null)
                for (parser ← parsers.asScala) {
                    parser.onInit()
                    
                    startScopedSpan("customParser", span,
                        "srvReqId" → ns.srvReqId,
                        "modelId" → mdl.model.getId,
                        "txt" → ns.text) { _ ⇒
                        def to(t: NCNlpSentenceToken): NCCustomWord =
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
                            NCRequestImpl(senMeta, ns.srvReqId),
                            mdl.model,
                            ns.map(to).asJava,
                            ns.flatten.distinct.filter(!_.isNlp).map(n ⇒ {
                                val noteId = n.noteType
                                val words = ns.filter(t ⇒ t.index >= n.tokenFrom && t.index <= n.tokenTo).map(to).asJava
                                val md = n.asMetadata()
                
                                new NCCustomElement() {
                                    override def getElementId: String = noteId
                                    override def getWords: util.List[NCCustomWord] = words
                                    override def getMetadata: util.Map[String, AnyRef] =
                                        md.map(p ⇒ p._1 → p._2.asInstanceOf[AnyRef]).asJava
                                }
                            }).asJava
                        )
        
                        if (res != null)
                            res.asScala.foreach(e ⇒ {
                                val elemId = e.getElementId
                                val words = e.getWords
                
                                if (elemId == null)
                                    throw new NCE(s"Custom model parser cannot return 'null' element ID.")
                
                                if (words == null || words.isEmpty)
                                    throw new NCE(s"Custom model parser cannot return empty custom tokens [elementId=$elemId]")
                
                                val matchedToks = words.asScala.map(w ⇒
                                    ns.find(t ⇒
                                        t.startCharIndex == w.getStartCharIndex && t.endCharIndex == w.getEndCharIndex
                                    ).getOrElse(throw new AssertionError(s"Custom model parser returned an invalid custom token: $w"))
                                )
                
                                if (!alreadyMarked(matchedToks, elemId))
                                    mark(
                                        ns,
                                        elem = mdl.elements.getOrElse(elemId, throw new NCE(s"Custom model parser returned unknown element ID: $elemId")),
                                        toks = matchedToks,
                                        direct = true,
                                        syn = None,
                                        metaOpt = Some(e.getMetadata.asScala),
                                        parts = Seq.empty
                                    )
                            })
                    }
                    
                    parser.onDiscard()
                }
        }
}
