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

package org.apache.nlpcraft.model.`abstract`

import org.apache.nlpcraft.NCTestElement
import org.apache.nlpcraft.model.{NCElement, NCModelAdapter}

import java.util
import scala.jdk.CollectionConverters.SetHasAsJava

class NCAbstractTokensModel extends NCModelAdapter(
    "nlpcraft.abstract.elems.mdl.test", "Abstract Elements Test Model", "1.0"
) {
    override def getElements: util.Set[NCElement] =
        Set(
            NCTestElement("anyWord", "//[a-zA-Z0-9]+//"),
            NCTestElement("wrapAnyWord", "the ^^[internal]{tok_id() == 'anyWord'}^^"),
            NCTestElement("wrapNum", "w1 ^^{tok_id() == 'nlpcraft:num'}^^ w2"),
            NCTestElement("wrapLimit", "before limit ^^[limitAlias]{tok_id() == 'nlpcraft:limit'}^^"),
            NCTestElement("wrapWrapLimit", "wrap ^^[wrapLimitAlias]{tok_id() == 'wrapLimit'}^^")
        )

    override def getAbstractTokens: util.Set[String] = Set("nlpcraft:num", "anyWord").asJava
    override def isPermutateSynonyms: Boolean = false
    override def isSparse: Boolean = false
}
