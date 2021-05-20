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

package org.apache.nlpcraft.model.properties

import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentMatch, NCModelAdapter, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util.Optional
import java.{lang, util}

abstract class NCTokenPropertiesModelAbstract extends NCModelAdapter(
    "nlpcraft.tokens.prop.test.mdl", "Tokens Properties Test Model", "1.0"
) {
    override def getElements: util.Set[NCElement] = Set(NCTestElement("ab", "a b"), NCTestElement("xy", "x y"))

    @NCIntent("intent=onAB term(t)={tok_id() == 'ab'}")
    def onAB(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=onXY term(t)={tok_id() == 'xy'}")
    def onXY(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    override def isPermutateSynonyms: Boolean = true
    override def isSparse: Boolean = true
}

case class NCPropTestElement(
    id: String, synonym: String, perm: Option[Boolean] = None, sparse: Option[Boolean] = None
) extends NCElement {
    override def getId: String = id
    override def getSynonyms: util.List[String] = util.Collections.singletonList(synonym)

    private def get(opt: Option[Boolean], getSuper: () => Optional[lang.Boolean]): Optional[lang.Boolean] =
        opt match {
            case Some(v) => Optional.of(v)
            case None => getSuper()
        }

    override def isPermutateSynonyms: Optional[lang.Boolean] = get(perm, () => super.isPermutateSynonyms)
    override def isSparse: Optional[lang.Boolean] = get(sparse, () => super.isSparse)
}

// 1. All enabled.
class NCTokenPropertiesModel1() extends NCTokenPropertiesModelAbstract

@NCTestEnvironment(model = classOf[NCTokenPropertiesModel1], startClient = true)
class NCTokenPropertiesModel1Spec extends NCTestContext {
    @Test
    def test(): Unit = {
        checkIntent("a b", "onAB")
        checkIntent("a test test b", "onAB")
        checkIntent("b a", "onAB")
        checkIntent("b test test a", "onAB")
        checkIntent("x y", "onXY")
        checkIntent("x test test y", "onXY")
        checkIntent("y x", "onXY")
        checkIntent("y test test x", "onXY")
    }
}

// 2. Permutation turned off.
class NCTokenPropertiesModel2 extends NCTokenPropertiesModelAbstract {
    override def isPermutateSynonyms: Boolean = false
    override def isSparse: Boolean = true
}

@NCTestEnvironment(model = classOf[NCTokenPropertiesModel2], startClient = true)
class NCTokenPropertiesModel2Spec extends NCTestContext {
    @Test
    def test(): Unit = {
        checkIntent("a b", "onAB")
        checkIntent("a test test b", "onAB")
        fail(
            "b a",
            "b test test a"
        )

        checkIntent("x y", "onXY")
        checkIntent("x test test y", "onXY")
        fail(
            "b a",
            "b test test a",
            "y x",
            "y test test x"
        )
    }
}

// 3. Sparse turned off.
class NCTokenPropertiesModel3 extends NCTokenPropertiesModelAbstract {
    override def isPermutateSynonyms: Boolean = true
    override def isSparse: Boolean = false
}

@NCTestEnvironment(model = classOf[NCTokenPropertiesModel3], startClient = true)
class NCTokenPropertiesModel3Spec extends NCTestContext {
    @Test
    def test(): Unit = {
        checkIntent("a b", "onAB")
        checkIntent("b a", "onAB")
        checkIntent("y x", "onXY")
        checkIntent("x y", "onXY")

        fail(
            "a test b",
            "b test a",
            "x test y",
            "y test x",
            "a test test b",
            "b test test a",
            "x test test y",
            "y test test x"

        )
    }
}

// 4. Permutation and sparse turned off.
class NCTokenPropertiesModel4 extends NCTokenPropertiesModelAbstract {
    override def isPermutateSynonyms: Boolean = false
    override def isSparse: Boolean = false
}

@NCTestEnvironment(model = classOf[NCTokenPropertiesModel4], startClient = true)
class NCTokenPropertiesModel4Spec extends NCTestContext {
    @Test
    def test(): Unit = {
        checkIntent("a b", "onAB")
        fail(
            "b a",
            "a test b",
            "b test a"
        )
    }
}

// 5. Permutation turned off for `ab` but enabled for 'xy'.
// Sparse turned on for both of them.
class NCTokenPropertiesModel5 extends NCTokenPropertiesModelAbstract {
    override def isPermutateSynonyms: Boolean = true
    override def isSparse: Boolean = true

    override def getElements: util.Set[NCElement] = {
        val set: Set[NCElement] = Set(
            NCPropTestElement("ab", "a b", perm = Some(false)),
            NCPropTestElement("xy", "x y", perm = Some(true))
        )

        set.asJava
    }
}

@NCTestEnvironment(model = classOf[NCTokenPropertiesModel5], startClient = true)
class NCTokenPropertiesModel5Spec extends NCTestContext {
    @Test
    def test(): Unit = {
        checkIntent("a b", "onAB")
        checkIntent("a test test b", "onAB")
        fail("b a")

        checkIntent("x y", "onXY")
        checkIntent("x test test y", "onXY")
        checkIntent("y x", "onXY")
        checkIntent("y test test x", "onXY")
    }
}

// 6. Sparse factor and permutation are turned off for `ab` but enabled for 'xy'.
class NCTokenPropertiesModel6 extends NCTokenPropertiesModelAbstract {
    override def isPermutateSynonyms: Boolean = true
    override def isSparse: Boolean = true

    override def getElements: util.Set[NCElement] = {
        val set: Set[NCElement] = Set(
            NCPropTestElement("ab", "a b", sparse = Some(false), perm = Some(false)),
            NCPropTestElement("xy", "x y")
        )

        set.asJava
    }
}

@NCTestEnvironment(model = classOf[NCTokenPropertiesModel6], startClient = true)
class NCTokenPropertiesModel6Spec extends NCTestContext {
    @Test
    def test(): Unit = {
        checkIntent("a b", "onAB")
        fail("b a")
        checkIntent("y x", "onXY")
        checkIntent("x y", "onXY")

        fail(
            "a test b",
            "b test a"
        )
        checkIntent("y test x", "onXY")
        checkIntent("x test y", "onXY")

        fail(
            "a test test b",
            "b test test a"
        )
        checkIntent("y test test x", "onXY")
        checkIntent("x test test y", "onXY")
    }
}

// 7. Sparse factor turned off for `ab` but enabled  for 'xy'.
class NCTokenPropertiesModel7 extends NCTokenPropertiesModelAbstract {
    override def isPermutateSynonyms: Boolean = true
    override def isSparse: Boolean = true

    override def getElements: util.Set[NCElement] = {
        val set: Set[NCElement] = Set(
            NCPropTestElement("ab", "a b", perm = Some(true), sparse = Some(false)),
            NCPropTestElement("xy", "x y", perm = Some(true), sparse = Some(true))
        )

        set.asJava
    }
}

@NCTestEnvironment(model = classOf[NCTokenPropertiesModel7], startClient = true)
class NCTokenPropertiesModel7Spec extends NCTestContext {
    @Test
    def test(): Unit = {
        checkIntent("a b", "onAB")
        checkIntent("b a", "onAB")
        fail("a test b")
        fail("b test a")
        checkIntent("y x", "onXY")
        checkIntent("x y", "onXY")
        checkIntent("y test test x", "onXY")
        checkIntent("x test test y", "onXY")
    }
}

