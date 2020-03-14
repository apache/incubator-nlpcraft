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

package org.apache.nlpcraft.model.intent.impl

import org.apache.nlpcraft.model.intent.utils.NCDslFlowItem
import org.scalatest.FlatSpec

/**
 * Unit tests for intent solver engine.
 */
class NCIntentSolverEngineSpec extends FlatSpec {
    behavior of "Intent solver engine"
    
    /**
     *
     * @param hist Matched intents.
     * @param flow Dialog flow template.
     * @return
     */
    private def matchFlow(hist: String, flow: (String/*Intent ID*/, Int/*min*/, Int/*max*/)*): Boolean = {
        NCIntentSolverEngine.matchFlow(
            flow.toArray.map(x ⇒ new NCDslFlowItem(x._1.split('|').map(_.trim), x._2, x._3)),
            hist.split(" ").map(_.trim)
        )
    }
    
    it should "match dialog flow" in {
        assert(!matchFlow("", ("a", 1, 1)))
        assert(matchFlow("a c", ("a", 1, 1), ("b", 0, 2), ("c", 1, 1)))
        assert(matchFlow("a b c", ("a", 1, 1), ("b", 0, 2), ("c", 1, 1)))
        assert(matchFlow("a b b c", ("a", 1, 1), ("b", 0, 2), ("c", 1, 1)))
        assert(matchFlow("a b b c", ("a", 1, 1), ("b|c", 0, 2), ("c", 1, 1)))
        assert(matchFlow("a a c c", ("a", 2, 2), ("b|c", 1, 2), ("d", 0, 1)))
        assert(matchFlow("a a c c d", ("a", 2, 2), ("b|c", 1, 2), ("d", 0, 1)))
        assert(matchFlow("a a c c e f g h", ("a", 2, 2), ("b|c", 1, 2), ("d", 0, 1)))
        assert(!matchFlow("a a c c x", ("a", 2, 2), ("b|c", 1, 2), ("d", 1, 1)))
    }
}
