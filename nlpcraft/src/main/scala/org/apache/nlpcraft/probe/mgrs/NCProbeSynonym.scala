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

import org.apache.nlpcraft.common.nlp.{NCNlpSentenceToken, NCNlpSentenceTokenBuffer}
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.intent.NCDslContext
import org.apache.nlpcraft.probe.mgrs.NCProbeSynonymChunkKind._

import scala.collection.mutable.ArrayBuffer

/**
  *
  * @param isElementId Is this an implicit element ID synonym?
  *     In this case chunks contain the element ID.
  * @param isValueName Is this an implicit value name synonym?
  *     In this case chunks contain value name.
  * @param isDirect Direct or permuted synonym flag.
  * @param value Optional value name if this is a value synonym.
  */
class NCProbeSynonym(
    val isElementId: Boolean,
    val isValueName: Boolean,
    val isDirect: Boolean,
    val value: String = null
) extends ArrayBuffer[NCProbeSynonymChunk] with Ordered[NCProbeSynonym] {
    require((isElementId && !isValueName && value == null) || !isElementId)
    require((isValueName && value != null) || !isValueName)
    
    lazy val isTextOnly: Boolean = forall(_.kind == TEXT)
    lazy val regexChunks: Int = count(_.kind == REGEX)
    lazy val dslChunks: Int = count(_.kind == DSL)
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
            case DSL ⇒ 1
            case REGEX ⇒ 2
            case _ ⇒ throw new AssertionError(s"Unexpected kind: $kind")
        }

    /**
      *
      * @param toks
      * @return
      */
    def isMatch(toks: NCNlpSentenceTokenBuffer): Boolean = {
        require(toks != null)

        if (toks.length == length) {
            if (isTextOnly)
                toks.stemsHash == stemsHash && toks.stems == stems
            else
                toks.zip(this).sortBy(p ⇒ getSort(p._2.kind)).forall {
                    case (tok, chunk) ⇒
                        chunk.kind match {
                            case TEXT ⇒ chunk.wordStem == tok.stem
                            case REGEX ⇒ chunk.regex.matcher(tok.origText).matches() || chunk.regex.matcher(tok.normText).matches()
                            case DSL ⇒ throw new AssertionError()
                            case _ ⇒ throw new AssertionError()
                        }
                }
        }
        else
            false
    }

    /**
      *
      * @param tows
      * @return
      */
    def isMatch(tows: Seq[Either[NCToken, NCNlpSentenceToken]], req: NCRequest): Boolean = {
        require(tows != null)

        type Token = NCToken
        type Word = NCNlpSentenceToken
        type TokenOrWord = Either[Token, Word]

        if (tows.length == length && tows.count(_.isLeft) >= dslChunks)
            tows.zip(this).sortBy(p ⇒ getSort(p._2.kind)).forall {
                case (tow, chunk) ⇒
                    def get0[T](fromToken: Token ⇒ T, fromWord: Word ⇒ T): T =
                        if (tow.isLeft) fromToken(tow.left.get) else fromWord(tow.right.get)

                    chunk.kind match {
                        case TEXT ⇒
                            chunk.wordStem == get0(_.stem, _.stem)

                        case REGEX ⇒
                            val r = chunk.regex

                            r.matcher(get0(_.origText, _.origText)).matches() || r.matcher(get0(_.normText, _.normText)).matches()

                        case DSL ⇒
                            get0(t ⇒ chunk.dslPred.apply(t, NCDslContext(req = req))._2, _ ⇒ false)

                        case _ ⇒ throw new AssertionError()
                    }
            }
        else
            false
    }
    
    override def toString(): String = mkString(" ")
    
    // Orders synonyms from least to most significant.
    override def compare(that: NCProbeSynonym): Int = {
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
                        else // Both direct or indirect.
                            isTextOnly match {
                                case true if !that.isTextOnly ⇒ 1
                                case false if that.isTextOnly ⇒ -1
                                case true if that.isTextOnly ⇒ compareIsValueSynonym()
                                case _ ⇒
                                    val thisDynCnt = regexChunks + dslChunks
                                    val thatDynCnt = that.regexChunks + that.dslChunks
                                    
                                    // Less PoS/regex/DSL chunks means less uncertainty, i.e. larger weight.
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
                dslChunks == that.dslChunks &&
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
            dslChunks,
            isValueSynonym,
            isElementId,
            isValueName,
            value
        )
        
        state.map(p ⇒ if (p == null) 0 else p.hashCode()).foldLeft(0)((a, b) ⇒ 31 * a + b)
    }
}

object NCProbeSynonym {
    /**
      *
      * @param isElementId
      * @param isValueName
      * @param isDirect
      * @param value
      * @param chunks
      * @return
      */
    def apply(isElementId: Boolean, isValueName: Boolean, isDirect: Boolean, value: String, chunks: Seq[NCProbeSynonymChunk]): NCProbeSynonym = {
        var syn = new NCProbeSynonym(isElementId, isValueName, isDirect, value)
        
        syn ++= chunks
        
        syn
    }
}
