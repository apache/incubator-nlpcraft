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

package org.apache.nlpcraft.probe.mgrs.sentence

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.nlp.NCNlpSentence.NoteLink
import org.apache.nlpcraft.common.nlp.pos.NCPennTreebank
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote, NCNlpSentenceToken}
import org.apache.nlpcraft.common.{NCE, NCService, U, _}
import org.apache.nlpcraft.model.NCModel
import org.apache.nlpcraft.probe.mgrs.NCTokenPartKey

import java.io.{Serializable => JSerializable}
import java.util
import java.util.{List => JList}
import scala.collection.mutable
import scala.collection.parallel.CollectionConverters._
import scala.jdk.CollectionConverters.{ListHasAsScala, SeqHasAsJava, SetHasAsJava, SetHasAsScala}
import scala.language.implicitConversions

/**
  * Sentences processing manager.
  */
object NCSentenceManager extends NCService {
    @volatile private var pool: java.util.concurrent.ForkJoinPool = _

    type CacheKey = Seq[Set[NCNlpSentenceNote]]
    type CacheValue = Seq[Seq[NCNlpSentenceNote]]
    private val combCache = mutable.HashMap.empty[String, mutable.HashMap[CacheKey, CacheValue]]

    def getLinks(notes: Seq[NCNlpSentenceNote]): Seq[NoteLink] = {
        val noteLinks = mutable.ArrayBuffer.empty[NoteLink]

        for (n <- notes.filter(n => n.noteType == "nlpcraft:limit" || n.noteType == "nlpcraft:references"))
            noteLinks += NoteLink(n("note").asInstanceOf[String], n("indexes").asInstanceOf[JList[Int]].asScala.toSeq.sorted)

        for (n <- notes.filter(_.noteType == "nlpcraft:sort")) {
            def add(noteName: String, idxsName: String): Unit = {
                val names = n(noteName).asInstanceOf[JList[String]]
                val idxsSeq = n(idxsName).asInstanceOf[JList[JList[Int]]]

                require(names.size() == idxsSeq.size())

                noteLinks ++=
                    (
                        for ((name, idxs) <- names.asScala.zip(idxsSeq.asScala.map(_.asScala)))
                            yield NoteLink(name, idxs.sorted.toSeq)
                    )
            }

            if (n.contains("subjnotes")) add("subjnotes", "subjindexes")
            if (n.contains("bynotes")) add("bynotes", "byindexes")
        }

        noteLinks
    }

    /**
      *
      * @param n
      */
    private def getParts(n: NCNlpSentenceNote): Option[Seq[NCTokenPartKey]] = {
        val res: Option[JList[NCTokenPartKey]]  = n.dataOpt("parts")

        res match {
            case Some(v) => Some(v.asScala)
            case None => None
        }
    }

    /**
      *
      * @param notes
      */
    def getPartKeys(notes: Seq[NCNlpSentenceNote]): Seq[NCTokenPartKey] =
        notes.filter(_.isUser).flatMap(getParts).flatten.distinct

    /**
      *
      * @param note
      * @return
      */
    def getPartKeys(note: NCNlpSentenceNote): Seq[NCTokenPartKey] =
        if (note.isUser) getParts(note).getOrElse(Seq.empty) else Seq.empty

