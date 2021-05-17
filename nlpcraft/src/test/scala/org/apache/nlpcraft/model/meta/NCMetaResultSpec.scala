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
import org.apache.nlpcraft.model.{NCElement, NCIntent, NCResult}
import org.apache.nlpcraft.{NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util

/**
  * Test model.
  */
object NCMetaResultSpecModel {
    final val K1 = "k1"
    final val K2 = "k2"
    final val K3 = "k3"

    final val V1 = "v1"
    final val V2 = 2.2.asInstanceOf[AnyRef]
    final val V3 = new util.HashMap[String, AnyRef]()

    V3.put(K1, V1)
    V3.put(K2, V2)
}

import org.apache.nlpcraft.model.meta.NCMetaResultSpecModel._

class NCMetaResultSpecModel extends NCAbstractTokensModel {
    override def getElements: util.Set[NCElement] = Set(NCTestElement("a"))

    @NCIntent("intent=i term(t)={tok_id() == 'a'}")
    def onIntent(): NCResult = {
        val res = NCResult.text("OK")

        res.getMetadata.put(K1, V1)
        res.getMetadata.put(K2, V2)
        res.getMetadata.put(K3, V3)

        res
    }
}

@NCTestEnvironment(model = classOf[NCMetaResultSpecModel], startClient = true)
class NCMetaResultSpec extends NCMetaSpecAdapter {
    @Test
    def test(): Unit = {
        val res = getClient.ask("a")

        require(res.isOk)
        require(res.getResultMeta.isPresent)

        val meta = res.getResultMeta.get()

        println(s"Meta received: $meta")

        require(meta.get(K1) == V1)
        require(meta.get(K2) == V2)
        require(meta.get(K3) == V3)
    }
}
