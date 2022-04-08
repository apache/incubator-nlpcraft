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

package org.apache.nlpcraft.examples.pizzeria.components

import org.apache.nlpcraft.*

import java.util
import java.util.List as JList
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import com.typesafe.scalalogging.LazyLogging

/**
  *
  * @param id
  * @param property
  */
case class EntityData(id: String, property: String)

/**
  * Element extender.
  * For each 'main' element it tries to find related extra element and to make new complex element instead of this pair.
  * This new element has:
  * 1. Same ID as main element, also all main element properties copied into this new element's properties.
  * 2. Tokens from both elements.
  * 3. Configured property from extra element copied into new element's properties.
  *
  * Note that it is simple example implementation.
  * It just tries to unite nearest neighbours and doesn't check intermediate words, order correctness etc.
  */
object ElementExtender:
    case class EntityHolder(element: NCEntity):
        lazy val position: Double =
            val toks = element.getTokens.asScala
            (toks.head.getIndex + toks.last.getIndex) / 2.0

    private def toTokens(e: NCEntity): mutable.Seq[NCToken] = e.getTokens.asScala

import ElementExtender.*

/**
  *
  * @param mainSeq
  * @param extra
  */
case class ElementExtender(mainSeq: Seq[EntityData], extra: EntityData) extends NCEntityMapper with LazyLogging:
    override def map(req: NCRequest, cfg: NCModelConfig, entities: JList[NCEntity]): JList[NCEntity] =
        def combine(m: NCEntity, mProp: String, e: NCEntity): NCEntity =
            new NCPropertyMapAdapter with NCEntity:
                m.keysSet().forEach(k => put(k, m.get(k)))
                put[String](mProp, e.get[String](extra.property).toLowerCase)
                override val getTokens: JList[NCToken] = (toTokens(m) ++ toTokens(e)).sortBy(_.getIndex).asJava
                override val getRequestId: String = req.getRequestId
                override val getId: String = m.getId

        val es = entities.asScala
        val mainById = mainSeq.map(p => p.id -> p).toMap
        val mainHs = mutable.HashSet.empty ++ es.filter(e => mainById.contains(e.getId)).map(p => EntityHolder(p))
        val extraHs = es.filter(_.getId == extra.id).map(p => EntityHolder(p))

        if mainHs.nonEmpty && mainHs.size >= extraHs.size then
            val main2Extra = mutable.HashMap.empty[NCEntity, NCEntity]

            for (e <- extraHs)
                val m = mainHs.minBy(m => Math.abs(m.position - e.position))
                mainHs -= m
                main2Extra += m.element -> e.element

            val newEs = for ((m, e) <- main2Extra) yield combine(m, mainById(m.getId).property, e)
            val used = (mainHs.map(_.element) ++ extraHs.map(_.element)).toSet

            (es.filter(e => !used.contains(e)) ++ mainHs.map(_.element) ++ newEs).sortBy(toTokens(_).head.getIndex).asJava
        else entities