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

import org.apache.nlpcraft.model.tools.sqlgen.NCSqlColumn

/**
  * SQL condition trait.
  */
sealed trait SqlCondition {
    /**
      * Gets condition column.
      */
    def column: NCSqlColumn
}

/**
  * SQL simple condition data holder.
  *
  * @param column Condition column.
  * @param operation Operation between column and it's value.
  * @param value Condition value.
  */
case class SqlSimpleCondition(column: NCSqlColumn, operation: String, value: Any) extends SqlCondition

/**
  * SQL IN condition data holder.
  *
  * @param column Condition column.
  * @param values Condition values.
  */
case class SqlInCondition(column: NCSqlColumn, values: Seq[Any]) extends SqlCondition

/**
  * SQL query data holder.
  *
  * @param sql SQL query text with placeholders `?` for its parameters.
  * @param parameters SQL query parameters.
  */
case class SqlQuery(sql: String, parameters: Seq[Any])

/**
  * SQL execution result data holder.
  *
  * @param columns Columns names.
  * @param rows Table of results in string representation, row by row.
  */
case class SqlResult(columns: Seq[String], rows: Seq[Seq[String]])