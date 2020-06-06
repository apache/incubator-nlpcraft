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

package org.apache.nlpcraft.model.impl

import java.text.SimpleDateFormat
import java.util

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ascii._
import org.apache.nlpcraft.common.nlp._
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.model.impl.NCTokenPimp._

import scala.collection.JavaConverters._
import scala.collection._

/**
 * Utility service that provides supporting functionality for ASCII rendering.
 */
object NCTokenLogger extends LazyLogging {
    case class NoteMetadata(noteType: String, filtered: Seq[String], isFull: Boolean)
    
    // Order and sorting of notes for ASCII output.
    private final val NOTE_TYPES = Seq[String](
        "nlpcraft:nlp",
        "nlpcraft:continent",
        "nlpcraft:subcontinent",
        "nlpcraft:country",
        "nlpcraft:metro",
        "nlpcraft:region",
        "nlpcraft:city",
        "nlpcraft:date",
        "nlpcraft:num",
        "nlpcraft:relation",
        "nlpcraft:sort",
        "nlpcraft:limit",
        "nlpcraft:coordinate"
    )

    // Filters for notes types. If filter is not set all columns will display.
    private final val NOTE_COLUMNS = Map[String, Seq[String]](
        "nlpcraft:nlp" →
            Seq(
                "index",
                "origText",
                "lemma",
                "pos",
                "quoted",
                "stopWord",
                "dict",
                "wordIndexes",
                "direct",
                "sparsity"
            )
    )
    
    private final val SORT: Map[String, Map[String, Int]] =
        Map(
            "nlpcraft:continent" → Seq("continent"),
            "nlpcraft:subcontinent" → Seq("subcontinent", "continent"),
            "nlpcraft:country" → Seq("country", "subcontinent", "continent"),
            "nlpcraft:metro" → Seq("metro"),
            "nlpcraft:region" → Seq("region", "country", "subcontinent", "continent", "metro"),
            "nlpcraft:city" → Seq("city", "latitude", "longitude", "region", "country", "subcontinent", "continent"),
            "nlpcraft:date" → Seq("from", "to", "periods"),
            "nlpcraft:relation" → Seq("type", "indexes", "note"),
            "nlpcraft:sort" → Seq("asc", "subjnotes", "subjindexes", "bynotes", "byindexes"),
            "nlpcraft:limit" → Seq("limit", "indexes", "asc", "note")
        ).map(p ⇒ p._1 → p._2.zipWithIndex.map(p ⇒ p._1 → p._2).toMap)

    private def format(l: Long): String = new SimpleDateFormat("yyyy/MM/dd").format(new java.util.Date(l))

    /**
     * Filters and sorts keys pairs to visually group notes logically.
     *
     * @param pairs Sequence of note key/note value key pairs.
     */
    private def filterKeysPairs(pairs: Seq[(String, String)]): Seq[NoteMetadata] = {
        val seq =
            pairs.map(_._1).distinct.map(p ⇒ p → pairs.filter(_._1 == p).map(_._2)).
                sortBy(p ⇒ {
                    val idx = NOTE_TYPES.indexWhere(_ == p._1)

                    if (idx >= 0) idx else Integer.MAX_VALUE
                })

        seq.map(s ⇒ {
            val t = s._1

            val (filtered, isFull) =
                if (t.startsWith("nlpcraft:"))
                    NOTE_COLUMNS.get(t) match {
                        case Some(fs) ⇒ (s._2.filter(fs.contains).sortBy(p ⇒ fs.indexWhere(_ == p)), false)
                        case None ⇒ (Seq.empty[String], true)
                    }
                else
                    (Seq.empty[String], true)

            NoteMetadata(t, filtered, isFull)
        })
    }
    
    /**
      * Normalize header.
      *
      * @param h Header.
      */
    private def normalizeHeader(h: String): String = if (h.startsWith("nlpcraft:")) h.replaceAll("nlpcraft:", "") else h

    /**
     *
     * @param md Notes.
     */
    private def mkTable(md: Seq[NoteMetadata]): NCAsciiTable =
        NCAsciiTable(md.flatMap(h ⇒
            if (h.isFull)
                Seq(normalizeHeader(h.noteType))
            else
                h.filtered.map(p ⇒ s"${normalizeHeader(h.noteType)}:${p.toLowerCase}")
        ): _*)
    
