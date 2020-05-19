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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.nio.charset.StandardCharsets.UTF_8
import java.util
import java.util.{Base64, Optional}

import org.apache.nlpcraft.model.NCToken
import resource.managed

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._

/**
  * Tests infrastructure beans.
  */

sealed trait NCTestToken {
    def id: String
    def text: String // Case-sensitive
    def isStop: Boolean = false
}

// Simplified set of tokens data. Added only fields for validation.

// Server enrichers.
case class NCTestNlpToken(text: String, override val isStop: Boolean = false) extends NCTestToken {
    require(text != null)

    override def id: String = "nlpcraft:nlp"
    override def toString: String = s"$text(nlp)<isStop=$isStop>"
}

// Skip non-deterministic properties verification.
case class NCTestDateToken(text: String) extends NCTestToken {
    require(text != null)

    override def id: String = "nlpcraft:date"
    override def toString: String = s"$text(date)"
}

case class NCTestCoordinateToken(text: String, latitude: Double, longitude: Double) extends NCTestToken {
    require(text != null)

    override def id: String = "nlpcraft:coordinate"
    override def toString: String = s"$text(coordinate)<lon=$longitude, lat=$longitude>"
}

case class NCTestNumericToken(text: String, from: Double, to: Double) extends NCTestToken {
    require(text != null)

    override def id: String = "nlpcraft:num"
    override def toString: String = s"$text(num)<from=$from, to=$to>"
}

case class NCTestCityToken(text: String, city: String) extends NCTestToken {
    require(text != null)
    require(city != null)

    override def id: String = "nlpcraft:city"
    override def toString: String = s"$text(city)[city=$city]"
}

case class NCTestCountryToken(text: String, country: String) extends NCTestToken {
    require(text != null)
    require(country != null)

    override def id: String = "nlpcraft:country"
    override def toString: String = s"$text(country)<country=$country>"
}

case class NCTestRegionToken(text: String, region: String) extends NCTestToken {
    require(text != null)
    require(region != null)

    override def id: String = "nlpcraft:region"
    override def toString: String = s"$text(region)<region=$region>"
}

case class NCTestContinentToken(text: String, continent: String) extends NCTestToken {
    require(text != null)
    require(continent != null)

    override def id: String = "nlpcraft:continent"
    override def toString: String = s"$text(continent)<continent=$continent>"
}

case class NCTestSubcontinentToken(text: String, subcontinent: String) extends NCTestToken {
    require(text != null)
    require(subcontinent != null)

    override def id: String = "nlpcraft:subcontinent"
    override def toString: String = s"$text(subcontinent)<subcontinent=$subcontinent>"
}

case class NCTestMetroToken(text: String, metro: String) extends NCTestToken {
    require(text != null)
    require(metro != null)

    override def id: String = "nlpcraft:metro"
    override def toString: String = s"$text(metro)<metro=$metro>"
}

// Probe enrichers.
case class NCTestSortToken(
    text: String,
    subjNotes: Seq[String] = Seq.empty,
    subjIndexes: Seq[Int] = Seq.empty,
    byNotes: Seq[String] = Seq.empty,
    byIndexes: Seq[Int] = Seq.empty,
    asc: Option[Boolean] = None
) extends NCTestToken {
    require(text != null)
    require(subjNotes != null)
    require(subjIndexes != null)
    require(byNotes != null)
    require(byIndexes != null)
    require(asc != null)

    require(subjNotes.nonEmpty || byNotes.nonEmpty)
    require(subjIndexes.nonEmpty || byIndexes.nonEmpty)
    require(subjNotes.isEmpty && subjIndexes.isEmpty || subjNotes.nonEmpty && subjIndexes.nonEmpty)
    require(byNotes.isEmpty && byIndexes.isEmpty || byNotes.nonEmpty && byIndexes.nonEmpty)

    override def id: String = "nlpcraft:sort"
    override def toString: String = {
        var s = s"$text(sort)<"

        if (subjNotes.nonEmpty)
            s = s"${s}subjNotes=[${subjNotes.mkString(",")}], subjIndexes=[${subjIndexes.mkString(",")}]"

        if (byNotes.nonEmpty) {
            val sBy = s"byNotes=[${byNotes.mkString(",")}], byIndexes=[${byIndexes.mkString(",")}]"

            s = if (subjNotes.nonEmpty) s"$s, $sBy" else s"$s$sBy"
        }

        if (asc.isDefined)
            s = s"$s, asc=${asc.get}"

        s = s"$s>"

        s
    }
}

