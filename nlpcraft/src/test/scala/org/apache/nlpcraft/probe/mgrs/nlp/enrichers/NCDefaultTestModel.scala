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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers

import org.apache.nlpcraft.NCTestElement
import org.apache.nlpcraft.model.{NCElement, NCModelAdapter, NCResult, NCValue}

import java.util
import java.util.Collections
import scala.collection.JavaConverters._
import scala.language.implicitConversions

import NCDefaultTestModel._

/**
  * Enrichers default test model.
  */
class NCDefaultTestModel extends NCModelAdapter(ID, "Model enrichers test", "1.0") with NCEnrichersTestContext {
    private implicit def convert(s: String): NCResult = NCResult.text(s)

    override def getElements: util.Set[NCElement] =
        Set(
            NCTestElement("A", "A"),
            NCTestElement("B", "B"),
            NCTestElement("C", "C"),
            NCTestElement("AB", "A B"),
            NCTestElement("BC", "B C"),
            NCTestElement("ABC", "A B C"),
            NCTestElement("D1", "D"),
            NCTestElement("D2", "D"),
            mkValueElement("V", "V1", "V2")
        ).asJava

    private def mkValueElement(id: String, vals: String*): NCElement =
        new NCElement {
            override def getId: String = id
            override def getValues: util.List[NCValue] = vals.map(v => new NCValue {
                override def getName: String = v
                override def getSynonyms: util.List[String] = Collections.singletonList(v)
            }).asJava
        }

    final override def getId: String = ID
}

object NCDefaultTestModel {
    final val ID = "dflt.enricher.test.model"
}
