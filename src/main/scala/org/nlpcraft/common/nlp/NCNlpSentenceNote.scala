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

package org.nlpcraft.common.nlp


import org.nlpcraft.common._
import org.nlpcraft.common.ascii._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.language.implicitConversions

/**
  * Sentence token note is a typed map of KV pairs.
  *
  * @param id Internal ID.
  */
class NCNlpSentenceNote(
    val id: String,
    val values: mutable.HashMap[String, java.io.Serializable] = mutable.HashMap[String, java.io.Serializable]()
) extends java.io.Serializable with NCAsciiLike {
    import NCNlpSentenceNote._

    private val hash: Int = id.hashCode()

    this.put("unid", this.id)

    // Shortcuts for mandatory fields. (Immutable fields)
    lazy val noteType: String = this("noteType").asInstanceOf[String]
    lazy val tokenFrom: Int = this("tokMinIndex").asInstanceOf[Int] // First index.
    lazy val tokenTo: Int = this("tokMaxIndex").asInstanceOf[Int] // Last index.
    lazy val tokenIndexes: Seq[Int] = this("tokWordIndexes").asInstanceOf[java.util.List[Int]].asScala // Includes 1st and last indices too.
    lazy val wordIndexes: Seq[Int] = this("wordIndexes").asInstanceOf[java.util.List[Int]].asScala // Includes 1st and last indices too.
    lazy val sparsity: Int = this("sparsity").asInstanceOf[Int]
    lazy val isContiguous: Boolean = this("contiguous").asInstanceOf[Boolean]
    lazy val isDirect: Boolean = this("direct").asInstanceOf[Boolean]
    lazy val isUser: Boolean = {
        val i = noteType.indexOf(":")

        if (i > 0) !TOK_PREFIXES.contains(noteType.take(i)) else true
    }

    lazy val isSystem: Boolean = !isUser
    lazy val isNlp: Boolean = noteType == "nlpcraft:nlp"

    // Typed getter.
    def data[T](key: String): T = this(key).asInstanceOf[T]
    def dataOpt[T](key: String): Option[T] = this.get(key).asInstanceOf[Option[T]]

    override def equals(obj: Any): Boolean = obj match {
        case h: NCNlpSentenceNote ⇒ h.hash == hash && h.id == id
        case _ ⇒ false
    }

    override def hashCode(): Int = hash

    /**
      * Clones this note.
      */
    def clone(indexes: Seq[Int], wordIndexes: Seq[Int], params: (String, Any)*): NCNlpSentenceNote =
        NCNlpSentenceNote(
            id,
            indexes,
            Some(wordIndexes),
            noteType,
            this.filter(p ⇒ !SKIP_CLONE.contains(p._1)).toSeq ++ params:_*
        )

    override def clone(): NCNlpSentenceNote = new NCNlpSentenceNote(id, values.clone())

    /**
      *
      * @return
      */
    override def toAscii: String =
        this.iterator.toSeq.sortBy(_._1).foldLeft(NCAsciiTable("Key", "Value"))((t, p) ⇒ t += p).toString

    /**
      *
      * @return
      */
    def skipNlp(): Map[String, java.io.Serializable] =
        this.filter { case (key, _) ⇒ !SKIP_CLONE.contains(key) && key != "noteType" }.toMap

    /**
      *
      */
    def asMetadata(): Map[String, java.io.Serializable] =
        if (isUser)
            this.get("meta") match {
                case Some(meta) ⇒ meta.asInstanceOf[Map[String, java.io.Serializable]]
                case None ⇒ Map.empty[String, java.io.Serializable]
            }
        else {
            val md = mutable.Map.empty[String, java.io.Serializable]

            val m = if (noteType != "nlpcraft:nlp") skipNlp() else this.toMap

            m.foreach { case (name, value) ⇒ md += (name.toLowerCase() → value)}

            md.toMap
        }

    /**
      *
      * @return
      */
    override def toString: String =
        this.toSeq.filter(_._1 != "unid").sortBy(t ⇒ { // Don't show internal ID.
            val typeSort = t._1 match {
                case "noteType" ⇒ 1
                case _ ⇒ Math.abs(t._1.hashCode)
            }
            (typeSort, t._1)
        }).map(p ⇒ s"${p._1}=${p._2}").mkString("NLP note [", ", ", "]")
}

object NCNlpSentenceNote {
    // These properties should be cloned as they are auto-set when new clone
    // is created.
    private final val SKIP_CLONE: Set[String] = Set(
        "unid",
        "minIndex",
        "maxIndex",
        "wordIndexes",
        "wordLength",
        "tokMinIndex",
        "tokMaxIndex",
        "tokWordIndexes",
        "contiguous",
        "sparsity"
    )

    private final val TOK_PREFIXES = Set("nlpcraft", "google", "opennlp", "stanford", "spacy")

    implicit def getValues(x: NCNlpSentenceNote): mutable.HashMap[String, java.io.Serializable] = x.values

    /**
      * Creates new note with given parameters.
      *
      * @param id Internal ID.
      * @param indexes Indexes in the sentence.
      * @param wordIndexesOpt Word indexes. Optional.
      * @param typ Type of the node.
      * @param params Parameters.
      */
    def apply(
        id: String,
        indexes: Seq[Int],
        wordIndexesOpt: Option[Seq[Int]],
        typ: String,
        params: (String, Any)*
    ): NCNlpSentenceNote = {
        def calc(seq: Seq[Int]): (Int, Int, Int, java.util.List[Int], Int) =
            (U.calcSparsity(seq), seq.min, seq.max, seq.asJava, seq.length)

        val (sparsity, tokMinIndex, tokMaxIndex, tokWordIndexes, len) = calc(wordIndexesOpt.getOrElse(indexes))

        new NCNlpSentenceNote(
            id,
            mutable.HashMap[String, java.io.Serializable]((
            params.filter(_._2 != null) :+
               ("noteType" → typ) :+
               ("tokMinIndex" → indexes.min) :+
               ("tokMaxIndex" → indexes.max) :+
               ("tokWordIndexes" → indexes.asJava) :+
               ("minIndex" → tokMinIndex) :+
               ("maxIndex" → tokMaxIndex) :+
               ("wordIndexes" → tokWordIndexes) :+
               ("wordLength" → len) :+
               ("sparsity" → sparsity) :+
               ("contiguous" → (sparsity == 0))
            ).map(p ⇒ p._1 → p._2.asInstanceOf[java.io.Serializable]): _*)
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
        apply(U.genGuid(), indexes, None, typ, params: _*)

    /**
      * Creates new note with given parameters.
      *
      * @param id ID.
      * @param indexes Indexes in the sentence.
      * @param wordIndexes Word indexes in the sentence.
      * @param typ Type of the node.
      * @param params Parameters.
      */
    def apply(id: String, indexes: Seq[Int], wordIndexes: Seq[Int], typ: String, params: (String, Any)*): NCNlpSentenceNote =
        apply(id, indexes, Some(wordIndexes), typ, params: _*)
}