    /**
      *
      * @param ns
      * @param idxs
      * @param notesType
      * @param note
      * @return
      */
    private def checkRelation(ns: NCNlpSentence, idxs: Seq[Int], notesType: String, note: NCNlpSentenceNote): Boolean = {
        val types = idxs.flatMap(idx => ns(idx).map(p => p).filter(!_.isNlp).map(_.noteType)).distinct

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
            case 0 => false
            case 1 => types.head == notesType
            case _ =>
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
        ns.filter(_.isTypeOf(noteType)).foreach(tok =>
            tok.getNoteOpt(noteType, idxsField) match {
                case Some(n) =>
                    val idxs: Seq[Int] = n.data[JList[Int]](idxsField).asScala.toSeq
                    var fixed = idxs

                    history.foreach { case (idxOld, idxNew) => fixed = fixed.map(i => if (i == idxOld) idxNew else i) }

                    fixed = fixed.distinct

                    if (idxs != fixed)
                        ns.fixNote(n, "indexes" -> fixed.asJava.asInstanceOf[JSerializable])
                case None => // No-op.
            }
        )

        ns.flatMap(_.getNotes(noteType)).forall(
            n => checkRelation(ns, n.data[JList[Int]]("indexes").asScala.toSeq, n.data[String](noteField), n)
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
            n => checkRelation(ns, n.data[JList[Int]](idxsField).asScala.toSeq, n.data[String](noteField), n)
        )

    /**
      *
      * @param note
      * @param idxsField
      * @param noteField
      * @param ns
      */
    private def fixNoteIndexesList(note: String, idxsField: String, noteField: String, ns: NCNlpSentence): Unit =
        ns.flatMap(_.getNotes(note)).foreach(rel =>
            rel.dataOpt[JList[JList[Int]]](idxsField) match {
                case Some(idxsList) =>
                    val notesTypes = rel.data[JList[String]](noteField)

                    require(idxsList.size() == notesTypes.size())

                    idxsList.asScala.zip(notesTypes.asScala).foreach {
                        case (idxs, notesType) => checkRelation(ns, idxs.asScala.toSeq, notesType, rel)
                    }
                case None => // No-op.
            }
        )

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
        toksCopy: NCNlpSentence,
        i: Int
    ): Seq[NCNlpSentenceToken] = {
        val tokCopy = toksCopy(i)

        history += tokCopy.index -> ns.size

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

        ns.filter(t => t.isStopWord && !t.isBracketed).foreach(t =>
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

            for (i <- nsCopyToks.indices)
                idxsSeq.find(_.contains(i)) match {
                    case Some(idxs) =>
                        if (!buf.contains(idxs.head)) {
                            buf += idxs.head

                            ns += mkCompound(ns, nsCopyToks.toSeq, idxs.toSeq, stop = true, ns.size, None, history)
                        }
                    case None => simpleCopy(ns, history, nsCopyToks, i)
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
    private def fixIndexes(ns: NCNlpSentence, userNoteTypes: Seq[String]): Unit = {
        // Replaces other notes indexes.
        for (t <- userNoteTypes :+ "nlpcraft:nlp"; note <- ns.getNotes(t)) {
            val toks = ns.filter(_.contains(note))

            val newNote = note.clone(toks.map(_.index), toks.flatMap(_.wordIndexes).toSeq.sorted)

            toks.foreach(t => {
                t.remove(note)
                t.add(newNote)
            })
        }

        // Special case - field index of core NLP note.
        ns.zipWithIndex.foreach { case (tok, idx) => ns.fixNote(tok.getNlpNote, "index" -> idx) }
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
        val nts = ns.getNotes(nType).filter(n => n.tokenFrom != n.tokenTo).sortBy(_.tokenFrom)

        val overlapped =
            nts.flatMap(n => n.tokenFrom to n.tokenTo).map(ns(_)).exists(
                t => userNotesTypes.map(pt => t.getNotes(pt).size).sum > 1
            )

        if (nts.nonEmpty && !overlapped) {
            val nsCopyToks = ns.clone()
            ns.clear()

            val buf = mutable.ArrayBuffer.empty[Int]

            for (i <- nsCopyToks.indices)
                nts.find(_.tokenIndexes.contains(i)) match {
                    case Some(n) =>
                        if (!buf.contains(n.tokenFrom)) {
                            buf += n.tokenFrom

                            ns += mkCompound(ns, nsCopyToks.toSeq, n.tokenIndexes, stop = false, ns.size, Some(n), history)
                        }
                    case None => simpleCopy(ns, history, nsCopyToks, i)
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
        val content = nsCopyToks.zipWithIndex.filter { case (_, idx) => indexes.contains(idx) }.map { case (tok, _) => tok}

        content.foreach(t => history += t.index -> idx)

        def mkValue(get: NCNlpSentenceToken => String): String = {
            val buf = mutable.Buffer.empty[String]

            val n = content.size - 1

            content.zipWithIndex.foreach { case (t, idx) =>
                buf += get(t)

                if (idx < n && t.endCharIndex != content(idx + 1).startCharIndex)
                    buf += " "
            }

            buf.mkString
        }

        val origText = mkValue((t: NCNlpSentenceToken) => t.origText)

        val idxs = Seq(idx)
        val wordIdxs = content.flatMap(_.wordIndexes).sorted

        val direct =
            commonNote match {
                case Some(n) if n.isUser => n.isDirect
                case _ => content.forall(_.isDirect)
            }

        val params = Seq(
            "index" -> idx,
            "pos" -> NCPennTreebank.SYNTH_POS,
            "posDesc" -> NCPennTreebank.SYNTH_POS_DESC,
            "lemma" -> mkValue((t: NCNlpSentenceToken) => t.lemma),
            "origText" -> origText,
            "normText" -> mkValue((t: NCNlpSentenceToken) => t.normText),
            "stem" -> mkValue((t: NCNlpSentenceToken) => t.stem),
            "start" -> content.head.startCharIndex,
            "end" -> content.last.endCharIndex,
            "charLength" -> origText.length,
            "quoted" -> false,
            "stopWord" -> stop,
            "bracketed" -> false,
            "direct" -> direct,
            "dict" -> (if (nsCopyToks.size == 1) nsCopyToks.head.getNlpNote.data[Boolean]("dict") else false),
            "english" -> nsCopyToks.forall(_.getNlpNote.data[Boolean]("english")),
            "swear" -> nsCopyToks.exists(_.getNlpNote.data[Boolean]("swear"))
        )

        val nlpNote = NCNlpSentenceNote(idxs, wordIdxs, "nlpcraft:nlp", params: _*)

        t.add(nlpNote)

        // Adds processed note with fixed indexes.
        commonNote match {
            case Some(n) =>
                ns.removeNote(n)
                t.add(n.clone(idxs, wordIdxs))
            case None => // No-op.
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

        for (tok <- ns.filter(_.isTypeOf(noteType)) if ok)
            tok.getNoteOpt(noteType, idxsField) match {
                case Some(n) =>
                    val idxs: Seq[Seq[Int]] = n.data[JList[JList[Int]]](idxsField).asScala.map(_.asScala.toSeq).toSeq
                    var fixed = idxs

                    history.foreach {
                        case (idxOld, idxNew) => fixed = fixed.map(_.map(i => if (i == idxOld) idxNew else i).distinct)
                    }

                    if (fixed.forall(_.size == 1))
                    // Fix double dimension array to one dimension,
                    // so it should be called always in spite of 'fixIndexesReferences' method.
                        ns.fixNote(n, idxsField -> fixed.map(_.head).asJava.asInstanceOf[JSerializable])
                    else
                        ok = false
                case None => // No-op.
            }

        ok &&
            ns.flatMap(_.getNotes(noteType)).forall(rel =>
                rel.dataOpt[JList[Int]](idxsField) match {
                    case Some(idxsList) =>
                        val notesTypes = rel.data[JList[String]](noteField)

                        require(idxsList.size() == notesTypes.size())

                        idxsList.asScala.zip(notesTypes.asScala).forall {
                            case (idxs, notesType) => checkRelation(ns, Seq(idxs), notesType, rel)
                        }
                    case None => true
                }
            )
    }

    /**
      * Fixes tokens positions.
      *
      * @param ns Sentence.
      * @param notNlpTypes Token types.
      * @param lastPhase Phase.
      */
    private def collapseSentence(ns: NCNlpSentence, notNlpTypes: Seq[String], lastPhase: Boolean): Boolean = {
        ns.
            filter(!_.isNlp).
            filter(_.isStopWord).
            flatten.
            filter(_.isNlp).
            foreach(n => ns.fixNote(n, "stopWord" -> false))

        val all = ns.tokens.flatten
        val nsNotes: Map[String, Seq[Int]] = all.map(p => p.noteType -> p.tokenIndexes).toMap

        for (
            t <- ns.tokens; stopReason <- t.stopsReasons
                if all.contains(stopReason) && nsNotes.getOrElse(stopReason.noteType, Seq.empty) == stopReason.tokenIndexes
        )
            ns.fixNote(t.getNlpNote, "stopWord" -> true)

        val history = mutable.ArrayBuffer.empty[(Int, Int)]

        fixNoteIndexes("nlpcraft:relation", "indexes", "note", ns)
        fixNoteIndexes("nlpcraft:limit", "indexes", "note", ns)
        fixNoteIndexesList("nlpcraft:sort", "subjindexes", "subjnotes", ns)
        fixNoteIndexesList("nlpcraft:sort", "byindexes", "bynotes", ns)

        notNlpTypes.foreach(typ => zipNotes(ns, typ, notNlpTypes, history))
        unionStops(ns, notNlpTypes, history)

        val histSeq = history.toSeq

        val res =
            fixIndexesReferences("nlpcraft:relation", "indexes", "note", ns, histSeq) &&
            fixIndexesReferences("nlpcraft:limit", "indexes", "note", ns, histSeq) &&
            fixIndexesReferencesList("nlpcraft:sort", "subjindexes", "subjnotes", ns, histSeq) &&
            fixIndexesReferencesList("nlpcraft:sort", "byindexes", "bynotes", ns, histSeq)

        // On last phase - just for performance reasons.
        if (res && lastPhase) {
            // Validation (all indexes calculated well)
            require(
                !res ||
                !ns.flatten.exists(n => ns.filter(_.wordIndexes.exists(n.wordIndexes.contains)).exists(t => !t.contains(n))),
                s"Invalid sentence:\n" +
                    ns.map(t =>
                        // Human readable invalid sentence for debugging.
                        s"${t.origText}{index:${t.index}}[${t.map(n => s"${n.noteType}, {range:${n.tokenFrom}-${n.tokenTo}}").mkString("|")}]"
                    ).mkString("\n")
            )
        }

        res
    }

    /**
      *
      * @param mdl
      * @param ns
      */
    private def dropAbstract(mdl: NCModel, ns: NCNlpSentence): Unit = {
        val abstractToks = mdl.getAbstractTokens

        if (!abstractToks.isEmpty) {
            val notes = ns.flatten.distinct.filter(n => abstractToks.contains(n.noteType))

            val keys = getPartKeys(notes)
            val noteLinks = getLinks(notes)

            notes.filter(n => {
                lazy val noteToks = ns.tokens.filter(t => t.index >= n.tokenFrom && t.index <= n.tokenTo)

                !noteLinks.contains(NoteLink(n.noteType, n.tokenIndexes.sorted)) &&
                !keys.exists(_.intersect(n.noteType, noteToks.head.startCharIndex, noteToks.last.startCharIndex))
            }).foreach(ns.removeNote)
        }
    }

    /**
      *
      * @param toks
      * @return
      */
    private def getNotNlpNotes(toks: Seq[NCNlpSentenceToken]): Seq[NCNlpSentenceNote] =
        toks.flatten.filter(!_.isNlp).distinct

    /**
      *
      * @param thisSen
      * @param sen
      * @param dels
      */
    private def addDeleted(thisSen: NCNlpSentence, sen: NCNlpSentence, dels: Iterable[NCNlpSentenceNote]): Unit =
        sen.addDeletedNotes(dels.map(n => {
            val savedDelNote = n.clone()
            val savedDelToks = n.tokenIndexes.map(idx => thisSen(idx).clone())

            val mainNotes = savedDelToks.flatten.filter(n => n.noteType != "nlpcraft:nlp" && n != savedDelNote)

            // Deleted note's tokens should contains only nlp data and deleted notes.
            for (savedDelTok <- savedDelToks; mainNote <- mainNotes)
                savedDelTok.remove(mainNote)

            savedDelNote -> savedDelToks
        }).toMap)

    /**
      *
      * @param sen
      * @param mdl
      * @param lastPhase
      * @param overlappedNotes
      */
    private def mkVariants(
        sen: NCNlpSentence, mdl: NCModel, lastPhase: Boolean, overlappedNotes: Seq[NCNlpSentenceNote]
    ): Seq[NCNlpSentence] = {
        def collapse0(ns: NCNlpSentence): Option[NCNlpSentence] = {
            if (lastPhase)
                dropAbstract(mdl, ns)

            if (collapseSentence(ns, getNotNlpNotes(ns.tokens).map(_.noteType).distinct, lastPhase)) Some(ns) else None
        }

        if (overlappedNotes.nonEmpty) {
            val notesSeqs: Seq[Set[NCNlpSentenceNote]] =
                overlappedNotes.flatMap(note => note.wordIndexes.map(_ -> note)).
                    groupBy { case (idx, _) => idx }.
                    map { case (_, seq) => seq.map { case (_, note) => note }.toSet }.
                    toSeq.
                    sortBy(-_.size)

            def findCombinations(): Seq[Seq[NCNlpSentenceNote]] =
                NCSentenceHelper.findCombinations(notesSeqs.map(_.asJava).asJava, pool).asScala.map(_.asScala.toSeq)

            val delCombs: Seq[Seq[NCNlpSentenceNote]] = combCache.
                getOrElseUpdate(sen.srvReqId, mutable.HashMap.empty[CacheKey, CacheValue]).
                getOrElseUpdate(notesSeqs, findCombinations())

            val seqSens =
                delCombs.
                    par.
                    flatMap(delComb => {
                        val nsClone = sen.clone()

                        // Saves deleted notes for sentence and their tokens.
                        addDeleted(sen, nsClone, delComb)
                        delComb.foreach(nsClone.removeNote)

                        // Has overlapped notes for some tokens.
                        require(!nsClone.exists(_.count(!_.isNlp) > 1))

                        collapse0(nsClone)
                    }).seq

            // It removes sentences which have only one difference - 'direct' flag of their user tokens.
            // `Direct` sentences have higher priority.
            type Key = Seq[Map[String, JSerializable]]
            case class Holder(key: Key, sentence: NCNlpSentence, factor: Int)

            def mkHolder(sen: NCNlpSentence): Holder = {
                val notes = sen.flatten

                Holder(
                    // We have to delete some keys to have possibility to compare sentences.
                    notes.map(_.clone().toMap.filter { case (name, _) => name != "direct" }).toSeq,
                    sen,
                    notes.filter(_.isNlp).map(p => if (p.isDirect) 0 else 1).sum
                )
            }

            seqSens.par.map(mkHolder).seq.groupBy(_.key).map { case (_, seq) => seq.minBy(_.factor).sentence }.toSeq
        }
        else
            collapse0(sen).flatMap(p => Option(Seq(p))).getOrElse(Seq.empty)

    }

    /**
      * This collapser handles several tasks:
      * - "overall" collapsing after all other individual collapsers had their turn.
      * - Special further enrichment of tokens like linking, etc.
      *
      * In all cases of overlap (full or partial) - the "longest" note wins. In case of overlap and equal
      * lengths - the winning note is chosen based on this priority.
      */
    @throws[NCE]
    private def collapseSentence(sen: NCNlpSentence, mdl: NCModel, lastPhase: Boolean = false): Seq[NCNlpSentence] = {
        // Always deletes `similar` notes.
        // Some words with same note type can be detected various ways.
        // We keep only one variant -  with `best` direct and sparsity parameters,
        // other variants for these words are redundant.
        val redundant: Seq[NCNlpSentenceNote] =
            sen.flatten.filter(!_.isNlp).distinct.
                groupBy(_.getKey()).
                map(p => p._2.sortBy(p =>
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

        redundant.foreach(sen.removeNote)

        var overlappedNotes: Seq[NCNlpSentenceNote] =
            getNotNlpNotes(sen.tokens).
                flatMap(note => getNotNlpNotes(note.tokenIndexes.map(sen(_))).filter(_ != note)).
                distinct

        // Optimization. Deletes all wholly swallowed notes.
        val links = getLinks(sen.tokens.toSeq.flatten)

        val swallowed =
            overlappedNotes.
                // There aren't links on it.
                filter(n => !links.contains(NoteLink(n.noteType, n.tokenIndexes.sorted))).
                // It doesn't have links.
                filter(n => getPartKeys(n).isEmpty).
                flatMap(note => {
                    val noteWordsIdxs = note.wordIndexesSet
                    val key = NCTokenPartKey(note, sen)

                    val delCombOthers =
                        overlappedNotes.filter(_ != note).flatMap(n => if (getPartKeys(n).contains(key)) Some(n) else None)

                    if (
                        delCombOthers.nonEmpty &&
                        !delCombOthers.exists(o => noteWordsIdxs.subsetOf(o.wordIndexesSet))
                    )
                        Some(note)
                    else
                        None
                })

        overlappedNotes = overlappedNotes.filter(p => !swallowed.contains(p))
        addDeleted(sen, sen, swallowed)
        swallowed.foreach(sen.removeNote)

        var sens = mkVariants( sen, mdl, lastPhase, overlappedNotes)

        sens.par.foreach(sen =>
            sen.foreach(tok =>
                tok.size match {
                    case 1 => require(tok.head.isNlp, s"Unexpected non-'nlpcraft:nlp' token: $tok")
                    case 2 => require(tok.head.isNlp ^ tok.last.isNlp, s"Unexpected token notes: $tok")
                    case _ => require(requirement = false, s"Unexpected token notes count: $tok")
                }
            )
        )

        // There are optimizations below. Similar variants by some criteria deleted.

        def notNlpNotes(s: NCNlpSentence): Seq[NCNlpSentenceNote] = s.flatten.filter(!_.isNlp)

        // Drops similar sentences with same notes structure based on greedy elements. Keeps with more notes found.
        val notGreedyElems =
            mdl.getElements.asScala.flatMap(e => if (!e.isGreedy.orElse(mdl.isGreedy)) Some(e.getId) else None).toSet

        sens = sens.groupBy(notNlpNotes(_).groupBy(_.noteType).keys.toSeq.sorted.distinct).
            flatMap { case (types, sensSeq) =>
                if (types.exists(notGreedyElems.contains))
                    sensSeq
                else {
                    val m: Map[NCNlpSentence, Int] = sensSeq.map(p => p -> notNlpNotes(p).size).toMap

                    val max = m.values.max

                    m.filter(_._2 == max).keys
                }
            }.toSeq

        var sensWithNotes = sens.map(s => s -> s.flatten.filter(!_.isNlp).toSet)

        var sensWithNotesIdxs = sensWithNotes.zipWithIndex

        // Drops similar sentences if there are other sentences with superset of notes.
        sens =
            sensWithNotesIdxs.filter { case ((_, notNlpNotes1), idx1) =>
                !sensWithNotesIdxs.
                    filter { case (_, idx2) => idx2 != idx1 }.
                    exists { case((_, notNlpNotes2), _) => notNlpNotes1.subsetOf(notNlpNotes2) }
            }.map { case ((sen, _), _) => sen }

        // Drops similar sentences. Among similar sentences we prefer one with minimal free words count.
        sens = sens.groupBy(notNlpNotes(_).map(_.getKey(withIndexes = false))).
            map { case (_, seq) => seq.minBy(_.filter(p => p.isNlp && !p.isStopWord).map(_.wordIndexes.length).sum) }.
            toSeq

        // Drops sentences if they are just subset of another (indexes ignored here)
        sensWithNotes = sensWithNotes.filter { case (sen, _) => sens.contains(sen) }

        sensWithNotesIdxs = sensWithNotes.zipWithIndex

        sens = sensWithNotesIdxs.filter { case ((_, notNlpNotes1), idx1) =>
            !sensWithNotesIdxs.exists { case ((_, notNlpNotes2), idx2) =>
                idx1 != idx2 && {
                    notNlpNotes2.size > notNlpNotes1.size &&
                    notNlpNotes1.forall(t1 => notNlpNotes2.exists(_.equalsWithoutIndexes(t1)))
                }
            }
        }.map { case ((sen, _), _) => sen }

        sens
    }

    override def start(parent: Span): NCService = {
        ackStarting()

        pool = new java.util.concurrent.ForkJoinPool()

        ackStarted()
    }

    override def stop(parent: Span): Unit = {
        ackStopping()

        U.shutdownPool(pool)

        ackStopped()
    }

    /**
      *
      * @param mdl
      * @param sen
      * @param lastPhase
      */
    def collapse(mdl: NCModel, sen: NCNlpSentence, lastPhase: Boolean = false): Seq[NCNlpSentence] =
        collapseSentence(sen, mdl, lastPhase)

    /**
      *
      * @param srvReqId
      */
    def clearRequestData(srvReqId: String): Unit = combCache -= srvReqId
}