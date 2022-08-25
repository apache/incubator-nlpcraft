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

import org.apache.nlpcraft.annotations.NCIntent
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.impl.NCModelScanner
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.Test

import scala.util.Using

class NCIDLFragmentsSpec:
    private val PL = mkEnPipeline

    private def mkCfg(id: String): NCModelConfig = NCModelConfig(id, "test", "1.0", desc = "Test", orig = "Test")

    // Normal models.

    // Fragment. One annotations order.
    @NCIntent("fragment=f term(city)~{# == 'opennlp:location'}")
    @NCIntent("intent=intent2 term~{# == 'x:time'} fragment(f)")
    class M1 extends NCModelAdapter(mkCfg("m1"), PL)

    // Fragment. Another annotations order.
    @NCIntent("intent=intent2 term~{# == 'x:time'} fragment(f)")
    @NCIntent("fragment=f term(city)~{# == 'opennlp:location'}")
    class M2 extends NCModelAdapter(mkCfg("m2"), PL)

    // Fragment. Reference from method to class.
    @NCIntent("fragment=f term(city)~{# == 'opennlp:location'}")
    class M3 extends NCModelAdapter(mkCfg("m3"), PL):
        @NCIntent("intent=intent2 term~{# == 'x:time'} fragment(f)")
        private def m(ctx: NCContext, im: NCIntentMatch): NCResult = null

    // Fragment. Reference from method (inside).
    class M4 extends NCModelAdapter(mkCfg("m4"), PL) :
        @NCIntent("fragment=f term(city)~{# == 'opennlp:location'} intent=intent2 term~{# == 'x:time'} fragment(f)")
        private def m(ctx: NCContext, im: NCIntentMatch): NCResult = null

    // Bad models.

    // Missed fragment definition.
    @NCIntent("intent=intent2 term~{# == 'x:time'} fragment(f)")
    class E1 extends NCModelAdapter(mkCfg("e1"), PL)

    // Attempt to reference on fragment defined in method.
    class E2 extends NCModelAdapter(mkCfg("e2"), PL):
        @NCIntent("fragment=f term(city)~{# == 'opennlp:location'} intent=intent1 term~{# == 'x:time'} fragment(f)")
        private def m1(ctx: NCContext, im: NCIntentMatch): NCResult = null

        @NCIntent("intent=intent2 term~{# == 'x:time'} fragment(f)")
        private def m2(ctx: NCContext, im: NCIntentMatch): NCResult = null

    // Attempt to reference on fragment defined in method.
    class E3 extends NCModelAdapter(mkCfg("e3"), PL):
        @NCIntent("fragment=f term(city)~{# == 'opennlp:location'} intent=intent1 term~{# == 'x:time'} fragment(f)")
        private def m2(ctx: NCContext, im: NCIntentMatch): NCResult = null

        @NCIntent("intent=intent2 term~{# == 'x:time'} fragment(f)")
        private def m1(ctx: NCContext, im: NCIntentMatch): NCResult = null

    private def testOk(mdls: NCModel*): Unit =
        for (mdl <- mdls)
            NCModelScanner.scan(mdl)
            println(s"Model valid: '${mdl.getConfig.getId}'")

    private def testError(mdls: NCModel*): Unit =
        for (mdl <- mdls)
            try
                NCModelScanner.scan(mdl)
                require(false, s"Model shouldn't be scanned: ${mdl.getConfig.getId}")
            catch case e: NCException => println(s"Model '${mdl.getConfig.getId}' expected error: '${e.getMessage}'")

    @Test
    def test(): Unit =
        testOk(
            new M1(),
            new M2(),
            new M3(),
            new M4()
        )

        testError(
            new E1(),
            new E2(),
            new E3()
        )