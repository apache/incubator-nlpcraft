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

import java.sql.{Connection, PreparedStatement, SQLException}

import com.github.vertical_blank.sqlformatter.SqlFormatter
import com.jakewharton.fliptables.FlipTable
import com.typesafe.scalalogging.LazyLogging
import org.h2.jdbc.JdbcSQLException
import org.h2.jdbcx.JdbcDataSource
import resource.managed

/**
  * Ad-hoc querying for H2 Database. This is a simple, single thread implementation.
  */
object SqlAccess extends LazyLogging {
    private final val LOG_ROWS = 10

    private var conn: Connection = _
    
    /**
      * Runs given query and return result. It also manages connections to database.
      *
      * @param qry SQl query.
      * @param logResult Log printing flag. Useful for debugging.
      */
    def select(qry: SqlQuery, logResult: Boolean): SqlResult = {
        def getConnection: Connection = {
            val ds = new JdbcDataSource

            ds.setUrl(SqlServer.H2_URL)

            ds.getConnection("", "")
        }

        def getPs: PreparedStatement = {
            if (conn == null)
                conn = getConnection

            try
                conn.prepareStatement(qry.sql)
            catch {
                case e: JdbcSQLException ⇒
                    close()

                    // H2 specific - connection broken: https://www.h2database.com/javadoc/org/h2/api/ErrorCode.html
                    if (e.getErrorCode != 90067)
                        throw e

                    // Connection recreated.
                    conn = getConnection

                    conn.prepareStatement(qry.sql)
            }
        }

        try {
            managed { getPs } acquireAndGet { ps ⇒
                qry.parameters.zipWithIndex.foreach { case (p, idx) ⇒ ps.setObject(idx + 1, p) }

                managed { ps.executeQuery() } acquireAndGet { rs ⇒
                    val md = rs.getMetaData
                    val cnt = md.getColumnCount

                    val cols = (1 to cnt).map(md.getColumnName)
                    var rows = List.empty[Seq[String]]

                    while (rs.next)
                        rows :+= (1 to cnt).map(i ⇒ {
                            val o = rs.getObject(i)

                            if (rs.wasNull()) "" else o.toString
                        })

                    if (logResult) {
                        logger.info(
                            s"Query executed successful" +
                                s" [\nsql=\n${SqlFormatter.format(qry.sql)}" +
                                s", \nparameters=${qry.parameters.mkString(",")}" +
                                s", \nrows=${rows.size}" +
                                s"]"
                        )

                        logger.info(s"Execution result, first $LOG_ROWS lines...")

                        var data = rows.take(LOG_ROWS).toArray.map(_.toArray)

                        if (rows.nonEmpty && rows.size > LOG_ROWS)
                            data = data ++ Array(cols.indices.map(_ ⇒ "...").toArray)

                        logger.info(s"\n${FlipTable.of(cols.toArray, data)}")
                    }

                    SqlResult(cols, rows)
                }
            }
        }
        catch {
            case e: SQLException ⇒
                close()

                conn = null

                logger.warn(
                    s"Query executed unsuccessful [sql=" +
                        s"\n${SqlFormatter.format(qry.sql)}" +
                        s"\nparameters=${qry.parameters.mkString(", ")}" +
                        s"\n]"
                )

                throw e
        }
    }

    /**
      * Closes this service by closing database connection. 
      */
    def close(): Unit =
        if (conn != null)
            try
                conn.close()
            catch {
                case _: Exception ⇒ logger.warn("Error closing DB connection.")
            }
}
