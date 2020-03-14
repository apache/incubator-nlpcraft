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
    notes: mutable.HashMap[String, NCNlpSentenceNote] = mutable.HashMap.empty[String, NCNlpSentenceNote],
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
    def getNotes(noteType: String): Iterable[NCNlpSentenceNote] = notes.values.filter(_.noteType == noteType)

    /**
      * Clones note.
      * Shallow copy.
      */
    def clone(index: Int): NCNlpSentenceToken =
        NCNlpSentenceToken(
            index,
            {
                val m = mutable.HashMap.empty[String, NCNlpSentenceNote]

                notes.foreach { case (key, note) ⇒ m += key → note.clone() }

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
      * @param id Note ID.
      */
    def remove(id: String): Unit = notes -= id

    /**
      * Removes notes with given IDs. No-op if ID wasn't found.
      *
      * @param ids Note IDs.
      */
    def remove(ids: Iterable[String]): Unit = notes --= ids

    /**
      * Tests whether or not this token contains note with given ID.
      */
    def contains(id: String): Boolean = notes.contains(id)

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
            nlpNote = notes.values.find(_.isNlp).orNull

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
      * @param elem Element.
      */
    def add(elem: NCNlpSentenceNote): Unit = {
        notes += elem.id → elem

        if (elem.isNlp)
            nlpNote = elem
    }

    /**
      * Simple word is a non synthetic word that's also not part of any domain-specific note type.
      */
    def isNlp: Boolean = this.forall(_.isNlp)

    /**
      *
      * @return
      */
    def isUser: Boolean = this.exists(_.isUser)

    /**
      *
      * @param reason
      */
    def addStopReason(reason: NCNlpSentenceNote): Unit = stopsReasons += reason

    /**
      *
      */
    def markAsStop(): Unit = getNlpNote += "stopWord" → true

    override def toString: String =
        notes.values.toSeq.sortBy(t ⇒ (if (t.isNlp) 0 else 1, t.noteType)).mkString("NLP token [", "|", "]")
}

object NCNlpSentenceToken {
    implicit def toNotes(x: NCNlpSentenceToken): Iterable[NCNlpSentenceNote] = x.notes.values
}
