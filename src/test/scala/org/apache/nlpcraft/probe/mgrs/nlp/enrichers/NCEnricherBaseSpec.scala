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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers

import org.apache.nlpcraft.model.tools.test.{NCTestClient, NCTestClientBuilder}
import org.apache.nlpcraft.probe.embedded.NCEmbeddedProbe
import org.junit.jupiter.api.Assertions.{assertTrue, fail}
import org.junit.jupiter.api.{AfterEach, BeforeEach}
import org.scalatest.Assertions

/**
  * Enrichers tests utility base class.
  */
class NCEnricherBaseSpec {
    private var client: NCTestClient = _

    def getModelClass: Option[Class[_ <: NCDefaultTestModel]] = Some(classOf[NCDefaultTestModel])

    @BeforeEach
    protected def setUp(): Unit = {
        val mdlId = NCDefaultTestModel.ID

        getModelClass match {
            case Some(claxx) ⇒
                println(s"Embedded probe is going to start with model [id=$mdlId, claxx=${claxx.getName}]")

                NCEmbeddedProbe.start(claxx)
            case None ⇒
                println(s"Probe should be already started as external process: $mdlId")
        }

        client = new NCTestClientBuilder().newBuilder.setResponseLog(false).build

        client.open(mdlId)
    }

    @AfterEach
    protected def tearDown(): Unit = {
        if (client != null)
            client.close()

        NCEmbeddedProbe.stop()
    }

    /**
      * Checks single variant.
      *
      * @param txt
      * @param expToks Expected tokens.
      */
    protected def checkExists(txt: String, expToks: NCTestToken*): Unit = {
        val res = client.ask(txt)

        if (res.isFailed)
            fail(s"Result failed [" +
                s"text=$txt, " +
                s"error=${res.getResultError.get()}" +
            s"]")

        assertTrue(res.getResult.isPresent, s"Missed result data")

        val sens = NCTestSentence.deserialize(res.getResult.get()).toSeq
        val expSen = NCTestSentence(expToks)

        assertTrue(
            sens.contains(expSen),
            s"Required token sequence not found [" +
                s"request=$txt, " +
                s"\nexpected=\n$expSen, " +
                s"\nfound=\n${sens.mkString("\n")}" +
            "\n]"
        )
    }

    /**
      * Checks multiple variants.
      *
      * @param txt
      * @param expToks
      */
    protected def checkAll(txt: String, expToks: Seq[NCTestToken]*): Unit = {
        val res = client.ask(txt)

        if (res.isFailed)
            fail(s"Result failed [" +
                s"text=$txt, " +
                s"error=${res.getResultError.get()}" +
            s"]")

        assertTrue(res.getResult.isPresent, s"Missed result data")

        val expSens = expToks.map(NCTestSentence(_))
        val sens = NCTestSentence.deserialize(res.getResult.get()).toSeq

        require(
            expSens.size == sens.size,
            s"Unexpected response size [" +
                s"request=$txt, " +
                s"expected=${expSens.size}, " +
                s"received=${sens.size}" +
            s"]"
        )

        for (expSen ← expSens)
            require(
                sens.contains(expSen),
                s"Required token sequence not found [" +
                    s"request=$txt, " +
                    s"\nexpected=\n$expSen, " +
                    s"\nfound=\n${sens.mkString("\n")}" +
                "\n]"
            )
    }

    /**
      *
      * @param tests
      */
    protected def runBatch(tests: Unit ⇒ Unit*): Unit = {
        val errs = tests.zipWithIndex.flatMap { case (test, i) ⇒
            try {
                test.apply(())

                None
            }
            catch {
                case e: Throwable ⇒ Some(e, i)
            }
        }

        if (errs.nonEmpty) {
            errs.foreach { case (err, i) ⇒
                println(s"${i + 1}. Test failed: ${err.getLocalizedMessage}")

                err.printStackTrace(System.out)
            }

            Assertions.fail(s"\n!!! Failed ${errs.size} from ${tests.size} tests. See errors list above. !!!\n")
        }
        else
            println(s"\n>>> All ${tests.size} tests passed. <<<\n")
    }
}
