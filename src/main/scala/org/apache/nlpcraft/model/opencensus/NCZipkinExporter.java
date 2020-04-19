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

package org.apache.nlpcraft.model.opencensus;

import io.opencensus.exporter.trace.zipkin.*;
import org.apache.nlpcraft.common.config.*;
import org.apache.nlpcraft.model.*;

/**
 * Probe lifecycle component that manages OpenCensus <a target=_ href="https://zipkin.io/">Zipkin</a> trace exporter. See
 * <a target=_ href="https://opencensus.io/exporters/supported-exporters/java/zipkin/">https://opencensus.io/exporters/supported-exporters/java/zipkin/</a>
 * for more details. This is a probe lifecycle component - see {@link NCLifecycle} for usage information.
 * <p>
 * Note that this exporter uses "always" sampling by default that is only suitable
 * for demo and development purposes. For production use you need to override {@link NCBaseTraceExporter#setSampling()}
 * method to provide a more efficient sampling strategy.
 * <p>
 * See <a target=_ href="https://nlpcraft.apache.org/server-and-probe.html">documentation</a> on how to configure probe life cycle
 * components, default values, etc.
 */
public class NCZipkinExporter extends NCBaseTraceExporter {
    private static class Config extends NCConfigurableJava {
        private static final String pre = "nlpcraft.probe.opencensus.zipkin";

        private final String url = getStringOrElse(pre + ".v2Url", "http://127.0.0.1:9411/api/v2/spans");
        private final String svcName = getStringOrElse(pre + ".serviceName", "nlpcraft-probe");
    }

    private static final Config cfg = new Config();
    
    /**
     * Creates new Zipkin OpenCensus trace export lifecycle component.
     */
    public NCZipkinExporter() {
        super("Zipkin");
    }

    @Override
    public void onInit() {
        ZipkinTraceExporter.createAndRegister(ZipkinExporterConfiguration.
            builder().
            setV2Url(cfg.url).
            setServiceName(cfg.svcName).
            build()
        );

        setSampling();

        log.info("Zipkin OpenCensus trace exporter '{}' started on: {}", cfg.svcName, cfg.url);
    }
}
