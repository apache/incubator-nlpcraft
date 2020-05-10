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

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.model.tools.sqlgen.NCSqlJoinType._
import org.apache.nlpcraft.model.tools.sqlgen._
import org.apache.nlpcraft.model.tools.sqlgen.impl.NCSqlSortImpl
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.{DefaultEdge, SimpleGraph}

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

    private val schemaPaths = {
        val g = new SimpleGraph[String, Edge](classOf[Edge])

        schemaTabs.foreach(t ⇒ g.addVertex(t.getTable))
        schemaJoins.foreach(j ⇒ g.addEdge(j.getFromTable, j.getToTable, Edge(j.getFromTable, j.getToTable)))

        new DijkstraShortestPath(g)
    }

    private var tabs: Seq[NCSqlTable] = Seq.empty
    private var cols: Seq[NCSqlColumn] = Seq.empty
    private var conds: Seq[SqlCondition] = Seq.empty
    private var sorts: Seq[NCSqlSort] = Seq.empty
    private var freeDateRangeOpt: Option[NCSqlDateRange] = None
    private var limit: Option[NCSqlLimit] = None

    private def sql(clause: NCSqlJoinType): String =
        clause match {
            case INNER ⇒ "INNER JOIN"
            case LEFT ⇒ "LEFT JOIN"
            case RIGHT ⇒ "RIGHT JOIN"

            case OUTER ⇒ throw new AssertionError(s"Unsupported join type: $clause")

            case _ ⇒ throw new AssertionError(s"Unexpected join type: $clause")
        }

    private def sql(tabs: Seq[NCSqlTable]): String = {
        val names = tabs.map(_.getTable)

        names.size match {
            case 0 ⇒ throw new AssertionError(s"Unexpected empty tables")
            case 1 ⇒ names.head
            case _ ⇒
                val used = mutable.HashSet.empty[String]

                val refs = names.
                    flatMap(t ⇒ schemaJoins.filter(j ⇒ j.getFromTable == t && names.contains(j.getToTable))).
                    sortBy(j ⇒ Math.min(names.indexOf(j.getFromTable), names.indexOf(j.getToTable))).
                    zipWithIndex.
                    map { case (join, idx) ⇒
                        val fromCols = join.getFromColumns.asScala
                        val toCols = join.getToColumns.asScala

                        require(fromCols.nonEmpty)
                        require(fromCols.size == toCols.size)

                        val fromTab = join.getFromTable
                        val toTab = join.getToTable

                        val onCondition = fromCols.zip(toCols).
                            map { case (fromCol, toCol) ⇒ s"$fromTab.$fromCol = $toTab.$toCol" }.mkString(" AND ")

                        if (idx == 0) {
                            used += fromTab
                            used += toTab

                            s"$fromTab ${sql(join.getType)} $toTab ON $onCondition"
                        }
                        else {
                            val fromAdded = used.add(fromTab)
                            val toAdded = used.add(toTab)

                            if (fromAdded) {
                                val reverseType =
                                    join.getType match {
                                        case LEFT ⇒ RIGHT
                                        case RIGHT ⇒ LEFT
                                        case _ ⇒ join.getType
                                    }
                                s"${sql(reverseType)} $fromTab ON $onCondition"
                            }
                            else if (toAdded)
                                s"${sql(join.getType)} $toTab ON $onCondition"
                            else
                                ""
                        }
                    }

                if (refs.length != names.length - 1)
                    throw new RuntimeException(s"Tables cannot be joined: ${names.mkString(", ")}")

                 refs.mkString(" ")
        }
    }
    private def sql(clause: NCSqlColumn): String = s"${clause.getTable}.${clause.getColumn}"
    private def sql(clause: NCSqlSort): String = s"${sql(clause.getColumn)} ${if (clause.isAscending) "ASC" else "DESC"}"

    private def sql(clause: SqlInCondition): String =
        s"${sql(clause.column)} IN (${clause.values.indices.map(_ ⇒ "?").mkString(",")})"

    private def sql(clause: SqlSimpleCondition): String = s"${sql(clause.column)} ${clause.operation} ?"

    private def sql(clause: SqlCondition): String =
        clause match {
            case x: SqlSimpleCondition ⇒ sql(x)
            case x: SqlInCondition ⇒ sql(x)

            case _ ⇒ throw new AssertionError(s"Unexpected condition: $clause")
        }

    private def isDate(col: NCSqlColumn): Boolean = col.getDataType == Types.DATE
    private def isString(col: NCSqlColumn): Boolean = col.getDataType == Types.VARCHAR

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
                    getOrElse(
                        tables.flatMap(_.getDefaultSort.asScala.
                            // 'Sort' can contain columns from select list only (some databases restriction)
                            flatMap(sort ⇒ if (cols.contains(sort.getColumn)) Some(sort) else None)
                        )
                    ).distinct
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
                // The simple algorithm, which takes into account only FKs between tables.
                val extra =
                    ext.combinations(2).flatMap(pair ⇒
                        schemaPaths.getPath(pair.head.getTable, pair.last.getTable) match {
                            case null ⇒ Seq.empty
                            case list ⇒ list.getEdgeList.asScala.flatMap(e ⇒ Seq(e.from, e.to))
                        }
                    ).toSeq.distinct.map(schemaTabsByNames)

                if (ext.exists(t ⇒ !extra.contains(t)))
                    throw new RuntimeException(
                        s"Select clause cannot be prepared with given tables set: " +
                        s"${ext.map(_.getTable).mkString(", ")}"
                    )

                extra
        }
    }

    private def limit2Sort(l: NCSqlLimit): NCSqlSort = NCSqlSortImpl(l.getColumn, l.isAscending)

    private def sort(cols2Sort: Seq[NCSqlColumn], cols: Seq[NCSqlColumn], tabs: Seq[NCSqlTable]): Seq[NCSqlColumn] =
        cols2Sort.sortBy(col ⇒
            if (cols.contains(col))
                0
            else if (tabs.contains(schemaTabsByNames(col.getTable)))
                1
            else
                2
        )

    private def findDateColumn(initTabs: Seq[NCSqlTable]): Option[NCSqlColumn] =
        schemaTabs.sortBy(t ⇒ {
            // The simple algorithm, which tries to find most suitable date type column for free date condition.
            // Higher priority for tables which were detected initially.
            val weight1 = if (initTabs.contains(t)) 0 else 1
            // Higher priority for tables which don't have references from another.
            // Probably these tables can have more specific records.
            val weight2 = if (!schemaJoins.exists(_.getToTable == t.getTable)) 0 else 1

            (weight1, weight2)
        }).flatMap(_.getDefaultDate.asScala).toStream.headOption match {
            case Some(col) ⇒ Some(col)
            case None ⇒
                logger.warn("Free date condition ignored without throwing exceptions.")

                None
        }

    private def extendConditions(
        conds: Seq[SqlCondition],
        extTabs: Seq[NCSqlTable],
        initTabs: Seq[NCSqlTable],
        freeDateColOpt: Option[NCSqlColumn]
    ): (Seq[String], Seq[Any]) = {
        val (freeDateConds, freeDateParams): (Seq[SqlCondition], Seq[Object]) =
            freeDateColOpt match {
                case Some(col) ⇒
                    val range = freeDateRangeOpt.getOrElse(throw new AssertionError("Missed date range"))

                    import org.apache.nlpcraft.examples.sql.db.{SqlSimpleCondition => C}

                    (Seq(C(col, ">=", range.getFrom), C(col, "<=", range.getTo)), Seq(range.getFrom, range.getTo))
                case None ⇒ (Seq.empty, Seq.empty)
            }

        (
            conds.map(sql) ++ freeDateConds.map(sql),
            conds.flatMap(p ⇒
                p match {
                    case x: SqlSimpleCondition ⇒ Seq(x.value)
                    case x: SqlInCondition ⇒ x.values
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
            this.conds.exists(cond ⇒ isDate(schemaCols(Key(cond.column.getTable, cond.column.getColumn))))
        )
            throw new RuntimeException("Too many date conditions.")

        var tabsNorm = mutable.ArrayBuffer.empty[NCSqlTable] ++ this.tabs
        var colsNorm = mutable.ArrayBuffer.empty[NCSqlColumn] ++ this.cols
        var condsNorm = mutable.ArrayBuffer.empty[SqlCondition] ++ this.conds
        var sortsNorm = mutable.ArrayBuffer.empty[NCSqlSort] ++ this.sorts

        colsNorm.foreach(col ⇒ tabsNorm += schemaTabsByNames(col.getTable))
        condsNorm.foreach(cond ⇒ { tabsNorm += schemaTabsByNames(cond.column.getTable); colsNorm += cond.column })
        sortsNorm.foreach(sort ⇒ { tabsNorm += schemaTabsByNames(sort.getColumn.getTable); colsNorm += sort.getColumn })

        var freeDateColOpt =
            if (freeDateRangeOpt.isDefined) tabsNorm.flatMap(_.getDefaultDate.asScala).toStream.headOption else None

        tabsNorm = tabsNorm.distinct

        val extTabs = extendTables(tabsNorm)

        if (freeDateColOpt.isEmpty && freeDateRangeOpt.isDefined) {
            freeDateColOpt = findDateColumn(tabsNorm)

            freeDateColOpt.toSeq.foreach(col ⇒ { tabsNorm += schemaTabsByNames(col.getTable); colsNorm += col })

            tabsNorm = tabsNorm.distinct
        }

        colsNorm = colsNorm.distinct
        condsNorm = condsNorm.distinct
        sortsNorm = sortsNorm.distinct

        val extCols = extendColumns(colsNorm, extTabs, freeDateColOpt)
        val (extConds, extParams) = extendConditions(condsNorm, extTabs, tabsNorm, freeDateColOpt)

        val extSortCols = sort(extCols, colsNorm, tabsNorm).map(sql)
        val distinct = extCols.forall(col ⇒ isString(schemaCols(Key(col.getTable, col.getColumn))))
        val extSorts = extendSort(sortsNorm, tabsNorm, extCols)

        SqlQuery(
            sql =
                s"""
                   |SELECT
                   |  ${if (distinct) "DISTINCT" else ""}
                   |  ${extSortCols.mkString(", ")}
                   |  FROM ${sql(extTabs)}
                   |  ${if (extConds.isEmpty) "" else s"WHERE ${extConds.mkString(" AND ")}"}
                   |  ${if (extSorts.isEmpty) "" else s"ORDER BY ${extSorts.map(sql).mkString(", ")}"}
                   |  LIMIT ${limit.flatMap(p ⇒ Some(p.getLimit)).getOrElse(DFLT_LIMIT)}
                   |""".stripMargin.split(" ").map(_.trim).filter(_.nonEmpty).mkString(" "),
            parameters = extParams
        )
    }
}