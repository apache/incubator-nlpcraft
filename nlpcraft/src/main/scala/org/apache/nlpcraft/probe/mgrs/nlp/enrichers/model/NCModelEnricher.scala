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
import org.apache.nlpcraft.common.nlp.NCNlpSentence.NoteLink
import org.apache.nlpcraft.common.nlp.{NCNlpSentence => Sentence, NCNlpSentenceNote => NlpNote, NCNlpSentenceToken => NlpToken}
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.impl.NCTokenImpl
import org.apache.nlpcraft.probe.mgrs.NCProbeSynonymChunkKind.NCSynonymChunkKind
import org.apache.nlpcraft.probe.mgrs.nlp.NCProbeEnricher
import org.apache.nlpcraft.probe.mgrs.nlp.impl.NCRequestImpl
import org.apache.nlpcraft.probe.mgrs.sentence.NCSentenceManager
import org.apache.nlpcraft.probe.mgrs.synonyms.NCSynonymsManager
import org.apache.nlpcraft.probe.mgrs.{NCProbeIdlToken => IdlToken, NCProbeModel, NCProbeVariants, NCTokenPartKey, NCProbeSynonym => Synonym}

import java.io.Serializable
import java.util.{List => JList}
import scala.collection.mutable
import scala.collection.parallel.CollectionConverters._
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsJava, MapHasAsScala, SeqHasAsJava}

/**
  * Model elements enricher.
  */
object NCModelEnricher extends NCProbeEnricher {
    type TokType = (NCToken, NCSynonymChunkKind)

    object IdlTokensSeq {
        def apply(all: Seq[IdlToken]): IdlTokensSeq = IdlTokensSeq(all.filter(_.isToken), all.flatMap(_.wordIndexes).toSet)
    }

    case class IdlTokensSeq(tokens: Seq[IdlToken], wordsIndexes: Set[Int]) {
        private val (idxsSet: Set[Int], minIndex: Int, maxIndex: Int) = {
            val seq = tokens.flatMap(_.wordIndexes).distinct.sorted

            (seq.toSet, seq.head, seq.last)
        }

        def isIntersect(minIndex: Int, maxIndex: Int, idxsSet: Set[Int]): Boolean =
            if (this.minIndex > maxIndex || this.maxIndex < minIndex)
                false
            else
                this.idxsSet.exists(idxsSet.contains)

        override def toString: String = tokens.mkString(" | ")
    }

    case class IdlTokensHolder(tokens: Seq[IdlToken], seqs: Seq[IdlTokensSeq])

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

