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

package org.apache.nlpcraft.model.ctxword

import org.apache.nlpcraft.model.ctxword.NCContextWordSpecModel._
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.{AfterEach, BeforeEach, Test}

/**
  * Test model.
  */
class NCContextWordSpecModel2 extends NCContextWordSpecModel {
    override val level: Double = 0
}

/**
  * @see NCConversationSpecModel
  */
@NCTestEnvironment(model = classOf[NCContextWordSpecModel2], startClient = true)
class NCContextWordSpec2 extends NCTestContext {
    private def check(txts: String*): Unit =
        for (txt <- txts)
            checkIntent(txt, "classification")

    @BeforeEach
    private[ctxword] def before(): Unit = tables.clear()

    @AfterEach
    private[ctxword] def after(): Unit = {
        println("MATCHED:")

        for ((txt, table) <- tables) {
            println(s"Text: $txt")
            table.render()
        }

        tables.clear()
    }

    @Test
    private[ctxword] def test(): Unit = {
        check(
            "I want to have dogs and foxes",
//            "I bought dog's meat",
//            "I bought meat dog's",
//
//            "I want to have a dog and fox",
//            "I fed your fish",
//
//            "I like to drive my Porsche and Volkswagen",
//            "Peugeot added motorcycles to its range in 1901",
//
//            "The frost is possible today",
//            "There's a very strong wind from the east now"
        )
    }
}
