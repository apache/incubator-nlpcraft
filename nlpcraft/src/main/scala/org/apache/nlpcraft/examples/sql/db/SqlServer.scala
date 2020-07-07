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

package org.apache.nlpcraft.examples.sql.db

import java.io.File
import java.sql.SQLException

import com.typesafe.scalalogging.LazyLogging
import org.h2.jdbcx.JdbcDataSource
import org.h2.tools.Server

/**
 * H2 database server. Starts standalone H2 TCP instance and loads up demo database from `northwind.sql`.
 */
object SqlServer extends LazyLogging {
    private final val H2_PORT: Int = 9092
    private final val H2_BASEDIR = new File(System.getProperty("user.home"), "nlpcraft-examples/h2").getAbsolutePath

    private final val SRV_PARAMS = Seq(
        "-baseDir", H2_BASEDIR,
        "-tcpPort", H2_PORT.toString,
        "-tcpAllowOthers"
    )

    private final val INIT_FILE = "classpath:org/apache/nlpcraft/examples/sql/db/northwind.sql"

    final val H2_URL: String = s"jdbc:h2:tcp://localhost:$H2_PORT/nlp2sql"

    @volatile private var srv: Server = _
    @volatile private var started: Boolean = false
    
    /**
      * Starts the H2 server.
      */
    def start(): Unit = {
        if (started)
            throw new IllegalStateException("Server already started.")

        srv = Server.createTcpServer(SRV_PARAMS:_*).start

        logger.info(s"H2 server start parameters: ${SRV_PARAMS.mkString(" ")}")
        logger.info(s"H2 server status: ${srv.getStatus}")

        val ds = new JdbcDataSource()

        // Unix format used to avoid escape files separator for windows.
        ds.setUrl(s"$H2_URL;INIT=RUNSCRIPT FROM '$INIT_FILE'")

        try {
            ds.getConnection

            logger.info(s"Database schema initialized for: $H2_URL")
        }
        catch {
            case e: SQLException ⇒
                // Table or view already exists - https://www.h2database.com/javadoc/org/h2/api/ErrorCode.html
                if (e.getErrorCode != 42101)
                    throw e

                logger.info(
                    s"Database '$H2_URL' is NOT initialized because data already exists. " +
                    s"To re-initialize - delete files in '$H2_BASEDIR' folder and start again. "
                )
        }

        started = true
    }
    
    /**
      * Stops H2 server.
      */
    def stop(): Unit = {
        if (!started)
            throw new IllegalStateException("Server already stopped.")

        if (srv != null)
            srv.stop()

        started = false

        logger.info(s"H2 server stopped.")
    }
}

/**
  * H2 database server runner for command line tooling.
  */
object SqlServerRunner extends App {
    SqlServer.start()
}