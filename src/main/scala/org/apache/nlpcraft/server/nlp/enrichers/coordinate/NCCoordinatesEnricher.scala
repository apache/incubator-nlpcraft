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

package org.apache.nlpcraft.server.nlp.enrichers.coordinate

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp._
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.nlp.numeric._
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher

import scala.collection._

/**
  * Coordinates enricher.
  *
  * Current version support only double representation of latitude and longitude.
  * Must be extended to support all variants of coordinates representation.
  * See http://www.geomidpoint.com/latlon.html.
  */
object NCCoordinatesEnricher extends NCServerEnricher {
    private final val LAT_STEMS = Seq("lat", "latitude").map(NCNlpCoreManager.stem)
    private final val LON_STEMS = Seq("lon", "longitude").map(NCNlpCoreManager.stem)

    private final val MARKERS_STEMS = {
        val p = new NCMacroParser

        Seq(
            "°",
            "{exact|approximate|*} {latitude|lat|longitude|lon}",
            "{following|*} {geo|*} coordinates {data|info|information|*}"
        ).flatMap(p.expand).map(NCNlpCoreManager.stem)
    }

    private final val SEPS = Seq(",", ";", "and")
    private final val EQUALS = Seq("=", "==", "is", "are", "equal")
    
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        super.start()
    }
    
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
    
    /**
      * 
      * @param num
      * @param max
      * @return
      */
    private def inRange(num: NCNumeric, max: Double): Boolean = Math.abs(num.value) < max
    
    /**
      *
      * @param nums
      * @return
      */
    private def similar2Coordinates(nums: NCNumeric*): Boolean =
        nums.forall(n ⇒ {
            val v = Math.abs(n.value)
            val s = v.toString
            val len = s.length - s.indexOf('.') - 1

            len == 5 || len == 6 || len == 7
        })
    
    /**
      *
      * @param ns
      * @param from
      * @param to
      * @param mkData
      * @return
      */
    private def get(
        ns: NCNlpSentence, from: Int, to: Int, filter: Seq[NCNlpSentenceToken] ⇒ Boolean, mkData: () ⇒ Seq[NCNlpSentenceToken]
    ): Option[Seq[NCNlpSentenceToken]] =
        if (to >= from) {
            val seq = ns.slice(from, to)

            if (seq.isEmpty || filter(seq)) Some(mkData() ++ seq) else None
        }
        else
            None
    
    /**
      *
      * @param ns
      * @param toks
      * @param markers
      * @return
      */
    private def getAfter(ns: NCNlpSentence, toks: Seq[NCNlpSentenceToken], markers: Seq[Seq[NCNlpSentenceToken]]): Seq[NCNlpSentenceToken] =
        if (toks.nonEmpty) {
            val from = toks.head.index

            markers.toStream.
                flatMap(m ⇒ get(
                    ns,
                    from,
                    m.head.index,
                    (seq: Seq[NCNlpSentenceToken]) ⇒ seq.forall(_.isStopWord),
                    () ⇒ m
                )).
                headOption.
                getOrElse(Seq.empty)
        }
        else
            Seq.empty
    
    /**
      *
      * @param ns
      * @param toks
      * @param markers
      * @return
      */
    private def getBefore(ns: NCNlpSentence, toks: Seq[NCNlpSentenceToken], markers: Seq[Seq[NCNlpSentenceToken]]): Seq[NCNlpSentenceToken] =
        if (toks.nonEmpty) {
            val to = toks.last.index + 1

            markers.toStream.
                flatMap(m ⇒ get(
                    ns,
                    m.last.index + 1,
                    to,
                    (seq: Seq[NCNlpSentenceToken]) ⇒ seq.forall(t ⇒ t.isStopWord || EQUALS.contains(t.normText)),
                    () ⇒ ns.
                        take(m.head.index).
                        reverse.
                        takeWhile(t ⇒ t.pos == "IN" || EQUALS.contains(t.normText)).
                        reverse ++ m
                )).
                headOption.
                getOrElse(Seq.empty)
        }
        else
            Seq.empty
    
    /**
      *
      * @param toks
      * @param stems
      * @return
      */
    private def hasStem(toks: Seq[NCNlpSentenceToken], stems: Seq[String]): Boolean = toks.exists(t ⇒ stems.contains(t.stem))
    
    override def enrich(ns: NCNlpSentence, parent: Span = null): Unit =
        startScopedSpan("enrich", parent, "srvReqId" → ns.srvReqId, "txt" → ns.text) { _ ⇒
            val nums = NCNumericManager.find(ns).sortBy(_.tokens.head.index)
            
            if (nums.size >= 2) {
                val markers = mutable.Buffer.empty[Seq[NCNlpSentenceToken]]
        
                def areSuitableTokens(toks: Seq[NCNlpSentenceToken]): Boolean =
                    toks.forall(t ⇒ !t.isQuoted && !t.isBracketed) && !markers.exists(_.exists(t ⇒ toks.contains(t)))
        
                for (toks ← ns.tokenMixWithStopWords() if areSuitableTokens(toks) && MARKERS_STEMS.contains(toks.map(_.stem).mkString(" ")))
                    markers += toks
                
                val allMarkers = markers.flatten
                val buf = mutable.Buffer.empty[NCNlpSentenceToken]
                
                for (pair ← nums.sliding(2) if !buf.exists(t ⇒ pair.flatMap(_.tokens).contains(t))) {
                    var lat = pair.head
                    var lon = pair.last
                
                    val between = ns.slice(lat.tokens.last.index + 1, lon.tokens.head.index)
                    val before = getBefore(ns, ns.take(lat.tokens.head.index), markers)
                
                    val after = getAfter(ns, ns.drop(lon.tokens.last.index + 1), markers)
                
                    if (hasStem(before, LON_STEMS) && hasStem(between, LAT_STEMS) ||
                        hasStem(between, LON_STEMS) && hasStem(after, LAT_STEMS) ||
                        !inRange(lat, 90) && inRange(lat, 180)
                    ) {
                        val tmp = lat
                        lat = lon
                        lon = tmp
                    }
                    if (inRange(lat, 90) && inRange(lon, 180) && (markers.nonEmpty || similar2Coordinates(lat, lon))) {
                        val normBetween = between.diff(allMarkers)
                        
                        if (normBetween.isEmpty ||
                            normBetween.forall(
                                t ⇒ t.isEmpty || t.pos == "IN" || SEPS.contains(t.normText) || EQUALS.contains(t.normText))
                        ) {
                            val extra = (before ++ after ++ between).sortBy(_.index)
                        
                            if (markers.exists(extra.containsSlice) || similar2Coordinates(lat, lon)) {
                                val toks = (lat.tokens ++ lon.tokens ++ extra ++ markers.flatten).distinct.sortBy(_.index)
                        
                                val note = NCNlpSentenceNote(
                                    toks.map(_.index),
                                    "nlpcraft:coordinate",
                                    "latitude" → lat.value,
                                    "longitude" → lon.value
                                )
                        
                                toks.foreach(_.add(note))
                        
                                buf ++= toks
                            }
                        }
                    }
                }
            }
        }
}
