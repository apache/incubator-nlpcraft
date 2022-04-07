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

/**
  *
  * @param id
  * @param property
  */
case class EntityData(id: String, property: String)

/**
  * Simple element extender.
  * For each 'main' element it tries to find extra element. If it cane done instead of this pair new element created.
  * This new element has 'main' element ID. Extra element configured property copied to this new element.
  *
  * Note that for simplified implementation, it doesn't care about words between pairs elements and pairs,
  * also it doesn't check correctness of words order.
  */
case class ElementExtender(mainDataSeq: Seq[EntityData], extraData: EntityData) extends NCEntityMapper:
    private def getToks(e: NCEntity): mutable.Seq[NCToken] = e.getTokens.asScala
    override def map(req: NCRequest, cfg: NCModelConfig, entities: util.List[NCEntity]): util.List[NCEntity] =
        val mainDataMap = mainDataSeq.map(p => p.id -> p).toMap

        var es = entities.asScala
        val main = es.filter(e => mainDataMap.contains(e.getId))
        val extra = es.filter(_.getId == extraData.id)

        if main.nonEmpty && main.size >= extra.size then
            var ok = true
            val mapped =
                for ((e1, e2) <- main.zip(extra) if ok) yield
                    if e1.getId == e2.getId then
                        ok = false
                        null
                    else
                        val (mEnt, eEnt) = if mainDataMap.contains(e1.getId) then (e1, e2) else (e2, e1)
                        new NCPropertyMapAdapter with NCEntity:
                            mEnt.keysSet().forEach(k => put(k, mEnt.get(k)))
                            put[String](mainDataMap(mEnt.getId).property, eEnt.get[String](extraData.property).toLowerCase)
                            override val getTokens: JList[NCToken] = (getToks(mEnt) ++ getToks(eEnt)).sortBy(_.getIndex).asJava
                            override val getRequestId: String = req.getRequestId
                            override val getId: String = mEnt.getId

            es = es --= main.take(mapped.size)
            es = es --= extra.take(mapped.size)
            (es ++ mapped).sortBy(getToks(_).head.getIndex).asJava
        else entities