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

import java.util
import java.util.concurrent._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

/**
 *
 */
object NCPoolManager extends NCService {
    @volatile private var data: ConcurrentHashMap[String, Holder] = new ConcurrentHashMap

    private case class Holder(context: ExecutionContext, pool: Option[ExecutorService])
    private case class PoolCfg(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long)

    private object Config extends NCConfigurable {
        private val module: NCModule = NCModule.getModule

        val moduleName: String = module.toString.toLowerCase

        val factories: Map[String, PoolCfg] = {
            val m: Option[Map[String, util.HashMap[String, Number]]] =
                getMapOpt(
                    module match {
                        case SERVER ⇒ "nlpcraft.server.pools"
                        case PROBE ⇒ "nlpcraft.server.pools"
                        case m ⇒ throw new AssertionError(s"Unexpected module: $m")
                    }
                )

            m.getOrElse(Map.empty).map { case (poolName, poolCfg) ⇒
                def get(name: String): Number = {
                    val v = poolCfg.get(name)

                    if (v == null)
                        throw new NCE(s"Missed value '$name' for pool '$poolName'")

                    v
                }

                poolName →
                    PoolCfg(
                        get("corePoolSize").intValue(),
                        get("maximumPoolSize").intValue(),
                        get("keepAliveTime").longValue()
                    )
            }
        }
    }

    def getContext(name: String): ExecutionContext =
        data.computeIfAbsent(
            name,
            (_: String) ⇒
                Config.factories.get(name) match {
                    case Some(poolCfg) ⇒
                        val p = new ThreadPoolExecutor(
                            poolCfg.corePoolSize,
                            poolCfg.maximumPoolSize,
                            poolCfg.keepAliveTime,
                            TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue[Runnable]
                        )

                        logger.info(
                            s"Executor service created `$name" +
                            s", corePoolSize=${poolCfg.corePoolSize}" +
                            s", maximumPoolSize=${poolCfg.maximumPoolSize}" +
                            s", keepAliveTime=${poolCfg.keepAliveTime}" +
                            s", module: ${Config.moduleName}"
                        )

                        Holder(ExecutionContext.fromExecutor(p), Some(p))
                    case None ⇒
                        logger.info(s"System executor service used for `$name`, module: `${Config.moduleName}.")

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
