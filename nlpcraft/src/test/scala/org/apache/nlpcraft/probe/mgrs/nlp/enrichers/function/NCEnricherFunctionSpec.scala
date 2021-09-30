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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.function

import org.apache.nlpcraft.NCTestEnvironment
import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.{NCDefaultTestModel, NCEnricherBaseSpec, NCTestNlpToken => nlp, NCTestFunctionToken => fun, NCTestUserToken => usr}
import org.junit.jupiter.api.Test

/**
  * Limit enricher test.
  */
@NCTestEnvironment(model = classOf[NCDefaultTestModel], startClient = true)
class NCEnricherFunctionSpec extends NCEnricherBaseSpec {
    /**
      *
      * @throws Exception
      */
    @Test
    def test(): Unit =
        runBatch(
            _ => checkAll(
                "max A test",
                Seq(
                    fun(text = "max", `type` = "max", index = 1, note = "A"),
                    usr(text = "A", id = "A"),
                    nlp(text = "test")
                )
            ),
            _ => checkAll(
                "maximum the A, maximum the the A",
                Seq(
                    fun(text = "maximum", `type` = "max", index = 2, note = "A"),
                    nlp(text = "the", isStop = true),
                    usr(text = "A", id = "A"),
                    nlp(text = ",", isStop = true),
                    fun(text = "maximum", `type` = "max", index = 6, note = "A"),
                    nlp(text = "the the", isStop = true),
                    usr(text = "A", id = "A")
                )
            ),
            _ => checkAll(
                "maximum the A, maximum the the A the A",
                Seq(
                    fun(text = "maximum", `type` = "max", index = 2, note = "A"),
                    nlp(text = "the", isStop = true),
                    usr(text = "A", id = "A"),
                    nlp(text = ",", isStop = true),
                    fun(text = "maximum", `type` = "max", index = 6, note = "A"),
                    nlp(text = "the the", isStop = true),
                    usr(text = "A", id = "A"),
                    nlp(text = "the", isStop = true),
                    usr(text = "A", id = "A")
                )
            )

        )
}
