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
import org.junit.jupiter.api.Test

import java.util
import java.util.Optional
import scala.collection.JavaConverters._

/**
  * Tests for 'company' functions.
  */
class NCIdlFunctionsCompany extends NCIdlFunctions {
    @Test
    def test(): Unit = {
        val comp = new NCCompany {
            override def getId: Long = -1 // TODO: No REST API data
            override def getName: String = "name"
            override def getWebsite: Optional[String] = Optional.of("website")
            override def getCountry: Optional[String] = Optional.of("country")
            override def getRegion: Optional[String] = Optional.of("region")
            override def getCity: Optional[String] = Optional.of("city")
            override def getAddress: Optional[String] = Optional.of("address")
            override def getPostalCode: Optional[String] = Optional.of("code")
            override def getMetadata: util.Map[String, AnyRef] =
                Map("k1" → "v1").map(p ⇒ p._1 → p._2.asInstanceOf[AnyRef]).asJava
        }

        val idlCtx = ctx(comp = comp)

        test(
            TestDesc(
                truth = s"comp_name() == '${comp.getName}'",
                idlCtx = idlCtx
            )
        )
    }
}
