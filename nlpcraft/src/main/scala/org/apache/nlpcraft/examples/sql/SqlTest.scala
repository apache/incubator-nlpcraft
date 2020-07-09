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

import com.github.difflib.text.DiffRowGenerator
import com.github.vertical_blank.sqlformatter.SqlFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jakewharton.fliptables.FlipTable
import org.apache.nlpcraft.examples.sql.db.SqlServer
import org.apache.nlpcraft.model.tools.test.{NCTestClient, NCTestClientBuilder}
import org.apache.nlpcraft.probe.embedded.NCEmbeddedProbe
import org.junit.jupiter.api.{AfterEach, BeforeEach, Test}

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._

/**
  * SQL model test.
  *
  * @see SqlModel
  */
class SqlTest {
    private val GSON = new Gson
    private val TYPE_RESP = new TypeToken[util.Map[String, Object]]() {}.getType
    private val NORM = Seq("\n", "\r", "\t")
    private val DIFF = DiffRowGenerator.create.
        showInlineDiffs(true).
        inlineDiffByWord(true).
        oldTag((_: java.lang.Boolean) ⇒ "~").
        newTag((_: java.lang.Boolean) ⇒ "**").
        build

    private var client: NCTestClient = _

    case class Case(texts: Seq[String], sql: String)

    @BeforeEach
    def setUp(): Unit = {
        SqlServer.start()

        NCEmbeddedProbe.start(classOf[SqlModel])

        client = new NCTestClientBuilder().newBuilder.setResponseLog(false).build

        client.open("sql.model.id")
    }

    @AfterEach
    def tearDown(): Unit = {
        if (client != null)
            client.close()

        NCEmbeddedProbe.stop()

        SqlServer.stop()
    }

    private def normalize(s: String): String =
        NORM.
            foldLeft(s) { (res, s) ⇒ res.replaceAll(s, " ") }.
            split(" ").
            map(_.trim).
            filter(_.nonEmpty).
            mkString(" ")

    private def toPretty(s: String): util.List[String] = SqlFormatter.format(s).split("\n").toSeq.asJava

    private def check(multiLineOut: Boolean, cases: Case*): Unit = {
        val errs = collection.mutable.LinkedHashMap.empty[String, String]

        cases.
            flatMap(c ⇒ c.texts.map(t ⇒ t → normalize(c.sql))).
            foreach {
                case (txt, expSqlNorm) ⇒
                    val res = client.ask(txt)

                    if (res.isOk) {
                        require(res.getResult.asScala.isDefined)

                        val m: util.Map[String, Object] = GSON.fromJson(res.getResult.get, TYPE_RESP)

                        val err = m.get("error")

                        if (err != null)
                            errs += txt → err.toString
                        else {
                            val resSqlNorm = normalize(m.asScala("sql").asInstanceOf[String])

                            if (resSqlNorm != expSqlNorm) {
                                if (multiLineOut) {
                                    val rows = DIFF.generateDiffRows(toPretty(expSqlNorm), toPretty(resSqlNorm)).asScala

                                    val table =
                                        FlipTable.of(
                                            Array("Expected", "Real"),
                                            rows.map(p ⇒ Array(p.getOldLine, p.getNewLine)).toArray
                                        )

                                    errs += txt → s"Unexpected SQL:\n$table"
                                }
                                else {
                                    val rows = DIFF.generateDiffRows(Seq(expSqlNorm).asJava, Seq(resSqlNorm).asJava).asScala

                                    require(rows.size == 1)

                                    val row = rows.head

                                    errs += txt →
                                        s"""Unexpected SQL (expected vs real)
                                           |${row.getOldLine}
                                           |${row.getNewLine}
                                        """.stripMargin
                                }
                            }
                        }
                    }
                    else {
                        require(res.getResultError.isPresent)

                        errs += txt → res.getResultError.get
                    }
            }

        if (errs.nonEmpty) {
            errs.foreach { case (txt, err) ⇒ System.err.println(s"Text: $txt\nError: $err\n") }

            throw new Exception(s"Test finished with errors [passed=${cases.size - errs.size}, failed=${errs.size}]")
        }
        else
            println("Passed")
    }

