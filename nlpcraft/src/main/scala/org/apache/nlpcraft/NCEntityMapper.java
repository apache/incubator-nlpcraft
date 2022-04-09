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

import java.util.List;

/**
 * A pipeline component that allows to map one set of entities into another after the entities were parsed
 * and enriched. Entity mapper is an optional component and the pipeline can have zero or more entity mappers. Mappers
 * are typically used for combine several existing entities into a new one without necessarily touching the entity
 * parser or enrichers. See {@link NCPipeline} for documentation on the overall processing pipeline.
 *
 * @see NCPipeline#getEntityMappers()
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
public interface NCEntityMapper extends NCLifecycle {
    /**
     * Maps given of entities into a new list of entities.
     *
     * @param req Input request descriptor.
     * @param cfg Configuration of the model this component is associated with.
     * @param ents List of entities to map.
     * @return List of entities (new or existing ones).
     */
    List<NCEntity> map(NCRequest req, NCModelConfig cfg, List<NCEntity> ents);
}
