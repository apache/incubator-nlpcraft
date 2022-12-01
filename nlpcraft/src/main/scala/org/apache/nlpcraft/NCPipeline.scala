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
  *
  * An NLP pipeline is a container for the sequence of processing components that take the input text at the beginning
  * of the pipeline and produce the list of [[NCVariant variants]] at the end of the pipeline.
  * Schematically the pipeline looks like this:
  * <pre>
  *                                      +----------+        +-----------+         +--------+
  *   *=========*     +---------+    +---+-------+  |    +---+-------+   |     +---+-----+  |
  *   :  Text   : ->  |  Token  | -> | Token     |  | -> | Token      |  | ->  | Entity  |  | ----.
  *   :  Input  :     |  Parser |    | Enrichers |--+    | Validators |--+     | Parsers |--+      \
  *   *=========*     +---------+    +-----------+       +------------+        +---------+          \
  *                                                                                                  }
  *                       +--------+        +--------+        +-----------+        +----------+     /
  * *============*    +---+-----+  |    +---+-----+  |    +---+--------+  |    +---+-------+  |    /
  * :  Variants  : <- | Variant |  | <- | Entity  |  | <- | Entity     |  | <- | Entity    |  | <-'
  * :  List      :    | Filters |--+    | Mappers |--+    | Validators |--+    | Enrichers |--+
  * *============*    +----- ---+       +----- ---+       +------------+       +-----------+
  * </pre>
  *
  * Pipeline has the following components:
  *
  */
trait NCPipeline:
    /**
      * Get the token parser. One token parser is required for the pipeline.
      */
    def getTokenParser: NCTokenParser

    /**
      * Gets the list of entity parser. At least one entity parser is required.
      */
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
    def getVariantFilters: List[NCVariantFilter] = List.empty

    /**
      * Gets optional list of entity mappers. Can return an empty list but never `null`.
      */
    def getEntityMappers: List[NCEntityMapper] = List.empty
