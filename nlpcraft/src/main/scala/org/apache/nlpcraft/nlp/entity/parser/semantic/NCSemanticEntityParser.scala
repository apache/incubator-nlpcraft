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

package org.apache.nlpcraft.nlp.entity.parser.semantic

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.makro.NCMacroParser
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.nlp.entity.parser.semantic.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.impl.*

import java.io.*
import java.util
import java.util.Objects
import java.util.regex.*
import scala.annotation.tailrec
import scala.collection.mutable

/**
  *
  */
object NCSemanticEntityParser:
    /**
      *
      * @param stemmer
      * @param parser
      * @param macros
      * @param elements
      * @return
      */
    def apply(
        stemmer: NCSemanticStemmer,
        parser: NCTokenParser,
        macros: Map[String, String],
        elements: List[NCSemanticElement]
    ): NCSemanticEntityParser =
        require(stemmer != null, "Stemmer cannot be null.")
        require(parser != null, "Parser cannot be null.")
        require(macros != null, "Macros cannot be null.")
        require(elements != null, "Elements cannot be null.")

        new NCSemanticEntityParser(stemmer, parser, macros = macros, elements = elements)

    /**
      *
      * @param stemmer
      * @param parser
      * @param elements
      * @return
      */
    def apply(
        stemmer: NCSemanticStemmer,
        parser: NCTokenParser,
        elements: List[NCSemanticElement]
    ): NCSemanticEntityParser =
        require(stemmer != null, "Stemmer cannot be null.")
        require(parser != null, "Parser cannot be null.")
        require(elements != null, "Elements cannot be null.")

        new NCSemanticEntityParser(stemmer, parser, macros = Map.empty, elements = elements)

    /**
      *
      * @param stemmer
      * @param parser
      * @param mdlSrc
      * @return
      */
    def apply(stemmer: NCSemanticStemmer, parser: NCTokenParser, mdlSrc: String): NCSemanticEntityParser =
        require(stemmer != null, "Stemmer cannot be null.")
        require(parser != null, "Parser cannot be null.")
        require(mdlSrc != null, "Model source cannot be null.")

        new NCSemanticEntityParser(stemmer, parser, mdlSrcOpt = Option(mdlSrc))

    /**
      * @param baseTokens Tokens.
      * @param variants Variants without stopwords.
      */
    private case class Piece(baseTokens: List[NCToken], variants: List[List[NCToken]])

    /**
      *
      * @param t
      * @return
      */
    private def isStopWord(t: NCToken): Boolean = t.getOpt[Boolean]("stopword").getOrElse(false)

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
    private def getPieces(toks: Seq[NCToken]): Seq[Piece] =
        (for (n <- toks.size until 0 by -1) yield toks.sliding(n)).flatten.map(p => p).map(combo => {
            val stops = combo.filter(s => isStopWord(s) && s != combo.head && s != combo.last)
            val slides = mutable.ArrayBuffer.empty[mutable.ArrayBuffer[NCToken]]

            for (stop <- stops)
                if slides.nonEmpty && slides.last.last.getIndex + 1 == stop.getIndex then
                    slides.last += stop
                else
                    slides += mutable.ArrayBuffer.empty :+ stop

            // Too many stopwords inside skipped.
            val bigSlides = slides.filter(_.size > 2)

            var stops4Delete =
                if bigSlides.nonEmpty then
                    val allBig = bigSlides.flatten
                    val stops4AllCombs = stops.filter(p => !allBig.contains(p))

                    if stops4AllCombs.nonEmpty then
                        for (
                            seq1 <- Range.inclusive(0, stops4AllCombs.size).flatMap(stops4AllCombs.combinations);
                            seq2 <- Range.inclusive(0, bigSlides.size).flatMap(bigSlides.combinations)
                        )
                        yield seq1 ++ seq2.flatten
                    else
                        for (seq <- Range.inclusive(0, bigSlides.size).flatMap(bigSlides.combinations))
                            yield seq.toSeq.flatten
                else
                    Range.inclusive(1, stops.size).flatMap(stops.combinations)

            stops4Delete = stops4Delete.filter(seq => !seq.contains(combo.head) && !seq.contains(combo.last))

            Piece(
                combo.toList,
                stops4Delete.
                    map(_.toSet).
                    map(del => combo.filter(t => !del.contains(t)).toList).filter(_.nonEmpty).sortBy(-_.size).
                    toList
            )
        })

    /**
      * Multiply 2 data sets.
      * Examples: if input is (A, B) and (1, 2) output will be ((A, B), (1, 2), (A, 2), (1, B))
      *
      * @param data1
      * @param data2
      * @param i
      * @param tmp
      * @return
      */
    @tailrec private def combine(data1: Seq[String], data2: Seq[String], i: Int = 0, tmp: Set[List[String]] = Set(List.empty)): Set[List[String]] =
        require(data1.size == data2.size)

        if data1.isEmpty then Set.empty
        else if i >= data1.size then tmp
        else combine(data1, data2, i + 1, tmp.map(_ :+ data1(i)) ++ tmp.map(_ :+ data2(i)))

import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticEntityParser.*

/**
  *
  * @param stemmer
  * @param parser
  * @param macros
  * @param elements
  * @param mdlSrcOpt
  */
