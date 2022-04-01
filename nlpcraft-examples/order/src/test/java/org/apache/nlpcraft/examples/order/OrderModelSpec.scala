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
import org.junit.jupiter.api.Test

import scala.util.Using
import scala.collection.mutable
/**
  *
  */
class OrderModelSpec:
    @Test
    def test(): Unit =
        val buf = mutable.ArrayBuffer.empty[String]

        def printDialog(): Unit = for (line <- buf) println(line)

        Using.resource(new NCModelClient(new OrderModel)) { client =>
            def ask(txt: String, expType: NCResultType): Unit =
                val resp = client.ask(txt, null, "userId")

                buf += s">> $txt"
                buf += s">> ${resp.getBody} (${resp.getType})"
                buf += ""

                if expType != resp.getType then
                    printDialog()
                    require(false, s"Unexpected type: ${resp.getType}, expected: ${expType}.")

            ask("I want to order margherita medium size, marbonara, marinara and tea", ASK_DIALOG)
            ask("large size please", ASK_DIALOG)
            ask("smallest", ASK_DIALOG)
            ask("you are right", ASK_RESULT)

            printDialog()
        }