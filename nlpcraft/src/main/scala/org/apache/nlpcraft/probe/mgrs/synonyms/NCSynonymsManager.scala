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

package org.apache.nlpcraft.probe.mgrs.synonyms

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.nlp.{NCNlpSentenceNote => NlpNote, NCNlpSentenceToken => NlpToken}
import org.apache.nlpcraft.common.{NCService, U}
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.intent.{NCIdlContext, NCIdlFunction}
import org.apache.nlpcraft.probe.mgrs.NCProbeSynonymChunkKind.{IDL, NCSynonymChunkKind, REGEX, TEXT}
import org.apache.nlpcraft.probe.mgrs.{NCProbeIdlToken => IdlToken, NCProbeSynonymChunk, NCProbeSynonym => Synonym}

import scala.collection.mutable
import scala.collection.parallel.CollectionConverters.ImmutableIterableIsParallelizable
import scala.compat.java8.OptionConverters._
import scala.jdk.CollectionConverters.ListHasAsScala

/**
  *
  */
object NCSynonymsManager extends NCService {
    private class CacheHolder[T] {
        private lazy val cache =
            mutable.HashMap.empty[String, mutable.HashMap[Int, mutable.HashMap[Seq[T], mutable.HashSet[Synonym]]]]

        def isUnprocessed(elemId: String, syn: Synonym, tokens: Seq[T]): Boolean =
            cache.
                getOrElseUpdate(
                    elemId,
                    mutable.HashMap.empty[Int, mutable.HashMap[Seq[T], mutable.HashSet[Synonym]]]
                ).
                getOrElseUpdate(
                    tokens.length,
                    mutable.HashMap.empty[Seq[T], mutable.HashSet[Synonym]]
                ).
                getOrElseUpdate(
                    tokens,
                    mutable.HashSet.empty[Synonym]
                ).add(syn)
    }

    private case class SavedIdlKey(id: String, startCharIndex: Int, endCharIndex: Int, other: Map[String, AnyRef] = Map.empty)

    private object SavedIdlKey {
        def apply(t: NCToken): SavedIdlKey =
            if (t.isUserDefined)
                SavedIdlKey(t.getId, t.getStartCharIndex, t.getEndCharIndex)
            else
                SavedIdlKey(
                    t.getId,
                    t.getStartCharIndex,
                    t.getEndCharIndex,
                    NlpNote.getBuiltProperties(t.getId).flatMap(p => t.metaOpt(p).asScala match {
                        case Some(v) => Some(p -> v)
                        case None => None
                    }).toMap
                )
    }

    private case class SavedIdlValue(request: NCRequest, variants: Seq[Seq[NCToken]], predicate: NCIdlFunction)

    private case class IdlChunkKey(token: IdlToken, chunk: NCProbeSynonymChunk)

    private val savedIdl = mutable.HashMap.empty[String, mutable.HashMap[SavedIdlKey, mutable.ArrayBuffer[SavedIdlValue]]]
    private val idlChunksCache = mutable.HashMap.empty[String, mutable.HashMap[IdlChunkKey, Boolean]]
    private val idlCaches = mutable.HashMap.empty[String, CacheHolder[IdlToken]]
    private val tokCaches = mutable.HashMap.empty[String, CacheHolder[Int]]

    override def start(parent: Span): NCService = {
        ackStarting()
        ackStarted()
    }

    override def stop(parent: Span): Unit = {
        ackStopping()
        ackStopped()
    }

    /**
      *
      * @param tok
      * @param chunk
      */
    private def isMatch(tok: NlpToken, chunk: NCProbeSynonymChunk): Boolean =
        chunk.kind match {
            case TEXT => chunk.wordStem == tok.stem
            case REGEX => chunk.regex.matcher(tok.origText).matches() || chunk.regex.matcher(tok.normText).matches()
            case IDL => throw new AssertionError()
            case _ => throw new AssertionError()
        }

    /**
      *
      * @param kind
      */
    private def getSort(kind: NCSynonymChunkKind): Int =
        kind match {
            case TEXT => 0
            case IDL => 1
            case REGEX => 2
            case _ => throw new AssertionError(s"Unexpected kind: $kind")
        }

