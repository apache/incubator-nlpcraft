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

import io.opencensus.exporter.trace.stackdriver.{StackdriverTraceConfiguration, StackdriverTraceExporter}
import org.apache.nlpcraft.common.NCE
import org.apache.nlpcraft.common.config.NCConfigurable

/**
  * Google Stackdriver OpenCensus trace exporter.
  */
class NCStackdriverTraceExporter extends NCBaseTraceExporter("Stackdriver") {
    private val pre = "nlpcraft.server.opencensus.stackdriver"
    
    private object Config extends NCConfigurable {
        def gpi = getString(s"$pre.googleProjectId")
    }
    
    override def beforeStart(): Unit = {
        try
            StackdriverTraceExporter.createAndRegister(StackdriverTraceConfiguration.builder.
                setProjectId(Config.gpi).
                build
            )
        catch {
            case e: IOException ⇒
                throw new NCE(
                    s"Stackdriver OpenCensus trace exporter cannot be registered for Google Project ID: ${Config.gpi}", e
                )
        }
    
        setSampling()
    
        logger.info(s"Stackdriver OpenCensus trace exporter started for Google Project ID: ${Config.gpi}")
    }
}
