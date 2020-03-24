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

import java.sql.Types
import java.util

import com.typesafe.scalalogging.LazyLogging
import org.jgrapht.alg.DijkstraShortestPath
import org.jgrapht.graph.{DefaultEdge, SimpleGraph}
import org.apache.nlpcraft.model.tools.sqlgen.{NCSqlAggregate, _}
import org.apache.nlpcraft.model.tools.sqlgen.impl.{NCSqlFunctionImpl, NCSqlSortImpl}

import scala.collection.JavaConverters._
import scala.collection.{Seq, mutable}
import scala.compat.java8.OptionConverters._

/**
  *
  * Simple SQL Builder based on following detected database elements: table and column names, conditions, sorts etc
  * This simplified version doesn't take into account some common SQL stuff like
  *  - OR logic conditions
  *  - negation (SQL <> condition),
  *  - LIKE and another widespread  functions.
  *  - etc
  *  Development of Universal SQL Builder (which can cover most of use cases) is big and non trivial task
  *  and it's  development is out of current example.
  *
  * @param schema
  */
case class SqlBuilder(schema: NCSqlSchema) extends LazyLogging {
    private final val DFLT_LIMIT = 1000

    private case class Edge(from: String, to: String) extends DefaultEdge
    private case class Key(table: String, column: String)

    private val schemaTabs = schema.getTables.asScala.toSeq.sortBy(_.getTable)
    private val schemaTabsByNames = schemaTabs.map(p ⇒ p.getTable → p).toMap
    private val schemaCols =
        schemaTabs.flatMap(p ⇒ p.getColumns.asScala.map(col ⇒ Key(col.getTable, col.getColumn) → col)).toMap
    private val schemaJoins = schema.getJoins.asScala

    private val graph = {
        val g = new SimpleGraph[String, Edge](classOf[Edge])

        schemaTabs.foreach(t ⇒ g.addVertex(t.getTable))
        schemaJoins.foreach(j ⇒ g.addEdge(j.getFromTable, j.getToTable, Edge(j.getFromTable, j.getToTable)))

        g
    }

    private var tabs: Seq[NCSqlTable] = Seq.empty
    private var cols: Seq[NCSqlColumn] = Seq.empty
    private var conds: Seq[SqlCondition] = Seq.empty
    private var sorts: Seq[NCSqlSort] = Seq.empty
    private var freeDateRangeOpt: Option[NCSqlDateRange] = None
    private var aggregationOpt: Option[NCSqlAggregate] = None
    private var limit: Option[NCSqlLimit] = None

    private def sql(clause: NCSqlTable): String = clause.getTable
    private def sql(clause: NCSqlColumn): String = s"${clause.getTable}.${clause.getColumn}"
    private def sql(clause: NCSqlSort): String = s"${sql(clause.getColumn)} ${if (clause.isAscending) "ASC" else "DESC"}"
    private def sql(clause: SqlFunction): String = s"${clause.getFunction.toLowerCase}(${sql(clause.getColumn)})"

    // TODO: implement based on join type.
    private def sql(clause: NCSqlJoin): String =
        clause.getFromColumns.asScala.
            zip(clause.getToColumns.asScala).
            map { case (fromCol, toCol) ⇒ s"${ clause.getFromTable}.$fromCol = ${clause.getToTable}.$toCol" }.
            mkString(" AND ")

    private def sql(clause: SqlInCondition): String =
        s"${sql(clause.getColumn)} IN (${(0 until clause.getValues.size()).map(_ ⇒ "?").mkString(",")})"

    private def sql(clause: SqlSimpleCondition): String = s"${sql(clause.getColumn)} ${clause.getOperation} ?"

    @throws[RuntimeException]
    private def sql(clause: SqlCondition): String =
        clause match {
            case x: SqlSimpleCondition ⇒ sql(x)
            case x: SqlInCondition ⇒ sql(x)

            case _ ⇒ throw new RuntimeException("Unexpected condition")
        }

    private def isDate(col: NCSqlColumn): Boolean = col.getDataType == Types.DATE
    private def isString(col: NCSqlColumn): Boolean = col.getDataType == Types.VARCHAR

    private def getImportant(tabs: Seq[NCSqlTable], cols: Seq[NCSqlColumn]): Seq[NCSqlColumn] =
        if (cols.nonEmpty)
            cols
        else {
            // val res = tabs.flatMap(_.getColumns.asScala.filter(_.isPk))
            val res = tabs.flatMap(_.getColumns.asScala.filter(_.isPk))

            if (res.isEmpty) tabs.map(_.getColumns.asScala.head) else res
        }

    private def extendColumns(
        cols: Seq[NCSqlColumn], extTabs: Seq[NCSqlTable], freeDateColOpt: Option[NCSqlColumn]
    ): Seq[NCSqlColumn] = {
        var res = (cols ++ freeDateColOpt.toSeq).distinct

        if (res.size < 3)
            res = (res ++
                schemaTabs.filter(extTabs.contains).
                    flatMap(t ⇒ t.getDefaultSelect.asScala.map(col ⇒ schemaCols(Key(t.getTable, col))))).distinct

        res
    }

    private def extendSort(sorts: Seq[NCSqlSort], tables: Seq[NCSqlTable], cols: Seq[NCSqlColumn]): Seq[NCSqlSort] =
        sorts.size match {
            case 0 ⇒
                limit.flatMap(l ⇒ if (cols.contains(l.getColumn)) Some(Seq(limit2Sort(l))) else None).
                    getOrElse({
                        tables.flatMap(_.getDefaultSort.asScala.
                            // 'Sort' can contains columns from select list only (some databases restriction)
                            flatMap(sort ⇒ if (cols.contains(sort.getColumn)) Some(sort) else None)
                        )
                    }).distinct
            case _ ⇒ sorts.distinct
        }

    @throws[RuntimeException]
    private def extendTables(tabs: Seq[NCSqlTable]): Seq[NCSqlTable] = {
        val ext =
            (tabs ++ schemaTabs.filter(tabs.contains).flatMap(_.getExtraTables.asScala.map(schemaTabsByNames))).distinct

        ext.size match {
            case 0 ⇒ throw new RuntimeException("Tables cannot be empty.")
            case 1 ⇒ ext
            case _ ⇒
                // Simple algorithm which takes into account only FKs between tables.
                val extra =
                    ext.sliding(2).flatMap(pair ⇒
                        new DijkstraShortestPath(graph, pair.head.getTable, pair.last.getTable).getPathEdgeList match {
                            case null ⇒ Seq.empty
                            case list ⇒ list.asScala.flatMap(e ⇒ Seq(e.from, e.to))
                        }
                    ).toSeq.distinct

                if (ext.exists(t ⇒ !extra.contains(t.getTable)))
                    throw new RuntimeException(s"Select clause cannot be prepared with given tables set: ${ext.mkString(", ")}")

                extra.map(schemaTabsByNames)
        }
    }

    private def limit2Sort(l: NCSqlLimit): NCSqlSort = NCSqlSortImpl(l.getColumn, l.isAscending)

    private def sort(cols2Sort: Seq[NCSqlColumn], cols: Seq[NCSqlColumn], tabs: Seq[NCSqlTable]): Seq[NCSqlColumn] =
        cols2Sort.sortBy(col ⇒
            if (cols.contains(col))
                0
            else if (tabs.contains(col.getTable))
                1
            else
                2
        )

    private def findDateColumn(initTabs: Seq[NCSqlTable]): Option[NCSqlColumn] =
        freeDateRangeOpt match {
            case Some(_) ⇒
                schemaTabs.sortBy(t ⇒ {
                    // Simple algorithm, which tries to find most suitable date type column for free date condition.
                    // Higher priority for tables which were detected initially.
                    val weight1 = if (initTabs.contains(t)) 0 else 1
                    // Higher priority for tables don't have references from another.
                    // Probably these tables can have more specific records.
                    val weight2 = if (!schemaJoins.exists(_.getToTable == t.getTable)) 0 else 1

                    (weight1, weight2)
                }).flatMap(_.getDefaultDate.asScala).toStream.headOption match {
                    case Some(col) ⇒ Some(col)
                    case None ⇒
                        logger.warn("Free date condition ignored without throwing exceptions.")

                        None
                }
            case None ⇒ None
        }

    private def extendConditions(
        conds: Seq[SqlCondition],
        extTabs: Seq[NCSqlTable],
        initTabs: Seq[NCSqlTable],
        freeDateColOpt: Option[NCSqlColumn]
    ): (Seq[String], Seq[Object]) = {
        val (freeDateConds, freeDateParams): (Seq[SqlCondition], Seq[Object]) =
            freeDateColOpt match {
                case Some(col) ⇒
                    val range = freeDateRangeOpt.getOrElse(throw new AssertionError("Missed date range"))

                    import org.apache.nlpcraft.model.tools.sqlgen.impl.{NCSqlSimpleConditionImpl => C}

                    (Seq(C(col, ">=", range.getFrom), C(col, "<=", range.getTo)), Seq(range.getFrom, range.getTo))
                case None ⇒ (Seq.empty, Seq.empty)
            }

        val etNames = extTabs.map(_.getTable)

        (
            conds.map(sql) ++
            freeDateConds.map(sql) ++
            etNames.flatMap(t ⇒ schemaJoins.filter(j ⇒ j.getFromTable == t && etNames.contains(j.getToTable))).map(sql),
            conds.flatMap(p ⇒
                p match {
                    case x: SqlSimpleCondition ⇒ Seq(x.getValue)
                    case x: SqlInCondition ⇒ x.getValues.asScala
                    case _ ⇒ throw new AssertionError(s"Unexpected condition: $p")
                }
            ) ++ freeDateParams
        )
    }

    def withTables(tables: NCSqlTable*): SqlBuilder = {
        this.tabs ++= tables

        this
    }

    def withColumns(cols: NCSqlColumn*): SqlBuilder = {
        this.cols ++=  cols

        this
    }

    def withAndConditions(conds: SqlCondition*): SqlBuilder = {
        this.conds ++= conds

        this
    }

    def withFreeDateRange(freeDateRange: NCSqlDateRange): SqlBuilder = {
        this.freeDateRangeOpt = Option(freeDateRange)

        this
    }

    def withSorts(sorts: NCSqlSort*): SqlBuilder = {
        this.sorts ++= sorts

        this
    }

    def withLimit(limit: NCSqlLimit): SqlBuilder = {
        this.limit = Option(limit)

        this
    }

    @throws[RuntimeException]
    def build(): SqlQuery = {
        // Collects data.
        if (freeDateRangeOpt.isDefined &&
            this.conds.exists(cond ⇒ isDate(schemaCols(Key(cond.getColumn.getTable, cond.getColumn.getColumn))))
        )
            throw new RuntimeException("Too many date conditions.")

        var tabsNorm = mutable.ArrayBuffer.empty[NCSqlTable] ++ this.tabs
        var colsNorm = mutable.ArrayBuffer.empty[NCSqlColumn] ++ this.cols
        var condsNorm = mutable.ArrayBuffer.empty[SqlCondition] ++ this.conds
        var sortsNorm = mutable.ArrayBuffer.empty[NCSqlSort] ++ this.sorts

        colsNorm.foreach(col ⇒ tabsNorm += schemaTabsByNames(col.getTable))
        condsNorm.foreach(cond ⇒ { tabsNorm += schemaTabsByNames(cond.getColumn.getTable); colsNorm += cond.getColumn })
        sortsNorm.foreach(sort ⇒ { tabsNorm += schemaTabsByNames(sort.getColumn.getTable); colsNorm += sort.getColumn })

        val freeDateColOpt = findDateColumn(tabsNorm)

        freeDateColOpt.toSeq.foreach(col ⇒ { tabsNorm += schemaTabsByNames(col.getTable); colsNorm += col })

        tabsNorm = tabsNorm.distinct
        colsNorm = colsNorm.distinct
        condsNorm = condsNorm.distinct
        sortsNorm = sortsNorm.distinct

        // Extends data.
        case class Ext(
            tables: Seq[NCSqlTable],
            columns: Seq[String],
            groupBy: Seq[NCSqlColumn],
            distinct: Boolean,
            sorts: Seq[NCSqlSort],
            conditions: Seq[String],
            parameters: Seq[Object]
        )

        val ext =
            aggregationOpt match {
                case Some(a) ⇒
                    require(!a.getSelect.isEmpty || !a.getGroupBy.isEmpty)

                    val aggrSelect = a.getSelect.asScala
                    val aggrSelectCols = aggrSelect.map(_.getColumn)

                    // If there aren't 'group by' columns, we try to add grouping by detected 'important' columns.
                    val groupBy: Seq[NCSqlColumn] =
                        if (a.getGroupBy.isEmpty)
                            getImportant(tabsNorm, colsNorm).filter(col ⇒ !aggrSelectCols.contains(col))
                        else
                            a.getGroupBy.asScala

                    val aggrFuncs =
                        if (aggrSelect.isEmpty)
                            getImportant(tabsNorm, colsNorm).
                                filter(p ⇒ !groupBy.contains(p)).
                                map(col ⇒ NCSqlFunctionImpl(col, "COUNT"))
                        else
                            aggrSelect

                    val extTabs = if (tabsNorm.isEmpty) extendTables(tabsNorm) else tabsNorm
                    val (extConds, extParams) = extendConditions(condsNorm, extTabs, tabsNorm, freeDateColOpt)

                    Ext(
                        tables = extTabs,
                        // To simplify, we don't take into account free date here.
                        columns = groupBy.map(sql) ++ aggrFuncs.map(sql),
                        groupBy = groupBy,
                        distinct = false,
                        // Sorts can be only from group by list.
                        sorts = (sortsNorm ++ limit.toSeq.map(limit2Sort)).filter(s ⇒ groupBy.contains(s.getColumn)),
                        conditions = extConds,
                        parameters = extParams
                    )

                case None ⇒
                    val extTabs = extendTables(tabsNorm)
                    val extCols = extendColumns(colsNorm, extTabs, freeDateColOpt)
                    val (extConds, extParams) = extendConditions(condsNorm, extTabs, tabsNorm, freeDateColOpt)

                    Ext(
                        tables = extTabs,
                        columns = sort(extCols, colsNorm, tabsNorm).map(sql),
                        groupBy = Seq.empty,
                        distinct = extCols.forall(col ⇒ isString(schemaCols(Key(col.getTable, col.getColumn)))),
                        sorts = extendSort(sortsNorm, tabsNorm, extCols),
                        conditions = extConds,
                        parameters = extParams
                    )
            }

        new SqlQuery {
            def getSql: String =
                s"""
                   |SELECT
                   |  ${if (ext.distinct) "DISTINCT" else ""}
                   |  ${ext.columns.mkString(", ")}
                   |  FROM ${ext.tables.map(sql).mkString(", ")}
                   |  ${if (ext.conditions.isEmpty) "" else s"WHERE ${ext.conditions.mkString(" AND ")}"}
                   |  ${if (ext.groupBy.isEmpty) "" else s"GROUP BY ${ext.groupBy.map(sql).mkString(", ")}"}
                   |  ${if (ext.sorts.isEmpty) "" else s"ORDER BY ${ext.sorts.map(sql).mkString(", ")}"}
                   |  LIMIT ${limit.flatMap(p ⇒ Some(p.getLimit)).getOrElse(DFLT_LIMIT)}
                   |""".stripMargin.split(" ").map(_.trim).filter(_.nonEmpty).mkString(" ")
            def getParameters: util.List[Object] = ext.parameters.asJava
        }
    }
}