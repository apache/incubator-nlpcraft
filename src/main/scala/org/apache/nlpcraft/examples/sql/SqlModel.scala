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

package org.apache.nlpcraft.examples.sql

import com.google.gson.Gson
import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.examples.sql.db._
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.tools.sqlgen._

import scala.collection.JavaConverters._
import scala.language.implicitConversions

class SqlModel extends NCModelFileAdapter("org/apache/nlpcraft/examples/sql/sql_model.yaml") with LazyLogging {
    private final val GSON = new Gson()
    private final val SCHEMA = NCSqlSchemaBuilder.makeSchema(this)

    case class Condition(column: NCToken, condition: NCToken)

    private def toJson(res: SqlResult, sql: String, params: java.util.List[Object]): String = {
        val m = new java.util.HashMap[String, Any]()

        m.put("columns", res.columns.asJava)
        m.put("rows", res.rows.map(_.asJava).asJava)
        // Added to result for debug reasons.
        m.put("sql", sql)
        m.put("parameters", params)

        GSON.toJson(m)
    }

    private def toJson(error: String): String = {
        val m = new java.util.HashMap[String, Any]()

        m.put("error", error)

        GSON.toJson(m)
    }
    
    private def findColumnToken(tok: NCToken): NCToken = {
        val cols = (Seq(tok) ++ tok.findPartTokens().asScala).flatMap(p ⇒ if (p.getGroups.contains("column")) Some(p) else None)
        
        cols.size match {
            case 1 ⇒ cols.head
            case 0 ⇒ throw new Exception(s"No columns found for token: $tok")
            case _ ⇒ throw new Exception("Too many columns found for token: $tok")
        }
    }
    
    /**
      * Complex element contains 2 tokens: column + date ot numeric condition.
      *
      * @param t
      * @param condTokId
      * @return
      */
    private def extractColumnAndCondition(t: NCToken, condTokId: String): Condition = {
        val parts = t.getPartTokens.asScala

        require(parts.size == 2)

        val condTok = parts.find(_.getId == condTokId).get
        val colTok = findColumnToken(parts.filter(_ != condTok).head)

        Condition(colTok, condTok)
    }

    @NCIntent(
        "intent=commonReport conv=true " +
        "term(tabs)={groups @@ 'table'}[0,7] " +
        "term(cols)={id == 'col:date' || id == 'col:num' || id == 'col:varchar'}[0,7] " +
        "term(condNums)={id == 'condition:num'}[0,7] " +
        "term(condVals)={id == 'condition:value'}[0,7] " +
        "term(condDates)={id == 'condition:date'}[0,7] " +
        "term(condFreeDate)={id == 'nlpcraft:date'}? " +
         // Simplified version of aggregation wih single function column and one optional group by column.
        "term(sort)={id == 'nlpcraft:sort'}? " +
        "term(limit)={id == 'nlpcraft:limit'}?"
    )
    def onCommonReport(
        ctx: NCIntentMatch,
        @NCIntentTerm("tabs") tabs: Seq[NCToken],
        @NCIntentTerm("cols") cols: Seq[NCToken],
        @NCIntentTerm("condNums") condNums: Seq[NCToken],
        @NCIntentTerm("condVals") condVals: Seq[NCToken],
        @NCIntentTerm("condDates") condDates: Seq[NCToken],
        @NCIntentTerm("condFreeDate") freeDateOpt: Option[NCToken],
        @NCIntentTerm("sort") sortTokOpt: Option[NCToken],
        @NCIntentTerm("limit") limitTokOpt: Option[NCToken]
    ): NCResult = {
        val ext = NCSqlExtractorBuilder.build(SCHEMA, ctx.getVariant)

        var query: SqlQuery = null

        try {
            query =
                SqlBuilder(SCHEMA).
                    withTables(tabs.map(ext.extractTable): _*).
                    withColumns(cols.map(ext.extractColumn): _*).
                    withAndConditions(ext.extractInConditions(condVals: _*).asScala: _*).
                    withAndConditions(
                        condDates.map(t ⇒ extractColumnAndCondition(t, "nlpcraft:date")).flatMap(h ⇒
                            ext.extractDateRangeConditions(h.column, h.condition).asScala
                        ): _*
                    ).
                    withAndConditions(
                        condNums.map(t ⇒ extractColumnAndCondition(t, "nlpcraft:num")).flatMap(h ⇒
                            ext.extractNumConditions(h.column, h.condition).asScala
                        ): _*
                    ).
                    withSorts(sortTokOpt.map(sortTok ⇒ ext.extractSort(sortTok)).toSeq: _*).
                    withLimit(limitTokOpt.flatMap(limitTok ⇒ Some(ext.extractLimit(limitTok))).orNull).
                    withFreeDateRange(freeDateOpt.flatMap(freeDate ⇒ Some(ext.extractDateRange(freeDate))).orNull).
                    build()

            NCResult.json(toJson(SqlAccess.select(query, true), query.getSql, query.getParameters))
        }
        catch {
            case e: Exception ⇒
                 System.err.println(if (query == null) "Query cannot be prepared" else "Query execution error")

                e.printStackTrace()

                NCResult.json(toJson("Question cannot be answered, reformulate it"))
        }
    }
}