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

package org.apache.nlpcraft

import org.apache.nlpcraft.model.tools.embedded.NCEmbeddedProbe
import org.apache.nlpcraft.model.tools.test.{NCTestClient, NCTestClientBuilder}
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api._

import java.util.Collections

/**
  *
  */
@TestInstance(Lifecycle.PER_CLASS)
abstract class NCTestContext {
    private final val MDL_CLASS = classOf[NCTestEnvironment]

    private var cli: NCTestClient = _
    private var probeStarted = false

    @BeforeEach
    @throws[Exception]
    private def beforeEach(info: TestInfo): Unit = start0(() => getMethodAnnotation(info))

    @BeforeAll
    @throws[Exception]
    private def beforeAll(info: TestInfo): Unit = start0(() => getClassAnnotation(info))

    @AfterEach
    @throws[Exception]
    private def afterEach(info: TestInfo): Unit =
        if (getMethodAnnotation(info).isDefined)
            stop0()

    @AfterAll
    @throws[Exception]
    private def afterAll(info: TestInfo): Unit =
        if (getClassAnnotation(info).isDefined)
            stop0()

    protected def getClassAnnotation(info: TestInfo): Option[NCTestEnvironment] =
        if (info.getTestClass.isPresent) Option(info.getTestClass.get().getAnnotation(MDL_CLASS)) else None

    private def getMethodAnnotation(info: TestInfo): Option[NCTestEnvironment] =
        if (info.getTestMethod.isPresent) Option(info.getTestMethod.get().getAnnotation(MDL_CLASS)) else None

    @throws[Exception]
    private def start0(extract: () => Option[NCTestEnvironment]): Unit =
        extract() match {
            case Some(ann) =>
                if (probeStarted || cli != null)
                    throw new IllegalStateException(
                        "Model already initialized. " +
                        s"Note that '@${classOf[NCTestEnvironment].getSimpleName}' can be set for class or method, " +
                        s"but not both of them."
                    )

                preProbeStart()

                probeStarted = false

                if (NCEmbeddedProbe.start(null, Collections.singletonList(ann.model().getName))) {
                    probeStarted = true
                
                    if (ann.startClient()) {
                        cli = new NCTestClientBuilder().newBuilder.setResponseLog(ann.clientLog()).build
                        
                        cli.open(NCModelManager.getAllModels().head.model.getId)
                    }
                }
                
            case None => // No-op.
        }

    @throws[Exception]
    private def stop0(): Unit = {
        if (cli != null) {
            cli.close()

            cli = null
        }

        if (probeStarted) {
            NCEmbeddedProbe.stop()

            probeStarted = false

            afterProbeStop()
        }
    }

    protected def preProbeStart(): Unit = { }

    protected def afterProbeStop(): Unit = { }

    /**
      *
      * @param txt
      * @param intent
      */
    protected def checkIntent(txt: String, intent: String): Unit = {
        val res = getClient.ask(txt)

        assertTrue(res.isOk, s"Checked: $txt")
        assertTrue(res.getResult.isPresent, s"Checked: $txt")
        assertEquals(intent, res.getIntentId, s"Checked: $txt")
    }

    /**
      *
      * @param txtsInts
      */
    protected def checkIntents(txtsInts: (String, String)*): Unit =
        for ((txt, intent) <- txtsInts) checkIntent(txt, intent)

    /**
      *
      * @param txt
      */
    protected def checkFail(txt: String): Unit =
        require(getClient.ask(txt).isFailed)

    /**
      * @param req
      * @param expResp
      */
    protected def checkResult(req: String, expResp: String): Unit = {
        val res = getClient.ask(req)

        assertTrue(res.isOk, s"Unexpected result, error=${res.getResultError.orElse(null)}")
        assertTrue(res.getResult.isPresent)
        assertEquals(expResp, res.getResult.get)
    }

    final protected def getClient: NCTestClient = {
        if (cli == null)
            throw new IllegalStateException("Client is not started.")

        cli
    }
}