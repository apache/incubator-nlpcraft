package org.apache.nlpcraft.model.intent.solver

import org.apache.nlpcraft.model.NCToken

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

        for (tok ← tokens.asScala) {
            if (!tok.isFreeWord && !tok.isStopWord) {
                wordCnt += tok.wordLength
                totalSparsity += tok.sparsity

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

    override def toString: String =
        s"Variant [" +
            s"userToks=$userToks" +
            s", wordCnt=$wordCnt" +
            s", totalUserDirect=$totalUserDirect" +
            s", avgWordsPerTokPct=$avgWordsPerTokPct" +
            s", sparsity=$totalSparsity" +
            s", toks=$tokens" +
            "]"
}
