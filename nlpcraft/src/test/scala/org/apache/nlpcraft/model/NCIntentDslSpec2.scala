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

package org.apache.nlpcraft.model

import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test

import java.util
import scala.collection.JavaConverters._
import scala.language.implicitConversions

/**
  * Intents DSL test model.
  */
class NCIntentDslSpecModel2 extends NCModelAdapter(
    "nlpcraft.intents.dsl.test", "Intents DSL Test Model", "1.0"
) {
    private implicit def convert(s: String): NCResult = NCResult.text(s)

    override def getElements: util.Set[NCElement] =
        Set("a", "b", "c", "d", "e").map(id ⇒ new NCElement { override def getId: String = id }).asJava

    // a. Mandatory, List, +, *, ?
    @NCIntent("intent=aMandatory term(a)={id == 'a' }")
    private def aMandatory(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntent("intent=aList term(a)={id == 'a' }[1,3]")
    private def aList(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntent("intent=aPlus term(a)={id == 'a' }+")
    private def aPlus(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntent("intent=aAsterisk term(a)={id == 'a' }*")
    private def aAsterisk(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntent("intent=aOptional term(a)={id == 'a' }?")
    private def aOptional(ctx: NCIntentMatch): NCResult = "OK"

    // b. List, +, *, ?
    @NCIntent("intent=bList term(b)={id == 'b' }[1,3]")
    private def bList(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntent("intent=bPlus term(b)={id == 'b' }+")
    private def bPlus(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntent("intent=bAsterisk term(b)={id == 'b' }*")
    private def bAsterisk(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntent("intent=bOptional term(b)={id == 'b' }?")
    private def bOptional(ctx: NCIntentMatch): NCResult = "OK"

    // c. +, *, ?
    @NCIntent("intent=cPlus term(c)={id == 'c' }+")
    private def cPlus(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntent("intent=cAsterisk term(c)={id == 'c' }*")
    private def cAsterisk(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntent("intent=cOptional term(c)={id == 'c' }?")
    private def cOptional(ctx: NCIntentMatch): NCResult = "OK"

    // d. *, ?
    @NCIntent("intent=dAsterisk term(d)={id == 'd' }*")
    private def dAsterisk(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntent("intent=dOptional term(d)={id == 'd' }?")
    private def dOptional(ctx: NCIntentMatch): NCResult = "OK"

    // e. ?
    @NCIntent("intent=eOptional term(e)={id == 'e' }?")
    private def eOptional(ctx: NCIntentMatch): NCResult = "OK"
}

/**
  * Intents DSL test.
  */
@NCTestEnvironment(model = classOf[NCIntentDslSpecModel2], startClient = true)
class NCIntentDslSpec2 extends NCTestContext {
    private def check(txt: String, intent: String): Unit = {
        val res = getClient.ask(txt)

        assertTrue(res.isOk, s"Checked: $txt")
        assertTrue(res.getResult.isPresent, s"Checked: $txt")
        assertEquals(intent, res.getIntentId, s"Checked: $txt")
    }

    private def checkError(txt: String): Unit = {
        val res = getClient.ask(txt)

        assertFalse(res.isOk)
    }

    @Test
    def test(): Unit = {
        check("a", "aMandatory")
        check("a a", "aList")
        check("a a a", "aList")
        check("a a a a", "aPlus")

        check("b", "bList")
        check("b b", "bList")
        check("b b b", "bList")
        check("b b b b", "bPlus")

        check("c", "cPlus")
        check("c c", "cPlus")
        check("c c c", "cPlus")
        check("c c c c", "cPlus")

        check("d", "dAsterisk")
        check("d d", "dAsterisk")
        check("d d d", "dAsterisk")
        check("d d d d", "dAsterisk")

        check("Moscow", "eOptional")
        check("e Moscow", "eOptional")
        checkError("e e")
        checkError("e e e")
    }
}

