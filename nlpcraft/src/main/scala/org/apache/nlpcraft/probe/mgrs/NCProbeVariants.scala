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

import org.apache.nlpcraft.common.nlp.pos.NCPennTreebank
import org.apache.nlpcraft.common.nlp.{NCNlpSentence => NlpSentence, NCNlpSentenceNote => NlpNote, NCNlpSentenceToken => NlpToken}
import org.apache.nlpcraft.common.{NCE, TOK_META_ALIASES_KEY}
import org.apache.nlpcraft.model.impl.{NCTokenImpl, NCTokenLogger, NCVariantImpl}
import org.apache.nlpcraft.model.{NCToken, NCVariant}

import java.io.{Serializable => JSerializable}
import java.util
import java.util.Collections.singletonList
import scala.collection.mutable

/**
  * Sentence to variants converter.
  */
object NCProbeVariants {
    private final val IDX: java.lang.Integer = -1
    private final val IDXS: JSerializable = singletonList(IDX).asInstanceOf[JSerializable]
    private final val IDXS2: JSerializable = singletonList(singletonList(IDX)).asInstanceOf[JSerializable]

    case class Key(id: String, from: Int, to: Int)

    object Key {
        def apply(m: util.HashMap[String, JSerializable]): Key = {
            def get[T](name: String): T = m.get(name).asInstanceOf[T]

            Key(get("id"), get("startcharindex"), get("endcharindex"))
        }

        def apply(t: NCToken): Key = Key(t.getId, t.getStartCharIndex, t.getEndCharIndex)
    }

    /**
      *
      * @param t
      * @return
      */
    private def convertTokenMetaIndexes(t: NCTokenImpl) : NCTokenImpl = {
        val meta = mutable.HashMap.empty[String, Any] ++
            Map(
                "nlpcraft:nlp:index" -> IDX,
                s"${t.getId}:tokenindexes" -> IDXS,
                s"${t.getId}:wordindexes" -> IDXS
            )

        t.getId match {
            case "nlpcraft:relation" | "nlpcraft:limit" => meta += "nlpcraft:relation:indexes" -> IDXS
            case "nlpcraft:sort" => meta += "nlpcraft:sort:subjindexes" -> IDXS2; meta += "nlpcraft:sort:byindexes" -> IDXS2
            case _ => // No-op.
        }

        t.getMetadata.putAll(meta.map(p => p._1 -> p._2.asInstanceOf[Object]).asJava)

        t
    }

    /**
      *
      * @param key
      * @param delNotes
      * @param noteTypePred
      * @return
      */
    private def findDeletedToken(
        key: Key,
        delNotes: Map[NlpNote, Seq[NlpToken]],
        noteTypePred: String => Boolean
    ): Option[NlpToken] =
        delNotes.toStream.
            flatMap { case (delNote, delNoteToks) =>
                if (noteTypePred(delNote.noteType)) {
                    val toks =
                        delNoteToks.
                            dropWhile(_.startCharIndex != key.from).
                            reverse.
                            dropWhile(_.endCharIndex != key.to).
                            reverse

                    toks.size match {
                        case 0 => None
                        case _ =>
                            val artTok = NlpToken(IDX)

                            artTok.add(mkNote(toks))

                            if (key.id != "nlpcraft:nlp") {
                                val ps = mkNlpNoteParams()

                                delNote.noteType match {
                                    case "nlpcraft:relation" | "nlpcraft:limit" => ps += "indexes" -> IDXS
                                    case "nlpcraft:sort" => ps += "subjindexes" -> IDXS2; ps += "byindexes" ->  IDXS2
                                    case _ => // No-op.
                                }

                                artTok.add(delNote.clone(ps :_*))
                            }

                            Some(artTok)
                    }
                }
                else
                    None
            }.headOption

    /**
      *
      * @return
      */
    private def mkNlpNoteParams(): mutable.ArrayBuffer[(String, JSerializable)] =
        mutable.ArrayBuffer.empty[(String, JSerializable)] ++ Seq("tokMinIndex" -> IDX, "tokMaxIndex" -> IDX)

    /**
      *
      * @param srcToks
      * @return
      */
    private def mkNote(srcToks: Seq[NlpToken]): NlpNote = {
        // Note, it adds stop-words too.
        def mkValue(get: NlpToken => String): String = {
            val buf = mutable.Buffer.empty[String]

            val n = srcToks.size - 1

            srcToks.zipWithIndex.foreach { case (t, idx) =>
                buf += get(t)

                if (idx < n && t.endCharIndex != srcToks(idx + 1).startCharIndex)
                    buf += " "
            }

            buf.mkString
        }

        def all(is: NlpToken => Boolean): Boolean = srcToks.forall(is)
        def exists(is: NlpToken => Boolean): Boolean = srcToks.exists(is)

        val origText = mkValue((t: NlpToken) => t.origText)

        val params = Seq(
            "index" -> IDX,
            "pos" -> NCPennTreebank.SYNTH_POS,
            "posDesc" -> NCPennTreebank.SYNTH_POS_DESC,
            "lemma" -> mkValue(_.lemma),
            "origText" -> origText,
            "normText" -> mkValue(_.normText),
            "stem" -> mkValue(_.stem),
            "start" -> srcToks.head.startCharIndex,
            "end" -> srcToks.last.endCharIndex,
            "charLength" -> origText.length,
            "quoted" -> false,
            "stopWord" -> exists(_.isStopWord),
            "bracketed" -> false,
            "direct" -> all(_.isDirect),
            "dict" -> all(_.isKnownWord),
            "english" -> all(_.isEnglish),
            "swear" -> exists(_.isSwearWord)
        )

        NlpNote(Seq(IDX.intValue()), srcToks.flatMap(_.wordIndexes).distinct.sorted, "nlpcraft:nlp", params: _*)
    }

