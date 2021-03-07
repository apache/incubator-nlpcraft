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

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.common.NCE
import org.apache.nlpcraft.common.nlp.pos.NCPennTreebank
import org.apache.nlpcraft.common.util.NCComboRecursiveTask
import org.apache.nlpcraft.model.NCModel

import java.io.{Serializable => JSerializable}
import java.util
import java.util.concurrent.ForkJoinPool
import java.util.{Collections, Comparator, List => JList}
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.collection.{Map, Seq, Set, mutable}
import scala.language.implicitConversions

object NCNlpSentence extends LazyLogging {
    implicit def toTokens(x: NCNlpSentence): ArrayBuffer[NCNlpSentenceToken] = x.tokens

    case class NoteKey(start: Int, end: Int)
    case class TokenKey(id: String, start: Int, end: Int)
    case class NoteLink(note: String, indexes: Seq[Int])

    case class PartKey(id: String, start: Int, end: Int) {
        require(start <= end)

        private def in(i: Int): Boolean = i >= start && i <= end
        def intersect(id: String, start: Int, end: Int): Boolean = id == this.id && (in(start) || in(end))
    }

    object PartKey {
        def apply(m: util.HashMap[String, JSerializable]): PartKey = {
            def get[T](name: String): T = m.get(name).asInstanceOf[T]

            PartKey(get("id"), get("startcharindex"), get("endcharindex"))
        }

        def apply(t: NCNlpSentenceNote, sen: NCNlpSentence): PartKey =
            PartKey(t.noteType, sen(t.tokenFrom).startCharIndex, sen(t.tokenTo).endCharIndex)
    }

    private def getLinks(notes: Seq[NCNlpSentenceNote]): Seq[NoteLink] = {
        val noteLinks = mutable.ArrayBuffer.empty[NoteLink]

        for (n ← notes.filter(n ⇒ n.noteType == "nlpcraft:limit" || n.noteType == "nlpcraft:references"))
            noteLinks += NoteLink(n("note").asInstanceOf[String], n("indexes").asInstanceOf[JList[Int]].asScala.sorted)

        for (n ← notes.filter(_.noteType == "nlpcraft:sort")) {
            def add(noteName: String, idxsName: String): Unit = {
                val names = n(noteName).asInstanceOf[JList[String]]
                val idxsSeq = n(idxsName).asInstanceOf[JList[JList[Int]]]

                require(names.size() == idxsSeq.size())

                noteLinks ++=
                    (for ((name, idxs) ← names.asScala.zip(idxsSeq.asScala.map(_.asScala)))
                        yield NoteLink(name, idxs.sorted)
                    )
            }

            if (n.contains("subjnotes")) add("subjnotes", "subjindexes")
            if (n.contains("bynotes")) add("bynotes", "byindexes")
        }

        noteLinks
    }

    private def getPartKeys(notes: NCNlpSentenceNote*): Seq[PartKey] =
        notes.
            filter(_.isUser).
            flatMap(n ⇒ {
                val optList: Option[JList[util.HashMap[String, JSerializable]]] = n.dataOpt("parts")

                optList
            }).flatMap(_.asScala).map(m ⇒ PartKey(m)).distinct

    /**
      *
      * @param ns
      * @param idxs
      * @param notesType
      * @param note
      * @return
      */
    private def checkRelation(ns: NCNlpSentence, idxs: Seq[Int], notesType: String, note: NCNlpSentenceNote): Boolean = {
        val types = idxs.flatMap(idx ⇒ ns(idx).map(p ⇒ p).filter(!_.isNlp).map(_.noteType)).distinct

        /**
          * Example:
          * 1. Sentence 'maximum x' (single element related function)
          * - maximum is aggregate function linked to date element.
          * - x defined as 2 elements: date and num.
          * So, the variant 'maximum x (as num)' should be excluded.
          * *
          * 2. Sentence 'compare x and y' (multiple elements related function)
          * - compare is relation function linked to date element.
          * - x an y defined as 2 elements: date and num.
          * So, variants 'x (as num) and x (as date)'  and 'x (as date) and x (as num)'
          * should not be excluded, but invalid relation should be deleted for these combinations.
          */
        types.size match {
            case 0 ⇒ false
            case 1 ⇒ types.head == notesType
            case _ ⇒
                // Equal elements should be processed together with function element.
                if (types.size == 1)
                    false
                else {
                    ns.removeNote(note)

                    logger.trace(s"Removed note: $note")

                    true
                }
        }
    }

