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

package org.apache.nlpcraft.internal.intent.matcher

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.ascii.NCAsciiTable
import org.apache.nlpcraft.internal.conversation.NCConversationManager
import org.apache.nlpcraft.internal.dialogflow.NCDialogFlowManager
import org.apache.nlpcraft.internal.impl.NCCallbackInput
import org.apache.nlpcraft.internal.intent.*

import java.util.function.Function
import scala.annotation.targetName
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

/**
  *
  */
enum NCIntentSolveType:
    case REGULAR, SEARCH, SEARCH_NO_HISTORY

/**
  *
  */
object NCIntentSolverManager:
    /**
      * Sentence variant & its weight.
      *
      * @param entities
      */
    private case class IntentSolverVariant(entities: List[NCEntity]) extends Ordered[IntentSolverVariant]:
        private lazy val weights = calcWeight()

        private def calcSparsity(toks: Seq[NCToken]): Int =
            val idxs = toks.map(_.getIndex)
            idxs.zipWithIndex.tail.map { (v, i) => Math.abs(v - idxs(i - 1)) }.sum - idxs.length + 1

        private def calcWeight(): Seq[Int] =
            val toks: Seq[Seq[NCToken]] = entities.map(_.getTokens.toSeq)

            val toksCnt = toks.map(_.size).sum
            val avgToksPerEntity = if toksCnt > 0 then Math.round((entities.size.toFloat / toksCnt) * 100) else 0
            val totalSparsity = -toks.map(calcSparsity).sum  // Less is better.

            // Order is important.
            Seq(toksCnt, avgToksPerEntity, totalSparsity)

        override def compare(other: IntentSolverVariant): Int =
            def compareWeight(weight1: Int, weight2: Int): Option[Int] =
                val res = Integer.compare(weight1, weight2)
                Option.when(res != 0)(res)

            weights.zip(other.weights).flatMap { (w1, w2) => compareWeight(w1, w2) }.to(LazyList).headOption.getOrElse(0)

        override def toString: String = s"${weights.mkString("[", ", ", "]")}"

    /**
      *
      * @param termId
      * @param entities
      */
    private case class IntentTermEntities(termId: Option[String], entities: List[NCEntity])

    /**
      *
      * @param getIntentId
      * @param getCallbackArguments
      * @param getCallback
      */
    private case class CallbackDataImpl(
        getIntentId: String,
        getCallbackArguments: List[List[NCEntity]],
        getCallback: List[List[NCEntity]] => NCResult
    ) extends NCMatchedCallback

    /**
      *
      * @param intentId
      * @param fn
      * @param groups
      * @param variant
      * @param variantIdx
      */
    private case class IntentSolverResult(intentId: String, fn: NCCallbackInput => NCResult, groups: List[IntentTermEntities], variant: IntentSolverVariant, variantIdx: Int)

    /**
      * NOTE: not thread-safe.
      */
    private class Weight(ws: Int*) extends Ordered[Weight]:
        private val buf = mutable.ArrayBuffer[Int]() ++ ws

        /**
          * Adds given weight to this weight.
          *
          * @param that Weight to add.
          * @return
          */
        @targetName("plusEqual")
        def +=(that: Weight): Weight =
            val tmp = mutable.ArrayBuffer[Int]()
            for (i <- 0 until Math.max(buf.size, that.buf.size))
                tmp.append(norm(i, buf) + norm(i, that.buf))
            buf.clear()
            buf ++= tmp
            this

        /**
          * Appends new weight.
          *
          * @param w New weight to append.
          * @return
          */
        def append(w: Int): Weight =
            buf.append(w)
            this

        /**
          * Prepends new weight.
          *
          * @param w New weight to prepend.
          * @return
          */
        def prepend(w: Int): Weight =
            buf.prepend(w)
            this

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
        private def norm(i: Int, c: mutable.ArrayBuffer[Int]): Int = if i < c.size then c(i) else 0

        /**
          *
          * @param that
          * @return
          */
        override def compare(that: Weight): Int =
            def compareWeight(idx: Int): Option[Int] =
                val res = Integer.compare(norm(idx, buf), norm(idx, that.buf))
                Option.when(res != 0)(res)

            (0 until Math.max(buf.size, that.buf.size)).flatMap(compareWeight).to(LazyList).headOption.getOrElse(0)

        def toSeq: Seq[Int] = buf.toSeq

        override def toString: String = buf.mkString("[", ", ", "]")

    /**
      *
      * @param used
      * @param entity
      */
    private case class IntentEntity(var used: Boolean, var conv: Boolean, entity: NCEntity)

    type ResultData = Either[NCResult, NCMatchedCallback]

    /**
      *
      * @param result
      * @param intentMatch
      */
    private case class IterationResult(result: ResultData, intentMatch: NCIntentMatch)

    /**
      * @param termId
      * @param usedEntities
      * @param weight
      */
    private case class TermMatch(termId: Option[String], usedEntities: List[IntentEntity], weight: Weight):
        private lazy val maxIndex: Int = usedEntities.map(_.entity.getTokens.map(_.getIndex).max).max

        def after(tm: TermMatch): Boolean = maxIndex > tm.maxIndex

    /**
      *
      * @param entities
      */
    private case class PredicateMatch(entities: List[IntentEntity], weight: Weight)

    /**
      *
      * @param term
      * @param usedEntities
      */
    private case class TermEntitiesGroup(
        term: NCIDLTerm,
        usedEntities: List[IntentEntity]
    )

    /**
      *
      * @param entityGroups
      * @param weight
      * @param intent
      */
    private case class IntentMatchHolder(
        entityGroups: List[TermEntitiesGroup],
        weight: Weight,
        intent: NCIDLIntent
    )

    /**
      *
      * @param intentMatch
      * @param callback
      * @param variant
      * @param variantIdx
      */
    private case class MatchHolder(
        intentMatch: IntentMatchHolder, // Match.
        callback: NCCallbackInput => NCResult, // Callback function.
        variant: IntentSolverVariant, // Variant used for the match.
        variantIdx: Int // Variant index.
    )

    /**
      *
      * @param userId
      * @param mldId
      */
    private case class UserModelKey(userId: String, mldId: String)

