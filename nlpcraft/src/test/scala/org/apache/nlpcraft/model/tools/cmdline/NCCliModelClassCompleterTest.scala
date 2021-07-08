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

package org.apache.nlpcraft.model.tools.cmdline

import org.apache.nlpcraft.common.version.NCVersion
import org.junit.jupiter.api.Test

import java.util
import scala.jdk.CollectionConverters.SetHasAsScala

/**
 * Unit test for the classpath completer. Note that working directory must be set
 * properly for this test to work.
 */
class NCCliModelClassCompleterTest {
    @Test
    def testClasspathCompleter(): Unit = {
        val cp = new util.ArrayList[String]()
        val completer = new NCCliModelClassCompleter()
        val ver = NCVersion.getCurrent.version

        // NOTE: make sure to properly set the current working directory for the runtime configuration
        // when running this test.
        cp.add(s".\\nlpcraft-examples\\lightswitch\\target\\nlpcraft-example-lightswitch-$ver.jar")
        cp.add(s".\\nlpcraft-examples\\alarm\\target\\nlpcraft-example-alarm-$ver.jar")
        cp.add(s".\\nlpcraft-examples\\weather\\target\\nlpcraft-example-weather-$ver.jar")
        cp.add(s".\\nlpcraft-examples\\time\\target\\nlpcraft-example-time-$ver.jar")

        val mdlClasses = completer.getModelClassNamesFromClasspath(cp).asScala

        mdlClasses.foreach(println)
    }
}