    private def note2String(note: NCNlpSentenceNote): String = {
        val sorted: Seq[(String, java.io.Serializable)] =
            SORT.get(note.noteType) match {
                case Some(map) ⇒ note.toSeq.sortBy(p ⇒ map.getOrElse(p._1, Int.MaxValue))
                case None ⇒ note.toSeq
            }

        def vals2String(seq: Seq[(String, java.io.Serializable)]): String = {
            def getValue(name: String): java.io.Serializable = {
                val found = seq.find(_._1 == name)
            
                // Fail-fast in case of programmatic errors.
                require(found.isDefined, s"Invalid note value: $name")
            
                found.get._2
            }
        
            def mkMore(incl: Boolean): String = if (incl) ">=" else ">"
            def mkLess(incl: Boolean): String = if (incl) "<=" else "<"
            def mkValue(name: String, fractionalField: String): String = {
                val d = getValue(name).asInstanceOf[Double]

                if (getValue(fractionalField).asInstanceOf[Boolean]) d.toString else d.toInt.toString
            }
            def mkBool(name: String): Boolean = getValue(name).asInstanceOf[Boolean]
            def mkBoolOpt(name: String): Option[Boolean] = getValueOpt(name) match {
                case Some(b) ⇒ Some(b.asInstanceOf[Boolean])
                case None ⇒ None
            }
            def mkString(name: String): String = getValue(name).toString
            def mkJListString(name: String): String =
                getValue(name).asInstanceOf[java.util.List[String]].asScala.mkString(",")
            def mkDate(name: String): String = format(getValue(name).asInstanceOf[Long])

            def getValueOpt(name: String): Option[java.io.Serializable] =
                seq.find(_._1 == name) match {
                    case Some(x) ⇒ Some(x._2)
                    case None ⇒ None
                }

            def mkStringOpt(name: String): Option[String] =
                getValueOpt(name) match {
                    case Some(jv) ⇒ Some(jv.toString)
                    case None ⇒ None
                }

            def mkDouble3(name: String): Double = (getValue(name).asInstanceOf[Double] * 1000).intValue / 1000.0

            def indexes2String(v: java.io.Serializable): String = v.asInstanceOf[util.List[Int]].asScala.mkString(",")
            def mkIndexes(name: String): String = indexes2String(getValue(name))
            def mkIndexesOpt(name: String): Option[String] =
                getValueOpt(name) match {
                    case Some(indexes) ⇒ Some(indexes2String(indexes))
                    case None ⇒ None
                }

            def getSeq(names: String*): String = names.flatMap(name ⇒ mkStringOpt(name)).mkString("|")

            note.noteType match {
                case "nlpcraft:continent" ⇒ getSeq("continent")
                case "nlpcraft:subcontinent" ⇒ getSeq("continent", "subcontinent")
                case "nlpcraft:country" ⇒ getSeq("continent", "subcontinent", "country")
                case "nlpcraft:region" ⇒ getSeq("continent", "subcontinent", "country", "region")
                case "nlpcraft:city" ⇒ getSeq("continent", "subcontinent", "country", "region", "city")
                case "nlpcraft:metro" ⇒ getSeq("metro")
                case "nlpcraft:date" ⇒
                    val from = mkDate("from")
                    val to = mkDate("to")
                    val ps = mkJListString("periods")
                
                    val r = s"$from:$to"
                
                    s"range=$r, periods=$ps"

                case "nlpcraft:relation" ⇒
                    val t = mkString("type")
                    val note = mkString("note")

                    s"type=$t, indexes=[${mkIndexes("indexes")}], note=$note"

                case "nlpcraft:sort" ⇒
                    var s =
                        mkStringOpt("subjnotes") match {
                            case Some(subjnotes) ⇒ s"subjnotes=$subjnotes, subjindexes=${mkIndexes("subjindexes")}"
                            case None ⇒ ""
                        }

                    mkStringOpt("bynotes") match {
                        case Some(bynotes) ⇒
                            val sBy = s"bynotes=$bynotes, byindexes=${mkIndexes("byindexes")}"

                            s = if (s.nonEmpty) s"$s, $sBy" else sBy
                        case None ⇒ // No-op.
                    }

                    val ascOpt = mkBoolOpt("asc")

                    ascOpt match {
                        case Some(asc) ⇒ s = s"$s, asc=$asc"
                        case None ⇒ // No-op.
                    }

                    s

                case "nlpcraft:limit" ⇒
                    val limit = mkDouble3("limit")
                    val ascOpt = mkBoolOpt("asc")
                    val note = mkString("note")

                    var s = s"limit=$limit, indexes=[${mkIndexes("indexes")}], note=$note"

                    ascOpt match {
                        case Some(asc) ⇒ s = s"$s, asc=$asc"
                        case None ⇒ // No-op.
                    }

                    s

                case "nlpcraft:coordinate" ⇒ s"${getValue("latitude")} and ${getValue("longitude")}"

                case "nlpcraft:num" ⇒
                    val from = mkValue("from", "isFractional")
                    val to = mkValue("to", "isFractional")
                    val fromIncl = mkBool("fromIncl")
                    val toIncl = mkBool("toIncl")
                    val isRangeCond = mkBool("isRangeCondition")
                    val isEqCond = mkBool("isEqualCondition")
                    val isNotEqCond = mkBool("isNotEqualCondition")
                    val isFromNegInf = mkBool("isFromNegativeInfinity")
                    val isToPosInf = mkBool("isToPositiveInfinity")

                    val x1 = if (isFromNegInf) "-Infinity" else from
                    val x2 = if (isToPosInf) "+Infinity" else to

                    var s =
                        if (isRangeCond)
                            s"${mkMore(fromIncl)}$x1 && ${mkLess(toIncl)}$x2"
                        else if (isEqCond)
                            s"=$x1"
                        else {
                            assert(isNotEqCond)

                            s"!=$x1"
                        }

                    s = getValueOpt("unit") match {
                        case Some(u) ⇒ s"$s, unit=$u(${getValue("unitType")})"
                        case None ⇒ s
                    }

                    s

                case name if name.startsWith("google:") ⇒
                    val meta =
                        getValue("meta").
                            asInstanceOf[java.util.Map[String, java.io.Serializable]].
                            asScala.map(p ⇒ s"${p._1}=${p._2}").mkString(",")

                    // Mentions.
                    val beginOffsets = getValue("mentionsBeginOffsets").asInstanceOf[java.util.List[Int]]
                    val contents = getValue("mentionsContents").asInstanceOf[java.util.List[String]]
                    val types = getValue("mentionsTypes").asInstanceOf[java.util.List[String]]

                    require(beginOffsets.size() == contents.size())
                    require(types.size() == contents.size())

                    val mentions =
                        beginOffsets.asScala.zip(contents.asScala).zip(types.asScala).
                            map { case ((o, c), t) ⇒ s"beginOffset=$o, content=$c, type=$t" }.mkString(", ")

                    val sal = mkDouble3("salience")

                    s"meta=[$meta], mentions=[$mentions], salience=$sal"

                case name if name.startsWith("stanford:") ⇒
                    var s = s"confidence=${mkDouble3("confidence")}"

                    mkStringOpt("nne") match {
                        case Some(nne) ⇒ s = s"$s, nne=$nne"
                        case None ⇒ // No-op.
                    }

                    s
                case name if name.startsWith("opennlp:") ⇒
                    s"probability=${mkDouble3("probability")}"

                case name if name.startsWith("spacy:") ⇒
                    var s = s"vector=${mkDouble3("vector")}, sentiment=${mkDouble3("sentiment")}"

                    getValueOpt("meta") match {
                        case Some(m) ⇒
                            val metaMap = m.asInstanceOf[java.util.Map[String, String]].asScala

                            if (metaMap.nonEmpty) {
                                val v = metaMap.map(p ⇒ s"${p._1}=${p._2}").mkString(",")

                                s = s"$s, meta=$v"
                            }
                        case None ⇒ // No-op.
                    }

                    s

                // User tokens.
                case _ ⇒ ""
            }
        }
    
        val v = if (sorted.lengthCompare(1) > 0) vals2String(sorted) else sorted.map(p ⇒ s"${p._2}").mkString(", ")
    
        if (note.tokenFrom < note.tokenTo)
            s"$v ${s"<${note.tokenFrom} to ${note.tokenTo}>"}"
        else
            s"$v"
    }

