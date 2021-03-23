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

package org.apache.nlpcraft.model.intent.idl.compiler.functions

import org.apache.nlpcraft.model.NCUser
import org.apache.nlpcraft.server.rest.NCRestSpec
import org.junit.jupiter.api.{BeforeEach, Test}

import java.util
import java.util.Optional

/**
  * Tests for 'user' functions.
  */
class NCIdlFunctionsUser extends NCRestSpec with NCIdlFunctions {
    private var usr: NCUser = _

    @BeforeEach
    def setCompany(): Unit = {
        var firstName: String = null
        var lastName: String = null
        var avatarUrl: String = null
        var email: String = null
        var adm: Boolean = false
        var props: java.util.Map[String, AnyRef] = null

        // Checks updated.
        post("user/get")(
            ("$.firstName", (v: String) ⇒ firstName = v),
            ("$.lastName", (v: String) ⇒ lastName = v),
            ("$.email", (v: String) ⇒ email = v),
            ("$.avatarUrl", (v: String) ⇒ avatarUrl = v),
            ("$.isAdmin", (v: Boolean) ⇒ adm = v),
            ("$.properties", (v: java.util.Map[String, AnyRef]) ⇒ props = v)
        )

        usr = new NCUser {
            override def getId: Long = -1  // TODO: No REST API data
            override def getFirstName: Optional[String] = Optional.ofNullable(firstName)
            override def getLastName: Optional[String] = Optional.ofNullable(lastName)
            override def getEmail: Optional[String] = Optional.ofNullable(email)
            override def getAvatarUrl: Optional[String] = Optional.ofNullable(avatarUrl)
            override def isAdmin: Boolean = adm
            override def getSignupTimestamp: Long = -1 // TODO: No REST API data
            override def getMetadata: util.Map[String, AnyRef] = props
        }
    }

    @Test
    def test(): Unit =  {
        val idlCtx = ctx(usr = usr)

        test(
            TrueFunc(
                truth = s"user_email() == '${usr.getEmail.get()}'",
                idlCtx = idlCtx
            )
        )
    }
}
