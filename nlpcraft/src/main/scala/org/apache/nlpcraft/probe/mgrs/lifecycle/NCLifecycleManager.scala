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

package org.apache.nlpcraft.probe.mgrs.lifecycle

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.model._

/**
  * Lifecycle component manager.
  */
object NCLifecycleManager extends NCService {
    @volatile private var beans: Seq[NCLifecycle] = _

    object Config extends NCConfigurable {
        def lifecycle: Seq[String] = getStringList("nlpcraft.probe.lifecycle")
    }

    /**
     *
     * @param parent Optional parent span.
     * @throws NCE
     * @return
     */
    @throws[NCE]
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        beans = Config.lifecycle.map(U.mkObject(_).asInstanceOf[NCLifecycle])
    
        ackStarted()
    }


    /**
     * Stops this service.
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span): Unit = startScopedSpan("start", parent) { _ =>
        ackStopping()
        ackStopped()
    }

    /**
      * Called before any other probe managers are started.
      * Default implementation is a no-op.
      */
    @throws[NCE]
    def onInit(): Unit = beans.foreach(_.onInit())
    
    /**
      * Called during probe shutdown after all other probe managers are stopped.
      * Default implementation is a no-op.
      */
    @throws[NCE]
    def onDiscard(): Unit = beans.foreach(_.onDiscard())
}
