/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.nlp.enrichers

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*

import java.io.*
import scala.collection.mutable

/**
  * Brackets [[NCTokenEnricher enricher]].
  *
  * This enricher adds `brackets` boolean [[NCPropertyMap metadata]] property to the [[NCToken token]]
  * instance if the word it represents is enclosed in brackets. Supported brackets are: `()`, `{}`,
  * `[]` and `<>`.
  *
  * **NOTE:** invalid enclosed brackets are ignored.
  */
//noinspection DuplicatedCode,ScalaWeakerAccess
class NCBracketsTokenEnricher extends NCTokenEnricher with LazyLogging:
    override def enrich(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): Unit =
        val stack = new java.util.Stack[String]()
        val map = mutable.HashMap.empty[NCToken, Boolean]
        var ok = true

        def check(expected: String): Unit = if stack.empty() || stack.pop() != expected then ok = false
        def mark(t: NCToken): Unit = map += t -> !stack.isEmpty

        for (t <- toks if ok)
            t.getText match
                case "(" | "{" | "[" | "<" =>
                    mark(t)
                    stack.push(t.getText)
                case ")" =>
                    check("(")
                    mark(t)
                case "}" =>
                    check("{")
                    mark(t)
                case "]" =>
                    check("[")
                    mark(t)
                case ">" =>
                    check("<")
                    mark(t)
                case _ => mark(t)

        if ok && stack.isEmpty then map.foreach { (tok, b) => tok.put("brackets", b) }
        else logger.warn(s"Detected invalid brackets in: ${req.getText}")