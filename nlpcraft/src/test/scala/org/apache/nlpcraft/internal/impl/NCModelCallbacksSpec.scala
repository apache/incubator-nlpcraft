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

package org.apache.nlpcraft.internal.impl

import org.apache.nlpcraft.*
import NCResultType.*
import annotations.*
import nlp.parsers.*
import nlp.util.*
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Using

/**
  *
  */
class NCModelCallbacksSpec extends AnyFunSuite:
    enum State:
        case
            MatchFalse, VariantFalse,
            ContextNotNull, ResultNotNull, RejectionNotNull, ErrorNotNull,
            IntentError, IntentReject

    import State.*

    private val states = collection.mutable.HashSet.empty[State]

    private val RESULT_INTENT = NCResult("result-intent")
    private val RESULT_CONTEXT = NCResult("result-context")
    private val RESULT_RESULT = NCResult("result-result")
    private val RESULT_REJECTION = NCResult("result-rejection")
    private val RESULT_ERROR = NCResult("result-error")

    private val MDL: NCTestModelAdapter =
        new NCTestModelAdapter():
            @NCIntent("intent=x term(x)={# == 'x'}")
            def intent(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("x") x: NCEntity): NCResult =
                if has(IntentError) then throw new RuntimeException("Expected test error")
                else if has(IntentReject) then throw new NCRejection("Expected test rejection")
                else RESULT_INTENT

            override def onMatchedIntent(ctx: NCContext, im: NCIntentMatch): Boolean = getOrElse(MatchFalse, false, true)
            override def onContext(ctx: NCContext): Option[NCResult] = getOrElse(ContextNotNull, Option(RESULT_CONTEXT), None)
            override def onResult(ctx: NCContext, im: NCIntentMatch, res: NCResult): Option[NCResult] = getOrElse(ResultNotNull, Option(RESULT_RESULT), None)
            override def onRejection(ctx: NCContext, im: Option[NCIntentMatch], e: NCRejection): Option[NCResult] = getOrElse(RejectionNotNull, Option(RESULT_REJECTION), None)
            override def onError(ctx: NCContext, e: Throwable): Option[NCResult] = getOrElse(ErrorNotNull, Option(RESULT_ERROR), None)

    MDL.pipeline.entParsers += NCTestUtils.mkEnSemanticParser(List(NCSemanticTestElement("x")))

    /**
      *
      * @param s
      */
    private def has(s: State): Boolean = states.synchronized { states.contains(s) }

    /**
      *
      * @param s
      * @param v
      * @param dtlt
      * @tparam T
      */
    private def getOrElse[T](s: State, v: T, dtlt: => T): T = if has(s) then v else dtlt

    /**
      *
      * @param states
      */
    private def set(states: State*) = this.states.synchronized {
        this.states.clear()
        this.states ++= states
    }

    /**
      *
      * @param client
      * @param exp
      * @param states
      */
    private def testOk(client: NCModelClient, exp: NCResult, states: State*): Unit =
        set(states*)
        require(client.ask("x", "userId").getBody == exp.getBody)

    /**
      *
      * @param client
      * @param states
      */
    private def testFail(client: NCModelClient, states: State*): Unit =
        set(states*)

        try
            client.ask("x", "userId")

            require(false)
        catch
            case e: Throwable => println(s"Expected error: ${e.getMessage}")

    /**
      *
      */
    test("test") {
        Using.resource(new NCModelClient(MDL)) { client =>
            testOk(client, RESULT_INTENT)
            testOk(client, RESULT_RESULT, ResultNotNull)
            testOk(client, RESULT_CONTEXT, ContextNotNull)

            testFail(client, IntentReject)
            testFail(client, IntentError)
            testFail(client, VariantFalse)

            testOk(client, RESULT_REJECTION, IntentReject, RejectionNotNull)
            testOk(client, RESULT_ERROR, IntentError, ErrorNotNull)

            // To avoid endless loop.
            val threadReset =
                new Thread("reset-thread"):
                    override def run(): Unit =
                        Thread.sleep(500)
                        states.clear()

            threadReset.start()

            testOk(client, RESULT_INTENT, MatchFalse)
        }
    }