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

case class Pizza(name: String, var size: Option[String], var qty: Option[Int]):
    require(name != null && name.nonEmpty)
case class Drink(name: String, var qty: Option[Int]):
    require(name != null && name.nonEmpty)
enum State:
    case NO_DIALOG, DIALOG_IS_READY, DIALOG_SHOULD_CANCEL, DIALOG_SPECIFY, DIALOG_CONFIRM

import org.apache.nlpcraft.examples.order.State.*

class Order:
    private var state = NO_DIALOG
    private val pizzas = mutable.LinkedHashMap.empty[String, Pizza]
    private val drinks = mutable.LinkedHashMap.empty[String, Drink]

    /**
      *
      * @return
      */
    def isEmpty: Boolean = pizzas.isEmpty && drinks.isEmpty

    /**
      *
      * @return
      */
    def isValid: Boolean = !isEmpty && findPizzaNoSize.isEmpty

    /**
      *
      * @param ps
      * @param ds
      */
    def add(ps: Seq[Pizza], ds: Seq[Drink]): Unit =
        for (p <- ps)
            pizzas.get(p.name) match
                case Some(ex) =>
                    if p.size.nonEmpty then ex.size = p.size
                    if p.qty.nonEmpty then ex.qty = p.qty
                case None => pizzas += p.name -> p

        for (d <- ds)
            drinks.get(d.name) match
                case Some(ex) => if d.qty.nonEmpty then ex.qty = d.qty
                case None => drinks += d.name -> d

    /**
      *
      * @return
      */
    def getPizzas: Map[String, Pizza] = pizzas.toMap

    /**
      *
      * @return
      */
    def getDrinks: Map[String, Drink] = drinks.toMap

    /**
      *
      * @return
      */
    def findPizzaNoSize: Option[Pizza] = pizzas.values.find(_.size.isEmpty)

    /**
      *
       * @param size
      */
    def setPizzaNoSize(size: String): Boolean =
        findPizzaNoSize match
            case Some(p) =>
                p.size = Option(size)
                true
            case None => false
    /**
      *
      * @return
      */
    def getState: State = state

    /**
      *
      * @param state
      */
    def setState(state: State): Unit = this.state = state