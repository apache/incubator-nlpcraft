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
  * Element description holder.
  *
  * @param elementType Element type.
  * @param propertyName Element's property name.
  */
case class PizzeriaOrderMapperDesc(elementType: String, propertyName: String)

/**
  * Element extender.
  *
  * For each 'main' element it tries to find related extra element and convert this pair to new complex element.
  * New element:
  * 1. Gets same type as main element, also all main element properties copied into this new one.
  * 2. Gets tokens from both elements.
  * 3. Configured extra element property copied into new element's properties.
  *
  * Note that it is simple example implementation.
  * It just tries to unite nearest neighbours and doesn't check intermediate words, order correctness etc.
  */
private object PizzeriaOrderMapper:
    extension(entity: NCEntity)
        private def position: Double =
            val toks = entity.getTokens
            (toks.head.getIndex + toks.last.getIndex) / 2.0
        private def tokens: List[NCToken] = entity.getTokens

    private def str(es: Iterable[NCEntity]): String =
        es.map(e => s"type=${e.getType}(${e.tokens.map(_.getIndex).mkString("[", ",", "]")})").mkString("{", ", ", "}")

    def apply(extra: PizzeriaOrderMapperDesc, descr: PizzeriaOrderMapperDesc*): PizzeriaOrderMapper = new PizzeriaOrderMapper(extra, descr)

import PizzeriaOrderMapper.*

/**
  * Custom [[NCEntityMapper]] implementation. It creates new [[NCEntity]] instances
  * based on `descr` elements extending them by `extra` element property.
  * If `descr` elements or `extra` element are not found or
  * `dests` and `extra` elements aren't located side by side in user input
  * then initial input [[NCEntity]] instances are passed as is.
  *
  * @param extra Extra data element description.
  * @param descr Base elements descriptions.
  */
case class PizzeriaOrderMapper(extra: PizzeriaOrderMapperDesc, descr: Seq[PizzeriaOrderMapperDesc]) extends NCEntityMapper with LazyLogging:
    /** @inheritdoc */
    override def map(req: NCRequest, cfg: NCModelConfig, ents: List[NCEntity]): List[NCEntity] =
        def map(destEnt: NCEntity, destProp: String, extraEnt: NCEntity): NCEntity =
            new NCPropertyMapAdapter with NCEntity:
                destEnt.keysSet.foreach(k => put(k, destEnt(k)))
                put[String](destProp, extraEnt[String](extra.propertyName).toLowerCase)
                override val getTokens: List[NCToken] = (destEnt.tokens ++ extraEnt.tokens).sortBy(_.getIndex)
                override val getRequestId: String = req.getRequestId
                override val getType: String = destEnt.getType

        val descrMap = descr.map(p => p.elementType -> p).toMap
        val destEnts = mutable.HashSet.empty ++ ents.filter(e => descrMap.contains(e.getType))
        val extraEnts = ents.filter(_.getType == extra.elementType)

        if destEnts.nonEmpty && extraEnts.nonEmpty && destEnts.size >= extraEnts.size then
            val used = (destEnts ++ extraEnts).toSet
            val dest2Extra = mutable.HashMap.empty[NCEntity, NCEntity]

            for (extraEnt <- extraEnts)
                val destEnt = destEnts.minBy(m => Math.abs(m.position - extraEnt.position))
                destEnts -= destEnt
                dest2Extra += destEnt -> extraEnt

            val unrelated = ents.filter(e => !used.contains(e))
            val artificial = for ((m, e) <- dest2Extra) yield map(m, descrMap(m.getType).propertyName, e)
            val unused = destEnts

            val res = (unrelated ++ artificial ++ unused).sortBy(_.tokens.head.getIndex)

            logger.debug(s"Elements mapped [input=${str(ents)}, output=${str(res)}]")

            res
        else ents