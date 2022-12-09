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

package org.apache.nlpcraft.nlp.util

import opennlp.tools.stemmer.PorterStemmer
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.ascii.NCAsciiTable
import org.apache.nlpcraft.internal.util.NCResourceReader
import org.apache.nlpcraft.nlp.common.NCStemmer
import org.apache.nlpcraft.nlp.parsers.*
import org.apache.nlpcraft.nlp.parsers
import org.apache.nlpcraft.nlp.parsers.{NCOpenNLPTokenParser, NCSemanticElement, NCSemanticEntityParser}

import java.util
import scala.util.Using

/**
  *
  */
object NCTestUtils:
    /**
      *
      * @param m
      */
    private def mkProps(m: NCPropertyMap): String =
        m.keysSet.toSeq.sorted.map(p => s"$p=${m[Any](p)}").mkString("{", ", ", "}")

    /**
      * @param toks
      */
    def printTokens(toks: Iterable[NCToken]): Unit =
        val tbl = NCAsciiTable("Text", "Index", "Stopword", "Start", "End", "Properties")

        for (t <- toks)
            tbl += (
                t.getText,
                t.getIndex,
                t.get[Boolean]("stopword") match
                    case Some(b) => b.toString
                    case None => "undef."
                ,
                t.getStartCharIndex,
                t.getEndCharIndex,
                mkProps(t)
            )

        tbl.print(s"Request: ${toks.map(_.getText).mkString(" ")}")

    /**
      *
      * @param req
      * @param ents
      */
    def printEntities(req: String, ents: Seq[NCEntity]): Unit =
        val tbl = NCAsciiTable("EntityId", "Tokens", "Tokens Position", "Properties")

        for (e <- ents)
            val toks = e.getTokens
            tbl += (
                e.getId,
                toks.map(_.getText).mkString("|"),
                toks.map(p => s"${p.getStartCharIndex}-${p.getEndCharIndex}").mkString("|"),
                mkProps(e)
            )

        tbl.print(s"Request: $req")

    /**
      *
      * @param req
      * @param vs
      */
    def printVariants(req: String, vs: Seq[NCVariant]): Unit =
        println(s"Request $req variants:")

        for ((v, idx) <- vs.zipWithIndex)
            val tbl = NCAsciiTable("EntityId", "Tokens", "Tokens Position", "Properties")

            for (e <- v.getEntities)
                val toks = e.getTokens
                tbl += (
                    e.getId,
                    toks.map(_.getText).mkString("|"),
                    toks.map(p => s"${p.getStartCharIndex}-${p.getEndCharIndex}").mkString("|"),
                    mkProps(e)
                )

            tbl.print(s"Variant: ${idx + 1}")

    /**
      *
      * @param mdl
      * @param expectedOk
      */
    def askSomething(mdl: NCModel, expectedOk: Boolean): Unit =
        Using.resource(new NCModelClient(mdl)) { client =>
            def ask(): NCResult = client.ask("test", "userId")

            if expectedOk then
                println(ask().getBody)
            else
                try
                    ask()
                    require(false)
                catch case e: Exception => println(s"Expected error: ${e.getMessage}")
        }

    /**
      *
      */
    private def mkSemanticStemmer: NCStemmer =
        new NCStemmer():
            private val ps = new PorterStemmer
            override def stem(txt: String): String = ps.synchronized { ps.stem(txt) }


    /**
      *
      * @param elms
      * @param macros
      */
    def mkEnSemanticParser(elms: List[NCSemanticElement], macros: Map[String, String] = Map.empty): NCSemanticEntityParser =
        parsers.NCSemanticEntityParser(mkSemanticStemmer, EN_TOK_PARSER, macros, elms)

    /**
      *
      * @param elms
      */
    def mkEnSemanticParser(elms: NCSemanticElement*): NCSemanticEntityParser =
        parsers.NCSemanticEntityParser(mkSemanticStemmer, EN_TOK_PARSER, elms.toList)

    /**
      *
      * @param mdlSrc
      */
    def mkEnSemanticParser(mdlSrc: String): NCSemanticEntityParser =
        parsers.NCSemanticEntityParser(mkSemanticStemmer, EN_TOK_PARSER, mdlSrc)