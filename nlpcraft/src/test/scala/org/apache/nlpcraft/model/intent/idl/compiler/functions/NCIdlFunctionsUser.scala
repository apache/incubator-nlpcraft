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
import org.junit.jupiter.api.Test

import java.util
import java.util.Optional
import scala.collection.JavaConverters._

/**
  * Tests for 'user' functions.
  */
class NCIdlFunctionsUser extends NCIdlFunctions {
    @Test
    def test(): Unit = {
        val usr = new NCUser {
            override def getId: Long = -1  // TODO: No REST API data
            override def getFirstName: Optional[String] = Optional.of("firstName")
            override def getLastName: Optional[String] = Optional.of("lastName")
            override def getEmail: Optional[String] = Optional.of("email")
            override def getAvatarUrl: Optional[String] = Optional.of("avatar")
            override def isAdmin: Boolean = true
            override def getSignupTimestamp: Long = -1 // TODO: No REST API data
            override def getMetadata: util.Map[String, AnyRef] =
                Map("k1" → "v1").map(p ⇒ p._1 → p._2.asInstanceOf[AnyRef]).asJava
        }

        val idlCtx = ctx(usr = usr)

        test(
            TestDesc(
                truth = s"user_email() == '${usr.getEmail.get()}'",
                idlCtx = idlCtx
            )
        )
    }
}
