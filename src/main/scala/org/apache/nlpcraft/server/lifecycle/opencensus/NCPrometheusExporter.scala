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

import com.typesafe.scalalogging.LazyLogging
import io.opencensus.exporter.stats.prometheus.{PrometheusStatsCollector, PrometheusStatsConfiguration}
import io.prometheus.client.exporter.HTTPServer
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.server.lifecycle.NCServerLifecycle

/**
  * Prometheus OpenCensus stats exporter.
  */
class NCPrometheusExporter extends NCServerLifecycle with LazyLogging {
    private object Config extends NCConfigurable {
        private val pre = "nlpcraft.server.opencensus.prometheus"
        private val hostPort = getHostPortOrElse(s"$pre.hostPort", "localhost", 8888)

        def namespace = getStringOrElse(s"$pre.namespace", "nlpcraft-server")
        def host = hostPort._1
        def port = hostPort._2
    }
    
    override def beforeStart(): Unit = {
        // Register the Prometheus exporter.
        PrometheusStatsCollector.createAndRegister(
            PrometheusStatsConfiguration.builder().
                setNamespace(Config.namespace).
                build()
        )
        
        // Run the server as a daemon.
        new HTTPServer(Config.host, Config.port, true)
    
        logger.info(s"Prometheus OpenCensus stats exporter '${Config.namespace}' started on: http://${Config.host}:${Config.port}")
    }
}
