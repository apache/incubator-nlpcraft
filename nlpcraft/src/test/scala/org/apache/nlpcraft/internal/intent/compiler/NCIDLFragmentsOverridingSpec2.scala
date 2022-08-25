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

package org.apache.nlpcraft.internal.intent.compiler

import org.apache.nlpcraft.*
import org.apache.nlpcraft.annotations.{NCIntent, NCIntentRef}
import org.apache.nlpcraft.internal.impl.NCModelScanner
import org.apache.nlpcraft.nlp.parsers.NCSemanticTestElement as TE
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.Test

import scala.util.Using

class NCIDLFragmentsOverridingSpec2:
    @NCIntent("import('org/apache/nlpcraft/internal/intent/compiler/impl_level2.idl')")
    class M extends NCTestModelAdapter:
        // Uses initial fragment version (with intent), defined in impl_level1.idl.
        @NCIntent("import('org/apache/nlpcraft/internal/intent/compiler/impl_level1.idl')")
        @NCIntentRef("i1") private def on1(ctx: NCContext, im: NCIntentMatch): NCResult = NCResult(1)

        // Uses fragment, defined on impl_level2.idl, which overrides impl_level1.idl.
        @NCIntent("intent=i2 fragment(f)")
        private def on2(ctx: NCContext, im: NCIntentMatch): NCResult = NCResult(2)

        // Uses own fragment, which overrides all.
        @NCIntent("fragment=f term(z)~{# == 'x3'} intent=i3 fragment(f)")
        private def on3(ctx: NCContext, im: NCIntentMatch): NCResult = NCResult(3)

        override val getPipeline: NCPipeline =
            val pl = mkEnPipeline
            pl.entParsers += NCTestUtils.mkEnSemanticParser(TE("x1"), TE("x2"), TE("x3"))
            pl

    @Test
    def test(): Unit =
        Using.resource(new NCModelClient(new M())) { client =>
            require(client.ask("x1", "usr").getBody == 1)
            require(client.ask("x2", "usr").getBody == 2)
            require(client.ask("x3", "usr").getBody == 3)
        }