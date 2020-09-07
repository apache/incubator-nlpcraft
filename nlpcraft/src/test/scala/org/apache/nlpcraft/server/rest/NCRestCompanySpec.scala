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

class NCRestCompanySpec extends NCRestSpec {
    @Test
    def testCurrentCompany(): Unit = {
        var name: String = null

        post("company/get")(("$.name", (n: String) ⇒ {
            assertNotNull(n)

            name = n
        }))

        post("company/update", "name" → "newName")()
        post("company/get")(("$.name", (n: String) ⇒ assertEquals("newName", n)))
        post("company/update", "name" → name)()
        post("company/get")(("$.name", (n: String) ⇒ assertEquals(name, n)))
    }

    @Test
    def testNewCompany(): Unit = {
        val adminEmail = s"${rnd()}@test.com"
        val adminPswd = "test"

        // Adds company.
        post(
            "company/add",
            "name" → rnd(),
            "adminEmail" → adminEmail,
            "adminPasswd" → adminPswd,
            "adminFirstName" → "test",
            "adminLastName" → "test"
        )(
            ("$.token", (tkn: String) ⇒ assertNotNull(tkn)),
            ("$.adminId", (id: Number) ⇒ assertNotNull(id))
        )

        // Connects as new company admin.
        var adminTkn = signin(adminEmail, adminPswd)

        // Disconnects.
        signout(adminTkn)

        // Connects again.
        adminTkn = signin(adminEmail, adminPswd)

        post("company/token/reset", adminTkn)()
        post("company/delete", adminTkn)()
    }
}
