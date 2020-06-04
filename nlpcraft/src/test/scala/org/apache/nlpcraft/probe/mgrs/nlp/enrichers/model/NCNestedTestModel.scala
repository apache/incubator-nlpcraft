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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.model

import java.util
import java.util.Collections

import org.apache.nlpcraft.model._
import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.NCDefaultTestModel

import scala.collection.JavaConverters._

/**
  * Nested Elements test model.
  */
class NCNestedTestModel extends NCDefaultTestModel {
    override def getElements: util.Set[NCElement] =
        Set(
            mkElement("x1", "{test|*} ^^id == 'nlpcraft:date'^^"),
            mkElement("x2", "{test1|*} ^^id == 'x1'^^"),
            mkElement("x3", "{test2|*} ^^id == 'x2'^^"),
            mkElement("y1", "y"),
            mkElement("y2", "^^id == 'y1'^^"),
            mkElement("y3", "^^id == 'y2'^^ ^^id == 'y2'^^")
        ).asJava

    private def mkElement(id: String, syn: String): NCElement =
        new NCElement {
            override def getId: String = id
            override def getSynonyms: util.List[String] = Collections.singletonList(syn)
        }
}
