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

package org.apache.nlpcraft.model.sparse

import org.apache.nlpcraft.model.`abstract`.NCAbstractTokensModel
import org.apache.nlpcraft.model.{NCContext, NCElement, NCResult, NCToken}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
import scala.collection.JavaConverters._
import scala.collection.mutable

class NCSparseModel extends NCAbstractTokensModel {
    override def getElements: util.Set[NCElement] = Set(NCTestElement("xyz", "x y z"))

    override def isPermutateSynonyms: Boolean = true
    override def isSparse: Boolean = true

    override def onContext(ctx: NCContext): NCResult = {
        val variants = ctx.getVariants.asScala

        def checkOneVariant(sparsity: Int): Unit = {
            require(variants.size == 1, "There is should be single variant.")

            val toks = variants.head.asScala.filter(_.getId == "xyz")

            require(toks.size == 3, "There are should be 3 `xyz` tokens.")

            checkSparsity(sparsity, toks)
        }

        def checkSparsity(sparsity: Int, toks: mutable.Buffer[NCToken]): Unit =
            require(
                toks.forall(_.getMetadata.get("nlpcraft:nlp:sparsity").asInstanceOf[Int] == sparsity),
                s"Sparsity of each tokens should be: $sparsity."
            )

        def checkExists(sparsity: Int): Unit =
            require(
                variants.exists(v => {
                    val toks = v.asScala.filter(_.getId == "xyz")

                    toks.size match {
                        case 3 =>
                            checkSparsity(sparsity, toks)

                            true
                        case _ =>
                            false
                    }
                }),
                s"Variant with 3 `xyz` tokens should be exists."
            )

        ctx.getRequest.getNormalizedText match {
            case "x y z x y z x y z" => checkOneVariant(0)
            case "x y z test x y z test x y z test" => checkOneVariant(0)
            case "x test y z x test y z x y test z" => checkOneVariant(1)
            case "x z y x z y x z y" => checkExists(0)
            case "x z y test x z y test x z y test" => checkExists(0)
            case "x test z y x test z y x test z y" => checkExists(1)

            case _ => throw new AssertionError(s"Unexpected request: ${ctx.getRequest.getNormalizedText}")
        }

        NCResult.text("OK")
    }
}

@NCTestEnvironment(model = classOf[NCSparseModel], startClient = true)
class NCSparseSpec extends NCTestContext {
    @Test
    def test(): Unit = {
        checkResult("x y z x y z x y z", "OK")
        checkResult("x y z test x y z test x y z test", "OK")
        checkResult("x test y z x test y z x y test z", "OK")

        // We don't check for sparsity > 1 because logic of synonyms permutation (neighbors only).
        // Tests will not be clear.

        checkResult("x z y x z y x z y", "OK")
        checkResult("x z y test x z y test x z y test", "OK")
        checkResult("x test z y x test z y x test z y", "OK")
    }
}