class NCSemanticEntityParser(
    stemmer: NCSemanticStemmer,
    parser: NCTokenParser,
    macros: Map[String, String] = Map.empty,
    elements: List[NCSemanticElement] = List.empty,
    mdlSrcOpt: Option[String] = None
) extends NCEntityParser with LazyLogging:
    require(stemmer != null)
    require(parser != null)
    require(macros != null)
    require(elements != null && elements.nonEmpty || mdlSrcOpt.isDefined)

    private lazy val scrType =
        require(mdlSrcOpt.isDefined)
        NCSemanticSourceType.detect(mdlSrcOpt.get)

    private var synsHolder: NCSemanticSynonymsHolder = _
    private var elemsMap: Map[String, NCSemanticElement] = _

    init()
    /**
      *
      */
    private def init(): Unit =
        val (macros, elements, elemsMap) =
            def toMap(elems: Seq[NCSemanticElement]): Map[String, NCSemanticElement] = elems.map(p => p.getId -> p).toMap

            mdlSrcOpt match
                case Some(mdlSrc) =>
                    val src = NCSemanticSourceReader.read(new BufferedInputStream(NCUtils.getStream(mdlSrc)), scrType)
                    logger.trace(s"Loaded resource: $mdlSrcOpt")
                    (src.macros, src.elements, toMap(src.elements))
                case None => (this.macros, this.elements, toMap(this.elements))

        this.synsHolder = NCSemanticSynonymsProcessor.prepare(stemmer, parser, macros, elements)
        this.elemsMap = elemsMap

    /**
      *
      * @param name
      */
    private def warnMissedProperty(name: String): Unit = logger.warn(s"'$name' property not found. Is proper token enricher configured?")

    override def parse(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): List[NCEntity] =
        if toks.exists(_.getOpt[String]("stopword").isEmpty) then warnMissedProperty("stopword")

        val stems = toks.map(p => p -> stemmer.stem(p.getText.toLowerCase)).toMap
        val stems4Lemms =
            var ok = true
            val seq =
                for (t <- toks; lemmaOpt = t.getOpt[String]("lemma") if ok)
                    yield
                        ok = lemmaOpt.isDefined
                        t -> lemmaOpt.orNull

            if ok then
                seq.toMap.map { (tok, lemma) => tok -> stemmer.stem(lemma.toLowerCase) }
            else
                warnMissedProperty("lemma")
                Map.empty

        val cache = mutable.HashSet.empty[Seq[Int]] // Variants (tokens without stopwords) can be repeated.

        case class Holder(elemId: String, tokens: List[NCToken], value: Option[String]):
            val tokensSet: Set[NCToken] = tokens.toSet
            val idxs: Set[Int] = tokensSet.map(_.getIndex)

            def isSuperSet(toks: Seq[NCToken]): Boolean = idxs.size > toks.size && toks.map(_.getIndex).toSet.subsetOf(idxs)

        var hs = mutable.ArrayBuffer.empty[Holder]

        for (piece <- getPieces(toks) if !hs.exists(_.isSuperSet(piece.baseTokens));
            variant <- Seq(piece.baseTokens) ++ piece.variants)
            def add(elemId: String, value: Option[String]): Unit = hs += Holder(elemId, variant, value)

            val idxs = variant.map(_.getIndex)
            if cache.add(idxs) then
                // Tries to search by stems.
                synsHolder.textSynonyms.get(variant.map(stems).mkString(" ")) match
                    case Some(elems) => elems.foreach(elem => add(elem.elementId, elem.value))
                    case None =>
                        // Combines stems(origin) and stems(lemma)
                        var found = false
                        if stems4Lemms.nonEmpty then
                            for (comb <- combine(variant.map(stems), variant.map(stems4Lemms)) if !found)
                                synsHolder.textSynonyms.get(comb.mkString(" ")) match
                                    case Some(elems) =>
                                        found = true
                                        elems.foreach(elem => add(elem.elementId, elem.value))
                                    case None => // No-op.
                        // With regex.
                        for ((elemId, syns) <- synsHolder.mixedSynonyms.getOrElse(variant.size, List.empty))
                            found = false

                            for (s <- syns if !found)
                                found = s.chunks.zip(variant).
                                    sortBy { (chunk, _) => if chunk.isText then 0 else 1 }.
                                    forall { (chunk, tok) =>
                                        if chunk.isText then
                                            chunk.stem == stems(tok) || (stems4Lemms.nonEmpty && chunk.stem == stems4Lemms(tok))
                                        else
                                            def match0(txt: String) = chunk.regex.matcher(txt).matches()
                                            match0(tok.getText) || match0(tok.getText.toLowerCase)
                                    }

                                if found then add(elemId, Option.when(s.value != null)(s.value))

        // Deletes redundant.
        hs = hs.distinct
        
        val del = mutable.ArrayBuffer.empty[Holder]
        // 1. Look at each element with its value.
        for (((_, _), seq) <- hs.groupBy(h => (h.elemId, h.value)) if seq.size > 1)
            // 2. If some variants are duplicated - keep only one, with most tokens counts.
            val seqIdxs = seq.zipWithIndex

            for ((h, idx) <- seqIdxs if !del.contains(h))
                del ++= seqIdxs.filter { (_, oIdx) => oIdx != idx }.map { (h, _) => h }.filter(_.tokensSet.subsetOf(h.tokensSet))

        hs --= del

        hs.toSeq.map(h => {
            val e = elemsMap(h.elemId)
            new NCPropertyMapAdapter with NCEntity:
                if e.getProperties != null then e.getProperties.foreach { (k, v) => put(s"${h.elemId}:$k", v) }
                h.value match
                    case Some(value) => put(s"${h.elemId}:value", value)
                    case None => // No-op.

                override val getTokens: List[NCToken] = h.tokens
                override val getRequestId: String = req.getRequestId
                override val getId: String = h.elemId
                override val getGroups: Set[String] = e.getGroups
        }).toList