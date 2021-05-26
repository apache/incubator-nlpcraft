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

package org.apache.nlpcraft.server.nlp.core

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.pool.NCThreadPoolManager
import org.apache.nlpcraft.common.{NCService, _}

import scala.collection.Seq
import scala.concurrent.ExecutionContext

/**
  * Server NLP manager.
  */
object NCNlpServerManager extends NCService {
    private implicit final val ec: ExecutionContext = NCThreadPoolManager.getSystemContext

    @volatile private var parser: NCNlpParser = _
    @volatile private var ners: Map[String, NCNlpNerEnricher] = _

    private object Config extends NCConfigurable {
        private final val TOKEN_PROVIDERS = Set("nlpcraft", "google", "stanford", "opennlp", "spacy")
        private final val prop = "nlpcraft.server.tokenProviders"

        def tokenProviders: Seq[String] = getStringList(prop)

        def support(name: String): Boolean = tokenProviders.contains(name)
    
        /**
          * 
          */
        def check(): Unit = {
            val unsupported = tokenProviders.filter(t => !TOKEN_PROVIDERS.contains(t))

            if (unsupported.nonEmpty)
                throw new NCE(s"Configuration property contains unsupported token providers [" +
                    s"name=$prop, " +
                    s"unsupported=${unsupported.mkString(", ")}" +
                s"]")
        }
    }

    Config.check()
    
    private val isStanfordNer: Boolean = Config.support("stanford")
    private val isGoogleNer: Boolean = Config.support("google")
    private val isOpenNer: Boolean = Config.support("opennlp")
    private val isSpacyNer: Boolean = Config.support("spacy")

    /**
     *
     * @param parent Optional parent span.
     * @return
     */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span =>
        ackStarting()

        addTags(span,
            "stanfordNer" -> isStanfordNer,
            "googleNer" -> isGoogleNer,
            "opennlpNer" -> isOpenNer,
            "spacyNer" -> isSpacyNer,
            "nlpEngine" -> NCNlpCoreManager.getEngine
        )
        
        parser =
            NCNlpCoreManager.getEngine match {
                case "stanford" => U.mkObject("org.apache.nlpcraft.server.nlp.core.stanford.NCStanfordParser")
                case "opennlp" => U.mkObject("org.apache.nlpcraft.server.nlp.core.opennlp.NCOpenNlpParser")

                case _ => throw new AssertionError(s"Unexpected NLP engine: ${NCNlpCoreManager.getEngine}")
            }

        parser.start()

        val m = collection.mutable.HashMap.empty[String, NCNlpNerEnricher]

        if (isGoogleNer) {
            m += "google" -> U.mkObject("org.apache.nlpcraft.server.nlp.core.google.NCGoogleNerEnricher")
            
            logger.info("Google Natural Language NER started.")
        }
        
        if (isOpenNer) {
            m += "opennlp" -> U.mkObject("org.apache.nlpcraft.server.nlp.core.opennlp.NCOpenNlpNerEnricher")
        
            logger.info("OpenNLP NER started.")
        }
        
        if (isStanfordNer) {
            m += "stanford" -> U.mkObject("org.apache.nlpcraft.server.nlp.core.stanford.NCStanfordNerEnricher")
    
            logger.info("Stanford CoreNLP NER started.")
        }

        if (isSpacyNer) {
            m += "spacy" -> U.mkObject("org.apache.nlpcraft.server.nlp.core.spacy.NCSpaCyNerEnricher")

            logger.info("spaCy NER started.")
        }

        ners = m.toMap

        // These component can be started independently.
        U.executeParallel(ners.values.map(ner => () => ner.start()).toSeq: _*)
    
        ackStarted()
    }

    /**
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        if (ners != null)
            ners.values.foreach(_.stop())

        if (parser != null && parser.isStarted)
            parser.stop()

        ackStopped()
    }

    /**
      *
      * @return
      */
    def getParser: NCNlpParser = parser

    /**
      *
      * @return
      */
    def getNers: Map[String, NCNlpNerEnricher] = ners
}
