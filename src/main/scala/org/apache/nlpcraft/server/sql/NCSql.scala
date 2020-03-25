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

package org.apache.nlpcraft.server.sql

import java.sql.Types._
import java.sql.{Connection, PreparedStatement, ResultSet, SQLException, Timestamp}

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.typesafe.scalalogging.LazyLogging
import org.apache.ignite.{Ignite, IgniteAtomicSequence, IgniteJdbcThinDriver}
import org.apache.ignite.transactions.Transaction
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.server.tx.NCTxManager
import resource._

import scala.collection._
import scala.util.control.Exception._

/**
 * Direct support for database.
 */
object NCSql extends LazyLogging {
    // Internal log switch.
    private final val LOG_SQL_STATEMENTS = false
    
    // Some built-in implicit parsers for simple values.
    object Implicits {
        /** A function that takes active result set and produces an object  by parsing one or multiple rows. */
        type RsParser[T] = ResultSet ⇒ T

        implicit val IntRsParser: RsParser[Int] = _.getInt(1)
        implicit val LongRsParser: RsParser[Long] = _.getLong(1)
        implicit val BooleanRsParser: RsParser[Boolean] = _.getBoolean(1)
        implicit val StringRsParser: RsParser[String] = _.getString(1)
        implicit val TimestampRsParser: RsParser[Timestamp] = _.getTimestamp(1)
    }

    import Implicits._

    private val threadLocal = new ThreadLocal[Connection]()

    // Type safe and eager settings container.
    private object Config extends NCConfigurable {
        final private val pre = "nlpcraft.server.database"

        def url: String = getString(s"$pre.jdbc.url")
        def driver: String = getString(s"$pre.jdbc.driver")
        def username: Option[String] = getStringOpt(s"$pre.jdbc.username")
        def passwd: Option[String] = getStringOpt(s"$pre.jdbc.password")
        def maxStmt: Int = getInt(s"$pre.c3p0.maxStatements")
        def initPoolSize: Int = getInt(s"$pre.c3p0.pool.initSize")
        def minPoolSize: Int = getInt(s"$pre.c3p0.pool.minSize")
        def maxPoolSize: Int = getInt(s"$pre.c3p0.pool.maxSize")
        def acqInc: Int = getInt(s"$pre.c3p0.pool.acquireIncrement")
    
        /**
          *
          */
        def check(): Unit = {
            if (minPoolSize > maxPoolSize)
                abortWith(s"Configuration property '$pre.c3p0.pool.minSize' ($minPoolSize) must be <= '$pre.c3p0.pool.maxSize' ($maxPoolSize).")
            if (minPoolSize > initPoolSize)
                abortWith(s"Configuration property '$pre.c3p0.pool.minSize' ($minPoolSize) must be <= '$pre.c3p0.pool.initSize' ($initPoolSize).")
            if (initPoolSize > maxPoolSize)
                abortWith(s"Configuration property '$pre.c3p0.pool.initSize' ($initPoolSize) must be <= '$pre.c3p0.pool.maxSize' ($maxPoolSize).")
            if (acqInc <= 0)
                abortWith(s"Configuration property '$pre.c3p0.pool.acquireIncrement' must be > 0: $acqInc")
        }
    }

    Config.check()

    // Pooled JDBC data source.
    private val c3p0 = {
        val ds = new ComboPooledDataSource

        // JDBC settings.
        ds.setDriverClass(Config.driver)
        ds.setJdbcUrl(Config.url)

        Config.username match {
            case Some(username) ⇒ ds.setUser(username)
            case None ⇒ // No-op.
        }

        Config.passwd match {
            case Some(passwd) ⇒ ds.setPassword(passwd)
            case None ⇒ // No-op.
        }

        // c3p0 settings.
        ds.setMaxStatements(Config.maxStmt)
        ds.setMinPoolSize(Config.minPoolSize)
        ds.setAcquireIncrement(Config.acqInc)
        ds.setMaxPoolSize(Config.maxPoolSize)
        ds.setInitialPoolSize(Config.initPoolSize)

        ds.setContextClassLoaderSource("library")
        ds.setPrivilegeSpawnedThreads(true)

        ds
    }

