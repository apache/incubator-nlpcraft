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
import org.junit.jupiter.api.Test

class NCRestErrorsSpec extends NCRestSpec {
    @Test
    def testSignin(): Unit = {
        // Missed field.
        postError("signin", 400, "NC_ERROR")

        // Too long field.
        postError("signin", 400, "NC_INVALID_FIELD", "email" → mkString(100), "passwd" → "x")

        // Invalid value.
        postError("signin", 401, "NC_SIGNIN_FAILURE", "email" → "x", "passwd" → "x")
    }

    @Test
    def testSignout(): Unit = {
        // Invalid values.
        postError("signout", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" → "UNEXPECTED")
    }

    @Test
    def testCancel(): Unit = {
        // Authorization error.
        postError("cancel", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" → "UNEXPECTED")

        // Invalid values.
        postError("cancel", 400, "NC_INVALID_FIELD", "usrId" → -1)
    }

    @Test
    def testCheck(): Unit = {
        // Authorization error.
        postError("check", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" → "UNEXPECTED")

        // Invalid values.
        postError("check", 400, "NC_INVALID_FIELD", "usrId" → -1)
        postError("check", 400, "NC_INVALID_FIELD", "maxRows" → -1)
    }

    @Test
    def testClear(): Unit = {
        // Authorization error.
        postError("clear/conversation", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" → "UNEXPECTED", "mdlId" → "nlpcraft.time.ex")
        postError("clear/dialog", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" → "UNEXPECTED", "mdlId" → "nlpcraft.time.ex")

        // Invalid values.
        postError("clear/conversation", 400, "NC_INVALID_FIELD", "usrId" → -1, "mdlId" → "nlpcraft.time.ex")
        postError("clear/dialog", 400, "NC_INVALID_FIELD", "usrId" → -1, "mdlId" → "nlpcraft.time.ex")

        postError("clear/conversation", 400, "NC_INVALID_FIELD",  "mdlId" → "UNKNOWN")
        postError("clear/dialog", 400, "NC_INVALID_FIELD", "mdlId" → "UNKNOWN")

    }

    @Test
    def testCompany(): Unit = {
        // Authorization error.
        postError(
            "company/add",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" → "UNEXPECTED",
            "name" → "name",
            "website" → "website",
            "country" → "country",
            "region" → "region",
            "city" → "city",
            "address" → "address",
            "postalCode" → "postalCode",
            "adminEmail" → "adminEmail",
            "adminPasswd" → "adminPasswd",
            "adminFirstName" → "firstName",
            "adminLastName" → "lastName",
            "adminAvatarUrl" → "avatarUrl"
        )

        // Invalid values.
        postError(
            "company/add",
            400,
            "NC_INVALID_FIELD",
            "name" → mkString(100),
            "website" → "website",
            "country" → "country",
            "region" → "region",
            "city" → "city",
            "address" → "address",
            "postalCode" → "postalCode",
            "adminEmail" → "adminEmail",
            "adminPasswd" → "adminPasswd",
            "adminFirstName" → "firstName",
            "adminLastName" → "lastName",
            "adminAvatarUrl" → "avatarUrl"
        )

        // TODO: add
    }

    @Test
    def testUser(): Unit = {
        // Authorization error.
        postError("user/get", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" → "UNEXPECTED")
        postError("user/update",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" → "UNEXPECTED",
            "firstName" → "firstName",
            "lastName" → "lastName",
            "avatarUrl" → "avatarUrl"
        )

        // Invalid values.
        postError("user/update",
            400,
            "NC_INVALID_FIELD",
            "firstName" → mkString(100),
            "lastName" → "lastName",
            "avatarUrl" → "avatarUrl"
        )

        // TODO: extend.
    }

    @Test
    def testFeedback(): Unit = {
        // Authorization error.
        postError("feedback/add",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" → "UNEXPECTED",
            "srvReqId" → U.genGuid(),
            "score" → 0.5
        )

        // Invalid values.
        postError("feedback/add",
            400,
            "NC_INVALID_FIELD",
            "srvReqId" → "",
            "score" → 0.5
        )
        postError("feedback/add",
            400,
            "NC_INVALID_FIELD",
            "srvReqId" → U.genGuid(),
            "score" → 10000
        )
    }

    @Test
    def testProbe(): Unit = {
        // Authorization error.
        postError("probe/all",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" → "UNEXPECTED"
        )
    }

    @Test
    def testAsk(): Unit = {
        // Authorization error.
        postError("ask",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" → "UNEXPECTED",
            "txt" → "What's the local time?",
            "mdlId" → "nlpcraft.time.ex"
        )
        postError("ask/sync",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" → "UNEXPECTED",
            "txt" → "What's the local time?",
            "mdlId" → "nlpcraft.time.ex"
        )

        // Invalid values.
        postError("ask",
            400,
            "NC_INVALID_FIELD",
            "txt" → "What's the local time?",
            "mdlId" → "nlpcraft.time.ex",
            "usrId" → -1
        )
        postError("ask/sync",
            400,
            "NC_INVALID_FIELD",
            "txt" → "What's the local time?",
            "mdlId" → "nlpcraft.time.ex",
            "usrId" → -1
        )
        postError("ask",
            400,
            "NC_INVALID_FIELD",
            "txt" → mkString(1025),
            "mdlId" → "nlpcraft.time.ex"
        )
        postError("ask/sync",
            400,
            "NC_INVALID_FIELD",
            "txt" → mkString(1025),
            "mdlId" → "nlpcraft.time.ex"
        )
        postError("ask",
            400,
            "NC_INVALID_FIELD",
            "txt" → "What's the local time?",
            "mdlId" → "UNKNOWN"
        )
        postError("ask/sync",
            400,
            "NC_INVALID_FIELD",
            "txt" → "What's the local time?",
            "mdlId" → "UNKNOWN"
        )

        // Missed fields.
        // TODO: different processing branches, as result - different errors handling.
        // TODO: we can emulate akka rejections  - example: ("msg": "Bad request: MalformedRequestContentRejection(Object is missing required member \u0027txt\u0027,spray.json.DeserializationException: Object is missing required member \u0027txt\u0027)")
        // It is actual for 'ask/sync' and 'model/sugn'
        postError("ask",
            400,
            "NC_ERROR",
            "mdlId" → "nlpcraft.time.ex"
        )
        postError("ask/sync",
            400,
            "NC_INVALID_FIELD",
            "mdlId" → "nlpcraft.time.ex"
        )
    }

    @Test
    def testModel(): Unit = {
        // Authorization error.
        postError("model/sugsyn",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" → "UNEXPECTED",
            "mdlId" → "nlpcraft.time.ex"
        )

        // Invalid values.
        postError("model/sugsyn",
            400,
            "NC_INVALID_FIELD",
            "mdlId" → "UKNKNOWN"
        )
        postError("model/sugsyn",
            400,
            "NC_INVALID_FIELD",
            "mdlId" → "nlpcraft.time.ex",
            "minScore" → 1000
        )
    }
}