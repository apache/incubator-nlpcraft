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
import org.apache.nlpcraft.annotations.*
import org.apache.nlpcraft.internal.impl.NCModelScanner
import org.junit.jupiter.api.Test
/**
  * It tests imports and nested objects usage.
  */
class NCModelIntentsNestedSpec:
    private val MDL_VALID1: NCModel = new NCTestModelAdapter:
        @NCIntentObject
        val nested1: Object = new Object():
            @NCIntentObject
            val nested2: Object = new Object():
                @NCIntent("intent=intent3 term(x)~{true}")
                def intent1(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("x") x: NCEntity) = NCTestResult()

            @NCIntent("intent=intent2 term(x)~{true}")
            def intent1(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("x") x: NCEntity) = NCTestResult()

        @NCIntent("intent=intent1 term(x)~{true}")
        def intent1(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("x") x: NCEntity) = NCTestResult()

        @NCIntent("import('scan/idl.idl')")
        def intent4(
            ctx: NCContext,
            im: NCIntentMatch,
            @NCIntentTerm("single") single: NCEntity,
            @NCIntentTerm("list") list: List[NCEntity],
            @NCIntentTerm("opt") opt: Option[NCEntity]
        ): NCResult = NCTestResult()

    private val MDL_VALID2: NCModel = new NCTestModelAdapter:
        @NCIntent("import('scan/idl.idl')")
        class RefClass

        @NCIntentObject
        val nested1: Object = new Object():
            @NCIntentObject
            val nested2 = new RefClass():
                @NCIntent("intent=intent3 term(x)~{true}")
                def intent1(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("x") x: NCEntity) = NCTestResult()

            @NCIntent("intent=intent2 term(x)~{true}")
            def intent1(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("x") x: NCEntity) = NCTestResult()

        @NCIntent("intent=intent1 term(x)~{true}")
        def intent1(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("x") x: NCEntity) = NCTestResult()

        @NCIntentRef("impIntId") // Reference via nested2 (RefClass)
        def intent4(
            ctx: NCContext,
            im: NCIntentMatch,
            @NCIntentTerm("single") single: NCEntity,
            @NCIntentTerm("list") list: List[NCEntity],
            @NCIntentTerm("opt") opt: Option[NCEntity]
        ): NCResult = NCTestResult()

    private val MDL_INVALID: NCModel = new NCTestModelAdapter :
        @NCIntentObject
        val nested1: Object = new Object():
            @NCIntentObject
            val nested2: Object = null

    @Test
    def test(): Unit =
        require(NCModelScanner.scan(MDL_VALID1).sizeIs == 4)
        require(NCModelScanner.scan(MDL_VALID2).sizeIs == 4)

    @Test
    def testNull(): Unit =
        try
            NCModelScanner.scan(MDL_INVALID)

            require(false)
        catch
            case e: NCException =>
                println("Expected stack trace:")
                e.printStackTrace(System.out)