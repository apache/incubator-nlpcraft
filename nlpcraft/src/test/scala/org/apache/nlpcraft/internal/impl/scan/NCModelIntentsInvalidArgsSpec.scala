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
import nlp.enrichers.*
import nlp.util.*
import internal.impl.{NCCallbackInput, NCModelScanner}
import org.scalatest.funsuite.AnyFunSuite

import java.util

/**
  * It tests invalid intents methods parameters types usage.
  * Note that for some kind of models (it depends on creation type) we can't check methods arguments during scan.
  */
class NCModelIntentsInvalidArgsSpec extends AnyFunSuite:
    class DefinedClassModelValid extends NCTestModelAdapter:
        @NCIntent("intent=validList term(list)~{# == 'x'}[0,10]")
        def validList(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("list") list: List[NCEntity]): NCResult = processListEntity(list)

        @NCIntent("intent=validOpt term(opt)~{# == 'x'}?")
        def validOpt(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("opt") opt: Option[NCEntity]): NCResult = processOptEntity(opt)

    class DefinedClassModelInvalidLst extends NCTestModelAdapter:
        @NCIntent("intent=invalidList term(list)~{# == 'x'}[0,10]")
        def invalidList(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("list") list: List[Int]): NCResult = processListInt(list)

    class DefinedClassModelInvalidArg1 extends NCTestModelAdapter:
        @NCIntent("intent=invalidList term(list)~{# == 'x'}[0,10]")
        def invalidArg(ctx: NCContext, @NCIntentTerm("list") list: List[Int]): NCResult = processListInt(list)

    class DefinedClassModelInvalidArg2 extends NCTestModelAdapter:
        @NCIntent("intent=invalidList term(list)~{# == 'x'}[0,10]")
        def invalidArg(im: NCIntentMatch, @NCIntentTerm("list") list: List[Int]): NCResult = processListInt(list)

    class DefinedClassModelInvalidArg3 extends NCTestModelAdapter:
        @NCIntent("intent=invalidList term(list)~{# == 'x'}[0,10]")
        def invalidList(im: NCIntentMatch, ctx: NCContext, @NCIntentTerm("list") list: List[Int]): NCResult = processListInt(list)

    class DefinedClassModelInvalidOpt extends NCTestModelAdapter:
        @NCIntent("intent=invalidOpt term(opt)~{# == 'x'}?")
        def invalidOpt(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("opt") opt: Option[Int]): NCResult = processOptInt(opt)

    private val CHECKED_MDL_VALID: NCModel = new DefinedClassModelValid
    private val CHECKED_MDL_INVALID_LST: NCModel = new DefinedClassModelInvalidLst
    private val CHECKED_MDL_INVALID_OPT: NCModel = new DefinedClassModelInvalidOpt

    private val CHECKED_MDL_INVALID_ARG1: NCModel = new DefinedClassModelInvalidArg1
    private val CHECKED_MDL_INVALID_ARG2: NCModel = new DefinedClassModelInvalidArg2
    private val CHECKED_MDL_INVALID_ARG3: NCModel = new DefinedClassModelInvalidArg3

    private val UNCHECKED_MDL: NCModel =
        new NCTestModelAdapter:
            @NCIntent("intent=validList term(list)~{# == 'x'}[0,10]")
            def validList(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("list") list: List[NCEntity]): NCResult = processListEntity(list)

            @NCIntent("intent=validOpt term(opt)~{# == 'x'}?")
            def validOpt(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("opt") opt: Option[NCEntity]): NCResult = processOptEntity(opt)

            @NCIntent("intent=invalidList term(list)~{# == 'x'}[0,10]")
            def invalidList(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("list") list: List[Int]): NCResult = processListInt(list)

            @NCIntent("intent=invalidOpt term(opt)~{# == 'x'}?")
            def invalidOpt(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("opt") opt: Option[Int]): NCResult = processOptInt(opt)

    private val INPUT =
        val e = NCTestEntity("id", "reqId", tokens = NCTestToken())

        def col[T](t: T): List[T] = List(t)

        val im = new NCIntentMatch:
            override val getIntentId: String = "intentId"
            override val getIntentEntities: List[List[NCEntity]] = col(col(e))
            override def getTermEntities(idx: Int): List[NCEntity] = col(e)
            override def getTermEntities(termId: String): List[NCEntity] = col(e)
            override val getVariant: NCVariant = new NCVariant:
                override val getEntities: List[NCEntity] = col(e)

        NCCallbackInput(null, im)

    private def mkResult0(obj: Any): NCResult =
        println(s"Result body: $obj, class=${obj.getClass}")
        NCResult(obj)

    private def processOptInt(opt: Option[Int]): NCResult =
        // Access and cast.
        val body: Int = opt.get
        mkResult0(body)

    private def processOptEntity(opt: Option[NCEntity]): NCResult =
        // Access and cast.
        val body: NCEntity = opt.get
        mkResult0(body)

    private def processListInt(list: List[Int]): NCResult =
        // Access and cast.
        val bodyHead: Int = list.head
        mkResult0(list)

    private def processListEntity(list: List[NCEntity]): NCResult =
        // Access and cast.
        val bodyHead: NCEntity = list.head
        mkResult0(list)

    private def testOk(mdl: NCModel, intentId: String): Unit =
        val i = NCModelScanner.scan(mdl).find(_.intent.id == intentId).get

        println(s"Test finished [modelClass=${mdl.getClass}, intent=$intentId, result=${i.function(INPUT)}")

    private def testRuntimeClassCast(mdl: NCModel, intentId: String): Unit =
        val i = NCModelScanner.scan(mdl).find(_.intent.id == intentId).get

        try
            i.function(INPUT)

            require(false)
        catch
            case e: NCException =>
                if e.getCause != null && e.getCause.isInstanceOf[ClassCastException] then
                    println("Expected stack trace:")
                    e.printStackTrace(System.out)
                else
                    throw e

    private def testScanValidation(mdl: NCModel): Unit =
        try
            NCModelScanner.scan(mdl)

            require(false)
        catch
            case e: NCException =>
                println("Expected stack trace:")
                e.printStackTrace(System.out)

    test("test") {
        testOk(CHECKED_MDL_VALID, "validList")
        testOk(CHECKED_MDL_VALID, "validOpt")

        // Errors thrown on scan phase if error found in any intent.
        testScanValidation(CHECKED_MDL_INVALID_LST)
        testScanValidation(CHECKED_MDL_INVALID_OPT)

        // Missed or wrong 2 mandatory callback arguments.
        testScanValidation(CHECKED_MDL_INVALID_ARG1)
        testScanValidation(CHECKED_MDL_INVALID_ARG2)
        testScanValidation(CHECKED_MDL_INVALID_ARG3)

        testOk(UNCHECKED_MDL, "validList")
        testOk(UNCHECKED_MDL, "validOpt")
        testRuntimeClassCast(UNCHECKED_MDL, "invalidList")
        testRuntimeClassCast(UNCHECKED_MDL, "invalidOpt")
    }        