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

package org.apache.nlpcraft.probe.mgrs

import org.apache.nlpcraft.common.nlp.pos.NCPennTreebank
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote, NCNlpSentenceToken}
import org.apache.nlpcraft.common.{NCE, TOK_META_ALIASES_KEY}
import org.apache.nlpcraft.model.NCVariant
import org.apache.nlpcraft.model.impl.{NCTokenImpl, NCVariantImpl}

import java.io.{Serializable ⇒ JSerializable}
import java.util
import java.util.Collections.singletonList
import scala.collection.JavaConverters._
import scala.collection.{Seq, mutable}

/**
  * Sentence to variants converter.
  */
object NCProbeVariants {
    private final val IDXS_SER: JSerializable = singletonList(-1).asInstanceOf[JSerializable]
    private final val IDXS2_SER: JSerializable = singletonList(singletonList(-1)).asInstanceOf[JSerializable]
    private final val IDXS_OBJ: Object = IDXS_SER.asInstanceOf[Object]
    private final val IDXS2_OBJ: Object = IDXS2_SER.asInstanceOf[Object]
    private final val IDX_OBJ = (-1).asInstanceOf[Object]

    private def mkNlpNote(srcToks: Seq[NCNlpSentenceToken]): NCNlpSentenceNote = {
        // Note, it adds stop-words too.
        def mkValue(get: NCNlpSentenceToken ⇒ String): String = {
            val buf = mutable.Buffer.empty[String]

            val n = srcToks.size - 1

            srcToks.zipWithIndex.foreach(p ⇒ {
                val t = p._1
                val idx = p._2

                buf += get(t)

                if (idx < n && t.endCharIndex != srcToks(idx + 1).startCharIndex)
                    buf += " "
            })

            buf.mkString
        }

        def all(is: NCNlpSentenceToken ⇒ Boolean): Boolean = srcToks.forall(is)
        def exists(is: NCNlpSentenceToken ⇒ Boolean): Boolean = srcToks.exists(is)

        val origText = mkValue((t: NCNlpSentenceToken) ⇒ t.origText)

        val params = Seq(
            "index" → -1,
            "pos" → NCPennTreebank.SYNTH_POS,
            "posDesc" → NCPennTreebank.SYNTH_POS_DESC,
            "lemma" → mkValue(_.lemma),
            "origText" → origText,
            "normText" → mkValue(_.normText),
            "stem" → mkValue(_.stem),
            "start" → srcToks.head.startCharIndex,
            "end" → srcToks.last.endCharIndex,
            "charLength" → origText.length,
            "quoted" → false,
            "stopWord" → exists(_.isStopWord),
            "bracketed" → false,
            "direct" → all(_.isDirect),
            "dict" → all(_.isKnownWord),
            "english" → all(_.isEnglish),
            "swear" → exists(_.isSwearWord)
        )

        NCNlpSentenceNote(Seq(-1), srcToks.flatMap(_.wordIndexes).distinct.sorted, "nlpcraft:nlp", params: _*)
    }

