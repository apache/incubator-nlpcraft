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

import io.opencensus.trace.*;
import io.opencensus.trace.config.*;
import io.opencensus.trace.samplers.*;
import org.apache.nlpcraft.model.*;
import org.slf4j.*;

/**
 * Base class for OpenCensus trace exporters.
 */
public abstract class NCBaseTraceExporter implements NCLifecycle {
    private final String name;

    protected static final Logger log = LoggerFactory.getLogger(NCBaseTraceExporter.class);

    /**
     * New base tracer with given name.
     * 
     * @param name Name of the exporter.
     */
    public NCBaseTraceExporter(String name) {
        this.name = name;
    }

    /**
     * Sets sampling for this trace exporter. Note that default implementation uses "always" sampling which
     * is only suitable for demo and development purposes. For the production use you need to override this
     * method and provide more efficient sampling strategy.
     *
     * @see Samplers
     */
    protected void setSampling() {
        TraceConfig cfg = Tracing.getTraceConfig();

        cfg.updateActiveTraceParams(cfg.getActiveTraceParams().toBuilder().setSampler(Samplers.alwaysSample()).build());
    }

    @Override
    public void onDiscard() {
        Tracing.getExportComponent().shutdown();

        log.info("{} OpenCensus trace exporter shutdown.", name);
    }
}


