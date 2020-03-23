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

package org.apache.nlpcraft.server.nlp.enrichers.quote

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.nlp._
import org.apache.nlpcraft.common.nlp.pos.NCPennTreebank
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher._

import scala.collection._

/**
 * Quote enricher.
 */
object NCQuoteEnricher extends NCServerEnricher {
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        super.start()
    }
    
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
    
    @throws[NCE]
    override def enrich(ns: NCNlpSentence, parent: Span = null) {
        startScopedSpan("enrich", parent, "srvReqId" → ns.srvReqId, "txt" → ns.text) { _ ⇒
            // Clone input sentence.
            val copy = ns.clone()
    
            // Clear the tokens in the input sentence.
            ns.clear()
    
            var inQuotes = false
            var buf = mutable.IndexedSeq.empty[NCNlpSentenceToken]
            var tokIdx = 0
    
            var quotesToks = copy.filter(isQuote)
    
            val size = quotesToks.size
    
            // Skips last quote if there is odd quotes quantity.
            if (size % 2 != 0)
                quotesToks = quotesToks.take(size - 1)
    
            for (tok ← copy)
                if (quotesToks.contains(tok))
                    // Assuming a closing quote.
                    if (inQuotes) {
                        // Add closing quote to the buffer.
                        buf :+= tok
    
                        // Skip empty quoted content.
                        if (buf.size > 2) {
                            // Retain indexes in the original text based on
                            // opening and closing quotes.
                            val startIdx = buf.head.startCharIndex
                            val endIdx = buf.last.endCharIndex
    
                            // Trim the buffer from opening and closing quotes.
                            val trimBuf = buf.tail.dropRight(1)
    
                            // NOTE: we drop (ignore) 'ne' and 'nne' notes.
                            val nlpNote = NCNlpSentenceNote(
                                Seq(tokIdx),
                                "nlpcraft:nlp",
                                "index" → tokIdx,
                                "pos" → NCPennTreebank.SYNTH_POS,
                                "posDesc" → NCPennTreebank.SYNTH_POS_DESC,
                                "lemma" → mkSumString(trimBuf, (t: NCNlpSentenceToken) ⇒ t.lemma),
                                "origText" → mkSumString(trimBuf, (t: NCNlpSentenceToken) ⇒ t.origText),
                                "normText" → mkSumString(trimBuf, (t: NCNlpSentenceToken) ⇒ t.normText),
                                "stem" → mkSumString(trimBuf, (t: NCNlpSentenceToken) ⇒ t.stem),
                                "start" → startIdx,
                                "end" → endIdx,
                                "length" → (trimBuf.map(_.wordLength).sum + trimBuf.length - 1),
                                "quoted" → true,
                                "stopWord" → false,
                                "bracketed" → false,
                                "direct" → trimBuf.forall(_.isDirect)
                            )
    
                            // New compound token (made out of buffer's content).
                            val newTok = NCNlpSentenceToken(tokIdx)
    
                            newTok.add(nlpNote)
    
                            // Add new compound token to the sentence.
                            ns += newTok
    
                            tokIdx += 1
                        }
    
                        // Mark as NOT in quotes.
                        inQuotes = false
    
                        // Clear the buffer.
                        buf = mutable.IndexedSeq.empty[NCNlpSentenceToken]
                    }
                    // Assume an opening quote.
                    else {
                        assume(buf.isEmpty)
    
                        // Mark as in quotes.
                        inQuotes = true
    
                        // Add beginning quote to the new buffer.
                        buf :+= tok
                    }
                else {
                    if (inQuotes)
                        buf :+= tok
                    else {
                        val newTok = tok.clone(tokIdx)
    
                        // Override indexes from CoreNLP enricher.
                        val nlpNote = newTok.getNlpNote
    
                        // NLP is single note.
                        newTok.remove(nlpNote)
                        newTok.add(nlpNote.clone(Seq(tokIdx), Seq(tokIdx), "index" → tokIdx, "quoted" → false))
    
                        // It shouldn't care about other kind of notes because
                        // CORE and QUOTE enrichers are first in processing.
                        ns += newTok
    
                        tokIdx += 1
                    }
                }
        }
    }
}