    logger.info(s"DB driver initialized: ${Config.driver}")

    /**
     * Wraps database error.
     *
     * @tparam R Type of the return value for the body.
     * @return Catcher.
     */
    @throws[NCE]
    private def psqlErrorCodes[R]: Catcher[R] = {
        case e: SQLException ⇒ throw new NCE(s"Database error: ${e.getLocalizedMessage}", e)
    }

    // Strips extra new lines, tabs a white spaces.
    private def strip(sql: String): String = 
        sql.replace("\n", " ").
            replace("\t", " ").
            split(" ").
            filter(!_.isEmpty).
            mkString(" ").
            trim

    /**
     * Gets connection from thread local or throws exception if one doesn't exist for this thread.
     *
     * @return JDBC connection for this thread.
     */
    @throws[NCE]
    def connection(): Connection = threadLocal.get() match {
        case c: Connection ⇒ c
        case null ⇒ throw new NCE("JDBC connection is not available (ensure to call 'Psql.sql').")
    }
    
    /**
     * Sets parameters for given prepared statement.
     *
     * @param ps Prepared statement to set parameter to.
     * @param params Set of tuples with JDBC types (legacy only) or individual values for the parameters to set.
     */
    private def prepareParams(ps: PreparedStatement, params: Any*): Unit = {
        params.zipWithIndex.foreach(z ⇒ {
            val p = z._1
    
            val idx = z._2 + 1
            
            p match {
                case x: (_, _) ⇒
                    val obj: Any = x._1
                    val typ: Int = x._2.asInstanceOf[Int]
    
                    obj match {
                        case None | null ⇒ ps.setNull(idx, typ)
                        case some: Some[_] ⇒ ps.setObject(idx, some.get, typ)
                        case _ ⇒ ps.setObject(idx, obj, typ)
                    }
                    
                case _ ⇒
                    def setObject(obj: Any): Unit = {
                        obj match {
                            // Special handling for java.util.Date.
                            case d: java.util.Date ⇒ ps.setObject(idx, d, TIMESTAMP)
                            case a: Array[_] ⇒ ps.setObject(idx, a, BINARY)
                            case x: Any ⇒ ps.setObject(idx, x)
                        }
                    }
                    
                    p match {
                        case None | null ⇒ ps.setNull(idx, NULL)
                        case Some(x) ⇒ setObject(x)
                        case _ ⇒ setObject(p)
                    }
            }
        })
    }

    /**
     * Creates new batch with given SQL statement and batch size.
     *
     * @param sql SQL statement for the batch.
     * @param size Size of the batch.
     * @return Batch instance.
     */
    def batch(sql: String, size: Int = 1000): NCSqlBatch = {
        new NCSqlBatch {
            private var ps: PreparedStatement = _

            private var cnt = 0
            private var sum = 0

            private var closed = false

            /**
             * Adds batch.
             *
             * @param params Set of tuples with JDBC types (legacy only) or individual values for the parameters to set.
             */
            @throws[NCE]
            override def add(params: Any*): Unit = {
                if (closed)
                    throw new NCE("Batch is already closed.")

                catching(psqlErrorCodes) {
                    if (ps == null) {
                        val s = sql.trim

                        ps = prepare(s, Seq.empty)

                        logger.trace(s"Batch prepared, sql: $s, size: $size")
                    }

                    prepareParams(ps, params: _*)

                    ps.addBatch()

                    cnt += 1
                    sum += 1

                    if (cnt >= size) {
                        cnt = 0

                        ps.executeBatch()
                    }
                }
            }

            /**
             * Finishes batch.
             */
            @throws[NCE]
            override def close(): Unit = {
                catching(psqlErrorCodes) {
                    if (cnt != 0) {
                        require(ps != null)

                        ps.executeBatch()
                    }

                    if (ps != null) {
                        ps.close()

                        ps = null
                    }

                    closed = true
                }

                logger.trace(s"Batch finished, total executed: $sum")
            }
        }
    }

