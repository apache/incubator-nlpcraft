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

import java.sql.SQLException

import com.typesafe.scalalogging.Logger
import org.apache.ignite.IgniteException
import org.apache.nlpcraft.common._

import scala.util.control.Exception._

/**
  * Mixin for Ignite exception handling.
  */
trait NCIgniteExceptions {
    /**
      * Partial function that wraps Ignite exceptions and rethrows 'NCE'.
      *
      * @tparam R Type of the return value for the body.
      * @return Catcher.
      */
    @throws[NCE]
    protected def wrapIE[R]: Catcher[R] = {
        case e: IgniteException ⇒ throw new NCE(s"Ignite error: ${e.getMessage}", e)
    }

    /**
      * Partial function that wraps SQL exceptions and rethrows 'NCE'.
      *
      * @tparam R Type of the return value for the body.
      * @return Catcher.
      */
    @throws[NCE]
    protected def wrapSql[R]: Catcher[R] = {
        case e: SQLException ⇒ throw new NCE(s"SQL error.", e)
    }

    /**
      * Partial function that catches Ignite exceptions and logs them.
      *
      * @return Catcher.
      */
    protected def logIE(logger: Logger): Catcher[Unit] = {
        case e: IgniteException ⇒ logger.error(s"Ignite error.", e)
    }
}