import org.apache.nlpcraft.internal.intent.matcher.NCIntentSolverManager.*

/**
 * Intent solver that finds the best matching intent given user sentence.
 */
class NCIntentSolverManager(
    dialog: NCDialogFlowManager,
    conv: NCConversationManager,
    intents: Map[NCIDLIntent, NCCallbackInput => NCResult]
) extends LazyLogging:
    private final val reqIds = mutable.HashMap.empty[UserModelKey, String]

    /**
     * Main entry point for intent engine.
     *
      * @param mdl Model.
     * @param ctx Query context.
     * @param intents Intents to match for.
     * @return
     */
    private def solveIntents(mdl: NCModel, ctx: NCContext, intents: Map[NCIDLIntent, NCCallbackInput => NCResult]): List[IntentSolverResult] =
        dialog.ack(ctx.getRequest.getUserId)

        val matches = mutable.ArrayBuffer.empty[MatchHolder]

        // Find all matches across all intents and sentence variants.
        for (
            (vrn, vrnIdx) <- ctx.getVariants.zipWithIndex if mdl.onVariant(vrn);
            ents = vrn.getEntities;
            varEntsGroups = ents.filter(t => t.getGroups != null && t.getGroups.nonEmpty).map(_.getGroups);
            (intent, callback) <- intents
        )
            val convEnts: Seq[IntentEntity] =
                if intent.terms.exists(_.conv) then
                    // We do not mix tokens with same group from the conversation and given sentence.
                    ctx.getConversation.getStm.
                        map(ent => ent -> (if ent.getGroups == null then Set.empty else ent.getGroups)).
                        filter { (_, entGroups)  => !varEntsGroups.exists(_.subsetOf(entGroups)) }.
                        map { (e, _) => IntentEntity(used = false, conv = true, e) }
                else
                    Seq.empty

            // Solve intent in isolation.
            solveIntent(ctx, intent, ents.map(IntentEntity(false, false, _)), convEnts, vrnIdx) match
                case Some(intentMatch) => matches += MatchHolder(intentMatch, callback, IntentSolverVariant(vrn.getEntities), vrnIdx)
                case None => // No-op.

        val sorted = matches.sortWith((m1: MatchHolder, m2: MatchHolder) =>
            // 1. First with maximum weight.
            m1.intentMatch.weight.compare(m2.intentMatch.weight) match { // Do not drop this bracket (IDE confused)
                case x1 if x1 < 0 => false
                case x1 if x1 > 0 => true
                case x1 =>
                    require(x1 == 0)

                    logEqualMatches(m1, m2)

                    // 2. First with maximum variant.
                    m1.variant.compareTo(m2.variant) match
                        case x2 if x2 < 0 => false
                        case x2 if x2 > 0 => true
                        case x2 =>
                            require(x2 == 0)

                            def calcHash(m: MatchHolder): Int =
                                val variantPart =
                                    m.variant.
                                        entities.
                                        map(t => s"${t.getId}${t.getGroups}${t.mkText}").
                                        mkString("")

                                val intentPart = m.intentMatch.intent.toString

                                (variantPart, intentPart).##

                            // Order doesn't make sense here.
                            // It is just to provide deterministic result for the matches with the same weights.
                            calcHash(m1) > calcHash(m2)
            }
        )

        logMatches(sorted)

        sorted.map(m =>
            IntentSolverResult(
                m.intentMatch.intent.id,
                m.callback,
                m.intentMatch.entityGroups.map(grp => IntentTermEntities(grp.term.id, grp.usedEntities.map(_.entity))),
                m.variant,
                m.variantIdx
            )
        ).toList

    /**
      *
      * @param matches
      */
    private def logMatches(matches: ArrayBuffer[MatchHolder]): Unit =
        if matches.nonEmpty then
            val tbl = NCAsciiTable("Variant", "Intent", "Term Entities", "Intent Match Weight")

            for (m <- matches)
                val im = m.intentMatch
                val w = im.weight
                val ents = mutable.ListBuffer.empty[String]

                ents += s"intent=${im.intent.id}"
                var grpIdx = 0

                for (grp <- im.entityGroups)
                    ents += s"  ${grp.term.toString}"
                    grpIdx += 1

                    if grp.usedEntities.nonEmpty then
                        var entIdx = 0
                        for (e <- grp.usedEntities)
                            val conv = if e.conv then "(conv) " else ""
                            ents += s"    #$entIdx: $conv${e.entity.getId}(${e.entity.mkText})"
                            entIdx += 1
                    else
                        ents += "    <empty>"

                if m == matches.head then
                    tbl += (
                        Seq(s"#${m.variantIdx + 1}", "<|best match|>"), Seq(im.intent.id, "<|best match|>"), ents, w
                    )
                else
                    tbl += (
                        s"#${m.variantIdx + 1}", im.intent.id, ents, w
                    )

            tbl.info(
                logger,
                Option(s"Found ${matches.size} matching ${if matches.size > 1 then "intents"else "intent"} (sorted best to worst):")
            )
        else
            logger.info(s"No matching intent found:")
            logger.info(s"  +-- Turn on DEBUG log level to see more details.")

    /**
      *
      * @param m1
      * @param m2
      */
    private def logEqualMatches(m1: MatchHolder, m2: MatchHolder): Unit =
        val mw1 = m1.intentMatch.weight
        val mw2 = m2.intentMatch.weight
        val v1 = m1.variant
        val v2 = m2.variant

        val tbl = new NCAsciiTable()

        tbl += (s"${"Intent ID"}", m1.intentMatch.intent.id, m2.intentMatch.intent.id)
        tbl += (s"${"Variant #"}", m1.variantIdx + 1, m2.variantIdx + 1)
        tbl += (s"${"Intent Match Weight"}", mw1.toString, mw2.toString)
        tbl += (s"${"Variant Weight"}", v1.toString, v2.toString)

        tbl.warn(logger, Option("Two matching intents have the same weight for their matches (variants weight will be used further):"))

    /**
     *
     * @param intent
     * @param senEnts
     * @param convEnts
     * @return
     */
    private def solveIntent(
        ctx: NCContext, intent: NCIDLIntent, senEnts: Seq[IntentEntity], convEnts: Seq[IntentEntity], varIdx: Int
    ): Option[IntentMatchHolder] =
        val intentId = intent.id
        val opts = intent.options
        val flow = dialog.getDialogFlow(ctx.getRequest.getUserId)
        val varStr = s"(variant #${varIdx + 1})"

        // Check dialog flow regex first, if any.
        val flowMatched: Boolean =
            intent.flowRegex match
                case Some(regex) =>
                    val flowStr = flow.map(_.getIntentMatch.getIntentId).mkString(" ")

                    def process(matched: Boolean): Boolean =
                        val s = if matched then "matched" else "did not match"
                        logger.info(s"Intent '$intentId' $s regex dialog flow $varStr:")
                        logger.info(s"  |-- ${"Intent IDs  :"} $flowStr")
                        logger.info(s"  +-- ${"Match regex :"} ${regex.toString}")

                        matched

                    process(regex.matcher(flowStr).find(0))
                case None => true

        if flowMatched then
            val intentW = new Weight()
            val intentGrps = mutable.ArrayBuffer.empty[TermEntitiesGroup]
            var abort = false
            var lastTermMatch: TermMatch = null
            val sess = ctx.getConversation.getData // Conversation metadata (shared across all terms).
            val convMeta = sess.keysSet.map(k => k -> sess(k).asInstanceOf[Object]).toMap
            val ents = senEnts.map(_.entity)

            // Check terms.
            for (term <- intent.terms if !abort)
                // Fresh context for each term.
                val idlCtx = NCIDLContext(
                    ctx.getModelConfig,
                    ents,
                    intentMeta = intent.meta,
                    convMeta = convMeta,
                    req = ctx.getRequest,
                    vars = mutable.HashMap.empty[String, NCIDLFunction] ++ term.decls
                )

                solveTerm(term, idlCtx, senEnts, if term.conv then convEnts else Seq.empty) match
                    case Some(termMatch) =>
                        if opts.ordered && lastTermMatch != null && !termMatch.after(lastTermMatch) then
                            abort = true
                        else
                            // Term is found.
                            // Add its weight and grab its entities.
                            intentW += termMatch.weight
                            intentGrps += TermEntitiesGroup(term, termMatch.usedEntities)
                            lastTermMatch = termMatch

                            logMatch(intent, term, termMatch)
                    case None =>
                        // Term is missing. Stop further processing for this intent. This intent cannot be matched.
                        logger.debug(s"Intent '$intentId' did not match because of unmatched term '$term' $varStr.")

                        abort = true

            if abort then
                None
            else
                val usedSenEnts = senEnts.filter(_.used)
                val usedConvEnts = convEnts.filter(_.used)
                val usedToks = usedSenEnts.flatMap(_.entity.getTokens)
                val unusedToks = ctx.getTokens.filter(p => !usedToks.contains(p))

                if !opts.allowStmEntityOnly && usedSenEnts.isEmpty && usedConvEnts.nonEmpty then
                    logger.info(
                        s"""
                           |Intent '$intentId' did not match because all its matched tokens came from STM $varStr.
                           |See intent 'allowStmEntityOnly' option.
                           |""".stripMargin
                    )
                    None
                else if !opts.ignoreUnusedFreeWords && unusedToks.nonEmpty then
                    logger.info(
                        s"""
                           |Intent '$intentId' did not match because of unused free words $varStr.
                           |See intent 'ignoreUnusedFreeWords' option.
                           |Unused free words indexes: ${unusedToks.map(_.getIndex).mkString("{", ",", "}")}
                           |""".stripMargin
                    )
                    None
                else
                    if usedSenEnts.isEmpty && usedConvEnts.isEmpty then
                        logger.warn(s"Intent '$intentId' matched but no entities were used $varStr.")

                    // Number of remaining (unused) non-free words in the sentence is a measure of exactness of the match.
                    // The match is exact when all non-free words are used in that match.
                    // Negate to make sure the bigger (smaller negative number) is better.
                    val nonFreeWordNum = -(ctx.getTokens.size - senEnts.map(_.entity.getTokens.size).sum)

                    intentW.prepend(nonFreeWordNum)

                    Option(IntentMatchHolder(entityGroups = intentGrps.toList, weight = intentW, intent = intent))
        else
            None

    /**
      *
      * @param intent
      * @param term
      * @param termMatch
      */
    private def logMatch(intent: NCIDLIntent, term: NCIDLTerm, termMatch: TermMatch): Unit =
        val tbl = NCAsciiTable()

        val w = termMatch.weight.toSeq

        tbl += ("Intent ID", s"${intent.id}")
        tbl += ("Matched Term", term)
        tbl += (
            "Matched Entities",
            termMatch.usedEntities.map(t =>
                val txt = t.entity.mkText
                val idx = t.entity.getTokens.map(_.getIndex).mkString("{", ",", "}")

                s"$txt${s"[$idx]"}").mkString(" ")
        )
        tbl += (
            s"Term Match Weight", s"${"<"}${w.head}, ${w(1)}, ${w(2)}, ${w(3)}, ${w(4)}, ${w(5)}${">"}"
        )

        tbl.debug(logger, Option("Term match found:"))

    /**
     * Solves term.
     *
     * @param term
     * @param idlCtx
     * @param convEnts
     * @param senEnts
     * @return
     */
    private def solveTerm(
        term: NCIDLTerm,
        idlCtx: NCIDLContext,
        senEnts: Seq[IntentEntity],
        convEnts: Seq[IntentEntity]
    ): Option[TermMatch] =
        if senEnts.isEmpty && convEnts.isEmpty then
            logger.warn(s"No entities available to match on for the term '$term'.")

        try
            solvePredicate(term, idlCtx, senEnts, convEnts) match
                case Some(pm) =>
                    Option(
                        TermMatch(
                            term.id,
                            pm.entities,
                            // If term match is non-empty we add the following weights:
                            //   - min
                            //   - delta between specified max and normalized max (how close the actual quantity was to the specified one).
                            //   - normalized max
                            // NOTE: 'usedEntities' can be empty.
                            pm.weight.
                                append(term.min).
                                append(-(term.max - pm.entities.size)).
                                // Normalize max quantifier in case of unbound max.
                                append(if term.max == Integer.MAX_VALUE then pm.entities.size else term.max)
                        )
                    )
                // Term not found at all.
                case None => None
        catch case e: Exception => E(s"Runtime error processing IDL term: $term", e)

    /**
     * Solves term's predicate.
     *
     * @param term
     * @param idlCtx
     * @param senEnts
     * @param convEnts
     * @return
     */
    private def solvePredicate(
        term: NCIDLTerm,
        idlCtx: NCIDLContext,
        senEnts: Seq[IntentEntity],
        convEnts: Seq[IntentEntity]
    ): Option[PredicateMatch] =
        // Algorithm is "hungry", i.e. it will fetch all entities satisfying item's predicate
        // in entire sentence even if these entities are separated by other already used entities
        // and conversation will be used only to get to the 'max' number of the item.
        val usedEnts = mutable.ArrayBuffer.empty[IntentEntity]
        var usesSum = 0
        var matchesCnt = 0

        // Collect to the 'max' from sentence & conversation, if possible.
        for (ents <- Seq(senEnts, convEnts); ent <- ents.filter(!_.used) if usedEnts.lengthCompare(term.max) < 0)
            val NCIDLStackItem(res, uses) = term.pred.apply(NCIDLEntity(ent.entity, matchesCnt), idlCtx)

            res match
                case b: java.lang.Boolean =>
                    if b then
                        matchesCnt += 1
                        if uses > 0 then
                            usesSum += uses
                            usedEnts += ent

                case _ => throw new NCException(s"Predicate returned non-boolean result: $res")

        // We couldn't collect even 'min' matches.
        if matchesCnt < term.min then
            None
        // Term is optional (min == 0) and no matches found (valid result).
        else if matchesCnt == 0 then
            require(term.min == 0)
            require(usedEnts.isEmpty)

            Option(PredicateMatch(List.empty, new Weight(0, 0, 0)))
        // We've found some matches (and min > 0).
        else
            // Number of entities from the current sentence.
            val senTokNum = usedEnts.count(e => !convEnts.contains(e))

            // Sum of conversation depths for each entities from the conversation.
            // Negated to make sure that bigger (smaller negative number) is better.
            def getConversationDepth(e: IntentEntity): Option[Int] =
                val depth = convEnts.indexOf(e)
                Option.when(depth >= 0)(depth + 1)

            val convDepthsSum = -usedEnts.flatMap(getConversationDepth).sum

            // Mark found entities as used.
            for (e <- usedEnts) e.used = true

            Option(PredicateMatch(usedEnts.toList, new Weight(senTokNum, convDepthsSum, usesSum)))

    /**
      *
      * @param mdl
      * @param ctx
      * @param typ
      * @param key
      * @return
      */
    private def solveIteration(mdl: NCModel, ctx: NCContext, typ: NCIntentSolveType, key: UserModelKey): Option[IterationResult] =
        require(intents.nonEmpty)

        val req = ctx.getRequest

        val intentResults =
            try solveIntents(mdl, ctx, intents)
            catch case e: Exception => throw new NCRejection("Processing failed due to unexpected error.", e)

        if intentResults.isEmpty then throw new NCRejection("No matching intent found.")

        object Loop:
            private var data: Option[IterationResult] = _

            def hasNext: Boolean = data == null
            def finish(data: IterationResult): Unit = Loop.data = Option(data)
            def finish(): Unit = Loop.data = None
            def result(): Option[IterationResult] =
                if data == null then throw new NCRejection("No matching intent found - all intents were skipped.")
                data

        for (intentRes <- intentResults.filter(_ != null) if Loop.hasNext)
            def mkIntentMatch(arg: List[List[NCEntity]]): NCIntentMatch =
                new NCIntentMatch:
                    override val getIntentId: String = intentRes.intentId
                    override val getIntentEntities: List[List[NCEntity]] = intentRes.groups.map(_.entities)
                    override def getTermEntities(idx: Int): List[NCEntity] = intentRes.groups(idx).entities
                    override def getTermEntities(termId: String): List[NCEntity] =
                        intentRes.groups.find(_.termId === termId) match
                            case Some(g) => g.entities
                            case None => List.empty
                    override val getVariant: NCVariant =
                        new NCVariant:
                            override def getEntities: List[NCEntity] = intentRes.variant.entities

            val im = mkIntentMatch(intentRes.groups.map(_.entities))
            try
                if mdl.onMatchedIntent(ctx, im) then
                    // This can throw NCIntentSkip exception.
                    import NCIntentSolveType.*

                    def saveHistory(res: Option[NCResult], im: NCIntentMatch): Unit =
                        dialog.addMatchedIntent(im, res, ctx)
                        conv.getConversation(req.getUserId).addEntities(
                            req.getRequestId, im.getIntentEntities.flatten.distinct
                        )
                        logger.info(s"Intent '${intentRes.intentId}' for variant #${intentRes.variantIdx + 1} selected as the <|best match|>")

                    def executeCallback(in: NCCallbackInput): NCResult =
                        var cbRes = intentRes.fn(in)
                        // Store winning intent match in the input.
                        if cbRes.getIntentId.isEmpty then cbRes = NCResult(cbRes.getBody, cbRes.getType, intentRes.intentId)
                        cbRes

                    def finishSearch(): Unit =
                        @volatile var called = false

                        def f(args: List[List[NCEntity]]): NCResult =
                            if called then E("Callback was already called.")
                            called = true

                            val reqId = reqIds.synchronized { reqIds.getOrElse(key, null) }

                            if reqId != ctx.getRequest.getRequestId then E("Callback is out of date.")

                            typ match
                                case SEARCH =>
                                    val imNew = mkIntentMatch(args)
                                    val cbRes = executeCallback(NCCallbackInput(ctx, imNew))
                                    dialog.replaceLastItem(imNew, cbRes, ctx)
                                    cbRes
                                case SEARCH_NO_HISTORY => executeCallback(NCCallbackInput(ctx, mkIntentMatch(args)))
                                case _ => throw new AssertionError(s"Unexpected state: $typ")

                        Loop.finish(IterationResult(Right(CallbackDataImpl(im.getIntentId, im.getIntentEntities, f)), im))

                    typ match
                        case REGULAR =>
                            val cbRes = executeCallback(NCCallbackInput(ctx, im))
                            saveHistory(Option(cbRes), im)
                            Loop.finish(IterationResult(Left(cbRes), im))
                        case SEARCH =>
                            saveHistory(None, im)
                            finishSearch()
                        case SEARCH_NO_HISTORY =>
                            finishSearch()
                else
                    logger.info(s"Model '${ctx.getModelConfig.getId}' triggered rematching of intents by intent '${intentRes.intentId}' on variant #${intentRes.variantIdx + 1}.")
                    Loop.finish()
                catch
                    case e: NCIntentSkip =>
                        // No-op - just skipping this result.
                        e.getMessage match
                            case s if s != null => logger.info(s"Selected intent '${intentRes.intentId}' skipped: $s")
                            case _ => logger.info(s"Selected intent '${intentRes.intentId}' skipped.")

        Loop.result()

    /**
      *
      * @param mdl
      * @param ctx
      * @param typ
      * @return
      */
    def solve(mdl: NCModel, ctx: NCContext, typ: NCIntentSolveType): ResultData =
        import NCIntentSolveType.REGULAR

        val key = UserModelKey(ctx.getRequest.getUserId, mdl.getConfig.getId)
        reqIds.synchronized { reqIds.put(key, ctx.getRequest.getRequestId)}

        mdl.onContext(ctx) match
            case Some(mdlCtxRes) =>
                if typ != REGULAR then E("'onContext()' method is overridden, intents cannot be found.")
                if intents.nonEmpty then logger.warn("'onContext()' method overrides existing intents - they are ignored.")

                Left(mdlCtxRes)
            case None =>
                if intents.isEmpty then
                    throw NCRejection("There are no registered intents and model's 'onContext()' method returns 'null' result.")

                var loopRes: IterationResult = null

                try
                    while (loopRes == null)
                        solveIteration(mdl, ctx, typ, key) match
                            case Some(iterRes) => loopRes = iterRes
                            case None => // No-op.

                    typ match
                        case REGULAR =>
                            mdl.onResult(ctx, loopRes.intentMatch, loopRes.result.swap.toOption.get) match
                                case Some(mdlRes) => Left(mdlRes)
                                case None => loopRes.result

                        case _ => loopRes.result
                catch
                    case e: NCRejection =>
                        typ match
                            case REGULAR =>
                                mdl.onRejection(ctx, Option.when(loopRes != null)(loopRes.intentMatch), e) match
                                    case Some(mdlRejRes) => Left(mdlRejRes)
                                    case None => throw e

                            case _ => throw e

                    case e: Throwable =>
                        typ match
                            case REGULAR =>
                                mdl.onError(ctx, e) match
                                    case Some(mdlErrRes) =>
                                        logger.warn("Error during execution.", e)
                                        Left(mdlErrRes)
                                    case None => throw e
                            case _ => throw e
    /**
      *
      */
    def close(): Unit = reqIds.clear()