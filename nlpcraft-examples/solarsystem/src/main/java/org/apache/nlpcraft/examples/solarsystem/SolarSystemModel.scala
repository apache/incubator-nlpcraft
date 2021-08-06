/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.examples.solarsystem

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.examples.solarsystem.tools.SolarSystemOpenApiService
import org.apache.nlpcraft.model.{NCIntent, NCIntentSample, NCIntentTerm, NCModelFileAdapter, NCResult, NCToken}

class SolarSystemModel extends NCModelFileAdapter("solarsystem_model.yaml") with LazyLogging {
    private var api: SolarSystemOpenApiService = _

    override def onInit(): Unit = {
        api = SolarSystemOpenApiService.getInstance()

        logger.info("Solar System Model started.")
    }

    override def onDiscard(): Unit = {
        if (api != null)
            api.stop()

        logger.info("Solar System Model stopped.")
    }

    @NCIntentSample(
        Array(
            "Moon!",
            "give me information about Larissa",
        )
    )
    @NCIntent(
        "intent=planetInfo " +
            "    options={" +
            "        'unused_usr_toks': false " +
            "    }" +
            "    term(planet)={tok_id() == 'planet'}"
    )
    def planetInfo(@NCIntentTerm("planet") planet: NCToken): NCResult =
        NCResult.text(api.bodyRequest().withFilter("id", "eq", planet.getNormalizedText).execute().toString())

    @NCIntentSample(
        Array(
            "What was discovered by Asaph Hall",
            "What was discovered by Hall",
            "Galileo Galilei planets",
            "Galilei planets",
        )
    )
    @NCIntent(
        "intent=discoverer " +
        "    options={" +
        "        'unused_usr_toks': true " +
        "    }" +
        "    term(discoverer)={tok_id() == 'discoverer'}"
    )
    def discoverer(@NCIntentTerm("discoverer") discoverer: NCToken): NCResult =
        NCResult.text(api.bodyRequest().withFilter("discoveredBy", "cs", discoverer.getNormalizedText).execute().toString())
}