    /**
     * Prepares SQL statement with given parameters.
     *
     * @param sql SQL statement to prepare.
     * @param params Set of tuples with JDBC types (legacy only) or individual values for the parameters to set.
     * @return JDBC prepared statement.
     */
    @throws[SQLException]
    private def prepare(sql: String, params: Seq[Any]): PreparedStatement = {
        val c = connection()
        
        val ps = c.prepareStatement(sql)

        prepareParams(ps, params: _*)

        if (LOG_SQL_STATEMENTS)
            logger.trace(s"SQL ${strip(ps.toString)}")

        ps
    }
    
    /**
      * Executes given function within a new transaction passing it properly initialized and managed JDBC connection.
      * Execution will be done isolation level 'TRANSACTION_READ_COMMITTED'. Note that either current
      * transaction will be used (i.e. for current thread) or a new one will be started.
      *
      * Notes:
      * - nesting of 'sql' blocks is supported only within the same thread.
      * - for multi-threaded operations (e.g. using Ignite cache stores) enable auto-commit.
      *
      * @param f Code to execute with open and configuration JDBC connection.
      * @tparam R Type of the execution result.
      * @return Result of SQL execution.
      */
    @throws[NCE]
    def sql[R](f: ⇒ R): R = {
        if (NCTxManager.inTx())
            sqlTx(NCTxManager.tx())(f)
        else
            NCTxManager.startTx {
                sqlTx(NCTxManager.tx())(f)
            }
    }

    /**
      * Executes given function in the context of the passed in transaction and passing it properly initialized
      * and managed JDBC connection.
      *
      * Notes:
      * - nesting of 'sql' blocks is supported only within the same thread.
      *
      * @param tx Optional ongoing transaction or `null`.
      * @param f Code to execute with open and configuration JDBC connection.
      * @tparam R Type of the execution result.
      * @return Result of SQL execution.
      */
    @throws[NCE]
    def sqlTx[R](tx: Transaction = null)(f: ⇒ R): R =
        sqlEx(Connection.TRANSACTION_READ_COMMITTED, tx)(f)
    
    /**
      * Executes given function explicitly without transaction.
      *
      * Notes:
      * - nesting of 'sql' blocks is supported only within the same thread.
      *
      * @param f Code to execute with open and configuration JDBC connection.
      * @tparam R Type of the execution result.
      * @return Result of SQL execution.
      */
    @throws[NCE]
    def sqlNoTx[R](f: ⇒ R): R =
        sqlEx(Connection.TRANSACTION_READ_COMMITTED, null)(f)

