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

package org.apache.nlpcraft.common.nlp.dict

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.nlp.dict.NCDictionaryType._

/**
 * English dictionary.
 */
object NCDictionaryManager extends NCService {
    // Mapping between dictionary type and its configuration file name.
    private val dictFiles: Map[NCDictionaryType, String] =
        Map(
            WORD -> "354984si.ngl",
            WORD_PROPER_NOUN -> "21986na.mes",
            WORD_ACRONYM -> "6213acro.nym",
            WORD_TOP_1000 -> "10001fr.equ",
            WORD_COMMON -> "74550com.mon"
        )

    // All dictionary types should be configured.
    require(NCDictionaryType.values.forall(dictFiles.contains))
    
    // Summary dictionary for all types.
    @volatile private var full: Set[String] = _
    @volatile private var dicts: Map[NCDictionaryType, Set[String]] = _
    
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent, "dicts" -> dictFiles.values.mkString(",")) { _ =>
        ackStarting()

        dicts = dictFiles.map(p => {
            val wordType = p._1
            val path = p._2

            def read(p: String => Boolean = _ => true) =
                U.mapResource(s"moby/$path", "iso-8859-1", logger, {
                    // Read single words only.
                    _.filter(!_.contains(" ")).
                        filter(p).
                        map(_.toLowerCase).
                        toSet
                })

            val words =
                wordType match {
                    // Skips proper nouns for this dictionary type.
                    case WORD_COMMON => read(_.head.isLower)
                    case _ => read()
                }

            wordType -> words
        })
        
        // Read summary dictionary.
        full = dicts.flatMap(_._2).toSet
        
        ackStarted()
    }

    /**
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()
        ackStopped()
    }

    /**
     * Checks if given lemma found in any dictionary.
     *
     * @param lemma Lemma to check for containment.
     */
    def contains(lemma: String): Boolean = full.contains(lemma)

    /**
      * Gets all word of dictionary.
      *
      * @param dictType Dictionary type.
      */
    def get(dictType: NCDictionaryType): Set[String] = dicts(dictType)
}
