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

package org.apache.nlpcraft.model.intent.idl.compiler

import org.apache.nlpcraft.model.intent.compiler.{NCIdlCompiler, NCIdlCompilerGlobal}
import org.apache.nlpcraft.model.intent.{NCIdlContext, NCIdlFunction, NCIdlStackItem}
import org.apache.nlpcraft.model.{NCModel, NCModelView, NCToken}
import org.junit.jupiter.api.{BeforeEach, Test}

import java.util
import java.util.{Collections, UUID}
import scala.collection.JavaConverters._

/**
  * Tests for IDL functions.
  */
class NCIdlCompilerSpecFunctions {
    private final val MODEL_ID = "test.mdl.id"

    private final val MODEL: NCModel = new NCModel {
        override val getId: String = MODEL_ID
        override val getName: String = MODEL_ID
        override val getVersion: String = "1.0.0"

        override def getOrigin: String = "test"
    }

    private final val DUMMY_CTX: NCIdlContext = NCIdlContext(req = null)

    @BeforeEach
    def before(): Unit = NCIdlCompilerGlobal.clearCache(MODEL_ID)

    import BoolFunc._

    case class BoolFunc(func: NCIdlFunction, token: NCToken, result: Boolean) {
        override def toString: String =
            s"Boolean function [" +
                s"token=${t2s(token)}, " +
                s"function=$func, " +
                s"expected=$result" +
                s"]"
    }

    object BoolFunc {
        private def t2s(t: NCToken) = s"${t.getOriginalText} (${t.getId})"

        private def mkToken(
            id: String = null,
            value: String = null,
            txt: String = null,
            start: Int = 0,
            end: Int = 0,
            meta: Map[String, AnyRef] = Map.empty[String, AnyRef]
        ): NCToken = {
            val map = new util.HashMap[String, AnyRef]

            map.putAll(meta.asJava)

            def nvl(v: String): String = if (v != null) v else "(not set)"

            map.put("nlpcraft:nlp:origtext", nvl(txt))

            new NCToken {
                override def getModel: NCModelView = MODEL
                override def getServerRequestId: String = UUID.randomUUID().toString
                override def getId: String = nvl(id)
                override def getParentId: String = null
                override def getAncestors: util.List[String] = Collections.emptyList()
                override def getPartTokens: util.List[NCToken] = Collections.emptyList()
                override def getAliases: util.Set[String] = Collections.emptySet()
                override def getValue: String = value
                override def getGroups: util.List[String] = Collections.singletonList(id)
                override def getStartCharIndex: Int = start
                override def getEndCharIndex: Int = end
                override def isAbstract: Boolean = false
                override def getMetadata: util.Map[String, AnyRef] = map
            }
        }

        private def mkFunc(term: String): NCIdlFunction = {
            val intents = NCIdlCompiler.compileIntents(s"intent=i term(t)={$term}", MODEL, MODEL_ID)

            require(intents.size == 1)

            val intent = intents.head

            require(intent.terms.size == 1)

            new NCIdlFunction() {
                override def apply(v1: NCToken, v2: NCIdlContext): NCIdlStackItem = intent.terms.head.pred.apply(v1, v2)
                override def toString(): String = s"Function, based on term: $term"
            }
        }

        def apply(boolCondition: String, token: String, result: Boolean): BoolFunc =
            BoolFunc(func = mkFunc(boolCondition), token = mkToken(token), result = result)

        def apply(boolCondition: String, tokenId: String): BoolFunc =
            BoolFunc(func = mkFunc(boolCondition), token = mkToken(tokenId), result = true)

        def apply(boolCondition: String, token: NCToken, result: Boolean): BoolFunc =
            BoolFunc(func = mkFunc(boolCondition), token, result = result)

        def apply(bool: String): BoolFunc =
            BoolFunc(func = mkFunc(bool), mkToken(), result = true)
    }

    private def test(funcs: BoolFunc*): Unit =
        for ((func, idx) ← funcs.zipWithIndex) {
            val res =
                try
                    func.func.apply(func.token, DUMMY_CTX).value
                catch {
                    case e: Exception ⇒ throw new Exception(s"Execution error [index=$idx, testFunc=$func]", e)
                }

                res match {
                    case b: java.lang.Boolean ⇒
                        require(b == func.result,
                            s"Unexpected result [" +
                                s"index=$idx, " +
                                s"testFunc=$func, " +
                                s"expected=${func.result}, " +
                                s"result=$res" +
                                s"]"
                        )
                    case _ ⇒
                        require(requirement = false,
                            s"Unexpected result type [" +
                                s"index=$idx, " +
                                s"testFunc=$func, " +
                                s"expected=${func.result}, " +
                                s"result=$res" +
                                s"]"
                        )
                }

        }

    @Test
    def test(): Unit = {
        val now = System.currentTimeMillis()

        test(
            BoolFunc(boolCondition = "id() == 'a'", tokenId = "a"),

            // Math.
            // BoolFunc(boolCondition = "sin(90.0) == 0")
            // BoolFunc(boolCondition = "rand() < 1")

            // String.
            BoolFunc(bool = "trim(' a b  ') == 'a b'"),
            BoolFunc(bool = "strip(' a b  ') == 'a b'"),
            BoolFunc(bool = "uppercase('aB') == 'AB'"),
            BoolFunc(bool = "lowercase('aB') == 'ab'"),
            BoolFunc(bool = "is_num('a') == false"),
            BoolFunc(bool = "is_num('1') == true"),

            // Statistical.
            // BoolFunc(boolCondition = "max(list(1, 2, 3)) == 3"),
            // BoolFunc(boolCondition = "min(list(1, 2, 3)) == 1")

            // Collection.
            // BoolFunc(boolCondition = "first(list(1, 2, 3)) == 1"),
            // BoolFunc(boolCondition = "last(list(1, 2, 3)) == 3")
            BoolFunc(bool = "is_empty(list()) == true"),
            BoolFunc(bool = "is_empty(list(1)) == false"),
            BoolFunc(bool = "non_empty(list()) == false"),
            BoolFunc(bool = "non_empty(list(1)) == true"),

            // Date-time functions.


            BoolFunc(bool = s"now() - $now < 1000")
        )
    }
}
