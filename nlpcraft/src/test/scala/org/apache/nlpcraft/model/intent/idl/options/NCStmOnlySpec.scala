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

class NCStmOnlyModel extends NCAbstractTokensModel {
    override def getElements: util.Set[NCElement] = Set(
        NCTestElement("a"),
        NCTestElement("b"),
        NCTestElement("c"),
        NCTestElement("d")
    )

    @NCIntent(
        "intent=before " +
            "    options={" +
            "        'ordered': false, " +
            "        'unused_free_words': false, " +
            "        'unused_sys_toks': false, " +
            "        'unused_usr_toks': false, " +
            "        'allow_stm_only': false" +
            "    }" +
            "    term(a)={tok_id() == 'a'}" +
            "    term(b)={tok_id() == 'b'}" +
            "    term(c)={tok_id() == 'c'}" +
            "    term(d)={tok_id() == 'd'}"
    )
    def before(): NCResult = NCResult.text("before")

    @NCIntent(
        "intent=i1 " +
        "    options={" +
        "        'ordered': false, " +
        "        'unused_free_words': true, " +
        "        'unused_sys_toks': false, " +
        "        'unused_usr_toks': false, " +
        "        'allow_stm_only': true" +
        "    }" +
        "    term(a)~{tok_id() == 'a'}" +
        "    term(b)~{tok_id() == 'b'}"
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
        "    term(a)={tok_id() == 'c'}" +
        "    term(b)={tok_id() == 'd'}"
    )
    def i2(): NCResult = NCResult.text("i2")
}

@NCTestEnvironment(model = classOf[NCStmOnlyModel], startClient = true)
class NCStmOnlySpec extends NCMetaSpecAdapter {
    private def clear(): Unit = {
        val client = getClient

        client.clearConversation()
        client.clearDialog()
    }

    @Test
    def test1(): Unit = {
        clear()

        checkResult("a b c d", "before")
        checkResult("b a d c", "before")
        checkResult("a b", "i1")

        // This should match as 'i1' intent allows:
        //   - unmatched free words, and
        //   - all of its matched tokens to come from STM, and
        //   - its terms are conversational (~).
        checkResult("x y", "i1")
    }

    @Test
    def test2(): Unit = {
        clear()

        checkResult("a b c d", "before")
        checkResult("c d", "i2")

        clear()

        checkResult("c d", "i2")
    }

    @Test
    def test3(): Unit = {
        clear()

        checkResult("a b c d", "before")
        checkResult("b a d c", "before")
        checkResult("a b", "i1")

        clear()

        // This should NOT match because STM is cleared.
        checkFail("x y")
    }
}
