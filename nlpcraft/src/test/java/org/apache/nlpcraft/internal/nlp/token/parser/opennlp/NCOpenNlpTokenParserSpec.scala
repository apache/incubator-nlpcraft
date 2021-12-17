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

package org.apache.nlpcraft.internal.nlp.token.parser.opennlp

import org.apache.nlpcraft.NCRequest
import org.junit.jupiter.api.Test
import scala.jdk.CollectionConverters.ListHasAsScala
import java.util

class NCOpenNlpTokenParserSpec {
    @Test
    def test(): Unit = {
        val parser =
            new NCOpenNlpTokenParser(
                "opennlp/en-token.bin",
                "opennlp/en-pos-maxent.bin",
                "opennlp/en-lemmatizer.dict"
            )

        parser.start()

        val toks = parser.parse(
            new NCRequest {
                override def getUserId: String = null
                override def getRequestId: String = null
                override def getNormalizedText: String = getOriginalText.toLowerCase
                override def getOriginalText: String = "Test requests!"
                override def getReceiveTimestamp: Long = 0
                override def getUserAgent: String = null
                override def getRequestData: util.Map[String, AnyRef] = null
            }
        )

        assert(toks != null)
        assert(!toks.isEmpty)

        toks.asScala.foreach(t =>
            println(
                s"Text: ${t.getOriginalText}" +
                s", normalized: ${t.getNormalizedText}" +
                s", pos: ${t.getPos}" +
                s", stem: ${t.getStem}" +
                s", start: ${t.getStartCharIndex}" +
                s", end: ${t.getEndCharIndex}" +
                s", length: ${t.getLength}" +
                s", isStop: ${t.isStopWord}"
            )
        )
    }
}
