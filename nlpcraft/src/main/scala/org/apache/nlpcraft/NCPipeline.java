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

package org.apache.nlpcraft;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * NLP processing pipeline for the input request. Pipeline is associated with the model.
 * <p>
 * An NLP pipeline is a container for various processing components that take the input text at the beginning of the
 * pipeline and produce the list of {@link NCEntity entities} at the end of the pipeline.
 * Schematically the pipeline looks like this:
 * <pre>
 *                                   +----------+        +-----------+
 * *=========*    +---------+    +---+-------+  |    +---+-------+   |
 * :  Text   : -> |  Token  | -> | Token     |  | -> | Token      |  | ----.
 * :  Input  :    |  Parser |    | Enrichers |--+    | Validators |--+      \
 * *=========*    +---------+    +-----------+       +------------+          \
 *                                                                            }
 *                    +-----------+        +----------+        +--------+    /
 * *=========*    +---+--------+  |    +---+-------+  |    +---+-----+  |   /
 * :  Entity : <- | Entity     |  | <- | Entity    |  | <- | Entity  |  | <-
 * :  List   :    | Validators |--+    | Enrichers |--+    | Parsers |--+
 * *=========*    +------------+       +-----------+       +---------+
 * </pre>
 * <p>
 * Pipeline has the following components:
 * <ul>
 *     <li>
 *         {@link NCTokenParser} is responsible for taking the input text and tokenize it into a list of
 *         {@link NCToken}. This process is called tokenization, i.e. the process of demarcating and
 *         classifying sections of a string of input characters. There's only one token parser for the pipeline.
 *     </li>
 *     <li>
 *         After the initial list of token is
 *     </li>
 * </ul>
 *
 *
 */
public interface NCPipeline {
    /**
     *
     * @return
     */
    NCTokenParser getTokenParser();

    /**
     *
     * @return
     */
    List<NCEntityParser> getEntityParsers();

    /**
     *
     * @return
     */
    default List<NCTokenEnricher> getTokenEnrichers() {
        return Collections.emptyList();
    }

    /**
     *
     * @return
     */
    default List<NCEntityEnricher> getEntityEnrichers() {
        return Collections.emptyList();
    }

    /**
     *
     * @return
     */
    default List<NCTokenValidator> getTokenValidators() {
        return Collections.emptyList();
    }

    /**
     *
     * @return
     */
    default List<NCEntityValidator> getEntityValidators() {
        return Collections.emptyList();
    }

    /**
     *
     * @return
     */
    default Optional<NCVariantFilter> getVariantFilter() {
        return Optional.empty();
    }
}