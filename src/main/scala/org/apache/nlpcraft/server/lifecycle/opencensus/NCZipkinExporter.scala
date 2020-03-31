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

import io.opencensus.exporter.trace.zipkin.{ZipkinExporterConfiguration, ZipkinTraceExporter}
import org.apache.nlpcraft.common.config.NCConfigurable

/**
  * Zipkin OpenCensus exporter.
  */
class NCZipkinExporter extends NCBaseTraceExporter("Zipkin") {
    private object Config extends NCConfigurable {
        private val pre = "nlpcraft.server.opencensus.zipkin"

        def url = getStringOrElse(s"$pre.v2Url", "http://127.0.0.1:9411/api/v2/spans")
        def svcName = getStringOrElse(s"$pre.serviceName", "nlpcraft-server")
    }

    override def beforeStart(): Unit = {
        ZipkinTraceExporter.createAndRegister(ZipkinExporterConfiguration.
            builder().
            setV2Url(Config.url).
            setServiceName(Config.svcName).
            build()
        )
        
        setSampling()
        
        logger.info(s"Zipkin OpenCensus trace exporter '${Config.svcName}' started on: ${Config.url}")
    }
}
