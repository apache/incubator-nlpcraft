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

package org.apache.nlpcraft.model.synonyms

import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentMatch, NCModelAdapter, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util

abstract class NCSynonymsSpecModel extends NCModelAdapter("nlpcraft.syns.test.mdl", "Synonyms Test Model", "1.0") {
    override def getElements: util.Set[NCElement] =
        Set(
            NCTestElement("e1", "A"),

            // Text - 3 words.
            NCTestElement("e2", "X Y Z"),

            // Regex - 3 words.
            NCTestElement("e3", "{//AA//}[3, 3]"),

            // Nested - 3 words.
            NCTestElement("e4", "{^^{tok_id() == 'e1'}^^}[3, 3]")
        )

    @NCIntent("intent=onE1 term(t)={tok_id() == 'e1'}")
    def onE1(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=onE2 term(t)={tok_id() == 'e2'}")
    def onE2(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=onE3 term(t)={tok_id() == 'e3'}")
    def onE3(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=onE4 term(t)={tok_id() == 'e4'}")
    def onE4(ctx: NCIntentMatch): NCResult = NCResult.text("OK")
}

class NCSynonymsSpec extends NCTestContext {
    def body(testNonDir: Boolean): Unit = {
        checkIntent("A", "onE1")
        checkIntent("X Y Z", "onE2") // Text direct.
        if (testNonDir)
            checkIntent("Y X Z", "onE2") // Text not direct.
        checkIntent("AA AA AA", "onE3") // Regex.
        checkIntent("A A A", "onE4") // Nested.
    }
}

class NCSynonymsSpecModel1 extends NCSynonymsSpecModel {
    override def isPermutateSynonyms: Boolean = true
    override def isSparse: Boolean = true
}

@NCTestEnvironment(model = classOf[NCSynonymsSpecModel1], startClient = true)
class NCSynonymsSpec1 extends NCSynonymsSpec {
    @Test
    def test(): Unit = body(testNonDir = true)
}

class NCSynonymsSpecModel2 extends NCSynonymsSpecModel {
    override def isPermutateSynonyms: Boolean = false
    override def isSparse: Boolean = true
}

@NCTestEnvironment(model = classOf[NCSynonymsSpecModel2], startClient = true)
class NCSynonymsSpec2 extends NCSynonymsSpec {
    @Test
    def test(): Unit = body(testNonDir = false)
}

class NCSynonymsSpecModel3 extends NCSynonymsSpecModel {
    override def isPermutateSynonyms: Boolean = true
    override def isSparse: Boolean = false
}

@NCTestEnvironment(model = classOf[NCSynonymsSpecModel3], startClient = true)
class NCSynonymsSpec3 extends NCSynonymsSpec {
    @Test
    def test(): Unit = body(testNonDir = true)
}

class NCSynonymsSpecModel4 extends NCSynonymsSpecModel {
    override def isPermutateSynonyms: Boolean = false
    override def isSparse: Boolean = false
}

@NCTestEnvironment(model = classOf[NCSynonymsSpecModel4], startClient = true)
class NCSynonymsSpec4 extends NCSynonymsSpec {
    @Test
    def test(): Unit = body(testNonDir = false)
}

