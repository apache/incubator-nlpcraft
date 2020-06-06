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

import java.util.regex.Pattern

import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.probe.mgrs.NCSynonymChunkKind._

/**
 *
 * @param alias Optional alias for this chunk from token DSL.
 * @param kind Kind of synonym chunk.
 * @param origText Original text.
 * @param wordStem Optional stem for a single word synonyms.
 * @param posTag Optional PoS tag to match on.
 * @param regex Optional regex expression to match on.
 * @param dslPred Optional DSL expression to match on.
 */
case class NCSynonymChunk(
    alias: String = null, // Not-null only for kind == DSL.
    kind: NCSynonymChunkKind,
    origText: String,
    wordStem: String = null, // Only for kind == TEXT.
    posTag: String = null,
    regex: Pattern = null,
    dslPred: java.util.function.Function[NCToken, java.lang.Boolean] = null
) {
    override def toString = s"($origText|$kind)"
}
