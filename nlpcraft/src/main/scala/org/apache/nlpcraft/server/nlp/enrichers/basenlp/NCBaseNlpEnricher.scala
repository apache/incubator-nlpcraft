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

package org.apache.nlpcraft.server.nlp.enrichers.basenlp

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.nlp.pos.NCPennTreebank
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote, NCNlpSentenceToken}
import org.apache.nlpcraft.server.nlp.core.{NCNlpParser, NCNlpServerManager}
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnricher

import scala.collection._

/**
  * Base NLP enricher.
  */
object NCBaseNlpEnricher extends NCServerEnricher {
    // http://www.vidarholen.net/contents/interjections/
    private final val INTERJECTIONS =
        Set(
            "aah", "aaah", "aaaahh", "aha", "a-ha", "ahem",
            "ahh", "ahhh", "argh", "augh", "aww", "aw",
            "awww", "aww", "aw", "ohh", "ahh", "aw",
            "oh", "bah", "boo", "booh", "brr", "brrrr",
            "duh", "eek", "eeeek", "eep", "eh", "huh",
            "eh", "huh", "eyh", "eww", "ugh", "ewww",
            "gah", "gee", "grr", "grrrr", "hmm", "hm",
            "hmmmm", "humph", "harumph", "huh", "hurrah", "hooray",
            "huzzah", "ich", "yuck", "yak", "meh", "eh",
            "mhm", "mmhm", "uh-hu", "mm", "mmm", "mmh",
            "muahaha", "mwahaha", "bwahaha", "nah", "nuh-uh", "nuh-hu",
            "nuhuh", "oh", "ooh-la-la", "oh-lala", "ooh", "oooh",
            "oomph", "umph", "oops", "ow", "oww", "ouch",
            "oy", "oi", "oyh", "oy", "oyvay", "oy-vay",
            "pew", "pee-yew", "pff", "pffh", "pssh", "pfft",
            "phew", "psst", "sheesh", "jeez", "shh", "hush",
            "shush", "shoo", "tsk-tsk", "tut-tut", "uh-hu", "uhuh",
            "mhm", "uh-oh", "oh-oh", "uh-uh", "unh-unh", "uhh",
            "uhm", "err", "wee", "whee", "weee", "whoa",
            "wow", "yahoo", "yippie", "yay", "yeah", "yeeeeaah",
            "yee-haw", "yeehaw", "yoo-hoo", "yoohoo", "yuh-uh", "yuh-hu",
            "yuhuh", "yuck", "ich", "blech", "bleh", "zing",
            "ba-dum-tss", "badum-tish"
        ).map(_.toLowerCase)
    
    // The acronyms stand for (Left|Right) (Round|Square|Curly) Bracket.
    // http://www.cis.upenn.edu/~treebank/tokenization.html
    private final val BRACKETS = Map(
        "-LRB-" → "(",
        "-RRB-" → ")",
        "-LSB-" → "[",
        "-RSB-" → "]",
        "-LCB-" → "{",
        "-RCB-" → "}"
    )

    @volatile private var parser: NCNlpParser = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        parser = NCNlpServerManager.getParser
        
        super.start()
    }
    
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
    
    @throws[NCE]
    override def enrich(ns: NCNlpSentence, parent: Span = null) {
        startScopedSpan("enrich", parent, "srvReqId" → ns.srvReqId, "txt" → ns.text) { _ ⇒
            // This must be 1st enricher in the pipeline.
            assume(ns.isEmpty)
            
            var idx = 0
            
            for (word ← parser.parse(ns.text)) {
                val value = word.word.toLowerCase
                val origTxt = word.word
                val tok = NCNlpSentenceToken(idx)
            
                // Override interjection (UH) analysis.
                // (INTERJECTIONS and lemma are should be in lowercase.)
                val pos = if (INTERJECTIONS.contains(word.lemma)) "UH" else word.pos
                val seq = mutable.ArrayBuffer(
                    "lemma" → processBracket(word.lemma),
                    "index" → idx,
                    "pos" → pos,
                    "origText" → processBracket(origTxt),
                    "normText" → processBracket(value),
                    "charLength" → value.length,
                    "stem" → processBracket(word.stem),
                    "posDesc" → NCPennTreebank.description(pos).getOrElse(pos),
                    "start" → word.start,
                    "end" → word.end,
                    "quoted" → false,
                    "stopWord" → false,
                    "bracketed" → false,
                    "direct" → true
                )
                
                tok.add(NCNlpSentenceNote(Seq(idx), "nlpcraft:nlp", seq: _*))
        
                // Add new token to NLP sentence.
                ns += tok
                idx += 1
            }
        }
    }
    
    private def processBracket(s: String): String = BRACKETS.getOrElse(s.toUpperCase, s)
}
