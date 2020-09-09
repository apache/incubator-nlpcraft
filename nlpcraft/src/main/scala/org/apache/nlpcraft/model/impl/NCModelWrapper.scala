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

package org.apache.nlpcraft.model.impl

import java.io.Serializable
import java.util

import org.apache.nlpcraft.common.TOK_META_ALIASES_KEY
import org.apache.nlpcraft.common.nlp.NCNlpSentence
import org.apache.nlpcraft.model.intent.impl.NCIntentSolver
import org.apache.nlpcraft.model.{NCContext, NCCustomParser, NCElement, NCIntentMatch, NCModel, NCRejection, NCResult, NCVariant}
import org.apache.nlpcraft.probe.mgrs.NCSynonym

import scala.collection.JavaConverters._
import scala.collection.{Seq, mutable}

/**
  *
  * @param proxy
  * @param solver
  * @param syns
  * @param synsDsl
  * @param addStopWordsStems
  * @param exclStopWordsStems
  * @param suspWordsStems
  * @param elms
  */
case class NCModelWrapper(
    proxy: NCModel,
    solver: NCIntentSolver,
    syns: Map[String/*Element ID*/, Map[Int/*Synonym length*/, Seq[NCSynonym]]], // Fast access map.
    synsDsl: Map[String/*Element ID*/, Map[Int/*Synonym length*/, Seq[NCSynonym]]], // Fast access map.
    addStopWordsStems: Set[String],
    exclStopWordsStems: Set[String],
    suspWordsStems: Set[String],
    elms: Map[String/*Element ID*/, NCElement]
) extends NCModel {
    require(proxy != null)
    
    override def getId: String = proxy.getId
    override def getName: String = proxy.getName
    override def getVersion: String = proxy.getVersion
    override def getDescription: String = proxy.getDescription
    override def getMaxUnknownWords: Int = proxy.getMaxUnknownWords
    override def getMaxFreeWords: Int = proxy.getMaxFreeWords
    override def getMaxSuspiciousWords: Int = proxy.getMaxSuspiciousWords
    override def getMinWords: Int = proxy.getMinWords
    override def getMaxWords: Int = proxy.getMaxWords
    override def getMinTokens: Int = proxy.getMinTokens
    override def getMaxTokens: Int = proxy.getMaxTokens
    override def getMinNonStopwords: Int = proxy.getMinNonStopwords
    override def isNonEnglishAllowed: Boolean = proxy.isNonEnglishAllowed
    override def isNotLatinCharsetAllowed: Boolean = proxy.isNotLatinCharsetAllowed
    override def isSwearWordsAllowed: Boolean = proxy.isSwearWordsAllowed
    override def isNoNounsAllowed: Boolean = proxy.isNoNounsAllowed
    override def isPermutateSynonyms: Boolean = proxy.isPermutateSynonyms
    override def isDupSynonymsAllowed: Boolean = proxy.isDupSynonymsAllowed
    override def getMaxTotalSynonyms: Int = proxy.getMaxTotalSynonyms
    override def isNoUserTokensAllowed: Boolean = proxy.isNoUserTokensAllowed
    override def getJiggleFactor: Int = proxy.getJiggleFactor
    override def getMetadata: util.Map[String, AnyRef] = proxy.getMetadata
    override def getAdditionalStopWords: util.Set[String] = proxy.getAdditionalStopWords
    override def getExcludedStopWords: util.Set[String] = proxy.getExcludedStopWords
    override def getSuspiciousWords: util.Set[String] = proxy.getSuspiciousWords
    override def getMacros: util.Map[String, String] = proxy.getMacros
    override def getParsers: util.List[NCCustomParser] = proxy.getParsers
    override def getElements: util.Set[NCElement] = proxy.getElements
    override def getEnabledBuiltInTokens: util.Set[String] = proxy.getEnabledBuiltInTokens
    override def onParsedVariant(`var`: NCVariant): Boolean = proxy.onParsedVariant(`var`)
    override def onContext(ctx: NCContext): NCResult = proxy.onContext(ctx)
    override def onMatchedIntent(ctx: NCIntentMatch): Boolean = proxy.onMatchedIntent(ctx)
    override def onResult(ctx: NCIntentMatch, res: NCResult): NCResult = proxy.onResult(ctx, res)
    override def onRejection(ctx: NCIntentMatch, e: NCRejection): NCResult = proxy.onRejection(ctx, e)
    override def onError(ctx: NCContext, e: Throwable): NCResult = proxy.onError(ctx, e)
    override def onInit(): Unit = proxy.onInit()
    override def onDiscard(): Unit = proxy.onDiscard()

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
}
