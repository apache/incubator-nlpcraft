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
import org.apache.nlpcraft.model.intent.utils.{NCDslIntent, NCDslTerm, NCDslTermContext}
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.impl.NCTokenLogger
import org.apache.nlpcraft.probe.mgrs.dialogflow.NCDialogFlowManager

import collection.convert.ImplicitConversions._
import scala.collection.mutable

/**
  * Intent solver that finds the best matching intent given user sentence.
  */
object NCIntentSolverEngine extends LazyLogging with NCOpenCensusTrace {
    /**
     * NOTE: not thread-safe.
     */
    private [impl] class Weight(ws: Int*) extends Ordered[Weight] {
        private var buf = mutable.ArrayBuffer[Int]()

        buf.appendAll(ws)

        /**
         * Adds given weight to this weight.
         *
         * @param that Weight to add.
         * @return
         */
        def +=(that: Weight): Weight = {
            val buf2 = mutable.ArrayBuffer[Int]()

            for (i ← 0 until Math.max(buf.size, that.buf.size))
                buf2.append(norm(i, buf) + norm(i, that.buf))

            buf = buf2

            this
        }

        /**
         * Appends new weight.
         *
         * @param w New weight to append.
         * @return
         */
        def append(w: Int): Weight = {
            buf.append(w)

            this
        }

        /**
         * Prepends new weight.
         *
         * @param w New weight to prepend.
         * @return
         */
        def prepend(w: Int): Weight = {
            buf.prepend(w)

            this
        }

        /**
         * Sets specific weight at a given index.
         *
         * @param idx
         * @param w
         */
        def setWeight(idx: Int, w: Int): Unit =
            buf(idx) = w

        /**
         * Gets element at given index or zero if index is out of bounds.
         *
         * @param i Index in collection.
         * @param c Collection.
         * @return
         */
        private def norm(i: Int, c: mutable.ArrayBuffer[Int]): Int = if (i < c.size) c(i) else 0

        /**
         *
         * @param that
         * @return
         */
        override def compare(that: Weight): Int = {
            var res = 0

            for (i ← 0 until Math.max(buf.size, that.buf.size) if res == 0)
                res = Integer.compare(norm(i, buf), norm(i, that.buf))

            res
        }

        /**
         *
         * @return
         */
        def toSeq: Seq[Int] = buf

        override def toString: String = s"Weight (${buf.mkString(", ")})"
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

        startScopedSpan("solve",
            "srvReqId" → req.getServerRequestId,
            "userId" → req.getUser.getId,
            "mdlId" → ctx.getModel.getId,
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
                            if (intent.terms.exists(_.conv))
                                Seq.empty[UsedToken] ++
                                    // We shouldn't mix tokens with same group from conversation
                                    // history and processed sentence.
                                    ctx.getConversation.getTokens.
                                        filter(t ⇒ {
                                            val convTokGroups = t.getGroups.sorted

                                            !senTokGroups.exists(convTokGroups.containsSlice)
                                        }).
                                        map(UsedToken(used = false, conv = true, _))
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

            val sorted = matches.sortWith((m1: MatchHolder, m2: MatchHolder) ⇒
                // 1. First with maximum weight.
                m1.intentMatch.weight.compare(m2.intentMatch.weight) match {
                    case x1 if x1 < 0 ⇒ false
                    case x1 if x1 > 0 ⇒ true
                    case x1 ⇒
                        require(x1 == 0)

                        val mw1 = m1.intentMatch.weight.toSeq
                        val mw2 = m2.intentMatch.weight.toSeq

                        val v1 = m1.variant
                        val v2 = m2.variant

                        val tbl = new NCAsciiTable()

                        tbl += (
                            s"${c("Intent ID")}",
                            m1.intentMatch.intent.id,
                            m2.intentMatch.intent.id
                        )
                        tbl += (
                            s"${c("Variant #")}",
                            m1.variantIdx + 1,
                            m2.variantIdx + 1
                        )
                        tbl += (
                            s"${c("Intent Match Weight")}",
                            Seq(
                                s"${y("XCT_VAL: ")}${mw1.head}",
                                s"${y("SEN_TOK: ")}${mw1(1)}",
                                s"${y("CNV_TOK: ")}${mw1(2)}",
                                s"${y("SPC_MIN: ")}${mw1(3)}",
                                s"${y("DLT_MAX: ")}${mw1(4)}",
                                s"${y("NRM_MAX: ")}${mw1(5)}"
                            ),
                            Seq(
                                s"${y("XCT_VAL: ")}${mw2.head}",
                                s"${y("SEN_TOK: ")}${mw2(1)}",
                                s"${y("CNV_TOK: ")}${mw2(2)}",
                                s"${y("SPC_MIN: ")}${mw2(3)}",
                                s"${y("DLT_MAX: ")}${mw2(4)}",
                                s"${y("NRM_MAX: ")}${mw2(5)}"
                            )
                        )
                        tbl += (
                            s"${c("Variant Weight")}",
                            Seq(
                                s"${y("USR_TOKS: ")}${v1.userToks}",
                                s"${y("WORD_CNT: ")}${v1.wordCnt}",
                                s"${y("USR_DRCT: ")}${v1.totalUserDirect}",
                                s"${y("AWPT_PCT: ")}${v1.avgWordsPerTokPct}",
                                s"${y("SPARSITY: ")}${v1.totalSparsity}"
                            ),
                            Seq(
                                s"${y("USR_TOKS: ")}${v2.userToks}",
                                s"${y("WORD_CNT: ")}${v2.wordCnt}",
                                s"${y("USR_DRCT: ")}${v2.totalUserDirect}",
                                s"${y("AWPT_PCT: ")}${v2.avgWordsPerTokPct}",
                                s"${y("SPARSITY: ")}${v2.totalSparsity}"
                            )
                        )

                        logger.warn(
                            s"\n" +
                                s"+------------------------------------------------------------------------+\n" +
                                s"| Two matching intents have the ${r("same weight")} for their matches.           |\n" +
                                s"| These intents will be sorted based on the weight of their variants.    |\n" +
                                s"| It is recommended that intents should NOT be so similar as to produce  |\n" +
                                s"| matches with identical weights. If possible, modify intent definitions |\n" +
                                s"| to avoid intersecting matches...                                       |\n" +
                                s"+------------------------------------------------------------------------+\n" +
                                tbl.toString
                        )

                        // 2. First with maximum variant.
                        m1.variant.compareTo(m2.variant) match {
                            case x2 if x2 < 0 ⇒ false
                            case x2 if x2 > 0 ⇒ true
                            case x2 ⇒
                                require(x2 == 0)

                                def calcHash(m: MatchHolder): Int = {
                                    val variantPart =
                                        m.variant.
                                        tokens.
                                        map(t ⇒ s"${t.getId}${t.getGroups}${t.getValue}${t.normText}").
                                        mkString("")

                                    val intentPart = m.intentMatch.intent.toString

                                    (variantPart, intentPart).##
                                }

                                // Order doesn't make sense here.
                                // It is just to provide deterministic result for the matches with the same weights.
                                calcHash(m1) > calcHash(m2)
                        }
                }
            )

            if (sorted.nonEmpty) {
                val tbl = NCAsciiTable("Variant", "Intent", "Term Tokens", "Intent Match Weight")

                sorted.foreach(m ⇒ {
                    val im = m.intentMatch
                    val w = im.weight.toSeq
                    val ws = Seq(
                        s"${y("XCT_VAL: ")}${w.head}",
                        s"${y("SEN_TOK: ")}${w(1)}",
                        s"${y("CNV_TOK: ")}${w(2)}",
                        s"${y("SPC_MIN: ")}${w(3)}",
                        s"${y("DLT_MAX: ")}${w(4)}",
                        s"${y("NRM_MAX: ")}${w(5)}"
                    )

                    if (m == sorted.head)
                        tbl += (
                            Seq(
                                s"#${m.variantIdx + 1}",
                                g(bo("best match"))
                            ),
                            Seq(
                                im.intent.id,
                                g(bo("best match"))
                            ),
                            mkPickTokens(im),
                            ws
                        )
                    else
                        tbl += (
                            s"#${m.variantIdx + 1}",
                            im.intent.id,
                            mkPickTokens(im),
                            ws
                        )

                    if (logHldr != null)
                        logHldr.addIntent(
                            im.intent.id,
                            im.exactMatch,
                            im.weight.toSeq,
                            im.tokenGroups.map(g ⇒
                                (if (g.termId == null) "" else g.termId) →
                                g.usedTokens.map(t ⇒ NCLogGroupToken(t.token, t.conv, t.used))
                            ).toMap
                        )
                })

                tbl.info(logger, Some(s"Found ${sorted.size} matching intents (sorted ${g(bo("best"))} to worst):"))
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
      * @param intent
      * @param senToks
      * @param convToks
      * @return
      */
    //noinspection DuplicatedCode
    private def solveIntent(
        ctx: NCContext,
        intent: NCDslIntent,
        senToks: Seq[UsedToken],
        convToks: Seq[UsedToken],
        varIdx: Int
    ): Option[IntentMatch] = {
        val intentId = intent.id
        val flow = NCDialogFlowManager.getDialogFlow(ctx.getRequest.getUser.getId, ctx.getModel.getId)
        val varStr = s"(variant #${varIdx + 1})"
        val flowRegex = intent.flowRegex
        
        var flowMatched = true

        // Check dialog flow regex first, if any.
        if (intent.flowRegex.isDefined) {
            val str = flow.map(_.getIntentId).mkString(" ")
            
            def x(s: String): Unit = {
                logger.info(s"Intent '$intentId' ${bo(s)} regex dialog flow $varStr:")
                logger.info(s"  |-- ${c("Intent IDs  :")} $str")
                logger.info(s"  +-- ${c("Match regex :")} ${flowRegex.get.toString}")
            }

            if (!flowRegex.get.matcher(str).find(0)) {
                x("did not match")
                
                flowMatched = false
            }
            else
                x("matched")
        }
        else if (intent.flowMtdName.isDefined) {
            // TODO.
        }
        
        if (flowMatched) {
            val intentW = new Weight()
            val intentGrps = mutable.ListBuffer.empty[TermTokensGroup]
            var abort = false
            val ordered = intent.ordered
            var lastTermMatch: TermMatch = null
            
            val termCtx = NCDslTermContext( // TODO
                intentMeta = null,
                reqMeta = null,
                usrMeta = null,
                convMeta = null,
                compMeta = null,
                fragMeta = null,
                req = ctx.getRequest
            )

            // Check terms.
            for (term ← intent.terms if !abort) {
                solveTerm(
                    term,
                    termCtx,
                    senToks,
                    if (term.conv) convToks else Seq.empty
                ) match {
                    case Some(termMatch) ⇒
                        if (ordered && lastTermMatch != null && lastTermMatch.maxIndex > termMatch.maxIndex)
                            abort = true
                        else {
                            // Term is found.
                            // Add its weight and grab its tokens.
                            intentW += termMatch.weight
                            intentGrps += TermTokensGroup(termMatch.termId, termMatch.usedTokens)
                            lastTermMatch = termMatch

                            val tbl = NCAsciiTable()

                            val w = termMatch.weight.toSeq

                            tbl += (s"${B}Intent ID$RST", s"$BO${intent.id}$RST")
                            tbl += (s"${B}Matched Term$RST", term.toString)
                            tbl += (s"${B}Matched Tokens$RST", termMatch.usedTokens.map(t ⇒ {
                                val txt = t.token.getOriginalText
                                val idx = t.token.getIndex

                                s"$txt${c("[" + idx + "]")}"
                            }).mkString(" "))
                            tbl += (
                                s"${B}Term Match Weight$RST",
                                Seq(
                                    s"${y("SEN_TOK: ")}${w.head}",
                                    s"${y("CNV_TOK: ")}${w(1)}",
                                    s"${y("SPC_MIN: ")}${w(2)}",
                                    s"${y("DLT_MAX: ")}${w(3)}",
                                    s"${y("NRM_MAX: ")}${w(4)}"
                                )
                            )

                            tbl.info(logger, Some("Term match found:"))
                        }

                    case None ⇒
                        // Term is missing. Stop further processing for this intent. This intent cannot be matched.
                        logger.info(s"Intent '$intentId' ${r("did not")} match because of unmatched term '$Y$term$RST' $varStr.")

                        abort = true
                }
            }

            if (abort)
                None
            else {
                val usedSenToks = senToks.filter(_.used)
                val unusedSenToks = senToks.filter(!_.used)
                val usedConvToks = convToks.filter(_.used)

                var res: Option[IntentMatch] = None

                if (usedSenToks.isEmpty && usedConvToks.isEmpty)
                    logger.info(s"Intent '$intentId' ${r("did not")} match because no tokens were matched $varStr.")
                else if (usedSenToks.isEmpty && usedConvToks.nonEmpty)
                    logger.info(s"Intent '$intentId' ${r("did not")} match because all its matched tokens came from STM $varStr.")
                else if (unusedSenToks.exists(_.token.isUserDefined))
                    NCTokenLogger.prepareTable(unusedSenToks.filter(_.token.isUserDefined).map(_.token)).
                        info(
                            logger,
                            Some(
                                s"Intent '$intentId' ${r("did not")} match because of remaining unused user tokens $varStr." +
                                s"\nUnused user tokens for intent '$intentId' $varStr:"
                            )
                        )
                else {
                    // Number of remaining (unused) non-free words in the sentence is a measure of exactness of the match.
                    // The match is exact when all non-free words are used in that match.
                    // Negate to make sure the bigger (smaller negative number) is better.
                    val nonFreeWordNum = -senToks.count(t ⇒ !t.used && !t.token.isFreeWord)

                    intentW.prepend(nonFreeWordNum)

                    res = Some(IntentMatch(
                        tokenGroups = intentGrps.toList,
                        weight = intentW,
                        intent = intent,
                        exactMatch = nonFreeWordNum == 0
                    ))
                }

                res
            }
        }
        else
            None
    }
    
    /**
      * Solves term.
      *
      * @param term
      * @param ctx
      * @param convToks
      * @param senToks
      * @return
      */
    @throws[NCE]
    private def solveTerm(
        term: NCDslTerm,
        ctx: NCDslTermContext,
        senToks: Seq[UsedToken],
        convToks: Seq[UsedToken]
    ): Option[TermMatch] =
        solvePredicate(term.pred, ctx, term.min, term.max, senToks, convToks) match {
            case Some((usedToks, predWeight)) ⇒ Some(
                TermMatch(
                    term.id,
                    usedToks,
                    if (usedToks.nonEmpty) {
                        // If term match is non-empty we add the following weights:
                        //   - min
                        //   - delta between specified max and normalized max (how close the actual quantity was to the specified one).
                        //   - normalized max
                        predWeight
                            .append(term.min)
                            .append(-(term.max - usedToks.size))
                            // Normalize max quantifier in case of unbound max.
                            .append(if (term.max == Integer.MAX_VALUE) usedToks.size else term.max)
                    } else {
                        // Term is optional (found but empty).
                        require(term.min == 0)

                        predWeight
                            .append(0)
                            .append(-term.max)
                            // Normalize max quantifier in case of unbound max.
                            .append(if (term.max == Integer.MAX_VALUE) 0 else term.max)
                    }
                )
            )

            // Term not found at all.
            case None ⇒ None
        }
    
    /**
      * Solves term's predicate.
      *
      * @param pred
      * @param ctx
      * @param min
      * @param max
      * @param senToks
      * @param convToks
      * @return
      */
    @throws[NCE]
    private def solvePredicate(
        pred: (NCToken, NCDslTermContext) ⇒ (Boolean/*Predicate.*/, Boolean/*Whether or not token was used.*/),
        ctx: NCDslTermContext,
        min: Int,
        max: Int,
        senToks: Seq[UsedToken],
        convToks: Seq[UsedToken]
    ): Option[(List[UsedToken], Weight)] = {
        // Algorithm is "hungry", i.e. it will fetch all tokens satisfying item's predicate
        // in entire sentence even if these tokens are separated by other already used tokens
        // and conversation will be used only to get to the 'max' number of the item.
    
        var usedToks = List.empty[UsedToken]
        
        var matches = 0

        // Collect to the 'max' from sentence & conversation, if possible.
        for (col ← Seq(senToks, convToks); tok ← col.filter(!_.used) if usedToks.lengthCompare(max) < 0) {
            val (res, used) = pred.apply(tok.token, ctx)
            
            if (res) {
                matches += 1
    
                if (used)
                    usedToks :+= tok
            }
        }
    
        // We couldn't collect even 'min' matches.
        if (matches < min)
            None
        // Term is optional (min == 0) and no matches found (valid result).
        else if (matches == 0) {
            require(min == 0)
            require(usedToks.isEmpty)
            
            Some(usedToks → new Weight(0, 0))
        }
        // We've found some matches (and min > 0).
        else {
            require(matches > 0 && matches > min)
            
            val convSrvReqIds = convToks.map(_.token.getServerRequestId).distinct

            // Number of tokens from the current sentence.
            val senTokNum = usedToks.count(t ⇒ !convSrvReqIds.contains(t.token.getServerRequestId))

            // Sum of conversation depths for each token from the conversation.
            // Negated to make sure that bigger (smaller negative number) is better.
            val convDepthsSum = -usedToks.filter(t ⇒ convSrvReqIds.contains(t.token.getServerRequestId)).zipWithIndex.map(_._2 + 1).sum

            // Mark found tokens as used.
            usedToks.foreach(_.used = true)

            Some(usedToks → new Weight(
                senTokNum,
                convDepthsSum
            ))
        }
    }
}
