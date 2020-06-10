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

package org.apache.nlpcraft.server.nlp.synonym

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.server.json.NCJson
import org.apache.nlpcraft.server.nlp.synonym.NCSynonymType._
import org.apache.nlpcraft.server.nlp.wordnet.NCWordNetManager

/**
 * Built-in synonyms manager.
 */
object NCSynonymManager extends NCService {
    @volatile private var m: Map[String, Seq[Seq[String]]] = _

    @throws[NCE]
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        m = NCJson.extractResource[Map[String, List[List[String]]]]("synonyms/synonyms.json", ignoreCase = true).
            map(p ⇒ p._1.toUpperCase → p._2)

        val sets = m.flatMap(_._2).toSeq
        val dups = sets.flatten.filter(w ⇒ sets.count(_.contains(w)) > 1).distinct

        if (dups.nonEmpty)
            throw new NCE(s"Duplicated synonyms: ${dups.mkString(", ")}")

        m.foreach(p ⇒
            if (p._2.exists(_.isEmpty))
                throw new NCE(s"Empty synonyms sets found for POS: ${p._1}")
        )
    
        super.start()
    }
    
    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
    
    /**
     * Gets synonyms.
     *
     * @param lemma Lemma to get synonyms for.
     * @param pos Penn Treebank POS tag.
     */
    def get(lemma: String, pos: String): Map[NCSynonymType, Seq[Seq[String]]] = {
        val dlSyns: Seq[String] =
            m.get(pos) match {
                case Some(seq) ⇒
                    seq.find(_.contains(lemma)) match {
                        case Some(s) ⇒ s
                        case None ⇒ Seq.empty
                    }
                case None ⇒ Seq.empty
            }

        val wnSyns: Seq[Seq[String]] = NCWordNetManager.getSynonyms(lemma, pos)

        Map(
            NLPCRAFT → (if (dlSyns.isEmpty)Seq.empty else Seq(dlSyns)),
            WORDNET → wnSyns
        ).filter(_._2.nonEmpty)
    }
}
