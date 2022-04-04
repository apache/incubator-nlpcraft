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
  * @param id1
  * @param id2
  * @param newId
  */
case class SimpleCombiner(id1: String, id2: String, newId: String) extends NCEntityMapper:
    private def extract(e: NCEntity): mutable.Seq[NCToken] = e.getTokens.asScala
    override def map(req: NCRequest, cfg: NCModelConfig, entities: util.List[NCEntity]): util.List[NCEntity] =
        var es = entities.asScala
        val es1 = es.filter(_.getId == id1)
        val es2 = es.filter(_.getId == id2)

        if es1.nonEmpty && es2.size == es1.size then
            var ok = true

            val mapped =
                for ((e1, e2) <- es1.zip(es2) if ok) yield
                    if e1.getId == e2.getId then
                        ok = false
                        null
                    else
                        new NCPropertyMapAdapter with NCEntity:
                            override val getTokens: JList[NCToken] = (extract(e1) ++ extract(e2)).sortBy(_.getIndex).asJava
                            override val getRequestId: String = req.getRequestId
                            override val getId: String = newId

            if ok then
                es = es --= es1
                es = es --= es2
                (es ++ mapped).sortBy(extract(_).head.getIndex).asJava
            else
                entities
        else
            entities


