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
import scala.compat.java8.OptionConverters._

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
        if (!tok.isMemberOf(grp))
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
      * @param grp
      * @return
      */
    private def getWithGroup(tok: NCToken, grp: String): Seq[NCToken] =
        Seq(tok) ++ tok.findPartTokens().asScala.flatMap(p ⇒ if (p.getGroups.contains(grp)) Some(p) else None)
    
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
        val tok = getLinks(variant, refTok)

        findAnyColumnTokenOpt(tok) match {
            // If reference is column - sort by column.
            case Some(t) ⇒ Some(extractColumn(t))
            case None ⇒
                // If reference is table - sort by any PK column of table.
                findAnyTableTokenOpt(tok) match {
                    case Some(tab) ⇒ Some(tab.getColumns.asScala.minBy(col ⇒ if (col.isPk) 0 else 1))
                    case None ⇒ None
                }
        }
    }

    /**
     *
     * @param refToks
     * @return
     */
    private def getReferences(refToks: Seq[NCToken]): Seq[NCSqlColumn] = {
        refToks
    }

    /**
      *
      * @param limitTok
      * @return
      */
    override def extractLimit(limitTok: NCToken): NCSqlLimit = {
        checkTokenId(limitTok, "nlpcraft:limit")
        
        // Skips indexes to simplify.
        val limit: Double = limitTok.metax("nlpcraft:limit:limit")

        val links = getLinks(variant, limitTok, "indexes", "note", false)

        val size = links.size

        size match {
            case 1 ⇒
                val link = links.head

                val res = findAnyColumnTokenOpt(link) match {
                    // If reference is column - sort by column.
                    case Some(t) ⇒ extractColumn(t)
                    case None ⇒
                        // If reference is table - sort by any PK column of table.
                        findAnyTableTokenOpt(link) match {
                            case Some(tab) ⇒ tab.getColumns.asScala.minBy(col ⇒ if (col.isPk) 0 else 1)
                            case None ⇒ throw new NCException("TODO:")
                        }
                }

                NCSqlLimitImpl(
                    res,
                    limit.intValue(),
                    limitTok.metax("nlpcraft:limit:asc")
                )
            case  _ ⇒ throw new NCException(s"Unexpected LIMIT links count: $size")
        }
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
    private def getLinks(variant: NCVariant, tok: NCToken, idxField: String, noteField: String, canBeEmpty: Boolean): Seq[NCToken] = {
        val idxsOpt: Option[util.List[Integer]] = tok.metaOpt(s"${tok.getId}:$idxField").asScala
        
        if (idxsOpt.isEmpty && !canBeEmpty)
            throw new NCException(s"Empty indexes for: $tok")

        idxsOpt.get.asScala.map(idx ⇒ {
            if (idx < variant.size) {
                val note: String = tok.metax(s"${tok.getId}:$noteField")

                val link = variant.get(idx)

                if (link.getId != note)
                    throw new NCException(s"Unexpected token with index: $idx, type: $note")

                link
            }
            else
                throw new NCException(s"Token not found with index: $idx")
        })

    }
}
