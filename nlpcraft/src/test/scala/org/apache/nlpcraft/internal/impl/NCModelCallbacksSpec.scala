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
import org.apache.nlpcraft.NCResultType.*
import org.apache.nlpcraft.nlp.entity.parser.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.*
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.*

import scala.jdk.CollectionConverters.*
import scala.util.Using

/**
  *
  */
class NCModelCallbacksSpec:
    enum State:
        case
            MatchFalse, VariantFalse,
            ContextNotNull, ResultNotNull, RejectionNotNull, ErrorNotNull,
            IntentError, IntentReject

    import State.*

    private val states = collection.mutable.HashSet.empty[State]

    private val RESULT_INTENT = new NCResult("result-intent", NCResultType.ASK_RESULT)
    private val RESULT_CONTEXT = new NCResult("result-context", NCResultType.ASK_RESULT)
    private val RESULT_RESULT = new NCResult("result-result", NCResultType.ASK_RESULT)
    private val RESULT_REJECTION = new NCResult("result-rejection", NCResultType.ASK_RESULT)
    private val RESULT_ERROR = new NCResult("result-error", NCResultType.ASK_RESULT)

    private val MDL: NCModel =
        new NCTestModelAdapter():
            @NCIntent("intent=x term(x)={# == 'x'}")
            def intent(im: NCIntentMatch, @NCIntentTerm("x") x: NCEntity): NCResult =
                if has(IntentError) then throw new RuntimeException("Error")
                else if has(IntentReject) then throw new NCRejection("Rejection")
                else RESULT_INTENT

            override def onMatchedIntent(ctx: NCIntentMatch): Boolean = getOrElse(MatchFalse, false, true)
            override def onVariant(vrn: NCVariant): Boolean = getOrElse(VariantFalse, false, true)
            override def onContext(ctx: NCContext): NCResult = getOrElse(ContextNotNull, RESULT_CONTEXT, null)
            override def onResult(ctx: NCIntentMatch, res: NCResult): NCResult = getOrElse(ResultNotNull, RESULT_RESULT, null)
            override def onRejection(ctx: NCIntentMatch, e: NCRejection): NCResult = getOrElse(RejectionNotNull, RESULT_REJECTION, null)
            override def onError(ctx: NCContext, e: Throwable): NCResult = getOrElse(ErrorNotNull, RESULT_ERROR, null)

    MDL.getPipeline.getEntityParsers.add(NCTestUtils.mkENSemanticParser(Seq(NCSemanticTestElement("x")).asJava))

    /**
      *
      * @param s
      * @return
      */
    private def has(s: State): Boolean = states.synchronized { states.contains(s) }

    /**
      *
      * @param s
      * @param v
      * @param dtlt
      * @tparam T
      * @return
      */
    private def getOrElse[T](s: State, v: T, dtlt: => T): T = if has(s) then v else dtlt

    /**
      *
      * @param states
      * @return
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
        require(client.ask("x", null, "userId").getBody == exp.getBody)

    /**
      *
      * @param client
      * @param states
      */
    private def testFail(client: NCModelClient, states: State*): Unit =
        set(states*)

        try
            client.ask("x", null, "userId")

            require(false)
        catch
            case e: Throwable => println(s"Expected error: ${e.getMessage}")

    /**
      *
      */
    @Test
    def test(): Unit = Using.resource(new NCModelClient(MDL)) { client =>
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
