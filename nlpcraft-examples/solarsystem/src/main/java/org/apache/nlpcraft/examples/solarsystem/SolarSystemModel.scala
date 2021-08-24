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
import org.apache.nlpcraft.examples.solarsystem.api.SolarSystemOpenApiService
import org.apache.nlpcraft.model.{NCModelAddPackage, NCModelFileAdapter}

@NCModelAddPackage(Array("org.apache.nlpcraft.examples.solarsystem.api"))
class SolarSystemModel extends NCModelFileAdapter("solarsystem_model.yaml") with LazyLogging {
    protected var api: SolarSystemOpenApiService = _

    override def onInit(): Unit = {
        api = SolarSystemOpenApiService.getInstance()

        logger.info("Solar System API initialized.")
    }

    override def onDiscard(): Unit = {
        if (api != null)
            api.stop()

        logger.info("Solar System API closed.")
    }
}