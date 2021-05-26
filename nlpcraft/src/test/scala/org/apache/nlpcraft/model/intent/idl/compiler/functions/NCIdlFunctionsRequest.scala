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

package org.apache.nlpcraft.model.intent.idl.compiler.functions

import org.junit.jupiter.api.Test

/**
  * Tests for 'requests' functions.
  */
class NCIdlFunctionsRequest extends NCIdlFunctions {
    @Test
    def test(): Unit = {
        val reqSrvReqId = "id"
        val reqNormText = "some text"
        val reqTstamp: java.lang.Long = 123
        val reqAddr = "address"
        val reqAgent = "agent"

        val idlCtx = ctx(
            reqSrvReqId = reqSrvReqId,
            reqNormText = reqNormText,
            reqTstamp = reqTstamp,
            reqAddr = reqAddr,
            reqAgent = reqAgent
        )

        def mkTestDesc(truth: String): TestDesc = TestDesc(truth = truth, idlCtx = idlCtx)

        test(
            mkTestDesc(s"req_id() == '$reqSrvReqId'"),
            mkTestDesc(s"req_normtext() == '$reqNormText'"),
            mkTestDesc(s"req_tstamp() == $reqTstamp"),
            mkTestDesc(s"req_addr() == '$reqAddr'"),
            mkTestDesc(s"req_agent() == '$reqAgent'")
        )
    }
}