    @Test
    def testConversation(): Unit = {
        check(
            true,
            Case(
                Seq(
                    "last year Exotic Liquids orders",
                    // Second and third sentences are the qualifying questions for first.
                    // Second by date, third by value.
                    // See logic implemented in org.apache.nlpcraft.examples.sql.SqlModel.onMatchedIntent
                    "last month",
                    "Norske Meierier"
                ),
                """SELECT
                  |  suppliers.company_name,
                  |  orders.order_date,
                  |  orders.order_id,
                  |  orders.required_date,
                  |  suppliers.supplier_id,
                  |  suppliers.contact_name,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  order_details.unit_price,
                  |  order_details.quantity,
                  |  order_details.discount,
                  |  products.product_id,
                  |  products.product_name,
                  |  products.quantity_per_unit,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  order_details
                  |  INNER JOIN orders ON order_details.order_id = orders.order_id
                  |  INNER JOIN products ON order_details.product_id = products.product_id
                  |  LEFT JOIN customers ON orders.customer_id = customers.customer_id
                  |  LEFT JOIN shippers ON orders.ship_via = shippers.shipper_id
                  |  LEFT JOIN employees ON orders.employee_id = employees.employee_id
                  |  LEFT JOIN suppliers ON products.supplier_id = suppliers.supplier_id
                  |WHERE
                  |  suppliers.company_name IN (?)
                  |  AND orders.order_date >= ?
                  |  AND orders.order_date <= ?
                  |ORDER BY
                  |  orders.order_id DESC,
                  |  suppliers.supplier_id DESC
                  |LIMIT
                  |  1000
                      """.stripMargin
            )
        )
    }

