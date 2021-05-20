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

/**
  * Tests for 'company' functions.
  */
class NCIdlFunctionsCompany extends NCIdlFunctions {
    @Test
    def test(): Unit = {
        test(new NCCompany {
            override def getId: Long = -1
            override def getName: String = "name"
            override def getWebsite: Optional[String] = Optional.of("website")
            override def getCountry: Optional[String] = Optional.of("country")
            override def getRegion: Optional[String] = Optional.of("region")
            override def getCity: Optional[String] = Optional.of("city")
            override def getAddress: Optional[String] = Optional.of("address")
            override def getPostalCode: Optional[String] = Optional.of("code")
            override def getMetadata: util.Map[String, AnyRef] =
                Map("k1" -> "v1").map(p => p._1 -> p._2.asInstanceOf[AnyRef]).asJava
        })

        test(new NCCompany {
            override def getId: Long = 1
            override def getName: String = "name"
            override def getWebsite: Optional[String] = Optional.empty()
            override def getCountry: Optional[String] = Optional.empty()
            override def getRegion: Optional[String] = Optional.empty()
            override def getCity: Optional[String] = Optional.empty()
            override def getAddress: Optional[String] = Optional.empty()
            override def getPostalCode: Optional[String] = Optional.empty()
            override def getMetadata: util.Map[String, AnyRef] =
                Map("k1" -> "v1").map(p => p._1 -> p._2.asInstanceOf[AnyRef]).asJava
        })
    }

    private def test(comp: NCCompany): Unit = {
        val idlCtx = ctx(reqComp = comp)

        def mkTestDesc(truth: String): TestDesc = TestDesc(truth = truth, idlCtx = idlCtx)
        def get(opt: Optional[String]): String = if (opt.isEmpty) null else s"'${opt.get()}'"

        test(
            mkTestDesc(s"comp_name() == '${comp.getName}'"),
            mkTestDesc(s"comp_website() == ${get(comp.getWebsite)}"),
            mkTestDesc(s"comp_country() == ${get(comp.getCountry)}"),
            mkTestDesc(s"comp_region() == ${get(comp.getRegion)}"),
            mkTestDesc(s"comp_city() == ${get(comp.getCity)}"),
            mkTestDesc(s"comp_addr() == ${get(comp.getAddress)}"),
            mkTestDesc(s"comp_postcode() == ${get(comp.getPostalCode)}"),
            mkTestDesc(s"comp_id() == ${comp.getId}"),
            mkTestDesc(s"comp_id() != ${comp.getId + 1}")
        )
    }
}
