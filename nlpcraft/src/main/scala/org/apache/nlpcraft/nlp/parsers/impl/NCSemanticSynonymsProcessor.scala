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

package org.apache.nlpcraft.nlp.parsers.impl

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.yaml.*
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.makro.NCMacroParser
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.nlp.stemmer.NCStemmer
import org.apache.nlpcraft.nlp.parsers.*
import org.apache.nlpcraft.nlp.parsers.impl.NCSemanticChunkKind.*

import java.io.InputStream
import java.util
import java.util.regex.*
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  *
  * @param elementType
  * @param value
  */
private[parsers] case class NCSemanticSynonymsElementData(elementType: String, value: Option[String])

/**
  *
  * @param textSynonyms
  * @param mixedSynonyms
  */
private[parsers] case class NCSemanticSynonymsHolder(
    textSynonyms: Map[String, Set[NCSemanticSynonymsElementData]],
    mixedSynonyms: Map[Int, Map[String, Seq[NCSemanticSynonym]]]
)

/**
  *
  */
private[parsers] object NCSemanticSynonymsProcessor extends LazyLogging:
    private final val SUSP_SYNS_CHARS = Seq("?", "*", "+")
    private final val REGEX_FIX = "//"
    private final val TYPE_REGEX = "^[_a-zA-Z]+[a-zA-Z0-9:\\-_]*$"

    /**
      *
      * @param iter
      */
    private def hasNullOrEmpty(iter: Iterable[String]): Boolean = iter.exists(p => p == null || p.strip.isEmpty)

    /**
      *
      * @param macros
      * @param elements
      */
    private def checkMacros(macros: Map[String, String], elements: Seq[NCSemanticElement]): Unit =
        require(elements != null)

        if macros != null then
            if hasNullOrEmpty(macros.keySet) then E("Some macro names are null or empty.")
            if hasNullOrEmpty(macros.values) then E("Some macro bodies are null or empty.")

            val set = elements.filter(_.getSynonyms != null).flatMap(_.getSynonyms) ++ macros.values

            for (makro <- macros.keys if !set.exists(_.contains(makro)))
                logger.warn(s"Unused macro detected [macro=$makro]")

            def isSuspicious(s: String): Boolean = SUSP_SYNS_CHARS.exists(s.contains)

            // Ignore suspicious chars if regex is used in macro...
            for ((name, value) <- macros if isSuspicious(name) || (isSuspicious(value) && !value.contains("//")))
                logger.warn(s"Suspicious macro definition (use of ${SUSP_SYNS_CHARS.map(s => s"'$s'").mkString(", ")} chars) [macro=$name]")

    /**
      *
      * @param syns
      * @param elemType
      * @param valueName
      */
    private def checkSynonyms(syns: Set[String], elemType: String, valueName: Option[String] = None): Unit =
        def mkDesc: String =
            val valuePart = if valueName.isDefined then s", value=${valueName.get}" else ""
            s"[type=$elemType$valuePart]"

        if syns != null then
            if hasNullOrEmpty(syns) then E(s"Some synonyms are null or empty $mkDesc")
            val susp = syns.filter(syn => !syn.contains("//") && SUSP_SYNS_CHARS.exists(susp => syn.contains(susp)))
            if susp.nonEmpty then
                logger.warn(
                    s"Suspicious synonyms detected (use of ${SUSP_SYNS_CHARS.map(s => s"'$s'").mkString(", ")} chars) $mkDesc"
                )
    /**
      *
      * @param elems
      */
    private def checkElements(elems: Seq[NCSemanticElement]): Unit =
        if elems == null || elems.isEmpty then E("Elements cannot be null or empty.")
        if elems.contains(null) then E("Some elements are null.")

        // Duplicates.
        val types = mutable.HashSet.empty[String]

        for (typ <- elems.map(_.getType))
            if types.contains(typ) then E(s"Duplicate element type [type=$typ]")
            else types += typ

        for (e <- elems)
            val typ = e.getType

            if typ == null || typ.isEmpty then E(s"Some element types are not provided or empty.")
            else if !typ.matches(TYPE_REGEX) then E(s"Element type does not match regex [type=$typ, regex=$TYPE_REGEX]")
            else if typ.exists(_.isWhitespace) then E(s"Element type cannot have whitespaces [type=$typ]")

            checkSynonyms(e.getSynonyms, typ)

            val vals = e.getValues
            if vals != null then
                if hasNullOrEmpty(vals.keySet) then E(s"Some values names are null or empty [element=$typ]")
                for ((name, syns) <- vals)
                    checkSynonyms(syns, typ, name.?)

    /**
      *
      * @param stemmer
      * @param tokParser
      * @param macroParser
      * @param elemType
      * @param syns
      */
    private def convertSynonyms(
        stemmer: NCStemmer,
        tokParser: NCTokenParser,
        macroParser: NCMacroParser,
        elemType: String,
        syns: Set[String]
    ): List[List[NCSemanticSynonymChunk]] =
        case class RegexHolder(text: String, var used: Boolean = false):
            private def stripSuffix(fix: String, s: String): String = s.slice(fix.length, s.length - fix.length)

            def mkChunk(): NCSemanticSynonymChunk =
                val ptrn = stripSuffix(REGEX_FIX, text)

                if ptrn.nonEmpty then
                    try NCSemanticSynonymChunk(REGEX, text, regex = Pattern.compile(ptrn))
                    catch case e: PatternSyntaxException => E(s"Invalid regex synonym syntax detected [element=$elemType, chunk=$text]", e)
                else E(s"Empty regex synonym detected [element=$elemType]")

        val regexes = mutable.HashMap.empty[Int, RegexHolder]

        def findRegex(t: NCToken): Option[RegexHolder] =
            if regexes.nonEmpty then (t.getStartCharIndex to t.getEndCharIndex).flatMap(regexes.get).to(LazyList).headOption
            else None

        syns.flatMap(macroParser.expand).
            map(syn => {
                // Drops redundant spaces without any warnings.
                val normSyn = syn.split(" ").map(_.strip).filter(_.nonEmpty)

                var start = 0
                var end = -1
                regexes.clear()

                // Saves regex chunks positions. Regex chunks can be found without tokenizer, just split by spaces.
                for (ch <- normSyn)
                    start = end + 1
                    end = start + ch.length

                    if ch.startsWith(REGEX_FIX) && ch.endsWith(REGEX_FIX) then
                        val r = RegexHolder(ch)
                        (start to end).foreach(regexes += _ -> r)

                // Tokenizes synonym without regex chunks. Regex chunks are used as is, without tokenization.
                tokParser.tokenize(normSyn.mkString(" ")).flatMap(tok =>
                    findRegex(tok) match
                        case Some(regex) =>
                            if regex.used then None
                            else
                                regex.used = true
                                regex.mkChunk().?
                        case None => NCSemanticSynonymChunk(TEXT, tok.getText, stemmer.stem(tok.getText.toLowerCase)).?
                ).toList
            }).toList.filter(_.nonEmpty)

    /**
      *
      * @param stemmer
      * @param tokParser
      * @param macros
      * @param elements
      */
    def prepare(
        stemmer: NCStemmer,
        tokParser: NCTokenParser,
        macros: Map[String, String],
        elements: Seq[NCSemanticElement]
    ): NCSemanticSynonymsHolder =
        require(stemmer != null && tokParser != null)

        // Order is important.
        checkElements(elements)
        checkMacros(macros, elements)

        val macroParser = new NCMacroParser

        if macros != null then for ((name, body) <- macros) macroParser.addMacro(name, body)

        case class Holder(synonym: NCSemanticSynonym, elementType: String):
            lazy val root: String = synonym.chunks.map(p => if p.isText then p.stem else p.text).mkString(" ")

        val buf = mutable.ArrayBuffer.empty[Holder]

        for (e <- elements)
            val elemType = e.getType

            def add(syns: Seq[NCSemanticSynonym]): Unit = buf ++= syns.map(Holder(_, elemType))
            def addSpec(txt: String, value: String = null): Unit =
                buf += Holder(NCSemanticSynonym(Seq(NCSemanticSynonymChunk(TEXT, txt, stemmer.stem(txt.toLowerCase))), value), elemType)

            addSpec(elemType)

            if e.getSynonyms != null then
                add(convertSynonyms(stemmer, tokParser, macroParser, elemType, e.getSynonyms).map(NCSemanticSynonym(_)))

            if e.getValues != null then
                for ((name, syns) <- e.getValues)
                    addSpec(name, value = name)

                    if syns != null then
                        add(
                            convertSynonyms(stemmer, tokParser, macroParser, elemType, syns).
                                map(chunks => NCSemanticSynonym(chunks, value = name))
                        )

        buf.groupBy(_.root).values.foreach(hs => {
            val elemTypes = hs.map(_.elementType).toSet

            if elemTypes.size > 1 then
                for (s <- hs.map(_.synonym).distinct)
                    logger.warn(s"Synonym appears in multiple elements [synonym='${s.chunks.mkString(" ")}', elements=${elemTypes.mkString("{", ",", "}")}]")
        })

        val txtBuf = buf.filter(_.synonym.isText)
        val txtSyns =
            txtBuf.groupBy(_.synonym.stem).
            map { (stem, hs) =>
                stem ->
                    hs.map(h =>
                        NCSemanticSynonymsElementData(h.elementType, Option.when(h.synonym.value != null)(h.synonym.value))
                    ).toSet
            }

        buf --= txtBuf

        val mixedSyns = buf.groupBy(_.synonym.size).
            map { (size, hs) => size -> hs.groupBy(_.elementType).map { (typ, hs) => typ -> hs.map(_.synonym).toSeq } }

        NCSemanticSynonymsHolder(txtSyns, mixedSyns)