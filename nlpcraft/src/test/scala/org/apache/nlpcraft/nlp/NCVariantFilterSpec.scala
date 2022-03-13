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
import org.apache.nlpcraft.nlp.entity.parser.NCNLPEntityParser
import org.apache.nlpcraft.nlp.util.NCTestUtils
import org.junit.jupiter.api.Test

import java.util
import java.util.List as JList
import scala.util.Using

/**
  *
  */
class NCVariantFilterSpec:
    private def test0(pipeline: NCModelPipeline, ok: Boolean): Unit =
        val mdl: NCModel = new NCModelAdapter(new NCModelConfig("test.id", "Test model", "1.0"), pipeline):
            @NCIntent("intent=i term(any)={true}")
            def onMatch(): NCResult = new NCResult("OK", NCResultType.ASK_RESULT)

        NCTestUtils.askSomething(mdl, ok)

    private def mkBuilder(): NCModelPipelineBuilder =
        new NCModelPipelineBuilder().
            withLanguage("EN").
            //  For intents matching, we have to add at least one entity parser.
            withEntityParser(new NCNLPEntityParser)

    private def mkPipeline(apply: NCModelPipelineBuilder => NCModelPipelineBuilder): NCModelPipeline = apply(mkBuilder()).build()

    @Test
    def test(): Unit =
        test0(
            mkBuilder().build(),
            true
        )

        test0(
            mkPipeline(_.withVariantFilter((req: NCRequest, cfg: NCModelConfig, variants: JList[NCVariant]) => util.Collections.emptyList())),
            false
        )