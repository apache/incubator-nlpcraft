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

import edu.stanford.nlp.ling.CoreAnnotations.{SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.util.{ArrayCoreMap, CoreMap}
import io.opencensus.trace.Span
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.server.ignite.NCIgniteInstance
import org.apache.nlpcraft.server.nlp.core.{NCNlpParser, NCNlpWord}

import scala.collection.JavaConverters._
import scala.collection.Seq

/**
  * Stanford NLP parser implementation.
  */
object NCStanfordParser extends NCService with NCNlpParser with NCIgniteInstance {
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

    override def parse(normTxt: String, parent: Span = null): Seq[NCNlpWord] =
        startScopedSpan("enrich", parent, "normTxt" → normTxt) { _ ⇒
            val a: java.util.List[CoreMap] = NCStanfordCoreManager.annotate(normTxt).annotation().get(classOf[SentencesAnnotation])
    
            if (a == null)
                throw new NCE("Sentence annotation not found.")
    
            a.asScala.flatMap(p ⇒ {
                val value: java.util.List[CoreLabel] = p.asInstanceOf[ArrayCoreMap].get(classOf[TokensAnnotation])
    
                value.asScala
            }).map(t ⇒ {
                val normalWord = t.originalText().toLowerCase
    
                NCNlpWord(
                    word = t.originalText(),
                    normalWord = normalWord,
                    lemma = t.lemma().toLowerCase,
                    stem = NCNlpCoreManager.stemWord(normalWord).toString,
                    pos = t.tag(),
                    start = t.beginPosition,
                    end = t.endPosition(),
                    length = t.endPosition() - t.beginPosition()
                )
            })
    }
}