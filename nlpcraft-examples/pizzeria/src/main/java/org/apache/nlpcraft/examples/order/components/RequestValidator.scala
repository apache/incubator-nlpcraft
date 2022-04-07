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
class RequestValidator extends NCEntityValidator:
    override def validate(req: NCRequest, cfg: NCModelConfig, ents: util.List[NCEntity]): Unit =
        val es = ents.asScala

        def count(id: String): Int = es.count(_.getId == id)

        val cntPizza = count("ord:pizza")
        val cntDrink = count("ord:drink")
        val cntNums = count("stanford:number")
        val cntSize = count("ord:pizza:size")

        if cntNums != 1 && cntNums > cntPizza + cntDrink then
            throw new NCRejection("Too many unrecognized numerics in the request.")
        if cntSize != 1 && cntSize > cntPizza then
            throw new NCRejection("Too many unrecognized pizza sizes in the request.")