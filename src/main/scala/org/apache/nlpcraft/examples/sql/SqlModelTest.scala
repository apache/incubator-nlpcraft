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
import java.util.function.Function

import com.github.difflib.text.DiffRowGenerator
import com.github.vertical_blank.sqlformatter.SqlFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jakewharton.fliptables.FlipTable
import org.apache.nlpcraft.model.tools.test.{NCTestClient, NCTestClientBuilder}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._

/**
  *
  */
class SqlModelTest extends FlatSpec with BeforeAndAfterAll {
    private val GSON = new Gson
    private val TYPE_RESP = new TypeToken[util.Map[String, Object]]() {}.getType
    private val NORM = Seq("\n", "\r", "\t")
    private val DIFF =
        DiffRowGenerator.create.
            showInlineDiffs(true).
            inlineDiffByWord(true).
            oldTag(new Function[java.lang.Boolean, String]() {
                override def apply(t: java.lang.Boolean): String = "~"
            }).
            newTag(new Function[java.lang.Boolean, String]() {
                override def apply(t: java.lang.Boolean): String = "**"
            }).
            build
    
    private var client: NCTestClient = _

    case class Case(texts: Seq[String], sql: String)

    private def normalize(s: String): String =
        NORM.
            foldLeft(s) { (res, s) ⇒ res.replaceAll(s, " ") }.
            split(" ").
            map(_.trim).
            filter(_.nonEmpty).
            mkString(" ")

    private def toPretty(s: String): util.List[String] = SqlFormatter.format(s).split("\n").toSeq.asJava
    
    override protected def beforeAll(): Unit = {
        client = new NCTestClientBuilder().newBuilder.setResponseLog(false).build
    
        client.open("sql.model.id")
    }
    
    override protected def afterAll(): Unit =
        if (client != null)
            client.close()
    
