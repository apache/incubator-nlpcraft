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

import java.io.BufferedInputStream

import io.opencensus.trace.Span
import opennlp.tools.tokenize.{Tokenizer, TokenizerME, TokenizerModel}
import org.apache.nlpcraft.common.nlp.core.{NCNlpCoreToken, NCNlpTokenizer}
import org.apache.nlpcraft.common.{NCService, _}
import resource.managed

import scala.language.{implicitConversions, postfixOps}

/**
  * OpenNLP tokenizer implementation.
  */
object NCOpenNlpTokenizer extends NCService with NCNlpTokenizer {
    private final val MODEL_PATH = "opennlp/en-token.bin"
    
    @volatile private var tokenizer: Tokenizer = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent, "model" → MODEL_PATH) { _ ⇒
        tokenizer = managed(new BufferedInputStream(U.getStream(MODEL_PATH))) acquireAndGet { in ⇒
            new TokenizerME(new TokenizerModel(in))
        }
     
        super.start()
    }
    
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
    
    override def tokenize(sen: String): Seq[NCNlpCoreToken] =
        this.synchronized {
            tokenizer.tokenizePos(sen)
        }
        .toSeq.map(s ⇒ NCNlpCoreToken(s.getCoveredText(sen).toString, s.getStart, s.getEnd, s.length()))
}
