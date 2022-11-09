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
import org.apache.nlpcraft.nlp.util.*
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Using

class NCIDLFragmentsSpec extends AnyFunSuite:
    // Normal models.

    // Fragment. One annotations order.
    @NCIntent("fragment=f term(x)~{# == 'x1'}")
    @NCIntent("intent=i1 term~{# == 'x2'} fragment(f)")
    class M1 extends NCModel(CFG, mkEmptyEnPipeline)

    // Fragment. Another annotations order.
    @NCIntent("intent=i1 term~{# == 'x2'} fragment(f)")
    @NCIntent("fragment=f term(x)~{# == 'x1'}")
    class M2 extends NCModel(CFG, mkEmptyEnPipeline)

    // Fragment. Reference from method to class.
    @NCIntent("fragment=f term(x)~{# == 'x1'}")
    class M3 extends NCModel(CFG, mkEmptyEnPipeline):
        @NCIntent("intent=i1 term~{# == 'x2'} fragment(f)")
        private def m(ctx: NCContext, im: NCIntentMatch): NCResult = null

    // Fragment. Reference from method (inside).
    class M4 extends NCModel(CFG, mkEmptyEnPipeline) :
        @NCIntent("fragment=f term(x)~{# == 'x1'} intent=i1 term~{# == 'x2'} fragment(f)")
        private def m(ctx: NCContext, im: NCIntentMatch): NCResult = null

    // Bad models.

    // Missed fragment definition.
    @NCIntent("intent=i2 term~{# == 'x2'} fragment(f)")
    class E1 extends NCModel(CFG, mkEmptyEnPipeline)

    // Attempt to reference on fragment defined in method.
    class E2 extends NCModel(CFG, mkEmptyEnPipeline):
        @NCIntent("fragment=f term(x)~{# == 'x1'} intent=i1 term~{# == 'x2'} fragment(f)")
        private def m1(ctx: NCContext, im: NCIntentMatch): NCResult = null

        @NCIntent("intent=i2 term~{# == 'x2'} fragment(f)")
        private def m2(ctx: NCContext, im: NCIntentMatch): NCResult = null

    // Attempt to reference on fragment defined in method.
    class E3 extends NCModel(CFG, mkEmptyEnPipeline):
        @NCIntent("fragment=f term(x)~{# == 'x1'} intent=i1 term~{# == 'x2'} fragment(f)")
        private def m2(ctx: NCContext, im: NCIntentMatch): NCResult = null

        @NCIntent("intent=i2 term~{# == 'x2'} fragment(f)")
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

    test("test") {
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
    }