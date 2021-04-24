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

import org.apache.nlpcraft.common.U
import org.apache.nlpcraft.common.nlp.{NCNlpSentenceToken, NCNlpSentenceTokenBuffer}
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.intent.NCIdlContext
import org.apache.nlpcraft.probe.mgrs.NCProbeSynonym.NCIdlContent
import org.apache.nlpcraft.probe.mgrs.NCProbeSynonymChunkKind._

import scala.collection.mutable

/**
  *
  * @param isElementId Is this an implicit element ID synonym?
  *     In this case chunks contain the element ID.
  * @param isValueName Is this an implicit value name synonym?
  *     In this case chunks contain value name.
  * @param isDirect Direct or permuted synonym flag.
  * @param value Optional value name if this is a value synonym.
  * @param sparse Flag.
  * @param permute Flag.
  */
class NCProbeSynonym(
    val isElementId: Boolean,
    val isValueName: Boolean,
    val isDirect: Boolean,
    val value: String = null,
    val sparse: Boolean,
    val permute: Boolean
) extends mutable.ArrayBuffer[NCProbeSynonymChunk] with Ordered[NCProbeSynonym] {
    require((isElementId && !isValueName && value == null) || !isElementId)
    require((isValueName && value != null) || !isValueName)

    lazy val isTextOnly: Boolean = forall(_.kind == TEXT)
    lazy val regexChunks: Int = count(_.kind == REGEX)
    lazy val idlChunks: Int = count(_.kind == IDL)
    lazy val hasIdl: Boolean = idlChunks != 0
    lazy val isValueSynonym: Boolean = value != null
    lazy val stems: String = map(_.wordStem).mkString(" ")
    lazy val stemsHash: Int = stems.hashCode

    /**
      *
      * @param kind
      * @return
      */
    private def getSort(kind: NCSynonymChunkKind): Int =
        kind match {
            case TEXT ⇒ 0
            case IDL ⇒ 1
            case REGEX ⇒ 2
            case _ ⇒ throw new AssertionError(s"Unexpected kind: $kind")
        }

    /**
      *
      * @param tok
      * @param chunk
      */
    private def isMatch(tok: NCNlpSentenceToken, chunk: NCProbeSynonymChunk): Boolean =
        chunk.kind match {
            case TEXT ⇒ chunk.wordStem == tok.stem
            case REGEX ⇒
                val regex = chunk.regex

                regex.matcher(tok.origText).matches() || regex.matcher(tok.normText).matches()
            case IDL ⇒ throw new AssertionError()
            case _ ⇒ throw new AssertionError()
        }

    /**
      *
      * @param toks
      * @param isMatch
      * @param getIndex
      * @param shouldBeNeighbors
      * @tparam T
      * @return
      */
    private def sparseMatch0[T](
        toks: Seq[T],
        isMatch: (T, NCProbeSynonymChunk) ⇒ Boolean,
        getIndex: T ⇒ Int,
        shouldBeNeighbors: Boolean
    ): Option[Seq[T]] =
        if (toks.size >= this.size) {
            lazy val res = mutable.ArrayBuffer.empty[T]
            lazy val all = mutable.HashSet.empty[T]

            var state = 0

            for (chunk ← this if state != -1) {
                val seq =
                    if (state == 0) {
                        state = 1

                        toks.filter(t ⇒ isMatch(t, chunk))
                    }
                    else
                        toks.filter(t ⇒ !res.contains(t) && isMatch(t, chunk))

                if (seq.nonEmpty) {
                    val head = seq.head

                    if (!permute && res.nonEmpty && getIndex(head) <= getIndex(res.last))
                        state = -1
                    else {
                        all ++= seq

                        if (all.size > this.size)
                            state = -1
                        else
                            res += head
                    }
                }
                else
                    state = -1
            }

            if (state != -1 && all.size == res.size && (!shouldBeNeighbors || U.isIncreased(res.map(getIndex).sorted)))
                Some(res)
            else
                None
        }
        else
            None

    /**
      *
      * @param tow
      * @param chunk
      * @param req
      */
    private def isMatch(tow: NCIdlContent, chunk: NCProbeSynonymChunk, req: NCRequest): Boolean = {
        def get0[T](fromToken: NCToken ⇒ T, fromWord: NCNlpSentenceToken ⇒ T): T =
            if (tow.isLeft) fromToken(tow.left.get) else fromWord(tow.right.get)

        chunk.kind match {
            case TEXT ⇒ chunk.wordStem == get0(_.stem, _.stem)
            case REGEX ⇒
                val r = chunk.regex

                r.matcher(get0(_.origText, _.origText)).matches() || r.matcher(get0(_.normText, _.normText)).matches()

            case IDL ⇒
                get0(t ⇒ chunk.idlPred.apply(t, NCIdlContext(req = req)).value.asInstanceOf[Boolean], _ ⇒ false)

            case _ ⇒ throw new AssertionError()
        }
    }

    /**
      *
      * @param toks
      */
    def isMatch(toks: NCNlpSentenceTokenBuffer): Boolean = {
        require(toks != null)
        require(!sparse && !hasIdl)

        if (toks.length == length) {
            if (isTextOnly)
                toks.stemsHash == stemsHash && toks.stems == stems
            else
                toks.zip(this).sortBy(p ⇒ getSort(p._2.kind)).forall { case (tok, chunk) ⇒ isMatch(tok, chunk) }
        }
        else
            false
    }

    /**
      *
      * @param tows
      * @param req
      * @return
      */
    def isMatch(tows: Seq[NCIdlContent], req: NCRequest): Boolean = {
        require(tows != null)

        if (tows.length == length && tows.count(_.isLeft) >= idlChunks)
            tows.zip(this).sortBy(p ⇒ getSort(p._2.kind)).forall { case (tow, chunk) ⇒ isMatch(tow, chunk, req) }
        else
            false
    }
    
    /**
      *
      * @param toks
      */
    def sparseMatch(toks: NCNlpSentenceTokenBuffer): Option[Seq[NCNlpSentenceToken]] = {
        require(toks != null)
        require(sparse && !hasIdl)

        sparseMatch0(toks, isMatch, (t: NCNlpSentenceToken) ⇒ t.startCharIndex, shouldBeNeighbors = false)
    }

    /**
      *
      * @param tows
      * @param req
      */
    def sparseMatch(tows: Seq[NCIdlContent], req: NCRequest): Option[Seq[NCIdlContent]] = {
        require(tows != null)
        require(req != null)
        require(hasIdl)

        sparseMatch0(
            tows,
            (t: NCIdlContent, chunk: NCProbeSynonymChunk) ⇒ isMatch(t, chunk, req),
            (t: NCIdlContent) ⇒ if (t.isLeft) t.left.get.getStartCharIndex else t.right.get.startCharIndex,
            shouldBeNeighbors = !sparse
        )
    }

    override def toString(): String = mkString(" ")

    // Orders synonyms from least to most significant.
    override def compare(that: NCProbeSynonym): Int = {
        require(hasIdl || sparse == that.sparse, s"Invalid comparing [this=$this, that=$that]")

        def compareIsValueSynonym(): Int =
            isValueSynonym match {
                case true if !that.isValueSynonym ⇒ 1
                case false if that.isValueSynonym ⇒ -1

                case _ ⇒ 0
            }

        if (that == null)
            1
        else
            isElementId match {
                case true if !that.isElementId ⇒ 1
                case false if that.isElementId ⇒ -1
                case true if that.isElementId ⇒ 0

                case _ ⇒ // None are element IDs.
                    if (length > that.length)
                        1
                    else if (length < that.length)
                        -1
                    else { // Equal length in chunks.
                        if (isDirect && !that.isDirect)
                            1
                        else if (!isDirect && that.isDirect)
                            -1
                        else if (permute && !that.permute)
                            -1
                        else if (!permute && that.permute)
                            1
                        else // Both direct or indirect.
                            isTextOnly match {
                                case true if !that.isTextOnly ⇒ 1
                                case false if that.isTextOnly ⇒ -1
                                case true if that.isTextOnly ⇒ compareIsValueSynonym()
                                case _ ⇒
                                    val thisDynCnt = regexChunks + idlChunks
                                    val thatDynCnt = that.regexChunks + that.idlChunks

                                    // Less PoS/regex/IDL chunks means less uncertainty, i.e. larger weight.
                                    if (thisDynCnt < thatDynCnt)
                                        1
                                    else if (thisDynCnt > thatDynCnt)
                                        -1
                                    else
                                        0
                            }
                    }
            }
    }

    override def canEqual(other: Any): Boolean = other.isInstanceOf[NCProbeSynonym]

    override def equals(other: Any): Boolean = other match {
        case that: NCProbeSynonym ⇒
            super.equals(that) &&
                (that canEqual this) &&
                isTextOnly == that.isTextOnly &&
                regexChunks == that.regexChunks &&
                idlChunks == that.idlChunks &&
                isValueSynonym == that.isValueSynonym &&
                isElementId == that.isElementId &&
                isValueName == that.isValueName &&
                value == that.value
        case _ ⇒ false
    }

    override def hashCode(): Int = {
        val state = Seq(
            super.hashCode(),
            isTextOnly,
            regexChunks,
            idlChunks,
            isValueSynonym,
            isElementId,
            isValueName,
            value
        )

        state.map(p ⇒ if (p == null) 0 else p.hashCode()).foldLeft(0)((a, b) ⇒ 31 * a + b)
    }
}

object NCProbeSynonym {
    type NCIdlContent = Either[NCToken, NCNlpSentenceToken]

    /**
      *
      * @param isElementId
      * @param isValueName
      * @param isDirect
      * @param value
      * @param chunks
      * @param sparse
      * @param permute
      */
    def apply(
        isElementId: Boolean,
        isValueName: Boolean,
        isDirect: Boolean,
        value: String,
        chunks: Seq[NCProbeSynonymChunk],
        sparse: Boolean,
        permute: Boolean
    ): NCProbeSynonym = {
        val syn = new NCProbeSynonym(isElementId, isValueName, isDirect, value, sparse, permute)
        
        syn ++= chunks
        
        syn
    }
}
