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

package org.apache.nlpcraft.nlp.entity.parser.opennlp.impl

import com.typesafe.scalalogging.LazyLogging
import opennlp.tools.namefind.*
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.nlp.token.enricher.impl.en.NCEnQuotesImpl.*

import java.io.*
import java.util
import java.util.{Optional, List as JList, Map as JMap}
import scala.Option.*
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*
import scala.language.postfixOps
import scala.util.Using
import scala.util.control.Exception.catching

object NCOpenNlpEntityParserImpl:
    def apply(res: String): NCOpenNlpEntityParserImpl = new NCOpenNlpEntityParserImpl(NCUtils.getStream(res), res)
    def apply(f: File): NCOpenNlpEntityParserImpl = new NCOpenNlpEntityParserImpl(new FileInputStream(f), f.getAbsolutePath)

/**
  *
  */
class NCOpenNlpEntityParserImpl(is: InputStream, res: String) extends NCEntityParser with LazyLogging :
    @volatile private var finder: NameFinderME = _

    private case class Holder(start: Int, end: Int, name: String, probability: Double)

    override def start(cfg: NCModelConfig): Unit =
        finder = new NameFinderME(new TokenNameFinderModel(NCUtils.getStream(res)))
        logger.trace(s"Loaded resource: $res")

    override def stop(): Unit = finder = null

    private def find(words: Array[String]): Array[Holder] =
        this.synchronized {
            try
                finder.find(words).map(p => Holder(p.getStart, p.getEnd - 1, p.getType, p.getProb))
            finally
                finder.clearAdaptiveData()
        }

    override def parse(req: NCRequest, cfg: NCModelConfig, toks: JList[NCToken]): JList[NCEntity] =
        val toksSeq = toks.asScala

        find(toksSeq.map(_.getText).toArray).flatMap(h =>
            def calcIndex(getHolderIndex: Holder => Int): Int =
                toksSeq.find(_.getIndex == getHolderIndex(h)) match
                    case Some(t) => t.getIndex
                    case None => -1

            val i1 = calcIndex(_.start)
            lazy val i2 = calcIndex(_.end)

            Option.when(i1 != -1 && i2 != -1)(
                new NCPropertyMapAdapter with NCEntity {
                    put(s"opennlp:${h.name}:probability", h.probability)

                    override def getTokens: JList[NCToken] = toksSeq.flatMap(t => Option.when(t.getIndex >= i1 && t.getIndex <= i2)(t)).asJava
                    override def getRequestId: String = req.getRequestId
                    override def getId: String = s"opennlp:${h.name}"
                }
            )
        ).toSeq.asJava