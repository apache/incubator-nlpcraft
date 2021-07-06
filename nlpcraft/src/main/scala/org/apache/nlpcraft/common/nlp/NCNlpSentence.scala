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

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.server.mdo.NCCtxWordConfigMdo

import java.io.{Serializable => JSerializable}
import java.util.{Collections, List => JList}
import scala.collection.mutable
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.language.implicitConversions

object NCNlpSentence {
    case class NoteKey(start: Int, end: Int)
    case class TokenKey(id: String, start: Int, end: Int)
    case class NoteLink(note: String, indexes: Seq[Int])
}

import org.apache.nlpcraft.common.nlp.NCNlpSentence._

/**
  * Parsed NLP sentence is a collection of tokens. Each token is a collection of notes and
  * each note is a collection of KV pairs.
  *
  * @param srvReqId Server request ID.
  * @param text Normalized text.
  * @param enabledBuiltInToks Enabled built-in tokens.
  * @param ctxWordConfig Machine learning configuration. Optional.
  * @param tokens Initial buffer.
  * @param firstProbePhase Processing phase flag.
  * @param deletedNotes Deleted overridden notes with their tokens.
  * @param initNlpNotes Initial NLP tokens.
  * @param nlpTokens NLP tokens.
  */
class NCNlpSentence(
    val srvReqId: String,
    val text: String,
    val enabledBuiltInToks: Set[String],
    val ctxWordConfig: Option[NCCtxWordConfigMdo] = None,
    var ctxWordCategories: Map[/** Token index*/Int, Map[/** Elements ID*/String, /** Confidence*/Double]] = Map.empty,
    override val tokens: mutable.ArrayBuffer[NCNlpSentenceToken] = new mutable.ArrayBuffer[NCNlpSentenceToken](32),
    var firstProbePhase: Boolean = true,
    private val deletedNotes: mutable.HashMap[NCNlpSentenceNote, Seq[NCNlpSentenceToken]] = mutable.HashMap.empty,
    private var initNlpNotes: Map[NoteKey, NCNlpSentenceNote] = null,
    private val nlpTokens: mutable.HashMap[TokenKey, NCNlpSentenceToken] = mutable.HashMap.empty
) extends NCNlpSentenceTokenBuffer(tokens) with JSerializable {
    @transient
    private var hash: java.lang.Integer = _

    private def calcHash(): Int = U.mkJavaHash(srvReqId, text, enabledBuiltInToks, tokens)

    // Deep copy.
    override def clone(): NCNlpSentence =
        new NCNlpSentence(
            srvReqId = srvReqId,
            text = text,
            enabledBuiltInToks = enabledBuiltInToks,
            ctxWordConfig = ctxWordConfig,
            tokens = tokens.map(_.clone()),
            deletedNotes = deletedNotes.map(p => p._1.clone() -> p._2.map(_.clone())),
            initNlpNotes = initNlpNotes,
            nlpTokens = nlpTokens,
            firstProbePhase = firstProbePhase
        )

    /**
      * Utility method that gets set of notes for given note type collected from
      * tokens in this sentence. Notes are sorted in the same order they appear
      * in this sentence.
      *
      * @param noteType Note type.
      */
    def getNotes(noteType: String): Seq[NCNlpSentenceNote] = this.flatMap(_.getNotes(noteType)).toSeq.distinct

    /**
      * Utility method that removes note with given ID from all tokens in this sentence.
      * No-op if such note wasn't found.
      *
      * @param note Note.
      */
    def removeNote(note: NCNlpSentenceNote): Unit = this.foreach(_.remove(note))

    //noinspection HashCodeUsesVar
    override def hashCode(): Int = {
        if (hash == null)
            hash = calcHash()

        hash
    }

    override def equals(obj: Any): Boolean = obj match {
        case x: NCNlpSentence =>
            tokens == x.tokens &&
                srvReqId == x.srvReqId &&
                text == x.text &&
                enabledBuiltInToks == x.enabledBuiltInToks

        case _ => false
    }

    /**
      *
      * @param note
      * @param kvs
      */
    def fixNote(note: NCNlpSentenceNote, kvs: (String, JSerializable)*): Unit = {
        val fixed = note.clone(kvs: _*)

        this.filter(t => t.index >= fixed.tokenIndexes.head && t.index <= fixed.tokenIndexes.last).foreach(t => {
            t.remove(note)
            t.add(fixed)
        })

        hash = null
    }

    /**
      * Returns flag are note notes equal (or similar) or not. Reason of ignored difference can be stopwords tokens.
      *
      * @param n1 First note.
      * @param n2 Second note.
      */
    def notesEqualOrSimilar(n1: NCNlpSentenceNote, n2: NCNlpSentenceNote): Boolean =
        if (n1.noteType != n2.noteType)
            false
        else {
            val stopIdxs = this.filter(_.isStopWord).map(_.index)

            // One possible difference - stopwords indexes.
            def wordsEqualOrSimilar0(n1: NCNlpSentenceNote, n2: NCNlpSentenceNote): Boolean = {
                val set1 = n1.wordIndexes.toSet
                val set2 = n2.wordIndexes.toSet

                set1 == set2 || set1.subsetOf(set2) && set2.diff(set1).forall(stopIdxs.contains)
            }

            def wordsEqualOrSimilar(n1: NCNlpSentenceNote, n2: NCNlpSentenceNote): Boolean =
                wordsEqualOrSimilar0(n1, n2) || wordsEqualOrSimilar0(n2, n1)

            def tokensEqualOrSimilar0(set1: Set[NCNlpSentenceToken], set2: Set[NCNlpSentenceToken]): Boolean =
                set1 == set2 || set1.subsetOf(set2) && set2.diff(set1).forall(_.isStopWord)

            def tokensEqualOrSimilar(set1: Set[NCNlpSentenceToken], set2: Set[NCNlpSentenceToken]): Boolean =
                tokensEqualOrSimilar0(set1, set2) || tokensEqualOrSimilar0(set2, set1)

            def getList(n: NCNlpSentenceNote, refIdxName: String): Set[NCNlpSentenceToken] =
                n.getOrElse(refIdxName, Collections.emptyList).asInstanceOf[JList[Int]].asScala.
                    map(this (_)).toSet

            def getListList(n: NCNlpSentenceNote, refIdxName: String): Set[NCNlpSentenceToken] =
                n.getOrElse(refIdxName, Collections.emptyList).asInstanceOf[JList[JList[Int]]].asScala.
                    flatMap(_.asScala.map(this (_))).toSet

            def referencesEqualOrSimilar0(n1: NCNlpSentenceNote, n2: NCNlpSentenceNote): Boolean = {
                require(n1.noteType == n2.noteType)

                n1.noteType match {
                    case "nlpcraft:sort" =>
                        tokensEqualOrSimilar(getListList(n1, "subjindexes"), getListList(n2, "subjindexes")) &&
                            tokensEqualOrSimilar(getListList(n1, "byindexes"), getListList(n2, "byindexes"))
                    case "nlpcraft:limit" =>
                        tokensEqualOrSimilar(getList(n1, "indexes"), getList(n2, "indexes"))
                    case "nlpcraft:reference" =>
                        tokensEqualOrSimilar(getList(n1, "indexes"), getList(n2, "indexes"))

                    case _ => true
                }
            }

            def referencesEqualOrSimilar(n1: NCNlpSentenceNote, n2: NCNlpSentenceNote): Boolean =
                referencesEqualOrSimilar0(n1, n2) || referencesEqualOrSimilar0(n2, n1)

            def getUniqueKey0(n: NCNlpSentenceNote): Seq[Any] = n.getKey(withIndexes = false, withReferences = false)

            getUniqueKey0(n1) == getUniqueKey0(n2) && wordsEqualOrSimilar(n1, n2) && referencesEqualOrSimilar(n1, n2)
        }

    /**
      *
      */
    def saveNlpNotes(): Unit =
        initNlpNotes = this.map(t => NoteKey(t.startCharIndex, t.endCharIndex) -> t.getNlpNote).toMap

    /**
      *
      * @return
      */
    def getInitialNlpNote(startCharIndex: Int, endCharIndex: Int): Option[NCNlpSentenceNote] =
        initNlpNotes.get(NoteKey(startCharIndex, endCharIndex))

    /**
      *
      * @param nlp
      */
    def addNlpToken(nlp: NCNlpSentenceToken): Unit = {
        require(nlp.size <= 2)

        nlp.foreach(n => nlpTokens += TokenKey(n.noteType, nlp.startCharIndex, nlp.endCharIndex) -> nlp)
    }

    /**
      *
      * @param noteType
      * @param startCharIndex
      * @param endCharIndex
      * @return
      */
    def getNlpToken(noteType: String, startCharIndex: Int, endCharIndex: Int): Option[NCNlpSentenceToken] =
        nlpTokens.get(TokenKey(noteType, startCharIndex, endCharIndex))

    /**
      *
      */
    def getDeletedNotes: Predef.Map[NCNlpSentenceNote, Seq[NCNlpSentenceToken]] = deletedNotes.toMap

    /***
      *
      * @param deletedNotes
      */
    def addDeletedNotes(deletedNotes: Map[NCNlpSentenceNote, Seq[NCNlpSentenceToken]]): Unit =
       this.deletedNotes ++= deletedNotes
}
