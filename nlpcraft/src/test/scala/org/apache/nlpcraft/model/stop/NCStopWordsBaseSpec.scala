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

package org.apache.nlpcraft.model.stop

import org.apache.nlpcraft.model.{NCElement, NCIntent, NCModelAdapter, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
import scala.language.implicitConversions

/**
  *
  */
class NCStopWordsBaseModel extends NCModelAdapter("nlpcraft.test", "Test Model", "1.0") {
    override def getElements: util.Set[NCElement] = Set(
        NCTestElement("a"),
        NCTestElement("b"),
        NCTestElement("xy", "x y"),
    )

    @NCIntent(
        "intent=twoWords " +
        "    term(a)~{# == 'a'}" +
        "    term(b)~{# == 'b'}"
    )
    def onTwoWords(): NCResult = NCResult.text("OK")

    @NCIntent(
        "intent=oneWord " +
        "    term(xt)~{# == 'xy'}"
    )
    def onOneWord(): NCResult = NCResult.text("OK")
}

/**
  *
  */
@NCTestEnvironment(model = classOf[NCStopWordsBaseModel], startClient = true)
class NCStopWordsBaseSpec extends NCTestContext {
    @Test
    def testTwoWords(): Unit = {
        checkIntent("a b", "twoWords")
        checkIntent("a the b", "twoWords")
        checkIntent("a the the b", "twoWords")
        checkIntent("the a the b", "twoWords")
        checkIntent("the a the b the the", "twoWords")
    }

    @Test
    def testOneWord(): Unit = {
        checkIntent("x y", "oneWord")
        checkIntent("x the y", "oneWord")
        checkIntent("x the the y", "oneWord")
        checkIntent("the x the y", "oneWord")
        checkIntent("the x the y the the", "oneWord")
    }
}