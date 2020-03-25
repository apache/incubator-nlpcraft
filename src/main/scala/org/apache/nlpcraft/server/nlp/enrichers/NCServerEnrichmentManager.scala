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

package org.apache.nlpcraft.server.nlp.enrichers

import io.opencensus.trace.Span
import org.apache.ignite.IgniteCache
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote, NCNlpSentenceToken}
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.server.ignite.NCIgniteHelpers._
import org.apache.nlpcraft.server.ignite.NCIgniteInstance
import org.apache.nlpcraft.server.nlp.core.{NCNlpNerEnricher, NCNlpServerManager}
import org.apache.nlpcraft.server.nlp.enrichers.basenlp.NCBaseNlpEnricher
import org.apache.nlpcraft.server.nlp.enrichers.coordinate.NCCoordinatesEnricher
import org.apache.nlpcraft.server.nlp.enrichers.date.NCDateEnricher
import org.apache.nlpcraft.server.nlp.enrichers.geo.NCGeoEnricher
import org.apache.nlpcraft.server.nlp.enrichers.numeric.NCNumericEnricher
import org.apache.nlpcraft.server.nlp.enrichers.quote.NCQuoteEnricher
import org.apache.nlpcraft.server.nlp.enrichers.stopword.NCStopWordEnricher
import org.apache.nlpcraft.server.nlp.preproc.NCPreProcessManager

import scala.collection.Seq
import scala.util.control.Exception.catching

/**
  * Server enrichment pipeline manager.
  */
object NCServerEnrichmentManager extends NCService with NCIgniteInstance {
    private object Config extends NCConfigurable {
        def supportNlpCraft: Boolean = getStringList("nlpcraft.server.tokenProviders").contains("nlpcraft")
    }

    private final val CUSTOM_PREFIXES = Set("google:", "opennlp:", "stanford:", "spacy:")

    @volatile private var ners: Map[String, NCNlpNerEnricher] = _
    @volatile private var supportedProviders: Set[String] = _

    // NOTE: this cache is independent from datasource.
    @volatile private var cache: IgniteCache[String, Holder] = _
    
    private val HEADERS: Map[String, (Int, Seq[String])] =
        Seq(
            "nlpcraft:nlp" → Seq("origText", "index", "pos", "lemma", "stem", "bracketed", "quoted", "stopWord", "ne", "nne"),
            "nlpcraft:continent" → Seq("continent"),
            "nlpcraft:subcontinent" → Seq("continent", "subcontinent"),
            "nlpcraft:country" → Seq("country"),
            "nlpcraft:region" → Seq("country", "region"),
            "nlpcraft:city" → Seq("city", "region"),
            "nlpcraft:metro" → Seq("metro"),
            "nlpcraft:date" → Seq("from", "to", "periods"),
            "nlpcraft:num" → Seq("from", "to", "unit", "unitType"),
            "nlpcraft:coordinate" → Seq("latitude", "longitude"),
            "google:" → Seq("meta", "salience"),
            "stanford:" → Seq("confidence", "nne"),
            "opennlp:" → Seq("probability"),
            "spacy:" → Seq("vector", "sentiment", "meta")
        ).zipWithIndex.map { case ((typ, seq), idx) ⇒ typ → (idx, seq) }.toMap

    private val GEO = Set(
        "nlpcraft:continent",
        "nlpcraft:subcontinent",
        "nlpcraft:country",
        "nlpcraft:metro",
        "nlpcraft:region",
        "nlpcraft:city"
    )

    case class Holder(sentence: NCNlpSentence, enabledBuiltInTokens: Set[String])

    /**
      *
      * @param srvReqId Server request ID.
      * @param normTxt Normalized text.
      * @param enabledBuiltInToks Enabled built-in tokens.
      * @param parent Optional parent span.
      * @return
      */
    private def process(
        srvReqId: String,
        normTxt: String,
        enabledBuiltInToks: Set[String],
        parent: Span = null): NCNlpSentence =
        startScopedSpan("process", parent, "srvReqId" → srvReqId, "txt" → normTxt) { span ⇒
            val s = new NCNlpSentence(srvReqId, normTxt, 1, enabledBuiltInToks)

            // Server-side enrichment pipeline.
            // NOTE: order of enrichers is IMPORTANT.
            NCBaseNlpEnricher.enrich(s, span)
            NCQuoteEnricher.enrich(s, span)
            NCStopWordEnricher.enrich(s, span)

            if (Config.supportNlpCraft) {
                if (enabledBuiltInToks.contains("nlpcraft:date"))
                    NCDateEnricher.enrich(s, span)

                if (enabledBuiltInToks.contains("nlpcraft:num"))
                    NCNumericEnricher.enrich(s, span)

                if (enabledBuiltInToks.exists(GEO.contains))
                    NCGeoEnricher.enrich(s, span)

                if (enabledBuiltInToks.contains("nlpcraft:coordinate"))
                    NCCoordinatesEnricher.enrich(s, span)
            }

            ner(s, enabledBuiltInToks)

            prepareAsciiTable(s).info(logger, Some(s"Sentence enriched: $normTxt"))

            cache += normTxt → Holder(s, enabledBuiltInToks)

            s
        }

