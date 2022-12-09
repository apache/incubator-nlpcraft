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

import org.apache.nlpcraft.*
import annotations.*
import nlp.parsers.*
import internal.impl.*
import nlp.util.*
import org.apache.nlpcraft.nlp.common.NCStemmer
import org.scalatest.funsuite.AnyFunSuite

import java.util
import java.util.UUID
import scala.collection.mutable
/**
  *
  */
class NCSemanticEntityParserLemmaSpec extends AnyFunSuite:
    private val lemmaStemmer =
        new NCStemmer():
            override def stem(word: String): String = if wrapped(word) then unwrap(word) else UUID.randomUUID().toString

    case class Data(text: String, elemId: String)

    private def wrap(s: String): String =
        require(s != null)
        s"_$s"

    private def deepWrap(s: String): String =
        require(s != null)
        s.split(" ").map(wrap).mkString(" ")

    private def wrapped(s: String): Boolean = s.length > 1 && s.head == '_'
    private def unwrap(s: String): String =
        require(wrapped(s))
        s.drop(1)

    private def ask(txt: String, elems: List[NCSemanticTestElement], expVrnts: List[List[Data]]): Unit =
        val mgr = new NCModelPipelineManager(
            CFG,
            new NCPipelineBuilder().
                withTokenParser(EN_TOK_PARSER).
                withTokenEnricher(EN_TOK_LEMMA_POS_ENRICHER).
                withTokenEnricher(EN_TOK_STOP_ENRICHER).
                // 1. Wraps lemmas.
                withTokenEnricher((_: NCRequest, _: NCModelConfig, toks: List[NCToken]) =>
                    toks.foreach(t => t.put("lemma", wrap(t[String]("lemma"))))
                ).
                // 2. Semantic parser with fixed stemmer which stems only lemmas.
                withEntityParser(NCSemanticEntityParser(lemmaStemmer, EN_TOK_PARSER, elems)).
                build
        )

        mgr.start()

        try
            val data = mgr.prepare(txt, null, "userId")

            NCTestUtils.printVariants(txt, data.variants)

            require(expVrnts.size == data.variants.size, s"Variant count: ${data.variants.size}, expected: ${expVrnts.size}")

            val vrnts = mutable.ArrayBuffer.empty[NCVariant] ++ data.variants

            for (expData <- expVrnts)
                val idx = vrnts.zipWithIndex.
                    find { case (v, _) => expData == v.getEntities.map(e => Data(e.mkText, e.getId)) }.
                    getOrElse(throw new AssertionError(s"Cannot find variant: $expData"))._2
                vrnts.remove(idx)

            require(vrnts.isEmpty)
        finally
            mgr.close()
    /**
      *
      */
    test("test") {
        // Lemma.
        ask(
            "my test",
            List(NCSemanticTestElement("X", synonyms = Set(deepWrap("my test")))),
            List(List(Data("my test", "X")))
        )

        // Regex.
        ask(
            "my test",
            List(NCSemanticTestElement("X", synonyms = Set(wrap("my //[a-z]+//")))),
            List(List(Data("my test", "X")))
        )

        // Both.
        ask(
            "my test",
            List(NCSemanticTestElement("X", synonyms = Set(deepWrap("my test"), wrap("my //[a-z]+//")))),
            List(List(Data("my test", "X")))
        )
    }