    private def mkCells(hs: Seq[NoteMetadata], t: NCNlpSentenceToken): Seq[String] = {
        def filter(h: NoteMetadata): Iterable[NCNlpSentenceNote] = t.filter(_.noteType == h.noteType)
        
        hs.flatMap(h ⇒
            if (h.isFull)
                Seq(filter(h).map(p ⇒ note2String(p)).mkString(", "))
            else
                h.filtered.
                    map(p ⇒ filter(h).filter(_.contains(p)).map(n ⇒ n(p)).mkString(", "))
        )
    }
    
    /**
      * Prepares table to print.
      */
    def prepareTable(sen: NCNlpSentence): NCAsciiTable = {
        val md = filterKeysPairs(sen.flatMap(t ⇒ t.map(n ⇒ for (vk ← n.keys) yield n.noteType → vk)).flatten.distinct)
        
        val tbl = mkTable(md)
        
        for (t ← sen) tbl += (mkCells(md, t): _*)
        
        tbl
    }

    /**
      * Prepares table to print.
      */
    def prepareTable(toks: Seq[NCToken]): NCAsciiTable = {
        val allFree = toks.forall(_.isFreeWord)

        val headers =
            mutable.ArrayBuffer.empty[String] ++
            Seq(
                "idx",
                "origtext",
                "lemma",
                "pos",
                "quoted",
                "stopword",
                "wordindexes",
                "direct",
                "sparsity"
            )

        if (!allFree)
            headers += "token data"

        val tbl = NCAsciiTable(headers :_*)

        toks.foreach(tok ⇒ {
            val md: util.Map[String, AnyRef] = tok.getMetadata
            val id = tok.getId

            def mkFullName(name: String): String = s"$id:$name"
            def get[T](name: String): T = md.get(mkFullName(name)).asInstanceOf[T]
            def getOpt[T](name: String): Option[T] = {
                val v = md.get(mkFullName(name))

                if (v != null) Some(v.asInstanceOf[T]) else None
            }

            def has(name: String): Boolean = md.containsKey(mkFullName(name))

            def mkString(names: String*): String = names.flatMap(name ⇒ {
                val opt = getOpt(name)

                opt
            }).mkString("|")

            def getIndexes(name: String): String = {
                val idxs: java.util.List[String] = get(name)

                idxs.asScala.mkString(", ")
            }

            def mkDouble3(name: String): Double = {
                val d: Double = get(name)

                (d * 1000).intValue / 1000.0
            }

            val row =
                Seq(
                    tok.index,
                    tok.origText,
                    tok.lemma,
                    tok.pos,
                    tok.isQuoted,
                    tok.isStopWord,
                    s"[${tok.wordIndexes.mkString(",")}]",
                    tok.isDirect,
                    tok.sparsity
                )

            if (allFree)
                tbl += (row :_*)
            else {
                val v =
                    id match {
                        case "nlpcraft:nlp" ⇒ ""

                        case "nlpcraft:continent" ⇒ mkString("continent")
                        case "nlpcraft:subcontinent" ⇒ mkString("continent", "subcontinent")
                        case "nlpcraft:country" ⇒ mkString("continent", "subcontinent", "country")
                        case "nlpcraft:region" ⇒ mkString("continent", "subcontinent", "country", "region")
                        case "nlpcraft:city" ⇒ mkString("continent", "subcontinent", "country", "region", "city")
                        case "nlpcraft:metro" ⇒ mkString("metro")
                        case "nlpcraft:date" ⇒
                            val from = format(get("from"))
                            val to = format(get("to"))
                            val ps: java.util.List[String] = get("periods")

                            val r = s"$from:$to"

                            s"range=$r, periods=${ps.asScala.mkString(",")}"

                        case "nlpcraft:relation" ⇒
                            val t = mkString("type")
                            val note = mkString("note")

                            s"type=$t, indexes=[${getIndexes("indexes")}], note=$note"

                        case "nlpcraft:sort" ⇒
                            def x(l: java.util.List[String]): String = l.asScala.mkString(", ")

                            def getList(notesName: String, indexesName: String): String = {
                                val notesOpt: Option[java.util.List[String]] = getOpt(notesName)

                                notesOpt match {
                                    case Some(notes) ⇒
                                        s"$notesName=${x(notes)}, $indexesName=[${getIndexes(indexesName)}]"
                                    case None ⇒ ""
                                }
                            }

                            var s = getList("subjnotes", "subjindexes")
                            val by = getList("bynotes", "byindexes")

                            if (by.nonEmpty)
                                s = if (s.nonEmpty) s"$s, $by" else by

                            require(s.nonEmpty)

                            if (has("asc"))
                                s = s"$s, asc=${get("asc")}"

                            s
                        case "nlpcraft:limit" ⇒
                            val limit = mkDouble3("limit")
                            val note = mkString("note")

                            var s = s"limit=$limit, indexes=[${getIndexes("indexes")}], note=$note"

                            if (has("asc"))
                                s = s"$s, asc=${get("asc")}"

                            s

                        case "nlpcraft:num" ⇒
                            def mkValue(name: String, fractionalField: String): String = {
                                val d: Double = get(name)
                                val fr: Boolean = get(fractionalField)

                                if (fr) d.toString else d.toInt.toString
                            }

                            def mkMore(incl: Boolean): String = if (incl) ">=" else ">"

                            def mkLess(incl: Boolean): String = if (incl) "<=" else "<"

                            val from = mkValue("from", "isfractional")
                            val to = mkValue("to", "isfractional")
                            val fromIncl: Boolean = get("fromincl")
                            val toIncl: Boolean = get("toincl")
                            val isRangeCond: Boolean = get("israngecondition")
                            val isEqCond: Boolean = get("isequalcondition")
                            val isNotEqCond: Boolean = get("isnotequalcondition")
                            val isFromNegInf: Boolean = get("isfromnegativeinfinity")
                            val isToPosInf: Boolean = get("istopositiveinfinity")

                            val x1 = if (isFromNegInf) "-Infinity" else from
                            val x2 = if (isToPosInf) "+Infinity" else to

                            var s =
                                if (isRangeCond)
                                    s"${mkMore(fromIncl)}$x1 && ${mkLess(toIncl)}$x2"
                                else if (isEqCond)
                                    s"=$x1"
                                else {
                                    assert(isNotEqCond)

                                    s"!=$x1"
                                }

                            if (has("unit")) {
                                val unit = mkString("unit")
                                val unitType = mkString("unittype")

                                s = s"$s, unit=$unit($unitType)"
                            }

                            s
                        case "nlpcraft:coordinate" ⇒ mkString("latitude", "longitude")
                        case name if name.startsWith("google:") ⇒
                            val meta: java.util.Map[String, java.io.Serializable] = get("meta")
                            val metaS = meta.asScala.map(p ⇒ s"${p._1}=${p._2}").mkString(",")

                            // Mentions.
                            val beginOffsets: java.util.List[Int] = get("mentionsbeginoffsets")
                            val contents: java.util.List[String] = get("mentionscontents")
                            val types: java.util.List[String] = get("mentionstypes")

                            require(beginOffsets.size() == contents.size())
                            require(types.size() == contents.size())

                            val mentions =
                                beginOffsets.asScala.zip(contents.asScala).zip(types.asScala).
                                    map { case ((o, c), t) ⇒ s"beginOffset=$o, content=$c, type=$t" }.mkString(", ")

                            val sal = mkDouble3("salience")

                            s"meta=[$metaS], mentions=[$mentions], salience=$sal"
                        case name if name.startsWith("opennlp:") ⇒ s"probability=${mkDouble3("probability")}"
                        case name if name.startsWith("stanford:") ⇒
                            var s = s"confidence=${mkDouble3("confidence")}"

                            if (has("nne"))
                                s = s"$s, nne=${get("nne")}"

                            s
                        case name if name.startsWith("spacy:") ⇒
                            var s = s"vector=${mkDouble3("vector")}, sentiment=${mkDouble3("sentiment")}"

                            val metaOpt: Option[java.util.Map[String, String]] = getOpt("meta")

                            metaOpt match {
                                case Some(m) ⇒
                                    val ms = m.asScala

                                    if (ms.nonEmpty) {
                                        val v = ms.map(p ⇒ s"${p._1}=${p._2}").mkString(",")

                                        s = s"$s, meta=$v"
                                    }
                                case None ⇒ // No-op.
                            }

                            s
                            
                        // User defined token.
                        case _ ⇒
                            def tok2Str(t: NCToken): String = {
                                var s = s"id=${t.getId}"

                                t.meta(TOK_META_ALIASES_KEY).asInstanceOf[java.util.Set[String]] match {
                                    case null ⇒ // No-op.
                                    case aliases ⇒ s = s"$s, aliases='${aliases.asScala.mkString(",")}'"
                                }

                                val parts = t.getPartTokens.asScala.map(tok2Str).mkString("|")

                                if (parts.nonEmpty)
                                    s = s"$s, parts=[$parts]"

                                s
                            }

                            tok2Str(tok)
                    }

                tbl += (
                    row
                    ++
                    // Token data.
                    Seq(if (tok.getId == "nlpcraft:nlp") "" else s"<<${tok.getId}>> $v") :_*
                )
            }
        })

        tbl
    }
}