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

package org.apache.nlpcraft.probe.mgrs.inspections.inspectors

import java.util.concurrent.{ExecutorService, Executors}

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.inspections.NCInspectionResult
import org.apache.nlpcraft.common.util.NCUtils

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

/**
  * // TODO: duplicated with server component (same API) etc - should we move it into common?
  */
private[inspections] trait NCInspector extends NCService {
    @volatile private var pool: ExecutorService = _
    @volatile protected var executor: ExecutionContextExecutor = _

    /**
      *
      * @param mdlId
      * @param inspId
      * @param args
      * @param parent
      * @return
      */
    def inspect(mdlId: String, inspId: String, args: Option[String], parent: Span = null): Future[NCInspectionResult]

    override def start(parent: Span): NCService =
        startScopedSpan("start", parent) { _ ⇒
            pool = Executors.newCachedThreadPool()
            executor = ExecutionContext.fromExecutor(pool)

            super.start(parent)
        }

    override def stop(parent: Span): Unit =
        startScopedSpan("stop", parent) { _ ⇒
            super.stop(parent)

            NCUtils.shutdownPools(pool)
            executor = null
        }
}
