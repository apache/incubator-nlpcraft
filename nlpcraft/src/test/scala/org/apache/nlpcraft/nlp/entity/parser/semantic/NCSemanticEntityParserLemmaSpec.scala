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

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.impl.*
import org.apache.nlpcraft.internal.util.*
import org.apache.nlpcraft.nlp.entity.parser.*
import org.apache.nlpcraft.nlp.token.enricher.*
import org.apache.nlpcraft.nlp.token.parser.*
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.*

import java.util
import java.util.{UUID, List as JList, Map as JMap, Set as JSet}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCSemanticEntityParserLemmaSpec:
    private val lemmaTokEnricher = new NCOpenNLPLemmaPosTokenEnricher(
        NCResourceReader.getPath("opennlp/en-pos-maxent.bin"),
        NCResourceReader.getPath("opennlp/en-lemmatizer.dict")
    )
    private val swTokEnricher = new NCEnStopWordsTokenEnricher
    private val tokParser = new NCOpenNLPTokenParser(NCResourceReader.getPath("opennlp/en-token.bin"))
    private val lemmaStemmer =
        new NCSemanticStemmer():
            override def stem(txt: String): String = if wrapped(txt) then unwrap(txt) else UUID.randomUUID().toString

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

    private def ask(txt: String, elems: Seq[NCSemanticTestElement], expVrnts: Seq[Seq[Data]]): Unit =
        val mgr = new NCModelPipelineManager(
            CFG,
            new NCModelPipelineBuilder().
                withTokenParser(tokParser).
                withTokenEnricher(lemmaTokEnricher).
                withTokenEnricher(swTokEnricher).
                // 1. Wraps lemmas.
                withTokenEnricher((req: NCRequest, cfg: NCModelConfig, toks: JList[NCToken]) =>
                    toks.forEach(t => t.put("lemma", wrap(t.get[String]("lemma"))))
                ).
                // 2. Semantic parser with fixed stemmer which stems only lemmas.
                withEntityParser(new NCSemanticEntityParser(lemmaStemmer, tokParser, elems.asJava)).
                build()
        )

        mgr.start()

        try
            val data = mgr.prepare(txt, null, "userId")

            NCTestUtils.printVariants(txt, data.variants)

            require(expVrnts.size == data.variants.size, s"Variant count: ${data.variants.size}, expected: ${expVrnts.size}")

            val vrnts = mutable.ArrayBuffer.empty[NCVariant] ++ data.variants

            for (expData <- expVrnts)
                val idx = vrnts.zipWithIndex.
                    find { case (v, idx) => expData == v.getEntities.asScala.map(e => Data(e.mkText(), e.getId)) }.
                    getOrElse(throw new AssertionError(s"Cannot find variant: $expData"))._2
                vrnts.remove(idx)

            require(vrnts.isEmpty)
        finally
            mgr.close()
    /**
      *
      */
    @Test
    def test(): Unit =
        import NCSemanticTestElement as E
        // Lemma.
        ask(
            "my test",
            Seq(E("X", synonyms = Set(deepWrap("my test")))),
            Seq(Seq(Data("my test", "X")))
        )

        // Regex.
        ask(
            "my test",
            Seq(E("X", synonyms = Set(wrap("my //[a-z]+//")))),
            Seq(Seq(Data("my test", "X")))
        )

        // Both.
        ask(
            "my test",
            Seq(E("X", synonyms = Set(deepWrap("my test"), wrap("my //[a-z]+//")))),
            Seq(Seq(Data("my test", "X")))
        )