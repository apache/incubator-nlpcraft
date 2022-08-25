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
import org.apache.nlpcraft.annotations.NCIntent
import org.apache.nlpcraft.internal.impl.NCModelScanner
import org.apache.nlpcraft.nlp.parsers.NCSemanticTestElement as TE
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.Test

import scala.util.Using

class NCIDLFragmentsOverridingSpec:
    @NCIntent("fragment=f term(x)~{# == 'x'}")
    class M extends NCTestModelAdapter:
        override val getPipeline: NCPipeline =
            val pl = mkEnPipeline
            pl.entParsers += NCTestUtils.mkEnSemanticParser(TE("x"), TE("y"))
            pl

        // Uses fragment defined on class level.
        @NCIntent("intent=i2 fragment(f)")
        private def onX(ctx: NCContext, im: NCIntentMatch): NCResult = NCResult("onX")

        // Overrides fragment defined on class level.
        @NCIntent("fragment=f term(y)~{# == 'y'} intent=i1 fragment(f)")
        private def onY(ctx: NCContext, im: NCIntentMatch): NCResult = NCResult("onY")
    
    @Test
    def test(): Unit =
        Using.resource(new NCModelClient(new M())) { client =>
            require(client.ask("x", "usr").getBody == "onX")
            require(client.ask("y", "usr").getBody == "onY")
        }
