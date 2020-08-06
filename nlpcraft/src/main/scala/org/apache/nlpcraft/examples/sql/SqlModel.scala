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

/**
  * Implementation for the SQL model from `northwind.sql`.
  * <p>
  *  - File `sql_model_init.yaml` was generated from `northwind.sql` (see `model.png` for the visual
  *    representation of this SQL schema) using `NCSqlModelGenerator.scala` utility.
  *  - File `sql_model.yaml` is modified version of the `sql_model_init.yaml` file with additional model elements,
  *    metadata, etc. You can run a diff between these two files to see what changes were made.
  */
class SqlModel extends NCModelFileAdapter("org/apache/nlpcraft/examples/sql/sql_model.yaml") with LazyLogging {
    private final val GSON = new Gson()
    private final val SCHEMA = NCSqlSchemaBuilder.makeSchema(this)

    case class Condition(column: NCToken, condition: NCToken)
    
    /**
      * Converts SQL execution result to JSON.
      *
      * @param res SQL result.
      * @param sql SQL query.
      * @param params SQL query parameters.
      */
    private def toJson(res: SqlResult, sql: String, params: Seq[Any]): String = {
        val m = new java.util.HashMap[String, Any]()

        m.put("columns", res.columns.asJava)
        m.put("rows", res.rows.map(_.asJava).asJava)
        // Added to result for debug reasons.
        m.put("sql", sql)
        m.put("parameters", params.asJava)

        GSON.toJson(m)
    }

    /**
      * Converts execution error to JSON.
      *
      * @param error Error text.
      */
    private def toJson(error: String): String = {
        val m = new java.util.HashMap[String, Any]()

        m.put("error", error)

        GSON.toJson(m)
    }
    
    /**
      * Prepares condition based on token which contains column definition and condition token ID.
      *
      * @param colWrapperTok Token which contains column.
      * @param condTokId Condition token ID.
      */
    private def extractColumnAndCondition(colWrapperTok: NCToken, condTokId: String): Condition = {
        val parts = colWrapperTok.getPartTokens.asScala

        require(parts.size == 2)

        val condTok = parts.find(_.getId == condTokId).get

        val pt = parts.filter(_ != condTok).head
        val colTok = findAnyColumnTokenOpt(pt).getOrElse(throw new RuntimeException(s"No columns found for token: $pt"))

        Condition(colTok, condTok)
    }
    
    /**
      * Tries to find a column definition for given token. It can be token itself or any constituent token of
      * the given token. Returns empty result if column definition not found.
      * Throws exception if found many column definitions in its parts.
      *
      * @param tok Token.
      */
    private def findAnyColumnTokenOpt(tok: NCToken): Option[NCToken] = {
        val cols =
            (Seq(tok) ++ tok.findPartTokens().asScala).
                flatMap(p ⇒ if (p.getGroups.contains("column")) Some(p) else None)

        cols.size match {
            case 1 ⇒ Some(cols.head)
            case 0 ⇒ None

            case _ ⇒ throw new RuntimeException(s"Too many columns found for token: $tok")
        }
    }
    
    /**
      * Find any first column token in the given token or its constittuent tokens.
      *
      * @param tok Token.
      * @return Column token or throws exception.
      */
    private def findAnyColumnToken(tok: NCToken): NCToken =
        findAnyColumnTokenOpt(tok).getOrElse(throw new RuntimeException(s"No columns found for token: $tok"))
    
    /**
      * Extracts numeric conditions. It creates conditions based on relations between column and numeric value.
      * Single value conditions list returned for single relation like 'col > 2' or 'col <= 3'.
      * Double values conditions list returned for range relation like 'col > 2 AND col <= 3'.
      *
      * @param ext SQL extractor.
      * @param colTok Column token.
      * @param numTok Numeric token.
      */
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
    
    /**
      * Extracts date range conditions. It creates conditions based on relations between column and date value.
      * It always returns double values conditions list because date condition processed as a range.
      *
      * @param ext SQL extractor.
      * @param colTok Column token.
      * @param dateTok Date token.
      */
    def extractDateRangeConditions(ext: NCSqlExtractor, colTok: NCToken, dateTok: NCToken): Seq[SqlSimpleCondition] = {
        val col = ext.extractColumn(colTok)
        val range = ext.extractDateRange(dateTok)

        Seq(SqlSimpleCondition(col, ">=", range.getFrom), SqlSimpleCondition(col, "<=", range.getTo))
    }
    