object NCTestSortTokenType extends Enumeration {
    type NCTestSortTokenType = Value
    val SUBJ_ONLY, BY_ONLY = Value
}

import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.NCTestSortTokenType._

object NCTestSortToken {
    def apply(
        text: String,
        subjNotes: Seq[String],
        subjIndexes: Seq[Int],
        byNotes: Seq[String],
        byIndexes: Seq[Int],
        asc: Boolean
    ): NCTestSortToken = new NCTestSortToken(text, subjNotes, subjIndexes, byNotes, byIndexes, Some(asc))

    def apply(
        text: String,
        subjNote: String,
        subjIndex: Int,
        byNote: String,
        byIndex: Int
    ): NCTestSortToken = new NCTestSortToken(text, Seq(subjNote), Seq(subjIndex), Seq(byNote), Seq(byIndex), None)

    def apply(
        text: String,
        typ: NCTestSortTokenType,
        note: String,
        index: Int
    ): NCTestSortToken =
        typ match {
            case SUBJ_ONLY ⇒ new NCTestSortToken(text, subjNotes = Seq(note), subjIndexes = Seq(index), asc = None)
            case BY_ONLY ⇒ new NCTestSortToken(text, byNotes = Seq(note), byIndexes = Seq(index), asc = None)

            case _ ⇒ throw new AssertionError(s"Unexpected type: $typ")
        }

    def apply(
        text: String,
        typ: NCTestSortTokenType,
        note: String,
        index: Int,
        asc: Boolean
    ): NCTestSortToken =
        typ match {
            case SUBJ_ONLY ⇒ new NCTestSortToken(text, subjNotes = Seq(note), subjIndexes = Seq(index), asc = Some(asc))
            case BY_ONLY ⇒ new NCTestSortToken(text, byNotes = Seq(note), byIndexes = Seq(index), asc = Some(asc))

            case _ ⇒ throw new AssertionError(s"Unexpected type: $typ")
        }

    def apply(
        text: String,
        subjNote: String,
        subjIndex: Int,
        byNote: String,
        byIndex: Int,
        asc: Boolean
    ): NCTestSortToken = new NCTestSortToken(text, Seq(subjNote), Seq(subjIndex), Seq(byNote), Seq(byIndex), Some(asc))
}

case class NCTestRelationToken(text: String, `type`: String, indexes: Seq[Int], note: String) extends NCTestToken {
    require(text != null)
    require(`type` != null)
    require(indexes != null)
    require(indexes.nonEmpty)
    require(note != null)

    override def id: String = "nlpcraft:relation"
    override def toString: String =
        s"$text(relation)" +
            s"<type=${`type`}" +
            s", indexes=[${indexes.mkString(",")}]" +
            s", note=$note>"
}

case class NCTestLimitToken(
    text: String,
    limit: Double,
    indexes: Seq[Int],
    note: String,
    asc: Option[Boolean]
) extends NCTestToken {
    require(text != null)
    require(indexes != null)
    require(indexes.nonEmpty)
    require(note != null)
    require(asc != null)

    override def id: String = "nlpcraft:limit"
    override def toString: String = {
        var s = s"$text(limit)" +
            s"<limit=$limit" +
            s", indexes=[${indexes.mkString(",")}]" +
            s", note=$note"

        if (asc.isDefined)
            s = s"$s, asc=${asc.get}"

        s = s"$s>"

        s
    }
}

object NCTestLimitToken {
    def apply(text: String, limit: Double, indexes: Seq[Int], note: String, asc: Boolean): NCTestLimitToken =
        new NCTestLimitToken(text, limit, indexes, note, Some(asc))

    def apply(text: String, limit: Double, indexes: Seq[Int], note: String): NCTestLimitToken =
        new NCTestLimitToken(text, limit, indexes, note, None)

    def apply(text: String, limit: Double, index: Int, note: String, asc: Boolean): NCTestLimitToken =
        new NCTestLimitToken(text, limit, Seq(index), note, Some(asc))

    def apply(text: String, limit: Double, index: Int, note: String): NCTestLimitToken =
        new NCTestLimitToken(text, limit, Seq(index), note, None)
}

case class NCTestUserToken(text: String, id: String) extends NCTestToken {
    require(text != null)
    require(id != null)

    override def toString: String = s"$text(user)<id=$id>"
}

// Token and sentence beans and utilities.