    /**
      * Fixes notes with references to other notes indexes.
      * Note that 'idxsField' is 'indexes' and 'noteField' is 'note' for all kind of references.
      *
      * @param noteType Note type.
      * @param idxsField Indexes field.
      * @param noteField Note field.
      * @param ns Sentence.
      * @param history Indexes transformation history.
      * @return Valid flag.
      */
    private def fixIndexesReferences(
        noteType: String,
        idxsField: String,
        noteField: String,
        ns: NCNlpSentence,
        history: Seq[(Int, Int)]
    ): Boolean = {
        ns.filter(_.isTypeOf(noteType)).foreach(tok ⇒
            tok.getNoteOpt(noteType, idxsField) match {
                case Some(n) ⇒
                    val idxs: Seq[Int] = n.data[JList[Int]](idxsField).asScala
                    var fixed = idxs

                    history.foreach { case (idxOld, idxNew) ⇒ fixed = fixed.map(i ⇒ if (i == idxOld) idxNew else i) }

                    fixed = fixed.distinct

                    if (idxs != fixed)
                        ns.fixNote(n, "indexes" → fixed.asJava.asInstanceOf[JSerializable])
                case None ⇒ // No-op.
            }
        )

        ns.flatMap(_.getNotes(noteType)).forall(
            n ⇒ checkRelation(ns, n.data[JList[Int]]("indexes").asScala, n.data[String](noteField), n)
        )
    }

    /**
      *
      * @param note
      * @param idxsField
      * @param noteField
      * @param ns
      */
    private def fixNoteIndexes(note: String, idxsField: String, noteField: String, ns: NCNlpSentence): Unit =
        ns.flatMap(_.getNotes(note)).foreach(
            n ⇒ checkRelation(ns, n.data[JList[Int]](idxsField).asScala, n.data[String](noteField), n)
        )

    /**
      *
      * @param note
      * @param idxsField
      * @param noteField
      * @param ns
      */
    private def fixNoteIndexesList(note: String, idxsField: String, noteField: String, ns: NCNlpSentence): Unit = {
        ns.flatMap(_.getNotes(note)).foreach(rel ⇒
            rel.dataOpt[JList[JList[Int]]](idxsField) match {
                case Some(idxsList) ⇒
                    val notesTypes = rel.data[JList[String]](noteField)

                    require(idxsList.size() == notesTypes.size())

                    idxsList.asScala.zip(notesTypes.asScala).foreach {
                        case (idxs, notesType) ⇒ checkRelation(ns, idxs.asScala, notesType, rel)
                    }
                case None ⇒ // No-op.
            }
        )
    }

    /**
      * Copies token.
      *
      * @param ns Sentence.
      * @param history Indexes transformation history.
      * @param toksCopy Copied tokens.
      * @param i Index.
      */
    private def simpleCopy(
        ns: NCNlpSentence,
        history: mutable.ArrayBuffer[(Int, Int)],
        toksCopy: NCNlpSentence, i: Int
    ): Seq[NCNlpSentenceToken] = {
        val tokCopy = toksCopy(i)

        history += tokCopy.index → ns.size

        ns += tokCopy.clone(ns.size)
    }

    /**
      * Glues stop words.
      *
      * @param ns Sentence.
      * @param userNoteTypes Notes types.
      * @param history Indexes transformation history.
      */
    private def unionStops(
        ns: NCNlpSentence,
        userNoteTypes: Seq[String],
        history: mutable.ArrayBuffer[(Int, Int)]
    ): Unit = {
        // Java collection used because using scala collections (mutable.Buffer.empty[mutable.Buffer[Token]]) is reason
        // Of compilation errors which seems as scala compiler internal error.
        val bufs = new util.ArrayList[mutable.Buffer[NCNlpSentenceToken]]()

        def last[T](l: JList[T]): T = l.get(l.size() - 1)

        ns.filter(t ⇒ t.isStopWord && !t.isBracketed).foreach(t ⇒
            if (!bufs.isEmpty && last(bufs).last.index + 1 == t.index)
                last(bufs) += t
            else
                bufs.add(mutable.Buffer.empty[NCNlpSentenceToken] :+ t)
        )

        val idxsSeq = bufs.asScala.filter(_.lengthCompare(1) > 0).map(_.map(_.index))

        if (idxsSeq.nonEmpty) {
            val nsCopyToks = ns.clone()
            ns.clear()

            val buf = mutable.Buffer.empty[Int]

            for (i ← nsCopyToks.indices)
                idxsSeq.find(_.contains(i)) match {
                    case Some(idxs) ⇒
                        if (!buf.contains(idxs.head)) {
                            buf += idxs.head

                            ns += mkCompound(ns, nsCopyToks, idxs, stop = true, ns.size, None, history)
                        }
                    case None ⇒ simpleCopy(ns, history, nsCopyToks, i)
                }

            fixIndexes(ns, userNoteTypes)
        }
    }

