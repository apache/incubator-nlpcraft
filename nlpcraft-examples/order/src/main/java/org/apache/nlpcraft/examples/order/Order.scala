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

enum State:
    case ORDER_EMPTY, ORDER_INVALID, ORDER_VALID, ASK_CONTINUE, ASK_CONFIRM, ASK_CANCEL

import org.apache.nlpcraft.examples.order.State.*

class Order:
    private var state =  ORDER_EMPTY
    private val pizzas = mutable.LinkedHashMap.empty[String, Pizza]
    private val drinks = mutable.LinkedHashMap.empty[String, Drink]

    private def findPizzaNoSize: Option[Pizza] = pizzas.values.find(_.size.isEmpty)

    def addPizza(p: Pizza): Unit =
        pizzas += p.name -> p
        state = if findPizzaNoSize.nonEmpty then ORDER_INVALID else ORDER_VALID
    def addDrink(d: Drink): Unit =
        if state == ORDER_EMPTY then state = ORDER_VALID
            drinks += d.name -> d

    def getState: State = state
    def setState(state: State) = this.state = state

    def getPizzas: Map[String, Pizza] = pizzas.toMap
    def getDrinks: Map[String, Drink] = drinks.toMap

    def getPizzaNoSize: Pizza =
        require(state == ORDER_INVALID)
        findPizzaNoSize.get
    def setPizzaNoSize(size: String): Unit =
        require(state == ORDER_INVALID)
        require(size != null)
        findPizzaNoSize.get.size = Option(size)
    def clear(): Unit =
        pizzas.clear()
        drinks.clear()


