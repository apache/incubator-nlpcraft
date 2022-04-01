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

private enum PizzaSize:
    case SMALL, MEDIUM, LARGE

private class Pizza(name: String, var sizeOpt: Option[PizzaSize]):
    override def toString: String =
        val sz = Option.when(sizeOpt.nonEmpty)(sizeOpt.toString.toLowerCase).getOrElse("unknown")
        s"$name $sz size"

object Order:
    def getPizzaSizeKinds: String = PizzaSize.values.map(_.toString.toLowerCase).mkString(", ")
    def pizza2Str(name: String, size: PizzaSize): String = s"$name ${size.toString.toLowerCase} size"

import Order.*

/**
  * Contract.
  * 1. 'mkSpecifyRequest' scans ordered data, finds first invalid element and asks to specify it.
  * 2. 'specify' methods (specifyPizzaSize) should specify elements in the same order.
  * So, we don't need to save which concrete element we treying to specify.
  */
class Order:
    private val pizza = mutable.LinkedHashMap.empty[String, PizzaSize]
    private val drinks = mutable.LinkedHashSet.empty[String]

    def addDrink(drink: String): Unit = drinks += drink
    def addPizza(name: String, size: PizzaSize): Unit = pizza += name -> size
    def addPizza(name: String): Unit = pizza += name -> null

    def inProgress(): Boolean = pizza.nonEmpty || drinks.nonEmpty

    def isValid(): Boolean =
        (pizza.nonEmpty || drinks.nonEmpty) &&
        (pizza.isEmpty || pizza.forall{ (_, size) => size != null } )

    def specifyPizzaSize(sz: PizzaSize): Boolean = pizza.find { (_, size) => size == null } match
        case Some((name, _)) => pizza += name -> sz; true
        case None => false

    def ask2Specify(): String =
        require(!isValid())

        if pizza.isEmpty && drinks.isEmpty then
            "Order is empty. Please order some pizza or drinks."
        else
            pizza.find { (_, size) => size == null } match
                case Some((name, _)) => s"Please specify $name size? It can be $getPizzaSizeKinds"
                case None => throw new AssertionError("Invalid state")

    def ask2Confirm(): String =
        require(isValid())
        s"Let me specify your order.\n${this.toString()}\nIs it correct?"

    override def toString(): String =
        val ps = if pizza.nonEmpty then s"Pizza: ${pizza.map { (name, sz) => pizza2Str(name, sz) }.mkString(", ")}. " else ""
        val ds = if drinks.nonEmpty then s"Drinks: ${drinks.mkString(", ")}. " else ""

        s"$ps$ds"

    def clear(): Unit =
        pizza.clear()
        drinks.clear()