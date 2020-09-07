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

package org.apache.nlpcraft.server.rest

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class NCRestFeedbackSpec extends NCRestSpec {
    /**
      *
      */
    private def addFeedback(): Long = {
        var fId: Long = 0

        post("feedback/add", "srvReqId" → rnd(), "score" → 0.5)(("$.id", (id: Number) ⇒ fId = id.longValue()))

        assertTrue(fId != 0)

        fId
    }

    @Test
    def test(): Unit = {
        // Gets.
        post("feedback/all")()

        // Adds.
        val id = addFeedback()

        // Gets and checks.
        post("feedback/all")(("$.feedback", (fs: DataMap) ⇒ assertTrue(containsLong(fs, "id", id))))

        // Deletes by id.
        post("feedback/delete", "id" → id)()

        // Adds few.
        addFeedback()
        addFeedback()

        // Deletes all.
        post("feedback/delete")()

        // Gets and checks.
        post("feedback/all")(("$.feedback", (feedbacks: DataMap) ⇒ assertTrue(feedbacks.isEmpty)))
    }
}
