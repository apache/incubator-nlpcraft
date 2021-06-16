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

package org.apache.nlpcraft.server.nlp.enrichers.ctxword

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.nlp.NCNlpSentence
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher

/**
  * ContextWord enricher.
  */
object ContextWordEnricher extends NCServerEnricher {
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()
        ackStarted()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()
        ackStopped()
    }

    override def enrich(ns: NCNlpSentence, parent: Span): Unit = {
        ns.mlConfig match {
            case Some(cfg) =>
                val nouns = ns.tokens.filter(_.pos.startsWith("N"))

                if (nouns.nonEmpty) {
                    nouns
                }

            case None => // No-op.
        }
    }
}
