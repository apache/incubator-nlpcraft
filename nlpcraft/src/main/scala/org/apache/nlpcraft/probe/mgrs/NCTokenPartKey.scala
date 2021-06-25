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

package org.apache.nlpcraft.probe.mgrs

import org.apache.nlpcraft.common.TOK_META_ALIASES_KEY
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote, NCNlpSentenceToken}
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.probe.mgrs.NCProbeSynonymChunkKind.{NCSynonymChunkKind, _}

import java.io.{Serializable => JSerializable}
import java.util
import java.util.{List => JList}
import scala.compat.java8.OptionConverters.RichOptionalGeneric
import scala.jdk.CollectionConverters.{MapHasAsJava, MapHasAsScala}
import scala.language.implicitConversions
import scala.collection.mutable

/**
  *
  */
object NCTokenPartKey {
    def apply(m: util.HashMap[String, JSerializable]): NCTokenPartKey = {
        def get[T](name: String): T = m.get(name).asInstanceOf[T]

        NCTokenPartKey(get("id"), get("startcharindex"), get("endcharindex"), get("data"))
    }

    def apply(part: NCToken, kind: NCSynonymChunkKind): NCTokenPartKey = {
        val id = part.getId

        val m: Map[String, Any] =
            if (kind != TEXT)
                id match {
                    case "nlpcraft:relation" =>
                        Map(
                            "type" -> part.meta[String](s"$id:type"),
                            "note" -> part.meta[String](s"$id:note")
                        )
                    case "nlpcraft:limit" =>
                        Map(
                            "limit" -> part.meta[Double](s"$id:limit"),
                            "note" -> part.meta[String](s"$id:note")
                        )
                    case "nlpcraft:sort" =>
                        val m = mutable.HashMap.empty[String, Any]

                        def add(name: String): Unit =
                            part.metaOpt[JList[String]](s"$id:$name").asScala match {
                                case Some(list) => m += name -> list
                                case None => // No-op.
                            }

                        add("subjnotes")
                        add("bynotes")

                        m.toMap
                    case _ => Map.empty
                }
            else
                Map.empty

        val key = new NCTokenPartKey(
            if (kind == TEXT) "nlpcraft:nlp" else id,
            part.getStartCharIndex,
            part.getEndCharIndex,
            m.asJava
        )

        key.aliases = part.getMetadata.get(TOK_META_ALIASES_KEY)

        key
    }

    def apply(t: NCToken): NCTokenPartKey =
        new NCTokenPartKey(t.getId, t.getStartCharIndex, t.getEndCharIndex, Map.empty[String, Any].asJava)

    def apply(note: NCNlpSentenceNote, sen: NCNlpSentence): NCTokenPartKey =
        NCTokenPartKey(
            note.noteType,
            sen(note.tokenFrom).startCharIndex,
            sen(note.tokenTo).endCharIndex,
            Map.empty[String, Any].asJava
        )

    def apply(note: NCNlpSentenceNote, toks: Seq[NCNlpSentenceToken]): NCTokenPartKey = {
        val sorted = toks.sortBy(_.index)

        NCTokenPartKey(
            note.noteType,
            sorted.head.startCharIndex,
            sorted.last.endCharIndex,
            Map.empty[String, Any].asJava
        )
    }
}

/**
  *
  * @param id
  * @param from
  * @param to
  * @param data
  */
case class NCTokenPartKey(id: String, from: Int, to: Int, data: util.Map[String, Any]) {
    require(from <= to)

    var aliases: AnyRef = _

    private def in(i: Int): Boolean = i >= from && i <= to

    def intersect(id: String, from: Int, to: Int): Boolean = id == this.id && (in(from) || in(to))

    def similar(note: NCNlpSentenceNote): Boolean =
        id == note.noteType &&
        (
            data.isEmpty ||
            data.asScala.forall { case (k, v) => note.contains(k) && note.data(k) == v }
        )
}