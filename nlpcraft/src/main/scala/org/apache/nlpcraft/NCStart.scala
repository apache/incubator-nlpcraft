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

package org.apache.nlpcraft

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.common.version.NCVersion
import org.apache.nlpcraft.probe.NCProbe
import org.apache.nlpcraft.server.NCServer

/**
  * Server or probe command line starter.
  */
object NCStart extends App with LazyLogging {
    /**
      *
      */
    private def execute(): Unit = {
        val seq = args.toSeq

        val isSrv = seq.indexWhere(_ == "-server") >= 0
        val isPrb = seq.indexWhere(_ == "-probe") >= 0

        /**
          *
          * @param p
          * @return
          */
        def removeParam(p: String): Array[String] = seq.filter(_ != p).toArray

        /**
          *
          * @param msgs
          */
        def error(msgs: String*): Unit = {
            val NL = System getProperty "line.separator"
            val ver = NCVersion.getCurrent
    
            val s = NL +
                raw"    _   ____      ______           ______   $NL" +
                raw"   / | / / /___  / ____/________ _/ __/ /_  $NL" +
                raw"  /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/  $NL" +
                raw" / /|  / / /_/ / /___/ /  / /_/ / __/ /_    $NL" +
                raw"/_/ |_/_/ .___/\____/_/   \__,_/_/  \__/    $NL" +
                raw"       /_/                                  $NL$NL" +
                s"Version: ${ver.version}$NL" +
                raw"${NCVersion.copyright}$NL"
    
            logger.info(s)
            
            for (msg ← msgs)
                logger.error(msg)
    
            logger.info("Usage:")
            logger.info("  Use '-server' argument to start server.")
            logger.info("  Use '-probe' argument to start probe.")

            System.exit(1)
        }

        if (!isSrv && !isPrb)
            error("Either '-server' or '-probe' argument must be provided.")
        else if (isSrv && isPrb)
            error("Only one '-server' or '-probe' argument must be provided.")
        else if (isPrb)
            NCProbe.main(removeParam("-probe"))
        else if (isSrv)
            NCServer.main(removeParam("-server"))
    }

    execute()
}
