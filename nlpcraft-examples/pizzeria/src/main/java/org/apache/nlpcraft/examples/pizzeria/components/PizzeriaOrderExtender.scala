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

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.NCResultType.ASK_DIALOG
import scala.collection.*

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
    extension(entity: NCEntity)
        def position: Double =
            val toks = entity.getTokens
            (toks.head.getIndex + toks.last.getIndex) / 2.0
        def tokens: List[NCToken] = entity.getTokens

import PizzeriaOrderExtender.*

/**
  *
  * @param mainDataSeq
  * @param extraData
  */
case class PizzeriaOrderExtender(mainDataSeq: Seq[EntityData], extraData: EntityData) extends NCEntityMapper with LazyLogging:
    override def map(req: NCRequest, cfg: NCModelConfig, ents: List[NCEntity]): List[NCEntity] =
        def combine(mainEnt: NCEntity, mainProp: String, extraEnt: NCEntity): NCEntity =
            new NCPropertyMapAdapter with NCEntity:
                mainEnt.keysSet.foreach(k => put(k, mainEnt.get(k)))
                put[String](mainProp, extraEnt.get[String](extraData.property).toLowerCase)
                override val getTokens: List[NCToken] = (mainEnt.tokens ++ extraEnt.tokens).sortBy(_.getIndex)
                override val getRequestId: String = req.getRequestId
                override val getId: String = mainEnt.getId

        val mainById = mainDataSeq.map(p => p.id -> p).toMap
        val main = mutable.HashSet.empty ++ ents.filter(e => mainById.contains(e.getId))
        val extra = ents.filter(_.getId == extraData.id)

        if main.nonEmpty && extra.nonEmpty && main.size >= extra.size then
            val used = (main ++ extra).toSet
            val main2Extra = mutable.HashMap.empty[NCEntity, NCEntity]

            for (e <- extra)
                val m = main.minBy(m => Math.abs(m.position - e.position))
                main -= m
                main2Extra += m -> e

            val unrelated = ents.filter(e => !used.contains(e))
            val artificial = for ((m, e) <- main2Extra) yield combine(m, mainById(m.getId).property, e)
            val unused = main

            val res = (unrelated ++ artificial ++ unused).sortBy(_.tokens.head.getIndex)

            def s(es: Iterable[NCEntity]) =
                ents.map(e => s"id=${e.getId}(${e.tokens.map(_.getIndex).mkString("[", ",", "]")})").mkString("{", ", ", "}")
            logger.debug(s"Elements mapped [input=${s(ents)}, output=${s(res)}]")

            res
        else ents