    /**
      * Makes variants for given sentences for given model.
      *
      * @param mdl Probe model.
      * @param srvReqId Server request ID.
      * @param nlpSens Sentences.
      * @param lastPhase Flag.
      */
    def convert(srvReqId: String, mdl: NCProbeModel, nlpSens: Seq[NlpSentence], lastPhase: Boolean = false): Seq[NCVariant] = {
        var vars =
            nlpSens.flatMap(nlpSen => {
                var ok = true

                def mkToken(nlpTok: NlpToken): NCTokenImpl = {
                    val ncTok = NCTokenImpl(mdl, srvReqId, nlpTok)

                    nlpSen.addNlpToken(nlpTok)

                    ncTok
                }

                val toks = nlpSen.map(mkToken)
                val keys2Toks = toks.map(t => Key(t) -> t).toMap

                def process(tok: NCTokenImpl, tokNlp: NlpToken): Unit = {
                    val optList: Option[util.List[util.HashMap[String, JSerializable]]] =
                        tokNlp.find(_.isUser) match {
                            case Some(u) => u.dataOpt("parts")
                            case None => None
                        }

                    optList match {
                        case Some(list) =>
                            val keys = list.asScala.map(Key(_))

                            val parts = keys.map(key =>
                                keys2Toks.get(key) match {
                                    // Notes for sentence.
                                    case Some(t) => convertTokenMetaIndexes(t)
                                    case None =>
                                        val delNotes = nlpSen.getDeletedNotes

                                        // Tries to find with same key.
                                        var nlpTokOpt = findDeletedToken(key, delNotes, _ == key.id)

                                        // If couldn't find nlp note, we can try to find any note on the same position.
                                        if (nlpTokOpt.isEmpty && key.id == "nlpcraft:nlp")
                                            nlpTokOpt = findDeletedToken(key, delNotes, _ => true)

                                        nlpTokOpt match {
                                            case Some(nlpTok) => mkToken(nlpTok)
                                            case None =>
                                                nlpSen.getInitialNlpNote(key.from, key.to) match {
                                                    case Some(nlpNote) =>
                                                        val artTok = NlpToken(IDX)

                                                        artTok.add(nlpNote.clone(mkNlpNoteParams(): _*))

                                                        mkToken(artTok)
                                                    case None =>
                                                        throw new NCE(
                                                            s"Part not found [" +
                                                            s"key=$key, " +
                                                            s"token=$tok, " +
                                                            s"lastPhase=$lastPhase" +
                                                        s"]")
                                                }
                                        }
                                }
                            )

                            parts.zip(list.asScala).foreach { case (part, map) =>
                                map.get(TOK_META_ALIASES_KEY) match {
                                    case null => // No-op.
                                    case aliases => part.getMetadata.put(TOK_META_ALIASES_KEY, aliases.asInstanceOf[Object])
                                }
                            }

                            tok.setParts(parts)

                            require(parts.nonEmpty)

                            for (tok <- parts)
                                process(tok,
                                    nlpSen.
                                        getNlpToken(tok.getId, tok.getStartCharIndex, tok.getEndCharIndex).
                                        getOrElse(throw new NCE(s"Token not found for $tok"))
                                )

                            ok = ok && !toks.exists(t => t.getId != "nlpcraft:nlp" && keys.contains(Key(t)))
                        case None => // No-op.
                    }
                }

                for ((tok, tokNlp) <- toks.zip(nlpSen) if tokNlp.isUser)
                    process(tok, tokNlp)

                if (ok) Some(new NCVariantImpl(toks.asJava)) else None
            })

        if (lastPhase && vars.size > 1) {
            // Drops empty.
            vars = vars.filter(v => !v.asScala.forall(_.getId == "nlpcraft:nlp"))

            // Sorts by tokens count, desc.
            val sortedVars = vars.sortBy(p => -p.asScala.count(_.getId != "nlpcraft:nlp"))

            val bestVars = mutable.ArrayBuffer.empty :+ sortedVars.head

            for (
                vrnt <- sortedVars.tail
                    // Skips if the candidate has same structure that exists between already saved and
                    // there is only one difference - some candidate's tokens are nlp tokens.
                    if !bestVars.exists(savedVrnt =>
                        savedVrnt.size == vrnt.size &&
                            savedVrnt.asScala.zip(vrnt.asScala).forall { case (savedTok, tok) =>
                                savedTok.getStartCharIndex == tok.getStartCharIndex &&
                                savedTok.getEndCharIndex == tok.getEndCharIndex &&
                                (
                                    savedTok.getId == tok.getId && savedTok.getMetadata == tok.getMetadata ||
                                    tok.getId == "nlpcraft:nlp"
                                )
                            }
                    )
            )
                bestVars += vrnt

            if (bestVars.size != vars.size)
                // Reverts orders.
                vars = bestVars.sortBy(sortedVars.indexOf)
        }

        for (v <- vars; t <- v.asScala)
            require(
                t.getIndex >= 0,
                s"Invalid token: $t with index: ${t.getIndex}, " +
                    s"lastPhase: $lastPhase, " +
                    s"sentence:\n${NCTokenLogger.prepareTable(v.asScala)}" +
                    s""
            )

        vars
    }
}