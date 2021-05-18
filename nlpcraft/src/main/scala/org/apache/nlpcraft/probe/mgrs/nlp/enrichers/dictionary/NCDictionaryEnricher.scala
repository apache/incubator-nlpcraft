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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.dictionary

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.extcfg.NCExternalConfigManager
import org.apache.nlpcraft.common.extcfg.NCExternalConfigType.BADFILTER
import org.apache.nlpcraft.common.nlp._
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.nlp.dict._
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.probe.mgrs.NCProbeModel
import org.apache.nlpcraft.probe.mgrs.nlp.NCProbeEnricher

import java.io.Serializable
import scala.collection.Map

/**
  * Dictionary enricher.
  *
  * This enricher must be used after all enrichers which can manipulate 'quote' and 'stopword' notes of token.
  */
object NCDictionaryEnricher extends NCProbeEnricher {
    private final val RESOURCE = "swear_words.txt"

    @volatile private var swearWords: Set[String] = _

    /**
     *
     * @param parent Optional parent span.
     * @return
     */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()
        swearWords = U.readTextStream(NCExternalConfigManager.getStream(BADFILTER, RESOURCE), "UTF-8", logger).
            map(NCNlpCoreManager.stem).
            toSet

        ackStarted()
    }

    /**
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()
        ackStopped()
    }
    
    @throws[NCE]
    override def enrich(mdl: NCProbeModel, ns: NCNlpSentence, senMeta: Map[String, Serializable], parent: Span = null): Unit = {
        require(isStarted)

        startScopedSpan("enrich", parent,
            "srvReqId" -> ns.srvReqId,
            "mdlId" -> mdl.model.getId,
            "txt" -> ns.text) { _ =>
            ns.foreach(t => {
                // Dictionary.
                val nlpNote = t.getNlpNote

                ns.fixNote(
                    nlpNote,
                    // Single letters seems suspiciously.
                    "dict" -> (NCDictionaryManager.contains(t.lemma) && t.lemma.length > 1),
                    // English.
                    "english" -> t.origText.matches("""[\s\w\p{Punct}]+"""),
                    // Swearwords.
                    "swear" -> swearWords.contains(t.stem)
                )
            })
        }
    }
}