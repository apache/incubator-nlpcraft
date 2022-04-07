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

package org.apache.nlpcraft.examples.order

import org.apache.nlpcraft.*
import org.apache.nlpcraft.NCResultType.*
import org.junit.jupiter.api.{AfterEach, BeforeEach, Test}

import scala.util.Using
import scala.collection.mutable
/**
  *
  */
class PizzeriaModelSpec:
    private val msgs = mutable.ArrayBuffer.empty[mutable.ArrayBuffer[String]]
    private val errs = mutable.HashMap.empty[Int, Throwable]

    private var client: NCModelClient = _
    private var testNum: Int = 0

    @BeforeEach def setUp(): Unit = client = new NCModelClient(new PizzeriaModel)
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

    private def dialog(reqs: String*): Unit =
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
                    errs += testNum -> new Exception(s"Error during test [num=$testNum, expRespType=$expType, type=${resp.getType}]")
            catch
                case e: Exception => errs += testNum -> new Exception(s"Error during test [num=$testNum]", e)

        testNum += 1

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
            "carbonara",
            "stop"
        )

        dialog(
            "carbonara two, marinara and 2 tea",
            "small",
            "small",
            "yes",
            "yes"
        )