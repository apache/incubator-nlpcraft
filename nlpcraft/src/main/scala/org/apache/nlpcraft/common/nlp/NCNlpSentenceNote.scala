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

package org.apache.nlpcraft.common.nlp

import org.apache.nlpcraft.common.U
import org.apache.nlpcraft.common.ascii._

import java.io.{Serializable => JSerializable}
import java.util.{List => JList}

import scala.collection.mutable
import scala.language.implicitConversions
import scala.jdk.CollectionConverters.{CollectionHasAsScala, SeqHasAsJava}

/**
  * Sentence token note is a typed map of KV pairs.
  */
class NCNlpSentenceNote(private val values: Map[String, JSerializable]) extends JSerializable with NCAsciiLike {
    import NCNlpSentenceNote._

    private lazy val dataWithoutIndexes = this.filter(p => !SKIP_CLONE.contains(p._1))
    private lazy val skipNlp = dataWithoutIndexes.filter { case (key, _) => key != "noteType" }

    @transient
    private lazy val hash = values.hashCode()

    // Shortcuts for mandatory fields. (Immutable fields)
    lazy val noteType: String = values("noteType").asInstanceOf[String]
    lazy val tokenFrom: Int = values("tokMinIndex").asInstanceOf[Int] // First index.
    lazy val tokenTo: Int = values("tokMaxIndex").asInstanceOf[Int] // Last index.
    lazy val tokenIndexes: Seq[Int] = values("tokWordIndexes").asInstanceOf[JList[Int]].asScala.toSeq // Includes 1st and last indices too.
    lazy val wordIndexes: Seq[Int] = values("wordIndexes").asInstanceOf[JList[Int]].asScala.toSeq // Includes 1st and last indices too.
    lazy val wordIndexesSet: Set[Int] = wordIndexes.toSet
    lazy val sparsity: Int = values("sparsity").asInstanceOf[Int]
    lazy val isDirect: Boolean = values("direct").asInstanceOf[Boolean]
    lazy val isUser: Boolean = {
        val i = noteType.indexOf(":")

        if (i > 0) !TOK_PREFIXES.contains(noteType.take(i)) else true
    }

    lazy val isSystem: Boolean = !isUser
    lazy val isNlp: Boolean = noteType == "nlpcraft:nlp"

    // Typed getter.
    def data[T](key: String): T = values(key).asInstanceOf[T]
    def dataOpt[T](key: String): Option[T] = values.get(key).asInstanceOf[Option[T]]

    override def equals(obj: Any): Boolean = obj match {
        case h: NCNlpSentenceNote => h.hashCode() == hashCode() && h.values == values
        case _ => false
    }

    override def hashCode(): Int = hash

    /**
      * Clones this note.
      */
    def clone(indexes: Seq[Int], wordIndexes: Seq[Int], params: (String, JSerializable)*): NCNlpSentenceNote =
        apply(
            indexes,
            Some(wordIndexes),
            noteType,
            dataWithoutIndexes ++ params.toMap
        )

    override def clone(): NCNlpSentenceNote = new NCNlpSentenceNote(values)

    /**
      *
      * @param n
      */
    def equalsWithoutIndexes(n: NCNlpSentenceNote): Boolean =
        this.noteType == n.noteType &&
        this.wordIndexes.size == n.wordIndexes.size &&
        this.wordIndexes.zip(n.wordIndexes).map(p => p._1 - p._2).distinct.size == 1 &&
        this.dataWithoutIndexes == n.dataWithoutIndexes

    /**
      *
      * @return
      */
    override def toAscii: String =
        values.iterator.toSeq.sortBy(_._1).foldLeft(NCAsciiTable("Key", "Value"))((t, p) => t += p).toString

    /**
      *
      * @return
      */
    def asMetadata(): Map[String, JSerializable] =
        if (isUser)
            values.get("meta") match {
                case Some(meta) => meta.asInstanceOf[Map[String, JSerializable]]
                case None => Map.empty[String, JSerializable]
            }
        else {
            val md = mutable.Map.empty[String, JSerializable]

            val m = if (noteType != "nlpcraft:nlp") skipNlp else values

            m.foreach { case (name, value) => md += (name.toLowerCase() -> value)}

            md.toMap
        }

    /**
     *
     * @param kvs
     */
    def clone(kvs : (String, JSerializable)*): NCNlpSentenceNote =
        new NCNlpSentenceNote(values ++ kvs)

    /**
      *
      * @param withIndexes
      * @param withReferences
      * @return
      */
    def getKey(withIndexes: Boolean = true, withReferences: Boolean = true): Seq[Any] = {
        val seq1 = if (withIndexes) Seq(wordIndexes, noteType) else Seq(noteType)
        val seq2 = if (isUser)
            Seq.empty
        else
            getBuiltProperties(noteType, withReferences).map(name => this.getOrElse(name, null))

        seq1 ++ seq2
    }

    /**
      *
      * @return
      */
    override def toString: String =
        values.toSeq.sortBy(t => { // Don't show internal ID.
            val typeSort = t._1 match {
                case "noteType" => 0
                case "origText" => 1
                case "wordIndexes" => 2
                case "direct" => 3
                case "sparsity" => 4
                case "parts" => 5

                case _ => 100
            }
            (typeSort, t._1)
        }).map(p => s"${p._1}=${p._2}").mkString("NLP note [", ", ", "]")
}

