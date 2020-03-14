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

package org.apache.nlpcraft.server.ignite

import com.typesafe.scalalogging.LazyLogging
import org.apache.ignite.{IgniteException, Ignition}
import org.apache.nlpcraft.common._
import java.io.File

import scala.sys.SystemProperties

/**
 * Provides Ignite runner.
 */
object NCIgniteRunner extends LazyLogging {
    /**
      * Starts Ignite node.
      *
      * @param cfgPath Full path for configuration file or `null` for default on the
      *               class path `ignite.xml` file.
      * @param body Function to execute on running Ignite node.
      */
    @throws[NCE]
    def runWith(cfgPath: String, body: ⇒ Unit) {
        val sysProps = new SystemProperties

        // Set up Ignite system properties.
        sysProps.put("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true")
        sysProps.put("IGNITE_ANSI_OFF", "false")
        sysProps.put("IGNITE_QUIET", sysProps.get("IGNITE_QUIET").getOrElse(true).toString)
        sysProps.put("IGNITE_UPDATE_NOTIFIER", "false")
        sysProps.put("java.net.preferIPv4Stack", "true")

        val ignite =
            try {
                // Start Ignite node.
                val ignite =
                    if (cfgPath != null)
                        // 1. Higher priority. It is defined.
                        Ignition.start(cfgPath)
                    else {
                        val cfgFile = "ignite.xml"

                        // 2. Tries to find config in the same folder with JAR.
                        val cfg = new File(cfgFile)

                        if (cfg.exists() && cfg.isFile)
                            Ignition.start(cfg.getAbsolutePath)
                        else {
                            // 3. Tries to start with config from JAR.
                            val stream = U.getStream(cfgFile)

                            if (stream == null)
                                throw new NCE(s"Resource not found: $cfgFile")

                            Ignition.start(stream)
                        }
                    }

                ignite.cluster().active(true)

                ignite
            }
            catch {
                case e: IgniteException ⇒ throw new NCE(s"Ignite error: ${e.getMessage}", e)
            }

            try
                body
            finally
                Ignition.stop(ignite.name(), true)
    }
}
