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

package org.apache.nlpcraft.model.intent.impl

import java.util.function.Function

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.debug.{NCLogGroupToken, NCLogHolder}
import org.apache.nlpcraft.common.opencensus.NCOpenCensusTrace
import org.apache.nlpcraft.model.intent.utils.{NCDslFlowItem, NCDslIntent, NCDslTerm}
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.impl.NCTokenLogger
import org.apache.nlpcraft.probe.mgrs.dialogflow.NCDialogFlowManager

import collection.convert.ImplicitConversions._
import scala.collection.mutable

/**
  * Intent solver that finds the best matching intent given user sentence.
  */
object NCIntentSolverEngine extends LazyLogging with NCOpenCensusTrace {
    private class Weight extends Ordered[Weight] {
        private val weights: Array[Int] = new Array(3)

        /**
          *
          * @param w0
          * @param w1
          * @param w2
          */
        def this(w0: Int, w1: Int, w2: Int) = {
            this()

            weights(0) = w0
            weights(1) = w1
            weights(2) = w2
        }

        /**
          * Sets specific weight at a given index.
          *
          * @param idx
          * @param w
          */
        def setWeight(idx: Int, w: Int): Unit =
            weights(idx) = w
    
        /**
          *
          * @param that
          * @return
          */
        def ++=(that: Weight): Weight = {
            for (i ← 0 until 3)
                this.setWeight(i, this.weights(i) + that.weights(i))
            
            this
        }
    
        /**
          *
          * @param that
          * @return
          */
        override def compare(that: Weight): Int = {
            var res = 0
    
            for ((i1, i2) ← this.weights.zip(that.weights) if res == 0)
                res = Integer.compare(i1, i2)
    
            res
        }

        def get: Seq[Int] = weights.toSeq
    
        override def toString: String = s"Weight (${weights.mkString(", ")})"
    }
    
    /**
      * 
      * @param used
      * @param token
      */
    private case class UsedToken(
        var used: Boolean,
        var conv: Boolean,
        token: NCToken
    )
    
    /**
      * @param termId
      * @param usedTokens
      * @param weight
      */
    private case class TermMatch(
        termId: String,
        usedTokens: List[UsedToken],
        weight: Weight
    ) {
        lazy val minIndex: Int = usedTokens.minBy(_.token.index).token.index
        lazy val maxIndex: Int = usedTokens.maxBy(_.token.index).token.index
    }

    /**
      *
      * @param termId
      * @param usedTokens
      */
    private case class TermTokensGroup(
        termId: String,
        usedTokens: List[UsedToken]
    )

    /**
      *
      * @param tokenGroups
      * @param weight
      * @param intent
      * @param exactMatch
      */
    private case class IntentMatch(
        tokenGroups: List[TermTokensGroup],
        weight: Weight,
        intent: NCDslIntent,
        exactMatch: Boolean
    )

