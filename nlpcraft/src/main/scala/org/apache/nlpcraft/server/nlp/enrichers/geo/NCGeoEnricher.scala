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

package org.apache.nlpcraft.server.nlp.enrichers.geo

import java.util

import com.fasterxml.jackson.core.`type`.TypeReference
import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.nlp._
import org.apache.nlpcraft.common.nlp.pos.NCPennTreebank
import org.apache.nlpcraft.server.geo.NCGeoLocationKind._
import org.apache.nlpcraft.server.geo._
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher
import org.apache.nlpcraft.server.nlp.wordnet.NCWordNetManager

import scala.collection._

/**
  * Geo-location enricher.
  */
object NCGeoEnricher extends NCServerEnricher {
    // US states that conflict with commonly used English words (lower case set).
    private final val US_CONFLICT_STATES = Set("hi", "or", "in", "ok", "as", "me")

    // Words used to express one location is inside another one.
    // Example is San Francisco of USA or Paris in France.
    private final val IN_WORDS =
        immutable.HashSet(",", "in", "within", "inside", "of", "inside of", "within of", "wherein")

    // USA large cities configuration file.
    private final val US_TOP_PATH = "geo/us_top.yaml"

    // World large cities configuration file.
    private final val WORLD_TOP_PATH = "geo/world_top.yaml"

    // Common word exceptions configuration folder.
    private final val EXCEPTIONS_PATH = "geo/exceptions"

    private final val GEO_TYPES: Set[String] = NCGeoLocationKind.values.map(mkName)

    @volatile private var locations: Map[String, Set[NCGeoEntry]] = _
    @volatile private var commons: Map[NCGeoLocationKind, Set[String]] = _
    @volatile private var topUsa: Set[String] = _
    @volatile private var topWorld: Set[String] = _

    // Extractor for largest cities.
    case class TopCity(name: String, region: String)

    private def glue(s: String*): String = s.map(_.toLowerCase).mkString("|")

    private def isConflictName(name: String): Boolean =
        US_CONFLICT_STATES.contains(name.toLowerCase) && name.exists(_.isLower)

    private def mkName(k: NCGeoLocationKind): String = s"nlpcraft:${k.toString.toLowerCase}"
    private def extractKind(note: NCNlpSentenceNote): NCGeoLocationKind =
        NCGeoLocationKind.withName(note.noteType.replace("nlpcraft:", "").toUpperCase)

    private def getGeoNotes(ns: NCNlpSentence): Set[NCNlpSentenceNote] = GEO_TYPES.flatMap(ns.getNotes)
    private def getGeoNotes(t: NCNlpSentenceToken): Set[NCNlpSentenceNote] = GEO_TYPES.flatMap(t.getNotes)

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()

