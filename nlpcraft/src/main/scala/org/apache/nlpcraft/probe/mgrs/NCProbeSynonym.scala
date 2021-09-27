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

package org.apache.nlpcraft.probe.mgrs

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

    private lazy val hash =
        Seq(
            super.hashCode(),
            isTextOnly,
            regexChunks,
            idlChunks,
            isValueSynonym,
            isElementId,
            isValueName,
            value
        ).map(p => if (p == null) 0 else p.hashCode()).foldLeft(0)((a, b) => 31 * a + b)

    override def toString(): String = mkString(" ")

    // Orders synonyms from least to most significant.
    override def compare(that: NCProbeSynonym): Int = {
        require(hasIdl || sparse == that.sparse, s"Invalid comparing [this=$this, that=$that]")

        def compareIsValueSynonym(): Int =
            isValueSynonym match {
                case true if !that.isValueSynonym => 1
                case false if that.isValueSynonym => -1

                case _ => 0
            }

        if (that == null)
            1
        else
            isElementId match {
                case true if !that.isElementId => 1
                case false if that.isElementId => -1
                case true if that.isElementId => 0

                case _ => // None are element IDs.
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
                                case true if !that.isTextOnly => 1
                                case false if that.isTextOnly => -1
                                case true if that.isTextOnly => compareIsValueSynonym()
                                case _ =>
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

    override def equals(other: Any): Boolean = other match {
        case that: NCProbeSynonym =>
            isElementId == that.isElementId &&
            isTextOnly == that.isTextOnly &&
            regexChunks == that.regexChunks &&
            idlChunks == that.idlChunks &&
            isValueSynonym == that.isValueSynonym &&
            isValueName == that.isValueName &&
            value == that.value &&
            super.equals(that)
        case _ => false
    }

    override def hashCode(): Int = hash
}

object NCProbeSynonym {
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