object NCNlpSentenceNote {
    // These properties should be cloned as they are auto-set when new clone
    // is created.
    private final val SKIP_CLONE: Set[String] = Set(
        "minIndex",
        "maxIndex",
        "wordIndexes",
        "wordLength",
        "tokMinIndex",
        "tokMaxIndex",
        "tokWordIndexes",
        "sparsity"
    )

    private final val TOK_PREFIXES = Set("nlpcraft", "google", "opennlp", "stanford", "spacy")

    /**
     * To immutable map.
     */
    implicit def values(note: NCNlpSentenceNote): Map[String, JSerializable] = note.values

    /**
      * Creates new note with given parameters.
      *
      * @param indexes Indexes in the sentence.
      * @param wordIndexesOpt Word indexes. Optional.
      * @param typ Type of the node.
      * @param params Parameters.
      */
    def apply(
        indexes: Seq[Int],
        wordIndexesOpt: Option[Seq[Int]],
        typ: String,
        params: Map[String, Any]
    ): NCNlpSentenceNote = {
        def calc(seq: Seq[Int]): (Int, Int, Int, JList[Int], Int) =
            (U.calcSparsity(seq), seq.min, seq.max, seq.asJava, seq.length)

        val (sparsity, tokMinIndex, tokMaxIndex, tokWordIndexes, len) = calc(wordIndexesOpt.getOrElse(indexes))

        new NCNlpSentenceNote(
            params.filter(_._2 != null).map(p => p._1 -> p._2.asInstanceOf[JSerializable]) ++
            Map[String, JSerializable](
               "noteType" -> typ,
               "tokMinIndex" -> indexes.min,
               "tokMaxIndex" -> indexes.max,
               "tokWordIndexes" -> indexes.asJava.asInstanceOf[JSerializable],
               "minIndex" -> tokMinIndex,
               "maxIndex" -> tokMaxIndex,
               "wordIndexes" -> tokWordIndexes.asInstanceOf[JSerializable],
               "wordLength" -> len,
               "sparsity" -> sparsity
            )
        )
    }

    /**
      * Creates new note with given parameters.
      *
      * @param indexes Indexes in the sentence.
      * @param typ Type of the node.
      * @param params Parameters.
      */
    def apply(indexes: Seq[Int], typ: String, params: (String, Any)*): NCNlpSentenceNote =
        apply(indexes, None, typ, params.toMap)

    /**
     * Creates new note with given parameters.
     *
     * @param indexes Indexes in the sentence.
     * @param typ Type of the node.
     * @param params Parameters.
     */
    def apply(indexes: mutable.Seq[Int], typ: String, params: (String, Any)*): NCNlpSentenceNote =
        apply(indexes.toSeq, None, typ, params.toMap)

    /**
      * Creates new note with given parameters.
      *
      * @param indexes Indexes in the sentence.
      * @param wordIndexes Word indexes in the sentence.
      * @param typ Type of the node.
      * @param params Parameters.
      */
    def apply(indexes: Seq[Int], wordIndexes: Seq[Int], typ: String, params: (String, Any)*): NCNlpSentenceNote =
        apply(indexes, Some(wordIndexes), typ, params.toMap)

    /**
     * Creates new note with given parameters.
     *
     * @param indexes Indexes in the sentence.
     * @param wordIndexes Word indexes in the sentence.
     * @param typ Type of the node.
     * @param params Parameters.
     */
    def apply(indexes: mutable.Seq[Int], wordIndexes: mutable.Seq[Int], typ: String, params: (String, Any)*): NCNlpSentenceNote =
        apply(indexes.toSeq, Some(wordIndexes.toSeq), typ, params.toMap)

    /**
      *
      * @param noteType
      * @param withReferences
      */
    def getBuiltProperties(noteType: String, withReferences: Boolean = true): Seq[String] = {
        def addRefs(names: String*): Seq[String] = if (withReferences) names else Seq.empty

        noteType match {
            case "nlpcraft:nlp" => Seq.empty

            case "nlpcraft:continent" => Seq("continent")
            case "nlpcraft:subcontinent" => Seq("continent", "subcontinent")
            case "nlpcraft:country" => Seq("continent", "subcontinent", "country")
            case "nlpcraft:region" => Seq("continent", "subcontinent", "country", "region")
            case "nlpcraft:city" => Seq("continent", "subcontinent", "country", "region", "city")
            case "nlpcraft:metro" => Seq("metro")
            case "nlpcraft:date" => Seq("from", "to")
            case "nlpcraft:relation" => Seq("type", "note") ++ addRefs("indexes")
            case "nlpcraft:sort" => Seq("asc", "subjnotes", "bynotes") ++ addRefs("subjindexes", "byindexes")
            case "nlpcraft:limit" => Seq("limit", "note") ++ addRefs("indexes", "asc") // Asc flag has sense only with references for limit.
            case "nlpcraft:coordinate" => Seq("latitude", "longitude")
            case "nlpcraft:num" => Seq("from", "to", "unit", "unitType")
            case x if x.startsWith("google:") => Seq("meta", "mentionsBeginOffsets", "mentionsContents", "mentionsTypes")
            case x if x.startsWith("stanford:") => Seq("nne")
            case x if x.startsWith("opennlp:") => Seq.empty
            case x if x.startsWith("spacy:") => Seq("vector")

            case _ => throw new AssertionError(s"Unexpected note type: $noteType")
        }
    }
}
