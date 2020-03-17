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
import java.util.{Base64, Optional}

import org.apache.nlpcraft.model.NCToken
import resource.managed

import scala.compat.java8.OptionConverters._
import scala.collection.JavaConverters._

/**
  * Tests infrastructure beans.
  */

sealed trait NCTestToken {
    def id: String
    def text: String
    def isStop: Boolean = false
}

// Simplified set of tokens data. Added only fields for validation.

// Server enrichers.
case class NCTestNlpToken(text: String, override val isStop: Boolean = false) extends NCTestToken {
    require(text != null)

    override def id: String = "nlpcraft:nlp"
    override def toString: String = s"$text(nlp)<isStop=$isStop>"
}

// Skip non-deteministric properties verification.
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
    subjNotes: Seq[String],
    subjIndexes: Seq[Int],
    byNotes: Option[Seq[String]] = None,
    byIndexes: Option[Seq[Int]] = None,
    asc: Option[Boolean] = None
) extends NCTestToken {
    require(text != null)
    require(subjNotes != null)
    require(subjNotes.nonEmpty)
    require(subjIndexes != null)
    require(subjIndexes.nonEmpty)
    require(byNotes != null)
    require(byNotes.isEmpty || byNotes.get.nonEmpty)
    require(byIndexes != null)
    require(byIndexes.isEmpty || byIndexes.get.nonEmpty)
    require(asc != null)

    override def id: String = "nlpcraft:sort"
    override def toString: String = {
        var s = s"$text(sort)" +
            s"<subjNotes=[${subjNotes.mkString(",")}]" +
            s", subjIndexes=[${subjIndexes.mkString(",")}]"

        if (byNotes.isDefined)
            s = s"$s" +
                s", byNotes=[${byNotes.get.mkString(",")}]" +
                s", byIndexes=[${byIndexes.get.mkString(",")}]"

        if (asc.isDefined)
            s = s"$s, asc=${asc.get}"

        s = s"$s>"

        s
    }
}

object NCTestSortToken {
    def apply(
        text: String,
        subjNotes: Seq[String],
        subjIndexes: Seq[Int],
        asc: Boolean
    ): NCTestSortToken = new NCTestSortToken(text, subjNotes, subjIndexes, None, None, Some(asc))

    def apply(
        text: String,
        subjNote: String,
        subjIndex: Int,
        asc: Boolean
    ): NCTestSortToken = new NCTestSortToken(text, Seq(subjNote), Seq(subjIndex), None, None, Some(asc))

    def apply(
        text: String,
        subjNote: String,
        subjIndex: Int
    ): NCTestSortToken = new NCTestSortToken(text, Seq(subjNote), Seq(subjIndex), None, None, None)

    def apply(
        text: String,
        subjNotes: Seq[String],
        subjIndexes: Seq[Int],
        byNotes: Seq[String],
        byIndexes: Seq[Int]
    ): NCTestSortToken = new NCTestSortToken(text, subjNotes, subjIndexes, Some(byNotes), Some(byIndexes), None)

    def apply(
        text: String,
        subjNote: String,
        subjIndex: Int,
        byNote: String,
        byIndex: Int): NCTestSortToken =
        new NCTestSortToken(text, Seq(subjNote), Seq(subjIndex), Some(Seq(byNote)), Some(Seq(byIndex)), None)

    def apply(
        text: String,
        subjNote: String,
        subjIndex: Int,
        byNote: String,
        byIndex: Int,
        asc: Boolean): NCTestSortToken =
        new NCTestSortToken(text, Seq(subjNote), Seq(subjIndex), Some(Seq(byNote)), Some(Seq(byIndex)), Some(asc))
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

case class NCTestAggregationToken(text: String, `type`: String, indexes: Seq[Int], note: String) extends NCTestToken {
    require(text != null)
    require(`type` != null)
    require(indexes != null)
    require(indexes.nonEmpty)
    require(note != null)

    override def id: String = "nlpcraft:aggregation"
    override def toString: String =
        s"$text(aggregation)" +
            s"<type=${`type`}" +
            s", indexes=[${indexes.mkString(",")}]" +
            s", note=$note>"
}

case class NCTestLimitToken(
    text: String,
    limit: Double,
    indexes: Seq[Int],
    note: String,
    asc: Option[Boolean] = None
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

case class NCTestUserToken(text: String, id: String) extends NCTestToken {
    require(text != null)
    require(id != null)
    override def toString: String = s"$text(user)<id=$id>"}

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
            case "nlpcraft:num" ⇒ NCTestNumericToken(
                txt,
                from = t.meta("nlpcraft:num:from"),
                to = t.meta("nlpcraft:num:to")
            )
            case "nlpcraft:date" ⇒ NCTestDateToken(txt)
            case "nlpcraft:city" ⇒ NCTestCityToken(txt, city = t.meta("nlpcraft:city:city"))
            case "nlpcraft:region" ⇒ NCTestRegionToken(txt, region = t.meta("nlpcraft:region:region"))
            case "nlpcraft:country" ⇒ NCTestCountryToken(txt, country = t.meta("nlpcraft:country:country"))
            case "nlpcraft:subcontinent" ⇒
                NCTestSubcontinentToken(txt, subcontinent = t.meta("nlpcraft:subcontinent:subcontinent"))
            case "nlpcraft:continent" ⇒
                NCTestContinentToken(txt, continent = t.meta("nlpcraft:continent:continent"))
            case "nlpcraft:metro" ⇒ NCTestMetroToken(txt, metro = t.meta("nlpcraft:metro:metro"))
            case "nlpcraft:sort" ⇒
                val subjNotes: java.util.List[String] = t.meta("nlpcraft:sort:subjnotes")
                val subjIndexes: java.util.List[Int] = t.meta("nlpcraft:sort:subjindexes")
                val byNotes: Optional[java.util.List[String]] = t.metaOpt("nlpcraft:sort:bynotes")
                val byIndexes: Optional[java.util.List[Int]] = t.metaOpt("nlpcraft:sort:byindexes")
                val asc: Optional[Boolean] = t.metaOpt("nlpcraft:sort:asc")

                def toOpt[T](lOpt: Optional[java.util.List[T]]): Option[Seq[T]] =
                    lOpt.asScala match {
                        case Some(l) ⇒ Some(l.asScala)
                        case None ⇒ None
                    }

                NCTestSortToken(txt, subjNotes.asScala, subjIndexes.asScala, toOpt(byNotes), toOpt(byIndexes), asc.asScala)
            case "nlpcraft:relation" ⇒
                val indexes: java.util.List[Int] = t.meta("nlpcraft:relation:indexes")

                NCTestRelationToken(
                    txt,
                    `type` = t.meta("nlpcraft:relation:type"),
                    indexes = indexes.asScala,
                    note = t.meta("nlpcraft:relation:note")
                )
            case "nlpcraft:aggregation" ⇒
                val indexes: java.util.List[Int] = t.meta("nlpcraft:aggregation:indexes")

                NCTestAggregationToken(
                    txt,
                    `type` = t.meta("nlpcraft:aggregation:type"),
                    indexes = indexes.asScala,
                    note = t.meta("nlpcraft:aggregation:note")
                )

            case "nlpcraft:limit" ⇒
                val indexes: java.util.List[Int] = t.meta("nlpcraft:limit:indexes")
                val asc: Optional[Boolean] = t.metaOpt("nlpcraft:sort:asc")

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