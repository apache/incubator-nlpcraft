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

package org.apache.nlpcraft

import org.apache.nlpcraft.model.NCElement

import java.util
import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.language.implicitConversions

/**
  * Simple test element.
  */
case class NCTestElement(id: String, syns: String*) extends NCElement {
    override def getId: String = id
    override def getSynonyms: util.List[String] = (syns :+ id).asJava
}

/**
  * Simple test element helper.
  */
object NCTestElement {
    private def to(e: NCTestElement): NCElement = e

    implicit def to(set: Set[NCTestElement]): util.Set[NCElement] = set.map(to).asJava
}