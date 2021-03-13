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

package org.apache.nlpcraft.model.properties

import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentMatch, NCModelAdapter, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
import scala.collection.JavaConverters._

class NCTokensRestrictionsModelAbstract extends NCModelAdapter(
    "nlpcraft.tokens.restr.test.mdl", "Tokens Restriction Test Model", "1.0"
) {
    override def getElements: util.Set[NCElement] = Set(NCTestElement("a"))

    @NCIntent("intent=onA term(t)={id() == 'a'}+")
    def onA(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=onLimit term(t)={id() == 'nlpcraft:limit'} term(a)={id() == 'a'}")
    def onLimit(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=onSort term(t)={id() == 'nlpcraft:sort'} term(a)={id() == 'a'}")
    def onSort(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=onRelation term(t)={id() == 'nlpcraft:relation'} term(a)={id() == 'a'}[2,2]")
    def onRelation(ctx: NCIntentMatch): NCResult = NCResult.text("OK")
}

// 1. Default model. Default behaviour, all relation elements will be found.
class NCTokensRestrictionsModel1 extends NCTokensRestrictionsModelAbstract

@NCTestEnvironment(model = classOf[NCTokensRestrictionsModel1], startClient = true)
class NCTokensRestrictionsSpec1 extends NCTestContext {
    @Test
    def test(): Unit = {
        checkIntent("a", "onA")
        checkIntent("top 5 a", "onLimit")
        checkIntent("a sorted desc", "onSort")
        checkIntent("compare a and a", "onRelation")
    }
}

// 2. Restrictions added, relation elements will not be found.
class NCTokensRestrictionsModel2 extends NCTokensRestrictionsModelAbstract {
    override def getRestrictedCombinations: util.Map[String, util.Set[String]] =
        Set("nlpcraft:limit", "nlpcraft:sort", "nlpcraft:relation").
            map(_ → Set("a").asJava).toMap.asJava
}

@NCTestEnvironment(model = classOf[NCTokensRestrictionsModel2], startClient = true)
class NCTokensRestrictionsSpec2 extends NCTestContext {
    @Test
    def test(): Unit = {
        checkIntent("a", "onA")
        checkIntent("top 5 a", "onA")
        checkIntent("a sorted desc", "onA")
        checkIntent("compare a and a", "onA")
    }
}