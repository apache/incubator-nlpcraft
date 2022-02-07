/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.internal.intent.matcher

import org.apache.nlpcraft.*

import java.util
import scala.jdk.CollectionConverters.*

/**
 * Sentence variant & its weight.
 */
case class NCIntentSolverVariant(entities: Seq[NCEntity]) extends Ordered[NCIntentSolverVariant]:
    private lazy val weights = calcWeight()

    /**
      *
      * @param toks
      */
    private def calcSparsity(toks: Seq[NCToken]): Int =
        val idxs = toks.map(_.getIndex)
        idxs.zipWithIndex.tail.map { (v, i) => Math.abs(v - idxs(i - 1)) }.sum - idxs.length + 1

    /**
     * Calculates weight components sequence.
     */
    private def calcWeight(): Seq[Int] =
        val toks: Seq[Seq[NCToken]] = entities.map(_.getTokens.asScala.toSeq)

        val toksCnt = toks.map(_.size).sum
        val avgToksPerEntity = if toksCnt > 0 then Math.round((entities.size.toFloat / toksCnt) * 100) else 0
        val totalSparsity = -toks.map(calcSparsity).sum  // Less is better.

        // Order is important.
        Seq(toksCnt, avgToksPerEntity, totalSparsity)

    override def compare(other: NCIntentSolverVariant): Int =
        def compareWeight(weight1: Int, weight2: Int): Option[Int] =
            if weight1 > weight2 then Option(1)
            else if weight2 > weight1 then Option(-1)
            else None

        weights.zip(other.weights).flatMap { (w1, w2) => compareWeight(w1, w2) }.to(LazyList).headOption.getOrElse(0)