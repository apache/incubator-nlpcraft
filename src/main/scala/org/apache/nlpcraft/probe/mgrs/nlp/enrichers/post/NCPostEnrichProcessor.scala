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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.post

import java.io.Serializable
import java.util

import com.typesafe.scalalogging.LazyLogging
import io.opencensus.trace.Span
import org.apache.nlpcraft.common.nlp.pos._
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, _}
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.model.impl.NCTokenImpl
import org.apache.nlpcraft.probe.mgrs.NCModelDecorator

import scala.collection.JavaConverters._
import scala.collection._

/**
  * This collapser handles several tasks:
  * - "overall" collapsing after all other individual collapsers had their turn.
  * - Special further enrichment of tokens like linking, etc.
  *
  * In all cases of overlap (full or partial) - the "longest" note wins. In case of overlap and equal
  * lengths - the winning note is chosen based on this priority.
  */
object NCPostEnrichProcessor extends NCService with LazyLogging {
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        super.start()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }

    /**
      *
      * @param p
      * @return
      */
    private def getParameters(p: NCNlpSentenceNote): Any =
        if (p.isUser)
            (p.wordIndexes, p.noteType)
        else {
            p.noteType match {
                case "nlpcraft:continent" ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("continent")
                    )
                case "nlpcraft:subcontinent" ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("continent"),
                        p.get("subcontinent")
                    )
                case "nlpcraft:country" ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("continent"),
                        p.get("subcontinent"),
                        p.get("country")
                    )
                case "nlpcraft:region" ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("continent"),
                        p.get("subcontinent"),
                        p.get("country"),
                        p.get("region")
                    )
                case "nlpcraft:city" ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("continent"),
                        p.get("subcontinent"),
                        p.get("country"),
                        p.get("region"),
                        p.get("city")
                    )
                case "nlpcraft:metro" ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("metro")
                    )
                case "nlpcraft:date" ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("from"),
                        p.get("to")
                    )
                case "nlpcraft:aggregation" ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("type"),
                        p.get("indexes"),
                        p.get("note")
                    )
                case "nlpcraft:relation" ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("type"),
                        p.get("indexes"),
                        p.get("note")
                    )
                case "nlpcraft:sort" ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("asc"),
                        p.get("indexes"),
                        p.get("note")
                    )
                case "nlpcraft:limit" ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("limit"),
                        p.getOrElse("asc", null),
                        p.get("indexes"),
                        p.get("note")
                    )
                case "nlpcraft:coordinate" ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("latitude"),
                        p.get("longitude")
                    )
                case "nlpcraft:num" ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("from"),
                        p.get("to"),
                        p.getOrElse("indexes", null),
                        p.getOrElse("note", null)

                    )
                case x if x.startsWith("google:") ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("meta"),
                        p.get("mentionsBeginOffsets"),
                        p.get("mentionsContents"),
                        p.get("mentionsTypes")
                    )
                case x if x.startsWith("stanford:") ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("nne")
                    )
                case x if x.startsWith("opennlp:") ⇒
                    (
                        p.wordIndexes,
                        p.noteType
                    )
                case x if x.startsWith("spacy:") ⇒
                    (
                        p.wordIndexes,
                        p.noteType,
                        p.get("vector")
                    )

                case _ ⇒ throw new AssertionError(s"Unexpected note type: ${p.noteType}")
            }
        }

    /**
      * Fixes tokens positions.
      *
      * @param ns Sentence.
      * @param notNlpTypes Token types.
      * @param idCache ID cache.
      */
    private def collapse(
        ns: NCNlpSentence,
        notNlpTypes: Seq[String],
        idCache: mutable.HashMap[String, String]
    ): Boolean = {
        ns.
            filter(!_.isNlp).
            filter(_.isStopWord).
            flatten.
            filter(_.isNlp).
            foreach(_ += "stopWord" → false)

        val nsNotes: Map[String, Seq[Int]] = ns.tokens.flatten.map(p ⇒ p.noteType → p.tokenIndexes).toMap

        for (
            t ← ns.tokens;
            stopReason ← t.stopsReasons
            if nsNotes.getOrElse(stopReason.noteType, Seq.empty) == stopReason.tokenIndexes
        )
            t.markAsStop()

        val history = mutable.ArrayBuffer.empty[(Int, Int)]

        notNlpTypes.foreach(typ ⇒ zipNotes(ns, typ, notNlpTypes, history, idCache))

        unionStops(ns, notNlpTypes, history, idCache)

        val res =
            Seq("nlpcraft:aggregation", "nlpcraft:relation", "nlpcraft:limit").
                forall(t ⇒ fixIndexesReferences(t, ns, history)) &&
            fixIndexesReferencesList("nlpcraft:sort", "subjIndexes", "subjNotes", ns, history) &&
            fixIndexesReferencesList("nlpcraft:sort", "byIndexes", "byNotes", ns, history)

        if (res)
            // Validation (all indexes calculated well)
            require(
                !ns.
                    flatten.
                    exists(n ⇒ ns.filter(_.wordIndexes.exists(n.wordIndexes.contains)).exists(p ⇒ !p.contains(n.id))),
                s"Invalid sentence:\n" +
                    ns.map(t ⇒
                        // Human readable invalid sentence for debugging.
                        s"${t.origText}{index:${t.index}}[${t.map(n ⇒ s"${n.noteType}, {range:${n.tokenFrom}-${n.tokenTo}}").mkString("|")}]"
                    ).mkString("\n")
            )
        res
    }

    /**
      *
      * @param ns
      * @param idxs
      * @param notesType
      * @param id
      * @return
      */
    private def checkRelation(ns: NCNlpSentence, idxs: Seq[Int], notesType: String, id: String): Boolean = {
        val types =
            idxs.flatMap(idx ⇒ {
                val types = ns(idx).map(p ⇒ p).filter(!_.isNlp).map(_.noteType)

                types.size match {
                    case 0 ⇒ None
                    case 1 ⇒ Some(types.head)
                    case _ ⇒ throw new AssertionError(s"Unexpected tokes: ${ns(idx)}")
                }
            }).distinct


        /**
        Example:
             1. Sentence 'maximum x' (single element related function)
              - maximum is aggregate function linked to date element.
              - x defined as 2 elements: date and num.
              So, the variant 'maximum x (as num)' should be excluded.

              2. Sentence 'compare x and y' (multiple elements related function)
              - compare is relation function linked to date element.
              - x an y defined as 2 elements: date and num.
              So, variants 'x (as num) and x (as date)'  and 'x (as date) and x (as num)'
              should't be excluded, but invalid relation should be deleted for these combinations.
          */

        types.size match {
            case 0 ⇒ throw new AssertionError(s"Unexpected empty types [notesType=$notesType]")
            case 1 ⇒ types.head == notesType
            case _ ⇒
                // Equal elements should be processed together with function element.
                if (types.size == 1)
                    false
                else {
                    ns.removeNote(id)

                    true
                }
        }
    }

    /**
      * Fixes notes with references to other notes indexes.
      * Note that 'idxsField' is 'indexes' and 'noteField' is 'note' for all kind of references.
      *
      * @param noteType Note type.
      * @param ns Sentence.
      * @param history Indexes transformation history.
      * @return Valid flag.
      */
    private def fixIndexesReferences(noteType: String, ns: NCNlpSentence, history: Seq[(Int, Int)]): Boolean = {
        ns.filter(_.isTypeOf(noteType)).foreach(tok ⇒
            tok.getNoteOpt(noteType, "indexes") match {
                case Some(n) ⇒
                    val idxs: Seq[Int] = n.data[java.util.List[Int]]("indexes").asScala
                    var fixed = idxs

                    history.foreach { case (idxOld, idxNew) ⇒ fixed = fixed.map(i ⇒ if (i == idxOld) idxNew else i) }

                    fixed = fixed.distinct

                    if (idxs != fixed) {
                        n += "indexes" → fixed.asJava.asInstanceOf[java.io.Serializable]

                        def x(seq: Seq[Int]): String = s"[${seq.mkString(", ")}]"

                        logger.trace(s"`$noteType` note `indexes` fixed [old=${x(idxs)}}, new=${x(fixed)}]")
                    }
                case None ⇒ // No-op.
            }
        )

        ns.flatMap(_.getNotes(noteType)).forall(
            n ⇒ checkRelation(ns, n.data[java.util.List[Int]]("indexes").asScala, n.data[String]("note"), n.id)
        )
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
        ns.filter(_.isTypeOf(noteType)).foreach(tok ⇒
            tok.getNoteOpt(noteType, idxsField) match {
                case Some(n) ⇒
                    val idxs: Seq[Seq[Int]] = n.data[java.util.List[java.util.List[Int]]](idxsField).asScala.map(_.asScala)
                    var fixed = idxs

                    history.foreach { case (idxOld, idxNew) ⇒ fixed = fixed.map(_.map(i ⇒ if (i == idxOld) idxNew else i).distinct) }

                    if (idxs != fixed) {
                        fixed.foreach(p ⇒ require(p.size == 1))

                        // Fix double dimension array to one dimension.
                        n += idxsField → fixed.map(_.head).asJava.asInstanceOf[java.io.Serializable]

                        def x(seq: Seq[Seq[Int]]): String = s"[${seq.map(p ⇒ s"[${p.mkString(",")}]").mkString(", ")}]"

                        logger.trace(s"`$noteType` note `indexes` fixed [old=${x(idxs)}}, new=${x(fixed)}]")
                    }
                case None ⇒ // No-op.
            }
        )

        ns.flatMap(_.getNotes(noteType)).forall(rel ⇒
            rel.dataOpt[java.util.List[Int]](idxsField) match {
                case Some(idxsList) ⇒
                    val notesTypes = rel.data[util.List[String]](noteField)

                    require(idxsList.size() == notesTypes.size())

                    idxsList.asScala.zip(notesTypes.asScala).forall {
                        case (idxs, notesType) ⇒ checkRelation(ns, Seq(idxs), notesType, rel.id)
                    }
                case None ⇒ true
            }
        )
    }

    /**
      * Zip notes with same type.
      *
      * @param ns Sentence.
      * @param nType Notes type.
      * @param userNotesTypes Notes types.
      * @param history Indexes transformation history.
      * @param idCache ID cache.
      */
    private def zipNotes(
        ns: NCNlpSentence,
        nType: String,
        userNotesTypes: Seq[String],
        history: mutable.ArrayBuffer[(Int, Int)],
        idCache: mutable.HashMap[String, String]
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

                            ns += mkCompound(ns, nsCopyToks, n.tokenIndexes, stop = false, ns.size, Some(n), history, idCache)
                        }
                    case None ⇒ simpleCopy(ns, history, nsCopyToks, i)
                }

            fixIndexes(ns, userNotesTypes)
        }
    }

    /**
      * Glues stop words.
      *
      * @param ns Sentence.
      * @param userNoteTypes Notes types.
      * @param history Indexes transformation history.
      * @param idCache ID cache.
      */
    private def unionStops(
        ns: NCNlpSentence,
        userNoteTypes: Seq[String],
        history: mutable.ArrayBuffer[(Int, Int)],
        idCache: mutable.HashMap[String, String]
    ): Unit = {
        // Java collection used because using scala collections (mutable.Buffer.empty[mutable.Buffer[Token]]) is reason
        // Of compilation errors which seems as scala compiler internal error.
        import java.util

        import scala.collection.JavaConverters._

        val bufs = new util.ArrayList[mutable.Buffer[NCNlpSentenceToken]]()

        def last[T](l: util.List[T]): T = l.get(l.size() - 1)

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

                            ns += mkCompound(ns, nsCopyToks, idxs, stop = true, ns.size, None, history, idCache)
                        }
                    case None ⇒ simpleCopy(ns, history, nsCopyToks, i)
                }

            fixIndexes(ns, userNoteTypes)
        }
    }

    /**
      * Copies token.
      *
      * @param ns Sentence.
      * @param history Indexes transformation history.
      * @param toksCopy Copied tokens.
      * @param i Index.
      */
    private def simpleCopy(ns: NCNlpSentence, history: mutable.ArrayBuffer[(Int, Int)], toksCopy: NCNlpSentence, i: Int): Seq[NCNlpSentenceToken] = {
        val tokCopy = toksCopy(i)

        history += tokCopy.index → ns.size

        ns += tokCopy.clone(ns.size)
    }

    /**
      * Fixes indexes for all notes after recreating tokens.
      *
      * @param ns            Sentence.
      * @param userNoteTypes Notes types.
      */
    private def fixIndexes(ns: NCNlpSentence, userNoteTypes: Seq[String]) {
        // Replaces other notes indexes.
        for (t ← userNoteTypes :+ "nlpcraft:nlp"; note ← ns.getNotes(t)) {
            val id = note.id

            val toks = ns.filter(_.contains(id)).sortBy(_.index)

            val newNote = note.clone(toks.map(_.index), toks.flatMap(_.wordIndexes).sorted)

            toks.foreach(t ⇒ {
                t.remove(id)
                t.add(newNote)
            })
        }

        // Special case - field index of core NLP note.
        ns.zipWithIndex.foreach { case (tok, idx) ⇒ tok.getNlpNote += "index" → idx }
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
      * @param idCache ID cache.
      */
    private def mkCompound(
        ns: NCNlpSentence,
        nsCopyToks: Seq[NCNlpSentenceToken],
        indexes: Seq[Int],
        stop: Boolean,
        idx: Int,
        commonNote: Option[NCNlpSentenceNote],
        history: mutable.ArrayBuffer[(Int, Int)],
        idCache: mutable.HashMap[String, String]
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

        val complexId = content.map(_.getNlpNote.id).mkString(" ")

        val id =
            idCache.get(complexId) match {
                case Some(cachedId) ⇒ cachedId
                case None ⇒
                    val id = U.genGuid()

                    idCache += complexId → id

                    id
            }

        val nlpNote = NCNlpSentenceNote(id, idxs, wordIdxs, "nlpcraft:nlp", params: _*)

        t.add(nlpNote)

        // Adds processed note with fixed indexes.
        commonNote match {
            case Some(n) ⇒
                ns.removeNote(n.id)
                t.add(n.clone(idxs, wordIdxs))
            case None ⇒ // No-op.
        }

        t
    }

    /**
      *
      * @param mdl
      * @param ns
      * @param parent Optional parent span.
      * @return
      */
    @throws[NCE]
    def collapse(mdl: NCModelDecorator, ns: NCNlpSentence, parent: Span = null): Seq[NCNlpSentence] =
        startScopedSpan("collapse", parent,
            "srvReqId" → ns.srvReqId,
            "txt" → ns.text,
            "modelId" → mdl.model.getId) { _ ⇒
            // Always deletes `similar` notes.
            // Some words with same note type can be detected various ways.
            // We keep only one variant -  with `best` direct and sparsity parameters,
            // other variants for these words are redundant.
            val idCache = mutable.HashMap.empty[String, String]

            val redundant: Seq[NCNlpSentenceNote] =
                ns.flatten.filter(!_.isNlp).distinct.
                    groupBy(getParameters).
                    map(p ⇒ p._2.sortBy(p ⇒
                        (
                            // System notes don't have such flags.
                            if (p.isUser) {
                                if (p.isDirect) 0 else 1
                            }
                            else
                                0,
                            if (p.isUser) p.sparsity else 0
                        )
                    )).
                    flatMap(_.drop(1)).
                    toSeq

            redundant.map(_.id).foreach(ns.removeNote)

            def getNotNlpNotes(toks: Seq[NCNlpSentenceToken]): Seq[NCNlpSentenceNote] =
                toks.flatten.filter(!_.isNlp).distinct

            val notNlpTypes = getNotNlpNotes(ns).map(_.noteType).distinct

            val delCombs: Seq[NCNlpSentenceNote] =
                getNotNlpNotes(ns).
                    flatMap(note ⇒ getNotNlpNotes(ns.slice(note.tokenFrom, note.tokenTo + 1)).filter(_ != note)).
                    distinct

            val toksByIdx: Seq[Seq[NCNlpSentenceNote]] =
                delCombs.flatMap(note ⇒ note.wordIndexes.map(_ → note)).
                    groupBy { case (idx, _) ⇒ idx }.
                    map { case (_, seq) ⇒ seq.map { case (_, note) ⇒ note } }.
                    toSeq.sortBy(-_.size)

            val minDelSize = if (toksByIdx.isEmpty) 1 else toksByIdx.map(_.size).max - 1

            val sens =
                if (delCombs.nonEmpty) {
                    val deleted = mutable.ArrayBuffer.empty[Seq[NCNlpSentenceNote]]

                    val sens =
                        (minDelSize to delCombs.size).
                            flatMap(i ⇒
                                delCombs.combinations(i).
                                    filter(delComb ⇒ !toksByIdx.exists(_.count(note ⇒ !delComb.contains(note)) > 1))
                            ).
                            sortBy(_.size).
                            flatMap(delComb ⇒
                                // Already processed with less subset of same deleted tokens.
                                if (!deleted.exists(_.forall(delComb.contains))) {
                                    val nsClone = ns.clone()

                                    delComb.map(_.id).foreach(nsClone.removeNote)

                                    // Has overlapped notes for some tokens.
                                    require(!nsClone.exists(_.count(!_.isNlp) > 1))

                                    deleted += delComb

                                    if (collapse(nsClone, notNlpTypes, idCache)) Some(nsClone) else None
                                }
                                else
                                    None
                            )

                    // Removes sentences which have only one difference - 'direct' flag of their user tokens.
                    // `Direct` sentences have higher priority.
                    case class Key(
                        sysNotes: Seq[mutable.HashMap[String, java.io.Serializable]],
                        userNotes: Seq[mutable.HashMap[String, java.io.Serializable]]
                    )
                    case class Value(sentence: NCNlpSentence, directCount: Int)

                    val m = mutable.HashMap.empty[Key, Value]

                    sens.map(sen ⇒ {
                        val sysNotes = sen.flatten.filter(_.isSystem)
                        val nlpNotes = sen.flatten.filter(_.isNlp)
                        val userNotes = sen.flatten.filter(_.isUser)

                        def get(seq: Seq[NCNlpSentenceNote], keys2Skip: String*): Seq[mutable.HashMap[String, java.io.Serializable]] =
                            seq.map(p ⇒ {
                                val m: mutable.HashMap[String, java.io.Serializable] = p.clone()

                                // We have to delete some keys to have possibility to compare sentences.
                                m.remove("unid")
                                m.remove("direct")

                                m
                            })

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
                else {
                    if (collapse(ns, notNlpTypes, idCache)) Seq(ns) else Seq.empty
                }.distinct

            sens.foreach(sen ⇒
                sen.foreach(tok ⇒
                    tok.size match {
                        case 1 ⇒ require(tok.head.isNlp, s"Unexpected non-'nlpcraft:nlp' token: $tok")
                        case 2 ⇒ require(tok.head.isNlp ^ tok.last.isNlp, s"Unexpected token notes: $tok")
                        case _ ⇒ require(false, s"Unexpected token notes count: $tok")
                    }
                )
            )

            sens
        }

    /**
      *
      * @param mdl
      * @param srvReqId
      * @param nlpToks
      * @return
      */
    def convert(mdl: NCModelDecorator, srvReqId: String, nlpToks: Seq[Seq[NCNlpSentenceToken]]): Seq[Seq[NCToken]] = {
        val seq = nlpToks.map(_.map(nlpTok ⇒ NCTokenImpl(mdl, srvReqId, nlpTok) → nlpTok))
        val toks = seq.map(_.map { case (tok, _) ⇒ tok })

        case class Key(id: String, from: Int, to: Int)

        val keys2Toks = toks.flatten.map(t ⇒ Key(t.getId, t.getStartCharIndex, t.getEndCharIndex) → t).toMap
        val partsKeys = mutable.HashSet.empty[Key]

        seq.flatten.foreach { case (tok, tokNlp) ⇒
            if (tokNlp.isUser) {
                val userNotes = tokNlp.filter(_.isUser)

                require(userNotes.size == 1)

                val optList: Option[util.List[util.HashMap[String, Serializable]]] = userNotes.head.dataOpt("parts")

                optList match {
                    case Some(list) ⇒
                        val keys =
                            list.asScala.map(m ⇒
                                Key(
                                    m.get("id").asInstanceOf[String],
                                    m.get("startcharindex").asInstanceOf[Integer],
                                    m.get("endcharindex").asInstanceOf[Integer]
                                )
                            )
                        val parts = keys.map(keys2Toks)

                        parts.zip(list.asScala).foreach { case (part, map) ⇒
                            map.get(TOK_META_ALIASES_KEY) match {
                                case null ⇒ // No-op.
                                case aliases ⇒ part.getMetadata.put(TOK_META_ALIASES_KEY, aliases.asInstanceOf[Object])
                            }
                        }

                        tok.setParts(parts)
                        partsKeys ++= keys

                    case None ⇒ // No-op.
                }
            }
        }

        //  We can't collapse parts earlier, because we need them here (setParts method, few lines above.)
        toks.filter(sen ⇒
            !sen.exists(t ⇒
                t.getId != "nlpcraft:nlp" &&
                    partsKeys.contains(Key(t.getId, t.getStartCharIndex, t.getEndCharIndex))
            )
        )
    }
}