    /**
      *
      * @param syn
      * @param toks
      * @param isMatch
      * @param getIndex
      * @param shouldBeNeighbors
      * @tparam T
      */
    private def sparseMatch0[T](
        syn: Synonym,
        toks: Seq[T],
        isMatch: (T, NCProbeSynonymChunk) => Boolean,
        getIndex: T => Int,
        shouldBeNeighbors: Boolean
    ): Option[Seq[T]] =
        if (toks.size >= syn.size) {
            lazy val res = mutable.ArrayBuffer.empty[T]
            lazy val all = mutable.HashSet.empty[T]

            // There are 3 states:
            // 0 - initial working state, first step.
            // 1 - working state, not first step.
            // -1 - stop state.
            var state = 0

            for (chunk <- syn if state != -1) {
                val seq =
                    if (state == 0) {
                        state = 1

                        toks.filter(t => isMatch(t, chunk))
                    }
                    else
                        toks.filter(t => !res.contains(t) && isMatch(t, chunk))

                if (seq.nonEmpty) {
                    val head = seq.head

                    if (!syn.permute && res.nonEmpty && getIndex(head) <= getIndex(res.last))
                        state = -1
                    else {
                        all ++= seq

                        if (all.size > syn.size)
                            state = -1
                        else
                            res += head
                    }
                }
                else
                    state = -1
            }

            if (
                state != -1 && // State ok.
                all.size == res.size && // There aren't excess processed tokens.
                // `neighbors` conditions, important for simple not sparse synonyms.
                (!shouldBeNeighbors || U.isIncreased(res.map(getIndex).toSeq.sorted))
            )
                Some(res.toSeq)
            else
                None
        }
        else
            None

    /**
      *
      * @param req
      * @param tok
      * @param pred
      * @param variantsToks
      */
    private def save(req: NCRequest, tok: NCToken, pred: NCIdlFunction, variantsToks: Seq[Seq[NCToken]]): Unit = {
        savedIdl.
            getOrElseUpdate(req.getServerRequestId, mutable.HashMap.empty).
                getOrElseUpdate(SavedIdlKey(tok), mutable.ArrayBuffer.empty) +=
                    SavedIdlValue(req, variantsToks, pred)
    }

    /**
      * Checks that given synonym is not checked yet with given NLP tokens' indexes.
      *
      * @param srvReqId
      * @param elemId
      * @param syn
      * @param tokens
      */
    private def isUnprocessedTokens(srvReqId: String, elemId: String, syn: Synonym, tokens: Seq[Int]): Boolean =
        tokCaches.getOrElseUpdate(srvReqId, new CacheHolder[Int]).isUnprocessed(elemId, syn, tokens)

    /**
      * Checks that given synonym is not checked yet with given IDL tokens.
      *
      * @param srvReqId
      * @param elemId
      * @param syn
      * @param tokens
      */
    private def isUnprocessedIdl(srvReqId: String, elemId: String, syn: Synonym, tokens: Seq[IdlToken]): Boolean =
        idlCaches.getOrElseUpdate(srvReqId, new CacheHolder[IdlToken]).isUnprocessed(elemId, syn, tokens)

    /**
      * Checks matching IDL token with synonym's chunk.
      *
      * @param t IDL token.
      * @param chunk Synonym's chunk.
      * @param req Request.
      * @param variantsToks All possible request's variants.
      */
    private def isMatch(
        t: IdlToken, chunk: NCProbeSynonymChunk, req: NCRequest, variantsToks: Seq[Seq[NCToken]]
    ): Boolean =
        idlChunksCache.
            getOrElseUpdate(
                req.getServerRequestId,
                mutable.HashMap.empty[IdlChunkKey, Boolean]
            ).
            getOrElseUpdate(
                IdlChunkKey(t, chunk),
                {
                    chunk.kind match {
                        case TEXT => chunk.wordStem == t.stem

                        case REGEX =>
                            chunk.regex.matcher(t.origText).matches() || chunk.regex.matcher(t.normText).matches()

                        case IDL =>
                            val ok = {
                                // IDL condition just for tokens.
                                t.isToken &&
                                // Should be found at least one suitable variant (valid NCIdlContext) for given token.
                                // This variant will be checked again on last processing phase.
                                variantsToks.par.exists(vrntToks =>
                                    chunk.idlPred.apply(
                                        t.token,
                                        NCIdlContext(toks = vrntToks, req = req)).value.asInstanceOf[Boolean]
                                    )
                            }

                            // Saves all variants for next validation.
                            // All suitable variants can be deleted, so this positive result can be abolished
                            // on last processing phase.
                            if (ok)
                                save(req, t.token, chunk.idlPred, variantsToks)

                            ok

                        case _ => throw new AssertionError()
                    }
                }
            )

    /**
      *
      * @param srvReqId
      * @param elemId
      * @param syn
      * @param toks
      * @param callback
      */
    def onMatch(srvReqId: String, elemId: String, syn: Synonym, toks: Seq[NlpToken], callback: Unit => Unit): Unit =
        if (isUnprocessedTokens(srvReqId, elemId, syn, toks.map(_.index))) {
            require(toks != null)
            require(!syn.sparse && !syn.hasIdl)

            if (toks.length == syn.length) { // Same length.
                val ok =
                    if (syn.isTextOnly)
                        toks.zip(syn).
                            // Checks all synonym chunks with all tokens.
                            forall { case (tok, chunk) => tok.stem == chunk.wordStem }
                    else
                        toks.zip(syn).
                            // Pre-sort by chunk kind for performance reasons, easier to compare should be first.
                            sortBy { case (_, chunk) => getSort(chunk.kind) }.
                            // Checks all synonym chunks with all tokens.
                            forall { case (tok, chunk) => isMatch(tok, chunk) }

                if (ok)
                    callback(())
            }
        }