    /**
      * @param srvReqId Server request ID.
      * @param txt Input text.
      * @param enabledBuiltInToks Set of enabled built-in token IDs.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def enrichPipeline(
        srvReqId: String,
        txt: String,
        enabledBuiltInToks: Set[String],
        parent: Span = null): NCNlpSentence = {
        startScopedSpan("enrichPipeline", parent, "srvReqId" → srvReqId, "txt" → txt) { span ⇒
            val normTxt = NCPreProcessManager.normalize(txt, true, span)

            if (normTxt != txt)
                logger.info(s"Sentence normalized to: $normTxt")

            val normEnabledBuiltInToks = enabledBuiltInToks.map(_.toLowerCase)

            catching(wrapIE) {
                cache(normTxt) match {
                    case Some(h) ⇒
                        if (h.enabledBuiltInTokens == normEnabledBuiltInToks) {
                            prepareAsciiTable(h.sentence).info(logger, Some(s"Sentence enriched (from cache): $normTxt"))

                            h.sentence
                        }
                        else
                            process(srvReqId, normTxt, enabledBuiltInToks, span)
                    case None ⇒
                        process(srvReqId, normTxt, enabledBuiltInToks, span)
                }
            }
        }
    }
    
    /**
      *
      * @param s NLP sentence to ASCII print.
      * @return
      */
    private def prepareAsciiTable(s: NCNlpSentence): NCAsciiTable = {
        case class Header(header: String, noteType: String, noteName: String)

        def isType(typ: String, s: String*): Boolean = s.exists(typ.startsWith)

        def mkNoteHeaders(n: NCNlpSentenceNote): scala.collection.Set[Header] = {
            val typ = n.noteType
            val prefix = typ.substring(typ.indexOf(':') + 1) // Remove 'nlpcraft:' prefix.

            n.keySet
                .filter(name ⇒ HEADERS.find(h ⇒ isType(typ, h._1)) match {
                    case Some((_, (_, names))) ⇒ names.contains(name)
                    case None ⇒ false
                })
                .map(name ⇒
                    Header(
                        if (isType(typ, "google:", "opennlp:", "stanford:", "spacy:"))
                            s"$typ:$name"
                        else
                            s"$prefix:$name",
                        typ,
                        name
                    )
                )
        }
        
        val headers = s.flatten.flatMap(mkNoteHeaders).distinct.sortBy(hdr ⇒ {
            val x = HEADERS.
                find(p ⇒ isType(hdr.noteType, p._1)).
                getOrElse(throw new NCE(s"Header not found for: ${hdr.noteType}"))._2
            
            (x._1 * 100) + x._2.indexOf(hdr.noteName)
        })

        val tbl = NCAsciiTable(headers.map(_.header): _*)
        
        def mkNoteValue(tok: NCNlpSentenceToken, hdr: Header): Seq[String] =
            tok.getNotes(hdr.noteType).filter(_.contains(hdr.noteName)).map(_(hdr.noteName).toString()).toSeq

        for (tok ← s)
            tbl += (headers.map(mkNoteValue(tok, _)): _*)

        tbl
    }

    /**
      * Enriches sentence by NER.
      *
      * @param ns Sentence.
      * @param enabledBuiltInToks Enabled built-in tokens.
      */
    private def ner(ns: NCNlpSentence, enabledBuiltInToks: Set[String]): Unit = {
        enabledBuiltInToks.flatMap(t ⇒
            t match {
                case x if x.startsWith("nlpcraft:") ⇒ None

                case x if CUSTOM_PREFIXES.exists(x.startsWith) ⇒
                    val typ = x.takeWhile(_ != ':')

                    Some(t.drop(typ.length + 1) → typ)

                case _ ⇒ throw new NCE(s"Unexpected token: $t")
            }
        ).
            groupBy { case (_, typ) ⇒ typ }.
            map { case (typ, seq) ⇒ typ → seq.map { case (tok, _ ) ⇒ tok } }.
            foreach { case (typ, toks) ⇒ ners(typ).enrich(ns, toks) }
    }

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span ⇒
        catching(wrapIE) {
            cache = ignite.cache[String, Holder]("sentence-cache")
        }
        
        NCBaseNlpEnricher.start(span)
        NCStopWordEnricher.start(span)
        NCQuoteEnricher.start(span)

        if (Config.supportNlpCraft) {
            // These component can be started independently.
            U.executeParallel(
                () ⇒ NCDateEnricher.start(span),
                () ⇒ NCNumericEnricher.start(span),
                () ⇒ NCGeoEnricher.start(span),
                () ⇒ NCCoordinatesEnricher.start(span)
            )
        }

        ners = NCNlpServerManager.getNers
        supportedProviders = ners.keySet ++ (if (Config.supportNlpCraft) Set("nlpcraft") else Set.empty)

        super.start()
    }
    
    /**
      * Stops this manager.
      */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { span ⇒
        if (Config.supportNlpCraft) {
            NCCoordinatesEnricher.stop(span)
            NCGeoEnricher.stop(span)
            NCNumericEnricher.stop(span)
            NCDateEnricher.stop(span)
        }

        NCQuoteEnricher.stop(span)
        NCStopWordEnricher.stop(span)
        NCBaseNlpEnricher.stop(span)
        
        cache = null
        
        super.stop()
    }

    /**
      *
      * @return
      */
    def getSupportedProviders: Set[String] =
        supportedProviders
}
