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

package org.apache.nlpcraft.server.lifecycle

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.config.NCConfigurable

/**
  * Lifecycle components manager.
  */
object NCServerLifecycleManager extends NCService {
    private object Config extends NCConfigurable {
        private final val prop = "nlpcraft.server.lifecycle"

        def classes: Seq[String] = getStringList(prop)
        var objects = Seq.empty[NCServerLifecycle]
    
        /**
          *
          */
        def loadAndCheck(): Unit = {
            if (classes.distinct.size != classes.size)
                throw new NCE(s"Configuration property cannot have duplicates [name=$prop]")
            
            objects = classes.map(U.mkObject(_).asInstanceOf[NCServerLifecycle])
        }
    }
    
    Config.loadAndCheck()

    /**
     *
     * @param parent Optional parent span.
     * @return
     */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        if (Config.objects.isEmpty)
            logger.info("No lifecycle components configured.")
        else {
            val tbl = NCAsciiTable("Class")
     
            Config.classes.foreach(tbl += _)
     
            tbl.info(logger, Some(s"Configured lifecycle components:"))
        }
     
        ackStart()
    }

    /**
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        ackStop()
    }
    
    /**
      * Called after Ignite started but before any other server components are started.
      */
    @throws[NCE]
    def beforeStart(): Unit = Config.objects.foreach(_.beforeStart())
    
    /**
      * Called after all server components are successfully started.
      */
    @throws[NCE]
    def afterStart(): Unit = Config.objects.foreach(_.afterStart())
    
    /**
      * Called during server shutdown before any server components are stopped.
      */
    @throws[NCE]
    def beforeStop(): Unit = Config.objects.foreach(_.beforeStop())
    
    /**
      * Called during server shutdown after all other server components are stopped.
      */
    @throws[NCE]
    def afterStop(): Unit = Config.objects.foreach(_.afterStop())
}
