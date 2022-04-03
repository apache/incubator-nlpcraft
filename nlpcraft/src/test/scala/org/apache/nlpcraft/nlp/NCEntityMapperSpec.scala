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

package org.apache.nlpcraft.nlp

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCResourceReader
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticTestElement
import org.apache.nlpcraft.nlp.token.parser.NCOpenNLPTokenParser
import org.apache.nlpcraft.nlp.util.NCTestUtils
import org.junit.jupiter.api.Test
import org.apache.nlpcraft.nlp.util.*

import java.util.List as JList
import scala.util.Using
import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCEntityMapperSpec:
    case class UnionMapper(id1: String, id2: String) extends NCEntityMapper:
        override def convert(req: NCRequest, entities: JList[NCEntity], toks: JList[NCToken]): JList[NCEntity] =
            val es = entities.asScala

            val id1AndId2 = es.filter(e => e.getId == id1 || e.getId == id2)
            val other = es.filter(e => !id1AndId2.contains(e))

            val newEntity = new NCPropertyMapAdapter with NCEntity:
                override def getTokens: JList[NCToken] = id1AndId2.flatMap(_.getTokens.asScala).sortBy(_.getIndex).asJava
                override def getRequestId: String = req.getRequestId
                override def getId: String = s"$id1$id2"

            (other ++ Seq(newEntity)).sortBy(_.getTokens.get(0).getIndex).asJava

    private val mdl: NCTestModelAdapter = new NCTestModelAdapter:
        override val getPipeline: NCPipeline =
            val pl = mkEnPipeline
            import NCSemanticTestElement as TE
            pl.getEntityParsers.add(NCTestUtils.mkEnSemanticParser(TE("a"), TE("b"), TE("c"), TE("d")))
            // Replaces [a, b] -> [ab]
            pl.getEntityMappers.add(UnionMapper("a", "b"))
            // Replaces [c, d] -> [cd]
            pl.getEntityMappers.add(UnionMapper("c", "d"))
            // Replaces [ab, cd] -> [abcd]
            pl.getEntityMappers.add(UnionMapper("ab", "cd"))
            pl

        @NCIntent("intent=i term(e)={# == 'abcd'}")
        def onMatch(@NCIntentTerm("e") e: NCEntity): NCResult = new NCResult("OK", NCResultType.ASK_RESULT)

    @Test
    def test(): Unit = Using.resource(new NCModelClient(mdl)) { client => require(client.ask("a b c d", null, "userId").getBody == "OK")}
