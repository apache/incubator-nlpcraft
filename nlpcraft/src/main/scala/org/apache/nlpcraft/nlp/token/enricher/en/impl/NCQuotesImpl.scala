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

package org.apache.nlpcraft.nlp.token.enricher.en.impl

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*

import java.util.List as JList
import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCQuotesImpl extends NCTokenEnricher with LazyLogging:
    private final val Q_POS: Set[String] = Set("``", "''")
    private def isQuote(t: NCToken): Boolean = Q_POS.contains(t.getPos)

    override def enrich(req: NCRequest, cfg: NCModelConfig, toksList: JList[NCToken]): Unit =
        val toks = toksList.asScala
        val quotes = toks.filter(isQuote)

        // Start and end quote can be different ("a` processed as valid)
        if quotes.nonEmpty && quotes.size % 2 == 0 then
            val m = toks.zipWithIndex.toMap
            val pairs = quotes.zipWithIndex.drop(1).flatMap { (t, idx) =>
                Option.when(idx % 2 != 0)(m(t) -> m(quotes(idx - 1)))
            }
            toks.zipWithIndex.foreach { (tok, idx) =>
                tok.put("quoted", pairs.exists { (from, to) => from > idx && to < idx })
            }
        else
            logger.warn(s"Detected invalid quotes in: ${req.getText}")