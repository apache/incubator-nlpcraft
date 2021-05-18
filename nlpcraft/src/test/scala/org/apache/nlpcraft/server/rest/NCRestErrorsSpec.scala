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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

import scala.collection.JavaConverters._

class NCRestErrorsSpec extends NCRestSpec {
    @Test
    def testApiSignin(): Unit = {
        // Invalid value.
        postError("signin", 401, "NC_SIGNIN_FAILURE", "email" -> "email", "passwd" -> "passwd")

        // Invalid values.
        postError(
            "signin", 400, "NC_INVALID_FIELD", "email" -> mkString(100), "passwd" -> "passwd"
        )

        // Missed field.
        postError("signin", 400, "NC_ERROR")
    }

    @Test
    def testApiSignout(): Unit = {
        // Authorization error.
        postError("signout", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" -> "UNEXPECTED")
    }

    @Test
    def testApiCancel(): Unit = {
        // Authorization error.
        postError("cancel", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" -> "UNEXPECTED")

        // Invalid values.
        postError("cancel", 400, "NC_INVALID_FIELD", "usrId" -> -1)
    }

    @Test
    def testApiCheck(): Unit = {
        // Authorization error.
        postError("check", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" -> "UNEXPECTED")

        // Invalid values.
        postError("check", 400, "NC_INVALID_FIELD", "usrId" -> -1)
        postError("check", 400, "NC_INVALID_FIELD", "maxRows" -> -1)
    }

    @Test
    def testApiClear(): Unit = {
        // Authorization error.
        postError(
            "clear/conversation",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" -> "UNEXPECTED",
            "mdlId" -> "rest.test.model"
        )
        postError("clear/dialog",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" -> "UNEXPECTED",
            "mdlId" -> "rest.test.model"
        )

        // Invalid values.
        postError("clear/conversation",
            400, "NC_INVALID_FIELD", "usrId" -> -1, "mdlId" -> "rest.test.model"
        )
        postError(
            "clear/dialog",
            400, "NC_INVALID_FIELD",
            "usrId" -> -1,
            "mdlId" -> "rest.test.model"
        )

        postError("clear/conversation", 400, "NC_INVALID_FIELD", "mdlId" -> "UNEXPECTED")
        postError("clear/dialog", 400, "NC_INVALID_FIELD", "mdlId" -> "UNEXPECTED")
    }

    @Test
    def testApiCompany(): Unit = {
        // Authorization error.
        postError(
            "company/add",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" -> "UNEXPECTED",
            "name" -> "name",
            "website" -> "website",
            "country" -> "country",
            "region" -> "region",
            "city" -> "city",
            "address" -> "address",
            "postalCode" -> "postalCode",
            "adminEmail" -> "adminEmail",
            "adminPasswd" -> "adminPasswd",
            "adminFirstName" -> "firstName",
            "adminLastName" -> "lastName",
            "adminAvatarUrl" -> "avatarUrl"
        )
        postError(
            "company/update",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" -> "UNEXPECTED",
            "name" -> "name",
            "website" -> "website",
            "country" -> "country",
            "region" -> "region",
            "city" -> "city",
            "address" -> "address",
            "postalCode" -> "postalCode"
        )
        postError("company/get", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" -> "UNEXPECTED")
        postError(
            "company/token/reset", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" -> "UNEXPECTED"
        )
        postError("company/delete", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" -> "UNEXPECTED")

        // Invalid values.
        postError(
            "company/add",
            400,
            "NC_INVALID_FIELD",
            "name" -> mkString(100),
            "website" -> "website",
            "country" -> "country",
            "region" -> "region",
            "city" -> "city",
            "address" -> "address",
            "postalCode" -> "postalCode",
            "adminEmail" -> "adminEmail",
            "adminPasswd" -> "adminPasswd",
            "adminFirstName" -> "firstName",
            "adminLastName" -> "lastName",
            "adminAvatarUrl" -> "avatarUrl"
        )
        postError(
            "company/update",
            400,
            "NC_INVALID_FIELD",
            "name" -> mkString(100),
            "website" -> "website",
            "country" -> "country",
            "region" -> "region",
            "city" -> "city",
            "address" -> "address",
            "postalCode" -> "postalCode"
        )

        // Missed fields.
        postError("company/add", 400, "NC_ERROR")
        postError("company/update", 400, "NC_ERROR")
    }

    @Test
    def testApiUser(): Unit = {
        // Authorization error.
        postError("user/get", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" -> "UNEXPECTED")
        postError("user/add",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" -> "UNEXPECTED",
            "email" -> s"test@test.com",
            "passwd" -> "test",
            "firstName" -> "firstName",
            "lastName" -> "lastName",
            "isAdmin" -> false
        )
        postError("user/update",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" -> "UNEXPECTED",
            "firstName" -> "firstName",
            "lastName" -> "lastName",
            "avatarUrl" -> "avatarUrl"
        )
        postError(
            "user/delete", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" -> "UNEXPECTED"
        )
        postError(
            "user/admin",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" -> "UNEXPECTED",
            "id" -> 1,
            "admin" -> false
        )
        postError(
            "user/passwd/reset",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" -> "UNEXPECTED",
            "id" -> 1,
            "newPasswd" -> "test1"
        )

        // Invalid values.
        postError("user/add",
            400,
            "NC_INVALID_FIELD",
            "email" -> s"test@test.com",
            "passwd" -> mkString(100),
            "firstName" -> "firstName",
            "lastName" -> "lastName",
            "isAdmin" -> false
        )
        postError("user/update",
            400,
            "NC_INVALID_FIELD",
            "firstName" -> mkString(100),
            "lastName" -> "lastName",
            "avatarUrl" -> "avatarUrl"
        )

        // Missed fields.
        postError("user/add", 400, "NC_ERROR")
        postError("user/update", 400, "NC_ERROR")
    }

    @Test
    def testApiFeedback(): Unit = {
        // Authorization error.
        postError("feedback/add",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" -> "UNEXPECTED",
            "srvReqId" -> U.genGuid(),
            "score" -> 0.5
        )
        postError("feedback/all", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" -> "UNEXPECTED")
        postError("feedback/delete", 401, "NC_INVALID_ACCESS_TOKEN", "acsTok" -> "UNEXPECTED")

        // Invalid values.
        postError("feedback/add",
            400,
            "NC_INVALID_FIELD",
            "srvReqId" -> "",
            "score" -> 0.5
        )
        postError("feedback/add",
            400,
            "NC_INVALID_FIELD",
            "srvReqId" -> U.genGuid(),
            "score" -> 10000
        )
        postError("feedback/all",
            400,
            "NC_INVALID_FIELD",
            "usrExtId" -> mkString(100)
        )

        // Missed fields.
        postError("feedback/add", 400, "NC_ERROR")
    }

    @Test
    def testApiProbe(): Unit = {
        // Authorization error.
        postError("probe/all",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" -> "UNEXPECTED"
        )
    }

    @Test
    def testApiAsk(): Unit = {
        // Authorization error.
        postError("ask",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" -> "UNEXPECTED",
            "txt" -> "What's the local time?",
            "mdlId" -> "rest.test.model"
        )
        postError("ask/sync",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" -> "UNEXPECTED",
            "txt" -> "What's the local time?",
            "mdlId" -> "rest.test.model"
        )

        // Invalid values.
        postError("ask",
            400,
            "NC_INVALID_FIELD",
            "txt" -> "What's the local time?",
            "mdlId" -> "rest.test.model",
            "usrId" -> -1
        )
        postError("ask/sync",
            400,
            "NC_INVALID_FIELD",
            "txt" -> "What's the local time?",
            "mdlId" -> "rest.test.model",
            "usrId" -> -1
        )
        postError("ask",
            400,
            "NC_INVALID_FIELD",
            "txt" -> mkString(1025),
            "mdlId" -> "rest.test.model"
        )
        postError("ask/sync",
            400,
            "NC_INVALID_FIELD",
            "txt" -> mkString(1025),
            "mdlId" -> "rest.test.model"
        )
        postError("ask",
            400,
            "NC_INVALID_FIELD",
            "txt" -> "What's the local time?",
            "mdlId" -> "UNEXPECTED"
        )
        postError("ask/sync",
            400,
            "NC_INVALID_FIELD",
            "txt" -> "What's the local time?",
            "mdlId" -> "UNEXPECTED"
        )

        // Missed fields.
        postError("ask",
            400,
            "NC_ERROR",
            "mdlId" -> "rest.test.model"
        )
        postError("ask/sync",
            400,
            "NC_ERROR",
            "mdlId" -> "rest.test.model"
        )
    }

    @Test
    def testApiModel(): Unit = {
        // Authorization error.
        postError("model/sugsyn",
            401,
            "NC_INVALID_ACCESS_TOKEN",
            "acsTok" -> "UNEXPECTED",
            "mdlId" -> "rest.test.model"
        )

        // Invalid values.
        postError("model/sugsyn",
            400,
            "NC_INVALID_FIELD",
            "mdlId" -> "UKNKNOWN"
        )
        postError("model/sugsyn",
            400,
            "NC_INVALID_FIELD",
            "mdlId" -> "rest.test.model",
            "minScore" -> 1000
        )
    }

    @Test
    def testErrorAdminAccess(): Unit = {
        // Adds `feedback` under admin.
        post("feedback/add", "srvReqId" -> U.genGuid(), "score" -> 0.5)()

        var notAdminId: Long = 0

        val email = s"${rnd()}@test.com"
        val pswd = "test"

        post(
            "user/add",
            "email" -> email,
            "passwd" -> pswd,
            "firstName" -> "firstName",
            "lastName" -> "lastName",
            "isAdmin" -> false,
        )(
            ("$.id", (id: Number) => notAdminId = id.longValue())
        )

        assertTrue(notAdminId > 0)

        try {
            val tkn = signin(email, pswd)

            // Tries to read all company's feedbacks, but can't because admin's feedbacks found.
            postError("feedback/all", 403, "NC_ADMIN_REQUIRED", "acsTok" -> tkn)
            postError("feedback/delete", 403, "NC_ADMIN_REQUIRED", "acsTok" -> tkn)
        }
        finally {
            post("user/delete", "id" -> notAdminId)()
            post("feedback/delete")()
        }
    }

    @Test
    def testErrorInvalidOperations(): Unit = {
        var curId: Long = 0

        post("user/get")(("$.id", (id: Number) => curId = id.longValue()))

        assertTrue(curId > 0)

        // Deletes all users except current.
        post("user/all")(("$.users", (users: ResponseList) => {
            users.asScala.foreach(p => {
                val id = p.asScala("id").asInstanceOf[Number].longValue()

                if (id != curId)
                    post("user/delete", "id" -> id)()
            })
        }))

        // Tries to reset admin privileges of single system user.
        postError("user/admin", 403, "NC_INVALID_OPERATION", "admin" -> false)
    }
}