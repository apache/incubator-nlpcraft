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
import scala.jdk.CollectionConverters.*

/**
  *
  */
class OrderValidator extends NCEntityValidator:
    override def validate(req: NCRequest, cfg: NCModelConfig, ents: util.List[NCEntity]): Unit =
        val es = ents.asScala

        if !es.exists(_.getId == "ord:pizza") then
            if es.count(_.getId == "stanford:number") > 1 then throw new NCRejection("Error1") // TODO:
            if es.count(_.getId == "ord:pizza:size") > 1 then throw new NCRejection("Error2") // TODO: