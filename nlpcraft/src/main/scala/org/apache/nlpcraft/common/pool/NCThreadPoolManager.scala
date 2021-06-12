/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.common.pool

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._

import java.util.concurrent._
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.CollectionHasAsScala

/**
 * Common thread pool manager.
 */
object NCThreadPoolManager extends NCService {
    // TODO: in the future - we may need to open this to user configuration.
    /**
     * Pools that should NOT default to a system context.
     */
    private final val NON_SYS_POOLS = Seq(
        "probes.communication",
        "probe.requests",
        "model.solver.pool"
    )

    private final val KEEP_ALIVE_MS = 60000
    private final val POOL_SIZE = Runtime.getRuntime.availableProcessors // Since JDK 10 is safe for containers.

    @volatile private var cache: ConcurrentHashMap[String, Holder] = _

    /**
     *
     * @param context
     * @param pool
     */
    private case class Holder(
        context: ExecutionContext,
        pool: Option[ExecutorService]
    )

    /**
     *
     * @return
     */
    def getSystemContext: ExecutionContext = ExecutionContext.Implicits.global

    /**
     *
     * @param name
     * @return
     */
    def getContext(name: String): ExecutionContext =
        cache.computeIfAbsent(
            name,
            (_: String) =>
                if (NON_SYS_POOLS.contains(name)) {
                    // Create separate executor for these pools...
                    val exec = new ThreadPoolExecutor(
                        0,
                        POOL_SIZE,
                        KEEP_ALIVE_MS,
                        TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue[Runnable]
                    )

                    Holder(ExecutionContext.fromExecutor(exec), Some(exec))
                }
                else
                    throw new NCE(s"Custom execution context for unexpected thread pool: $name")
        ).context

    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        cache = new ConcurrentHashMap

        ackStarted()
    }

    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        cache.values().asScala.flatMap(_.pool).foreach(U.shutdownPool)
        cache.clear()

        cache = null

        ackStopped()
    }
}
