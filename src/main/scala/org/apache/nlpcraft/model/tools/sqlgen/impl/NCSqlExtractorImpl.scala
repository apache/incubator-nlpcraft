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

import org.apache.nlpcraft.common.NCException
import org.apache.nlpcraft.model.tools.sqlgen._
import org.apache.nlpcraft.model._

import scala.collection.JavaConverters._

/**
  *
  */
class NCSqlExtractorImpl(schema: NCSqlSchema, variant: NCVariant) extends NCSqlExtractor {
    require(schema != null)
    require(variant != null)
    
    /**
      *
      * @param tok
      * @param id
      */
    private def checkTokenId(tok: NCToken, id: String): Unit =
        if (tok.getId != id)
            throw new NCException(s"Expected token ID '$id' but got: $tok")
    
    /**
      *
      * @param tok
      * @param grp
      */
    private def checkGroup(tok: NCToken, grp: String): Unit =
        if (!tok.isOfGroup(grp))
            throw new NCException(s"Token does not belong to the group '$grp': $tok")
    
    /**
      *
      * @param name
      * @return
      */
    private def findSchemaTable(name: String): NCSqlTable =
        schema.getTables.asScala.find(_.getTable == name).getOrElse(throw new NCException(s"Table not found: $name"))
    
    /**
      *
      * @param cols
      * @param tbl
      * @param col
      * @return
      */
    private def findSchemaColumn(cols: util.List[NCSqlColumn], tbl: String, col: String): NCSqlColumn =
        cols.asScala.find(_.getColumn == col).getOrElse(throw new NCException(s"Column not found: $tbl.$col"))
    
    /**
      *
      * @param tok
      * @param group
      * @return
      */
    private def getWithGroup(tok: NCToken, group: String): Seq[NCToken] =
        (Seq(tok) ++ tok.findPartTokens().asScala).flatMap(p ⇒ if (p.getGroups.contains(group)) Some(p) else None)
    
    /**
      *
      * @param tok
      * @return
      */
    private def findAnyTableTokenOpt(tok: NCToken): Option[NCSqlTable] = {
        val tabs = getWithGroup(tok, "table")
        
        tabs.size match {
            case 1 ⇒ Some(findSchemaTable(tabs.head.metax("sql:name")))
            case 0 ⇒ None
            case _ ⇒ throw new NCException(s"Too many tables found for: $tok")
        }
    }
    
    /**
      * 
      * @param tok
      * @return
      */
    private def findAnyColumnTokenOpt(tok: NCToken): Option[NCToken] = {
        val cols = getWithGroup(tok, "column")
        
        cols.size match {
            case 1 ⇒ Some(cols.head)
            case 0 ⇒ None
            case _ ⇒ throw new NCException(s"Too many columns found for token: $tok")
        }
    }

    /**
      *
      * @param refTok
      * @return
      */
    private def getReference(refTok: NCToken): Option[NCSqlColumn] = {
        val tok = getLinkBySingleIndex(variant.asScala, refTok)
        
        findAnyColumnTokenOpt(tok) match {
            // If reference is column - sort by column.
            case Some(t) ⇒ Some(extractColumn(t))
            case None ⇒
                // If reference is table -  sort by any PK column of table.
                findAnyTableTokenOpt(tok) match {
                    case Some(tab) ⇒ Some(tab.getColumns.asScala.minBy(col ⇒ if (col.isPk) 0 else 1))
                    case None ⇒ None
                }
        }
    }
    
    /**
      *
      * @param tok
      * @return
      */
    private def findAnyColumnToken(tok: NCToken): NCToken =
        findAnyColumnTokenOpt(tok).getOrElse(throw new NCException(s"No columns found for token: $tok"))
    
