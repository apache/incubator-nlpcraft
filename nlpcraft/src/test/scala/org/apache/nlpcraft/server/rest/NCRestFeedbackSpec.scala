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

import org.apache.nlpcraft.common.U
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{BeforeEach, Test}

class NCRestFeedbackSpec extends NCRestSpec {
    private var usrId: Long = 0

    @BeforeEach
    def setUp(): Unit = {
        post("user/get")(("$.id", (id: Number) ⇒ usrId = id.longValue()))

        assertTrue(usrId > 0)
    }

    @Test
    def test(): Unit = {
        // Gets current state.
        post("feedback/all")()

        // Adds.
        val id = addFeedback()

        // Gets and checks.
        post("feedback/all")(("$.feedback", (fs: ResponseList) ⇒ assertTrue(containsLong(fs, "id", id))))

        // Deletes by id.
        post("feedback/delete", "id" → id)()

        // Checks deleted.
        post("feedback/all")(("$.feedback", (fs: ResponseList) ⇒ assertFalse(containsLong(fs, "id", id))))

        // Deletes all
        post("feedback/delete")()

        // Checks all deleted.
        post("feedback/all")(("$.feedback", (feedbacks: ResponseList) ⇒ assertTrue(feedbacks.isEmpty)))

        // Adds few.
        addFeedback(usrId = Some(usrId))
        addFeedback(comment = Some("comment"))
        addFeedback(usrId = Some(usrId), comment = Some("comment"))

        // Checks
        post("feedback/all")(("$.feedback", (fs: ResponseList) ⇒ {
            assertEquals(3, fs.size())

            // 3 - because default userId used if parameter `usrId` is skipped in feedback.
            assertEquals(3, count(fs, "usrId", (o: Object) ⇒ o.asInstanceOf[Number].longValue(), usrId))
            assertEquals(2, count(fs, "comment", (o: Object) ⇒ o.asInstanceOf[String], "comment"))
        }))

        // Deletes all.
        post("feedback/delete")()

        // Checks all deleted.
        post("feedback/all")(("$.feedback", (feedbacks: ResponseList) ⇒ assertTrue(feedbacks.isEmpty)))
    }

    /**
      *
      * @param usrId
      * @param comment
      */
    private def addFeedback(usrId: Option[java.lang.Long] = None, comment: Option[String] = None): Long = {
        var fId: Long = 0

        post("feedback/add",
            "srvReqId" → U.genGuid(),
            "score" → 0.5,
            "usrId" → usrId.orNull,
            "comment" → comment.orNull
        )(
            ("$.id", (id: Number) ⇒ fId = id.longValue())
        )

        assertTrue(fId != 0)

        fId
    }

    // @Test
    def testErrors(): Unit = {
        // Too big score.
        postError("feedback/add", 400, "aa", "srvReqId" → rnd(), "score" → 100)
    }
}
