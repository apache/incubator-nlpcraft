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
    private case class Combiner(ids: String*) extends NCEntityMapper:
        override def map(req: NCRequest, cfg: NCModelConfig, entities: JList[NCEntity]): JList[NCEntity] =
            val es = entities.asScala
            val replaced = es.filter(p => ids.contains(p.getId))

            if replaced.isEmpty then
                entities
            else
                val newEntity: NCEntity = new NCPropertyMapAdapter with NCEntity:
                    override val getTokens: JList[NCToken] = replaced.flatMap(_.getTokens.asScala).sortBy(_.getIndex).asJava
                    override val getRequestId: String = req.getRequestId
                    override val getId: String = ids.mkString

                es --= replaced
                (es :+ newEntity).sortBy(_.getTokens.asScala.head.getIndex).asJava

    private val mdl = new NCTestModelAdapter:
        import NCSemanticTestElement as TE
        override val getPipeline: NCPipeline =
            val pl = mkEnPipeline
            val ms = pl.getEntityMappers

            pl.getEntityParsers.add(NCTestUtils.mkEnSemanticParser(TE("a"), TE("b"), TE("c"), TE("d")))

            // Replaces [a, b] -> [ab]
            ms.add(Combiner("a", "b"))
            // Replaces [c, d] -> [cd]
            ms.add(Combiner("c", "d"))
            // Replaces [ab, cd] -> [abcd]
            ms.add(Combiner("ab", "cd"))

            pl

        @NCIntent("intent=abcd term(abcd)={# == 'abcd'}")
        def onMatch(@NCIntentTerm("abcd") abcd: NCEntity): NCResult = new NCResult("OK", NCResultType.ASK_RESULT)

    @Test
    def test(): Unit = Using.resource(new NCModelClient(mdl)) { client =>
        require(client.ask("a b c d", null, "userId").getIntentId == "abcd")
    }