        commons = null
        topUsa = null
        topWorld = null
        locations = null
    }

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        locations = NCGeoManager.getModel.synonyms

        val extOpt = U.sysEnv("NLPCRAFT_RESOURCE_EXT")

        // GEO names matched with common english words and user defined exception GEO names.
        // Note that 'ignore case' parameter set as false because DLGeoLocationKind definition (CITY ect)

        commons =
            U.getContent(
                EXCEPTIONS_PATH,
                extOpt,
                (name: String) ⇒ name.endsWith("yaml")
            ).
                flatMap { case (path, data) ⇒
                    U.extractYamlString(
                        data,
                        path,
                        ignoreCase = false,
                        new TypeReference[immutable.Map[String, immutable.Set[String]]] {}
                    )
                }.
                map(p ⇒ NCGeoLocationKind.withName(p._1.toUpperCase) → p._2).
                groupBy(_._1).
                map(p ⇒ p._1 → p._2.flatMap(_._2).toSet).map(p ⇒ p._1 → p._2.map(_.toLowerCase))

        def readCities(res: String): List[TopCity] =
            U.extractYamlString(U.getContent(res, extOpt), res, ignoreCase = true, new TypeReference[List[TopCity]] {})

        topUsa = readCities(US_TOP_PATH).map(city ⇒ glue(city.name, city.region)).toSet
        topWorld = readCities(WORLD_TOP_PATH).map(city ⇒ glue(city.name, city.region)).toSet

        super.start()
    }

    @throws[NCE]
    override def enrich(ns: NCNlpSentence, parent: Span = null): Unit =
        startScopedSpan("enrich", parent, "srvReqId" → ns.srvReqId, "txt" → ns.text) { _ ⇒
            // This stage must not be 1st enrichment stage.
            assume(ns.nonEmpty)

            for (toks ← ns.tokenMixWithStopWords(withQuoted = true)) {
                def mkNote(kind: NCGeoLocationKind, seq: (String, Any)*): NCNlpSentenceNote =
                    NCNlpSentenceNote(toks.map(_.index), mkName(kind), seq :_*)

                def toSerializable(m: Map[String, Any]): java.io.Serializable= {
                    val ser = new util.HashMap[String, Object]()

                    m.foreach { case (k, v) ⇒ ser.put(k, v.asInstanceOf[Object]) }

                    ser
                }

                def make(e: NCGeoEntry): NCNlpSentenceNote =
                    e match {
                        case x: NCGeoMetro ⇒
                            mkNote(
                                METRO,
                                "metro" → x.name
                            )

                        case x: NCGeoContinent ⇒
                            mkNote(
                                CONTINENT,
                                "continent" → x.name
                            )

                        case x: NCGeoSubContinent ⇒
                            mkNote(
                                SUBCONTINENT,
                                "continent" → x.continent.name,
                                "subcontinent" → x.name
                            )

                        case x: NCGeoCountry ⇒
                            mkNote(
                                COUNTRY,
                                "continent" → x.subContinent.continent.name,
                                "subcontinent" → x.subContinent.name,
                                "country" → x.name,
                                "countrymeta" → toSerializable(x.meta)
                            )

                        case x: NCGeoRegion ⇒
                            mkNote(
                                REGION,
                                "continent" → x.country.subContinent.continent.name,
                                "subcontinent" → x.country.subContinent.name,
                                "country" → x.country.name,
                                "region" → x.name,
                                "countrymeta" → toSerializable(x.country.meta)
                            )

                        case x: NCGeoCity ⇒
                            mkNote(
                                CITY,
                                "continent" → x.region.country.subContinent.continent.name,
                                "subcontinent" → x.region.country.subContinent.name,
                                "country" → x.region.country.name,
                                "region" → x.region.name,
                                "city" → x.name,
                                "countrymeta" → toSerializable(x.region.country.meta),
                                "citymeta" → toSerializable(x.meta)
                            )
                            
                        case _ ⇒ throw new AssertionError(s"Unexpected data: $e")
                    }

                def addAll(locs: Set[NCGeoEntry]): Unit =
                    for (loc ← locs) {
                        val note = make(loc)

                        toks.foreach(t ⇒ t.add(note))

                        // Other types(JJ etc) and quoted word are not re-marked.
                        toks.filter(t ⇒ !NCPennTreebank.NOUNS_POS.contains(t.pos) && t.pos != "FW").
                            foreach(t ⇒ ns.fixNote(t.getNlpNote, "pos" → NCPennTreebank.SYNTH_POS))
                    }

                locations.get(toks.map(_.normText).mkString(" ")) match {
                    case Some(locs) ⇒
                        // If multiple token match - add it.
                        if (toks.length > 1)
                            addAll(locs)
                        else {
                            // Only one token - toks.length == 1
                            val t = toks.head

                            // If LOCATION or noun - add it.
                            if (NCPennTreebank.NOUNS_POS.contains(t.pos))
                                addAll(locs)
                            // If US state - add it.
                            else
                            // For now - simply ignore abbreviations for US states that
                            // conflict with commonly used English words. User will have to
                            // use full names.
                            if (!isConflictName(t.origText)) {
                                def isTopCity(g: NCGeoCity): Boolean = {
                                    val name = glue(g.name, g.region.name)

                                    topUsa.contains(name) || topWorld.contains(name)
                                }

                                addAll(locs.collect {
                                    case g: NCGeoContinent ⇒ g
                                    case g: NCGeoSubContinent ⇒ g
                                    case g: NCGeoCountry ⇒ g
                                    case g: NCGeoMetro ⇒ g
                                    case g: NCGeoRegion if g.country.name == "united states" ⇒ g
                                    case g: NCGeoCity if isTopCity(g) ⇒ g
                                })
                            }
                            // In all other cases - ignore one-token match.
                        }
                    case None ⇒
                        // Case sensitive synonyms.
                        locations.get(toks.map(_.origText).mkString(" ")) match {
                            case Some(locs) ⇒ addAll(locs)
                            case None ⇒
                                // If there is no direct match try to convert JJs to NNs and re-check
                                // for a possible match, e.g. "american" ⇒ "america".
                                if (toks.size == 1) {
                                    val tok = toks.head

                                    if (NCPennTreebank.JJS_POS.contains(tok.pos)) {
                                        var endLoop = false

                                        for (noun ← NCWordNetManager.getNNsForJJ(tok.normText); if !endLoop) {
                                            def onResult(locs: Set[NCGeoEntry]): Unit = {
                                                addAll(locs)
                                                endLoop = true
                                            }

                                            locations.get(noun) match {
                                                case Some(locs) ⇒ onResult(locs)
                                                case None ⇒
                                                    locations.get(noun.toLowerCase) match {
                                                        case Some(locs) ⇒ onResult(locs)
                                                        case None ⇒ // No-op.
                                                    }
                                            }
                                        }
                                    }
                                }
                        }
                }
            }

            collapse(ns)
        }

    private def getValue(note: NCNlpSentenceNote, key: String): String = note(key).asInstanceOf[String]
    private def getValueOpt(note: NCNlpSentenceNote, key: String): Option[String] = note.get(key) match {
        case Some(s) ⇒ Some(s.asInstanceOf[String])
        case None ⇒ None
    }

    private def getName(kind: NCGeoLocationKind, note: NCNlpSentenceNote): String =
        kind match {
            case METRO ⇒ getValue(note, "metro")
            case CONTINENT ⇒ getValue(note, "continent")
            case SUBCONTINENT ⇒ getValue(note, "subcontinent")
            case COUNTRY ⇒ getValue(note, "country")
            case REGION ⇒ getValue(note, "region")
            case CITY ⇒ getValue(note, "city")
            case _ ⇒ throw new AssertionError(s"sUnexpected kind: $kind")
        }

    private def isChild(note: NCNlpSentenceNote, parent: NCNlpSentenceNote): Boolean = {
        def same(n: String) = getValue(note, n) == getValue(parent, n)

        val nKind = extractKind(note)
        val pKind = extractKind(parent)

        if (nKind != pKind)
            nKind match {
                case CITY ⇒
                    pKind match {
                        case REGION ⇒ same("country") && same("region")
                        case COUNTRY ⇒ same("country")
                        case _ ⇒ false
                    }
                case REGION ⇒
                    pKind match {
                        case COUNTRY ⇒ same("country")
                        case SUBCONTINENT ⇒ same("subcontinent")
                        case CONTINENT ⇒ same("continent")
                        case _ ⇒ false
                    }
                case COUNTRY ⇒
                    pKind match {
                        case CONTINENT ⇒ same("continent")
                        case SUBCONTINENT ⇒ same("subcontinent")
                        case _ ⇒ false
                    }
                case CONTINENT ⇒ false
                case METRO ⇒ false
            }
        else
            false
    }

    @throws[NCE]
    private def collapse(ns: NCNlpSentence) {
        // Candidates for excluding.
        // GEO names matched with common words. (Single words only)
        val excls = new mutable.HashSet[NCNlpSentenceNote]() ++ getGeoNotes(ns).filter(note ⇒ {
            val kind = extractKind(note)

            commons.get(kind) match {
                // GEO is common word defined directly or via synonym.
                case Some(cs) ⇒
                    cs.contains(getName(kind, note)) ||
                        cs.contains(
                            ns.
                                filter(t ⇒ t.index >= note.tokenFrom && t.index <= note.tokenTo).
                                filter(!_.isStopWord).
                                map(_.normText).
                                mkString(" ")
                        )
                case None ⇒ false
            }
        })

        // Also added tokens with very short GEO names (with length is 1)
        excls ++= getGeoNotes(ns).filter(note ⇒ getName(extractKind(note), note).length == 1)

        def removeNote(n: NCNlpSentenceNote): Unit = ns.removeNote(n)

        // Check that city is inside country or region.
        // When true - remove larger location note and replace with
        // enlarged more detailed location note.
        def checkExtendNote(first: NCNlpSentenceNote, second: NCNlpSentenceNote, small: NCNlpSentenceNote, big: NCNlpSentenceNote) {
            if (isChild(small, big) || small == big) {
                logger.debug(s"Extending $small and swallow $big.")

                val note = small.clone(
                    first.tokenIndexes ++ second.tokenIndexes, first.wordIndexes ++ second.wordIndexes
                )

                removeNote(second)
                removeNote(first)

                // GEO names matched with common words shouldn't be excluded if they specified by valid GEO parents.
                excls -= first

                ns.
                    filter(t ⇒ t.index >= first.tokenFrom && t.index <= second.tokenTo && !t.isStopWord).
                    foreach(_.add(note))
            }
        }

        // Finds two collapsible neighboring location entities, takes more detailed one,
        // removes less detailed one, and enlarges the remaining (more detailed) one to
        // "cover" the tokens originally occupied by both entities.
        def enlarge(withOverlap: Boolean) {
            val locs = getGeoNotes(ns)

            locs.foreach(p ⇒ {
                // Get holders after the end of this one immediately or separated by IN_WORDS strings.
                locs.filter(x ⇒
                    if (x.tokenFrom > p.tokenFrom) {
                        val strBetween = ns.
                            filter(t ⇒ t.index > p.tokenTo && t.index < x.tokenFrom).
                            map(_.normText).mkString(" ")

                        ((x.tokenFrom <= p.tokenTo) && (x.tokenTo > p.tokenTo) &&
                        (x.tokenFrom != p.tokenFrom) && withOverlap) ||
                        (x.tokenFrom == p.tokenTo + 1) ||
                        ((x.tokenFrom > p.tokenTo + 1) && IN_WORDS.contains(strBetween))
                    }
                    else
                        false
                ).foreach(z ⇒ {
                    if (extractKind(p) > extractKind(z))
                        checkExtendNote(p, z, p, z) // 'a' is smaller and more detailed.
                    else
                        checkExtendNote(p, z, z, p)
                })
            })
        }

        // Do two iterations for cases like San Francisco, CA USA.
        // Second pass glues together results from the first
        // pass (San Francisco CA) and (CA USA).
        enlarge(false)
        enlarge(true)

        excls.foreach(e ⇒ removeNote(e))

        // Calculate a weight to rank locations.
        // ------------------------------------
        // Most important is note length. A less important is a kind of a note.
        // In US cities prevail over states, in the rest of the world - country over region
        // and over city. Across cities more important are world top cities and than US top
        // cities.

        def calcWeight(note: NCNlpSentenceNote): Seq[Int] = {
            def get(name: String): String = getValue(note, name)
            def getOpt(name: String): Option[String] = getValueOpt(note, name)
            val kind = extractKind(note)

            // Most important factor - length of catch tokens.
            val lenFactor = ns.filter(t ⇒ t.index >= note.tokenFrom && t.index <= note.tokenTo).count(!_.isStopWord)

            // If location is a city - top world cities get 20 additional points while US top
            // cities get 10 point. Note that 'DLGeoLocationKind' enumeration value ID is used
            // for scoring (city has bigger ID than region and country).
            val topsFactor = kind match {
                case CITY ⇒
                    val cityReg = glue(get("city"), get("region"))

                    if (topWorld.contains(cityReg))
                        2
                    else if (topUsa.contains(cityReg))
                        1
                    else
                        0
                case _ ⇒ 0
            }

            val usaFactor = getOpt("country") match {
                case Some(v) ⇒ if (v == "united states") 1 else 0
                case None ⇒ 0
            }

            // Note length has higher priority, than goes type of location
            // So, country > region > city for other countries).
            val kindFactor =
                kind match {
                    case CITY ⇒ 0
                    case REGION ⇒ 1
                    case METRO ⇒ 2
                    case COUNTRY ⇒ 3
                    case SUBCONTINENT ⇒ 4
                    case CONTINENT ⇒ 5

                    case _ ⇒ throw new AssertionError(s"Unexpected kind: $kind")
                }

            Seq(lenFactor, topsFactor, kindFactor, usaFactor)
        }

        case class Holder(note: NCNlpSentenceNote, kind: NCGeoLocationKind, weight: Seq[Int])

        for (tok ← ns) {
            val sorted = getGeoNotes(tok).
                map(n ⇒ Holder(n, extractKind(n), calcWeight(n))).
                toSeq.
                sortBy(
                    -_.weight.
                    reverse.
                    zipWithIndex.map { case (v, idx) ⇒ v * Math.pow(10, idx) }.sum
                )

            if (sorted.nonEmpty) {
                val sortedByKind = sorted.groupBy(_.kind)

                // Keeps best candidates for each GEO kind.
                val remainHs = sortedByKind.values.
                    flatMap(hsByKind ⇒ Seq(hsByKind.head) ++ hsByKind.tail.filter(_.weight == hsByKind.head.weight)).
                    toSeq

                sorted.diff(remainHs).foreach(p ⇒ removeNote(p.note))
            }
        }

        // Drops GEO notes which are not included into enabled built-in token list.
        // We can't do it before (or just ignore notes which are not from enabled list)
        // because GEO notes with different types influence on each other during processing.
        GEO_TYPES.diff(ns.enabledBuiltInToks).flatMap(ns.getNotes).foreach(ns.removeNote)
    }
}
