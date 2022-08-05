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

import scala.collection.mutable

/**
  * Order states.
  */
enum PizzeriaOrderState:
    case DIALOG_EMPTY, DIALOG_IS_READY, DIALOG_SHOULD_CANCEL, DIALOG_SPECIFY, DIALOG_CONFIRM

private object OrderPosition:
    val DFLT_QTY = 1
/**
  *
  */
private trait OrderPosition:
    val name: String
    var qty: Option[Int]
    require(name != null && name.nonEmpty)

/**
  * Pizza order data holder.
  *
  * @param name Name.
  * @param size Optional size.
  * @param qty Optional quantity.
  */
case class Pizza(name: String, var size: Option[String], var qty: Option[Int]) extends OrderPosition:
    override def toString = s"$name '${size.getOrElse("undefined size")}' ${qty.getOrElse(OrderPosition.DFLT_QTY)} pcs"

/**
  * Drink order data holder.
  *
  * @param name Name.
  * @param qty Optional quantity.
  */
case class Drink(name: String, var qty: Option[Int]) extends OrderPosition:
    override def toString = s"$name ${qty.getOrElse(OrderPosition.DFLT_QTY)} pcs"

import org.apache.nlpcraft.examples.pizzeria.PizzeriaOrderState.*

/**
  * Order.
  */
class PizzeriaOrder:
    private var state = DIALOG_EMPTY
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
    def isValid: Boolean = !isEmpty && findPizzaWithoutSize.isEmpty

    /**
      *
      * @param ps
      * @param ds
      */
    def add(ps: Seq[Pizza], ds: Seq[Drink]): Unit =
        def setByName[T <: OrderPosition](buf: mutable.ArrayBuffer[T], t: T): Unit =
            buf.find(_.name == t.name) match
                case Some(foundT) => if t.qty.nonEmpty then foundT.qty = t.qty
                case None => buf += t

        for (p <- ps)
            def setPizza(pred: Pizza => Boolean, notFound: => () => Unit): Unit =
                pizzas.find(pred) match
                    case Some(foundPizza) =>
                        if p.size.nonEmpty then foundPizza.size = p.size
                        if p.qty.nonEmpty then foundPizza.qty = p.qty
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
    def findPizzaWithoutSize: Option[Pizza] = pizzas.find(_.size.isEmpty)

    /**
      *
       * @param size
      */
    def fixPizzaWithoutSize(size: String): Boolean =
        findPizzaWithoutSize match
            case Some(p) =>
                p.size = Option(size)
                true
            case None => false
    /**
      *
      * @return
      */
    def getState: PizzeriaOrderState = state

    /**
      *
      * @param state
      */
    def setState(state: PizzeriaOrderState): Unit = this.state = state

    override def toString: String =
        if !isEmpty then
            val ps = if pizzas.nonEmpty then s"pizza: ${pizzas.mkString(", ")}" else ""
            val ds = if drinks.nonEmpty then s"drinks: ${drinks.mkString(", ")}" else ""

            if ds.isEmpty then ps else if ps.isEmpty then ds else s"$ps, $ds"
        else "nothing ordered"