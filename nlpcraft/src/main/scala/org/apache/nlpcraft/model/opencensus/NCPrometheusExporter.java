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

import io.opencensus.exporter.stats.prometheus.*;
import io.prometheus.client.exporter.*;
import org.apache.nlpcraft.common.*;
import org.apache.nlpcraft.common.config.*;
import org.apache.nlpcraft.model.*;
import org.slf4j.*;
import scala.*;
import java.io.*;

/**
 * Probe lifecycle component that manages OpenCensus <a target=_ href="https://prometheus.io">Prometheus</a> stats exporter. See
 * <a target=_ href="https://opencensus.io/exporters/supported-exporters/java/prometheus/">https://opencensus.io/exporters/supported-exporters/java/prometheus/</a>
 * for more details. This is a probe lifecycle component - see {@link NCLifecycle} for usage information.
 * <p>
 * When running Prometheus you will need to supply it a configuration file. Here's an example of the minimal
 * YAML configuration you can use to monitor both the server and the probe:
 * <pre class="brush: js">
 * global:
 *   scrape_interval: 5s
 *
 *   external_labels:
 *     monitor: 'nlpcraft'
 *
 * scrape_configs:
 *   - job_name: 'prometheus'
 *     scrape_interval: 5s
 *     static_configs:
 *       - targets: ['localhost:9090']
 *   - job_name: 'nlpcraft-server'
 *     scrape_interval: 5s
 *     static_configs:
 *       - targets: ['localhost:8888']
 *   - job_name: 'nlpcraft-probe'
 *     scrape_interval: 5s
 *     static_configs:
 *       - targets: ['localhost:8889']
 * </pre>
 * See <a target=_ href="https://nlpcraft.apache.org/server-and-probe.html">documentation</a> on how to configure probe life cycle
 * components, default values, etc.
 * <p>
 */
public class NCPrometheusExporter implements NCLifecycle {
    private static class Config extends NCConfigurableJava {
        private static final String pre = "nlpcraft.probe.opencensus.prometheus";

        private final String namespace = getStringOrElse(pre + ".namespace", "nlpcraft-probe");
        private final Tuple2<String, Integer> hostPort =
            getHostPortOrElse(pre + ".hostport", "localhost", 8889);
        private final String host = hostPort._1;
        private final int port = hostPort._2;
    }

    private static final Config cfg = new Config();

    private static final Logger log = LoggerFactory.getLogger(NCPrometheusExporter.class);

    @Override
    public void onInit() {
        // Register the Prometheus exporter.
        PrometheusStatsCollector.createAndRegister(
            PrometheusStatsConfiguration.builder().
                setNamespace(cfg.namespace).
                build()
        );

        // Run the server as a daemon.
        try {
            new HTTPServer(cfg.host, cfg.port, true);
        }
        catch (IOException e) {
            throw new NCException(String.format("Failed to start HTTP server on:  http://%s:%d", cfg.host, cfg.port), e);
        }

        log.info("Prometheus OpenCensus stats exporter '{}' started on: http://{}:{}", cfg.namespace, cfg.host, cfg.port);
    }
}
