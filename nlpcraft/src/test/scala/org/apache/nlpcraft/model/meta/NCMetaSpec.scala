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

package org.apache.nlpcraft.model.meta

import org.apache.nlpcraft.model.`abstract`.NCAbstractTokensModel
import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentMatch, NCResult}
import org.apache.nlpcraft.server.rest.NCRestSpec
import org.apache.nlpcraft.{NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
import scala.collection.JavaConverters._
import scala.sys.SystemProperties

/**
  * Model for test following meta usage: company, user and system.
  */
class NcMetaModel extends NCAbstractTokensModel {
    override def getElements: util.Set[NCElement] = Set(NCTestElement("a", "a"))

    @NCIntent(
        "intent=i " +
        "  term(t)={" +
        "      tok_id() == 'a' && " +
        "      meta_user('k1') == 'v1' && " +
        "      meta_company('k1') == 'v1' && " +
        "      meta_sys('k1') == 'v1'" +
        "  }"
    )
    def onIntent(ctx: NCIntentMatch): NCResult = NCResult.text("OK")
}

@NCTestEnvironment(model = classOf[NcMetaModel], startClient = true)
class NCMetaSpec extends NCRestSpec {
    type Meta = java.util.Map[String, String]
    case class MetaHolder(userMeta: Meta, companyMeta: Meta)

    private def get(): MetaHolder = {
        var userMeta: Meta = null
        var companyMeta: Meta = null

        post("user/get")(
            ("$.properties", (props: java.util.Map[String, String]) ⇒ userMeta = props)
        )

        post("company/get")(
            ("$.properties", (props: java.util.Map[String, String]) ⇒ companyMeta = props)
        )

        MetaHolder(userMeta, companyMeta)
    }

    private def runTest(h: MetaHolder): Unit = {
        def convert(m: Meta): Meta = if (m == null) util.Collections.emptyMap() else m

        // 1. We have to save all existing company's fields for following updates.
        var compName: String = null
        var compWebsite: String = null
        var compCountry: String = null
        var compRegion: String = null
        var compCity: String = null
        var compAddress: String = null
        var compPostalCode: String = null

        post("company/get")(
            ("$.name", (v: String) ⇒ compName = v),
            ("$.website", (v: String) ⇒ compWebsite = v),
            ("$.country", (v: String) ⇒ compCountry = v),
            ("$.region", (v: String) ⇒ compRegion = v),
            ("$.city", (v: String) ⇒ compCity = v),
            ("$.address", (v: String) ⇒ compAddress = v),
            ("$.postalCode", (v: String) ⇒ compPostalCode = v)
        )

        post("company/update",
            "name" → compName,
            "website" → compWebsite,
            "country" → compCountry,
            "region" → compRegion,
            "city" → compCity,
            "address" → compAddress,
            "postalCode" → compPostalCode,
            "properties" → convert(h.companyMeta)
        )()

        // 2. We have to save all existing user's fields for following updates.
        var usrFirstName: String = null
        var usrLastName: String = null
        var usrAvatarUrl: String = null

        post("user/get")(
            ("$.firstName", (v: String) ⇒ usrFirstName = v),
            ("$.lastName", (v: String) ⇒ usrLastName = v),
            ("$.avatarUrl", (v: String) ⇒ usrAvatarUrl = v)
        )

        post("user/update",
        "firstName" → usrFirstName,
            "lastName" → usrLastName,
            "avatarUrl" → usrAvatarUrl,
            "properties" → convert(h.userMeta)
        )()
    }

    @Test
    def testWithoutMeta(): Unit = require(getClient.ask("a").isFailed)

    @Test
    def testWithMeta(): Unit = {
        val currUserCompMeta = get()
        val sys = new SystemProperties

        val m = Map("k1" → "v1").asJava

        try {
            // Sets company and user metadata.
            runTest(MetaHolder(m, m))

            // It is not enough.
            require(getClient.ask("a").isFailed)

            // Sets sys metadata.
            sys.put("k1", "v1")

            // Ok.
            require(getClient.ask("a").isOk)
        }
        finally {
            sys.remove("k1")

            runTest(currUserCompMeta)
        }
    }
}
