/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.server.rest

import org.apache.nlpcraft.NCTestElement
import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentSample, NCModelAdapter, NCResult, NCValue}

import java.util

/**
  * REST test model helper.
  */
object RestTestModel {
    final val K1 = "k1"
    final val K2 = "k2"
    final val K3 = "k3"

    final val V1 = "v1"
    final val V2 = 2.2.asInstanceOf[AnyRef]
    final val V3 = new util.HashMap[String, AnyRef]()

    V3.put(K1, V1)
    V3.put(K2, V2)
}

import RestTestModel._

/**
  * REST test model.
  */
class RestTestModel extends NCModelAdapter("rest.test.model", "REST test model", "1.0.0") {
    override def getElements: util.Set[NCElement] =
        Set(
            NCTestElement("a"),
            NCTestElement("b"),
            NCTestElement("x", "cat"),
            NCTestElement("meta"),
            NCTestElement("valElem", Seq("valElem1"), Map("v1"->Seq("v11", "v12"), "v2" -> Seq("v21")))
        )

    @NCIntent("intent=onA term(t)={tok_id() == 'a'}")
    @NCIntentSample(Array("My A"))
    private def a(): NCResult = NCResult.text("OK")

    @NCIntent("intent=onB term(t)={tok_id() == 'b'}")
    @NCIntentSample(Array("My B"))
    private def b(): NCResult = NCResult.text("OK")

    @NCIntent("intent=onMeta term(t)={tok_id() == 'meta'}")
    @NCIntentSample(Array("meta"))
    private def meta(): NCResult = {
        val res = NCResult.text("OK")

        res.getMetadata.put(K1, V1)
        res.getMetadata.put(K2, V2)
        res.getMetadata.put(K3, V3)

        res
    }
}
