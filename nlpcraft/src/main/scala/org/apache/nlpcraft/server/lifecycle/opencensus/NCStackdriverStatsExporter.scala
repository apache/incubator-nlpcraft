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

package org.apache.nlpcraft.server.lifecycle.opencensus

import java.io.IOException

import com.typesafe.scalalogging.LazyLogging
import io.opencensus.exporter.stats.stackdriver.{StackdriverStatsConfiguration, StackdriverStatsExporter}
import org.apache.nlpcraft.common.NCE
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.server.lifecycle.NCServerLifecycle

/**
  * Google Stackdriver OpenCensus stats exporter.
  */
class NCStackdriverStatsExporter extends NCServerLifecycle with LazyLogging {
    private val pre = "nlpcraft.server.opencensus.stackdriver"
    
    private object Config extends NCConfigurable {
        def gpi = getString(s"$pre.googleProjectId")
        def prefix = getStringOrElse(s"$pre.metricsPrefix", "custom.googleapis.com/nlpcraft/server")
    }
    
    override def beforeStart(): Unit = {
        try
            StackdriverStatsExporter.createAndRegister(StackdriverStatsConfiguration.builder.
                setMetricNamePrefix(Config.prefix).
                setProjectId(Config.gpi).
                build
            )
        catch {
            case e: IOException ⇒
                throw new NCE(
                    s"Stackdriver OpenCensus stats exporter cannot be registered for Google Project ID: ${Config.gpi}", e
                )
        }
        
        logger.info(s"Stackdriver OpenCensus stats exporter '${Config.prefix}' started for Google Project ID: ${Config.gpi}")
    }
}
