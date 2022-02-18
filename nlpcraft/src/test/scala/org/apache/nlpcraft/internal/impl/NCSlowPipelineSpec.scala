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

package org.apache.nlpcraft.internal.impl

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.util.NCTestModelAdapter
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.*

import java.util.List as JList
import java.util.concurrent.*
import scala.jdk.CollectionConverters.*
import scala.util.Using

/**
  *
  */
class NCSlowPipelineSpec:
    /**
      *
      * @param delayMs
      * @param iterCnt
      * @return
      */
    private def mkSlowModel(delayMs: Long, iterCnt: Int): NCModel =
        val pipeline = EN_PIPELINE.clone()

        pipeline.getEntityParsers.clear()

        def mkSlowParser(i: Int) =
            new NCEntityParser:
                override def parse(req: NCRequest, cfg: NCModelConfig, toks: JList[NCToken]): JList[NCEntity] =
                    println(s"Parser called: $i")
                    Thread.sleep(delayMs)
                    java.util.Collections.emptyList()

        (0 until iterCnt).foreach(i => pipeline.getEntityParsers.add(mkSlowParser(i)))

        new NCTestModelAdapter:
            override val getPipeline: NCModelPipeline = pipeline
    /**
      *
      */
    @Test
    def testCancel(): Unit =
        Using.resource(new NCModelClient(mkSlowModel(1, 10000))) { client =>
            val fut = client.ask("any", null, "userId")

            Thread.sleep(20)
            require(fut.cancel(true))
            Thread.sleep(20)

            Assertions.assertThrows(classOf[CancellationException], () => fut.get)
        }
    /**
      *
      */
    @Test
    def testTimeout(): Unit =
        Using.resource(new NCModelClient(mkSlowModel(1, 10000))) { client =>
            val fut = client.ask("any", null, "userId")

            Thread.sleep(20)

            try Assertions.assertThrows(classOf[TimeoutException], () => fut.get(1, TimeUnit.MILLISECONDS))
            finally fut.cancel(true)
        }