    /**
      * Extracts 'values' conditions. It creates conditions based on relations between token values which
      * extracted from given tokens and their column. These conditions grouped by columns.
      *
      * @param ext SQL extractor.
      * @param allValsToks Values tokens.
      */
    def extractValuesConditions(ext: NCSqlExtractor, allValsToks: Seq[NCToken]): Seq[SqlInCondition] =
        allValsToks.map(tok ⇒ {
            val valToks = (Seq(tok) ++ tok.findPartTokens().asScala).filter(_.getValue != null)

            val valTok =
                valToks.size match {
                    case 1 ⇒ valToks.head

                    case 0 ⇒ throw new RuntimeException(s"Values column not found for token: $tok")
                    case _ ⇒ throw new RuntimeException(s"Too many values columns found token: $tok")
                }

            ext.extractColumn(valTok) → valTok.getValue
        }).
            groupBy { case (col, _) ⇒ col }.
            map { case (col, seq) ⇒ SqlInCondition(col, seq.map { case (_, value) ⇒ value})}.toSeq
    
    /**
      * Creates and executes SQL request by given parameters.
      *
      * @param ext SQL extractor.
      * @param tabs Tables tokens.
      * @param cols Columns tokens.
      * @param condNums Numeric condition tokens.
      * @param condVals Values conditions tokens.
      * @param condDates Date range condition tokens.
      * @param freeDateOpt Free date token. Optional.
      * @param limitTokOpt Limit token. Optional.
      * @param sorts Sorts tokens.
      */
    protected def select0(
        ext: NCSqlExtractor,
        tabs: Seq[NCToken],
        cols: Seq[NCToken],
        condNums: Seq[NCToken],
        condVals: Seq[NCToken],
        condDates: Seq[NCToken],
        freeDateOpt: Option[NCToken],
        limitTokOpt: Option[NCToken],
        sorts: Seq[NCSqlSort]
    ): NCResult = {
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
                    withSorts(sorts: _*).
                    withLimit(limitTokOpt.flatMap(limitTok ⇒ Some(ext.extractLimit(limitTok))).orNull).
                    withFreeDateRange(freeDateOpt.flatMap(freeDate ⇒ Some(ext.extractDateRange(freeDate))).orNull).
                    build()

            NCResult.json(toJson(SqlAccess.select(query, logResult = true), query.sql, query.parameters))
        }
        catch {
            case e: Exception ⇒
                System.err.println(if (query == null) "Query cannot be prepared." else "Query execution error.")

                e.printStackTrace()

                NCResult.json(toJson("Question cannot be answered as is."))
        }
    }

    /**
      * Main generic intent which allows to process set of common questions.
      * It processes questions which contains tables and column names, column values and some conditions for table columns.
      *
      * @param ctx Intent matching context.
      * @param tbls Tables tokens. Can be empty.
      * @param cols Columns tokens. Can be empty.
      * @param condNums Numeric condition tokens. Can be empty.
      * @param condVals Values conditions tokens. Can be empty.
      * @param condDates Date range condition tokens. Can be empty.
      * @param freeDateOpt Free date token. Optional. Special case.
      *    Free date detected in sentence is not related to cny concrete table column and relation set programmatically
      *    based on model configuration.
      * @param sortTokOpt Sort token. Optional.
      * @param limitTokOpt Limit token. Optional.
      */
    @NCIntent(
        "intent=commonReport conv=true " +
        "term(tbls)={groups @@ 'table'}[0,7] " +
        "term(cols)={id == 'col:date' || id == 'col:num' || id == 'col:varchar'}[0,7] " +
        "term(condNums)={id == 'condition:num'}[0,7] " +
        "term(condVals)={id == 'condition:value'}[0,7] " +
        "term(condDates)={id == 'condition:date'}[0,7] " +
        "term(condFreeDate)={id == 'nlpcraft:date'}? " +
        "term(sort)={id == 'nlpcraft:sort'}? " +
        "term(limit)={id == 'nlpcraft:limit'}?"
    )
    @NCIntentSample(Array(
        "order date, please!",
        "show me the order dates",
        "list dates of orders"
    ))
    def onCommonReport(
        ctx: NCIntentMatch,
        @NCIntentTerm("tbls") tbls: Seq[NCToken],
        @NCIntentTerm("cols") cols: Seq[NCToken],
        @NCIntentTerm("condNums") condNums: Seq[NCToken],
        @NCIntentTerm("condVals") condVals: Seq[NCToken],
        @NCIntentTerm("condDates") condDates: Seq[NCToken],
        @NCIntentTerm("condFreeDate") freeDateOpt: Option[NCToken],
        @NCIntentTerm("sort") sortTokOpt: Option[NCToken],
        @NCIntentTerm("limit") limitTokOpt: Option[NCToken]
    ): NCResult = {
        val ext: NCSqlExtractor = NCSqlExtractorBuilder.build(SCHEMA, ctx.getVariant)

        select0(
            NCSqlExtractorBuilder.build(SCHEMA, ctx.getVariant),
            tbls,
            cols,
            condNums,
            condVals,
            condDates,
            freeDateOpt,
            limitTokOpt,
            sortTokOpt match {
                case Some(sortTok) ⇒ ext.extractSort(sortTok).asScala
                case None ⇒ Seq.empty
            }
        )
    }
    
    /**
      * Modified version of main generic intent, which uses implicit sort element definition.
      * It is developed as an example of way by which this model can be extended to support more
      * complicated questions comparing to a generic case.
      *
      * @param ctx Intent matching context.
      * @param sortTok Sort token. Mandatory. it is defined via user elements 'sort:best' or 'sort:worst'.
      * @param tbls Tables tokens. Can be empty.
      * @param cols Columns tokens. Can be empty.
      * @param condNums Numeric condition tokens. Can be empty.
      * @param condVals Values conditions tokens. Can be empty.
      * @param condDates Date range condition tokens. Can be empty.
      * @param freeDateOpt Free date token. Optional. Special case.
      *    Free date detected in sentence is not related to cny concrete table column and relation set programmatically
      *    based on model configuration.
      * @param limitTokOpt Limit token. Optional.
      */
    @NCIntent(
        "intent=customSortReport conv=true " +
        "term(sort)={id == 'sort:best' || id == 'sort:worst'} " +
        "term(tbls)={groups @@ 'table'}[0,7] " +
        "term(cols)={id == 'col:date' || id == 'col:num' || id == 'col:varchar'}[0,7] " +
        "term(condNums)={id == 'condition:num'}[0,7] " +
        "term(condVals)={id == 'condition:value'}[0,7] " +
        "term(condDates)={id == 'condition:date'}[0,7] " +
        "term(condFreeDate)={id == 'nlpcraft:date'}? " +
        "term(limit)={id == 'nlpcraft:limit'}?"
    )
    @NCIntentSample(Array(
        "What are the least performing categories for the last quarter?"
    ))
    def onCustomSortReport(
        ctx: NCIntentMatch,
        @NCIntentTerm("sort") sortTok: NCToken,
        @NCIntentTerm("tbls") tbls: Seq[NCToken],
        @NCIntentTerm("cols") cols: Seq[NCToken],
        @NCIntentTerm("condNums") condNums: Seq[NCToken],
        @NCIntentTerm("condVals") condVals: Seq[NCToken],
        @NCIntentTerm("condDates") condDates: Seq[NCToken],
        @NCIntentTerm("condFreeDate") freeDateOpt: Option[NCToken],
        @NCIntentTerm("limit") limitTokOpt: Option[NCToken]
    ): NCResult = {
        val ordersFreightColSort: NCSqlSort =
            new NCSqlSort {
                override def getColumn: NCSqlColumn = SCHEMA.getTables.asScala.find(_.getTable == "orders").
                    getOrElse(throw new RuntimeException(s"Table `orders` not found.")).
                    getColumns.asScala.find(_.getColumn == "freight").
                    getOrElse(throw new RuntimeException(s"Column `orders.freight` not found."))
                override def isAscending: Boolean =
                    sortTok.getId match {
                        case "sort:best" ⇒ false
                        case "sort:worst" ⇒ true

                        case  _ ⇒ throw new AssertionError(s"Unexpected ID: ${sortTok.getId}")
                    }

                    if (sortTok.getId == "sort:best") false else true
            }

        select0(
            NCSqlExtractorBuilder.build(SCHEMA, ctx.getVariant),
            tbls,
            cols,
            condNums,
            condVals,
            condDates,
            freeDateOpt,
            limitTokOpt,
            Seq(ordersFreightColSort)
        )
    }
    
    /**
      * Custom callback implementation. See `NCModel.onMatchedIntent` method documentation for more details.
      * <p>
      * This callback allows to clear conversation context. In this implementation the conversation context is always
      * cleared between user questions, except for the obvious clarifying questions. We assume that question is being
      * clarified if its tokens satisfy one of criteria:
      *  - all these tokens are values (What about 'Exotic Liquids')
      *  - all these tokens are columns (Give me 'last name')
      *  - new token is single date token (What about 'tomorrow')
      *  <p>
      *  If new sentence tokens satisfied any of these criteria,
      *  conversation context between this and previous questions will not be cleared.
      */
    override def onMatchedIntent(m: NCIntentMatch): Boolean = {
        val toks = m.getVariant.getMatchedTokens.asScala
        val intentConvToks = m.getIntentTokens.asScala.flatMap(_.asScala) -- toks

        // Variant doesn't use tokens from the conversation context (STM).
        if (intentConvToks.isEmpty)
            true
        else {
            def isValue(t: NCToken): Boolean = findAnyColumnTokenOpt(t) match {
                case Some(col) ⇒ col.getValue != null
                case None ⇒ false
            }
            def isColumn(t: NCToken): Boolean = findAnyColumnTokenOpt(t).isDefined
            def isDate(t: NCToken): Boolean = t.getId == "nlpcraft:date"

            val ok = toks.forall(isValue) || toks.forall(isColumn) || toks.size == 1 && isDate(toks.head)

            if (!ok) {
                m.getContext.getConversation.clearAllStm()

                logger.info("Conversation reset, trying without conversation.")
            }

            ok
        }
    }
}