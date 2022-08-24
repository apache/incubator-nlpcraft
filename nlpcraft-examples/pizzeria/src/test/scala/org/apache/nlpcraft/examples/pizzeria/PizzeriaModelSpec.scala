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
import org.apache.nlpcraft.examples.pizzeria.PizzeriaModel.Result
import org.apache.nlpcraft.examples.pizzeria.PizzeriaOrderState.*
import org.junit.jupiter.api.*

import scala.language.implicitConversions
import scala.util.Using
import scala.collection.mutable

object PizzeriaModelSpec:
    type Request = (String, NCResultType)
    private class ModelTestWrapper extends PizzeriaModel:
        private var o: PizzeriaOrder = _

        override def doExecute(o: PizzeriaOrder)(using ctx: NCContext, im: NCIntentMatch): Result =
            val res = super.doExecute(o)
            this.o = o
            res

        def getLastExecutedOrder: PizzeriaOrder = o
        def clearLastExecutedOrder(): Unit = o = null

    private class Builder:
        private val o = new PizzeriaOrder
        o.setState(DIALOG_EMPTY)
        def withPizza(name: String, size: String, qty: Int): Builder =
            o.add(Seq(Pizza(name, Some(size), Some(qty))), Seq.empty)
            this
        def withDrink(name: String, qty: Int): Builder =
            o.add(Seq.empty, Seq(Drink(name, Some(qty))))
            this
        def build: PizzeriaOrder = o

import PizzeriaModelSpec.*
/**
  *
  */
class PizzeriaModelSpec:
    private val mdl = new ModelTestWrapper()
    private val client = new NCModelClient(mdl)
    private val msgs = mutable.ArrayBuffer.empty[mutable.ArrayBuffer[String]]
    private val errs = mutable.HashMap.empty[Int, Throwable]

    private var testNum: Int = 0

    @AfterEach def tearDown(): Unit =
        if client != null then client.close()

        for ((seq, num) <- msgs.zipWithIndex)
            println("#" * 150)
            for (line <- seq) println(line)
            errs.get(num) match
                case Some(err) => err.printStackTrace()
                case None => // No-op.

        require(errs.isEmpty, s"There are ${errs.size} errors above.")

    private def dialog(exp: PizzeriaOrder, reqs: Request*): Unit =
        val testMsgs = mutable.ArrayBuffer.empty[String]
        msgs += testMsgs

        testMsgs += s"Test: $testNum"

        for (((txt, expType), idx) <- reqs.zipWithIndex)
            try
                mdl.clearLastExecutedOrder()
                val resp = client.ask(txt, "userId")

                testMsgs += s">> Request: $txt"
                testMsgs += s">> Response: '${resp.getType}': ${resp.getBody}"

                if expType != resp.getType then
                    errs += testNum -> new Exception(s"Unexpected result type [num=$testNum, txt=$txt, expected=$expType, type=${resp.getType}]")

                // Check execution result on last request.
                if idx == reqs.size - 1 then
                    val lastOrder = mdl.getLastExecutedOrder
                    def s(o: PizzeriaOrder) = if o == null then null else s"Order [state=${o.getState}, desc=$o]"
                    val s1 = s(exp)
                    val s2 = s(lastOrder)
                    if s1 != s2 then
                        errs += testNum ->
                            new Exception(
                                s"Unexpected result [num=$testNum, txt=$txt]" +
                                s"\nExpected: $s1" +
                                s"\nReal    : $s2"
                            )
            catch
                case e: Exception => errs += testNum -> new Exception(s"Error during test [num=$testNum]", e)

        testNum += 1

    @Test
    def test(): Unit =
        given Conversion[String, Request] with
            def apply(txt: String): Request = (txt, ASK_DIALOG)

        dialog(
            new Builder().withDrink("tea", 2).build,
            "Two tea",
            "yes",
            "yes" -> ASK_RESULT
        )

        dialog(
            new Builder().
                withPizza("carbonara", "large", 1).
                withPizza("marinara", "small", 1).
                withDrink("tea", 1).
                build,
            "I want to order carbonara, marinara and tea",
            "large size please",
            "smallest",
            "yes",
            "correct" -> ASK_RESULT
        )

        dialog(
            new Builder().withPizza("carbonara", "small", 2).build,
            "carbonara two small",
            "yes",
            "yes" -> ASK_RESULT
        )

        dialog(
            new Builder().withPizza("carbonara", "small", 1).build,
            "carbonara",
            "small",
            "yes",
            "yes" -> ASK_RESULT
        )

        dialog(
            null,
            "marinara",
            "stop" -> ASK_RESULT
        )

        dialog(
            new Builder().
                withPizza("carbonara", "small", 2).
                withPizza("marinara", "large", 4).
                withDrink("cola", 3).
                withDrink("tea", 1).
                build,
            "3 cola",
            "one tea",
            "carbonara 2",
            "small",
            "4 marinara big size",
            "menu" -> ASK_RESULT,
            "done",
            "yes" -> ASK_RESULT
        )

        dialog(
            new Builder().
                withPizza("margherita", "small", 2).
                withPizza("marinara", "small", 1).
                withDrink("tea", 3).
                build,
            "margherita two, marinara and three tea",
            "small",
            "small",
            "yes",
            "yes" -> ASK_RESULT
        )

        dialog(
            new Builder().
                withPizza("margherita", "small", 2).
                withPizza("marinara", "large", 1).
                withDrink("cola", 3).
                build,
            "small margherita two, marinara big one and three cola",
            "yes",
            "yes" -> ASK_RESULT
        )

        dialog(
            new Builder().
                withPizza("margherita", "small", 1).
                withPizza("marinara", "large", 2).
                withDrink("coffee", 2).
                build,
            "small margherita, 2 marinara and 2 coffee",
            "large",
            "yes",
            "yes" -> ASK_RESULT
        )