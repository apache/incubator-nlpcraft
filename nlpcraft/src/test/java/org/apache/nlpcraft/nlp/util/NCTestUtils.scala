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

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.ascii.NCAsciiTable
import org.apache.nlpcraft.nlp.token.parser.opennlp.en.NCEnOpenNlpTokenParser

import scala.jdk.CollectionConverters.*

/**
  *
  */
object NCTestUtils:
    /**
      * @param toks
      */
    def printTokens(toks: Seq[NCToken]): Unit =
        val tbl = NCAsciiTable("Text", "Index", "POS", "Stem", "Lemma", "Start", "End", "Length", "Stopword", "Properties")

        for (t <- toks)
            tbl += (
                t.getText,
                t.getIndex,
                t.getPos,
                t.getStem,
                t.getLemma,
                t.getStartCharIndex,
                t.getEndCharIndex,
                t.getLength,
                t.isStopWord,
                t.keysSet().asScala.map(p => s"$p=${t.get[Any](p)}").mkString("[", ", ", "]")
            )

        tbl.print(s"Request: ${toks.map(_.getText).mkString(" ")}")

    /**
      *
      * @param req
      * @param ents
      */
    def printEntities(req: String, ents: Seq[NCEntity]): Unit =
        val tbl = NCAsciiTable("EntityId", "Tokens", "Properties")

        for (e <- ents)
            tbl += (
                e.getId,
                e.getTokens.asScala.map(_.getText).mkString("|"),
                e.keysSet().asScala.map(p => s"$p=${e.get[Any](p)}").mkString("{", ", ", "}")
            )

        tbl.print(s"Request: $req")

    /**
      *
      * @param make
      * @tparam T
      * @return
      */
    def makeAndStart[T <: NCLifecycle](make: => T): T =
        def now() = System.currentTimeMillis()

        val start = now()
        val t = make
        val started = now()
        
        t.start(null) // TODO: fix it.
        println(s"'${t.getClass.getSimpleName}' created in ${started - start}ms and started in ${now() - started}ms.")
        t

    /**
      *
       * @return
      */
    def mkEnParser: NCEnOpenNlpTokenParser = new NCEnOpenNlpTokenParser(
        "opennlp/en-token.bin",
        "opennlp/en-pos-maxent.bin",
        "opennlp/en-lemmatizer.dict"
    )