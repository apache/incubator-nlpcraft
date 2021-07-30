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

package org.apache.nlpcraft.models.stm.indexes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.nlpcraft.model.{NCElement, NCModelAdapter}

import java.util
import java.util.Collections
import scala.jdk.CollectionConverters.{SeqHasAsJava, SetHasAsJava}

object NCSpecModelAdapter {
    val mapper = new ObjectMapper()

    mapper.registerModule(new DefaultScalaModule())
}

class NCSpecModelAdapter extends NCModelAdapter("nlpcraft.stm.idxs.test", "STM Indexes Test Model", "1.0") {
    override def getElements: util.Set[NCElement] =
        Set(
            mkElement("A2", "G1", "a a"),
            mkElement("B2", "G1", "b b"),

            mkElement("X", "G2", "x"),
            mkElement("Y", "G2", "y"),

            mkElement("Z", "G3", "z")
        ).asJava

    private def mkElement(id: String, group: String, syns: String*): NCElement =
        new NCElement {
            override def getId: String = id
            override def getSynonyms: util.List[String] = {
                val seq: Seq[String] = syns

                seq.asJava
            }
            override def getGroups: util.List[String] = Collections.singletonList(group)
        }
}
