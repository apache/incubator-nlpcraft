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
import org.apache.nlpcraft.nlp.util.*

/**
  *
  */
object NCTestModelScala:
    object NCTestModelScalaObj extends NCTestModelAdapter :
        @NCIntent("intent=locInt term(single)~{# == 'id1'} term(list)~{# == 'id2'}[0,10] term(opt)~{# == 'id3'}?")
        @NCIntentSample(Array("What are the least performing categories for the last quarter?"))
        def intent(
            @NCIntentTerm("single") single: NCEntity,
            @NCIntentTerm("list") list: Seq[NCEntity],
            @NCIntentTerm("opt") opt: Option[NCEntity]
        ): NCResult = new NCResult()

        @NCIntent("import('scan/idl.idl')")
        @NCIntentSampleRef("scan/samples.txt")
        def intentImport(
            @NCIntentTerm("single") single: NCEntity,
            @NCIntentTerm("list") list: List[NCEntity],
            @NCIntentTerm("opt") opt: Option[NCEntity]
        ): NCResult = new NCResult()

    @NCIntent("import('scan/idl.idl')")
    class NCTestModelScalaClass extends NCTestModelAdapter :
        @NCIntent("intent=locInt term(single)~{# == 'id1'} term(list)~{# == 'id2'}[0,10] term(opt)~{# == 'id3'}?")
        @NCIntentSample(Array("What are the least performing categories for the last quarter?"))
        def intent(
            @NCIntentTerm("single") single: NCEntity,
            @NCIntentTerm("list") list: Seq[NCEntity],
            @NCIntentTerm("opt") opt: Option[NCEntity]
        ) = new NCResult()

        @NCIntentRef("impIntId")
        @NCIntentSampleRef("scan/samples.txt")
        def intentImport(
            @NCIntentTerm("single") single: NCEntity,
            @NCIntentTerm("list") list: List[NCEntity],
            @NCIntentTerm("opt") opt: Option[NCEntity]
        ) = new NCResult()

    /**
      *
      * @return
      */
    def mkModel: NCModel = new NCTestModelAdapter() :
        @NCIntent("intent=locInt term(single)~{# == 'id1'} term(list)~{# == 'id2'}[0,10] term(opt)~{# == 'id3'}?")
        @NCIntentSample(Array("What are the least performing categories for the last quarter?"))
        def intent(
            @NCIntentTerm("single") single: NCEntity,
            @NCIntentTerm("list") list: Seq[NCEntity],
            @NCIntentTerm("opt") opt: Option[NCEntity]
        ): NCResult = new NCResult()

        @NCIntent("import('scan/idl.idl')")
        @NCIntentSampleRef("scan/samples.txt")
        def intentImport(
            @NCIntentTerm("single") single: NCEntity,
            @NCIntentTerm("list") list: List[NCEntity],
            @NCIntentTerm("opt") opt: Option[NCEntity]
        ): NCResult = new NCResult()