    /**
      * Main entry point for intent engine.
      *
      * @param ctx Query context.
      * @param intents Set of intents to match for.
      * @param logHldr Log holder.
      * @return
      */
    @throws[NCE]
    def solve(
        ctx: NCContext,
        intents: List[(NCDslIntent/*Intent*/, NCIntentMatch ⇒ NCResult)/*Callback*/],
        logHldr: NCLogHolder
    ): List[NCIntentSolverResult] = {
        case class MatchHolder(
            intentMatch: IntentMatch, // Match.
            callback: Function[NCIntentMatch, NCResult], // Callback function.
            variant: NCIntentSolverVariant, // Variant used for the match.
            variantIdx: Int // Variant index.
        )
        val req = ctx.getRequest
        val conv = ctx.getConversation.getTokens

        startScopedSpan("solve",
            "srvReqId" → req.getServerRequestId,
            "userId" → req.getUser.getId,
            "modelId" → ctx.getModel.getId,
            "normText" → req.getNormalizedText) { _ ⇒
            val matches = mutable.ArrayBuffer.empty[MatchHolder]

            // Find all matches across all intents and sentence variants.
            for ((vrn, vrnIdx) ← ctx.getVariants.zipWithIndex) {
                val availToks = vrn.filter(t ⇒ !t.isStopWord)

                matches.appendAll(
                    intents.flatMap(pair ⇒ {
                        val intent = pair._1
                        val callback = pair._2

                        // Isolated sentence tokens.
                        val senToks = Seq.empty[UsedToken] ++ availToks.map(UsedToken(false, false, _))
                        val senTokGroups = availToks.map(t ⇒ if (t.getGroups != null) t.getGroups.sorted else Seq.empty)

                        // Isolated conversation tokens.
                        val convToks =
                            if (intent.conv)
                                Seq.empty[UsedToken] ++
                                    // We shouldn't mix tokens with same group from conversation
                                    // history and processed sentence.
                                    conv.
                                        filter(t ⇒ {
                                            val convTokGroups = t.getGroups.sorted

                                            !senTokGroups.exists(convTokGroups.containsSlice)
                                        }).
                                        map(UsedToken(false, true, _))
                            else
                                Seq.empty[UsedToken]

                        // Solve intent in isolation.
                        solveIntent(ctx, intent, senToks, convToks, vrnIdx) match {
                            case Some(intentMatch) ⇒ Some(MatchHolder(intentMatch, callback, NCIntentSolverVariant(vrn), vrnIdx))
                            case None ⇒ None
                        }
                    })
                )
            }

            val sorted =
                matches.sortWith((m1: MatchHolder, m2: MatchHolder) ⇒
                    // 1. First with maximum weight.
                    m1.intentMatch.weight.compare(m2.intentMatch.weight) match {
                        case x1 if x1 < 0 ⇒ false
                        case x1 if x1 > 0 ⇒ true
                        case x1 ⇒
                            require(x1 == 0)

                            // 2. First with maximum variant.
                            m1.variant.compareTo(m2.variant) match {
                                case x2 if x2 < 0 ⇒ false
                                case x2 if x2 > 0 ⇒ true
                                case x2 ⇒
                                    require(x2 == 0)

                                    def calcHash(m: MatchHolder): Int =
                                        m.variant.tokens.map(t ⇒
                                            s"${t.getId}${t.getGroups}${t.getValue}${t.normText}"
                                        ).mkString("").hashCode

                                    // Order doesn't make sense here.
                                    // It is just to provide deterministic result for the matches with the same weight.
                                    calcHash(m1) > calcHash(m2)
                            }
                    }
                )

            if (sorted.nonEmpty) {
                val tbl = NCAsciiTable("Variant", "Intent", "Tokens")
    
                sorted.foreach(m ⇒ {
                    val im = m.intentMatch
    
                    tbl += (
                        s"#${m.variantIdx + 1}",
                        im.intent.id,
                        mkPickTokens(im)
                    )

                    if (logHldr != null)
                        logHldr.addIntent(
                            im.intent.id,
                            im.exactMatch,
                            im.weight.get,
                            im.tokenGroups.map(g ⇒
                                (if (g.termId == null) "" else g.termId) →
                                g.usedTokens.map(t ⇒ NCLogGroupToken(t.token, t.conv, t.used))
                            ).toMap
                        )
                })
                tbl.info(logger, Some(s"Found matching intents (sorted from best to worst match):"))
            }
            else
                logger.info("No matching intent found.")

            sorted.map(m ⇒
                NCIntentSolverResult(
                    m.intentMatch.intent.id,
                    m.callback,
                    m.intentMatch.tokenGroups.map(grp ⇒ NCIntentTokensGroup(grp.termId, grp.usedTokens.map(_.token))),
                    m.intentMatch.exactMatch,
                    m.variant,
                    m.variantIdx
                )
            ).toList
        }
    }

    /**
      *
      * @param im
      * @return
      */
    private def mkPickTokens(im: IntentMatch): List[String] = {
        val buf = mutable.ListBuffer.empty[String]
        
        buf += im.intent.toString
        
        var grpIdx = 0
        
        for (grp ← im.tokenGroups) {
            val termId = if (grp.termId == null) s"#$grpIdx" else s"'${grp.termId}'"
            buf += s"  Term $termId"
            
            grpIdx += 1
            
            if (grp.usedTokens.nonEmpty) {
                var tokIdx = 0
                
                for (tok ← grp.usedTokens) {
                    val conv = if (tok.conv) "(conv) " else ""
                    
                    buf += s"    #$tokIdx: $conv${tok.token}"
                    
                    tokIdx += 1
                }
            }
            else
                buf += "    <empty>"
        }
        
        buf.toList
    }
    
    /**
     * 
     * @param flow
     * @param hist
     * @return
     */
    private[impl] def matchFlow(flow: Array[NCDslFlowItem], hist: Seq[String]): Boolean = {
        var flowIdx = 0
        var histIdx = 0
        var abort = false
        
        while (flowIdx < flow.length && !abort) {
            val item = flow(flowIdx)
    
            val intents = item.intents
            val min = item.min
            val max = item.max
            
            var i = 0
            
            // Check min first.
            while (i < min && histIdx < hist.length && !abort) {
                abort = !intents.contains(hist(histIdx))
                
                histIdx += 1
                i += 1
            }
            
            if (!abort && i < min)
                abort = true // Need at least min.

            if (!abort) {
                var ok = true
                
                // Grab up until max, if available.
                while (i < max && histIdx < hist.length && ok) {
                    ok = intents.contains(hist(histIdx))
        
                    if (ok)
                        histIdx += 1
                    
                    i += 1
                }
            }
            
            flowIdx += 1
        }
        
        !abort
    }
    
