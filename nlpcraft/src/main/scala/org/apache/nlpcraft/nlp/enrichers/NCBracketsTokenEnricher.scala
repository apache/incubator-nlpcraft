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
import scala.collection.{Map, mutable}

/**
  * Companion helper.
  */
object NCBracketsTokenEnricher:
    private val BRACKETS = Map("(" -> ")", "{" -> "}", "[" -> "]", "<" -> ">")
    private val BRACKETS_REVERSED = BRACKETS.map { case (key, value) => value -> key }

import NCBracketsTokenEnricher.*

/**
  * Brackets [[NCTokenEnricher token enricher]].
  *
  * This enricher adds `brackets` boolean [[NCPropertyMap metadata]] property to the [[NCToken token]]
  * instance if the word it represents is enclosed in brackets.
  *
  * Supported brackets are: `()`, `{}`, `[]` and `<>`.
  *
  * **NOTE:** invalid enclosed brackets are ignored and for all input tokens property `brackets` assigned as `false`.
  */
//noinspection DuplicatedCode,ScalaWeakerAccess
class NCBracketsTokenEnricher extends NCTokenEnricher with LazyLogging:
    /** @inheritdoc */
    override def enrich(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): Unit =
        val stack = new java.util.Stack[String]()
        val map = mutable.HashMap.empty[NCToken, Boolean]
        var ok = true

        def check(expected: String): Unit = if stack.empty() || stack.pop() != expected then ok = false
        def add(t: NCToken): Unit = map += t -> !stack.isEmpty

        for (t <- toks if ok; txt = t.getText)
            if BRACKETS.contains(txt) then
                add(t)
                stack.push(txt)
            else if BRACKETS_REVERSED.contains(txt) then
                check(BRACKETS_REVERSED(txt))
                add(t)
            else
                add(t)

        if ok && stack.isEmpty then
            map.foreach { (tok, b) => tok.put("brackets", b) }
        else
            toks.foreach(_.put("brackets",false))
            logger.warn(s"Detected invalid brackets in: ${req.getText}")