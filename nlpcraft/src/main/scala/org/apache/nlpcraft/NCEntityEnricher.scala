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
  * A pipeline component that enrichers entities by settings their properties.
  * See {@link NCPipeline} for documentation on the overall processing pipeline. Note that this is
  * an optional component in the pipeline.
  *
  * @see NCPipeline#getEntityEnrichers()
  * @see NCEntity
  * @see NCToken
  * @see NCTokenParser
  * @see NCTokenEnricher
  * @see NCTokenValidator
  * @see NCEntityParser
  * @see NCEntityEnricher
  * @see NCEntityValidator
  * @see NCEntityMapper
  */
trait NCEntityEnricher extends NCLifecycle :
    /**
      * Enriches given list of entities by settings their properties.
      *
      * @param req Input request descriptor.
      * @param cfg Configuration of the model this component is associated with.
      * @param ents List of entities to enrich.
      * @throws NCException Thrown in case of any errors.
      * @see NCPropertyMap */
    def enrich(req: NCRequest, cfg: NCModelConfig, ents: List[NCEntity]): Unit
