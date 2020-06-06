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

package org.apache.nlpcraft.common.nlp

import org.apache.nlpcraft.common.nlp.pos._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.language.implicitConversions

/**
  * NLP token is a collection of NLP notes associated with that token.
  */
case class NCNlpSentenceToken(
    index: Int,
    private val notes: mutable.HashSet[NCNlpSentenceNote] = mutable.HashSet.empty[NCNlpSentenceNote],
    stopsReasons: mutable.HashSet[NCNlpSentenceNote] = mutable.HashSet.empty[NCNlpSentenceNote]
) extends java.io.Serializable {
    @transient
    private var nlpNote: NCNlpSentenceNote = _

    // Shortcuts for some frequently used *mandatory* notes.
    def normText: String = getNlpValue[String]("normText")
    def startCharIndex: Int = getNlpValue[Int]("start").intValue() // Start character index.
    def endCharIndex: Int = getNlpValue[Int]("end").intValue() // End character index.
    def origText: String = getNlpValue[String]("origText")
    def words: Int = origText.split(" ").length
    def wordLength: Int = getNlpValue[Int]("wordLength").intValue()
    def wordIndexes: Seq[Int] = getNlpValue[java.util.List[Int]]("wordIndexes").asScala
    def pos: String = getNlpValue[String]("pos")
    def posDesc: String = getNlpValue[String]( "posDesc")
    def lemma: String = getNlpValue[String]("lemma")
    def stem: String = getNlpValue[String]("stem")
    def isStopWord: Boolean = getNlpValue[Boolean]("stopWord")
    def isBracketed: Boolean = getNlpValue[Boolean]("bracketed")
    def isDirect: Boolean = getNlpValue[Boolean]("direct")
    def isQuoted: Boolean = getNlpValue[Boolean]("quoted")
    def isSynthetic: Boolean = NCPennTreebank.isSynthetic(pos)
    def isKnownWord: Boolean = getNlpValue[Boolean]("dict")
    def isSwearWord: Boolean = getNlpValue[Boolean]("swear")
    def isEnglish: Boolean = getNlpValue[Boolean]("english")

    /**
      *
      * @param noteType Note type.
      */
    def getNotes(noteType: String): Iterable[NCNlpSentenceNote] = notes.filter(_.noteType == noteType)

    /**
      * Clones note.
      * Shallow copy.
      */
    def clone(index: Int): NCNlpSentenceToken =
        NCNlpSentenceToken(
            index,
            {
                val m = mutable.HashSet.empty[NCNlpSentenceNote]

                notes.foreach(n ⇒ m += n.clone())

                m
            },
            stopsReasons.clone()
        )

    /**
      * Clones note.
      * Shallow copy.
      */
    override def clone(): NCNlpSentenceToken = clone(index)

    /**
      * Removes note with given ID. No-op if ID wasn't found.
      *
      * @param note Note.
      */
    def remove(note: NCNlpSentenceNote): Unit = notes.remove(note)

    /**
      * Tests whether or not this token contains note.
      * It is important to convert notes to set each time,
      * because otherwise note cannot be found because its content changed and its hashCode changed too.
      * https://stackoverflow.com/questions/43553806/hashset-contains-returns-false-when-it-shouldnt/43554123
      */
    def contains(note: NCNlpSentenceNote): Boolean = notes.contains(note)

    /**
      *
      * @param noteType Note type.
      * @param noteName Note name.
      */
    def getNoteOpt(noteType: String, noteName: String): Option[NCNlpSentenceNote] = {
        val ns = getNotes(noteType).filter(_.contains(noteName))

        ns.size match {
            case 0 ⇒ None
            case 1 ⇒ Some(ns.head)
            case _ ⇒
                throw new AssertionError(
                    s"Multiple notes found [type=$noteType, name=$noteName, token=$notes]"
                )
        }
    }

    /**
      * Gets note with given type and name.
      *
      * @param noteType Note type.
      * @param noteName Note name.
      */
    def getNote(noteType: String, noteName: String): NCNlpSentenceNote =
        getNoteOpt(noteType, noteName) match {
            case Some(n) ⇒ n
            case None ⇒
                throw new AssertionError(s"Note not found [type=$noteType, name=$noteName, token=$notes]")
        }

    /**
      * Gets NLP note.
      */
    def getNlpNote: NCNlpSentenceNote = {
        if (nlpNote == null)
            nlpNote = notes.find(_.isNlp).orNull

        nlpNote
    }

    /**
      *
      * @param noteName Note name.
      * @tparam T Type of the note value.
      */
    def getNlpValueOpt[T: Manifest](noteName: String): Option[T] =
        getNlpNote.get(noteName) match {
            case Some(v) ⇒ Some(v.asInstanceOf[T])
            case None ⇒ None
        }

    /**
      *
      * @param noteName Note name.
      * @tparam T Type of the note value.
      */
    def getNlpValue[T: Manifest](noteName: String): T = getNlpNote(noteName).asInstanceOf[T]

    /**
      * Tests if this token has any notes of given type(s).
      *
      * @param types Note type(s) to check.
      */
    def isTypeOf(types: String*): Boolean = types.exists(t ⇒ getNotes(t).nonEmpty)

    /**
      * Adds element.
      *
      * @param note Element.
      */
    def add(note: NCNlpSentenceNote): Unit = {
        val added = notes.add(note)

        if (added && note.isNlp)
            nlpNote = note
    }

    /**
      * Simple word is a non synthetic word that's also not part of any domain-specific note type.
      */
    def isNlp: Boolean = notes.forall(_.isNlp)

    /**
      *
      * @return
      */
    def isUser: Boolean = notes.exists(_.isUser)

    /**
      *
      * @param reason
      */
    def addStopReason(reason: NCNlpSentenceNote): Unit = stopsReasons += reason

    override def toString: String =
        notes.toSeq.sortBy(t ⇒ (if (t.isNlp) 0 else 1, t.noteType)).mkString("NLP token [", "|", "]")
}

object NCNlpSentenceToken {
    /**
     * To immutable iterator.
     */
    implicit def notes(x: NCNlpSentenceToken): Iterable[NCNlpSentenceNote] = x.notes.toSet
}
