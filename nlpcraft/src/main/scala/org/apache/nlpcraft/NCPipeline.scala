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

package org.apache.nlpcraft

/**
  * NLP processing pipeline for the input request. Pipeline is associated with the model.
  * <p>
  * An NLP pipeline is a container for various processing components that take the input text at the beginning of the
  * pipeline and produce the list of {@link NCEntity entities} at the end of the pipeline.
  * Schematically the pipeline looks like this:
  * <pre>
  * +----------+        +-----------+
  * *=========*    +---------+    +---+-------+  |    +---+-------+   |
  * :  Text   : -> |  Token  | -> | Token     |  | -> | Token      |  | ----.
  * :  Input  :    |  Parser |    | Enrichers |--+    | Validators |--+      \
  * *=========*    +---------+    +-----------+       +------------+          \
  * }
  * +-----------+        +----------+        +--------+    /
  * *=========*    +---+--------+  |    +---+-------+  |    +---+-----+  |   /
  * :  Entity : <- | Entity     |  | <- | Entity    |  | <- | Entity  |  | <-
  * :  List   :    | Validators |--+    | Enrichers |--+    | Parsers |--+
  * *=========*    +------------+       +-----------+       +---------+
  * </pre>
  * <p>
  * Pipeline has the following components:
  * <ul>
  * <li>
  * {@link NCTokenParser} is responsible for taking the input text and tokenize it into a list of
  * {@link NCToken
  * }. This process is called tokenization, i.e. the process of demarcating and
  * classifying sections of a string of input characters. There's only one token parser for the pipeline.
  * </li>
  * <li>
  * After the initial list of token is
  * </li>
  * </ul>
  *
  */
trait NCPipeline:
    /**
      *
      * @return */
    def getTokenParser: NCTokenParser

    /**
      * Gets the list of entity parser. At least one entity parser is required.
      *
      * @return */
    def getEntityParsers: List[NCEntityParser]

    /**
      *
      */
    def getTokenEnrichers: List[NCTokenEnricher] = List.empty

    /**
      *
      */
    def getEntityEnrichers: List[NCEntityEnricher] = List.empty

    /**
      *
      */
    def getTokenValidators: List[NCTokenValidator] = List.empty

    /**
      *
      */
    def getEntityValidators: List[NCEntityValidator] = List.empty

    /**
      *
      */
    def getVariantFilter: Option[NCVariantFilter] = None

    /**
      * Gets optional list of entity mappers.
      *
      * @return Optional list of entity mappers. Can be empty but never `null`.
      */
    def getEntityMappers: List[NCEntityMapper] = List.empty