    /**
      *
      * @param limitTok
      * @return
      */
    override def extractLimit(limitTok: NCToken): NCSqlLimit = {
        checkTokenId(limitTok, "nlpcraft:limit")
        
        // Skips indexes to simplify.
        val limit: Double = limitTok.metax("nlpcraft:limit:limit")
        
        NCSqlLimitImpl(
            getReference(limitTok).getOrElse(throw new NCException(s"Limit not found for: $limitTok")),
            limit.intValue(),
            limitTok.metax("nlpcraft:limit:asc")
        )
    }
    
    /**
      *
      * @param colTok
      * @param dateTok
      * @return
      */
    override def extractDateRangeConditions(colTok: NCToken, dateTok: NCToken): util.List[NCSqlSimpleCondition] = {
        checkTokenId(dateTok, "nlpcraft:date")
        checkGroup(colTok, "column")
        
        val col = extractColumn(colTok)
        val range = extractDateRange(dateTok)
        
        val seq: Seq[NCSqlSimpleCondition] = Seq(
            NCSqlSimpleConditionImpl(col, ">=", range.getFrom),
            NCSqlSimpleConditionImpl(col, "<=", range.getTo)
        )
        
        seq.asJava
    }
    
    /**
      *
      * @param colTok
      * @param numTok
      * @return
      */
    override def extractNumConditions(colTok: NCToken, numTok: NCToken): util.List[NCSqlSimpleCondition] = {
        checkTokenId(numTok, "nlpcraft:num")
        checkGroup(colTok, "column")

        val col = extractColumn(colTok)
        
        val from: java.lang.Double = numTok.metax("nlpcraft:num:from")
        val fromIncl: Boolean = numTok.metax("nlpcraft:num:fromincl")
        val to: java.lang.Double = numTok.metax("nlpcraft:num:to")
        val toIncl: Boolean = numTok.metax("nlpcraft:num:toincl")
        
        val isRangeCondition: Boolean = numTok.metax("nlpcraft:num:israngecondition")
        val isEqualCondition: Boolean = numTok.metax("nlpcraft:num:isequalcondition")
        val isNotEqualCondition: Boolean = numTok.metax("nlpcraft:num:isnotequalcondition")
        val isFromNegativeInfinity: Boolean = numTok.metax("nlpcraft:num:isfromnegativeinfinity")
        val isToPositiveInfinity: Boolean = numTok.metax("nlpcraft:num:istopositiveinfinity")
        
        val seq: Seq[NCSqlSimpleCondition] =
            if (isEqualCondition)
                Seq(NCSqlSimpleConditionImpl(col, "=", from))
            else if (isNotEqualCondition)
                Seq(NCSqlSimpleConditionImpl(col, "<>", from))
            else {
                require(isRangeCondition)
                
                if (isFromNegativeInfinity)
                    Seq(NCSqlSimpleConditionImpl(col, if (fromIncl) "<=" else "<", to))
                else if (isToPositiveInfinity)
                    Seq(NCSqlSimpleConditionImpl(col, if (fromIncl) ">=" else ">", from))
                else
                    Seq(
                        NCSqlSimpleConditionImpl(col, if (fromIncl) ">=" else ">", from),
                        NCSqlSimpleConditionImpl(col, if (toIncl) "<=" else "<", to)
                    )
            }
        
        seq.asJava
    }
    
    /**
      *
      * @param allValsToks
      * @return
      */
    override def extractInConditions(allValsToks: NCToken*): util.List[NCSqlInCondition] = {
        allValsToks.map(tok ⇒ {
            val valToks = (Seq(tok) ++ tok.findPartTokens().asScala).filter(_.getValue != null)
            
            val valTok =
                valToks.size match {
                    case 1 ⇒ valToks.head
                    
                    case 0 ⇒ throw new NCException(s"Values column not found for token: $tok")
                    case _ ⇒ throw new NCException(s"Too many values columns found token: $tok")
                }
            
            extractColumn(valTok) → valTok.getValue
        })
        .groupBy { case (col, _) ⇒ col }.map { case (col, seq) ⇒
            NCSqlInConditionImpl(col, seq.map {
                case (_, value) ⇒ value
            }).asInstanceOf[NCSqlInCondition]
        }
        .toSeq
        .asJava
    }
    
