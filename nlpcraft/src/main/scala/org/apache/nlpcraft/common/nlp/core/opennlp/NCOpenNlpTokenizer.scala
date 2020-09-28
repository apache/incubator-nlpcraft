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

package org.apache.nlpcraft.common.nlp.core.opennlp

import io.opencensus.trace.Span
import opennlp.tools.tokenize.{Tokenizer, TokenizerME, TokenizerModel}
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.nlp.core.{NCNlpCoreToken, NCNlpTokenizer}
import org.apache.nlpcraft.common.extcfg.NCExternalConfigManager
import resource.managed
import org.apache.nlpcraft.common.extcfg.NCExternalConfigType.OPENNLP
import scala.language.{implicitConversions, postfixOps}

/**
  * OpenNLP tokenizer implementation.
  */
object NCOpenNlpTokenizer extends NCNlpTokenizer {
    private final val RESOURCE = "en-token.bin"

    @volatile private var tokenizer: Tokenizer = _

    /**
     *
     * @param parent Optional parent span.
     * @return
     */
    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ ⇒
        ackStarting()

        tokenizer = managed(NCExternalConfigManager.getStream(OPENNLP, RESOURCE)) acquireAndGet { in ⇒
            new TokenizerME(new TokenizerModel(in))
        }

        ackStarted()
    }

    /**
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span): Unit = startScopedSpan("start", parent) { _ ⇒
        ackStopping()

        tokenizer = null

        ackStopped()
    }

    /**
     *
     * @param sen Sentence
     * @return
     */
    override def tokenize(sen: String): Seq[NCNlpCoreToken] =
        this.synchronized {
            tokenizer.tokenizePos(sen)
        }
        .toSeq.map(s ⇒ NCNlpCoreToken(s.getCoveredText(sen).toString, s.getStart, s.getEnd, s.length()))
}
