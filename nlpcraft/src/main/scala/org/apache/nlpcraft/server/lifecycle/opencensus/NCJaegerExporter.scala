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

import io.opencensus.exporter.trace.jaeger.{JaegerExporterConfiguration, JaegerTraceExporter}
import org.apache.nlpcraft.common.config.NCConfigurable

/**
  * Jaeger OpenCensus trace exporter.
  */
class NCJaegerExporter extends NCBaseTraceExporter("Jaeger") {
    private object Config extends NCConfigurable {
        private val pre = "nlpcraft.server.opencensus.jaeger"

        def url = getStringOrElse(s"$pre.thriftUrl", "http://127.0.0.1:14268/api/traces")
        def svcName = getStringOrElse(s"$pre.serviceName", "nlpcraft-server")
    }
    
    override def beforeStart(): Unit = {
        JaegerTraceExporter.createAndRegister(JaegerExporterConfiguration.builder.
            setThriftEndpoint(Config.url).
            setServiceName(Config.svcName).
            build
        )
        
        setSampling()
    
        logger.info(s"Jaeger OpenCensus trace exporter '${Config.svcName}' started on: ${Config.url}")
    }
}