    private def check(multiLineOut: Boolean, cases: Case*): Unit = {
        val errs = collection.mutable.LinkedHashMap.empty[String, String]
        
        cases.
            flatMap(c ⇒ {
                val sql = normalize(c.sql)
                
                c.texts.map(t ⇒ t → sql)
            }).
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
            errs.foreach { case (txt, err) ⇒ println(s"Text: $txt\nError: $err\n")}

            throw new Exception(s"Test finished with errors [passed=${cases.size - errs.size}, failed=${errs.size}]")
        }
        else
            println("Passed")
    }

    it should "work fine" in {
        check(
            true,
            Case(
                Seq(
                    "order date",
                    "show me the order dates",
                    "list dates of orders"
                ),
                """SELECT
                  |  orders.order_date,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  orders.order_id,
                  |  orders.required_date,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  orders,
                  |  customers,
                  |  shippers,
                  |  employees
                  |WHERE
                  |  orders.customer_id = customers.customer_id
                  |  AND orders.ship_via = shippers.shipper_id
                  |  AND orders.employee_id = employees.employee_id
                  |ORDER BY
                  |  orders.order_id DESC
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "orders for last month"
                ),
                """SELECT
                  |  orders.order_date,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  orders.order_id,
                  |  orders.required_date,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  orders,
                  |  customers,
                  |  shippers,
                  |  employees
                  |WHERE
                  |  orders.order_date >= ?
                  |  AND orders.order_date <= ?
                  |  AND orders.customer_id = customers.customer_id
                  |  AND orders.ship_via = shippers.shipper_id
                  |  AND orders.employee_id = employees.employee_id
                  |ORDER BY
                  |  orders.order_id DESC
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "shippers data"
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
                    "show me orders with freight more than 10 for last year"
                ),
                """SELECT
                  |  orders.freight,
                  |  orders.order_id,
                  |  orders.order_date
                  |FROM
                  |  orders,
                  |  customers,
                  |  shippers,
                  |  employees
                  |WHERE
                  |  orders.freight > ?
                  |  AND orders.order_date >= ?
                  |  AND orders.order_date <= ?
                  |  AND orders.customer_id = customers.customer_id
                  |  AND orders.ship_via = shippers.shipper_id
                  |  AND orders.employee_id = employees.employee_id
                  |ORDER BY
                  |  orders.order_id ASC
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq("show me orders with max quantity group by product name"
                ),
                """SELECT
                  |  products.product_name,
                  |  max(order_details.quantity)
                  |FROM
                  |  orders,
                  |  order_details,
                  |  products
                  |WHERE
                  |  order_details.order_id = orders.order_id
                  |  AND order_details.product_id = products.product_id
                  |GROUP BY
                  |  products.product_name
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "show me orders with max quantity group by product"
                ),
                """SELECT
                  |  products.product_id,
                  |  max(order_details.quantity)
                  |FROM
                  |  orders,
                  |  products,
                  |  order_details
                  |WHERE
                  |  order_details.order_id = orders.order_id
                  |  AND order_details.product_id = products.product_id
                  |GROUP BY
                  |  products.product_id
                  |LIMIT
                  |  1000
                """.stripMargin),
            Case(
                Seq(
                    "territories data"
                ),
                """SELECT
                  |  region.region_id,
                  |  region.region_description,
                  |  territories.territory_id,
                  |  territories.territory_description,
                  |  territories.region_id
                  |FROM
                  |  territories,
                  |  region
                  |WHERE
                  |  territories.region_id = region.region_id
                  |ORDER BY
                  |  territories.territory_id DESC
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "employees territories"
                ),
                """SELECT
                  |  employee_territories.employee_id,
                  |  employee_territories.territory_id,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  region.region_id,
                  |  region.region_description,
                  |  territories.territory_id,
                  |  territories.territory_description,
                  |  territories.region_id
                  |FROM
                  |  employee_territories,
                  |  employees,
                  |  territories,
                  |  region
                  |WHERE
                  |  employee_territories.employee_id = employees.employee_id
                  |  AND employee_territories.territory_id = territories.territory_id
                  |  AND territories.region_id = region.region_id
                  |ORDER BY
                  |  employees.employee_id DESC,
                  |  territories.territory_id DESC
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "10 suppliers"
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
                    "orders group by employees")
                ,
                """SELECT
                  |  employees.employee_id,
                  |  count(orders.order_id)
                  |FROM
                  |  orders,
                  |  employees
                  |WHERE
                  |  orders.employee_id = employees.employee_id
                  |GROUP BY
                  |  employees.employee_id
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "orders group by products"
                ),
                """SELECT
                  |  products.product_id,
                  |  count(orders.order_id)
                  |FROM
                  |  orders,
                  |  products
                  |GROUP BY
                  |  products.product_id
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "show me orders count group by shipper company for last 3 months"
                ),
                """SELECT
                  |  shippers.shipper_id,
                  |  count(orders.order_id),
                  |  count(orders.order_date)
                  |FROM
                  |  orders,
                  |  shippers
                  |WHERE
                  |  orders.order_date >= ?
                  |  AND orders.order_date <= ?
                  |  AND orders.ship_via = shippers.shipper_id
                  |GROUP BY
                  |  shippers.shipper_id
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "give me average order freight"
                ),
                """SELECT
                  |  avg(orders.freight)
                  |FROM
                  |  orders
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "give me average order freight, ship name"
                ),
                """SELECT
                  |  orders.ship_name,
                  |  avg(orders.freight)
                  |FROM
                  |  orders
                  |GROUP BY
                  |  orders.ship_name
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "give me average order freight group by ship region"
                )
                ,
                """SELECT
                  |  orders.ship_region,
                  |  avg(orders.freight)
                  |FROM
                  |  orders
                  |GROUP BY
                  |  orders.ship_region
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "last year Exotic Liquids orders"
                ),
                """SELECT
                  |  suppliers.company_name,
                  |  orders.order_date,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  order_details.unit_price,
                  |  order_details.quantity,
                  |  order_details.discount,
                  |  orders.order_id,
                  |  orders.required_date,
                  |  products.product_id,
                  |  products.product_name,
                  |  products.quantity_per_unit,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone,
                  |  suppliers.supplier_id,
                  |  suppliers.contact_name
                  |FROM
                  |  order_details,
                  |  orders,
                  |  products,
                  |  suppliers,
                  |  customers,
                  |  shippers,
                  |  employees
                  |WHERE
                  |  suppliers.company_name IN (?)
                  |  AND orders.order_date >= ?
                  |  AND orders.order_date <= ?
                  |  AND order_details.order_id = orders.order_id
                  |  AND order_details.product_id = products.product_id
                  |  AND orders.customer_id = customers.customer_id
                  |  AND orders.ship_via = shippers.shipper_id
                  |  AND orders.employee_id = employees.employee_id
                  |  AND products.supplier_id = suppliers.supplier_id
                  |ORDER BY
                  |  orders.order_id DESC,
                  |  suppliers.supplier_id DESC
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "products grouped by categories"
                ),
                """SELECT
                  |  categories.category_id,
                  |  count(products.product_id)
                  |FROM
                  |  products,
                  |  categories
                  |WHERE
                  |  products.category_id = categories.category_id
                  |GROUP BY
                  |  categories.category_id
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "products names grouped by categories"
                ),
                """SELECT
                  |  categories.category_id,
                  |  count(products.product_name)
                  |FROM
                  |  categories,
                  |  products
                  |WHERE
                  |  products.category_id = categories.category_id
                  |GROUP BY
                  |  categories.category_id
                  |LIMIT
                  |  1000
                """.stripMargin
            ),
            Case(
                Seq(
                    "max orders discount grouped by product"
                ),
                """SELECT
                  |  products.product_id,
                  |  max(order_details.discount)
                  |FROM
                  |  products,
                  |  order_details
                  |WHERE
                  |  order_details.product_id = products.product_id
                  |GROUP BY
                  |  products.product_id
                  |LIMIT
                  |  1000
                """.stripMargin
            ) ,
            Case(
                Seq(
                    "give me the orders sorted by ship date"
                ),
                """
                  |SELECT
                  |  orders.shipped_date,
                  |  customers.customer_id,
                  |  customers.company_name,
                  |  customers.contact_name,
                  |  employees.employee_id,
                  |  employees.last_name,
                  |  employees.first_name,
                  |  orders.order_id,
                  |  orders.order_date,
                  |  orders.required_date,
                  |  shippers.shipper_id,
                  |  shippers.company_name,
                  |  shippers.phone
                  |FROM
                  |  orders,
                  |  customers,
                  |  shippers,
                  |  employees
                  |WHERE
                  |  orders.customer_id = customers.customer_id
                  |  AND orders.ship_via = shippers.shipper_id
                  |  AND orders.employee_id = employees.employee_id
                  |ORDER BY
                  |  orders.shipped_date ASC
                  |LIMIT
                  |  1000
                  |""".stripMargin
            )
        )
    }
}
