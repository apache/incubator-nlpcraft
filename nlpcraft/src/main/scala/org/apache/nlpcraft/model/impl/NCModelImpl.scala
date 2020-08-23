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

import org.apache.nlpcraft.model.intent.impl.NCIntentSolver
import org.apache.nlpcraft.model.{NCContext, NCIntentMatch, NCModel, NCRejection, NCResult, NCVariant}

/**
 * Internal model implementation combining model and intent solver.
 *
 * @param proxy Mandatory model proxy.
 * @param solver Optional solver.
 */
class NCModelImpl(val proxy: NCModel, val solver: NCIntentSolver) extends NCModel {
    require(proxy != null)
    
    override def getId: String = proxy.getId
    override def getName: String = proxy.getName
    override def getVersion: String = proxy.getVersion
    override def getDescription = proxy.getDescription
    override def getMaxUnknownWords = proxy.getMaxUnknownWords
    override def getMaxFreeWords = proxy.getMaxFreeWords
    override def getMaxSuspiciousWords = proxy.getMaxSuspiciousWords
    override def getMinWords = proxy.getMinWords
    override def getMaxWords = proxy.getMaxWords
    override def getMinTokens = proxy.getMinTokens
    override def getMaxTokens = proxy.getMaxTokens
    override def getMinNonStopwords = proxy.getMinNonStopwords
    override def isNonEnglishAllowed = proxy.isNonEnglishAllowed
    override def isNotLatinCharsetAllowed = proxy.isNotLatinCharsetAllowed
    override def isSwearWordsAllowed = proxy.isSwearWordsAllowed
    override def isNoNounsAllowed = proxy.isNoNounsAllowed
    override def isPermutateSynonyms = proxy.isPermutateSynonyms
    override def isDupSynonymsAllowed = proxy.isDupSynonymsAllowed
    override def getMaxTotalSynonyms = proxy.getMaxTotalSynonyms
    override def isNoUserTokensAllowed = proxy.isNoUserTokensAllowed
    override def getJiggleFactor = proxy.getJiggleFactor
    override def getMetadata = proxy.getMetadata
    override def getAdditionalStopWords = proxy.getAdditionalStopWords
    override def getExcludedStopWords = proxy.getExcludedStopWords
    override def getSuspiciousWords = proxy.getSuspiciousWords
    override def getMacros = proxy.getMacros
    override def getParsers = proxy.getParsers
    override def getElements = proxy.getElements
    override def getEnabledBuiltInTokens = proxy.getEnabledBuiltInTokens
    override def onParsedVariant(`var`: NCVariant) = proxy.onParsedVariant(`var`)
    override def onContext(ctx: NCContext) = proxy.onContext(ctx)
    override def onMatchedIntent(ctx: NCIntentMatch) = proxy.onMatchedIntent(ctx)
    override def onResult(ctx: NCIntentMatch, res: NCResult) = proxy.onResult(ctx, res)
    override def onRejection(ctx: NCIntentMatch, e: NCRejection) = proxy.onRejection(ctx, e)
    override def onError(ctx: NCContext, e: Throwable) = proxy.onError(ctx, e)
    override def onInit(): Unit = proxy.onInit()
    override def onDiscard(): Unit = proxy.onDiscard()
}
