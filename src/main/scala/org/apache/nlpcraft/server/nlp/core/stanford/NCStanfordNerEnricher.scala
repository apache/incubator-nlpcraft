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

package org.apache.nlpcraft.server.nlp.core.stanford

import edu.stanford.nlp.ling.CoreAnnotations.NormalizedNamedEntityTagAnnotation
import io.opencensus.trace.Span
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote}
import org.apache.nlpcraft.server.ignite.NCIgniteInstance
import org.apache.nlpcraft.server.nlp.core.NCNlpNerEnricher

import scala.collection.JavaConverters._

/**
  * Stanford NLP NER enricher.
  */
object NCStanfordNerEnricher extends NCService with NCNlpNerEnricher with NCIgniteInstance {
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span ⇒
        // Should be started even if another NLP engine configured.
        if (!NCStanfordCoreManager.isStarted)
            NCStanfordCoreManager.start(span)

        super.start()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { span ⇒
        if (NCStanfordCoreManager.isStarted)
            NCStanfordCoreManager.stop(span)
    
        super.stop()
    }
    
    /**
     *
     * @param ns
     * @param enabledBuiltInToks Set of enabled built-in token IDs.
     */
    override def enrich(ns: NCNlpSentence, enabledBuiltInToks: Set[String], parent: Span = null): Unit =
        startScopedSpan("enrich", parent, "srvReqId" → ns.srvReqId, "txt" → ns.text) { _ ⇒
            NCStanfordCoreManager.
                annotate(ns.text).
                entityMentions().asScala.
                filter(e ⇒ enabledBuiltInToks.contains(e.entityType().toLowerCase)).
                foreach(e ⇒ {
                    val offsets = e.charOffsets()
    
                    val t1 = ns.find(_.startCharIndex == offsets.first)
                    val t2 = ns.find(_.endCharIndex == offsets.second)
    
                    if (t1.nonEmpty && t2.nonEmpty) {
                        val buf = collection.mutable.ArrayBuffer.empty[(String, Any)]
    
                        val nne = e.coreMap().get(classOf[NormalizedNamedEntityTagAnnotation])
    
                        if (nne != null)
                            buf += "nne" → nne
    
                        val conf = e.entityTypeConfidences()
    
                        // Key ignored because it can be category with higher level (`location` for type `country`)
                        if (conf.size() == 1)
                            buf += "confidence" → conf.asScala.head._2
    
                        val typ = e.entityType().toLowerCase
    
                        val i1 = t1.get.startCharIndex
                        val i2 = t2.get.endCharIndex
                        val toks = ns.filter(t ⇒ t.startCharIndex >= i1 && t.endCharIndex <= i2)
    
                        val note = NCNlpSentenceNote(toks.map(_.index), s"stanford:$typ", buf: _*)
    
                        toks.foreach(_.add(note))
                    }
                })
        }
}