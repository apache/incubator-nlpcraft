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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.model.anyword.adapters

import org.apache.nlpcraft.NCTestElement
import org.apache.nlpcraft.model.{NCElement, NCIntent, NCResult}

import java.util
import scala.jdk.CollectionConverters.SetHasAsJava

/**
  * 'any' ('not greedy') element - regex.
  * 'compose' element.
  * intent without DSL.
  * 'any' element's position is not restricted.
  */
abstract class NCNestedTestModelAny1 extends NCNestedModelAnyAdapter {
    override def getAbstractTokens: util.Set[String] = Set("a", "b", "any").asJava

    // Variants:
    // a t1 t2 t3 b - 1
    // a t1 t2 b - 1
    // a t1 b - 1
    // a t1 t2 t3 t4 b - 1
    // a b - 1
    override def getElements: util.Set[NCElement] =
        Set(
            NCTestElement("a"),
            NCTestElement("b"),
            mkNotGreedy("any", s"$anyDefinition[1, 3]"),
            NCTestElement("compose", "^^{# == 'a'}^^ ^^{# == 'any'}^^ ^^{# == 'b'}^^")
        )

    @NCIntent("intent=compose term(x)={# == 'compose'}")
    def onCompose(): NCResult = NCResult.text("OK")
}