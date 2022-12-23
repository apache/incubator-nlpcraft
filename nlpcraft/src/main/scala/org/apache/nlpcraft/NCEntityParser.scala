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
  * A pipeline component that converts list of tokens into the list of entities.
  *
  * Parser instance can produce [[NCEntity]] instances with different types.
  * Each [[NCEntity]] instance contains [[NCToken]] instances list and
  * each [[NCToken]] instance can belong to different [[NCEntity]] instances.
  * Order of result entities list is not important.
  *
  * Example. For [[NCToken tokens]] **San** and **Diego** can be found two [[NCEntity entities]]:
  *  - **City** entity which contains tokens **San** and **Diego**.
  *  - **Name** entity which contains token **Diego**.
  *
  *  **NOTE** that even if this parser instance produces [[NCEntity]] instances with only one same type,
  *  [[NCPipeline]] can contain multiple [[NCEntityParser]] instances, so total result set of [[NCEntity]] instances can contain different
  *  entities types. Based on this entities total result set the system prepares [[NCVariant]] instances .
  *
  * See [[NCPipeline]] for documentation on the overall processing pipeline. Note that pipeline
  * must have at least one entity parser.
  *
  * @see [[NCPipeline.getEntityParsers]]
  * @see [[NCEntity]]
  * @see [[NCToken]]
  * @see [[NCTokenParser]]
  * @see [[NCTokenEnricher]]
  * @see [[NCTokenValidator]]
  * @see [[NCEntityParser]]
  * @see [[NCEntityEnricher]]
  * @see [[NCEntityValidator]]
  * @see [[NCEntityMapper]]
  */
trait NCEntityParser extends NCLifecycle:
    /**
      * Parses and converts given list of tokens into the list of entities.
      *
      * @param req Input request descriptor.
      * @param cfg Configuration of the model this component is associated with.
      * @param toks List of tokens to convert.
      * @return List of parsed entities, potentially empty but never `null`.
      * @throws NCException Thrown in case of any errors.
      */
    def parse(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): List[NCEntity]

