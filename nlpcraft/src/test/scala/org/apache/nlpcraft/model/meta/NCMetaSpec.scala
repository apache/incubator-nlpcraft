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
import org.apache.nlpcraft.{NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
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
class NCMetaSpec extends NCMetaSpecAdapter {
    @Test
    def testWithoutMeta(): Unit = require(getClient.ask("a").isFailed)

    @Test
    def testWithMeta(): Unit = {
        val currUserCompMeta = getMeta()
        val sys = new SystemProperties

        val m = Map("k1" -> "v1".asInstanceOf[Object]).asJava

        try {
            // Sets company and user metadata.
            setMeta(MetaHolder(m, m))

            // It is not enough.
            require(getClient.ask("a").isFailed)

            // Sets sys metadata.
            sys.put("k1", "v1")

            // Ok.
            require(getClient.ask("a").isOk)
        }
        finally {
            sys.remove("k1")

            setMeta(currUserCompMeta)
        }
    }
}
