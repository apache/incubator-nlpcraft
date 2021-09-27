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

        def isUnprocessed(elemId: String, s: Synonym, tokens: Seq[T]): Boolean =
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
                ).add(s)
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

    private case class Value(request: NCRequest, variants: Seq[Seq[NCToken]], predicate: NCIdlFunction) {
        override def toString: String = variants.toString()
    }

    private case class IdlChunkKey(token: IdlToken, chunk: NCProbeSynonymChunk)

    private val savedIdl = mutable.HashMap.empty[String, mutable.HashMap[SavedIdlKey, mutable.ArrayBuffer[Value]]]
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
      * @param s
      * @param toks
      * @param isMatch
      * @param getIndex
      * @param shouldBeNeighbors
      * @tparam T
      */
    private def sparseMatch0[T](
        s: Synonym,
        toks: Seq[T],
        isMatch: (T, NCProbeSynonymChunk) => Boolean,
        getIndex: T => Int,
        shouldBeNeighbors: Boolean
    ): Option[Seq[T]] =
        if (toks.size >= s.size) {
            lazy val res = mutable.ArrayBuffer.empty[T]
            lazy val all = mutable.HashSet.empty[T]

            var state = 0

            for (chunk <- s if state != -1) {
                val seq =
                    if (state == 0) {
                        state = 1

                        toks.filter(t => isMatch(t, chunk))
                    }
                    else
                        toks.filter(t => !res.contains(t) && isMatch(t, chunk))

                if (seq.nonEmpty) {
                    val head = seq.head

                    if (!s.permute && res.nonEmpty && getIndex(head) <= getIndex(res.last))
                        state = -1
                    else {
                        all ++= seq

                        if (all.size > s.size)
                            state = -1
                        else
                            res += head
                    }
                }
                else
                    state = -1
            }

            if (state != -1 && all.size == res.size && (!shouldBeNeighbors || U.isIncreased(res.map(getIndex).toSeq.sorted)))
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
                Value(req, variantsToks, pred)
    }

    /**
      *
      * @param srvReqId
      * @param elemId
      * @param s
      * @param tokens
      */
    private def isUnprocessedTokens(srvReqId: String, elemId: String, s: Synonym, tokens: Seq[Int]): Boolean =
        tokCaches.getOrElseUpdate(srvReqId, new CacheHolder[Int]).isUnprocessed(elemId, s, tokens)

    /**
      *
      * @param srvReqId
      * @param elemId
      * @param s
      * @param tokens
      */
    private def isUnprocessedIdl(srvReqId: String, elemId: String, s: Synonym, tokens: Seq[IdlToken]): Boolean =
        idlCaches.getOrElseUpdate(srvReqId, new CacheHolder[IdlToken]).isUnprocessed(elemId, s, tokens)

    /**
      *
      * @param tow
      * @param chunk
      * @param req
      * @param variantsToks
      */
    private def isMatch(
        tow: IdlToken, chunk: NCProbeSynonymChunk, req: NCRequest, variantsToks: Seq[Seq[NCToken]]
    ): Boolean =
        idlChunksCache.
            getOrElseUpdate(req.getServerRequestId,
                mutable.HashMap.empty[IdlChunkKey, Boolean]
            ).
            getOrElseUpdate(
                IdlChunkKey(tow, chunk),
                {
                    def get0[T](fromToken: NCToken => T, fromWord: NlpToken => T): T =
                        if (tow.isToken) fromToken(tow.token) else fromWord(tow.word)

                    chunk.kind match {
                        case TEXT => chunk.wordStem == get0(_.stem, _.stem)

                        case REGEX =>
                            chunk.regex.matcher(get0(_.origText, _.origText)).matches() ||
                            chunk.regex.matcher(get0(_.normText, _.normText)).matches()

                        case IDL =>
                            val ok =
                                variantsToks.par.exists(vrntToks =>
                                    get0(t =>
                                        chunk.idlPred.apply(t, NCIdlContext(toks = vrntToks, req = req)).
                                            value.asInstanceOf[Boolean],
                                        _ => false
                                    )
                                )

                            if (ok)
                                save(req, tow.token, chunk.idlPred, variantsToks)

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

            lazy val matched =
                if (syn.isTextOnly)
                    toks.zip(syn).
                        forall(p => p._1.stem == p._2.wordStem)
                else
                    toks.zip(syn).
                        sortBy(p => getSort(p._2.kind)).
                        forall { case (tok, chunk) => isMatch(tok, chunk) }

            if (toks.length == syn.length && matched)
                callback()
        }

    /**
      *
      * @param srvReqId
      * @param elemId
      * @param s
      * @param toks
      * @param req
      * @param variantsToks
      * @param callback
      */
    def onMatch(
        srvReqId: String,
        elemId: String,
        s: Synonym,
        toks: Seq[IdlToken],
        req: NCRequest,
        variantsToks: Seq[Seq[NCToken]],
        callback: Unit => Unit
    ): Unit =
        if (isUnprocessedIdl(srvReqId, elemId, s, toks)) {
            require(toks != null)

            lazy val matched =
                toks.zip(s).
                    sortBy(p => getSort(p._2.kind)).
                    forall { case (tow, chunk) => isMatch(tow, chunk, req, variantsToks) }

            if (toks.length == s.length && toks.count(_.isToken) >= s.idlChunks && matched)
                callback()
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
                (t: IdlToken) => if (t.isToken) t.token.getStartCharIndex else t.word.startCharIndex,
                shouldBeNeighbors = !syn.sparse
            ) match {
                case Some(res) => callback(res)
                case None => // No-op.
            }
        }

    /**
      *
      * @param srvReqId
      * @param senToks
      */
    def isStillValidIdl(srvReqId: String, senToks: Seq[NCToken]): Boolean =
        savedIdl.get(srvReqId) match {
            case Some(m) =>
                lazy val allCheckedSenToks = {
                    val set = mutable.HashSet.empty[SavedIdlKey]

                    def add(t: NCToken): Unit = {
                        set += SavedIdlKey(t)

                        t.getPartTokens.asScala.foreach(add)
                    }

                    senToks.foreach(add)

                    set
                }

                senToks.forall(tok =>
                    m.get(SavedIdlKey(tok)) match {
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
                    })

            case None => true
        }

    /**
      *
      * @param srvReqId
      */
    def clearRequestData(srvReqId: String): Unit = {
        clearIteration(srvReqId)

        savedIdl -= srvReqId
    }

    /**
      *
      * @param srvReqId
      */
    def clearIteration(srvReqId: String): Unit = {
        idlChunksCache -= srvReqId
        idlCaches -= srvReqId
        tokCaches -= srvReqId
    }
}
