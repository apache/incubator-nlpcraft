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

package org.apache.nlpcraft.probe.mgrs.deploy1

import org.apache.nlpcraft.model.{NCElement, NCAddElement, NCAddElementClass, NCIntent, NCModelAdapter, NCModelAddClasses, NCModelAddPackage, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util

@NCAddElement("""{ "id": "e5" }""")
@NCAddElement("""{ "id": "e6",  "synonyms": ["e66"] }""")
class NCElementAnnotations1 {
    @NCAddElement("""{ "id": "e7" }""")
    @NCAddElement("""{ "id": "e8" }""")
    def x(): Unit = ()
}

class NCElementAnn1 extends NCElement {
    override def getId: String = "e12"
}
/**
  *
  */
@NCModelAddClasses(Array{classOf[NCElementAnnotations1]})
@NCModelAddPackage(Array("org.apache.nlpcraft.probe.mgrs.deploy1.pack"))
@NCAddElement("""{ "id": "e3" }""")
@NCAddElement("""{ "id": "e4" }""")
class NCElementAnnotationsSpecModel extends NCModelAdapter("nlpcraft.intents.idl.test", "IDL Test Model", "1.0") {
    override def getElements: util.Set[NCElement] = Set(NCTestElement("e1"))

    @NCIntent("intent=onE1 term={# == 'e1'}")
    def onE1(): NCResult = NCResult.text("OK")

    @NCAddElement("""{ "id": "e2" }""")
    @NCIntent("intent=onE2 term={# == 'e2'}")
    def onE2(): NCResult = NCResult.text("OK")

    @NCIntent("intent=onE3 term={# == 'e3'}")
    def onE3(): NCResult = NCResult.text("OK")

    @NCIntent("intent=onE4 term={# == 'e4'}")
    def onE4(): NCResult = NCResult.text("OK")

    @NCIntent("intent=onE5 term={# == 'e5'}")
    def onE5(): NCResult = NCResult.text("OK")

    @NCIntent("intent=onE6 term={# == 'e6'}")
    def onE6(): NCResult = NCResult.text("OK")

    @NCIntent("intent=onE7 term={# == 'e7'}")
    def onE7(): NCResult = NCResult.text("OK")

    @NCIntent("intent=onE8 term={# == 'e8'}")
    def onE8(): NCResult = NCResult.text("OK")

    @NCIntent("intent=onE9 term={# == 'e9'}")
    def onE9(): NCResult = NCResult.text("OK")

    @NCIntent("intent=onE10 term={# == 'e10'}")
    def onE10(): NCResult = NCResult.text("OK")

    @NCIntent("intent=onE11 term={# == 'e11'}")
    def onE11(): NCResult = NCResult.text("OK")

    @NCAddElementClass(classOf[NCElementAnn1])
    @NCIntent("intent=onE12 term={# == 'e12'}")
    def onE12(): NCResult = NCResult.text("OK")
}
/**
 *
 */
@NCTestEnvironment(model = classOf[NCElementAnnotationsSpecModel], startClient = true)
class NCElementAnnotationsSpec extends NCTestContext {
    /**
     *
     */
    @Test
    def test(): Unit = {
        // Defined in model.
        checkIntent("e1", "onE1")

        // Added via annotation to model method.
        checkIntent("e2", "onE2")

        // Added via annotation to model class.
        checkIntent("e3", "onE3")
        checkIntent("e4", "onE4")

        // Added via annotation to class and methods, where class added via NCModelAddClasses.
        // Multiple annotation tested.
        // Complex JSON tested.
        checkIntent("e5", "onE5")
        checkIntent("e66", "onE6")
        checkIntent("e7", "onE7")
        checkIntent("e8", "onE8")

        // Added via annotation to class and method, where class added via NCModelAddPackage.
        // Complex YAML tested.
        checkIntent("e9", "onE9")
        checkIntent("e101", "onE10")
        checkIntent("e11", "onE11")

        // Added via class annotation (second approach).
        checkIntent("e12", "onE12")
    }
}