/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.examples.pizzeria

import org.apache.nlpcraft.*
import org.apache.nlpcraft.NCResultType.*
import org.apache.nlpcraft.examples.pizzeria.State.*
import org.junit.jupiter.api.*

import scala.util.Using
import scala.collection.mutable
/**
  *
  */
class PizzeriaModelSpec:
    private class TestWrapper extends PizzeriaModel:
        private var o: PizzeriaOrder = _
        override def doExecute(im: NCIntentMatch, o: PizzeriaOrder): NCResult =
            val res = super.doExecute(im, o)
            this.o = o
            res
        def getLastExecutedOrder: PizzeriaOrder = o

    case class Builder(state: State):
        private val o = new PizzeriaOrder
        o.setState(state)
        def withPizza(name: String, size: String, qty: Int): Builder =
            o.add(Seq(Pizza(name, Some(size), Some(qty))), Seq.empty)
            this
        def withDrink(name: String, qty: Int): Builder =
            o.add(Seq.empty, Seq(Drink(name, Some(qty))))
            this
        def build: PizzeriaOrder = o

    private val mdl = new TestWrapper()
    private val client = new NCModelClient(mdl)

    private val msgs = mutable.ArrayBuffer.empty[mutable.ArrayBuffer[String]]
    private val errs = mutable.HashMap.empty[Int, Throwable]
    private var testNum: Int = 0

    @AfterEach def tearDown(): Unit =
        if client != null then client.close()

        for ((seq, num) <- msgs.zipWithIndex)
            println("#################################################################################################")
            for (line <- seq) println(line)
            errs.get(num) match
                case Some(err) => err.printStackTrace()
                case None => // No-op.
            println()

        require(errs.isEmpty)

    private def dialog(reqs: String*): Unit = dialog(None, reqs*)
    private def dialog(expResOrder: PizzeriaOrder, reqs: String*): Unit = dialog(Option(expResOrder), reqs*)
    private def dialog(expResOrderOpt: Option[PizzeriaOrder], reqs: String*): Unit =
        val testMsgs = mutable.ArrayBuffer.empty[String]
        msgs += testMsgs

        testMsgs += s"Test: $testNum"

        for ((txt, idx) <- reqs.zipWithIndex)
            try
                val resp = client.ask(txt, null, "userId")

                testMsgs += s">> Request: $txt"
                testMsgs += s">> Response: '${resp.getType}': ${resp.getBody}"

                val expType = if idx == reqs.size - 1 then ASK_RESULT else ASK_DIALOG

                if expType != resp.getType then
                    errs += testNum -> new Exception(s"Unexpected result for test:$testNum [expected:\n$expType, type=${resp.getType}]")

                if idx == reqs.size - 1 then
                    expResOrderOpt match
                        case Some(expResOrder) =>
                            val lastOrder = mdl.getLastExecutedOrder
                            def s(o: PizzeriaOrder) = if o == null then null else s"Order [state=${o.getState}, desc=${o.getDescription}]"
                            val s1 = s(expResOrder)
                            val s2 = s(lastOrder)
                            if s1 != s2 then
                                errs += testNum -> new Exception(s"Unexpected result for test (excepted/real):$testNum\n$s1\n$s2")
                        case None => // No-op.
            catch
                case e: Exception => errs += testNum -> new Exception(s"Error during test [num=$testNum]", e)

        testNum += 1

    private def mkOrder(state: State, ps: Seq[Pizza], ds: Seq[Drink]): PizzeriaOrder =
        val o = new PizzeriaOrder
        o.setState(state)
        o.add(ps, ds)
        o

    @Test
    def test(): Unit =
        dialog(
            "One tea",
            "yes",
            "yes"
        )

        dialog(
            "I want to order carbonara, marinara and tea",
            "large size please",
            "smallest",
            "yes",
            "correct"
        )

        dialog(
            "carbonara two small",
            "yes",
            "yes"
        )

        dialog(
            "carbonara",
            "small",
            "yes",
            "yes"
        )

        dialog(
            "marinara",
            "stop"
        )
        dialog(
            Builder(NO_DIALOG).
                withPizza("margherita", "small", 2).
                withPizza("marinara", "small", 3).
                withDrink("tea", 1).
                build,
            "margherita two, marinara and 3 tea",
            "small",
            "small",
            "yes",
            "yes"
        )