    /**
      * Fixes indexes for all notes after recreating tokens.
      *
      * @param ns Sentence.
      * @param userNoteTypes Notes types.
      */
    private def fixIndexes(ns: NCNlpSentence, userNoteTypes: Seq[String]) {
        // Replaces other notes indexes.
        for (t ← userNoteTypes :+ "nlpcraft:nlp"; note ← ns.getNotes(t)) {
            val toks = ns.filter(_.contains(note)).sortBy(_.index)

            val newNote = note.clone(toks.map(_.index), toks.flatMap(_.wordIndexes).sorted)

            toks.foreach(t ⇒ {
                t.remove(note)
                t.add(newNote)
            })
        }

        // Special case - field index of core NLP note.
        ns.zipWithIndex.foreach { case (tok, idx) ⇒ ns.fixNote(tok.getNlpNote, "index" → idx) }
    }

    /**
      * Zip notes with same type.
      *
      * @param ns Sentence.
      * @param nType Notes type.
      * @param userNotesTypes Notes types.
      * @param history Indexes transformation history.
      */
    private def zipNotes(
        ns: NCNlpSentence,
        nType: String,
        userNotesTypes: Seq[String],
        history: mutable.ArrayBuffer[(Int, Int)]
    ): Unit = {
        val nts = ns.getNotes(nType).filter(n ⇒ n.tokenFrom != n.tokenTo).sortBy(_.tokenFrom)

        val overlapped =
            nts.flatMap(n ⇒ n.tokenFrom to n.tokenTo).map(ns(_)).exists(
                t ⇒ userNotesTypes.map(pt ⇒ t.getNotes(pt).size).sum > 1
            )

        if (nts.nonEmpty && !overlapped) {
            val nsCopyToks = ns.clone()
            ns.clear()

            val buf = mutable.ArrayBuffer.empty[Int]

            for (i ← nsCopyToks.indices)
                nts.find(_.tokenIndexes.contains(i)) match {
                    case Some(n) ⇒
                        if (!buf.contains(n.tokenFrom)) {
                            buf += n.tokenFrom

                            ns += mkCompound(ns, nsCopyToks, n.tokenIndexes, stop = false, ns.size, Some(n), history)
                        }
                    case None ⇒ simpleCopy(ns, history, nsCopyToks, i)
                }

            fixIndexes(ns, userNotesTypes)
        }
    }

