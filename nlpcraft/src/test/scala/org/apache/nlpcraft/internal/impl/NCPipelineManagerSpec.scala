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
import org.apache.nlpcraft.nlp.parsers.*
import org.apache.nlpcraft.nlp.util.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.*


import java.util
import java.util.concurrent.*

/**
  *
  */
class NCPipelineManagerSpec extends AnyFunSuite:
    /**
      *
      */
    test("test") {
        def test(txt: String, variantCnt: Int, elements: NCSemanticElement*): Unit =
            val pipeline = mkEnPipeline

            pipeline.entParsers += NCTestUtils.mkEnSemanticParser(elements*)

            val res = new NCModelPipelineManager(CFG, pipeline).prepare(txt, null, "userId")

            println(s"Variants count: ${res.variants.size}")
            for ((v, idx) <- res.variants.zipWithIndex)
                println(s"Variant: $idx")
                NCTestUtils.printEntities(txt, v.getEntities)

            require(res.variants.sizeIs == variantCnt)

        test("t1 t2", 4, NCSemanticTestElement("t1", "t2"), NCSemanticTestElement("t2", "t1"))
        test("t1 t2", 2, NCSemanticTestElement("t1", "t2"), NCSemanticTestElement("t2"))
    }
