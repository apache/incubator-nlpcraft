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
import annotations.*
import nlp.parsers.*
import nlp.util.*
import internal.util.NCResourceReader
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Using
/**
  *
  */
class NCEntityMapperSpec extends AnyFunSuite:
    private case class Combiner(ids: String*) extends NCEntityMapper:
        override def map(req: NCRequest, cfg: NCModelConfig, es: List[NCEntity]): List[NCEntity] =
            val replaced = es.filter(p => ids.contains(p.getId))

            if replaced.isEmpty then
                es
            else
                val newEntity: NCEntity = new NCPropertyMapAdapter with NCEntity:
                    override val getTokens: List[NCToken] = replaced.flatMap(_.getTokens).sortBy(_.getIndex).toList
                    override val getRequestId: String = req.getRequestId
                    override val getId: String = ids.mkString

                val buf = collection.mutable.ArrayBuffer.empty[NCEntity]
                buf ++= es

                buf --= replaced
                (buf :+ newEntity).sortBy(_.getTokens.head.getIndex).toList

    private val mdl = new NCTestModelAdapter:
        override val getPipeline: NCPipeline =
            import NCSemanticTestElement as TE
            val pl = mkEnPipeline
            val ms = pl.entMappers

            pl.entParsers += NCTestUtils.mkEnSemanticParser(TE("a"), TE("b"), TE("c"), TE("d"))

            // Replaces [a, b] -> [ab]
            ms += Combiner("a", "b")
            // Replaces [c, d] -> [cd]
            ms += Combiner("c", "d")
            // Replaces [ab, cd] -> [abcd]
            ms += Combiner("ab", "cd")

            pl

        @NCIntent("intent=abcd term(abcd)={# == 'abcd'}")
        def onMatch(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("abcd") abcd: NCEntity): NCResult = TEST_RESULT

    test("test") {
        Using.resource(new NCModelClient(mdl)) { client =>
            require(client.ask("a b c d", "userId").getIntentId.orNull == "abcd")
        }
    }
