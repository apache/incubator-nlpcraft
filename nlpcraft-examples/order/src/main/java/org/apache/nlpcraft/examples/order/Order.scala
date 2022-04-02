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

object Order:
    private val allPizzaSizeKinds: String = withComma(PizzaSize.values.map(_.toString.toLowerCase))

    private def withComma(iter: Iterable[String]): String = iter.mkString(", ")
    private def pizza2Str(name: String, size: PizzaSize): String =
        if size != null then s"$name ${size.toString.toLowerCase} size" else name
    private def seq2Str[T](name: String, seq: Seq[T], toStr: T => String = (t: T) => t.toString): String =
        if seq.nonEmpty then s"$name: ${withComma(seq.map(toStr))}." else ""
    private def norm(s: String) = s.trim.replaceAll("(?m)^[ \t]*\r?\n", "")

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

    def addDrink(name: String): Unit = drinks += name
    def addPizza(name: String): Unit = pizza += name -> null
    def addPizza(name: String, size: PizzaSize): Unit = pizza += name -> size

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
                case Some((name, _)) => s"Please specify $name size? It can be $allPizzaSizeKinds"
                case None => throw new AssertionError("Invalid state")

    def ask2Confirm(): String =
        require(isValid())
        norm(
            s"""
           |Let me specify your order.
           |${seq2Str("Pizza", pizza.toSeq, pizza2Str)}
           |${seq2Str("Drinks", drinks.toSeq)}
           |Is it correct?
           """.stripMargin
        )

    def getState(): String =
        norm(
            s"""
               |Current order state: '${if inProgress() then "in progress" else "empty"}'.
               |${seq2Str("Pizza", pizza.toSeq, pizza2Str)}
               |${seq2Str("Drinks", drinks.toSeq)}
           """.stripMargin
        )
    
    override def toString(): String =
        norm(
            s"""
           |${seq2Str("Pizza", pizza.toSeq, pizza2Str)}
           |${seq2Str("Drinks", drinks.toSeq)}
           """.stripMargin
        ).replaceAll("\n", " ")

    def clear(): Unit =
        pizza.clear()
        drinks.clear()
