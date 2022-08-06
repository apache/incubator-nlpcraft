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

package org.apache.nlpcraft.nlp.util

import org.apache.nlpcraft.NCModelConfig
import org.apache.nlpcraft.internal.util.NCResourceReader as R
import org.apache.nlpcraft.nlp.token.enricher.*
import org.apache.nlpcraft.nlp.token.parser.NCOpenNLPTokenParser
import org.apache.nlpcraft.nlp.util.*

final val CFG = new NCModelConfig("testId", "test", "1.0", description = Some("Test description"), origin = Some("Test origin"))
final val EN_TOK_PARSER = new NCOpenNLPTokenParser(R.getPath("opennlp/en-token.bin"))
final val EN_TOK_STOP_ENRICHER = new NCEnStopWordsTokenEnricher
final val EN_TOK_LEMMA_POS_ENRICHER =
    new NCOpenNLPLemmaPosTokenEnricher(R.getPath("opennlp/en-pos-maxent.bin"), R.getPath("opennlp/en-lemmatizer.dict"))
final def mkEnPipeline = NCTestPipeline(EN_TOK_PARSER)
