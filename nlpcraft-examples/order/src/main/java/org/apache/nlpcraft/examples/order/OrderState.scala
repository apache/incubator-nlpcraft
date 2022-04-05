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

package org.apache.nlpcraft.examples.order

import scala.collection.mutable

case class Pizza(name: String, var size: Option[String], qty: Option[Int])
case class Drink(name: String, qty: Option[Int])

class OrderState:
    private val pizzas = mutable.LinkedHashMap.empty[String, Pizza]
    private val drinks = mutable.LinkedHashMap.empty[String, Drink]

    private var wait4Appr = false

    private def findPizzaNoSize: Option[Pizza] = pizzas.values.find(_.size.isEmpty)

    def addPizza(p: Pizza): Unit = pizzas += p.name -> p
    def addDrink(d: Drink): Unit = drinks += d.name -> d

    def getPizzas: Map[String, Pizza] = pizzas.toMap
    def getDrinks: Map[String, Drink] = drinks.toMap

    def inProgress: Boolean = pizzas.nonEmpty || drinks.nonEmpty
    def isValid: Boolean = (pizzas.nonEmpty || drinks.nonEmpty) && pizzas.forall(_._2.size.nonEmpty)
    def isWait4Approve: Boolean = wait4Appr
    def wait4Approve(): Unit = wait4Appr = true
    def getPizzaNoSize: Pizza =
        require(!isValid)
        findPizzaNoSize.get
    def setPizzaNoSize(size: String): Unit =
        require(!isValid)
        require(size != null)
        findPizzaNoSize.get.size = Option(size)
    def clear(): Unit =
        pizzas.clear()
        drinks.clear()


