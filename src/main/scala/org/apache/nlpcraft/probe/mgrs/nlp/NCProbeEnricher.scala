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

package org.apache.nlpcraft.probe.mgrs.nlp

import java.io.Serializable

import com.typesafe.scalalogging.LazyLogging
import io.opencensus.trace.Span
import org.apache.nlpcraft.common.nlp._
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.probe.mgrs.NCModelDecorator

import scala.collection.{Map, Seq}
import scala.language.implicitConversions

/**
 * Base class for NLP enricher.
 */
abstract class NCProbeEnricher extends NCService with LazyLogging {
    /**
      *
      * @param buf
      * @param toks
      */
    protected def areSuitableTokens(buf: Seq[Set[NCNlpSentenceToken]], toks: Seq[NCNlpSentenceToken]): Boolean =
        !buf.exists(_.exists(toks.contains)) && toks.forall(t ⇒ !t.isQuoted && !t.isBracketed)

    /**
      *
      * @param toks
      * @param pred
      */
    protected def getCommonNotes(
        toks: Seq[NCNlpSentenceToken], pred: Option[NCNlpSentenceNote ⇒ Boolean] = None
    ): Set[String] =
        if (toks.isEmpty)
            Set.empty
        else {
            def getCommon(sortedToks: Seq[NCNlpSentenceToken]): Set[String] = {
                require(sortedToks.nonEmpty)

                val h = sortedToks.head
                val l = sortedToks.last

                val notes = pred match {
                    case Some(p) ⇒ h.filter(p)
                    case None ⇒ h.map(p ⇒ p)
                }

                notes.filter(!_.isNlp).filter(n ⇒ h.index == n.tokenFrom && l.index == n.tokenTo).map(_.noteType).toSet
            }

            var sortedToks = toks.sortBy(_.index)

            var res = getCommon(sortedToks)

            if (res.isEmpty) {
                sortedToks = sortedToks.filter(!_.isStopWord)

                if (sortedToks.nonEmpty)
                    res = getCommon(sortedToks)
            }

            if (res.isEmpty) Set.empty else res
        }

    /**
      *
      * Processes this NLP sentence.
      *
      * @param mdl Model decorator.
      * @param ns NLP sentence to enrich.
      * @param senMeta Sentence metadata.
      * @param parent Span parent.
      */
    @throws[NCE]
    def enrich(mdl: NCModelDecorator, ns: NCNlpSentence, senMeta: Map[String, Serializable], parent: Span): Unit
}