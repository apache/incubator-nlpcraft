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

package org.apache.nlpcraft.examples.order.components

import org.apache.nlpcraft.*

import java.util
import java.util.List as JList
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/**
  *
  */
class PizzaSizeExtender extends NCEntityMapper:
    private def extract(e: NCEntity): mutable.Seq[NCToken] = e.getTokens.asScala
    override def map(req: NCRequest, cfg: NCModelConfig, entities: util.List[NCEntity]): util.List[NCEntity] =
        var es = entities.asScala
        val pizzas = es.filter(_.getId == "ord:pizza")
        val sizes = es.filter(_.getId == "ord:pizza:size")

        if pizzas.nonEmpty && sizes.nonEmpty then
            if pizzas.size != sizes.size then throw new NCRejection("Pizza and their sizes should be defined together1")
            var ok = true
            val mapped =
                for ((e1, e2) <- pizzas.zip(sizes) if ok) yield
                    if e1.getId == e2.getId then
                        ok = false
                        null
                    else
                        val (pizza, size) = if e1.getId == "ord:pizza" then (e1, e2) else (e2, e1)
                        new NCPropertyMapAdapter with NCEntity:
                            // Copy from pizza.
                            size.keysSet().forEach(k => put(k, size.get(k)))
                            // New value from size.
                            put[String]("ord:pizza:size", size.get[String]("ord:pizza:size:value").toLowerCase)

                            override val getTokens: JList[NCToken] = (extract(pizza) ++ extract(size)).sortBy(_.getIndex).asJava
                            override val getRequestId: String = req.getRequestId
                            override val getId: String = pizza.getId

            es = es --= pizzas
            es = es --= sizes
            (es ++ mapped).sortBy(extract(_).head.getIndex).asJava
        else entities