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

import org.apache.nlpcraft.model.{NCContext, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

/**
  * Test model.
  */
class NCContextWordSpecModel2 extends NCContextWordSpecModel {
    override val MDL_LEVEL = 0
    override def onContext(ctx: NCContext): NCResult = NCResult.text("OK")
}

/**
  * Run this test just for manual review all found categories for given model.
  * Note that initial confidence set as zero.
  */
@NCTestEnvironment(model = classOf[NCContextWordSpecModel2], startClient = true)
class NCContextWordSpec2 extends NCTestContext {
    @Test
    private[ctxword] def test(): Unit =
        Seq(
            "I want to have dogs and foxes",
            "I bought dog's meat",
            "I bought meat dog's",

            "I want to have a dog and fox",
            "I fed your fish",

            "I like to drive my Porsche and Volkswagen",
            "Peugeot added motorcycles to its range year ago",

            "The frost is possible today",
            "There's a very strong wind from the east now"
        ).foreach(getClient.ask)
}