    /**
      * Makes variants for given sentences for given model.
      *
      * @param mdl Probe model.
      * @param srvReqId Server request ID.
      * @param sens Sentences.
      * @param lastPhase Flag.
      */
    def convert(srvReqId: String, mdl: NCProbeModel, sens: Seq[NCNlpSentence], lastPhase: Boolean = false): Seq[NCVariant] = {
        val seq = sens.map(_.toSeq.map(nlpTok ⇒ NCTokenImpl(mdl, srvReqId, nlpTok) → nlpTok))
        val toks = seq.map(_.map { case (tok, _) ⇒ tok })

        case class Key(id: String, from: Int, to: Int)

        val keys2Toks = toks.flatten.map(t ⇒ Key(t.getId, t.getStartCharIndex, t.getEndCharIndex) → t).toMap
        val partsKeys = mutable.HashSet.empty[Key]

        val nlpTok2nlpSen: Map[NCNlpSentenceToken, Seq[NCNlpSentence]] =
            sens.
            flatMap(sen ⇒ sen.map(_ → sen)).
            groupBy { case (tok, _) ⇒ tok }.
            map { case (tok, seq) ⇒ tok → seq.map { case (_, sen) ⇒ sen } }

        seq.flatten.foreach { case (tok, tokNlp) ⇒
            if (tokNlp.isUser) {
                val userNotes = tokNlp.filter(_.isUser)

                require(userNotes.size == 1)

                val optList: Option[util.List[util.HashMap[String, JSerializable]]] = userNotes.head.dataOpt("parts")

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

                        val parts = keys.map(key ⇒ {
                            keys2Toks.get(key) match {
                                // Notes for sentence.
                                case Some(t) ⇒
                                    val meta = mutable.HashMap.empty[String, Object]

                                    meta += "nlpcraft:nlp:index" → IDX_OBJ

                                    meta += s"${t.getId}:tokenindexes" → IDXS_OBJ
                                    meta += s"${t.getId}:wordindexes" → IDXS_OBJ

                                    t.getId match {
                                        case "nlpcraft:relation"  ⇒
                                            meta += "nlpcraft:relation:indexes" → IDXS_OBJ
                                        case "nlpcraft:limit" ⇒
                                            meta += "nlpcraft:limit:indexes" → IDXS_OBJ
                                        case "nlpcraft:sort" ⇒
                                            meta += "nlpcraft:sort:subjindexes" → IDXS2_OBJ
                                            meta += "nlpcraft:sort:byindexes" → IDXS2_OBJ
                                        case _ ⇒ // No-op.
                                    }

                                    t.getMetadata.putAll(meta.asJava)

                                    t
                                case None ⇒
                                    // Tries to find between deleted notes.
                                    val delNotes = nlpTok2nlpSen(tokNlp).flatMap(_.deletedNotes).distinct

                                    def find(noteTypePred: String ⇒ Boolean): Option[NCNlpSentenceToken] =
                                        delNotes.toStream.
                                            flatMap { case (delNote, delNoteToks) ⇒
                                                if (noteTypePred(delNote.noteType)) {
                                                    val toks =
                                                        delNoteToks.
                                                            dropWhile(_.startCharIndex != key.from).
                                                            reverse.
                                                            dropWhile(_.endCharIndex != key.to).
                                                            reverse

                                                    toks.size match {
                                                        case 0 ⇒ None
                                                        case _ ⇒
                                                            val artTok = NCNlpSentenceToken(-1)

                                                            artTok.add(mkNlpNote(toks))

                                                            if (key.id != "nlpcraft:nlp") {
                                                                val ps =
                                                                    mutable.ArrayBuffer.empty[(String, JSerializable)]

                                                                ps += "tokenIndexes" → IDXS_SER
                                                                ps += "wordIndexes" → IDXS_SER

                                                                delNote.noteType match {
                                                                    case "nlpcraft:relation" ⇒
                                                                        ps += "indexes" → IDXS_SER
                                                                    case "nlpcraft:limit" ⇒
                                                                        ps += "indexes" → IDXS_SER
                                                                    case "nlpcraft:sort" ⇒
                                                                        ps += "subjindexes" → IDXS2_SER
                                                                        ps += "byindexes" → IDXS2_SER
                                                                    case _ ⇒ // No-op.
                                                                }

                                                                artTok.add(delNote.clone(ps :_*))
                                                            }

                                                            Some(artTok)
                                                    }
                                                }
                                                else
                                                    None
                                            }.headOption

                                    // Tries to find with same key.
                                    var nlpTokOpt = find(_ == key.id)

                                    // If couldn't find nlp note, we can try to find any note on the same position.
                                    if (nlpTokOpt.isEmpty && key.id == "nlpcraft:nlp")
                                        nlpTokOpt = find(_ ⇒ true)

                                    val nlpTok = nlpTokOpt.getOrElse(throw new NCE(s"Part not found for: $key"))

                                    NCTokenImpl(mdl, srvReqId, nlpTok)
                            }
                        })

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
        var vars = toks.filter(sen ⇒
            !sen.exists(t ⇒
                t.getId != "nlpcraft:nlp" &&
                partsKeys.contains(Key(t.getId, t.getStartCharIndex, t.getEndCharIndex))
            )
        ).map(p ⇒ new NCVariantImpl(p.asJava))

        if (lastPhase && vars.size > 1) {
            // Drops empty.
            vars = vars.filter(v ⇒ !v.asScala.forall(_.getId == "nlpcraft:nlp"))

            // Sorts by tokens count, desc.
            val sortedVars = vars.sortBy(p ⇒ -p.asScala.count(_.getId != "nlpcraft:nlp"))

            val bestVars = mutable.ArrayBuffer.empty :+ sortedVars.head

            for (
                vrnt ← sortedVars.tail
                // Skips if the candidate has same structure that exists between already saved and
                // there is only one difference - some candidate's tokens are nlp tokens.
                if !bestVars.exists(savedVrnt ⇒
                    savedVrnt.size == vrnt.size &&
                    savedVrnt.asScala.zip(vrnt.asScala).forall { case (savedTok, tok) ⇒
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

        vars
    }
}
