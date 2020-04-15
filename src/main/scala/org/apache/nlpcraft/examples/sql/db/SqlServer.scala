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
 * TODO
 *
 * H2 database should be started before probe is started.
 * Usually H2 started in memory mode for such examples,
 * but we need to start it as separated service to have possibility to connect to this server from generator application
 * and prepare JSON model stub based on it.
 *
 * Start H2 server this helper allows to
 *  - start TCP server and
 *  - creates demo database schema, fill it with data.
 *
 *  This utility added just for convenience. You can start via h2 jars files - http://www.h2database.com/html/main.html
 * and install data with northwind.sql before using this example.
 */
object SqlServer extends App with LazyLogging {
    private lazy final val H2_PORT: Int = 9092
    private lazy final val H2_BASEDIR = "~/nlpcraft-examples/h2"

    private final val SRV_PARAMS = Seq(
        "-baseDir", H2_BASEDIR,
        "-tcpPort", H2_PORT.toString,
        "-tcpAllowOthers"
    )
    private final val INIT_FILE = "src/main/scala/org/apache/nlpcraft/examples/sql/db/northwind.sql"

    lazy final val H2_URL: String = s"jdbc:h2:tcp://localhost:$H2_PORT/nlp2sql"

    private def process(): Unit = {
        val srv = Server.createTcpServer(SRV_PARAMS:_*).start

        logger.info(s"H2 server start parameters: ${SRV_PARAMS.mkString(" ")}")
        logger.info(s"H2 server status: ${srv.getStatus}")

        val ds = new JdbcDataSource()

        ds.setUrl(s"$H2_URL;INIT=RUNSCRIPT FROM '${new File(INIT_FILE).getAbsolutePath}'")

        try {
            ds.getConnection

            logger.info(s"Database schema initialized for: $H2_URL")
        }
        catch {
            case e: SQLException ⇒
                // https://www.h2database.com/javadoc/org/h2/api/ErrorCode.html
                if (e.getErrorCode == 42101)
                    logger.info(
                        s"Database '$H2_URL' is NOT initialized because data already exists. " +
                            s"To re-initialize - delete files in '$H2_BASEDIR' folder and start again. "
                    )
                else
                    throw e
        }

        // Wait indefinitely.
        Thread.currentThread().join()
    }

    process()
}
