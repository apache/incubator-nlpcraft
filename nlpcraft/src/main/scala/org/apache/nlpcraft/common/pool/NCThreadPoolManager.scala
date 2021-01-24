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

package org.apache.nlpcraft.common.pool

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.module.NCModule
import org.apache.nlpcraft.common.module.NCModule._
import org.apache.nlpcraft.common.{NCE, NCService, U}

import java.util.concurrent._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

/**
 * Common thread pool manager.
 */
object NCThreadPoolManager extends NCService {
    private final val KEEP_ALIVE_MS = 60000

    @volatile private var data: ConcurrentHashMap[String, Holder] = new ConcurrentHashMap

    private case class Holder(context: ExecutionContext, pool: Option[ExecutorService])

    private object Config extends NCConfigurable {
        private val module: NCModule = NCModule.getModule

        val moduleName: String = module.toString.toLowerCase

        val sizes: Map[String, Integer] = {
            val m: Option[Map[String, Integer]] =
                getMapOpt(
                    module match {
                        case SERVER ⇒ "nlpcraft.server.pools"
                        case PROBE ⇒ "nlpcraft.probe.pools"

                        case m ⇒ throw new AssertionError(s"Unexpected runtime module: $m")
                    }
                )

            m.getOrElse(Map.empty)
        }

        @throws[NCE]
        def check(): Unit = {
            val inv = sizes.filter(_._2 <= 0)

            if (inv.nonEmpty)
                throw new NCE(s"Invalid pool maximum sizes for: [${inv.keys.mkString(", ")}]")
        }
    }

    Config.check()

    def getContext(name: String): ExecutionContext =
        data.computeIfAbsent(
            name,
            (_: String) ⇒
                Config.sizes.get(name) match {
                    case Some(maxSize) ⇒
                        val ex = new ThreadPoolExecutor(
                            0,
                            maxSize,
                            KEEP_ALIVE_MS,
                            TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue[Runnable]
                        )

                        logger.info(s"Custom executor service created for '$name' with maxThreadSize: $maxSize.")

                        Holder(ExecutionContext.fromExecutor(ex), Some(ex))

                    case None ⇒
                        logger.info(s"Default system executor service used for '$name'")

                        Holder(ExecutionContext.Implicits.global, None)
                }
            ).context

    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ ⇒
        ackStarting()

        data = new ConcurrentHashMap

        ackStarted()
    }

    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        ackStopping()

        data.values().asScala.flatMap(_.pool).foreach(U.shutdownPool)
        data.clear()
        data = null

        ackStopped()
    }
}
