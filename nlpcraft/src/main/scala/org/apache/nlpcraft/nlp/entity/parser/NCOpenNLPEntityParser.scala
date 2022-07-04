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

package org.apache.nlpcraft.nlp.entity.parser

import com.typesafe.scalalogging.LazyLogging
import opennlp.tools.namefind.*
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils

import java.io.*
import java.util
import java.util.Objects
import scala.Option.*
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*
import scala.language.postfixOps
import scala.util.Using

/**
  *
  */
object NCOpenNLPEntityParser:
    def apply(src: String): NCOpenNLPEntityParser =
        require(src != null, "Model source cannot be null.")
        new NCOpenNLPEntityParser(List(src))

/**
  *
  * @param srcs
  */
class NCOpenNLPEntityParser(srcs: List[String]) extends NCEntityParser with LazyLogging:
    require(srcs != null, "Models source cannot be null.")

    private var finders: Seq[NameFinderME] = _
    private case class Holder(start: Int, end: Int, name: String, probability: Double)

    init()
    /**
      *
      */
    private def init(): Unit =
        val finders = mutable.ArrayBuffer.empty[NameFinderME]
        NCUtils.execPar(
            srcs.map(res => () => {
                val f = new NameFinderME(new TokenNameFinderModel(NCUtils.getStream(res)))
                logger.trace(s"Loaded resource: $res")
                finders.synchronized { finders += f }
            }))(ExecutionContext.Implicits.global)

        this.finders = finders.toSeq

    /**
      *
      * @param finder
      * @param words
      * @return
      */
    private def find(finder: NameFinderME, words: Array[String]): Array[Holder] = finder.synchronized {
        try finder.find(words).map(p => Holder(p.getStart, p.getEnd - 1, p.getType, p.getProb))
        finally finder.clearAdaptiveData()
    }

    override def parse(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): List[NCEntity] =
        val txtArr = toks.map(_.getText).toArray

        finders.flatMap(find(_, txtArr)).flatMap(h => {
            def calcIndex(getHolderIndex: Holder => Int): Int =
                toks.find(_.getIndex == getHolderIndex(h)) match
                    case Some(t) => t.getIndex
                    case None => -1

            val i1 = calcIndex(_.start)
            lazy val i2 = calcIndex(_.end)

            Option.when(i1 != -1 && i2 != -1)(
                new NCPropertyMapAdapter with NCEntity:
                    put(s"opennlp:${h.name}:probability", h.probability)

                    override val getTokens: List[NCToken] = toks.flatMap(t => Option.when(t.getIndex >= i1 && t.getIndex <= i2)(t))
                    override val getRequestId: String = req.getRequestId
                    override val getId: String = s"opennlp:${h.name}"
            )
        }).toList