    /**
      *
      * @param sortTok
      * @return
      */
    override def extractSort(sortTok: NCToken): NCSqlSort = {
        checkTokenId(sortTok, "nlpcraft:sort")
        
        NCSqlSortImpl(
            getReference(sortTok).getOrElse(throw new NCException(s"Sort not found for: $sortTok")),
            sortTok.metax("nlpcraft:sort:asc")
        )
    }
    
    /**
      *
      * @param aggrFun
      * @param aggrGrpTok
      * @return
      */
    override def extractAggregate(aggrFun: NCToken, aggrGrpTok: NCToken): NCSqlAggregate = {
        if (aggrFun != null)
            checkTokenId(aggrFun, "nlpcraft:aggregation")
        
        val select = aggrFun match {
            case null ⇒ Seq.empty
            case _ ⇒
                Seq(
                    NCSqlFunctionImpl(
                        extractColumn(findAnyColumnToken(getLinkBySingleIndex(variant.asScala, aggrFun))),
                        aggrFun.meta(s"${aggrFun.getId}:type")
                    )
                )
        
        }
    
        val grpBy = aggrGrpTok match {
            case null ⇒ Seq.empty
            case aggrGrp ⇒
                val grpTok = getLinkBySingleIndex(variant.asScala, aggrGrp)
            
                // If reference is column - group by column.
                findAnyColumnTokenOpt(grpTok) match {
                    case Some(grpCol) ⇒ Seq(extractColumn(grpCol))
                    case None ⇒
                        // If reference is table - group by all PK columns of table or
                        // (if there aren't PKs, by any table column)
                        findAnyTableTokenOpt(grpTok) match {
                            case Some(tab) ⇒
                                val cols = tab.getColumns.asScala
                                val pkCols = cols.filter(_.isPk)
                            
                                if (pkCols.nonEmpty) pkCols else Seq(cols.head)
                            case None ⇒ throw new NCException(s"Group by not found for: $aggrGrpTok")
                        }
                }
        }
    
        NCSqlAggregateImpl(select, grpBy)
    }
    
    /**
      *
      * @param tblTok
      * @return
      */
    override def extractTable(tblTok: NCToken): NCSqlTable = {
        checkGroup(tblTok, "table")
        
        findSchemaTable(tblTok.metax("sql:name"))
    }
    
    /**
      *
      * @param colTok
      * @return
      */
    override def extractColumn(colTok: NCToken): NCSqlColumn = {
        checkGroup(colTok, "column")
    
        val tbl: String = colTok.metax("sql:tablename")
        val col: String = colTok.metax("sql:name")
    
        findSchemaColumn(findSchemaTable(tbl).getColumns, tbl, col)
    }
    
    /**
      *
      * @param tok
      * @return
      */
    override def extractDateRange(tok: NCToken): NCSqlDateRange = {
        checkTokenId(tok, "nlpcraft:date")
        
        NCSqlDateRangeImpl(
            new Timestamp(tok.metax("nlpcraft:date:from")),
            new Timestamp(tok.metax("nlpcraft:date:to"))
        )
    }
    
    /**
      * 
      * @param variant
      * @param tok
      * @return
      */
    private def getLinkBySingleIndex(variant: Seq[NCToken], tok: NCToken): NCToken = {
        val idxs: util.List[Integer] = tok.metax(s"${tok.getId}:indexes")

        val idx = idxs.asScala.head

        if (idx < variant.length) {
            val note: String = tok.metax(s"${tok.getId}:note")

            val link = variant(idx)

            if (link.getId != note)
                throw new NCException(s"Unexpected token with index: $idx, type: $note")

            link
        }
        else
            throw new NCException(s"Token not found with index: $idx")
    }
}
