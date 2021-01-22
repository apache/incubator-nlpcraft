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
 * Common thread pool manager.
 */
object NCThreadPoolManager extends NCService {
    @volatile private var data: ConcurrentHashMap[String, Holder] = new ConcurrentHashMap

    private case class Holder(context: ExecutionContext, pool: Option[ExecutorService])
    // Names are same as parameter names of 'ThreadPoolExecutor'.
    private case class PoolCfg(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long)

    private object Config extends NCConfigurable {
        private val module: NCModule = NCModule.getModule

        val moduleName: String = module.toString.toLowerCase

        val factories: Map[String, PoolCfg] = {
            val m: Option[Map[String, util.HashMap[String, Number]]] =
                getMapOpt(
                    module match {
                        case SERVER ⇒ "nlpcraft.server.pools"
                        case PROBE ⇒ "nlpcraft.probe.pools"

                        case m ⇒ throw new AssertionError(s"Unexpected runtime module: $m")
                    }
                )

            m.getOrElse(Map.empty).map { case (name, cfg) ⇒
                val cfgMap = cfg.asScala

                def get(prop: String): Number = {
                    try
                        cfgMap.getOrElse(prop, throw new NCE(s"Missing property value '$prop' for thread pool: $name"))
                    catch {
                        case e: ClassCastException ⇒ throw new NCE(s"Invalid property value '$prop' for thread pool: $name", e)
                    }
                }

                name →
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
                    case Some(pCfg) ⇒
                        val p = new ThreadPoolExecutor(
                            pCfg.corePoolSize,
                            pCfg.maximumPoolSize,
                            pCfg.keepAliveTime,
                            TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue[Runnable]
                        )

                        logger.info(s"Custom executor service created [ " +
                            s"name=$name, " +
                            s"module=${Config.moduleName}, " +
                            s"corePoolSize=${pCfg.corePoolSize}, " +
                            s"maximumPoolSize=${pCfg.maximumPoolSize}, " +
                            s"keepAliveTime=${pCfg.keepAliveTime}" +
                        "]")

                        Holder(ExecutionContext.fromExecutor(p), Some(p))

                    case None ⇒
                        logger.info(s"Default system executor service used [ " +
                            s"name=$name, " +
                            s"module=${Config.moduleName}" +
                        "]")

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