    /**
      * Makes compound note.
      *
      * @param ns Sentence.
      * @param nsCopyToks Tokens.
      * @param indexes Indexes.
      * @param stop Flag.
      * @param idx Index.
      * @param commonNote Common note.
      * @param history Indexes transformation history.
      */
    private def mkCompound(
        ns: NCNlpSentence,
        nsCopyToks: Seq[NCNlpSentenceToken],
        indexes: Seq[Int],
        stop: Boolean,
        idx: Int,
        commonNote: Option[NCNlpSentenceNote],
        history: mutable.ArrayBuffer[(Int, Int)]
    ): NCNlpSentenceToken = {
        val t = NCNlpSentenceToken(idx)

        // Note, it adds stop-words too.
        val content = nsCopyToks.zipWithIndex.filter(p ⇒ indexes.contains(p._2)).map(_._1)

        content.foreach(t ⇒ history += t.index → idx)

        def mkValue(get: NCNlpSentenceToken ⇒ String): String = {
            val buf = mutable.Buffer.empty[String]

            val n = content.size - 1

            content.zipWithIndex.foreach(p ⇒ {
                val t = p._1
                val idx = p._2

                buf += get(t)

                if (idx < n && t.endCharIndex != content(idx + 1).startCharIndex)
                    buf += " "
            })

            buf.mkString
        }

        val origText = mkValue((t: NCNlpSentenceToken) ⇒ t.origText)

        val idxs = Seq(idx)
        val wordIdxs = content.flatMap(_.wordIndexes).sorted

        val direct =
            commonNote match {
                case Some(n) if n.isUser ⇒ n.isDirect
                case _ ⇒ content.forall(_.isDirect)
            }

        val params = Seq(
            "index" → idx,
            "pos" → NCPennTreebank.SYNTH_POS,
            "posDesc" → NCPennTreebank.SYNTH_POS_DESC,
            "lemma" → mkValue((t: NCNlpSentenceToken) ⇒ t.lemma),
            "origText" → origText,
            "normText" → mkValue((t: NCNlpSentenceToken) ⇒ t.normText),
            "stem" → mkValue((t: NCNlpSentenceToken) ⇒ t.stem),
            "start" → content.head.startCharIndex,
            "end" → content.last.endCharIndex,
            "charLength" → origText.length,
            "quoted" → false,
            "stopWord" → stop,
            "bracketed" → false,
            "direct" → direct,
            "dict" → (if (nsCopyToks.size == 1) nsCopyToks.head.getNlpNote.data[Boolean]("dict") else false),
            "english" → nsCopyToks.forall(_.getNlpNote.data[Boolean]("english")),
            "swear" → nsCopyToks.exists(_.getNlpNote.data[Boolean]("swear"))
        )

        val nlpNote = NCNlpSentenceNote(idxs, wordIdxs, "nlpcraft:nlp", params: _*)

        t.add(nlpNote)

        // Adds processed note with fixed indexes.
        commonNote match {
            case Some(n) ⇒
                ns.removeNote(n)
                t.add(n.clone(idxs, wordIdxs))
            case None ⇒ // No-op.
        }

        t
    }

    /**
      * Fixes notes with references list to other notes indexes.
      *
      * @param noteType Note type.
      * @param idxsField Indexes field.
      * @param noteField Note field.
      * @param ns Sentence.
      * @param history Indexes transformation history.
      * @return Valid flag.
      */
    private def fixIndexesReferencesList(
        noteType: String,
        idxsField: String,
        noteField: String,
        ns: NCNlpSentence,
        history: Seq[(Int, Int)]
    ): Boolean = {
        var ok = true

        for (tok ← ns.filter(_.isTypeOf(noteType)) if ok)
            tok.getNoteOpt(noteType, idxsField) match {
                case Some(n) ⇒
                    val idxs: Seq[Seq[Int]] =
                        n.data[JList[JList[Int]]](idxsField).asScala.map(_.asScala)
                    var fixed = idxs

                    history.foreach {
                        case (idxOld, idxNew) ⇒ fixed = fixed.map(_.map(i ⇒ if (i == idxOld) idxNew else i).distinct)
                    }

                    if (fixed.forall(_.size == 1))
                        // Fix double dimension array to one dimension,
                        // so it should be called always in spite of 'fixIndexesReferences' method.
                        ns.fixNote(n, idxsField → fixed.map(_.head).asJava.asInstanceOf[JSerializable])
                    else
                        ok = false
                case None ⇒ // No-op.
            }

        ok &&
            ns.flatMap(_.getNotes(noteType)).forall(rel ⇒
                rel.dataOpt[JList[Int]](idxsField) match {
                    case Some(idxsList) ⇒
                        val notesTypes = rel.data[JList[String]](noteField)

                        require(idxsList.size() == notesTypes.size())

                        idxsList.asScala.zip(notesTypes.asScala).forall {
                            case (idxs, notesType) ⇒ checkRelation(ns, Seq(idxs), notesType, rel)
                        }
                    case None ⇒ true
                }
            )
    }

