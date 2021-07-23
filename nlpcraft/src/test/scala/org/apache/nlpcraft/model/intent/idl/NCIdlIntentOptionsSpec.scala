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

package org.apache.nlpcraft.model.intent.idl

import org.apache.nlpcraft.model.`abstract`.NCAbstractTokensModel
import org.apache.nlpcraft.model.meta.NCMetaSpecAdapter
import org.apache.nlpcraft.model.{NCElement, NCIntent, NCResult}
import org.apache.nlpcraft.{NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util

class NCIdlIntentOptionsModel extends NCAbstractTokensModel {
    override def getElements: util.Set[NCElement] = Set(
        NCTestElement("i1_a"),
        NCTestElement("i1_b"),

        NCTestElement("i2_a"),
        NCTestElement("i2_b")
    )

    @NCIntent(
        "intent=i1 " +
        "    options={" +
        "        'ordered': true, " +
        "        'unused_free_words': false, " +
        "        'unused_sys_toks': false, " +
        "        'unused_usr_toks': false, " +
        "        'allow_stm_only': false" +
        "    }" +
        "    term(a)={tok_id() == 'i1_a'}" +
        "    term(b)={tok_id() == 'i1_b'}"
    )
    def i1(): NCResult = NCResult.text("i1")

    @NCIntent(
        "intent=i2 " +
        "    options={" +
        "        'ordered': false, " +
        "        'unused_free_words': false, " +
        "        'unused_sys_toks': false, " +
        "        'unused_usr_toks': false, " +
        "        'allow_stm_only': false" +
        "    }" +
        "    term(a)={tok_id() == 'i2_a'}" +
        "    term(b)={tok_id() == 'i2_b'}"
    )
    def i2(): NCResult = NCResult.text("i2")
}

@NCTestEnvironment(model = classOf[NCIdlIntentOptionsModel], startClient = true)
class NCIdlIntentOptionsSpec extends NCMetaSpecAdapter {
    @Test
    def test(): Unit = {
        checkResult("i1_a i1_b", "i1")
        checkFail("i1_b i1_a")

        checkResult("i2_a i2_b", "i2")
        checkResult("i2_b i2_a", "i2")
    }
}
