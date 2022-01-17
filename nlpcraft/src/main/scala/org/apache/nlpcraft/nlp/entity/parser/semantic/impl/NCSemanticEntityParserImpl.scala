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
package org.apache.nlpcraft.nlp.entity.parser.semantic.impl


import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.makro.NCMacroParser
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.nlp.entity.parser.semantic.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.impl.NCSemanticChunkKind.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.impl.NCSemanticSourceType.*

import java.io.*
import java.util.regex.*
import java.util.{List as JList, Map as Jmap}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/**
  *
  */
object NCSemanticEntityParserImpl:
    /**
      *
      * @param stemmer
      * @param parser
      * @param macros
      * @param elms
      * @return
      */
    def apply(
        stemmer: NCSemanticStemmer,
        parser: NCTokenParser,
        macros: Jmap[String, String],
        elms: JList[NCSemanticElement]
    ): NCSemanticEntityParserImpl =
        require(elms != null)

        new NCSemanticEntityParserImpl(
            stemmer,
            parser,
            macros = if macros == null then null else macros.asScala.toMap,
            elements = elms.asScala.toSeq
        )

    /**
      *
      * @param stemmer
      * @param parser
      * @param src
      * @return
      */
    def apply(stemmer: NCSemanticStemmer, parser: NCTokenParser, src: String): NCSemanticEntityParserImpl =
        require(src != null)

        new NCSemanticEntityParserImpl(stemmer, parser, mdlSrc = src, scrType = NCSemanticSourceType.detect(src))

    /**
      * @param baseTokens Tokens.
      * @param variants Variants without stopwords.
      */
    private case class Piece(baseTokens: Seq[NCToken], variants: Seq[Seq[NCToken]])

    /**
      *
      * @param t
      * @return
      */
    private def isStopWord(t: NCToken): Boolean = t.getOpt[Boolean]("stopword").orElse(false)

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
                combo,
                stops4Delete.
                    map(_.toSet).
                    map(del => combo.filter(t => !del.contains(t))).filter(_.nonEmpty).sortBy(-_.size)
            )
        })

import org.apache.nlpcraft.nlp.entity.parser.semantic.impl.NCSemanticEntityParserImpl.*

/**
  *
  * @param macros
  * @param elements
  */
class NCSemanticEntityParserImpl(
    stemmer: NCSemanticStemmer,
    parser: NCTokenParser,
    macros: Map[String, String] = null,
    elements: Seq[NCSemanticElement] = null,
    mdlSrc: String = null,
    scrType: NCSemanticSourceType = null
) extends NCEntityParser with LazyLogging:
    require(stemmer != null && parser != null)
    require(macros != null && elements != null || mdlSrc != null && scrType != null)

    private var synsHolder: NCSemanticSynonymsHolder = _
    private var elemsMap: Map[String, NCSemanticElement] = _

    init()

    /**
      *
      */
    private def init(): Unit =
        val (macros, elements, elemsMap) =
            def toMap(elems: Seq[NCSemanticElement]): Map[String, NCSemanticElement] = elems.map(p => p.getId -> p).toMap

            if mdlSrc != null then
                val src = NCSemanticSourceReader.read(new BufferedInputStream(NCUtils.getStream(mdlSrc)), scrType)

                logger.trace(s"Loaded resource: $mdlSrc")

                (src.macros, src.elements, toMap(src.elements))
            else
                (this.macros, this.elements, toMap(this.elements))

        this.synsHolder = NCSemanticSynonymsProcessor.prepare(stemmer, parser, macros, elements)
        this.elemsMap = elemsMap

    override def parse(req: NCRequest, cfg: NCModelConfig, toksList: JList[NCToken]): JList[NCEntity] =
        val toks = toksList.asScala.toSeq
        val stems = toks.map(p => p -> stemmer.stem(p.getText)).toMap

        if toks.exists(_.getOpt[Boolean]("stopword").isEmpty) then
            logger.warn("'stopword' property not found. Is stopword token enricher configured?")

        val cache = mutable.HashSet.empty[Seq[Int]] // Variants (tokens without stopwords) can be repeated.

        case class Holder(elemId: String, tokens: Seq[NCToken], value: Option[String]):
            private val idxs = tokens.map(_.getIndex).toSet
            def isSuperSet(toks: Seq[NCToken]): Boolean = idxs.size > toks.size && toks.map(_.getIndex).toSet.subsetOf(idxs)

        val hs = mutable.ArrayBuffer.empty[Holder]

        for (
            piece <- getPieces(toks) if !hs.exists(_.isSuperSet(piece.baseTokens));
            variant <- Seq(piece.baseTokens) ++ piece.variants
        )
            def add(elemId: String, value: Option[String]): Unit = hs += Holder(elemId, variant, value)

            val idxs = variant.map(_.getIndex)

            if cache.add(idxs) then
                synsHolder.textSynonyms.get(variant.map(t => stems(t)).mkString(" ")) match
                    case Some(elems) => elems.foreach(elem => add(elem.elementId, elem.value))
                    case None =>
                        for ((elemId, syns) <- synsHolder.mixedSynonyms.getOrElse(variant.size, Seq.empty))
                            var found = false

                            for (s <- syns if !found)
                                found =
                                    s.chunks.zip(variant).
                                        sortBy { (chunk, _) => if chunk.isText then 0 else 1 }.
                                        forall { (chunk, tok) =>
                                            if chunk.isText then
                                                chunk.stem == stems(tok)
                                            else
                                                def match0(txt: String) = chunk.regex.matcher(txt).matches()
                                                match0(tok.getText) || match0(tok.getText.toLowerCase)
                                        }

                                if found then add(elemId, Option.when(s.value != null)(s.value))

        hs.toSeq.map(h => {
            val e = elemsMap(h.elemId)
            new NCPropertyMapAdapter with NCEntity:
                if (e.getProperties != null) e.getProperties.asScala.foreach { (k, v) => put(s"${h.elemId}:$k", v) }

                h.value match
                    case Some(value) => put(s"${h.elemId}:value", value)
                    case None => // No-op.

                override val getTokens: JList[NCToken] = h.tokens.asJava
                override val getRequestId: String = req.getRequestId
                override val getId: String = h.elemId
        }).asJava