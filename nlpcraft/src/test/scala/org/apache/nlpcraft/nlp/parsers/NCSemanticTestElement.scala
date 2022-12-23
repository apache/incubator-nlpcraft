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

package org.apache.nlpcraft.nlp.parsers

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.parsers.NCSemanticElement

import java.util

/**
  *
  * @param id
  * @param synonyms
  * @param values
  * @param groups
  */
case class NCSemanticTestElement(
    id: String,
    synonyms: Set[String] = Set.empty,
    values: Map[String, Set[String]] = Map.empty,
    groups: Seq[String] = Seq.empty,
    props: Map[String, AnyRef] = Map.empty
) extends NCSemanticElement:
    override val getType: String = id
    override val getGroups: Set[String] = groups.toSet
    override val getValues: Map[String, Set[String]] = values
    override val getSynonyms: Set[String] = synonyms
    override val getProperties: Map[String, Object] = props

/**
  *
  */
object NCSemanticTestElement:
    def apply(id: String, synonyms: String*) = new NCSemanticTestElement(id, synonyms = synonyms.toSet)
