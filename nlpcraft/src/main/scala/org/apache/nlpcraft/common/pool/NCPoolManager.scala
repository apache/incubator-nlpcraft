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
import org.apache.nlpcraft.common.{NCService, U}

import java.util.concurrent.{ConcurrentHashMap, ExecutorService}
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

/**
 *
 * @param cfg
 */
abstract class NCPoolManager(cfg: String) extends NCService {
    @volatile private var data: ConcurrentHashMap[String, Holder] = new ConcurrentHashMap

    private case class Holder(context: ExecutionContext, pool: Option[ExecutorService])

    private object Config extends NCConfigurable {
        val factories: Map[String, NcPoolFactory] = {
            val m: Option[Map[String, String]] = getMapOpt(cfg)

            m.getOrElse(Map.empty).map(p ⇒ p._1 → U.mkObject(p._2))
        }
    }

    def getContext(name: String): ExecutionContext =
        data.computeIfAbsent(
            name,
            (_: String) ⇒
                Config.factories.get(name) match {
                    case Some(f) ⇒
                        val p = f.mkExecutorService()

                        logger.info(s"Executor service created with factory '${f.getClass.getName}' for '$name'")

                        Holder(ExecutionContext.fromExecutor(p), Some(p))
                    case None ⇒
                        logger.info(s"System executor service used for '$name'")

                        Holder(scala.concurrent.ExecutionContext.Implicits.global, None)
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
