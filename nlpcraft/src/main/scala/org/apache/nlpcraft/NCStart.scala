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
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ansi.NCAnsi._
import org.apache.nlpcraft.model.tools.cmdline.NCCli

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
        val isCli = seq.indexWhere(_ == "-cli") >= 0

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
            val ver = NCVersion.getCurrent
    
            logger.info(
                U.NL +
                U.asciiLogo() +
                s"${U.NL}" +
                s"Version: ${ansiBold(ver.version)}${U.NL}" +
                s"${NCVersion.copyright}${U.NL}"
            )
            
            for (msg <- msgs)
                logger.error(msg)
    
            logger.info(g("Usage:"))
            logger.info("  Use '-server' argument to start server.")
            logger.info("  Use '-probe' argument to start probe.")
            logger.info("  Use '-cli' argument to start CLI.")

            System.exit(1)
        }

        if (!isSrv && !isPrb && !isCli)
            error("Either '-server', '-probe' or '-cli' argument must be provided.")
        else if (isPrb)
            NCProbe.main(removeParam("-probe"))
        else if (isSrv)
            NCServer.main(removeParam("-server"))
        else if (isCli)
            NCCli.main(removeParam("-cli"))
    }

    execute()
}