    @Test
    def test() {
        check(
            true,
            Case(
                Seq(
                    "order date, please!",
                    "show me the order dates",
                    "list dates of orders"
                ),
                """SELECT
                  |  orders.order_date,
                  |  orders.order_id,
                  |  orders.required_date,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  orders
                  |  LEFT JOIN customers ON orders.customer_id = customers.customer_id
                  |  LEFT JOIN shippers ON orders.ship_via = shippers.shipper_id
                  |  LEFT JOIN employees ON orders.employee_id = employees.employee_id
                  |ORDER BY
                  |  orders.order_id DESC
                  |LIMIT
                  |  1000
                  """.stripMargin
            ),
            Case(
                Seq(
                    "give me orders for the last month"
                ),
                """SELECT
                  |  orders.order_date,
                  |  orders.order_id,
                  |  orders.required_date,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  orders
                  |  LEFT JOIN customers ON orders.customer_id = customers.customer_id
                  |  LEFT JOIN shippers ON orders.ship_via = shippers.shipper_id
                  |  LEFT JOIN employees ON orders.employee_id = employees.employee_id
                  |WHERE
                  |  orders.order_date >= ?
                  |  AND orders.order_date <= ?
                  |ORDER BY
                  |  orders.order_id DESC
                  |LIMIT
                  |  1000
                  """.stripMargin
            ),
            Case(
                Seq(
                    "list shippers data"
                ),
                """SELECT
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  shippers
                  |ORDER BY
                  |  shippers.shipper_id DESC
                  |LIMIT
                  |  1000
                  """.stripMargin
            ),
            Case(
                Seq(
                    "show me orders with freight more than 10 for the last year"
                ),
                """SELECT
                  |  orders.freight,
                  |  orders.order_date,
                  |  orders.order_id,
                  |  orders.required_date,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  orders
                  |  LEFT JOIN customers ON orders.customer_id = customers.customer_id
                  |  LEFT JOIN shippers ON orders.ship_via = shippers.shipper_id
                  |  LEFT JOIN employees ON orders.employee_id = employees.employee_id
                  |WHERE
                  |  orders.freight > ?
                  |  AND orders.order_date >= ?
                  |  AND orders.order_date <= ?
                  |ORDER BY
                  |  orders.order_id DESC
                  |LIMIT
                  |  1000
                  """.stripMargin
            ),
            Case(
                Seq(
                    "show territories data"
                ),
                """SELECT
                  |  territories.territory_id,
                  |  territories.territory_description,
                  |  territories.region_id,
                  |  region.region_id,
                  |  region.region_description
                  |FROM
                  |  territories
                  |  INNER JOIN region ON territories.region_id = region.region_id
                  |ORDER BY
                  |  territories.territory_id DESC
                  |LIMIT
                  |  1000
                  """.stripMargin
            ),
            Case(
                Seq(
                    "display employees territories"
                ),
                """SELECT
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  territories.territory_id,
                  |  territories.territory_description,
                  |  territories.region_id,
                  |  employee_territories.employee_id,
                  |  employee_territories.territory_id,
                  |  region.region_id,
                  |  region.region_description
                  |FROM
                  |  employee_territories
                  |  INNER JOIN employees ON employee_territories.employee_id = employees.employee_id
                  |  INNER JOIN territories ON employee_territories.territory_id = territories.territory_id
                  |  INNER JOIN region ON territories.region_id = region.region_id
                  |ORDER BY
                  |  employees.employee_id DESC,
                  |  territories.territory_id DESC
                  |LIMIT
                  |  1000
                  """.stripMargin
            ),
            Case(
                Seq(
                    "get me 10 suppliers"
                ),
                """SELECT
                  |  suppliers.supplier_id,
                  |  suppliers.company_name,
                  |  suppliers.contact_name
                  |FROM
                  |  suppliers
                  |ORDER BY
                  |  suppliers.supplier_id DESC
                  |LIMIT
                  |  10
                  """.stripMargin
            ),
            Case(
                Seq(
                    "last year Exotic Liquids orders"
                ),
                """SELECT
                  |  suppliers.company_name,
                  |  orders.order_date,
                  |  orders.order_id,
                  |  orders.required_date,
                  |  suppliers.supplier_id,
                  |  suppliers.contact_name,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  order_details.unit_price,
                  |  order_details.quantity,
                  |  order_details.discount,
                  |  products.product_id,
                  |  products.product_name,
                  |  products.quantity_per_unit,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  order_details
                  |  INNER JOIN orders ON order_details.order_id = orders.order_id
                  |  INNER JOIN products ON order_details.product_id = products.product_id
                  |  LEFT JOIN customers ON orders.customer_id = customers.customer_id
                  |  LEFT JOIN shippers ON orders.ship_via = shippers.shipper_id
                  |  LEFT JOIN employees ON orders.employee_id = employees.employee_id
                  |  LEFT JOIN suppliers ON products.supplier_id = suppliers.supplier_id
                  |WHERE
                  |  suppliers.company_name IN (?)
                  |  AND orders.order_date >= ?
                  |  AND orders.order_date <= ?
                  |ORDER BY
                  |  orders.order_id DESC,
                  |  suppliers.supplier_id DESC
                  |LIMIT
                  |  1000
                  """.stripMargin
            ),
            Case(
                Seq(
                    "give me the orders sorted by the ship date"
                ),
                """SELECT
                  |  orders.shipped_date,
                  |  orders.order_id,
                  |  orders.order_date,
                  |  orders.required_date,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  orders
                  |  LEFT JOIN customers ON orders.customer_id = customers.customer_id
                  |  LEFT JOIN shippers ON orders.ship_via = shippers.shipper_id
                  |  LEFT JOIN employees ON orders.employee_id = employees.employee_id
                  |ORDER BY
                  |  orders.shipped_date DESC
                  |LIMIT
                  |  1000
                  """.stripMargin
            ),
            Case(
                Seq(
                    "give me the orders sorted by the ship date in ascending order"
                ),
                """SELECT
                  |  orders.shipped_date,
                  |  orders.order_id,
                  |  orders.order_date,
                  |  orders.required_date,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  orders
                  |  LEFT JOIN customers ON orders.customer_id = customers.customer_id
                  |  LEFT JOIN shippers ON orders.ship_via = shippers.shipper_id
                  |  LEFT JOIN employees ON orders.employee_id = employees.employee_id
                  |ORDER BY
                  |  orders.shipped_date ASC
                  |LIMIT
                  |  1000
                  """.stripMargin
            ),
            Case(
                // Default sort (nlpcraft:sort shouldn't be found)
                Seq(
                    "give me the orders sorted by date"
                ),
                """SELECT
                  |  orders.order_date,
                  |  orders.order_id,
                  |  orders.required_date,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  orders
                  |  LEFT JOIN customers ON orders.customer_id = customers.customer_id
                  |  LEFT JOIN shippers ON orders.ship_via = shippers.shipper_id
                  |  LEFT JOIN employees ON orders.employee_id = employees.employee_id
                  |ORDER BY
                  |  orders.order_id DESC
                  |LIMIT
                  |  1000
                  """.stripMargin
            ),
            Case(
                Seq(
                    "What are the top orders for the last 2 weeks sorted by order quantity?"
                ),
                """SELECT
                  |  order_details.quantity,
                  |  orders.order_date,
                  |  order_details.unit_price,
                  |  order_details.discount,
                  |  orders.order_id,
                  |  orders.required_date,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  products.product_id,
                  |  products.product_name,
                  |  products.quantity_per_unit,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  order_details
                  |  INNER JOIN orders ON order_details.order_id = orders.order_id
                  |  INNER JOIN products ON order_details.product_id = products.product_id
                  |  LEFT JOIN customers ON orders.customer_id = customers.customer_id
                  |  LEFT JOIN shippers ON orders.ship_via = shippers.shipper_id
                  |  LEFT JOIN employees ON orders.employee_id = employees.employee_id
                  |WHERE
                  |  orders.order_date >= ?
                  |  AND orders.order_date <= ?
                  |ORDER BY
                  |  order_details.quantity DESC
                  |LIMIT
                  |  10
                  """.stripMargin
            ),
            Case(
                Seq(
                    "What are the top 25 orders for the last 2 weeks sorted by order quantity?"
                ),
                """SELECT
                  |  order_details.quantity,
                  |  orders.order_date,
                  |  order_details.unit_price,
                  |  order_details.discount,
                  |  orders.order_id,
                  |  orders.required_date,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  products.product_id,
                  |  products.product_name,
                  |  products.quantity_per_unit,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  order_details
                  |  INNER JOIN orders ON order_details.order_id = orders.order_id
                  |  INNER JOIN products ON order_details.product_id = products.product_id
                  |  LEFT JOIN customers ON orders.customer_id = customers.customer_id
                  |  LEFT JOIN shippers ON orders.ship_via = shippers.shipper_id
                  |  LEFT JOIN employees ON orders.employee_id = employees.employee_id
                  |WHERE
                  |  orders.order_date >= ?
                  |  AND orders.order_date <= ?
                  |ORDER BY
                  |  order_details.quantity DESC
                  |LIMIT
                  |  25
                  """.stripMargin
            ),
            Case(
                Seq(
                    "What are the best performing products for the last quarter?"
                ),
                """SELECT
                  |  orders.freight,
                  |  orders.order_date,
                  |  orders.order_id,
                  |  orders.required_date,
                  |  products.product_id,
                  |  products.product_name,
                  |  products.quantity_per_unit,
                  |  categories.category_id,
                  |  categories.category_name,
                  |  categories.description,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  order_details.unit_price,
                  |  order_details.quantity,
                  |  order_details.discount,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone,
                  |  suppliers.supplier_id,
                  |  suppliers.company_name,
                  |  suppliers.contact_name
                  |FROM
                  |  order_details
                  |  INNER JOIN orders ON order_details.order_id = orders.order_id
                  |  INNER JOIN products ON order_details.product_id = products.product_id
                  |  LEFT JOIN customers ON orders.customer_id = customers.customer_id
                  |  LEFT JOIN shippers ON orders.ship_via = shippers.shipper_id
                  |  LEFT JOIN employees ON orders.employee_id = employees.employee_id
                  |  LEFT JOIN suppliers ON products.supplier_id = suppliers.supplier_id
                  |  LEFT JOIN categories ON products.category_id = categories.category_id
                  |WHERE
                  |  orders.order_date >= ?
                  |  AND orders.order_date <= ?
                  |ORDER BY
                  |  orders.freight DESC
                  |LIMIT
                  |  1000
                  """.stripMargin
            ),
            Case(
                Seq(
                    "What are the least performing categories for the last quarter?"
                ),
                 """SELECT
                  |  orders.freight,
                  |  orders.order_date,
                  |  categories.category_id,
                  |  categories.category_name,
                  |  categories.description,
                  |  orders.order_id,
                  |  orders.required_date,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  order_details.unit_price,
                  |  order_details.quantity,
                  |  order_details.discount,
                  |  products.product_id,
                  |  products.product_name,
                  |  products.quantity_per_unit,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  order_details
                  |  INNER JOIN orders ON order_details.order_id = orders.order_id
                  |  INNER JOIN products ON order_details.product_id = products.product_id
                  |  LEFT JOIN customers ON orders.customer_id = customers.customer_id
                  |  LEFT JOIN shippers ON orders.ship_via = shippers.shipper_id
                  |  LEFT JOIN employees ON orders.employee_id = employees.employee_id
                  |  LEFT JOIN categories ON products.category_id = categories.category_id
                  |WHERE
                  |  orders.order_date >= ?
                  |  AND orders.order_date <= ?
                  |ORDER BY
                  |  orders.freight ASC
                  |LIMIT
                  |  1000
                  """.stripMargin
            )
        )
    }
}