    /**
      * Executes given function passing it properly initialized and managed JDBC connection in the context
      * of given transaction.
      *
      * Notes:
      * - nesting of `sql` blocks is supported only within the same thread.
      *
      * @param isoLvl Isolation level. 'TRANSACTION_READ_COMMITTED' by default.
      * @param tx Optional ongoing transaction or `null`.
      * @param f Code to execute with open and configuration JDBC connection.
      * @tparam R Type of the execution result.
      * @return Result of SQL execution.
      */
    @throws[NCE]
    def sqlEx[R](
        isoLvl: Int = Connection.TRANSACTION_READ_COMMITTED,
        tx: Transaction = null)(f: ⇒ R): R = {
        catching[R](psqlErrorCodes) {
            val inTx = tx != null
          
            // Attempt to get connection (a) from tx, and (b) from local thread.
            var c =
                if (inTx)
                    NCTxManager.connection()
                else {
                    val x = threadLocal.get()
                    
                    if (x == null || x.isClosed) {
                        if (x != null)
                            threadLocal.set(null)
                        
                        null
                    }
                    else
                        x
                }
            
            val isNew = c == null
            
            try {
                if (isNew) {
                    c = c3p0.getConnection
    
                    // Configure the connection.
                    c.setAutoCommit(false)
                    c.setTransactionIsolation(isoLvl)

                    // Store connection in the local thread unless we are in tx.
                    threadLocal.set(c)
                }
                // Reuse existing connection and ensure that isolation level is
                // the same between this call and existing connection for this thread.
                else {
                    if (c.getTransactionIsolation != isoLvl)
                        throw new NCE("Isolation level conflicts with existing JDBC connection.")
                }

                // If we are in tx - attach connection to the current tx (it's a no-op if it's already attached).
                if (inTx)
                    NCTxManager.attach(tx, c)

                try {
                    val res = f
                    
                    c.commit()

                    res
                }
                catch {
                    // Strange that Scala-ARM doesn't support it automatically.
                    case e: Throwable ⇒
                        ignoring(classOf[SQLException])(c.rollback())

                        throw e
                }
            }
            finally {
                // Only close and reset connection if we were the ones creating it.
                // Close it only if we are NOT in tx.
                // If we are in tx - tx manager will close it when tx is closed.
                if (isNew && !inTx) {
                    ignoring(classOf[SQLException])(c.close())
                    
                    threadLocal.set(null)
                }
            }
        }
    }
    
    /**
      * Tests if this SQL wrapper has any valid JDBC connections.
      */
    def isValid: Boolean = {
        connection().isValid(5/*secs.*/)
    }

    /**
     * Commits the current thread's JDBC connection.
     */
    def commit(): Unit =
        connection().commit()

    /**
     * Rolls back the current thread's JDBC connection.
     */
    def rollback(): Unit =
        connection().rollback()

    /**
     * Executes given SQL query and returns list of the rows from result set parsed to objects.
     *
     * @param sql SQL query statement to execute.
     * @param params Set of tuples with JDBC types (legacy only) or individual values for the parameters to set.
     * @param p Implicit result set parser.
     * @tparam R Type of result objects.
     * @return List of result objects (possibly empty).
     */
    @throws[NCE]
    def select[R](sql: String, params: Any*)(implicit p: RsParser[R]): List[R] = {
        var r = List.empty[R]

        catching(psqlErrorCodes) {
            for (ps ← managed { prepare(sql, params) } ; rs ← managed { ps.executeQuery() } )
                while (rs.next)
                    r :+= p(rs)

            r
        }
    }

    /**
     * Tests whether given SQL query produces any results at all. If so - 'true' is return, 'false' otherwise.
     * Do not ask query with huge result here.
     *
     * @param sql SQL query statement to execute.
     * @param params Set of tuples with JDBC types (legacy only) or individual values for the parameters to set.
     */
    @throws[NCE]
    def exists(sql: String, params: Any*): Boolean =
        selectSingle[Long](s"SELECT count(*) FROM $sql", params: _*) match {
            case Some(cnt) ⇒ cnt > 0
            case None ⇒ throw new AssertionError("Unexpected empty result")
        }

    /**
     * Executes given SQL query and returns a single row from result set parsed to an object. If
     * there are more than one object a 'SQLException' will be thrown.
     *
     * @param sql SQL query statement to execute.
     * @param params Set of tuples with JDBC types (legacy only) or individual values for the parameters to set.
     * @param p Implicit result set parser.
     * @tparam R Type of result object.
     * @return 'Some' of single object result or 'None'.
     */
    @throws[NCE]
    def selectSingle[R](sql: String, params: Any*)(implicit p: RsParser[R]): Option[R] = {
        catching(psqlErrorCodes) {
            select(sql, params: _*)(p) match {
                case Nil ⇒ None
                case List(r) ⇒ Some(r)
                case _ ⇒ throw new SQLException("Single value expected.")
            }
        }
    }

