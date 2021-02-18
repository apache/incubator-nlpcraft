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

package org.apache.nlpcraft.model.`abstract`

import org.apache.nlpcraft.model.{NCElement, NCModelAdapter}

import java.util
import scala.collection.JavaConverters._

class NCAbstractTokensModel extends NCModelAdapter(
    "nlpcraft.abstract.elems.mdl.test", "Abstract Elements Test Model", "1.0"
) {
    private implicit val toList: String ⇒ util.List[String] = (s: String) ⇒ Seq(s).asJava

    override def getElements: util.Set[NCElement] =
        Set(
            new NCElement {
                override def getId: String = "anyWord"
                override def getSynonyms: util.List[String] = "//[a-zA-Z0-9]+//"
            },
            new NCElement {
                override def getId: String = "wrapAnyWord"
                override def getSynonyms: util.List[String] = "the ^^[internal](id == 'anyWord')^^"
            },
            new NCElement {
                override def getId: String = "wrapNum"
                override def getSynonyms: util.List[String] = "w1 ^^id == 'nlpcraft:num'^^ w2"
            },
            new NCElement {
                override def getId: String = "wrapLimit"
                override def getSynonyms: util.List[String] = "before limit ^^[limitAlias](id == 'nlpcraft:limit')^^"
            },
            new NCElement {
                override def getId: String = "wrapWrapLimit"
                override def getSynonyms: util.List[String] = "wrap ^^[wrapLimitAlias](id == 'wrapLimit')^^"
            }
        ).asJava

    override def getAbstractTokens: util.Set[String] = Set("nlpcraft:num", "anyWord").asJava
    override def isPermutateSynonyms: Boolean = false
    override def getJiggleFactor: Int = 0
}
