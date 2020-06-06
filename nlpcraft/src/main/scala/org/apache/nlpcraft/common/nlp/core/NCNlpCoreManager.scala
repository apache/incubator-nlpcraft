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

package org.apache.nlpcraft.common.nlp.core

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.{NCE, NCService, _}


/**
 *  NLP core manager.
 */
object NCNlpCoreManager extends NCService {
    private final val SUPPORTED_NLP_ENGINES = Seq("opennlp", "stanford")
    
    private object Config extends NCConfigurable {
        def engine: String = getString("nlpcraft.nlpEngine")
    }
    
    @volatile private var tokenizer: NCNlpTokenizer = _

    /**
      *
      * @return
      */
    def getEngine: String = Config.engine

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span ⇒
        // NOTE: DO NOT confuse this with token providers.
        if (!SUPPORTED_NLP_ENGINES.contains(Config.engine))
            throw new NCE(s"Unsupported NLP engine: ${Config.engine}")
        
        addTags(span, "nlpEngine" → Config.engine)
      
        logger.info(s"NLP engine configured: ${Config.engine}")
      
        tokenizer =
            Config.engine match {
                case "stanford" ⇒ U.mkObject("org.apache.nlpcraft.common.nlp.core.stanford.NCStanfordTokenizer")
                case "opennlp" ⇒ U.mkObject("org.apache.nlpcraft.common.nlp.core.opennlp.NCOpenNlpTokenizer")
        
                case _ ⇒ throw new AssertionError(s"Unexpected engine: ${Config.engine}")
            }
      
        super.start()
    }
    
    override def stop(parent: Span): Unit = startScopedSpan("stop", parent)(_ ⇒ super.stop())
    
    /**
      * Stems given word or a sequence of words which will be tokenized before.
      *
      * @param words One or more words to stemmatize.
      * @return Sentence with stemmed words.
      */
    def stem(words: String): String = {
        val seq = tokenizer.tokenize(words).map(p ⇒ p → NCNlpPorterStemmer.stem(p.token))

        seq.zipWithIndex.map { case ((tok, stem), idx) ⇒
            idx match {
                case 0 ⇒ stem
                // Suppose there aren't multiple spaces.
                case _ ⇒ if (seq(idx - 1)._1.to + 1 < tok.from) s" $stem"
                else stem
            }
        }.mkString(" ")
    }

    /**
      * Stems given word.
      *
      * @param word Word to stemmatize.
      * @return Stemmed word.
      */
    def stemWord(word: String): String = NCNlpPorterStemmer.stem(word)

    /**
      * Tokenizes given sentence.
      *
      * @param sen Sentence text.
      * @return Tokens.
      */
    def tokenize(sen: String): Seq[NCNlpCoreToken] = tokenizer.tokenize(sen)
}