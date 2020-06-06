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

import java.io.Serializable
import java.util

import org.apache.nlpcraft.common.TOK_META_ALIASES_KEY
import org.apache.nlpcraft.common.nlp.NCNlpSentence
import org.apache.nlpcraft.model.impl.{NCTokenImpl, NCVariantImpl}
import org.apache.nlpcraft.model.{NCElement, NCModel, NCVariant}

import scala.collection.JavaConverters._
import scala.collection.{Seq, mutable}
import scala.language.implicitConversions

/**
  *
  * @param model Decorated model.
  * @param synonyms Fast-access synonyms map for first phase.
  * @param synonymsDsl Fast-access synonyms map for second phase.
  * @param additionalStopWordsStems Stemmatized additional stopwords.
  * @param excludedStopWordsStems Stemmatized excluded stopwords.
  * @param suspiciousWordsStems Stemmatized suspicious stopwords.
  * @param elements Map of model elements.
  */
case class NCModelDecorator(
    model: NCModel,
    synonyms: Map[String/*Element ID*/, Map[Int/*Synonym length*/, Seq[NCSynonym]]], // Fast access map.
    synonymsDsl: Map[String/*Element ID*/, Map[Int/*Synonym length*/, Seq[NCSynonym]]], // Fast access map.
    additionalStopWordsStems: Set[String],
    excludedStopWordsStems: Set[String],
    suspiciousWordsStems: Set[String],
    elements: Map[String/*Element ID*/, NCElement]
) extends java.io.Serializable {
    /**
      * Makes variants for given sentences.
      *
      * @param srvReqId Server request ID.
      * @param sens Sentences.
      */
    def makeVariants(srvReqId: String, sens: Seq[NCNlpSentence]): Seq[NCVariant] = {
        val seq = sens.map(_.toSeq.map(nlpTok ⇒ NCTokenImpl(this, srvReqId, nlpTok) → nlpTok))
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
        ).map(p ⇒ new NCVariantImpl(p.asJava))
    }

    override def toString: String = {
        s"Probe model decorator [" +
            s"id=${model.getId}, " +
            s"name=${model.getName}, " +
            s"version=${model.getVersion}" +
        s"]"
    }
}
