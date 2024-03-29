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

package org.apache.nlpcraft.internal.intent.compiler.functions

import org.apache.nlpcraft.*
import nlp.util.*
import internal.intent.compiler.functions.NCIDLFunctions.*

import scala.language.implicitConversions

/**
  * Tests for 'requests' functions.
  */
class NCIDLFunctionsModel extends NCIDLFunctions:
    test("test") {
        val idlCtx = mkIdlContext(cfg = CFG)

        require(idlCtx.mdlCfg.getOrigin.nonEmpty)

        def mkTestDesc(truth: String): TestDesc = TestDesc(truth = truth, idlCtx = idlCtx)

        test(
            mkTestDesc(s"mdl_id == '${idlCtx.mdlCfg.getId}'"),
            mkTestDesc(s"mdl_name == '${idlCtx.mdlCfg.getName}'"),
            mkTestDesc(s"mdl_ver == '${idlCtx.mdlCfg.getVersion}'"),
            mkTestDesc(s"mdl_origin == '${idlCtx.mdlCfg.getOrigin.orNull}'")
        )
    }

    test("test empty origin") {
        val idlCtx = mkIdlContext(cfg = NCModelConfig("testId", "test", "1.0"))

        require(idlCtx.mdlCfg.getOrigin.isEmpty)

        test(TestDesc(truth = s"null == ${idlCtx.mdlCfg.getOrigin.orNull}", idlCtx = idlCtx))
    }
