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

import io.opencensus.exporter.stats.stackdriver.*;
import org.apache.nlpcraft.common.*;
import org.apache.nlpcraft.common.config.*;
import org.apache.nlpcraft.model.*;
import org.slf4j.*;
import java.io.*;

/**
 * Probe lifecycle component that manages OpenCensus <a target=_ href="https://cloud.google.com/stackdriver/">Stackdriver</a> stats exporter. See
 * <a target=_ href="https://opencensus.io/exporters/supported-exporters/java/stackdriver-stats/">https://opencensus.io/exporters/supported-exporters/java/stackdriver-stats/</a>
 * for more details. This is a probe lifecycle component - see {@link NCLifecycle} for usage information.
 * <p>
 * See <a target=_ href="https://nlpcraft.apache.org/server-and-probe.html">documentation</a> on how to configure probe life cycle
 * components, default values, etc.
 */
public class NCStackdriverStatsExporter implements NCLifecycle {
    private static class Config extends NCConfigurableJava {
        private static final String pre = "nlpcraft.probe.opencensus.stackdriver";

        private final String prefix = getStringOrElse(pre + ".metricsPrefix", "custom.googleapis.com/nlpcraft/probe");
        private final String gpi = getString(pre + ".googleProjectId");
    }

    private static final Config cfg = new Config();

    private static final Logger log = LoggerFactory.getLogger(NCStackdriverStatsExporter.class);

    @Override
    public void onInit() {
        try {
            StackdriverStatsExporter.createAndRegister(StackdriverStatsConfiguration.builder().
               setMetricNamePrefix(cfg.prefix).
               setProjectId(cfg.gpi).
               build()
            );
        }
        catch (IOException e) {
            throw new NCException(
                String.format("Stackdriver OpenCensus stats exporter cannot be registered for project: %s", cfg.gpi), e
            );
        }

        log.info("Stackdriver OpenCensus stats exporter started for project: {}", cfg.gpi);
    }
}
