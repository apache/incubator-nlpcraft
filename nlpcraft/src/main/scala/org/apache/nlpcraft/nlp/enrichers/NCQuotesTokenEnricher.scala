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
import scala.collection.*

/**
  * Companion helper.
  */
object NCQuotesTokenEnricher:
    private case class Range(from: Int, to: Int):
        def in(idx: Int): Boolean = idx >= from && idx <= to

    private val QUOTES = Map("«" -> "»", "\"" -> "\"", "`" -> "`", "'" -> "'")

    private val QUOTES_REVERSED = QUOTES.map { case (key, value) => value -> key }
    private val QUOTES_SYMBOLS = QUOTES.flatMap { case (key, value) => Set(key, value) }.toSet

    private def isQuote(t: NCToken): Boolean = QUOTES_SYMBOLS.contains(t.getText)

import NCQuotesTokenEnricher.*

/**
  * Quotes [[NCTokenEnricher token enricher]].
  *
  * This enricher adds `quoted` boolean [[NCPropertyMap metadata]] property to the [[NCToken token]]
  * instance if word it represents is in quotes. The value `true` of the metadata property indicates that this word is in quotes,
  * `false` value indicates otherwise.
  *
  * Supported quotes are: **«**, **»**, **"**, **'**, **&#96;**.
  * For any invalid situations, like unexpected quotes count or their invalid order detection, for all tokens
  * property `quoted` value assigned as `false`.
  */
//noinspection ScalaWeakerAccess
class NCQuotesTokenEnricher extends NCTokenEnricher with LazyLogging:
    //noinspection DuplicatedCode
    /** @inheritdoc */
    override def enrich(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): Unit =
        def mark(get: NCToken => Boolean): Unit = for (t <- toks) t.put("quoted", get(t))
        def markFalse(invalid: Boolean): Unit =
            if invalid then logger.warn(s"Detected invalid quotes in: ${req.getText}")
            mark(_ => false)

        val quotes = toks.filter(isQuote)

        if quotes.isEmpty then
            markFalse(false)
        else if quotes.length % 2 != 0 then
            markFalse(true)
        else
            val ranges = mutable.HashSet.empty[Range]
            val stack = mutable.Stack.empty[NCToken]

            for (q <- quotes)
                if stack.nonEmpty then
                    val top = stack.top
                    if top.getText == QUOTES_REVERSED.getOrElse(q.getText, null) then
                        ranges += Range(top.getIndex + 1, q.getIndex - 1)
                        stack.pop()
                    else
                        stack.push(q)
                else
                    stack.push(q)

            if stack.isEmpty then
                mark(t => ranges.exists(_.in(t.getIndex)))
            else
                markFalse(true)