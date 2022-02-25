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

package org.apache.nlpcraft.examples.lightswitch

import org.apache.nlpcraft.*
import org.apache.nlpcraft.examples.lightswitch.nlp.token.enricher.NCRuStopWordsTokenEnricher
import org.apache.nlpcraft.examples.lightswitch.nlp.token.parser.NCRuTokenParser
import org.apache.nlpcraft.internal.util.NCResourceReader
import org.apache.nlpcraft.nlp.token.enricher.en.*
import org.apache.nlpcraft.nlp.token.parser.opennlp.NCOpenNLPTokenParser

import java.util
import java.util.*
import scala.jdk.CollectionConverters.*

/**
  * Default RU implementation based on Open Nlp token parser, and stopword token enricher.
  * Also at least one entity parser must be defined. */
class NCRuPipeline(parser: NCEntityParser) extends NCModelPipeline:
    override val getTokenParser: NCTokenParser = new NCRuTokenParser()
    override val getEntityParsers: util.List[NCEntityParser] = Seq(parser).asJava
    override val getTokenEnrichers: util.List[NCTokenEnricher] = Seq(new NCRuStopWordsTokenEnricher()).asJava
