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

package org.apache.nlpcraft.model.dialog

import org.apache.nlpcraft.model.{NCDialogFlowItem, NCElement, NCIntent, NCModel, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util

object NCDialogSpecModelFlow2  {
    var error = false
}

import NCDialogSpecModelFlow2._

/**
  * Test model.
  */
class NCDialogSpecModelFlow2  {
    def invalidDef1(flow: java.util.List[NCDialogFlowItem]): String = "string"
    def invalidDef2(): Boolean = true
    def invalidDef3(flow: java.util.List[NCDialogFlowItem]): Boolean = throw new IllegalStateException()
    def invalidDef4(flow: java.util.List[NCDialogFlowItem]): java.lang.Boolean = null
    def validDef(flow: java.util.List[NCDialogFlowItem]): Boolean = {
        if (error)
            throw new IllegalStateException()

        true
    }
}

/**
  *
  */
class NCDialogSpecModel21 extends NCModel {
    override def getId: String = this.getClass.getSimpleName
    override def getName: String = this.getClass.getSimpleName
    override def getVersion: String = "1.0.0"

    override def getElements: util.Set[NCElement] = Set(NCTestElement("a"))

    @NCIntent("intent=onA flow=/org.apache.nlpcraft.model.dialog.NCDialogSpecModelFlow2#invalidDef1/ term~{tok_id() == 'a'}")
    def onA(): NCResult = NCResult.text("ok")
}

/**
  *
  */
@NCTestEnvironment(model = classOf[NCDialogSpecModel21], startClient = true)
class NCDialogSpec21 extends NCTestContext {
    @Test
    def test(): Unit = require(getClient.ask("a").isFailed)
}

/**
  *
  */
class NCDialogSpecModel22 extends NCDialogSpecModel21 {
    @NCIntent("intent=onA flow=/org.apache.nlpcraft.model.dialog.NCDialogSpecModelFlow2#invalidDef2/ term~{tok_id() == 'a'}")
    override def onA(): NCResult = NCResult.text("ok")
}

/**
  *
  */
@NCTestEnvironment(model = classOf[NCDialogSpecModel22], startClient = true)
class NCDialogSpec22 extends NCDialogSpec21

/**
  *
  */
class NCDialogSpecModel23 extends NCDialogSpecModel21 {
    @NCIntent("intent=onA flow=/org.apache.nlpcraft.model.dialog.NCDialogSpecModelFlow2#invalidDef3/ term~{tok_id() == 'a'}")
    override def onA(): NCResult = NCResult.text("ok")
}
/**
  *
  */
@NCTestEnvironment(model = classOf[NCDialogSpecModel23], startClient = true)
class NCDialogSpec23 extends NCDialogSpec21

/**
  *
  */
class NCDialogSpecModel24 extends NCDialogSpecModel21 {
    @NCIntent("intent=onA flow=/org.apache.nlpcraft.model.dialog.NCDialogSpecModelFlow2#invalidDef4/ term~{tok_id() == 'a'}")
    override def onA(): NCResult = NCResult.text("ok")
}
/**
  *
  */
@NCTestEnvironment(model = classOf[NCDialogSpecModel24], startClient = true)
class NCDialogSpec24 extends NCDialogSpec21
/**
  *
  */
class NCDialogSpecModel25 extends NCDialogSpecModel21 {
    @NCIntent("intent=onA flow=/org.apache.nlpcraft.model.dialog.NCDialogSpecModelFlow2#validDef/ term~{tok_id() == 'a'}")
    override def onA(): NCResult = NCResult.text("ok")
}
/**
  *
  */
@NCTestEnvironment(model = classOf[NCDialogSpecModel25], startClient = true)
class NCDialogSpec25 extends NCTestContext {
    @Test
    def test(): Unit = {
        def test(txt: String, exp: Boolean): Unit = {
            NCDialogSpecModelFlow2.error = !exp

            require(getClient.ask("a").isOk == exp)
        }

        test(txt = "a", exp = true)
        test(txt = "a", exp = false)
        test(txt = "a", exp = true)
    }
}



