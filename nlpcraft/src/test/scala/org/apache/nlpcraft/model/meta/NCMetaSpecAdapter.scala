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

import org.apache.nlpcraft.common.JavaMeta
import org.apache.nlpcraft.server.rest.NCRestSpec

import java.util

/**
  * This test adapter provides user and company metadata manipulations methods.
  */
abstract class NCMetaSpecAdapter extends NCRestSpec {
    case class MetaHolder(userMeta: JavaMeta, companyMeta: JavaMeta)

    protected def getMeta(): MetaHolder = {
        var userMeta: JavaMeta = null
        var companyMeta: JavaMeta = null

        post("user/get")(
            ("$.properties", (props: JavaMeta) ⇒ userMeta = props)
        )

        post("company/get")(
            ("$.properties", (props: JavaMeta) ⇒ companyMeta = props)
        )

        MetaHolder(userMeta, companyMeta)
    }

    protected def setMeta(h: MetaHolder): Unit = {
        def convert(m: JavaMeta): JavaMeta = if (m == null) util.Collections.emptyMap() else m

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
}