    /**
      * Fixes tokens positions.
      *
      * @param ns Sentence.
      * @param notNlpTypes Token types.
      */
    private def collapseSentence(ns: NCNlpSentence, notNlpTypes: Seq[String]): Boolean = {
        ns.
            filter(!_.isNlp).
            filter(_.isStopWord).
            flatten.
            filter(_.isNlp).
            foreach(n ⇒ ns.fixNote(n, "stopWord" → false))

        val all = ns.tokens.flatten
        val nsNotes: Map[String, Seq[Int]] = all.map(p ⇒ p.noteType → p.tokenIndexes).toMap

        for (
            t ← ns.tokens; stopReason ← t.stopsReasons
                if all.contains(stopReason) && nsNotes.getOrElse(stopReason.noteType, Seq.empty) == stopReason.tokenIndexes
        )
            ns.fixNote(t.getNlpNote, "stopWord" → true)

        val history = mutable.ArrayBuffer.empty[(Int, Int)]

        fixNoteIndexes("nlpcraft:relation", "indexes", "note", ns)
        fixNoteIndexes("nlpcraft:limit", "indexes", "note", ns)
        fixNoteIndexesList("nlpcraft:sort", "subjindexes", "subjnotes", ns)
        fixNoteIndexesList("nlpcraft:sort", "byindexes", "bynotes", ns)

        notNlpTypes.foreach(typ ⇒ zipNotes(ns, typ, notNlpTypes, history))
        unionStops(ns, notNlpTypes, history)

        val res =
            fixIndexesReferences("nlpcraft:relation", "indexes", "note", ns, history) &&
            fixIndexesReferences("nlpcraft:limit", "indexes", "note", ns, history) &&
            fixIndexesReferencesList("nlpcraft:sort", "subjindexes", "subjnotes", ns, history) &&
            fixIndexesReferencesList("nlpcraft:sort", "byindexes", "bynotes", ns, history)

        if (res) {
            // Validation (all indexes calculated well)
            require(
                !res ||
                    !ns.flatten.
                        exists(n ⇒ ns.filter(_.wordIndexes.exists(n.wordIndexes.contains)).exists(t ⇒ !t.contains(n))),
                s"Invalid sentence:\n" +
                    ns.map(t ⇒
                        // Human readable invalid sentence for debugging.
                        s"${t.origText}{index:${t.index}}[${t.map(n ⇒ s"${n.noteType}, {range:${n.tokenFrom}-${n.tokenTo}}").mkString("|")}]"
                    ).mkString("\n")
            )
        }

        res
    }

    private def dropAbstract(mdl: NCModel, ns: NCNlpSentence): Unit =
        if (!mdl.getAbstractTokens.isEmpty) {
            val notes = ns.flatten

            val keys = getPartKeys(notes: _*)
            val noteLinks = getLinks(notes)

            notes.filter(n ⇒ {
                val noteToks = ns.tokens.filter(_.contains(n))

                mdl.getAbstractTokens.contains(n.noteType) &&
                    !keys.exists(_.intersect(n.noteType, noteToks.head.startCharIndex, noteToks.last.startCharIndex)) &&
                    !noteLinks.contains(NoteLink(n.noteType, n.tokenIndexes.sorted))
            }).foreach(ns.removeNote)
        }

    private def getNotNlpNotes(toks: Seq[NCNlpSentenceToken]): Seq[NCNlpSentenceNote] =
        toks.flatten.filter(!_.isNlp).distinct

    private def addDeleted(thisSen: NCNlpSentence, sen: NCNlpSentence, dels: Iterable[NCNlpSentenceNote]): Unit =
        sen.deletedNotes ++= dels.map(n ⇒ {
            val savedDelNote = n.clone()
            val savedDelToks = n.tokenIndexes.map(idx ⇒ thisSen(idx).clone())

            val mainNotes = savedDelToks.flatten.filter(n ⇒ n.noteType != "nlpcraft:nlp" && n != savedDelNote)

            // Deleted note's tokens should contains only nlp data and deleted notes.
            for (savedDelTok ← savedDelToks; mainNote ← mainNotes)
                savedDelTok.remove(mainNote)

            savedDelNote → savedDelToks
        })