    /**
     * Executes SQL insert statement and returns update count.
     *
     * @param sql SQL insert statement to execute.
     * @param params Set of tuples with JDBC types (legacy only) or individual values for the parameters to set.
     * @return Update count.
     */
    @throws[NCE]
    def insert(sql: String, params: Any*): Unit =
        catching(psqlErrorCodes) {
            managed { prepare(sql, params) } acquireAndGet { _.executeUpdate }
        }

    /**
     * Executes update statement.
     *
     * @param sql SQL statement.
     * @param params Set of tuples with JDBC types (legacy only) or individual values for the parameters to set.
     */
    private def exec(sql: String, params: Any*): Int =
        catching[Int](psqlErrorCodes) {
            managed { prepare(sql, params) } acquireAndGet { _.executeUpdate }
        }

    /**
     * Executes SQL update statement and returns update count.
     *
     * @param sql SQL update statement to execute.
     * @param params Set of tuples with JDBC types (legacy only) or individual values for the parameters to set.
     * @return Update count.
     */
    @throws[NCE]
    def update(sql: String, params: Any*): Int = exec(sql, params: _*)

    /**
     * Executes DDL statement and returns update count.
     *
     * @param sql DDL statement to execute.
     * @param params Set of tuples with JDBC types (legacy only) or individual values for the parameters to set.
     * @return Update count.
     */
    @throws[NCE]
    def ddl(sql: String, params: Any*): Int = exec(sql, params: _*)

    /**
     * Executes SQL delete statement and returns update count.
     *
     * @param sql SQL delete statement to execute.
     * @param params Set of tuples with JDBC types (legacy only) or individual values for the parameters to set.
     * @return Update count.
     */
    @throws[NCE]
    def delete(sql: String, params: Any*): Int = {
        // We have 'delete' method for aesthetics only.
        exec(sql, params: _*)
    }

    /**
     * Executes given SQL query and execute callback for each selected row.
     *
     * @param sql SQL query statement to execute.
     * @param callback On record callback.
     * @param params Set of tuples with JDBC types (legacy only) or individual values for the parameters to set.
     * @param p Implicit result set parser.
     * @tparam R Type of result objects.
     */
    @throws[NCE]
    def select[R](sql: String, callback: R ⇒ Unit, params: Any*) (implicit p: RsParser[R]): Unit =
        catching(psqlErrorCodes) {
            for (ps ← managed { prepare(sql, params) } ; rs ← managed { ps.executeQuery() } )
                while (rs.next)
                    callback(p(rs))
        }

    /**
      * Makes sequence.
      *
      * @param ignite Ignite instance.
      * @param name Sequence name.
      * @param tblName Table name.
      * @param colName Column name.
      */
    def mkSeq(ignite: Ignite, name:String, tblName: String, colName: String): IgniteAtomicSequence =
        NCSql.sqlNoTx {
            ignite.atomicSequence(
                name,
                selectSingle[Long](s"SELECT max($colName) FROM $tblName").getOrElse(0),
                true
            )
        }

    /**
      * Gets Ignite database usage flag.
      */
    def isIgniteDb: Boolean = c3p0.getDriverClass == classOf[IgniteJdbcThinDriver].getName

    /**
      * Gets all schema tables.
      */
    def getSchemaTables(schema: String): Seq[String] = {
        val schemaLc = schema.toLowerCase
        val tbls = mutable.ArrayBuffer.empty[String]

        catching(psqlErrorCodes) {
            for (rs ← managed { connection().getMetaData.getTables(null, null, null, null)})
                while (rs.next) {
                    val tblSchema = rs.getString(2)
             
                    if (tblSchema != null && tblSchema.toLowerCase == schemaLc)
                        tbls += rs.getString(3)
                }
        }

        tbls
    }

    /**
      * Gets configured connection schema.
      */
    def getSchema: String = catching(psqlErrorCodes) { connection().getSchema }
}
