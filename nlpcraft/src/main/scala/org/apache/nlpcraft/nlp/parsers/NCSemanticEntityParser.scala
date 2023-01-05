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

package org.apache.nlpcraft.nlp.parsers

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.makro.NCMacroParser
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.nlp.stemmer.NCStemmer
import org.apache.nlpcraft.nlp.parsers.*
import org.apache.nlpcraft.nlp.parsers.impl.*

import java.io.*
import java.util
import java.util.Objects
import java.util.regex.*
import scala.annotation.tailrec
import scala.collection.mutable

/**
  * [[NCSemanticEntityParser]] helper.
  */
private object NCSemanticEntityParser:
    /**
      * @param baseTokens Tokens.
      * @param variants Variants without stopwords.
      */
    private case class Piece(baseTokens: List[NCToken], variants: List[List[NCToken]])

    /**
      *
      * @param t
      */
    private def isStopWord(t: NCToken): Boolean = t.get[Boolean]("stopword").getOrElse(false)

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
      */
    @tailrec private def combine(data1: Seq[String], data2: Seq[String], i: Int = 0, tmp: Set[List[String]] = Set(List.empty)): Set[List[String]] =
        require(data1.sizeIs == data2.size)

        if data1.isEmpty then Set.empty
        else if i >= data1.size then tmp
        else combine(data1, data2, i + 1, tmp.map(_ :+ data1(i)) ++ tmp.map(_ :+ data2(i)))

import NCSemanticEntityParser.*

/**
  * **Semantic** [[NCEntityParser entity parser]] implementation.
  *
  * This synonyms based parser provides simple but very powerful way to find domain specific data in the input text.
  * It is configured via [[NCSemanticElement]] list which are represent [[NCEntity name entities]] and
  * can be produced by this parser.
  *
  * [[NCSemanticElement]] elements list can be configured via YAML or JSON files in special format or
  * programmatically prepared list of [[NCSemanticElement]] can be passed in this parser directly.
  *
  * [[NCSemanticElement]] elements synonyms can be based on special
  * [[https://nlpcraft.apache.org/built-in-entity-parser.html#macros macros]] definitions which can be provided
  * in YAML and JSON files or passed directly in case when programmatically prepared list of [[NCSemanticElement]] passed in this parser.
  *
  * See detailed description on the website [[https://nlpcraft.apache.org/built-in-entity-parser.html#parser-semantic Semantic Parser]].
  *
  * **NOTE:** [[NCSemanticElement]] synonyms, **stemmer** and **parser** parameters must be configured for the same language.
  * `stemmer` implementation language should be corresponded to other components of [[NCPipeline]], but
  * required `stemmer` implementation is independent from other components' stemmers.
  *
  * **NOTE:** that parser can produce different types of [[NCEntity]] instances and each input [[NCToken]] can be included into several output [[NCEntity]] instances.
  *
  * There are several constructors with different set of parameters.
  * - **stemmer** [[NCStemmer]] implementation which used for matching tokens and given [[NCSemanticElement]] synonyms.
  * - **parser** [[NCTokenParser]] implementation which used for given [[NCSemanticElement]] synonyms tokenization. It should be same implementation as used in [[NCPipeline.getTokenParser]].
  * - **macros** Macros map which are used for extracting [[NCSemanticElement]] synonyms defined via **macros**. Empty by default. Look more at the website [[https://nlpcraft.apache.org/built-in-entity-parser.html#macros Macros]].
  * - **elements** Programmatically prepared [[NCSemanticElement]] instances.
  * - **mdlRes** Relative path, absolute path, classpath resource or URL to YAML or JSON semantic model which contains [[NCSemanticElement]] definitions.
  *
  * @see [[NCSemanticElement]]
  */