    /**
      * This collapser handles several tasks:
      * - "overall" collapsing after all other individual collapsers had their turn.
      * - Special further enrichment of tokens like linking, etc.
      *
      * In all cases of overlap (full or partial) - the "longest" note wins. In case of overlap and equal
      * lengths - the winning note is chosen based on this priority.
      */
    @throws[NCE]
    private def collapseSentence(thisSen: NCNlpSentence, mdl: NCModel, lastPhase: Boolean = false): Seq[NCNlpSentence] = {
        def collapse0(ns: NCNlpSentence): Option[NCNlpSentence] = {
            if (lastPhase)
                dropAbstract(mdl, ns)

            if (collapseSentence(ns, getNotNlpNotes(ns).map(_.noteType).distinct)) Some(ns) else None
        }

        // Always deletes `similar` notes.
        // Some words with same note type can be detected various ways.
        // We keep only one variant -  with `best` direct and sparsity parameters,
        // other variants for these words are redundant.
        val redundant: Seq[NCNlpSentenceNote] =
            thisSen.flatten.filter(!_.isNlp).distinct.
                groupBy(_.getKey()).
                map(p ⇒ p._2.sortBy(p ⇒
                    (
                        // System notes don't have such flags.
                        if (p.isUser) {
                            if (p.isDirect)
                                0
                            else
                                1
                        }
                        else
                            0,
                        if (p.isUser)
                            p.sparsity
                        else
                            0
                    )
                )).
                flatMap(_.drop(1)).
                toSeq

        redundant.foreach(thisSen.removeNote)

        var delCombs: Seq[NCNlpSentenceNote] =
            getNotNlpNotes(thisSen).
                flatMap(note ⇒ getNotNlpNotes(note.tokenIndexes.sorted.map(i ⇒ thisSen(i))).filter(_ != note)).
                distinct

        // Optimization. Deletes all wholly swallowed notes.
        val links = getLinks(thisSen.flatten)

        val swallowed =
            delCombs.
                // There aren't links on it.
                filter(n ⇒ !links.contains(NoteLink(n.noteType, n.tokenIndexes.sorted))).
                // It doesn't have links.
                filter(getPartKeys(_).isEmpty).
                flatMap(note ⇒ {
                    val noteWordsIdxs = note.wordIndexes.toSet
                    val key = PartKey(note, thisSen)

                    val delCombOthers =
                        delCombs.filter(_ != note).flatMap(n ⇒ if (getPartKeys(n).contains(key)) Some(n) else None)

                    if (delCombOthers.exists(o ⇒ noteWordsIdxs == o.wordIndexes.toSet)) Some(note) else None
                })

        delCombs = delCombs.filter(p ⇒ !swallowed.contains(p))
        addDeleted(thisSen, thisSen, swallowed)
        swallowed.foreach(thisSen.removeNote)

        val toksByIdx: Seq[Seq[NCNlpSentenceNote]] =
            delCombs.flatMap(note ⇒ note.wordIndexes.map(_ → note)).
                groupBy { case (idx, _) ⇒ idx }.
                map { case (_, seq) ⇒ seq.map { case (_, note) ⇒ note } }.
                toSeq.sortBy(-_.size)

//        val toksByIdx1 =
//            delCombs.flatMap(note ⇒ note.wordIndexes.map(_ → note)).
//                groupBy { case (idx, _) ⇒ idx }.
//                map { case (idx, seq) ⇒ idx → seq.map { case (_, note) ⇒ note } }.
//                toSeq.sortBy(_._2.size)
//
//        toksByIdx.foreach{ case (seq) ⇒
//            println(s"toksByIdx seq=${seq.map(i ⇒ s"${i.noteType} ${i.wordIndexes.mkString(",")}").mkString(" | ")}")
//        }

//        toksByIdx1.sortBy(_._1).foreach{ case (i, seq) ⇒
//            println(s"toksByIdx1 ${i} seq=${seq.map(i ⇒ s"${i.noteType} ${i.wordIndexes.mkString(",")}").mkString(" | ")}")
//        }

        val dict = mutable.HashMap.empty[String, NCNlpSentenceNote]

        var i = 'A'

        val converted: Seq[Seq[String]] =
            toksByIdx.map(seq ⇒ {
                seq.map(
                    n ⇒ {
                        val s = s"$i"

                        i = (i.toInt + 1).toChar

                        dict += s → n

                        s
                    }
                )
            })

        //val minDelSize = if (toksByIdx.isEmpty) 1 else toksByIdx.map(_.size).max - 1

        var sens =
            if (delCombs.nonEmpty) {
                val p = new ForkJoinPool()

                val tmp = NCComboRecursiveTask.findCombinations(
                    converted.map(_.asJava).asJava,
                    new Comparator[String]() {
                        override def compare(n1: String, n2: String): Int = n1.compareTo(n2)
                    },
                    p
                )

                p.shutdown()

                val seq1 = tmp.asScala.map(_.asScala.map(dict))

                val sens =
                    seq1.
                        flatMap(p ⇒ {
                            val delComb: Seq[NCNlpSentenceNote] = p

                            val nsClone = thisSen.clone()

                            // Saves deleted notes for sentence and their tokens.
                            addDeleted(thisSen, nsClone, delComb)
                            delComb.foreach(nsClone.removeNote)

                            // Has overlapped notes for some tokens.
                            require(
                                !nsClone.exists(_.count(!_.isNlp) > 1),
                                s"Invalid notes: ${nsClone.filter(_.count(!_.isNlp) > 1).mkString("|")}"
                            )

                            collapse0(nsClone)
                        })

                // It removes sentences which have only one difference - 'direct' flag of their user tokens.
                // `Direct` sentences have higher priority.
                case class Key(sysNotes: Seq[Map[String, JSerializable]], userNotes: Seq[Map[String, JSerializable]])
                case class Value(sentence: NCNlpSentence, directCount: Int)

                val m = mutable.HashMap.empty[Key, Value]

                sens.map(sen ⇒ {
                    val notes = sen.flatten

                    val sysNotes = notes.filter(_.isSystem)
                    val nlpNotes = notes.filter(_.isNlp)
                    val userNotes = notes.filter(_.isUser)

                    def get(seq: Seq[NCNlpSentenceNote]): Seq[Map[String, JSerializable]] =
                        seq.map(p ⇒
                            // We have to delete some keys to have possibility to compare sentences.
                            p.clone().filter(_._1 != "direct")
                        )

                    (Key(get(sysNotes), get(userNotes)), sen, nlpNotes.map(p ⇒ if (p.isDirect) 0 else 1).sum)
                }).
                    foreach { case (key, sen, directCnt) ⇒
                        m.get(key) match {
                            case Some(v) ⇒
                                // Best sentence is sentence with `direct` synonyms.
                                if (v.directCount > directCnt)
                                    m += key → Value(sen, directCnt)
                            case None ⇒ m += key → Value(sen, directCnt)
                        }
                    }

                m.values.map(_.sentence).toSeq
            }
            else
                collapse0(thisSen).flatMap(p ⇒ Option(Seq(p))).getOrElse(Seq.empty)

        sens = sens.distinct

        sens.foreach(sen ⇒
            sen.foreach(tok ⇒
                tok.size match {
                    case 1 ⇒ require(tok.head.isNlp, s"Unexpected non-'nlpcraft:nlp' token: $tok")
                    case 2 ⇒ require(tok.head.isNlp ^ tok.last.isNlp, s"Unexpected token notes: $tok")
                    case _ ⇒ require(requirement = false, s"Unexpected token notes count: $tok")
                }
            )
        )

        // Drops similar sentences (with same tokens structure).
        // Among similar sentences we prefer one with minimal free words count.
        sens.groupBy(_.flatten.filter(!_.isNlp).map(_.getKey(withIndexes = false))).
            map { case (_, seq) ⇒ seq.minBy(_.filter(p ⇒ p.isNlp && !p.isStopWord).map(_.wordIndexes.length).sum) }.
            toSeq
    }
}