    /**
      *
      * @param ns
      * @param elemId
      * @param toks
      * @param direct
      * @param syn
      * @param parts
      * @param metaOpt
      */
    private def mark(
        ns: Sentence,
        elemId: String,
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

        if (parts.nonEmpty)
            params += "parts" -> parts.map { case (p, kind) => NCTokenPartKey(p, kind) }.asJava

        val idxs = toks.map(_.index).sorted

        val note = NlpNote(idxs, elemId, params: _*)

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
                            override def getMetadata: JavaMeta =
                                md.map { case (k, v) => k -> v.asInstanceOf[AnyRef] }.asJava
                        }
                    }).asJava
                )

                if (res != null)
                    res.asScala.foreach(e => {
                        val elmId = e.getElementId
                        val words = e.getWords

                        if (elmId == null)
                            throw new NCE(s"Custom model parser cannot return 'null' element ID.")

                        if (words == null || words.isEmpty)
                            throw new NCE(s"Custom model parser cannot return empty custom tokens for element: $elmId")

                        val matchedToks = words.asScala.map(w =>
                            ns.find(t =>
                                t.startCharIndex == w.getStartCharIndex && t.endCharIndex == w.getEndCharIndex
                            ).getOrElse(throw new AssertionError(s"Custom model parser returned an invalid custom token: $w"))
                        )

                        // Checks element's tokens.
                        if (!alreadyMarked(ns, elmId, matchedToks, matchedToks.map(_.index).sorted))
                            mark(
                                ns,
                                elemId = elmId,
                                toks = matchedToks,
                                direct = true,
                                metaOpt = Some(e.getMetadata.asScala.toMap)
                            )
                    })
            }

            parser.onDiscard()
        }
    }

    /**
      *
      * 1. Prepares combination of tokens (sliding).
      *  Example: 'A B C D' -> {'A B C', 'A B', 'B C', 'A', 'B', 'C'}
      *  One sentence converted to 4 pieces.
      *
      * 2. Additionally, each piece converted into set of elements with all possible its stopwords permutations.
      *  Example: Piece: 'x1, x2(stopword), x3(stopword), x4' will be expanded  into
      *  {'x1, x2, x3, x4', 'x1, x2, x4', 'x1, x3, x4', 'x1, x4'}
      *
      *  3. All variants collected, duplicated sets deleted, etc.
      *
      * @param toks
      */
    private def combosTokens(toks: Seq[NlpToken]): Seq[(Seq[NlpToken], Seq[NlpToken])] =
        combos(toks).flatMap(combo => {
            val stops = combo.filter(s => s.isStopWord && s != combo.head && s != combo.last)

            val slides = mutable.ArrayBuffer.empty[mutable.ArrayBuffer[NlpToken]]

            for (stop <- stops)
                if (slides.nonEmpty && slides.last.last.index + 1 == stop.index)
                    slides.last += stop
                else
                    slides += mutable.ArrayBuffer.empty :+ stop

            // Too many stopwords inside skipped.
            val bigSlides = slides.filter(_.size > 2)

            var stops4Delete: Seq[Seq[NlpToken]] =
                if (bigSlides.nonEmpty) {
                    val allBig = bigSlides.flatten
                    val stops4AllCombs = stops.filter(p => !allBig.contains(p))

                    if (stops4AllCombs.nonEmpty)
                        for (
                            seq1 <- Range.inclusive(0, stops4AllCombs.size).flatMap(stops4AllCombs.combinations);
                                seq2 <- Range.inclusive(0, bigSlides.size).flatMap(bigSlides.combinations)
                        )
                        yield seq1 ++ seq2.flatten
                    else
                        for (seq <- Range.inclusive(0, bigSlides.size).flatMap(bigSlides.combinations))
                            yield seq.toSeq.flatten
                }
                else
                    Range.inclusive(1, stops.size).flatMap(stops.combinations)

            stops4Delete = stops4Delete.filter(seq => !seq.contains(combo.head) && !seq.contains(combo.last))

            (Seq(combo) ++ stops4Delete.map(del => combo.filter(t => !del.contains(t)))).map(_ -> combo).distinct
        }).
            filter { case (seq, _) => seq.nonEmpty }.
            groupBy { case (seq, _) => seq }.
            map { case (toksKey, seq) => toksKey -> seq.map(_._2).minBy(p => (-p.size, p.head.index)) }.
            sortBy { case(data, combo) => (-combo.size, -data.size, combo.head.index, data.head.index) }

    /**
      *
      * @param toks
      */
    private def combos[T](toks: Seq[T]): Seq[Seq[T]] =
        (for (n <- toks.size until 0 by -1) yield toks.sliding(n)).flatten.map(p => p)

    /**
      *
      * @param seq
      * @param s
      */
    private def toParts(mdl: NCProbeModel, stvReqId: String, seq: Seq[IdlToken], s: Synonym): Seq[TokType] =
        seq.zip(s.map(_.kind)).flatMap {
            case (idlTok, kind) =>
                val t = if (idlTok.isToken) idlTok.token else mkNlpToken(mdl, stvReqId, idlTok.word)

                Some(t -> kind)
        }

    /**
      *
      * @param idlToks
      * @param ns
      */
    private def toNlpTokens(idlToks: Seq[IdlToken], ns: Sentence): Seq[NlpToken] = {
        val words = idlToks.filter(_.isWord).map(_.word)
        val suitableToks =
            idlToks.filter(_.isToken).map(_.token).
                flatMap(w => ns.filter(t => t.wordIndexes.intersect(w.wordIndexes).nonEmpty))

        (words ++ suitableToks).sortBy(_.startCharIndex)
    }

    /**
      *
      * @param m
      * @param id
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
    private def mkHolder(mdl: NCProbeModel, ns: Sentence): IdlTokensHolder = {
        val toks = ns.map(IdlToken(_))

        val seqs =
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
                                    Seq(IdlToken(t))
                                else
                                    t.wordIndexes.map(toks)
                            )
                            // Drops without tokens (IDL part works with tokens).
                        }).filter(_.exists(_.isToken)).map(IdlTokensSeq(_)).distinct
                ).seq

        IdlTokensHolder(toks, seqs)
    }

    /**
      *
      * @param mdl
      * @param srvReqId
      * @param t
      */
    private def mkNlpToken(mdl: NCProbeModel, srvReqId: String, t: NlpToken): NCToken = {
        val notes = mutable.HashSet.empty[NlpNote]

        notes += t.getNlpNote

        NCTokenImpl(mdl, srvReqId, NlpToken(t.index, notes, t.stopsReasons))
    }

    /**
      * Prepares IDL tokens based on NLP tokens.
      *
      * @param h
      * @param toks
      */
    private def mkCombinations(h: IdlTokensHolder, toks: Seq[NlpToken]): Seq[Seq[IdlToken]] = {
        val idxs = toks.flatMap(_.wordIndexes).toSet

        h.seqs.par.
            flatMap(seq => {
                val rec = seq.tokens.filter(_.wordIndexes.exists(idxs.contains))

                // Drops without tokens (IDL part works with tokens).
                if (rec.nonEmpty)
                    Some(rec ++ (seq.wordsIndexes.intersect(idxs) -- rec.flatMap(_.wordIndexes)).map(h.tokens))
                else
                    None
            }).seq
    }

    /**
      *
      * @param matched
      * @param toks2Match
      */
    private def getSparsedTokens(matched: Seq[NlpToken], toks2Match: Seq[NlpToken]): Seq[NlpToken] = {
        require(matched.nonEmpty)

        // Matched tokens should be already sorted.
        val stopsInside = toks2Match.filter(t =>
            t.isStopWord && !matched.contains(matched) && t.index > matched.head.index && t.index < matched.last.index
        )

        if (stopsInside.nonEmpty) (matched ++ stopsInside).sortBy(_.index) else matched
    }

    @throws[NCE]
    override def enrich(mdl: NCProbeModel, ns: Sentence, senMeta: Map[String, Serializable], parent: Span = null): Unit = {
        require(isStarted)

        startScopedSpan(
            "enrich", parent, "srvReqId" -> ns.srvReqId, "mdlId" -> mdl.model.getId, "txt" -> ns.text
        ) { span =>
            val req = NCRequestImpl(senMeta, ns.srvReqId)

            lazy val ch = mkHolder(mdl, ns)
            lazy val variantsToks =
                ch.seqs.map(
                    p => p.tokens.map(p => if (p.isToken) p.token else mkNlpToken(mdl, ns.srvReqId, p.word))
                )

            def execute(simpleEnabled: Boolean, idlEnabled: Boolean): Unit =
                startScopedSpan(
                    "execute", span, "srvReqId" -> ns.srvReqId, "mdlId" -> mdl.model.getId, "txt" -> ns.text
                ) { _ =>
                    if (DEEP_DEBUG)
                        logger.trace(s"Execution started [simpleEnabled=$simpleEnabled, idlEnabled=$idlEnabled]")

                    for (
                        // 'toksExt' is piece of sentence, 'toks' is the same as 'toksExt' or without some stopwords set.
                        (toks, toksExt) <- combosTokens(ns.toSeq);
                            idxs = toks.map(_.index);
                            e <- mdl.elements.values;
                            elemId = e.getId;
                            greedy = e.isGreedy.orElse(mdl.model.isGreedy)
                            if !greedy || !alreadyMarked(ns, elemId, toks, idxs)
                    ) {
                        def add(
                            dbgType: String,
                            elemToks: Seq[NlpToken],
                            syn: Synonym,
                            parts: Seq[TokType] = Seq.empty
                        ): Unit = {
                            val resIdxs = elemToks.map(_.index)

                            val ok =
                                (!greedy || !alreadyMarked(ns, elemId, elemToks, idxs)) &&
                                    ( parts.isEmpty || !parts.exists { case (tok, _) => tok.getId == elemId })

                            if (ok)
                                mark(
                                    ns,
                                    elemId,
                                    elemToks,
                                    direct = syn.isDirect && U.isIncreased(resIdxs),
                                    syn = Some(syn),
                                    parts = parts
                                )

                            if (DEEP_DEBUG)
                                logger.trace(
                                    s"${if (ok) "Added" else "Skipped"} element [" +
                                        s"id=$elemId, " +
                                        s"type=$dbgType, " +
                                        s"text='${elemToks.map(_.origText).mkString(" ")}', " +
                                        s"indexes=${resIdxs.mkString("[", ",", "]")}, " +
                                        s"allTokensIndexes=${idxs.mkString("[", ",", "]")}, " +
                                        s"synonym=$syn" +
                                        s"]"
                                )
                        }

                        // 1. SIMPLE.
                        if (simpleEnabled && (if (idlEnabled) mdl.hasIdlSynonyms(elemId) else !mdl.hasIdlSynonyms(elemId))) {
                            lazy val tokStems = toks.map(_.stem).mkString(" ")

                            // 1.1 Continuous.
                            var found = false

                            if (mdl.hasContinuousSynonyms)
                                fastAccess(mdl.continuousSynonyms, elemId, toks.length) match {
                                    case Some(h) =>
                                        def tryMap(syns: Map[String, Synonym], notFound: () => Unit): Unit =
                                            syns.get(tokStems) match {
                                                case Some(s) =>
                                                    found = true
                                                    add("simple continuous", toksExt, s)
                                                case None => notFound()
                                            }

                                        def tryScan(syns: Seq[Synonym]): Unit =
                                            for (syn <- syns if !found)
                                                NCSynonymsManager.onMatch(
                                                    ns.srvReqId,
                                                    elemId,
                                                    syn,
                                                    toks,
                                                    _ => {
                                                        found = true
                                                        add("simple continuous scan", toksExt, syn)
                                                    }
                                                )

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
                                for (syn <- get(mdl.sparseSynonyms, elemId))
                                    NCSynonymsManager.onSparseMatch(
                                        ns.srvReqId,
                                        elemId,
                                        syn,
                                        toks,
                                        res => add("simple sparse", getSparsedTokens(res, toks), syn)
                                    )
                        }

                        // 2. IDL.
                        if (idlEnabled) {
                            val allSyns = get(mdl.idlSynonyms, elemId)
                            lazy val allCombs = mkCombinations(ch, toks)

                            // 2.1 Continuous.
                            if (!mdl.hasSparseSynonyms) {
                                var found = false

                                for (syn <- allSyns; comb <- allCombs;  if !found)
                                    NCSynonymsManager.onMatch(
                                        ns.srvReqId,
                                        elemId,
                                        syn,
                                        comb,
                                        req,
                                        variantsToks,
                                        _ => {
                                            val parts = toParts(mdl, ns.srvReqId, comb, syn)

                                            add("IDL continuous", toksExt, syn, parts)

                                            found = true
                                        }
                                    )
                            }
                            else
                            // 2.2 Sparse.
                                for (syn <- allSyns; comb <- allCombs)
                                    NCSynonymsManager.onSparseMatch(
                                        ns.srvReqId,
                                        elemId,
                                        syn,
                                        comb,
                                        req,
                                        variantsToks,
                                        res => {
                                            val toks = getSparsedTokens(toNlpTokens(res, ns), toNlpTokens(comb, ns))
                                            val parts = toParts(mdl, ns.srvReqId, res, syn)
                                            val typ = if (syn.sparse) "IDL sparse"else "IDL continuous"

                                            add(typ, toks, syn, parts)
                                        }
                                    )
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

        NCSynonymsManager.clearIteration(ns.srvReqId)

        normalize(ns)
    }

    /**
      *
      * @param ns
      */
    private def normalize(ns: Sentence): Unit = {
        // Find and removes user notes if sentence contains notes with similar structure but less count of swallowed stop-words.
        // These stop-words can be used for detection another user tokens and if they are free words it is harmless too.
        // Ignored notes with links and with references on them.
        val usrNotes = ns.flatten.filter(_.isUser).distinct
        val links = NCSentenceManager.getLinks(usrNotes)
        val parts = NCSentenceManager.getPartKeys(usrNotes)

        val usrNotesIdxs = usrNotes.
            filter(n => !links.contains(NoteLink(n.noteType, n.tokenIndexes.sorted))).
            filter(n => !parts.contains(NCTokenPartKey(n, ns))).
            zipWithIndex

        usrNotesIdxs.
            foreach { case (n, idx) =>
                usrNotesIdxs.find { case (candidate, candidateIdx) =>
                    candidateIdx != idx &&
                        candidate.noteType == n.noteType &&
                        candidate.dataOpt("parts") == n.dataOpt("parts") &&
                        candidate.wordIndexesSet.subsetOf(n.wordIndexesSet) &&
                        n.wordIndexes.filter(n => !candidate.wordIndexes.contains(n)).
                            forall(wordIdx => ns.tokens.exists(t => t.wordIndexes.contains(wordIdx) && t.isStopWord))
                } match {
                    case Some(better) =>
                        ns.removeNote(n)

                        logger.trace(s"Element removed: $n, better: $better")
                    case None => // No-op.
                }
            }
    }

    // TODO: simplify, add tests, check model properties (sparse etc) for optimization.
    /**
      *
      * @param elmId Element ID.
      * @param toks Tokens.
      * @param idxs Indexes, note that it can be not exactly tokens indexes (sparse case)
      */
    private def alreadyMarked(ns: Sentence, elmId: String, toks: Seq[NlpToken], idxs: Seq[Int]): Boolean = {
        lazy val toksIdxsSorted = toks.map(_.index).sorted

        // All tokens with given indexes found with zero sparsity.
        val ok1 = idxs.map(ns).forall(_.exists(n => n.noteType == elmId && n.sparsity == 0))

        lazy val ok2 =
            toks.exists(_.exists(n =>
                if (n.noteType == elmId) {
                    val noteOk1 = n.sparsity == 0 &&
                        (idxs.containsSlice(n.tokenIndexes) || n.tokenIndexes.containsSlice(toksIdxsSorted))

                    lazy val noteOk2 =
                        n.tokenIndexes == toksIdxsSorted ||
                        n.tokenIndexes.containsSlice(toksIdxsSorted) &&
                        U.isContinuous(toksIdxsSorted) &&
                        U.isContinuous(n.tokenIndexes)

                    noteOk1 || noteOk2
                }
                else
                    false
            ))

        ok1 || ok2
    }
}
