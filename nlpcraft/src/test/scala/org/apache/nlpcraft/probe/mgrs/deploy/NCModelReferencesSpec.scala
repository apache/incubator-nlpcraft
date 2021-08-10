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

package org.apache.nlpcraft.probe.mgrs.deploy

import org.apache.nlpcraft.model.{NCElement, NCModelAdapter, NCModelAddPackage}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util

/**
  *
  */
@NCTestEnvironment(model = classOf[org.apache.nlpcraft.probe.mgrs.deploy.scalatest.NCModelClassesWrapper], startClient = true)
class NCModelClassesWrapperScalaSpec extends NCTestContext {
    /**
     *
     */
    @Test
    def test(): Unit = {
        checkIntent("scalaClass", "scalaClass")
        checkIntent("scalaStatic", "scalaStatic")
        checkFail("javaClass")
        checkFail("javaStatic")
    }
}

/**
 *
 */
@NCTestEnvironment(model = classOf[org.apache.nlpcraft.probe.mgrs.deploy.scalatest.NCModelPackagesWrapper], startClient = true)
class NCModelPackagesWrapperScalaSpec extends NCTestContext {
    /**
     *
     */
    @Test
    def test(): Unit = {
        checkIntent("scalaClass", "scalaClass")
        checkIntent("scalaStatic", "scalaStatic")
        checkFail("javaClass")
        checkFail("javaStatic")
    }
}

/**
 *
 */
@NCTestEnvironment(model = classOf[org.apache.nlpcraft.probe.mgrs.deploy.javatest.NCModelClassesWrapper], startClient = true)
class NCModelClassesWrapperJavaSpec extends NCTestContext {
    /**
     *
     */
    @Test
    def test(): Unit = {
        checkIntent("javaClass", "javaClass")
        checkIntent("javaStatic", "javaStatic")
        checkFail("scalaClass")
        checkFail("scalaStatic")
    }
}

/**
 *
 */
@NCTestEnvironment(model = classOf[org.apache.nlpcraft.probe.mgrs.deploy.javatest.NCModelPackagesWrapper], startClient = true)
class NCModelPackagesWrapperJavaSpec extends NCTestContext {
    /**
     *
     */
    @Test
    def test(): Unit = {
        checkIntent("javaClass", "javaClass")
        checkIntent("javaStatic", "javaStatic")
        checkFail("scalaClass")
        checkFail("scalaStatic")
    }
}

/**
 *
 */
@NCModelAddPackage(Array("org.apache.nlpcraft.probe.mgrs.deploy"))
class NCModelPackagesWrapperMix extends NCModelAdapter("nlpcraft.deploy.test.mix.mdl", "Test Model", "1.0") {
    override def getElements: util.Set[NCElement] =
        Set(
            NCTestElement("scalaClass"),
            NCTestElement("scalaStatic"),
            NCTestElement("javaClass"),
            NCTestElement("javaStatic")
        )
}

/**
 *
 */
@NCTestEnvironment(model = classOf[org.apache.nlpcraft.probe.mgrs.deploy.NCModelPackagesWrapperMix], startClient = true)
class NCModelPackagesWrapperMixSpec extends NCTestContext {
    /**
     *
     */
    @Test
    def test(): Unit = {
        checkIntent("scalaClass", "scalaClass")
        checkIntent("scalaStatic", "scalaStatic")
        checkIntent("javaClass", "javaClass")
        checkIntent("javaStatic", "javaStatic")
    }
}
