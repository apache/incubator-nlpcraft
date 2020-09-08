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

import scala.collection.JavaConverters._

class NCRestUserSpec extends NCRestSpec {
    private final val PROPS = Map("k1" → "v1", "k2" → "v2").asJava

    @Test
    def test(): Unit = {
        // Checks own ID.
        post("user/get")(("$.id", (id: Number) ⇒ assertNotNull(id)))

        // Adds user.
        val id1 = addUser()

        // Checks.
        post("user/get", "id" → id1)(("$.id", (id: Number) ⇒ assertEquals(id1, id.longValue())))

        // Updates.

        post(
            "user/update",
            "firstName" → "firstName",
            "lastName" → "lastName",
            "avatarUrl" → "avatarUrl",
            "properties" → PROPS
        )()

        // Checks updated.
        post("user/get")(
            ("$.firstName", (firstName: String) ⇒ assertEquals("firstName", firstName)),
            ("$.lastName", (lastName: String) ⇒ assertEquals("lastName", lastName)),
            ("$.avatarUrl", (avatarUrl: String) ⇒ assertEquals("avatarUrl", avatarUrl)),
            ("$.properties", (properties: java.util.Map[String, String]) ⇒ assertEquals(PROPS, properties))
        )

        // Updates again.
        post(
            "user/update",
            "firstName" → "firstName2",
            "lastName" → "lastName2"
        )()

        // Checks updated.
        post("user/get")(
            ("$.firstName", (firstName: String) ⇒ assertEquals("firstName2", firstName)),
            ("$.lastName", (lastName: String) ⇒ assertEquals("lastName2", lastName)),
            ("$.avatarUrl", (avatarUrl: String) ⇒ assertEquals(null, avatarUrl)),
            ("$.properties", (properties: java.util.Map[String, String]) ⇒ assertEquals(null, properties))
        )

        // Updates (special cases).
        post("user/admin", "id" → id1, "admin" → true)()
        post("user/admin", "id" → id1, "admin" → false)()
        post("user/passwd/reset", "id" → id1, "newPasswd" → "test1")()

        // Checks existed.
        post("user/all")(("$.users", (users: DataMap) ⇒ assertTrue(containsLong(users, "id", id1))))

        // Deletes.
        post("user/delete", "id" → id1)()

        // Checks not existed.
        post("user/all")(("$.users", (users: DataMap) ⇒ assertFalse(containsLong(users, "id", id1))))

        // Adds.
        val id2 = addUser()
        val id3 = addUser()

        // Checks existed.
        post("user/all")(("$.users", (users: DataMap) ⇒ {
            assertTrue(containsLong(users, "id", id2))
            assertTrue(containsLong(users, "id", id3))
        }))

        // Deletes.
        post("user/delete")()

        // Checks not existed.
        post("user/all")(("$.users", (users: DataMap) ⇒ {
            assertFalse(containsLong(users, "id", id2))
            assertFalse(containsLong(users, "id", id3))
        }))
    }

    @Test
    def testParameters(): Unit = {
        checkUser()
        checkUser(isAdmin = true)
        checkUser(isAdmin = true, avatarUrl = Some("http:/test.com"))
        checkUser(isAdmin = true, avatarUrl = Some("http:/test.com"), props = Some(PROPS))
    }

    /**
      *
      * @param isAdmin
      * @param avatarUrl
      * @param props
      */
    private def addUser(
        isAdmin: Boolean = false, avatarUrl: Option[String] = None, props: Option[java.util.Map[String, String]] = None
    ): Long = {
        var usrId: Long = 0

        post(
            "user/add",
            "email" → s"${rnd()}@test.com",
            "passwd" → "test",
            "firstName" → "firstName",
            "lastName" → "lastName",
            "isAdmin" → isAdmin,
            "avatarUrl" → avatarUrl.orNull,
            "properties" → props.orNull
        )(
            ("$.id", (id: Number) ⇒ usrId = id.longValue())
        )

        assertTrue(usrId > 0)

        usrId
    }

    /**
      *
      * @param isAdmin
      * @param avatarUrl
      * @param props
      */
    private def checkUser(
        isAdmin: Boolean = false, avatarUrl: Option[String] = None, props: Option[java.util.Map[String, String]] = None
    ): Unit = post("user/delete", "id" → addUser(isAdmin, avatarUrl, props))()
}

