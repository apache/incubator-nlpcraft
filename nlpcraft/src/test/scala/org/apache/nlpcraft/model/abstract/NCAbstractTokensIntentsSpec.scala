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

package org.apache.nlpcraft.model.`abstract`

import org.apache.nlpcraft.model.{NCIntent, NCIntentMatch, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

class NCAbstractTokensModelIntents extends NCAbstractTokensModel {
    @NCIntent("intent=wrapAnyWordIntent term(t)={# == 'wrapAnyWord'}")
    private def onWrapInternal(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=wrapNumIntent term(t)={# == 'wrapNum'}")
    private def onWrapNum(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=wrapLimitWrapAnyWord term(t1)={# == 'wrapLimit'} term(t2)={# == 'wrapAnyWord'}")
    private def wrapLimitWrapAnyWord(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=wrapWrapLimit term(t1)={# == 'wrapWrapLimit'} term(t2)={# == 'wrapAnyWord'}")
    private def wrapWrapLimit(ctx: NCIntentMatch): NCResult = NCResult.text("OK")
}

@NCTestEnvironment(model = classOf[NCAbstractTokensModelIntents], startClient = true)
class NCAbstractTokensIntentsSpec extends NCTestContext {
    @Test
    def test(): Unit = {
        // First 'word' - will be deleted (abstract).
        // Second 'word' - will be swallow (wrapAnyWord element).
        checkIntent("word the word", "wrapAnyWordIntent")

        // First numeric - will be deleted (abstract).
        // Second numeric - will be swallow (wrapNum element).
        checkIntent("10 w1 10 w2", "wrapNumIntent")

        checkIntent("before limit top 6 the any", "wrapLimitWrapAnyWord")
        checkIntent("a wrap before limit top 6 the any", "wrapWrapLimit")
    }
}