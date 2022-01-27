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

package org.apache.nlpcraft.internal.impl.scan

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.impl.NCAnnotationsScanner
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.Test

/**
  * It tests imports and nested objects usage.
  */
class NCAnnotationNestedSpec:
    private val mdl: NCModel = new NCModel :
        override def getConfig: NCModelConfig = CFG
        override def getPipeline: NCModelPipeline = EN_PIPELINE

        @NCIntentObject
        val nested1: Object = new Object() {
            @NCIntentObject
            val nested2: Object = new Object() {
                @NCIntentImport(Array("scan/idl.idl"))
                @NCIntent("intent=intent3 term(x)~{true}")
                def intent1(@NCIntentTerm("x") x: NCEntity) = new NCResult()
            }

            @NCIntent("intent=intent2 term(x)~{true}")
            def intent1(@NCIntentTerm("x") x: NCEntity) = new NCResult()
        }

        @NCIntent("intent=intent1 term(x)~{true}")
        def intent1(@NCIntentTerm("x") x: NCEntity) = new NCResult()

        // Imported in `nested2` (scan/idl.idl)
        @NCIntentRef("impIntId")
        def intent4(
            @NCIntentTerm("single") single: NCEntity,
            @NCIntentTerm("list") list: List[NCEntity],
            @NCIntentTerm("opt") opt: Option[NCEntity]
        ): NCResult = new NCResult()

    @Test
    def test(): Unit = require(new NCAnnotationsScanner(mdl).scan().sizeIs == 4)
