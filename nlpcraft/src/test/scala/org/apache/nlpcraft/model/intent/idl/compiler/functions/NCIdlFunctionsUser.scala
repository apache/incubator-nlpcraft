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
        test(
            new NCUser {
                override def getId: Long = -1  // TODO: No REST API data (user_id() cannot be used)
                override def getFirstName: Optional[String] = Optional.of("firstName")
                override def getLastName: Optional[String] = Optional.of("lastName")
                override def getEmail: Optional[String] = Optional.of("email")
                override def getAvatarUrl: Optional[String] = Optional.of("avatar")
                override def isAdmin: Boolean = true
                override def getSignupTimestamp: Long = -1 // TODO: No REST API data (user_signup_tstamp() cannot be used)
                override def getMetadata: util.Map[String, AnyRef] =
                    Map("k1" → "v1").map(p ⇒ p._1 → p._2.asInstanceOf[AnyRef]).asJava
            }
        )
        test(
            new NCUser {
                override def getId: Long = -1  // TODO: No REST API data (user_id() cannot be used)
                override def getFirstName: Optional[String] = Optional.empty()
                override def getLastName: Optional[String] = Optional.empty
                override def getEmail: Optional[String] = Optional.empty
                override def getAvatarUrl: Optional[String] = Optional.empty
                override def isAdmin: Boolean = false
                override def getSignupTimestamp: Long = -1 // TODO: No REST API data (user_signup_tstamp() cannot be used)
                override def getMetadata: util.Map[String, AnyRef] =
                    Map("k1" → "v1").map(p ⇒ p._1 → p._2.asInstanceOf[AnyRef]).asJava
            }
        )
    }

    private def test(usr: NCUser): Unit = {
        val idlCtx = ctx(usr = usr)

        def mkTestDesc(truth: String): TestDesc = TestDesc(truth = truth, idlCtx = idlCtx)
        def get(opt: Optional[String]): String = if (opt.isEmpty) null else opt.get()

        test(
            mkTestDesc(s"user_fname() == '${get(usr.getFirstName)}'"),
            mkTestDesc(s"user_lname() == '${get(usr.getLastName)}'"),
            mkTestDesc(s"user_email() == '${get(usr.getEmail)}'"),
            mkTestDesc(s"user_admin() == '${usr.isAdmin}'")
        )
    }
}
