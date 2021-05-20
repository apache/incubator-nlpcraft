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

package org.apache.nlpcraft.model.intent.solver

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.model.impl.NCTokenPimp._

import scala.jdk.CollectionConverters._
import java.util

/**
 * Sentence variant & its weight.
 */
case class NCIntentSolverVariant(tokens: util.List[NCToken]) extends Ordered[NCIntentSolverVariant] {
    val (userToks, wordCnt, avgWordsPerTokPct, totalSparsity, totalUserDirect) = calcWeight()

    /**
     * Calculates weight components.
     */
    private def calcWeight(): (Int, Int, Int, Int, Int) = {
        var userToks = 0 // More is better.
        var wordCnt = 0
        var avgWordsPerTok = 0
        var totalSparsity = 0
        var totalUserDirect = 0

        var tokCnt = 0

        for (tok <- tokens.asScala) {
            if (!tok.isFreeWord && !tok.isStopWord) {
                wordCnt += tok.getWordLength
                totalSparsity += tok.getSparsity

                if (tok.isUserDefined) {
                    userToks += 1

                    if (tok.isDirect)
                        totalUserDirect += 1
                }

                tokCnt += 1
            }
        }

        totalSparsity = -totalSparsity // Less is better.

        avgWordsPerTok = if (wordCnt > 0) Math.round((tokCnt.toFloat / wordCnt) * 100) else 0

        (userToks, wordCnt, avgWordsPerTok, totalSparsity, totalUserDirect)
    }

    override def compare(v: NCIntentSolverVariant): Int = {
        // Order is important.
        if (userToks > v.userToks) 1
        else if (userToks < v.userToks) -1
        else if (wordCnt > v.wordCnt) 1
        else if (wordCnt < v.wordCnt) -1
        else if (totalUserDirect > v.totalUserDirect) 1
        else if (totalUserDirect < v.totalUserDirect) -1
        else if (avgWordsPerTokPct > v.avgWordsPerTokPct) 1
        else if (avgWordsPerTokPct < v.avgWordsPerTokPct) -1
        else if (totalSparsity > v.totalSparsity) 1
        else if (totalSparsity < v.totalSparsity) -1
        else 0
    }

    def toAnsiString: String = s"${y(bo("["))}$userToks, $wordCnt, $totalUserDirect, $avgWordsPerTokPct, $totalSparsity${y(bo("]"))}"
}
