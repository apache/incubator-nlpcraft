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

package org.apache.nlpcraft.nlp.parsers.impl

import org.apache.nlpcraft.NCToken
import org.apache.nlpcraft.nlp.parsers.impl.NCSemanticChunkKind.*

import java.util.regex.Pattern

/**
  *
  */
private[impl] enum NCSemanticChunkKind:
    case TEXT, REGEX

/**
  *
  * @param kind Kind of synonym chunk.
  * @param text Original text.
  * @param stem Optional stem for a single word synonyms.
  * @param regex Optional regex expression to match on.
  */
private[impl] case class NCSemanticSynonymChunk(
    kind: NCSemanticChunkKind, text: String, stem: String = null, regex: Pattern = null
):
    require(text != null && kind != null)
    require(stem != null ^ regex != null)

    val isText: Boolean = stem != null

    override def toString = s"($text|$kind)"

/**
  *
  * @param chunks
  * @param value
  */
private[impl] case class NCSemanticSynonym(chunks: Seq[NCSemanticSynonymChunk], value: String = null) extends Comparable[NCSemanticSynonym]:
    require(chunks != null)
    require(chunks.nonEmpty)

    final val size = chunks.size
    private final val regexCount = size - chunks.count(_.kind == TEXT)
    final val isText = regexCount == 0
    final lazy val stem = if isText then chunks.map(_.stem).mkString(" ") else null

    override def compareTo(o: NCSemanticSynonym): Int = Integer.compare(regexCount, o.regexCount)
