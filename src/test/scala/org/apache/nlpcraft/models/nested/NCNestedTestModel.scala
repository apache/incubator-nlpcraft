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

package org.apache.nlpcraft.models.nested

import java.util
import java.util.Collections

import org.apache.nlpcraft.model.{NCIntentMatch, _}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

/**
  * Nested Elements test model.
  */
class NCNestedTestModel extends NCModelAdapter("nlpcraft.nested.test", "Nested Elements Test Model", "1.0") {
    private implicit def convert(s: String): NCResult = NCResult.text(s)

    override def getElements: util.Set[NCElement] =
        Set(
            mkElement("x:nested", "{test|*} ^^id == 'nlpcraft:date'^^"),
            mkElement("x:nested1", "{test1|*} ^^id == 'x:nested'^^"),
            mkElement("x:nested2", "{test2|*} ^^id == 'x:nested1'^^")
        ).asJava

    private def mkElement(id: String, syn: String): NCElement =
        new NCElement {
            override def getId: String = id
            override def getSynonyms: util.List[String] = Collections.singletonList(syn)
        }

    @NCIntent("intent=nested term={id=='x:nested'}")
    private def onNested(ctx: NCIntentMatch): NCResult = "nested"

    @NCIntent("intent=nested term={id=='x:nested1'}")
    private def onNested1(ctx: NCIntentMatch): NCResult = "nested1"

    @NCIntent("intent=nested term={id=='x:nested2'}")
    private def onNested2(ctx: NCIntentMatch): NCResult = "nested2"
}
