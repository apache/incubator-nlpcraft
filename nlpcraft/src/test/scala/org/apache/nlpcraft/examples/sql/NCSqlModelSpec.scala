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

import java.util

import org.apache.nlpcraft.examples.sql.db.SqlServer
import org.apache.nlpcraft.model.NCElement
import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.NCTestSortTokenType._
import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.{NCDefaultTestModel, NCEnricherBaseSpec, NCTestDateToken ⇒ dte, NCTestLimitToken ⇒ lim, NCTestNlpToken ⇒ nlp, NCTestSortToken ⇒ srt, NCTestUserToken ⇒ usr}
import org.junit.jupiter.api.{AfterEach, BeforeEach, Test}

class NCSqlModelWrapper extends NCDefaultTestModel {
    private val delegate = new SqlModel()

    override def getElements: util.Set[NCElement] = delegate.getElements
    override def getMacros: util.Map[String, String] = delegate.getMacros
}

class NCSqlModelSpec extends NCEnricherBaseSpec {
    override def getModelClass: Option[Class[_ <: NCDefaultTestModel]] = Some(classOf[NCSqlModelWrapper])

    @BeforeEach
    override def setUp(): Unit = {
        // org.apache.nlpcraft.examples.sql.SqlModel.SqlModel initialized via DB.
        // (org.apache.nlpcraft.examples.sql.db.SqlValueLoader configured in its model yaml file.)
        SqlServer.start()

        super.setUp()
    }

    @AfterEach
    override def tearDown(): Unit = {
        super.tearDown()

        SqlServer.stop()
    }

    /**
      *
      * @throws Exception
      */
    @Test
    def test(): Unit = {
        runBatch(
            _ ⇒ checkExists(
                txt = "order date",
                usr(text = "order date", id = "col:date")
            ),
            _ ⇒ checkExists(
                txt = "show me the order dates",
                nlp(text = "show me the", isStop = true),
                usr(text = "order dates", id = "col:date")
            ),
            _ ⇒ checkExists(
                txt = "list dates of orders",
                nlp(text = "list", isStop = true),
                usr(text = "dates orders", id = "col:date"),
                nlp(text = "of")
            ),
            _ ⇒ checkExists(
                txt = "orders for last month",
                usr(text = "orders", id = "tbl:orders"),
                dte(text = "for last month")
            ),
            _ ⇒ checkExists(
                txt = "shippers data",
                usr(text = "shippers", id = "tbl:shippers"),
                nlp(text = "data")
            ),
            _ ⇒ checkExists(
                txt = "show me orders with freight more than 10 for last year",
                nlp(text = "show me", isStop = true),
                srt(text = "orders with", typ = BY_ONLY, note = "condition:num", index = 2),
                usr(text = "freight more than 10", id = "condition:num"),
                dte(text = "for last year")
            ),
            _ ⇒ checkExists(
                txt = "territories data",
                usr(text = "territories", id = "tbl:territories"),
                nlp(text = "data")
            ),
            _ ⇒ checkExists(
                txt = "employees territories",
                usr(text = "employees territories", id = "tbl:employee_territories")
            ),
            _ ⇒ checkExists(
                txt = "10 suppliers",
                lim(text = "10", limit=10, index =1, note="tbl:suppliers"),
                usr(text = "suppliers", id = "tbl:suppliers")
            ),
            _ ⇒ checkExists(
                txt = "last year Exotic Liquids orders",
                dte(text="last year"),
                usr(text = "Exotic Liquids", id = "condition:value"),
                usr(text = "orders", id = "tbl:orders")
            ),
            _ ⇒ checkExists(
                txt = "give me the orders sorted by ship date",
                nlp(text = "give me the", isStop = true),
                usr(text = "orders", id = "tbl:orders"),
                srt(text = "sorted by", typ = BY_ONLY, note = "col:date", index = 3),
                usr(text = "ship date", id = "col:date"),
            ),
            _ ⇒ checkExists(
                txt = "give me the orders sorted by ship date",
                nlp(text = "give me the", isStop = true),
                usr(text = "orders", id = "tbl:orders"),
                srt(text = "sorted by", typ = BY_ONLY, note = "col:date", index = 3),
                usr(text = "ship date", id = "col:date"),
            ),
            _ ⇒ checkExists(
                txt = "give me the orders sorted by ship date asc",
                nlp(text = "give me the", isStop = true),
                usr(text = "orders", id = "tbl:orders"),
                srt(text = "sorted by", typ = BY_ONLY, note = "col:date", index = 3, asc = true),
                usr(text = "ship date", id = "col:date"),
                nlp(text = "asc", isStop = true)
            ),
            _ ⇒ checkExists(
                txt = "give me the orders sorted by date",
                nlp(text = "give me the", isStop = true),
                usr(text = "orders date", id = "col:date"),
                nlp(text = "sorted"),
                nlp(text = "by")
            ),
            _ ⇒ checkExists(
                txt = "What are the top orders for the last 2 weeks sorted by order quantity?",
                lim(text = "What are the top", limit = 10, index = 1, note = "tbl:orders", asc = false),
                usr(text = "orders", id = "tbl:orders"),
                dte(text = "for last 2 weeks"),
                nlp(text = "the", isStop = true),
                srt(text = "sorted by", typ = BY_ONLY, note = "col:num", index = 5),
                usr(text = "order quantity", id = "col:num"),
                nlp(text = "?", isStop = true)
            ),
            _ ⇒ checkExists(
                txt = "What are the top 25 orders for the last 2 weeks sorted by order quantity?",
                lim(text = "What are the top 25", limit = 25, index = 1, note = "tbl:orders", asc = false),
                usr(text = "orders", id = "tbl:orders"),
                dte(text = "for last 2 weeks"),
                nlp(text = "the", isStop = true),
                srt(text = "sorted by", typ = BY_ONLY, note = "col:num", index = 5),
                usr(text = "order quantity", id = "col:num"),
                nlp(text = "?", isStop = true)
            ),
            _ ⇒ checkExists(
                txt = "What are the best performing products for the last quarter?",
                nlp(text = "What are the", isStop = true),
                usr(text = "best performing", id = "sort:best"),
                usr(text = "products", id = "tbl:products"),
                dte(text = "for last quarter"),
                nlp(text = "the ?", isStop = true)
            ),
            _ ⇒ checkExists(
                txt = "What are the least performing categories for the last quarter?",
                nlp(text = "What are the", isStop = true),
                usr(text = "least performing", id = "sort:worst"),
                usr(text = "categories", id = "tbl:categories"),
                dte(text = "for last quarter"),
                nlp(text = "the ?", isStop = true)
            )
        )
    }
}
