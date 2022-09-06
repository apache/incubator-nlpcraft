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

package org.apache.nlpcraft.nlp

import org.apache.nlpcraft.*
import annotations.*
import nlp.util.*
import nlp.parsers.*
import internal.util.NCResourceReader
import org.scalatest.funsuite.AnyFunSuite

import java.util
import scala.util.Using
/**
  *
  */
class NCVariantFilterSpec extends AnyFunSuite:
    private def test0(pipeline: NCPipeline, ok: Boolean): Unit =
        val mdl: NCModel = new NCModelAdapter(NCModelConfig("test.id", "Test model", "1.0"), pipeline):
            @NCIntent("intent=i term(any)={true}")
            def onMatch(ctx: NCContext, im: NCIntentMatch): NCResult = TEST_RESULT

        NCTestUtils.askSomething(mdl, ok)

    private def mkBuilder(): NCPipelineBuilder =
        new NCPipelineBuilder().
            withTokenParser(EN_TOK_PARSER).
            //  For intents matching, we have to add at least one entity parser.
            withEntityParser(new NCNLPEntityParser)

    private def mkPipeline(apply: NCPipelineBuilder => NCPipelineBuilder): NCPipeline = apply(mkBuilder()).build

    test("test") {
        test0(
            mkBuilder().build,
            true
        )

        test0(
            mkPipeline(_.withVariantFilter((_: NCRequest, _: NCModelConfig, _: List[NCVariant]) => List.empty)),
            false
        )
    }