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

package org.apache.nlpcraft.internal.impl

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.entity.parser.{NCEnSemanticEntityParser, NCSemanticElement}
import org.apache.nlpcraft.nlp.entity.parser.semantic.*
import org.apache.nlpcraft.nlp.entity.parser.impl.NCNLPEntityParserImpl
import org.apache.nlpcraft.nlp.util.*
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.function.Executable

import java.util
import java.util.List as JList
import java.util.concurrent.*
import scala.concurrent.CancellationException
import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCModelPipelineManagerSpec:
    /**
      *
      */
    @Test
    def test(): Unit =
        def test(txt: String, variantCnt: Int, elements: NCSemanticElement*): Unit =
            val pipeline = EN_PIPELINE.clone()

            val parser = new NCEnSemanticEntityParser(elements.asJava)
            pipeline.getEntityParsers.clear()
            pipeline.getEntityParsers.add(parser)

            val res = new NCModelPipelineManager(CFG, pipeline).prepare(txt, null, "userId")

            println(s"Variants count: ${res.variants.size}")
            for ((v, idx) <- res.variants.zipWithIndex)
                println(s"Variant: $idx")
                NCTestUtils.printEntities(txt, v.getEntities.asScala.toSeq)

            require(res.variants.sizeIs == variantCnt)

        test("t1 t2", 4, NCSemanticTestElement("t1", "t2"), NCSemanticTestElement("t2", "t1"))
        test("t1 t2", 2, NCSemanticTestElement("t1", "t2"), NCSemanticTestElement("t2"))

