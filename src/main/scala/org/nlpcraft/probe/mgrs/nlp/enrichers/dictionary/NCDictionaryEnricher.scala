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

package org.nlpcraft.probe.mgrs.nlp.enrichers.dictionary

import java.io.Serializable

import io.opencensus.trace.Span
import org.nlpcraft.common.nlp._
import org.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.nlpcraft.common.nlp.dict._
import org.nlpcraft.common.{NCService, _}
import org.nlpcraft.probe.mgrs.NCModelDecorator
import org.nlpcraft.probe.mgrs.nlp.NCProbeEnricher

import scala.collection.Map

/**
  * Dictionary enricher.
  *
  * This enricher must be used after all enrichers which can manipulate 'quote' and 'stopword' notes of token.
  */
object NCDictionaryEnricher extends NCProbeEnricher {
    @volatile private var swearWords: Set[String] = _

    /**
      * Starts this component.
      */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        swearWords =
            U.readTextResource(s"badfilter/swear_words.txt", "UTF-8", logger).
                map(NCNlpCoreManager.stem).
                toSet

        super.start()
    }
    
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
    
    @throws[NCE]
    override def enrich(mdl: NCModelDecorator, ns: NCNlpSentence, senMeta: Map[String, Serializable], parent: Span = null): Boolean =
        startScopedSpan("enrich", parent,
            "srvReqId" → ns.srvReqId,
            "modelId" → mdl.model.getId,
            "txt" → ns.text) { _ ⇒
            val res = ns.exists(!_.getNlpNote.contains("dict"))

            ns.foreach(t ⇒ {
                // Dictionary.
                val nlpNote = t.getNlpNote
    
                // Single letters seems suspiciously.
                nlpNote += "dict" → (NCDictionaryManager.contains(t.lemma) && t.lemma.length > 1)
    
                // English.
                nlpNote += "english" → t.origText.matches("""[\s\w\p{Punct}]+""")
    
                // Swearwords.
                nlpNote += "swear" → swearWords.contains(t.stem)
            })

            res
        }
}