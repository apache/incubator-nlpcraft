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

package org.apache.nlpcraft.internal.impl.scan

import org.apache.nlpcraft.*
import annotations.*
import internal.impl.NCModelScanner

import org.junit.jupiter.api.Test
import java.util

/**
  * It tests invalid intents definition.
  */
class NCModelIntentsInvalidIntentsSpec:
    private def testError(mdl: NCModel): Unit =
        try
            NCModelScanner.scan(mdl)

            require(false)
        catch
            case e: NCException =>
                println("Expected stack trace:")
                e.printStackTrace(System.out)

    /**
      * Two intents on one method.
      */
    @Test
    def testError1(): Unit =
        testError(
            new NCTestModelAdapter():
                @NCIntent("intent=validList1 term(list)~{# == 'x'}[0,10]")
                @NCIntent("intent=validList2 term(list)~{# == 'x'}[0,10]")
                def x(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("list") list: List[NCEntity]): NCResult = null
        )

    /**
      * Invalid reference.
      */
    @Test
    def testError2(): Unit =
        testError(
            new NCTestModelAdapter():
                @NCIntentRef("missed")
                def x(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("list") list: List[NCEntity]): NCResult = null
        )

    /**
      * Duplicated intents definitions.
      */
    @Test
    def testError3(): Unit =
        @NCIntent("intent=validList1 term(list)~{# == 'x'}[0,10]")
        @NCIntent("intent=validList1 term(list)~{# == 'x'}[0,11]")
        class X:
            val x = 1

        testError(
            new NCTestModelAdapter():
                @NCIntentObject
                val x = new X()
        )

    /**
      * Invalid argument type.
      */
    @Test
    def testError4(): Unit =
        testError(
            new NCTestModelAdapter():
                @NCIntent("intent=validList1 term(list)~{# == 'x'}[0,10]")
                def x(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("list") e: NCEntity): NCResult = null
        )