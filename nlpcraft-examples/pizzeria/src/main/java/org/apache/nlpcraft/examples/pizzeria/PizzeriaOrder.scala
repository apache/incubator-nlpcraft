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

package org.apache.nlpcraft.examples.pizzeria

import java.util.Objects
import scala.collection.mutable

object OrderElement:
    val DFLT_QTY = 1

/**
  *
  */
private abstract class OrderElement:
    val name: String
    var qty: Option[Int]
    require(name != null && name.nonEmpty)

/**
  *
  * @param name
  * @param size
  * @param qty
  */
case class Pizza(name: String, var size: Option[String], var qty: Option[Int]) extends OrderElement:
    override def toString = s"$name '${size.getOrElse("undefined size")}' ${qty.getOrElse(OrderElement.DFLT_QTY)} pcs"

/**
  *
  * @param name
  * @param qty
  */
case class Drink(name: String, var qty: Option[Int]) extends OrderElement:
    override def toString = s"$name ${qty.getOrElse(OrderElement.DFLT_QTY)} pcs"

enum State:
    case NO_DIALOG, DIALOG_IS_READY, DIALOG_SHOULD_CANCEL, DIALOG_SPECIFY, DIALOG_CONFIRM

import org.apache.nlpcraft.examples.pizzeria.State.*

class PizzeriaOrder:
    private var state = NO_DIALOG
    private val pizzas = mutable.ArrayBuffer.empty[Pizza]
    private val drinks = mutable.ArrayBuffer.empty[Drink]

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
        def setByName[T <: OrderElement](buf: mutable.ArrayBuffer[T], t: T) =
            buf.find(_.name == t.name) match
                case Some(found) => if t.qty.nonEmpty then found.qty = t.qty
                case None => buf += t

        for (p <- ps)
            def setPizza[T](pred: Pizza => Boolean, notFound: => () => Unit): Unit =
                pizzas.find(pred) match
                    case Some(found) =>
                        if p.size.nonEmpty then found.size = p.size
                        if p.qty.nonEmpty then found.qty = p.qty
                    case None => notFound()

            if p.size.nonEmpty then setPizza(
                x => x.name == p.name && x.size == p.size,
                () => setPizza(x => x.name == p.name && x.size.isEmpty, () => pizzas += p)
            )
            else setByName(pizzas, p)

        for (d <- ds) setByName(drinks, d)
    /**
      *
      * @return
      */
    def getPizzas: Seq[Pizza] = pizzas.toSeq

    /**
      *
      * @return
      */
    def getDrinks: Seq[Drink] = drinks.toSeq

    /**
      *
      * @return
      */
    def findPizzaNoSize: Option[Pizza] = pizzas.find(_.size.isEmpty)

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

    /**
      *
      * @return
      */
    def getDesc: String =
        if !isEmpty then
            val ps = if pizzas.nonEmpty then s"Pizza: ${pizzas.mkString(", ")}" else ""
            val ds = if drinks.nonEmpty then s"Drinks: ${drinks.mkString(", ")}" else ""

            if ds.isEmpty then ps else if ps.isEmpty then ds else s"$ps, $ds"
        else "Nothing ordered."