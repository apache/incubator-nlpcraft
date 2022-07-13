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
  * @param elementId Element.
  * @param propertyName Element's property name.
  */
case class MapperDesc(elementId: String, propertyName: String)

/**
  * Element extender.
  * For each 'main' dest element it tries to find related extra element and convert this pair to new complex element.
  * New element:
  * 1. Gets same ID as main element, also all main element properties copied into this new one.
  * 2. Gets tokens from both elements.
  * 3. Configured extra element property copied into new element's properties.
  *
  * Note that it is simple example implementation.
  * It just tries to unite nearest neighbours and doesn't check intermediate words, order correctness etc.
  */
object PizzeriaOrderMapper:
    extension(entity: NCEntity)
        def position: Double =
            val toks = entity.getTokens
            (toks.head.getIndex + toks.last.getIndex) / 2.0
        def tokens: List[NCToken] = entity.getTokens

    private def str(es: Iterable[NCEntity]): String =
        es.map(e => s"id=${e.getId}(${e.tokens.map(_.getIndex).mkString("[", ",", "]")})").mkString("{", ", ", "}")

    def apply(extra: MapperDesc, dests: MapperDesc*): PizzeriaOrderMapper = new PizzeriaOrderMapper(extra, dests)

import PizzeriaOrderMapper.*

case class PizzeriaOrderMapper(extra: MapperDesc, dests: Seq[MapperDesc]) extends NCEntityMapper with LazyLogging:
    override def map(req: NCRequest, cfg: NCModelConfig, ents: List[NCEntity]): List[NCEntity] =
        def map(destEnt: NCEntity, destProp: String, extraEnt: NCEntity): NCEntity =
            new NCPropertyMapAdapter with NCEntity:
                destEnt.keysSet.foreach(k => put(k, destEnt.get(k)))
                put[String](destProp, extraEnt.get[String](extra.propertyName).toLowerCase)
                override val getTokens: List[NCToken] = (destEnt.tokens ++ extraEnt.tokens).sortBy(_.getIndex)
                override val getRequestId: String = req.getRequestId
                override val getId: String = destEnt.getId

        val mainById = dests.map(p => p.elementId -> p).toMap
        val descEnts = mutable.HashSet.empty ++ ents.filter(e => mainById.contains(e.getId))
        val extraEnts = ents.filter(_.getId == extra.elementId)

        if descEnts.nonEmpty && extraEnts.nonEmpty && descEnts.size >= extraEnts.size then
            val used = (descEnts ++ extraEnts).toSet
            val main2Extra = mutable.HashMap.empty[NCEntity, NCEntity]

            for (e <- extraEnts)
                val m = descEnts.minBy(m => Math.abs(m.position - e.position))
                descEnts -= m
                main2Extra += m -> e

            val unrelated = ents.filter(e => !used.contains(e))
            val artificial = for ((m, e) <- main2Extra) yield map(m, mainById(m.getId).propertyName, e)
            val unused = descEnts

            val res = (unrelated ++ artificial ++ unused).sortBy(_.tokens.head.getIndex)

            logger.debug(s"Elements mapped [input=${str(ents)}, output=${str(res)}]")

            res
        else ents