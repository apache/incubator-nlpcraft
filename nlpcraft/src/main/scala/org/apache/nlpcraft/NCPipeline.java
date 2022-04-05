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
 *                                               ,---------.        ,----------.        ,-------.
 *   o/         *=========*    ,--------.    ,---'-------. |    ,---'--------. |    ,---'-----. |
 *  /|     -&gt;   :  Text   : -&gt; | Token  | -&gt; | Token     | | -&gt; | Token      | | -&gt; | Entity  | | -------.
 *  / \         :  Input  :    | Parser |    | Enrichers |-'    | Validators |-'    | Parsers |-'        |
 *              *=========*    `--------'    `-----------'      `------------'      `---------'          |
 *                                                                                                       |
 *                                                                 ,----------.        ,---------.       |
 *              *============*    ,---------.    ,--------.    ,---'--------. |    ,---'-------. |       |
 * Intent   &lt;-  :  Entity    : &lt;- | Variant | &lt;- | Entity | &lt;- | Entity     | | &lt;- | Entity    | | &lt;-----'
 * Matching     :  Variants  :    | Filter  |    | Mapper |    | Validators |-'    | Enrichers |-'
 *              *============*    `---------'    `--------'    `------------'      `-----------'
 * </pre>
 * <p>
 * Pipeline has the following components:
 * <ul>
 *     <li>
 *         <p>
 *              {@link NCTokenParser} is responsible for taking the input text and tokenize it into a list of
 *              {@link NCToken}. This process is called tokenization, i.e. the process of demarcating and
 *              classifying sections of a string of input characters. There's only one token parser for the pipeline
 *              and token parser is mandatory part of the pipeline.
 *         </p>
 *     </li>
 *     <li>
 *         <p>
 *              After the initial list of token is created one or more {@link NCTokenEnricher} are called to enrich
 *              each token. Enrichment consists of adding properties to {@link NCToken} instance. Example of enrichers
 *              could be stopword detection, geo-location detection, POS tagging, etc. Token enrichers are optional and
 *              by default the list of token enrichers is empty.
 *         </p>
 *     </li>
 *     <li>
 *         <p>
 *              After all tokens are enriched the {@link NCTokenValidator} are called. Token validators provide an opportunity
 *              to reject input request at the early stage of token processing. Some of the examples of token validation
 *              can be curse words filtration, privacy checks, adult content blocking, etc. Token validators are optional
 *              and by default the list of token validators is empty.
 *         </p>
 *     </li>
 *     <li>
 *         <p>
 *              Once tokens are parsed, enriched and validated they are passed into one or more {@link NCEntityParser}.
 *              Entity parser is responsible for taking a list of tokens and converting them into a list of entity, where
 *              an entity is typically has a consistent semantic meaning and usually denotes a real-world object, such as
 *              persons, locations, number, date and time, organizations, products, etc. - where such objects can be
 *              abstract or have a physical existence.
 *         </p>
 *         <p>
 *              At least one entity parser must be defined in the pipeline. If multiple parsers are defined their collective
 *              output is combined for further processing. Note that it is possible and in many cases is required that a single
 *              list of tokens can be converted to the list of entities in more than one way that is called {@link NCVariant}.
 *              Having multiple entity parsers allows to compartmentalize this logic.
 *         </p>
 *     </li>
 *     <li>
 *         <p>
 *              Just like with tokens, once entity list (or lists) are obtained, they go through {@link NCEntityEnricher}.
 *              Entity enrichment consists of adding properties to {@link NCEntity} instance. Entity enrichers are optional
 *              and by default the list of entity enrichers is empty. Examples of the entity enrichment are always application
 *              specific since they are dealing with application specific entities: it could be access tokens, special
 *              markers, etc.
 *         </p>
 *     </li>
 *     <li>
 *         <p>
 *              After entity enrichment is done the list(s) of entities go through {@link NCEntityValidator}. Just like
 *              token validators, entity validators allow to reject input request at the level of entity processing.
 *              Entity validators are optional and by default the list of entity validators is empty. Examples of the entity
 *              validators can be security checks, authentication and authorization, ACL checks, etc.
 *         </p>
 *     </li>
 *     <li>
 *         <p>
 *             After entities have been validated they go through the list of optional entity mappers. Entity mapper's primary
 *             role is to combine multiple entities into a new one without a need to modify entity parser or enrichers. More
 *             than one entity mapper can be chained together to form a transformation sub-pipeline. For example,
 *             if you have individual entities for pizza, pizza size and list of toppings, the entity mapper could combine all these
 *             individual entities into a new single pizza order entity that would then be used in intent matching in much easier way.
 *         </p>
 *     </li>
 *     <li>
 *         <p>
 *              Finally, there is an optional filter for {@link NCVariant} instances before they get into intent matching. This
 *              filter allows to filter out unnecessary (or spurious) parsing variants based on application-specific logic.
 *              Note that amount of parsing variants directly correlates to the overall performance of intent matching.
 *         </p>
 *     </li>
 * </ul>
 *
 * @see NCEntity
 * @see NCToken
 * @see NCTokenParser
 * @see NCTokenEnricher
 * @see NCTokenValidator
 * @see NCEntityParser
 * @see NCEntityEnricher
 * @see NCEntityValidator
 */
public interface NCPipeline {
    /**
     * Gets mandatory token parser.
     *
     * @return Token parser.
     */
    NCTokenParser getTokenParser();

    /**
     * TODO: can be empty.
     * Gets the list of entity parser. At least one entity parser is required.
     *
     * @return List of entity parser. List should contain at least one entity parser.
     */
    default List<NCEntityParser> getEntityParsers() {
        return Collections.emptyList();
    }

    /**
     * Gets optional list of token enrichers.
     *
     * @return Optional list of token enrichers. Can be empty but never {@code null}.
     */
    default List<NCTokenEnricher> getTokenEnrichers() {
        return Collections.emptyList();
    }

    /**
     * Gets optional list of entity enrichers.
     *
     * @return Optional list of entity enrichers. Can be empty but never {@code null}.
     */
    default List<NCEntityEnricher> getEntityEnrichers() {
        return Collections.emptyList();
    }

    /**
     * Gets optional list of token validators.
     *
     * @return Optional list of token validators. Can be empty but never {@code null}.
     */
    default List<NCTokenValidator> getTokenValidators() {
        return Collections.emptyList();
    }

    /**
     * Gets optional list of entity validators.
     *
     * @return Optional list of entity validators. Can be empty but never {@code null}.
     */
    default List<NCEntityValidator> getEntityValidators() {
        return Collections.emptyList();
    }

    /**
     * Gets optional variant filter.
     *
     * @return Optional variant filter.
     */
    default Optional<NCVariantFilter> getVariantFilter() {
        return Optional.empty();
    }

    /**
     * Gets optional list of entity mappers.
     *
     * @return Optional list of entity mappers. Can be empty but never {@code null}.
     */
    default List<NCEntityMapper> getEntityMappers() {
        return Collections.emptyList();
    }
}