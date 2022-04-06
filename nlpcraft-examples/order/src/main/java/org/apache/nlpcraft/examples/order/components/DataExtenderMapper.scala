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
case class DataExtenderMapper(key: String, prop: String, extKey: String, extProp: String) extends NCEntityMapper:
    private def extract(e: NCEntity): mutable.Seq[NCToken] = e.getTokens.asScala
    override def map(req: NCRequest, cfg: NCModelConfig, entities: util.List[NCEntity]): util.List[NCEntity] =
        var es = entities.asScala
        val data = es.filter(_.getId == key)
        val extData = es.filter(_.getId == extKey)

        if data.nonEmpty && data.size == extData.size then
            var ok = true
            val mapped =
                for ((e1, e2) <- data.zip(extData) if ok) yield
                    if e1.getId == e2.getId then
                        ok = false
                        null
                    else
                        val (data, extData) = if e1.getId == key then (e1, e2) else (e2, e1)
                        new NCPropertyMapAdapter with NCEntity:
                            data.keysSet().forEach(k => put(k, data.get(k)))
                            put[String](extProp, extData.get[String](extProp).toLowerCase)
                            override val getTokens: JList[NCToken] = (extract(data) ++ extract(extData)).sortBy(_.getIndex).asJava
                            override val getRequestId: String = req.getRequestId
                            override val getId: String = data.getId

            es = es --= data
            es = es --= extData
            (es ++ mapped).sortBy(extract(_).head.getIndex).asJava
        else entities