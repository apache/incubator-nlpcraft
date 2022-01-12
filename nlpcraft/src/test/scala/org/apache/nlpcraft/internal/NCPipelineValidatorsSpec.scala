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

package org.apache.nlpcraft.internal

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.entity.parser.nlp.NCNlpEntityParser
import org.apache.nlpcraft.nlp.util.*
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.*

import java.util
import java.util.List as JList
import scala.concurrent.CancellationException
import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCPipelineValidatorsSpec:
    class TestException extends Exception

    /**
      *
      * @param addComponent
      * @return
      */
    private def prepare(addComponent: NCTestPipeline => Unit): NCPipelineProcessor.VariantsHolder =
        val pipeline = EN_PIPELINE.clone()

        pipeline.getEntityParsers.clear()
        pipeline.getEntityParsers.add(new NCNlpEntityParser)
        addComponent(pipeline)

        NCPipelineProcessor(new NCModelAdapter(CFG, pipeline)).
            prepare("test1 test2", null, "testId")

    /**
      *
      * @param addComponent
      * @param isError
      * @param expVariantCnt
      */
    private def testOk(addComponent: NCTestPipeline => Unit, expVariantCnt: Int): Unit =
        val h = prepare(addComponent)

        println(s"Variants count: ${h.variants.size}")

        for ((v, idx) <- h.variants.zipWithIndex)
            println(s"Variant: $idx")
            NCTestUtils.printEntities(h.request.getText, v.getEntities.asScala.toSeq)

        require(h.variants.sizeIs == expVariantCnt)

    /**
      *
      * @param addComponent
      * @param isError
      * @param expVariantCnt
      */
    private def testError(addComponent: NCTestPipeline => Unit): Unit =
        Assertions.assertThrows(classOf[TestException], () => prepare(addComponent))

    @Test
    def testNoValidator(): Unit = testOk(_ => (), expVariantCnt = 1)

    @Test
    def testVariantValidator1(): Unit =
        val v = new NCVariantValidator:
            override def filter(req: NCRequest, cfg: NCModelConfig, variants: JList[NCVariant]): JList[NCVariant] =
                java.util.Collections.emptyList()

        testOk(_.getVariantValidators.add(v), expVariantCnt = 0)

    @Test
    def testVariantValidator2(): Unit =
        val v = new NCVariantValidator:
            override def filter(req: NCRequest, cfg: NCModelConfig, variants: JList[NCVariant]): JList[NCVariant] =
                variants

        testOk(_.getVariantValidators.add(v), expVariantCnt = 1)

    @Test
    def testTokenValidator1(): Unit =
        val v = new NCTokenValidator:
            override def validate(req: NCRequest, cfg: NCModelConfig, toks: JList[NCToken]): Unit =
                throw new TestException()

        testError(_.getTokenValidators.add(v))

    @Test
    def testTokenValidator2(): Unit =
        val v = new NCTokenValidator:
            override def validate(req: NCRequest, cfg: NCModelConfig, toks: JList[NCToken]): Unit = ()

        testOk(_.getTokenValidators.add(v), 1)

    @Test
    def testEntityValidator1(): Unit =
        val v = new NCEntityValidator:
            override def validate(req: NCRequest, cfg: NCModelConfig, ents: JList[NCEntity]): Unit =
                throw new TestException()

        testError(_.getEntityValidators.add(v))

    @Test
    def testEntityValidator2(): Unit =
        val v = new NCEntityValidator:
            override def validate(req: NCRequest, cfg: NCModelConfig, ents: JList[NCEntity]): Unit = ()

        testOk(_.getEntityValidators.add(v), 1)