object NCTestToken {
    def apply(t: NCToken): NCTestToken = {
        val txt = t.getOriginalText
        val id = t.getId

        id match {
            case "nlpcraft:nlp" ⇒ NCTestNlpToken(txt, t.isStopWord)
            case "nlpcraft:coordinate" ⇒
                NCTestCoordinateToken(
                    txt,
                    latitude = t.meta("nlpcraft:coordinate:latitude"),
                    longitude = t.meta("nlpcraft:coordinate:longitude")
                )
            case "nlpcraft:num" ⇒
                NCTestNumericToken(
                    txt,
                    from = t.meta("nlpcraft:num:from"),
                    to = t.meta("nlpcraft:num:to")
                )
            case "nlpcraft:date" ⇒ NCTestDateToken(txt)
            case "nlpcraft:city" ⇒ NCTestCityToken(txt, city = t.meta("nlpcraft:city:city"))
            case "nlpcraft:region" ⇒ NCTestRegionToken(txt, region = t.meta("nlpcraft:region:region"))
            case "nlpcraft:country" ⇒ NCTestCountryToken(txt, country = t.meta("nlpcraft:country:country"))
            case "nlpcraft:subcontinent" ⇒ NCTestSubcontinentToken(txt, subcontinent = t.meta("nlpcraft:subcontinent:subcontinent"))
            case "nlpcraft:continent" ⇒ NCTestContinentToken(txt, continent = t.meta("nlpcraft:continent:continent"))
            case "nlpcraft:metro" ⇒ NCTestMetroToken(txt, metro = t.meta("nlpcraft:metro:metro"))
            case "nlpcraft:sort" ⇒
                val subjNotes: Optional[java.util.List[String]] = t.metaOpt("nlpcraft:sort:subjnotes")
                val subjIndexes: Optional[java.util.List[Int]] = t.metaOpt("nlpcraft:sort:subjindexes")
                val byNotes: Optional[java.util.List[String]] = t.metaOpt("nlpcraft:sort:bynotes")
                val byIndexes: Optional[java.util.List[Int]] = t.metaOpt("nlpcraft:sort:byindexes")
                val asc: Optional[Boolean] = t.metaOpt("nlpcraft:sort:asc")

                def get[T](opt: Optional[util.List[T]]) =
                    opt.asScala match {
                        case Some(list) ⇒ list.asScala
                        case None ⇒ Seq.empty
                    }

                NCTestSortToken(txt, get(subjNotes), get(subjIndexes), get(byNotes), get(byIndexes), asc.asScala)
            case "nlpcraft:relation" ⇒
                val indexes: java.util.List[Int] = t.meta("nlpcraft:relation:indexes")

                NCTestRelationToken(
                    txt,
                    `type` = t.meta("nlpcraft:relation:type"),
                    indexes = indexes.asScala,
                    note = t.meta("nlpcraft:relation:note")
                )

            case "nlpcraft:limit" ⇒
                val indexes: java.util.List[Int] = t.meta("nlpcraft:limit:indexes")
                val asc: Optional[Boolean] = t.metaOpt("nlpcraft:limit:asc")

                NCTestLimitToken(
                    txt,
                    limit = t.meta("nlpcraft:limit:limit"),
                    indexes = indexes.asScala,
                    note = t.meta("nlpcraft:limit:note"),
                    asc.asScala
                )

            case _ ⇒
                if (t.isUserDefined)
                    NCTestUserToken(txt, id)
                else
                    throw new AssertionError(s"Unsupported token: $id")
        }
    }

    def apply(text: String, isStop: Boolean): NCTestToken = NCTestNlpToken(text, isStop)
}

case class NCTestSentence(tokens: Seq[NCTestToken]) {
    override def toString = s"Sentence: ${tokens.mkString("|")}"
}

object NCTestSentence {
    def serialize(sens: Iterable[NCTestSentence]): String =
        managed(new ByteArrayOutputStream()) acquireAndGet { bos ⇒
            managed(new ObjectOutputStream(bos)) acquireAndGet { os ⇒
                os.writeObject(sens)

                os.flush()

                new String(Base64.getEncoder.encode(bos.toByteArray), UTF_8)
            }
        }

    def deserialize(s: String): Iterable[NCTestSentence] =
        managed(new ObjectInputStream(
            new ByteArrayInputStream(Base64.getDecoder.decode(s.getBytes(UTF_8))))
        ) acquireAndGet { is ⇒
            is.readObject.asInstanceOf[Iterable[NCTestSentence]]
        }
}