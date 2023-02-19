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
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Using

class NCIDLFragmentsOverridingSpec extends AnyFunSuite:
    @NCIntent("fragment=f term(x)~{# == 'x1'}")
    class M extends NCTestModelAdapter:
        // Uses fragment defined on class level.
        @NCIntent("intent=i1 fragment(f)")
        private def on1(ctx: NCContext, im: NCIntentMatch): NCResult = NCResult(1)

        // Overrides fragment defined on class level by its own.
        @NCIntent("fragment=f term(y)~{# == 'x2'} intent=i2 fragment(f)")
        private def on2(ctx: NCContext, im: NCIntentMatch): NCResult = NCResult(2)

        override val getPipeline: NCPipeline = mkEnPipeline(TE("x1"), TE("x2"))

    test("test") {
        Using.resource(new NCModelClient(new M())) { client =>
            def test(ps: (String, Int)*): Unit =
                ps.foreach { case (txt, res) => require(client.ask(txt, "usr").get.getBody == res) }

            test("x1" -> 1, "x2" -> 2)
        }
    }