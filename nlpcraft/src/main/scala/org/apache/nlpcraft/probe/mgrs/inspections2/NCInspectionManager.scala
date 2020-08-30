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

package org.apache.nlpcraft.probe.mgrs.inspections2

import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.model.opencensus.stats.NCOpenCensusModelStats
import io.opencensus.trace.Span
import org.apache.nlpcraft.common.inspections2.NCInspectionResult

/**
 *
 */
object NCInspectionManager extends NCService with NCOpenCensusModelStats {
    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ ⇒
        // TODO

        super.start(parent)
    }

    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()

        // TODO
    }

    /**
     *
     * @param mdlId Model ID.
     * @param inspId Inspection ID.
     * @param inspArgs Inspection arguments as JSON string.
     * @param parent Optional parent trace span.
     * @return
     */
    def inspect(mdlId: String, inspId: String, inspArgs: String, parent: Span = null): NCInspectionResult = ???
}
