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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.limit

import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.{NCEnricherBaseSpec, NCTestLimitToken ⇒ lim, NCTestUserToken ⇒ usr}
import org.junit.jupiter.api.Test

/**
  * Limit enricher test.
  */
class NCEnricherLimitSpec extends NCEnricherBaseSpec {
    /**
      *
      * @throws Exception
      */
    @Test
    def test(): Unit = {
        runBatch(
            _ ⇒ checkExists(
                "top A",
                lim(text = "top", limit = 10, index = 1, note = "A", asc = false),
                usr(text = "A", id = "A")
            ),
            _ ⇒ checkExists(
                "few A B",
                lim(text = "few", limit = 3, index = 1, note = "AB", asc = false),
                usr(text = "A B", id = "AB")
            ),
            _ ⇒ checkExists(
                "top 10 D1",
                lim(text = "top 10", limit = 10, index = 1, note = "D1", asc = false),
                usr(text = "D1", id = "D1")
            ),
            _ ⇒ checkExists(
                "handful of A B",
                lim(text = "handful of", limit = 5, index = 1, note = "AB", asc = false),
                usr(text = "A B", id = "AB")
            )
        )
    }
}
