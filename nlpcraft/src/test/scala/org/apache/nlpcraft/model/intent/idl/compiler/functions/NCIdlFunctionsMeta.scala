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

import org.apache.nlpcraft.model.intent.NCIdlContext
import org.apache.nlpcraft.model.{NCCompany, NCToken, NCUser}
import org.junit.jupiter.api.Test

import java.util
import java.util.Optional
import scala.sys.SystemProperties

/**
  * Tests for 'meta' functions.
  */
class NCIdlFunctionsMeta extends NCIdlFunctions {
    @Test
    def testMetaSys(): Unit = {
        val sys = new SystemProperties

        sys.put("k1", "v1")

        try
            testValue("meta_sys")
        finally
            sys.remove("k1")
    }

    @Test
    def testMetaToken(): Unit =
        testValue(
            "meta_tok",
            token = Some(tkn(meta = Map("k1" -> "v1")))
        )

    @Test
    def testMetaRequest(): Unit =
        testValue(
            "meta_req",
            ctx(reqData = Map("k1" -> "v1"))
        )

    @Test
    def testMetaConv(): Unit =
        testValue(
            "meta_conv",
            ctx(convMeta = Map("k1" -> "v1"))
        )

    @Test
    def testMetaFrag(): Unit =
        testValue(
            "meta_frag",
            ctx(fragMeta = Map("k1" -> "v1"))
        )

    @Test
    def testMetaUser(): Unit =
        testValue(
            "meta_user",
            ctx(reqUsr =
                new NCUser {
                    override def getId: Long = -1
                    override def getFirstName: Optional[String] = Optional.empty()
                    override def getLastName: Optional[String] = Optional.empty()
                    override def getEmail: Optional[String] = Optional.empty()
                    override def getAvatarUrl: Optional[String] = Optional.empty()
                    override def isAdmin: Boolean = true
                    override def getSignupTimestamp: Long = -1
                    override def getMetadata: util.Map[String, AnyRef] =
                        Map("k1" -> "v1").map(p => p._1 -> p._2.asInstanceOf[AnyRef]).asJava
                }
            )
        )

    @Test
    def testMetaCompany(): Unit =
        testValue(
            "meta_company",
            ctx(reqComp =
                new NCCompany {
                    override def getId: Long = -1
                    override def getName: String = "name"
                    override def getWebsite: Optional[String] = Optional.empty()
                    override def getCountry: Optional[String] = Optional.empty()
                    override def getRegion: Optional[String] = Optional.empty()
                    override def getCity: Optional[String] = Optional.empty()
                    override def getAddress: Optional[String] = Optional.empty()
                    override def getPostalCode: Optional[String] = Optional.empty()
                    override def getMetadata: util.Map[String, AnyRef] =
                        Map("k1" -> "v1").map(p => p._1 -> p._2.asInstanceOf[AnyRef]).asJava
                }
            )
        )

    // Simplified test.
    @Test
    def testMetaModel(): Unit = testNoValue("meta_model", ctx())

    // Simplified test.
    @Test
    def testMetaIntent(): Unit = testNoValue("meta_intent", ctx())

    private def testValue(f: String, idlCtx: => NCIdlContext = ctx(), token: Option[NCToken] = None): Unit =
        test(TestDesc(truth = s"$f('k1') == 'v1'", token = token, idlCtx = idlCtx))

    private def testNoValue(f: String, idlCtx: => NCIdlContext = ctx(), token: Option[NCToken] = None): Unit =
        test(TestDesc(truth = s"$f('k1') == null", token = token, idlCtx = idlCtx))
}
