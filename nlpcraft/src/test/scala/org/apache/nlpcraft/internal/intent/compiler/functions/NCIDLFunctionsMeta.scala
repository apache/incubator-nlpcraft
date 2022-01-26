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
import org.apache.nlpcraft.internal.intent.NCIDLContext
import org.apache.nlpcraft.internal.intent.compiler.functions.NCIDLFunctions.*
import org.apache.nlpcraft.nlp.util.NCTestToken
import org.junit.jupiter.api.Test

import java.util
import java.util.Optional
import scala.jdk.CollectionConverters.MapHasAsJava
import scala.language.implicitConversions
import scala.sys.SystemProperties

/**
  * Tests for 'meta' functions.
  */
class NCIDLFunctionsMeta extends NCIDLFunctions:
    @Test
    def testMetaSys(): Unit =
        val sys = new SystemProperties

        sys.put("k1", "v1")

        try
            testValue("meta_sys")
        finally
            sys.remove("k1")

    @Test
    def testMetaEntity(): Unit =
        testValue(
            "meta_ent",
            entity = Option(mkEntity(meta = Map("k1" -> "v1"), NCTestToken()))
        )

    @Test
    def testMetaRequest(): Unit =
        testValue(
            "meta_req",
            mkIdlContext(reqData = Map("k1" -> "v1"))
        )

    @Test
    def testMetaConv(): Unit =
        testValue(
            "meta_conv",
            mkIdlContext(convMeta = Map("k1" -> "v1"))
        )

    @Test
    def testMetaFrag(): Unit =
        testValue(
            "meta_frag",
            mkIdlContext(fragMeta = Map("k1" -> "v1"))
        )

    // Simplified test.
    @Test
    def testMetaConfig(): Unit = testNoValue("meta_cfg", mkIdlContext())

    // Simplified test.
    @Test
    def testMetaIntent(): Unit = testNoValue("meta_intent", mkIdlContext())

    private def testValue(f: String, idlCtx: => NCIDLContext = mkIdlContext(), entity: Option[NCEntity] = None): Unit =
        test(TestDesc(truth = s"$f('k1') == 'v1'", entity = entity, idlCtx = idlCtx))

    private def testNoValue(f: String, idlCtx: => NCIDLContext = mkIdlContext(), entity: Option[NCEntity] = None): Unit =
        test(TestDesc(truth = s"$f('k1') == null", entity = entity, idlCtx = idlCtx))
