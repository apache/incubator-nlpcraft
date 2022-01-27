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
import org.apache.nlpcraft.internal.impl.NCModelScanner
import org.apache.nlpcraft.nlp.util.*
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.Test

import java.util

/**
  * It tests invalid intents methods parameters types usage.
  * Note that for some kind of models (it depends on creation type) we can't check methods arguments during scan.
  */
class NCModelIntentsInvalidArgsSpec:
    class DefinedClassModelValid extends NCTestModelAdapter:
        @NCIntent("intent=validList term(list)~{# == 'x'}[0,10]")
        def validList(@NCIntentTerm("list") list: List[NCEntity]): NCResult = processListEntity(list)

        @NCIntent("intent=validOpt term(opt)~{# == 'x'}?")
        def validOpt(@NCIntentTerm("opt") opt: Option[NCEntity]): NCResult = processOptEntity(opt)

    class DefinedClassModelInvalidLst extends NCTestModelAdapter:
        @NCIntent("intent=invalidList term(list)~{# == 'x'}[0,10]")
        def invalidList(@NCIntentTerm("list") list: List[Int]): NCResult = processListInt(list)

    class DefinedClassModelInvalidOpt extends NCTestModelAdapter:
        @NCIntent("intent=invalidOpt term(opt)~{# == 'x'}?")
        def invalidOpt(@NCIntentTerm("opt") opt: Option[Int]): NCResult = processOptInt(opt)

    private val CHECKED_MDL_VALID: NCModel = new DefinedClassModelValid
    private val CHECKED_MDL_INVALID_LST: NCModel = new DefinedClassModelInvalidLst
    private val CHECKED_MDL_INVALID_OPT: NCModel = new DefinedClassModelInvalidOpt
    private val UNCHECKED_MDL: NCModel =
        new NCTestModelAdapter:
            @NCIntent("intent=validList term(list)~{# == 'x'}[0,10]")
            def validList(@NCIntentTerm("list") list: List[NCEntity]): NCResult = processListEntity(list)

            @NCIntent("intent=validOpt term(opt)~{# == 'x'}?")
            def validOpt(@NCIntentTerm("opt") opt: Option[NCEntity]): NCResult = processOptEntity(opt)

            @NCIntent("intent=invalidList term(list)~{# == 'x'}[0,10]")
            def invalidList(@NCIntentTerm("list") list: List[Int]): NCResult = processListInt(list)

            @NCIntent("intent=invalidOpt term(opt)~{# == 'x'}?")
            def invalidOpt(@NCIntentTerm("opt") opt: Option[Int]): NCResult = processOptInt(opt)

    private val INTENT_MATCH =
        val e = NCTestEntity("id", "reqId", tokens = NCTestToken())

        def col[T](t: T): util.List[T] = java.util.Collections.singletonList(t)

        new NCIntentMatch:
            override def getIntentId: String = "intentId"
            override def getIntentEntities: util.List[util.List[NCEntity]] = col(col(e))
            override def getTermEntities(idx: Int): util.List[NCEntity] = col(e)
            override def getTermEntities(termId: String): util.List[NCEntity] = col(e)
            override def getVariant: NCVariant = new NCVariant:
                override def getEntities: util.List[NCEntity] = col(e)

    private def mkResult0(obj: Any): NCResult =
        println(s"Result body: $obj, class=${obj.getClass}")
        val res = new NCResult()
        res.setBody(obj)
        res

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
        val i = new NCModelScanner(mdl).scan().find(_.intent.id == intentId).get

        println(s"Test finished [modelClass=${mdl.getClass}, intent=$intentId, result=${i.function(INTENT_MATCH)}")

    private def testRuntimeClassCast(mdl: NCModel, intentId: String): Unit =
        val i = new NCModelScanner(mdl).scan().find(_.intent.id == intentId).get

        try
            i.function(INTENT_MATCH)

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
            new NCModelScanner(mdl).scan()

            require(false)
        catch
            case e: NCException =>
                println("Expected stack trace:")
                e.printStackTrace(System.out)

    @Test
    def test(): Unit =
        testOk(CHECKED_MDL_VALID, "validList")
        testOk(CHECKED_MDL_VALID, "validOpt")

        // Errors thrown on san phase if error found in any intent.
        testScanValidation(CHECKED_MDL_INVALID_LST)
        testScanValidation(CHECKED_MDL_INVALID_OPT)

        testOk(UNCHECKED_MDL, "validList")
        testOk(UNCHECKED_MDL, "validOpt")
        testRuntimeClassCast(UNCHECKED_MDL, "invalidList")
        testRuntimeClassCast(UNCHECKED_MDL, "invalidOpt")