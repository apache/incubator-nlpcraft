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

package org.apache.nlpcraft.model.intent.idl.options

import org.apache.nlpcraft.model.`abstract`.NCAbstractTokensModel
import org.apache.nlpcraft.model.meta.NCMetaSpecAdapter
import org.apache.nlpcraft.model.{NCElement, NCIntent, NCResult}
import org.apache.nlpcraft.{NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util

class NCSysTokensModel extends NCAbstractTokensModel {
    override def getElements: util.Set[NCElement] = Set(
        NCTestElement("a"),
        NCTestElement("b"),
        NCTestElement("c"),
        NCTestElement("d")
    )

    @NCIntent(
        "intent=i1 " +
        "    options={" +
        "        'ordered': false, " +
        "        'unused_free_words': false, " +
        "        'unused_sys_toks': true, " +
        "        'unused_usr_toks': false, " +
        "        'allow_stm_only': false" +
        "    }" +
        "    term(a)={# == 'a'}" +
        "    term(b)={# == 'b'}"
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
        "    term(a)={# == 'c'}" +
        "    term(b)={# == 'd'}"
    )
    def i2(): NCResult = NCResult.text("i2")
}

@NCTestEnvironment(model = classOf[NCSysTokensModel], startClient = true)
class NCSysTokensSpec extends NCMetaSpecAdapter {
    @Test
    def test(): Unit = {
        checkResult("a b", "i1")
        checkResult("a today b", "i1")

        checkResult("c d", "i2")
        checkFail("c today d")
    }
}
