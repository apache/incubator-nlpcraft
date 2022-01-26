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

package org.apache.nlpcraft.nlp.util.opennlp

import org.apache.nlpcraft.NCModelConfig
import org.apache.nlpcraft.nlp.token.parser.opennlp.NCOpenNLPTokenParser
import org.apache.nlpcraft.nlp.util.*

/**
  *
  */
final val CFG = new NCModelConfig("testId", "test", "1.0", "Test description", "Test origin")

/**
  *
  */
final val EN_PIPELINE = NCTestPipeline(
    new NCOpenNLPTokenParser(
        "opennlp/en-token.bin",
        "opennlp/en-pos-maxent.bin",
        "opennlp/en-lemmatizer.dict"
    )
)