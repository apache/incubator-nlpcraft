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

package org.apache.nlpcraft

import org.apache.nlpcraft.model.{NCElement, NCValue}

import java.{lang, util}
import java.util.Optional
import scala.jdk.CollectionConverters.{SeqHasAsJava, SetHasAsJava}
import scala.language.implicitConversions

/**
  * Simple test element.
  */
case class NCTestElement(id: String, syns: String*) extends NCElement {
    private val values = new util.ArrayList[NCValue]

    var metadata: util.Map[String, AnyRef] = super.getMetadata
    var description: String = super.getDescription
    var parentId: String = super.getParentId
    var permutateSynonyms: Optional[lang.Boolean] = super.isPermutateSynonyms
    var sparse: Optional[lang.Boolean] = super.isSparse
    var greedy: Optional[lang.Boolean] = super.isGreedy
    var groups: Seq[String] = Seq(id)

    override def getId: String = id
    override def getSynonyms: util.List[String] = (syns :+ id).asJava
    override def getValues: util.List[NCValue] = values
    override def getGroups: util.List[String] = groups.asJava

    override def getMetadata: util.Map[String, AnyRef] = metadata
    override def getDescription: String = description
    override def getParentId: String = parentId
    override def isPermutateSynonyms: Optional[lang.Boolean] = permutateSynonyms
    override def isSparse: Optional[lang.Boolean] = sparse
    override def isGreedy: Optional[lang.Boolean] = greedy
}

/**
  * Simple test element helper.
  */
object NCTestElement {
    private def to(e: NCTestElement): NCElement = e

    implicit def to(set: Set[NCTestElement]): util.Set[NCElement] = set.map(to).asJava

    def apply(id: String, syns: Seq[String], vals: Map[String, Seq[String]]): NCTestElement = {
        val e = NCTestElement(id, syns :_*)

        e.getValues.addAll(
            vals.map { case (value, syns) =>
                new NCValue {
                    override def getName: String = value
                    override def getSynonyms: util.List[String] = syns.asJava
                }
            }.toSeq.asJava
        )

        e
    }
}