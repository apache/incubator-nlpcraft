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
        var compName: String = null

        post("company/get")(("$.name", (name: String) ⇒ {
            assertNotNull(name)

            compName = name
        }))

        post("company/update", "webSite" → compName)()
        post("company/get")(("$.name", (n: String) ⇒ assertEquals("newName", n)))
        post("company/update", "name" → compName)()
        post("company/get")(("$.name", (n: String) ⇒ assertEquals(compName, n)))
    }

    @Test
    def testNewCompany(): Unit = {
        val compName = rnd()

        val adminEmail = s"$compName@test.com"
        val adminPswd = "test"

        // Adds company.
        post(
            "company/add",
            "name" → compName,
            "website" → "website",
            "country" → "country",
            "region" → "region",
            "city" → "city",
            "address" → "address",
            "postalCode" → "postalCode",
            "adminEmail" → adminEmail,
            "adminPasswd" → adminPswd,
            "adminFirstName" → "firstName",
            "adminLastName" → "lastName",
            "adminAvatarUrl" → "avatarUrl"
        )(
            ("$.token", (tkn: String) ⇒ assertNotNull(tkn)),
            ("$.adminId", (id: Number) ⇒ assertNotNull(id))
        )

        val adminTkn = signin(adminEmail, adminPswd)

        // Checks company fields.
        post("company/get", adminTkn)(
            ("$.name", (name: String) ⇒ assertEquals(compName, name)),
            ("$.website", (website: String) ⇒ assertEquals("website", website)),
            ("$.country", (country: String) ⇒ assertEquals("country", country)),
            ("$.region", (region: String) ⇒ assertEquals("region", region)),
            ("$.city", (city: String) ⇒ assertEquals("city", city)),
            ("$.address", (address: String) ⇒ assertEquals("address", address)),
            ("$.postalCode", (postalCode: String) ⇒ assertEquals("postalCode", postalCode))
        )

        // Checks company's admin fields.
        post("user/get", adminTkn)(
            ("$.isAdmin", (isAdmin: Boolean) ⇒ assertTrue(isAdmin)),
            ("$.email", (email: String) ⇒ assertEquals(adminEmail, email)),
            ("$.firstName", (firstName: String) ⇒ assertEquals("firstName", firstName)),
            ("$.lastName", (lastName: String) ⇒ assertEquals("lastName", lastName)),
            ("$.avatarUrl", (avatarUrl: String) ⇒ assertEquals("avatarUrl", avatarUrl))
        )

        // Updates company.
        post("company/update", adminTkn,
            "name" → compName,
            "website" → "website2",
            "country" → "country2",
            "region" → "region2",
            "city" → "city2",
            "address" → "address2",
            "postalCode" → "postalCode2"
        )()

        // Checks company fields.
        post("company/get", adminTkn)(
            ("$.name", (name: String) ⇒ assertEquals(compName, name)),
            ("$.website", (website: String) ⇒ assertEquals("website2", website)),
            ("$.country", (country: String) ⇒ assertEquals("country2", country)),
            ("$.region", (region: String) ⇒ assertEquals("region2", region)),
            ("$.city", (city: String) ⇒ assertEquals("city2", city)),
            ("$.address", (address: String) ⇒ assertEquals("address2", address)),
            ("$.postalCode", (postalCode: String) ⇒ assertEquals("postalCode2", postalCode))
        )

        // Updates company.
        post("company/update", adminTkn,
            "name" → compName,
            "website" → "",
            "country" → "",
            "region" → "",
            "city" → "",
            "address" → "",
            "postalCode" → "",
        )()

        // Checks company fields.
        post("company/get", adminTkn)(
            ("$.name", (name: String) ⇒ assertEquals(compName, name)),
            ("$.website", (website: String) ⇒ assertEquals("", website)),
            ("$.country", (country: String) ⇒ assertEquals("", country)),
            ("$.region", (region: String) ⇒ assertEquals("", region)),
            ("$.city", (city: String) ⇒ assertEquals("", city)),
            ("$.address", (address: String) ⇒ assertEquals("", address)),
            ("$.postalCode", (postalCode: String) ⇒ assertEquals("", postalCode))
        )

        // Resets token.
        post("company/token/reset", adminTkn)()

        // Deletes company.
        post("company/delete", adminTkn)()
    }
}
