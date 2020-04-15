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

    private def toJson(res: SqlResult, sql: String, params: Seq[Any]): String = {
        val m = new java.util.HashMap[String, Any]()

        m.put("columns", res.columns.asJava)
        m.put("rows", res.rows.map(_.asJava).asJava)
        // Added to result for debug reasons.
        m.put("sql", sql)
        m.put("parameters", params.asJava)

        GSON.toJson(m)
    }

    private def toJson(error: String): String = {
        val m = new java.util.HashMap[String, Any]()

        m.put("error", error)

        GSON.toJson(m)
    }
    
    private def findColumnToken(tok: NCToken): NCToken = {
        val cols = (Seq(tok) ++ tok.findPartTokens().asScala).
            flatMap(p ⇒ if (p.getGroups.contains("column")) Some(p) else None)
        
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

    private def getWithGroup(tok: NCToken, group: String): Seq[NCToken] =
        (Seq(tok) ++ tok.findPartTokens().asScala).flatMap(p ⇒ if (p.getGroups.contains(group)) Some(p) else None)

    private def findAnyColumnTokenOpt(tok: NCToken): Option[NCToken] = {
        val cols = getWithGroup(tok, "column")

        cols.size match {
            case 1 ⇒ Some(cols.head)

            case 0 ⇒ None
            case _ ⇒ throw new IllegalArgumentException(s"Too many columns found for token: $tok")
        }
    }

    private def findAnyColumnToken(tok: NCToken): NCToken =
        findAnyColumnTokenOpt(tok).getOrElse(throw new IllegalArgumentException(s"No columns found for token: $tok"))

    private def extractNumConditions(ext: NCSqlExtractor, colTok: NCToken, numTok: NCToken): Seq[SqlSimpleCondition] = {
        val col = ext.extractColumn(colTok)

        val from: java.lang.Double = numTok.meta("nlpcraft:num:from")
        val fromIncl: Boolean = numTok.meta("nlpcraft:num:fromincl")
        val to: java.lang.Double = numTok.meta("nlpcraft:num:to")
        val toIncl: Boolean = numTok.meta("nlpcraft:num:toincl")

        val isRangeCondition: Boolean = numTok.meta("nlpcraft:num:israngecondition")
        val isEqualCondition: Boolean = numTok.meta("nlpcraft:num:isequalcondition")
        val isNotEqualCondition: Boolean = numTok.meta("nlpcraft:num:isnotequalcondition")
        val isFromNegativeInfinity: Boolean = numTok.meta("nlpcraft:num:isfromnegativeinfinity")
        val isToPositiveInfinity: Boolean = numTok.meta("nlpcraft:num:istopositiveinfinity")

        if (isEqualCondition)
            Seq(SqlSimpleCondition(col, "=", from))
        else if (isNotEqualCondition)
            Seq(SqlSimpleCondition(col, "<>", from))
        else {
            require(isRangeCondition)

            if (isFromNegativeInfinity)
                Seq(SqlSimpleCondition(col, if (fromIncl) "<=" else "<", to))
            else if (isToPositiveInfinity)
                Seq(SqlSimpleCondition(col, if (fromIncl) ">=" else ">", from))
            else
                Seq(
                    SqlSimpleCondition(col, if (fromIncl) ">=" else ">", from),
                    SqlSimpleCondition(col, if (toIncl) "<=" else "<", to)
                )
        }
    }

    def extractDateRangeConditions(ext: NCSqlExtractor, colTok: NCToken, dateTok: NCToken): Seq[SqlSimpleCondition] = {
        val col = ext.extractColumn(colTok)
        val range = ext.extractDateRange(dateTok)

        Seq(SqlSimpleCondition(col, ">=", range.getFrom), SqlSimpleCondition(col, "<=", range.getTo))
    }

    def extractValuesConditions(ext: NCSqlExtractor, allValsToks: Seq[NCToken]): Seq[SqlInCondition] =
        allValsToks.map(tok ⇒ {
            val valToks = (Seq(tok) ++ tok.findPartTokens().asScala).filter(_.getValue != null)

            val valTok =
                valToks.size match {
                    case 1 ⇒ valToks.head

                    case 0 ⇒ throw new IllegalStateException(s"Values column not found for token: $tok")
                    case _ ⇒ throw new IllegalStateException(s"Too many values columns found token: $tok")
                }

            ext.extractColumn(valTok) → valTok.getValue
        }).
            groupBy { case (col, _) ⇒ col }.
            map { case (col, seq) ⇒ SqlInCondition(col, seq.map { case (_, value) ⇒ value})}.toSeq

    @NCIntent(
        "intent=commonReport conv=true " +
        "term(tabs)={groups @@ 'table'}[0,7] " +
        "term(cols)={id == 'col:date' || id == 'col:num' || id == 'col:varchar'}[0,7] " +
        "term(condNums)={id == 'condition:num'}[0,7] " +
        "term(condVals)={id == 'condition:value'}[0,7] " +
        "term(condDates)={id == 'condition:date'}[0,7] " +
        "term(condFreeDate)={id == 'nlpcraft:date'}? " +
         // The simplified version of aggregation wih single function column and one optional group by column.
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
        val ext: NCSqlExtractor = NCSqlExtractorBuilder.build(SCHEMA, ctx.getVariant)

        var query: SqlQuery = null

        try {
            query =
                SqlBuilder(SCHEMA).
                    withTables(tabs.map(ext.extractTable): _*).
                    withColumns(cols.map(col ⇒ ext.extractColumn(findAnyColumnToken(col))): _*).
                    withAndConditions(extractValuesConditions(ext, condVals): _*).
                    withAndConditions(
                        condDates.map(t ⇒ extractColumnAndCondition(t, "nlpcraft:date")).flatMap(h ⇒
                            extractDateRangeConditions(ext, h.column, h.condition)
                        ): _*
                    ).
                    withAndConditions(
                        condNums.map(t ⇒ extractColumnAndCondition(t, "nlpcraft:num")).flatMap(h ⇒
                            extractNumConditions(ext, h.column, h.condition)
                        ): _*
                    ).
                    withSorts((
                        sortTokOpt match {
                            case Some(sortTok) ⇒ ext.extractSort(sortTok).asScala
                            case None ⇒ Seq.empty
                        }
                    ): _*).
                    withLimit(limitTokOpt.flatMap(limitTok ⇒ Some(ext.extractLimit(limitTok))).orNull).
                    withFreeDateRange(freeDateOpt.flatMap(freeDate ⇒ Some(ext.extractDateRange(freeDate))).orNull).
                    build()

            NCResult.json(toJson(SqlAccess.select(query, true), query.sql, query.parameters))
        }
        catch {
            case e: Exception ⇒
                 System.err.println(if (query == null) "Query cannot be prepared" else "Query execution error")

                e.printStackTrace()

                NCResult.json(toJson("Question cannot be answered, reformulate it"))
        }
    }

    override def onMatchedIntent(m: NCIntentMatch): Boolean = {
        val toks = m.getVariant.getMatchedTokens.asScala
        val newToks = toks -- m.getContext.getConversation.getTokens.asScala

        println("toks=" + toks.map(_.origText))
        println("conv=" + m.getContext.getConversation.getTokens.asScala.map(_.origText))
        println("newToks=" + newToks.map(_.origText))

        // Variant doesn't use conversation tokens.
        if (newToks.length == toks.length)
            true
        else {
            def isValue(t: NCToken): Boolean = findAnyColumnTokenOpt(t) match {
                case Some(col) ⇒ col.getValue != null
                case None ⇒ false
            }
            def isColumn(t: NCToken): Boolean = findAnyColumnTokenOpt(t).isDefined
            def isDate(t: NCToken): Boolean = t.getId == "nlpcraft:date"

            // Conversation supported if
            // - all new tokens are values,
            // - all new tokens are columns,
            // - new single token is date.
            // So, this example supports conversation for simple qualifying questions.
            val suitable =
                newToks.forall(isValue) ||
                newToks.forall(isColumn) ||
                newToks.size == 1 && isDate(toks.head)

            if (!suitable) {
                // TODO: drop it.
                if (m.getContext.getVariants.size() == 1)
                    throw new NCRejection("Question cannot be answered")

                logger.info("Conversation reset")

                m.getContext.getConversation.clearAllStm()
            }

            suitable
        }
    }
}