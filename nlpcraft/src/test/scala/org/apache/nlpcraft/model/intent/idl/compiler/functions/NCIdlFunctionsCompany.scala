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

import org.apache.nlpcraft.model.NCCompany
import org.apache.nlpcraft.server.rest.NCRestSpec
import org.junit.jupiter.api.{BeforeEach, Test}

import java.util
import java.util.Optional

/**
  * Tests for IDL functions.
  */
class NCIdlFunctionsCompany extends NCRestSpec with NCIdlFunctions {
    private var company: NCCompany = _

    @BeforeEach
    def setCompany(): Unit = {
        var compName: String = null
        var compWebsite: String = null
        var compCountry: String = null
        var compRegion: String = null
        var compCity: String = null
        var compAddress: String = null
        var compPostalCode: String = null
        var props: java.util.Map[String, AnyRef] = null

        post("company/get")(
            ("$.name", (v: String) ⇒ compName = v),
            ("$.website", (v: String) ⇒ compWebsite = v),
            ("$.country", (v: String) ⇒ compCountry = v),
            ("$.region", (v: String) ⇒ compRegion = v),
            ("$.city", (v: String) ⇒ compCity = v),
            ("$.address", (v: String) ⇒ compAddress = v),
            ("$.postalCode", (v: String) ⇒ compPostalCode = v),
            ("$.properties", (v: java.util.Map[String, AnyRef]) ⇒ props = v)
        )

        company = new NCCompany() {
            override def getId: Long = -1  // TODO: No REST API data
            override def getName: String = compName
            override def getWebsite: Optional[String] = Optional.ofNullable(compWebsite)
            override def getCountry: Optional[String] = Optional.ofNullable(compCountry)
            override def getRegion: Optional[String] = Optional.ofNullable(compRegion)
            override def getCity: Optional[String] = Optional.ofNullable(compCity)
            override def getAddress: Optional[String] = Optional.ofNullable(compAddress)
            override def getPostalCode: Optional[String] = Optional.ofNullable(compPostalCode)
            override def getMetadata: util.Map[String, AnyRef] = props
        }
    }

    @Test
    def test(): Unit = {
        val ctx = mkIdlContext(comp = company)

        test(
            BoolFunc(s"comp_name() == '${company.getName}'", ctx)
        )
    }
}
