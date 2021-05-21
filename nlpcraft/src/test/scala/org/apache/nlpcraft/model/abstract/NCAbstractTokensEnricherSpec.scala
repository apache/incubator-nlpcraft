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

package org.apache.nlpcraft.model.`abstract`

import org.apache.nlpcraft.NCTestEnvironment
import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.{NCEnricherBaseSpec, NCEnrichersTestContext, NCTestNlpToken => nlp, NCTestUserToken => usr}
import org.junit.jupiter.api.Test

class NCAbstractTokensModelEnrichers extends NCAbstractTokensModel with NCEnrichersTestContext

@NCTestEnvironment(model = classOf[NCAbstractTokensModelEnrichers], startClient = true)
class NCAbstractTokensEnricherSpec extends NCEnricherBaseSpec {
    @Test
    def test(): Unit = {
        // Checks that there aren't any other variants.
        runBatch(
            _ => checkAll(
                "word the word",
                Seq(
                    nlp(text = "word"),
                    usr("the word", "wrapAnyWord")
                )
            ),
            _ => checkExists(
                "10 w1 10 w2",
                nlp(text = "10"),
                usr("w1 10 w2", "wrapNum")
            ),
            _ => checkExists(
                "before limit top 6 the any",
                usr("before limit top 6", "wrapLimit"),
                usr("the any", "wrapAnyWord")
            ),
            _ => checkExists(
                "a wrap before limit top 6 the any",
                nlp("a", isStop = true),
                usr("wrap before limit top 6", "wrapWrapLimit"),
                usr("the any", "wrapAnyWord")
            )
        )
    }
}