import org.apache.nlpcraft.common.nlp.NCNlpSentence._

/**
  * Parsed NLP sentence is a collection of tokens. Each token is a collection of notes and
  * each note is a collection of KV pairs.
  *
  * @param srvReqId Server request ID.
  * @param text Normalized text.
  * @param enabledBuiltInToks Enabled built-in tokens.
  * @param tokens Initial buffer.
  * @param deletedNotes Deleted overridden notes with their tokens.
  */
class NCNlpSentence(
    val srvReqId: String,
    val text: String,
    val enabledBuiltInToks: Set[String],
    override val tokens: mutable.ArrayBuffer[NCNlpSentenceToken] = new mutable.ArrayBuffer[NCNlpSentenceToken](32),
    private val deletedNotes: mutable.HashMap[NCNlpSentenceNote, Seq[NCNlpSentenceToken]] = mutable.HashMap.empty,
    private var initNlpNotes: Map[NoteKey, NCNlpSentenceNote] = null,
    private val nlpTokens: mutable.HashMap[TokenKey, NCNlpSentenceToken] = mutable.HashMap.empty
) extends NCNlpSentenceTokenBuffer(tokens) with JSerializable {
    @transient
    private var hash: java.lang.Integer = _

    private def calcHash(): Int =
        Seq(srvReqId, text, enabledBuiltInToks, tokens).map(_.hashCode()).foldLeft(0)((a, b) ⇒ 31 * a + b)

    // Deep copy.
    override def clone(): NCNlpSentence =
        new NCNlpSentence(
            srvReqId,
            text,
            enabledBuiltInToks,
            tokens.map(_.clone()),
            deletedNotes.map(p ⇒ p._1.clone() → p._2.map(_.clone())),
            initNlpNotes = initNlpNotes
        )

    /**
      * Utility method that gets set of notes for given note type collected from
      * tokens in this sentence. Notes are sorted in the same order they appear
      * in this sentence.
      *
      * @param noteType Note type.
      */
    def getNotes(noteType: String): Seq[NCNlpSentenceNote] = this.flatMap(_.getNotes(noteType)).distinct

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

    def fixNote(note: NCNlpSentenceNote, kvs: (String, JSerializable)*): Unit = {
        val fixed = note.clone(kvs: _*)

        this.filter(t ⇒ t.index >= fixed.tokenIndexes.head && t.index <= fixed.tokenIndexes.last).foreach(t ⇒ {
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
                    case "nlpcraft:sort" ⇒
                        tokensEqualOrSimilar(getListList(n1, "subjindexes"), getListList(n2, "subjindexes")) &&
                            tokensEqualOrSimilar(getListList(n1, "byindexes"), getListList(n2, "byindexes"))
                    case "nlpcraft:limit" ⇒
                        tokensEqualOrSimilar(getList(n1, "indexes"), getList(n2, "indexes"))
                    case "nlpcraft:reference" ⇒
                        tokensEqualOrSimilar(getList(n1, "indexes"), getList(n2, "indexes"))

                    case _ ⇒ true
                }
            }

            def referencesEqualOrSimilar(n1: NCNlpSentenceNote, n2: NCNlpSentenceNote): Boolean =
                referencesEqualOrSimilar0(n1, n2) || referencesEqualOrSimilar0(n2, n1)

            def getUniqueKey0(n: NCNlpSentenceNote): Seq[Any] = n.getKey(withIndexes = false, withReferences = false)

            getUniqueKey0(n1) == getUniqueKey0(n2) && wordsEqualOrSimilar(n1, n2) && referencesEqualOrSimilar(n1, n2)
        }

    override def equals(obj: Any): Boolean = obj match {
        case x: NCNlpSentence ⇒
            tokens == x.tokens &&
                srvReqId == x.srvReqId &&
                text == x.text &&
                enabledBuiltInToks == x.enabledBuiltInToks

        case _ ⇒ false
    }

    /**
      *
      */
    def saveNlpNotes(): Unit =
        initNlpNotes = this.map(t ⇒ NoteKey(t.startCharIndex, t.endCharIndex) → t.getNlpNote).toMap

    /**
      *
      * @return
      */
    def findInitialNlpNote(startCharIndex: Int, endCharIndex: Int): Option[NCNlpSentenceNote] =
        initNlpNotes.get(NoteKey(startCharIndex, endCharIndex))

    /**
      *
      * @param nlp
      */
    def addNlpToken(nlp: NCNlpSentenceToken): Unit = {
        require(nlp.size <= 2)

        nlp.foreach(n ⇒ nlpTokens += TokenKey(n.noteType, nlp.startCharIndex, nlp.endCharIndex) → nlp)
    }

    /**
      *
      * @param noteType
      * @param startCharIndex
      * @param endCharIndex
      * @return
      */
    def findNlpToken(noteType: String, startCharIndex: Int, endCharIndex: Int): Option[NCNlpSentenceToken] =
        nlpTokens.get(TokenKey(noteType, startCharIndex, endCharIndex))

    /**
      *
      */
    def getDeletedNotes: Predef.Map[NCNlpSentenceNote, Seq[NCNlpSentenceToken]] = deletedNotes.toMap

    def collapse(mdl: NCModel, lastPhase: Boolean = false): Seq[NCNlpSentence] = {
        collapseSentence(this, mdl, lastPhase)
    }
}