    /**
      *
      * @param srvReqId
      * @param elemId
      * @param syn
      * @param toks
      * @param req
      * @param variantsToks
      * @param callback
      */
    def onMatch(
        srvReqId: String,
        elemId: String,
        syn: Synonym,
        toks: Seq[IdlToken],
        req: NCRequest,
        variantsToks: Seq[Seq[NCToken]],
        callback: Unit => Unit
    ): Unit =
        if (isUnprocessedIdl(srvReqId, elemId, syn, toks)) {
            require(toks != null)

            if (
                toks.length == syn.length && // Same length.
                toks.count(_.isToken) >= syn.idlChunks && // Enough tokens.
                toks.zip(syn).sortBy { // Pre-sort by chunk kind for performance reasons, easier to compare should be first.
                    case (_, chunk) => getSort(chunk.kind)
                }.
                forall { // Checks all synonym chunks with all tokens.
                    case (idlTok, chunk) => isMatch(idlTok, chunk, req, variantsToks)
                }
            )
                callback(())
        }

    /**
      *
      * @param srvReqId
      * @param elemId
      * @param syn
      * @param toks
      * @param callback
      */
    def onSparseMatch(
        srvReqId: String, elemId: String, syn: Synonym, toks: Seq[NlpToken], callback: Seq[NlpToken] => Unit
    ): Unit =
        if (isUnprocessedTokens(srvReqId, elemId, syn, toks.map(_.index))) {
            require(toks != null)
            require(syn.sparse && !syn.hasIdl)

            sparseMatch0(syn, toks, isMatch, (t: NlpToken) => t.startCharIndex, shouldBeNeighbors = false) match {
                case Some(res) => callback(res)
                case None => // No-op.
            }
        }

    /**
      *
      * @param srvReqId
      * @param elemId
      * @param syn
      * @param toks
      * @param req
      * @param variantsToks
      * @param callback
      */
    def onSparseMatch(
        srvReqId: String,
        elemId: String,
        syn: Synonym,
        toks: Seq[IdlToken],
        req: NCRequest,
        variantsToks: Seq[Seq[NCToken]],
        callback: Seq[IdlToken] => Unit
    ): Unit =
        if (isUnprocessedIdl(srvReqId, elemId, syn, toks)) {
            require(toks != null)
            require(req != null)
            require(syn.hasIdl)

            sparseMatch0(
                syn,
                toks,
                (t: IdlToken, chunk: NCProbeSynonymChunk) => isMatch(t, chunk, req, variantsToks),
                (t: IdlToken) => t.startCharIndex,
                shouldBeNeighbors = !syn.sparse
            ) match {
                case Some(res) => callback(res)
                case None => // No-op.
            }
        }

    /**
      * Checks that suitable variant wasn't deleted and IDL condition for token is still valid.
      * We have to check it because NCIdlContext which used in predicate based on variant.
      *
      * @param srvReqId
      * @param toks
      */
    def isStillValidIdl(srvReqId: String, toks: Seq[NCToken]): Boolean =
        savedIdl.get(srvReqId) match {
            case Some(map) =>
                lazy val allCheckedSenToks = {
                    val set = mutable.HashSet.empty[SavedIdlKey]

                    def add(t: NCToken): Unit = {
                        set += SavedIdlKey(t)

                        t.getPartTokens.asScala.foreach(add)
                    }

                    toks.foreach(add)

                    set
                }

                toks.forall(tok =>
                    if (tok.isUserDefined)
                        map.get(SavedIdlKey(tok)) match {
                            case Some(vals) =>
                                vals.exists(
                                    v =>
                                        v.variants.exists(winHistVariant =>
                                            v.predicate.apply(
                                                tok, NCIdlContext(toks = winHistVariant, req = v.request)
                                            ).value.asInstanceOf[Boolean] &&
                                                winHistVariant.map(SavedIdlKey(_)).forall(t =>
                                                    t.id == "nlpcraft:nlp" || allCheckedSenToks.contains(t)
                                                )
                                        )
                                )

                            case None => true
                        }
                    else
                        true
                )

            case None => true
        }

    /**
      * Called when request processing finished.
      *
      * @param srvReqId
      */
    def clearRequestData(srvReqId: String): Unit = {
        clearIteration(srvReqId)

        savedIdl -= srvReqId
    }

    /**
      * Called on each request enrichment iteration.
      *
      * @param srvReqId
      */
    def clearIteration(srvReqId: String): Unit = {
        idlChunksCache -= srvReqId
        idlCaches -= srvReqId
        tokCaches -= srvReqId
    }
}
