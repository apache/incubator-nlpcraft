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

package org.apache.nlpcraft.model.tools.sqlgen.impl

import java.sql.Timestamp
import java.util
import java.util.Optional

import org.apache.nlpcraft.model.tools.sqlgen._

import scala.collection.JavaConverters._
import scala.collection._
import scala.compat.java8.OptionConverters._

case class NCSqlAggregateImpl(select: Seq[NCSqlFunction], groupBy: Seq[NCSqlColumn]) extends NCSqlAggregate {
    override def getSelect: util.List[NCSqlFunction] = select.asJava
    override def getGroupBy: util.List[NCSqlColumn] = groupBy.asJava
}

case class NCSqlColumnImpl(table: String, column: String, dataType: Int, isPk: Boolean, isNullable: Boolean) extends NCSqlColumn {
    override def getTable: String = table
    override def getColumn: String = column
    override def getDataType: Int = dataType
}

case class NCSqlSimpleConditionImpl(col: NCSqlColumn, op: String, value: Object) extends NCSqlSimpleCondition {
    override def getColumn: NCSqlColumn = col
    override def getOperation: String = op
    override def getValue: Object = value
}

case class NCSqlInConditionImpl(col: NCSqlColumn, values: Seq[Object]) extends NCSqlInCondition {
    override def getColumn: NCSqlColumn = col
    override def getValues: util.List[Object] = values.asJava
}

case class NCSqlDateRangeImpl(from: Timestamp, to: Timestamp) extends NCSqlDateRange {
    override def getFrom: Timestamp = from
    override def getTo: Timestamp = to
}

case class NCSqlFunctionImpl(column: NCSqlColumn, function: String) extends NCSqlFunction {
    override def getColumn: NCSqlColumn = column
    override def getFunction: String = function
}

case class NCSqlJoinImpl(fromTable: String, toTable: String, fromColumns: Seq[String], toColumns: Seq[String], typ: NCSqlJoinType) extends NCSqlJoin {
    override def getFromTable: String = fromTable
    override def getToTable: String = toTable
    override def getFromColumns: util.List[String] = fromColumns.asJava
    override def getToColumns: util.List[String] = toColumns.asJava
    override def getType: NCSqlJoinType = typ
}

case class NCSqlSortImpl(column: NCSqlColumn, asc: Boolean) extends NCSqlSort {
    override def getColumn: NCSqlColumn = column
    override def isAscending: Boolean = asc
}

case class NCSqlTableImpl(table: String, columns: Seq[NCSqlColumn], sort: Seq[NCSqlSort], select: Seq[String], extraTables: Seq[String], defaultDate: NCSqlColumn) extends NCSqlTable {
    override def getTable: String = table
    override def getColumns: util.List[NCSqlColumn] = columns.asJava
    override def getDefaultSort: util.List[NCSqlSort] = sort.asJava
    override def getDefaultSelect: util.List[String] = select.asJava
    override def getExtraTables: util.List[String] = extraTables.asJava
    override def getDefaultDate: Optional[NCSqlColumn] = Option(defaultDate).asJava
}

case class NCSqlSchemaImpl(tables: Seq[NCSqlTable], joins: Seq[NCSqlJoin]) extends NCSqlSchema {
    override def getTables: util.Collection[NCSqlTable] = tables.asJava
    override def getJoins: util.Collection[NCSqlJoin] = joins.asJava
}

case class NCSqlLimitImpl(column: NCSqlColumn, limit: Int, asc: Boolean) extends NCSqlLimit {
    override def getLimit: Int = limit
    override def isAscending: Boolean = asc
    override def getColumn: NCSqlColumn = column
}
