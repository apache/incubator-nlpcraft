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
import org.apache.nlpcraft.NCResultType.ASK_DIALOG

/**
  *
  * @param id
  * @param property
  */
case class EntityData(id: String, property: String)

/**
  * Element extender.
  * For each 'main' element it tries to find related extra element and convert this pair to new complex element.
  * New element:
  * 1. Gets same ID as main element, also all main element properties copied into this new one.
  * 2. Gets tokens from both elements.
  * 3. Configured extra element property copied into new element's properties.
  *
  * Note that it is simple example implementation.
  * It just tries to unite nearest neighbours and doesn't check intermediate words, order correctness etc.
  */
object PizzeriaOrderExtender:
    case class EntityHolder(entity: NCEntity):
        lazy val position: Double =
            val toks = entity.getTokens.asScala
            (toks.head.getIndex + toks.last.getIndex) / 2.0
    private def extract(e: NCEntity): mutable.Seq[NCToken] = e.getTokens.asScala

import PizzeriaOrderExtender.*

/**
  *
  * @param mainDataSeq
  * @param extraData
  */
case class PizzeriaOrderExtender(mainDataSeq: Seq[EntityData], extraData: EntityData) extends NCEntityMapper with LazyLogging:
    override def map(req: NCRequest, cfg: NCModelConfig, entities: JList[NCEntity]): JList[NCEntity] =
        def combine(mainEnt: NCEntity, mainProp: String, extraEnt: NCEntity): NCEntity =
            new NCPropertyMapAdapter with NCEntity:
                mainEnt.keysSet().forEach(k => put(k, mainEnt.get(k)))
                put[String](mainProp, extraEnt.get[String](extraData.property).toLowerCase)
                override val getTokens: JList[NCToken] = (extract(mainEnt) ++ extract(extraEnt)).sortBy(_.getIndex).asJava
                override val getRequestId: String = req.getRequestId
                override val getId: String = mainEnt.getId

        val es = entities.asScala
        val mainById = mainDataSeq.map(p => p.id -> p).toMap
        val main = mutable.HashSet.empty ++ es.filter(e => mainById.contains(e.getId)).map(p => EntityHolder(p))
        val extra = es.filter(_.getId == extraData.id).map(p => EntityHolder(p))

        if main.nonEmpty && extra.nonEmpty && main.size >= extra.size then
            val used = (main.map(_.entity) ++ extra.map(_.entity)).toSet
            val main2Extra = mutable.HashMap.empty[NCEntity, NCEntity]

            for (e <- extra)
                val m = main.minBy(m => Math.abs(m.position - e.position))
                main -= m
                main2Extra += m.entity -> e.entity

            val unrelatedEs = es.filter(e => !used.contains(e))
            val artificialEs = for ((m, e) <- main2Extra) yield combine(m, mainById(m.getId).property, e)
            val unused = main.map(_.entity)

            val res = (unrelatedEs ++ artificialEs ++ unused).sortBy(extract(_).head.getIndex)

            def str(es: mutable.Buffer[NCEntity]) =
                es.map(e => s"id=${e.getId}(${extract(e).map(_.getIndex).mkString("[", ",", "]")})").mkString("{", ", ", "}")
            logger.debug(s"Elements mapped [input=${str(es)}, output=${str(res)}]")

            res.asJava
        else entities