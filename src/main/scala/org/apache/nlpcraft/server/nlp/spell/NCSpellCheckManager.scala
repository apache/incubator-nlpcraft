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

package org.apache.nlpcraft.server.nlp.spell

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.server.json.NCJson

import scala.collection._

/**
  * Basic dictionary-based spell checker.
  */
object NCSpellCheckManager extends NCService {
    case class Record(correct: String, misspellings: Seq[String])
    
    private val dict: Map[String, String] = (
        for (rec ← NCJson.extractResource[List[Record]]("spell/dictionary.json", ignoreCase = true)) yield {
            for (v ← rec.misspellings) yield v → rec.correct
        })
        .flatten.toMap
    
    private def isWordUpper(s: String): Boolean = s.forall(_.isUpper)
    private def isHeadUpper(s: String): Boolean = s.head.isUpper
    private def split(s: String): Seq[String] = s.split(" ").filter(!_.isEmpty)
    private def processCase(s: String, sample: String): String =
        if (isWordUpper(sample))
            s.toUpperCase
        else if (isHeadUpper(sample))
            s.capitalize
        else
            s // Full lower case by default.
    
    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ ⇒
        super.start()
    }
    
    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
    
    /**
      * Gets correctly spelled word for a given one (if correction exists in predefined dictionary).
      * Returns the same word if it's correctly spelled or correction isn't available.
      *
      * NOTE: this method will retain the case of the 1st letter.
      *
      * @param in Word to check.
      */
    def check(in: String): String = dict.get(in.toLowerCase) match {
        case None ⇒ in
        case Some(out) ⇒
            val inSeq = split(in)
            val outSeq = split(out)
            
            if (inSeq.lengthCompare(outSeq.size) == 0)
                outSeq.zip(inSeq).map(p ⇒ processCase(p._1, p._2)).mkString(" ")
            else
                processCase(out, in)
    }
}
