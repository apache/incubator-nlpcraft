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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.stopword

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceToken}
import org.apache.nlpcraft.common.{NCE, NCService, U}
import org.apache.nlpcraft.probe.mgrs.NCProbeModel
import org.apache.nlpcraft.probe.mgrs.nlp.NCProbeEnricher

import java.io.Serializable
import scala.annotation.tailrec

/**
  * Stop words enricher.
  */
object NCStopWordEnricher extends NCProbeEnricher {
    private final val POSES: Seq[String] = Seq("DT", "PRP", "PRP$", "WDT", "WP", "WP$", "WRB", "TO", "IN")

    private final val GEO_TYPES = Set(
        "nlpcraft:continent",
        "nlpcraft:subcontinent",
        "nlpcraft:country",
        "nlpcraft:metro",
        "nlpcraft:region",
        "nlpcraft:city"
    )

    @volatile private var geoPreWords: Seq[Seq[String]] = _
    @volatile private var geoKindStops: Map[String, Seq[String]] = _
    @volatile private var numPrefixStops:Seq[String] = _

    /**
     *
     * @param parent Optional parent span.
     * @return
     */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        // NOTE: stemmatization is done already by generator.
        geoPreWords = U.readTextResource(s"context/geo_pre_words.txt", "UTF-8", logger).
            map(_.split(" ").toSeq).sortBy(-_.size)

        geoKindStops = Map(
            "nlpcraft:city" -> Seq("city", "town"),
            "nlpcraft:country" -> Seq("country", "land", "countryside", "area", "territory"),
            "nlpcraft:region" -> Seq("region", "area", "state", "county", "district", "ground", "territory"),
            "nlpcraft:continent" -> Seq("continent", "land", "area")
        ).map(p => p._1 -> p._2.map(NCNlpCoreManager.stem))

        numPrefixStops = Seq(
            "is",
            "was",
            "were",
            "are",
            "with value",
            "might be",
            "would be",
            "has value",
            "can be",
            "should be",
            "must be"
        ).map(NCNlpCoreManager.stem)

        ackStarted()
    }

    /**
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        geoPreWords = null
        geoKindStops = null
        numPrefixStops = null

        ackStopped()
    }

    /**
      * Marks tokens as stopwords before token with given type if these tokens are suitable by given predicate.
      *
      * @param sen Sentence.
      * @param noteType Note type.
      */
    private def markBefore(sen: NCNlpSentence, noteType: String): Unit =
        for (note <- sen.getNotes(noteType) if note.tokenFrom > 0)
            sen.
                take(note.tokenFrom).
                reverse.
                takeWhile(t => t.isStopWord || t.isNlp && POSES.contains(t.pos)).
                filter(!_.isStopWord).foreach(_.addStopReason(note))

    /**
      * Processes geo tokens. Sets additional stopwords.
      *
      * @param ns Sentence.
      */
    private def processGeo(ns: NCNlpSentence): Unit = {

        // 1. Marks some specific words before GEO (like 'origin for London')
        for (note <- GEO_TYPES.flatMap(ns.getNotes)) {
            val toks = ns.
                take(note.tokenFrom).
                reverse.
                takeWhile(t => t.isStopWord || t.isNlp).
                reverse

            if (toks.nonEmpty) {
                val stems = toks.map(_.stem)

                geoPreWords.find(stems.endsWith) match {
                    case Some(words) => toks.reverse.take(words.size).filter(!_.isStopWord).foreach(_.addStopReason(note))
                    case None => // No-op.
                }
            }
        }

        // 2. Marks some specific words before and after GEO (like 'city London')
        geoKindStops.foreach { case (typ, stops) =>
            for (geoNote <- ns.getNotes(typ)) {
                def process(toks: Seq[NCNlpSentenceToken]): Unit =
                    toks.find(!_.isStopWord) match {
                        case Some(t) => if (stops.contains(t.stem)) t.addStopReason(geoNote)
                        case None => // No-op.
                    }

                process(ns.filter(_.index > geoNote.tokenTo))
                process(ns.filter(_.index < geoNote.tokenFrom).reverse)
            }
        }

        // 3. Marks stop-words like prepositions before.
        GEO_TYPES.foreach(t => markBefore(ns, t))
    }

    /**
      * Processes nums tokens. Sets additional stopwords.
      *
      * @param ns Sentence.
      */
    private def processNums(ns: NCNlpSentence): Unit =
        // Try to find words from configured list before numeric condition and mark them as STOP words.
        ns.getNotes("nlpcraft:num").foreach(numNote => {
            val before = ns.filter(_.index < numNote.tokenFrom)

            before.indices.map(i => before.drop(i)).find(
                seq => seq.forall(
                    t =>
                        t.isStopWord ||
                        (!t.isBracketed && !t.isQuoted)) &&
                    numPrefixStops.contains(seq.filter(!_.isStopWord).map(_.stem).mkString(" "))
            ) match {
                case Some(seq) => seq.filter(!_.isStopWord).foreach(_.addStopReason(numNote))
                case None => // No-op.
            }
        })

    /**
      * Processes dates. Sets additional stopwords.
      *
      * @param ns Sentence.
      */
    private def processDate(ns: NCNlpSentence): Unit = markBefore(ns, "nlpcraft:date")

    /**
      * Marks as stopwords, words with POS from configured list, which also placed before another stop words.
      */
    private def processCommonStops(mdl: NCProbeModel, ns: NCNlpSentence): Unit = {
        /**
          * Marks as stopwords, words with POS from configured list, which also placed before another stop words.
          */
        @tailrec
        def processCommonStops0(mdl: NCProbeModel, ns: NCNlpSentence): Unit = {
            val max = ns.size - 1
            var stop = true

            for (
                (tok, idx) <- ns.zipWithIndex
                if idx != max &&
                    !tok.isStopWord &&
                    !mdl.exclStopWordsStems.contains(tok.stem) &&
                    POSES.contains(tok.pos) &&
                    ns(idx + 1).isStopWord
            ) {
                ns.fixNote(tok.getNlpNote, "stopWord" -> true)

                stop = false
            }

            if (!stop)
                processCommonStops0(mdl, ns)
        }

        processCommonStops0(mdl, ns)
    }

    @throws[NCE]
    override def enrich(mdl: NCProbeModel, ns: NCNlpSentence, senMeta: Map[String, Serializable], parent: Span = null): Unit = {
        require(isStarted)

        def mark(stems: Set[String], f: Boolean): Unit =
            ns.filter(t => stems.contains(t.stem)).foreach(t => ns.fixNote(t.getNlpNote, "stopWord" -> f))

        startScopedSpan(
            "enrich", parent, "srvReqId" -> ns.srvReqId, "mdlId" -> mdl.model.getId, "txt" -> ns.text
        ) { _ =>
            if (mdl.model.isStopWordsAllowed) {
                mark(mdl.exclStopWordsStems, f = false)
                mark(mdl.addStopWordsStems, f = true)

                // If stop word swallowed by any built token (numeric, date etc) - it's stop word marking dropped.
                ns.filter(t => t.isStopWord && !t.isNlp).foreach(t => ns.fixNote(t.getNlpNote, "stopWord" -> false))

                processGeo(ns)
                processDate(ns)
                processNums(ns)
                processCommonStops(mdl, ns)
            }
            else
                ns.filter(_.isStopWord).foreach(t => ns.fixNote(t.getNlpNote, "stopWord" -> false))
        }
    }
}