    /**
      *
      * @param intent
      * @param senToks
      * @param convToks
      * @return
      */
    private def solveIntent(
        ctx: NCContext,
        intent: NCDslIntent,
        senToks: Seq[UsedToken],
        convToks: Seq[UsedToken],
        varIdx: Int
    ): Option[IntentMatch] = {
        val intentId = intent.id
    
        val hist = NCDialogFlowManager.getDialogFlow(ctx.getRequest.getUser.getId, ctx.getModel.getId)
        
        val varStr = s"(variant #${varIdx + 1})"
    
        // Check dialog flow first.
        if (!intent.flow.isEmpty && !matchFlow(intent.flow, hist)) {
            logger.info(s"Intent '$intentId' didn't match because of dialog flow $varStr.")
            None
        }
        else {
            val intentW = new Weight()
            val intentGrps = mutable.ListBuffer.empty[TermTokensGroup]
            var abort = false
            val ordered = intent.ordered
            var lastTermMatch: TermMatch = null
            
            // Check terms.
            for (term ← intent.terms if !abort) {
                solveTerm(
                    term,
                    senToks,
                    convToks
                ) match {
                    case Some(termMatch) ⇒
                        if (ordered && lastTermMatch != null && lastTermMatch.maxIndex > termMatch.maxIndex)
                            abort = true
                        else {
                            // Term is found.
                            // Add its weight and grab its tokens.
                            intentW ++= termMatch.weight
                            intentGrps += TermTokensGroup(termMatch.termId, termMatch.usedTokens)
                            lastTermMatch = termMatch
                        }

                    case None ⇒
                        // Term is missing. Stop further processing for this intent.
                        // This intent cannot be matched.
                        logger.trace(s"Term '$term' is missing for intent '$intentId' (stopping further processing).")
                        abort = true
                }
            }
            
            if (abort) {
                logger.info(s"Intent '$intentId' didn't match because of missing term $varStr.")
                None
            }
            else if (senToks.exists(tok ⇒ !tok.used && tok.token.isUserDefined)) {
                logger.info(s"Intent '$intentId' didn't match because of remaining unused user tokens $varStr.")
    
                NCTokenLogger.prepareTable(senToks.filter(tok ⇒ !tok.used && tok.token.isUserDefined).map(_.token)).
                    info(
                        logger,
                        Some(s"Unused user tokens for intent '$intentId' $varStr:")
                    )
                
                None
            }
            else if (!senToks.exists(tok ⇒ tok.used && !tok.conv)) {
                logger.info(s"Intent '$intentId' didn't match because all tokens came from STM $varStr.")
                None
            }
            else {
                val exactMatch = !senToks.exists(tok ⇒ !tok.used && !tok.token.isFreeWord)
            
                intentW.setWeight(0, if (exactMatch) 1 else 0)
                
                Some(IntentMatch(
                    tokenGroups = intentGrps.toList,
                    weight = intentW,
                    intent = intent,
                    exactMatch = exactMatch
                ))
            }
        }
    }
    
    /**
      * 
      * @param term
      * @param convToks
      * @param senToks
      * @return
      */
    @throws[NCE]
    private def solveTerm(
        term: NCDslTerm,
        senToks: Seq[UsedToken],
        convToks: Seq[UsedToken]
    ): Option[TermMatch] = {
        var termToks = List.empty[UsedToken]
        var termWeight = new Weight()

        solvePredicate(term.getPredicate, term.getMin, term.getMax, senToks, convToks) match {
            case Some(t) ⇒
                termToks = termToks ::: t._1
                termWeight ++= t._2
    
                Some(TermMatch(term.getId, termToks, termWeight))
            case None ⇒
                None
        }
    }
    
    /**
      *
      * @param pred
      * @param min
      * @param max
      * @param senToks
      * @param convToks
      * @return
      */
    @throws[NCE]
    private def solvePredicate(
        pred: Function[NCToken, java.lang.Boolean],
        min: Int,
        max: Int,
        senToks: Seq[UsedToken],
        convToks: Seq[UsedToken]
    ): Option[(List[UsedToken], Weight)] = {
        // Algorithm is "hungry", i.e. it will fetch all tokens satisfying item's predicate
        // in entire sentence even if these tokens are separated by other already used tokens
        // and conversation will be used only to get to the 'max' number of the item.
    
        var combToks = List.empty[UsedToken]
        var predW = 0

        /**
          *
          * @param from Collection to collect tokens from.
          * @param maxLen Maximum number of tokens to collect.
          */
        def collect(from: Iterable[UsedToken], maxLen: Int): Unit =
            for (tok ← from.filter(!_.used) if combToks.lengthCompare(maxLen) < 0) {
                if (pred.apply(tok.token)) {
                    combToks :+= tok

                    predW += 1
                }
            }

        // Collect to the 'max', if possible.
        collect(senToks, max)
        
        collect(convToks, max)

        if (combToks.lengthCompare(min) < 0) // We couldn't collect even 'min' tokens.
            None
        else if (combToks.isEmpty) { // Item is optional and no tokens collected (valid result).
            require(min == 0)
            
            Some(combToks → new Weight())
        }
        else { // We've collected some tokens.
            // Youngest first.
            val convSrvReqIds = convToks.map(_.token.getServerRequestId).distinct

            // Specificity weight ('1' if conversation wasn't used, -'index of conversation depth' if wasn't).
            // (It is better to be not from conversation or be youngest tokens from conversation)
            val convW = -combToks.map(t ⇒ convSrvReqIds.indexOf(t.token.getServerRequestId)).sum

            combToks.foreach(_.used = true) // Mark tokens as used.

            Some(combToks → new Weight(0/* set later */, convW, predW))
        }
    }
}
