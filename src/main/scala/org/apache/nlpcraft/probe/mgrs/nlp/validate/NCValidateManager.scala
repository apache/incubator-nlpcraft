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

package org.apache.nlpcraft.probe.mgrs.nlp.validate

import com.typesafe.scalalogging.LazyLogging
import io.opencensus.trace.Span
import org.apache.tika.langdetect.OptimaizeLangDetector
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.nlp.NCNlpSentence
import org.apache.nlpcraft.probe.mgrs.NCModelDecorator

/**
 * Probe pre/post enrichment validator.
 */
object NCValidateManager extends NCService with LazyLogging {
    // Create new language finder singleton.
    @volatile private var langFinder: OptimaizeLangDetector = _
    
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        langFinder = new OptimaizeLangDetector()
        
        // Initialize language finder.
        langFinder.loadModels()
        
        super.start()
    }
    
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
    
    /**
     *
     * @param mdl Model decorator.
     * @param ns Sentence to validate.
     * @param parent Parent tracing span.
     */
    @throws[NCValidateException]
    def preValidate(mdl: NCModelDecorator, ns: NCNlpSentence, parent: Span = null): Unit = 
        startScopedSpan("validate", parent,
            "srvReqId" → ns.srvReqId,
            "txt" → ns.text,
            "modelId" → mdl.model.getId) { _ ⇒
            val model = mdl.model
            
            if (!model.isNotLatinCharsetAllowed && !ns.text.matches("""[\s\w\p{Punct}]+"""))
                throw NCValidateException("ALLOW_NON_LATIN_CHARSET")
            if (!model.isNonEnglishAllowed && !langFinder.detect(ns.text).isLanguage("en"))
                throw NCValidateException("ALLOW_NON_ENGLISH")
            if (!model.isNoNounsAllowed && !ns.exists(_.pos.startsWith("n")))
                throw NCValidateException("ALLOW_NO_NOUNS")
            if (model.getMinWords > ns.map(_.wordLength).sum)
                throw NCValidateException("MIN_WORDS")
            if (ns.size > model.getMaxTokens)
                throw NCValidateException("MAX_TOKENS")
        }
    
    /**
     *
     * @param mdl Model decorator.
     * @param ns Sentence to validate.
     * @param parent Optional parent span.
     */
    @throws[NCValidateException]
    def postValidate(mdl: NCModelDecorator, ns: NCNlpSentence, parent: Span = null): Unit =
        startScopedSpan("validate", parent,
            "srvReqId" → ns.srvReqId,
            "txt" → ns.text,
            "modelId" → mdl.model.getId) { _ ⇒
            val types = ns.flatten.filter(!_.isNlp).map(_.noteType).distinct
            val overlapNotes = ns.map(tkn ⇒ types.flatMap(tp ⇒ tkn.getNotes(tp))).filter(_.size > 1).flatten
            val model = mdl.model
            
            if (overlapNotes.nonEmpty)
                throw NCValidateException("OVERLAP_NOTES")
            if (!model.isNoUserTokensAllowed && !ns.exists(_.exists(!_.noteType.startsWith("nlpcraft:"))))
                throw NCValidateException("ALLOW_NO_USER_TOKENS")
            if (!model.isSwearWordsAllowed && ns.exists(_.getNlpValueOpt[Boolean]("swear").getOrElse(false)))
                throw NCValidateException("ALLOW_SWEAR_WORDS")
            if (model.getMinNonStopwords > ns.count(!_.isStopWord))
                throw NCValidateException("MIN_NON_STOPWORDS")
            if (model.getMinTokens > ns.size)
                throw NCValidateException("MIN_TOKENS")
            if (model.getMaxUnknownWords < ns.count(t ⇒ t.isNlp && !t.isSynthetic && !t.isKnownWord))
                throw NCValidateException("MAX_UNKNOWN_WORDS")
            if (model.getMaxSuspiciousWords < ns.count(_.getNlpValueOpt[Boolean]("suspNoun").getOrElse(false)))
                throw NCValidateException("MAX_SUSPICIOUS_WORDS")
            if (model.getMaxFreeWords < ns.count(_.isNlp))
                throw NCValidateException("MAX_FREE_WORDS")
        }
}

