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

package org.apache.nlpcraft.examples.time

import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.apache.nlpcraft.common.NCException
import org.junit.jupiter.api.Test

import java.io.IOException

@NCTestEnvironment(model = classOf[TimeModel], startClient = true)
class NCTimeModelSpec extends NCTestContext {
    @Test
    @throws[NCException]
    @throws[IOException]
    private[time] def testIntentsPriorities(): Unit = {
        // intent1 must be winner for `What's the local time?` question, because exact matching.
        // Accumulated history (geo:city information) must be ignored.

        // 1. Without conversation.
        checkIntent("Show me time of the day in London.", "intent2")
        getClient.clearConversation()
        checkIntent("What's the local time?", "intent1")

        // 2. The same with conversation.
        checkIntent("Show me time of the day in London.", "intent2")
        checkIntent("What's the local time?", "intent1")
    }
}