class NCSemanticEntityParser private (
    stemmer: NCStemmer,
    parser: NCTokenParser,
    macros: Map[String, String],
    elements: List[NCSemanticElement],
    mdlResOpt: Option[String]
) extends NCEntityParser with LazyLogging:
    require(stemmer != null, "Stemmer cannot be null.")
    require(parser != null, "Token parser cannot be null.")
    require(macros != null, "Macroses cannot be null.")
    require(elements != null && elements.nonEmpty || mdlResOpt.isDefined, "Elements cannot be null or empty or model resource cannot be empty.")

    /**
      * Creates [[NCSemanticEntityParser]] instance.
      *
      * @param stemmer  [[NCStemmer]] implementation for synonyms language.
      * @param parser   [[NCTokenParser]] implementation.
      * @param macros   Macros map. Empty by default.
      * @param elements [[NCSemanticElement]] list.
      */
    def this(stemmer: NCStemmer, parser: NCTokenParser, macros: Map[String, String], elements: List[NCSemanticElement]) =
        this(stemmer, parser, macros, elements, None)

    /**
      *
      * Creates [[NCSemanticEntityParser]] instance.
      *
      * @param stemmer  [[NCStemmer]] implementation for synonyms language.
      * @param parser   [[NCTokenParser]] implementation.
      * @param elements [[NCSemanticElement]] list.
      */
    def this(stemmer: NCStemmer, parser: NCTokenParser, elements: List[NCSemanticElement]) =
        this(stemmer, parser, Map.empty, elements, None)

    /**
      *
      * Creates [[NCSemanticEntityParser]] instance.
      *
      * @param stemmer [[NCStemmer]] implementation for synonyms language.
      * @param parser  [[NCTokenParser]] implementation.
      * @param mdlRes  Relative path, absolute path, classpath resource or URL to YAML or JSON semantic model definition.
      */
    def this(stemmer: NCStemmer, parser: NCTokenParser, mdlRes: String) =
        this(stemmer, parser, Map.empty, List.empty, mdlRes.?)

    private lazy val scrType =
        require(mdlResOpt.isDefined)
        NCSemanticSourceType.detect(mdlResOpt.get)

    private var synsHolder: NCSemanticSynonymsHolder = _
    private var elemsMap: Map[String, NCSemanticElement] = _

    init()

    /**
      *
      */
    private def init(): Unit =
        val (macros, elements, elemsMap) =
            def toMap(elems: Seq[NCSemanticElement]): Map[String, NCSemanticElement] = elems.map(p => p.getType -> p).toMap

            mdlResOpt match
                case Some(mdlSrc) =>
                    val src = NCSemanticSourceReader.read(new BufferedInputStream(NCUtils.getStream(mdlSrc)), scrType)
                    logger.trace(s"Loaded resource: $mdlResOpt")
                    (src.macros, src.elements, toMap(src.elements))
                case None => (this.macros, this.elements, toMap(this.elements))

        this.synsHolder = NCSemanticSynonymsProcessor.prepare(stemmer, parser, macros, elements)
        this.elemsMap = elemsMap

    /**
      *
      * @param name
      */
    private def warnMissedProperty(name: String): Unit = logger.warn(s"'$name' property not found. Is proper token enricher configured?")

    /** @inheritdoc */
    override def parse(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): List[NCEntity] =
        if toks.exists(_.get[String]("stopword").isEmpty) then warnMissedProperty("stopword")

        val stems = toks.map(p => p -> stemmer.stem(p.getText.toLowerCase)).toMap
        val stems4Lemms =
            var ok = true
            val seq =
                for (t <- toks; lemmaOpt = t.get[String]("lemma") if ok)
                    yield
                        ok = lemmaOpt.isDefined
                        t -> lemmaOpt.orNull

            if ok then
                seq.toMap.map { (tok, lemma) => tok -> stemmer.stem(lemma.toLowerCase) }
            else
                warnMissedProperty("lemma")
                Map.empty

        val cache = mutable.HashSet.empty[Seq[Int]] // Variants (tokens without stopwords) can be repeated.

        case class Holder(elemType: String, tokens: List[NCToken], value: Option[String]):
            val tokensSet: Set[NCToken] = tokens.toSet
            val idxs: Set[Int] = tokensSet.map(_.getIndex)

            def isSuperSet(toks: Seq[NCToken]): Boolean = idxs.size > toks.size && toks.map(_.getIndex).toSet.subsetOf(idxs)

        var hs = mutable.ArrayBuffer.empty[Holder]

        for (piece <- getPieces(toks) if !hs.exists(_.isSuperSet(piece.baseTokens));
            variant <- Seq(piece.baseTokens) ++ piece.variants)
            def add(elemType: String, value: Option[String]): Unit = hs += Holder(elemType, variant, value)

            val idxs = variant.map(_.getIndex)
            if cache.add(idxs) then
                // Tries to search by stems.
                synsHolder.textSynonyms.get(variant.map(stems).mkString(" ")) match
                    case Some(elems) => elems.foreach(elem => add(elem.elementType, elem.value))
                    case None =>
                        // Combines stems(origin) and stems(lemma)
                        var found = false
                        if stems4Lemms.nonEmpty then
                            for (comb <- combine(variant.map(stems), variant.map(stems4Lemms)) if !found)
                                synsHolder.textSynonyms.get(comb.mkString(" ")) match
                                    case Some(elems) =>
                                        found = true
                                        elems.foreach(elem => add(elem.elementType, elem.value))
                                    case None => // No-op.
                        // With regex.
                        for ((elemType, syns) <- synsHolder.mixedSynonyms.getOrElse(variant.size, List.empty))
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

                                if found then add(elemType, Option.when(s.value != null)(s.value))

        // Deletes redundant.
        hs = hs.distinct

        val del = mutable.ArrayBuffer.empty[Holder]
        // 1. Look at each element with its value.
        for (((_, _), seq) <- hs.groupBy(h => (h.elemType, h.value)) if seq.size > 1)
            // 2. If some variants are duplicated - keep only one, with most tokens counts.
            val seqIdxs = seq.zipWithIndex

            for ((h, idx) <- seqIdxs if !del.contains(h))
                del ++= seqIdxs.filter { (_, oIdx) => oIdx != idx }.map { (h, _) => h }.filter(_.tokensSet.subsetOf(h.tokensSet))

        hs --= del

        hs.toSeq.map(h => {
            val e = elemsMap(h.elemType)
            new NCPropertyMapAdapter with NCEntity:
                if e.getProperties != null then e.getProperties.foreach { (k, v) => put(s"${h.elemType}:$k", v) }
                h.value match
                    case Some(value) => put(s"${h.elemType}:value", value)
                    case None => // No-op.

                override val getTokens: List[NCToken] = h.tokens
                override val getRequestId: String = req.getRequestId
                override val getType: String = h.elemType
                override val getGroups: Set[String] = e.getGroups
        }).toList