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

package org.apache.nlpcraft.model.intent.idl.compiler.functions

import org.apache.nlpcraft.common.ScalaMeta
import org.apache.nlpcraft.model.intent.compiler.{NCIdlCompiler, NCIdlCompilerGlobal}
import org.apache.nlpcraft.model.intent.{NCIdlContext, NCIdlTerm}
import org.apache.nlpcraft.model.{NCCompany, NCModel, NCModelView, NCRequest, NCToken, NCUser}
import org.junit.jupiter.api.BeforeEach

import java.util
import java.util.{Collections, Optional}
import scala.collection.JavaConverters._
import scala.language.implicitConversions

/**
  * Tests for IDL functions.
  */
private[functions] trait NCIdlFunctions {
    private final val MODEL_ID = "test.mdl.id"

    final val MODEL: NCModel = new NCModel {
        override val getId: String = MODEL_ID
        override val getName: String = MODEL_ID
        override val getVersion: String = "1.0.0"

        override def getOrigin: String = "test"
    }

    @BeforeEach
    def before(): Unit = NCIdlCompilerGlobal.clearCache(MODEL_ID)

    case class TestDesc(truth: String, token: Option[NCToken] = None, idlCtx: NCIdlContext = ctx()) {
        val term: NCIdlTerm = {
            val intents = NCIdlCompiler.compileIntents(s"intent=i term(t)={$truth}", MODEL, MODEL_ID)

            require(intents.size == 1)
            require(intents.head.terms.size == 1)

            intents.head.terms.head
        }

        override def toString: String =
            token match {
                case Some(t) ⇒ s"Predicate [body='$truth', token=${t2s(t)}]"
                case None ⇒ s"Predicate '$truth'"
            }
    }

    object TestDesc {
        def apply(truth: String, token: NCToken, idlCtx: NCIdlContext): TestDesc =
            TestDesc(truth = truth, token = Some(token), idlCtx = idlCtx)

        def apply(truth: String, token: NCToken): TestDesc =
            TestDesc(truth = truth, token = Some(token))
    }

    private def t2s(t: NCToken) = {
        def nvl(s: String, name: String): String = if (s != null) s else s"$name (not set)"

        s"text=${nvl(t.getOriginalText, "text")} [${nvl(t.getId, "id")}]"
    }

    protected def ctx(
        usr: NCUser = null,
        comp: NCCompany = null,
        srvReqId: String = null,
        normTxt: String = null,
        recTimestamp: Long = 0,
        remAddress: String = null,
        clientAgent: String = null,
        reqData: ScalaMeta = Map.empty[String, AnyRef],
        intentMeta: ScalaMeta = Map.empty[String, Object],
        convMeta: ScalaMeta = Map.empty[String, Object],
        fragMeta: ScalaMeta = Map.empty[String, Object]
    ): NCIdlContext =
        NCIdlContext(
            intentMeta = intentMeta,
            convMeta = convMeta,
            fragMeta = fragMeta,
            req =
                new NCRequest() {
                    override def getUser: NCUser = usr
                    override def getCompany: NCCompany = comp
                    override def getServerRequestId: String = srvReqId
                    override def getNormalizedText: String = normTxt
                    override def getReceiveTimestamp: Long = recTimestamp
                    override def getRemoteAddress: Optional[String] = Optional.ofNullable(remAddress)
                    override def getClientAgent: Optional[String] = Optional.ofNullable(clientAgent)
                    override def getRequestData: util.Map[String, AnyRef] = reqData.asJava
                }
        )

    protected def tkn(
        id: String = null,
        srvReqId: String = null,
        parentId: String = null,
        value: String = null,
        txt: String = null,
        start: Int = 0,
        end: Int = 0,
        groups: Seq[String] = Seq.empty,
        ancestors: Seq[String] = Seq.empty,
        aliases: Set[String] = Set.empty,
        partTokens: Seq[NCToken] = Seq.empty,
        meta: Map[String, AnyRef] = Map.empty[String, AnyRef]
    ): NCToken = {
        val map = new util.HashMap[String, AnyRef]

        map.putAll(meta.asJava)

        map.put("nlpcraft:nlp:origtext", txt)
        map.put("nlpcraft:nlp:origtext", txt)

        new NCToken {
            override def getModel: NCModelView = MODEL
            override def getServerRequestId: String = srvReqId
            override def getId: String = id
            override def getParentId: String = parentId
            override def getAncestors: util.List[String] = ancestors.asJava
            override def getPartTokens: util.List[NCToken] = partTokens.asJava
            override def getAliases: util.Set[String] = aliases.asJava
            override def getValue: String = value
            override def getGroups: util.List[String] =
                if (groups.isEmpty && id != null) Collections.singletonList(id) else groups.asJava
            override def getStartCharIndex: Int = start
            override def getEndCharIndex: Int = end
            override def isAbstract: Boolean = false
            override def getMetadata: util.Map[String, AnyRef] = map
        }
    }

    protected def test(funcs: TestDesc*): Unit =
        for (f ← funcs) {
            val res =
                try {
                    // Process declarations.
                    f.idlCtx.vars ++= f.term.decls

                    // Execute term's predicate.
                    f.term.pred.apply(f.token.getOrElse(tkn()), f.idlCtx).value
                }
                catch {
                    case e: Exception ⇒ throw new Exception(s"Execution error processing: $f", e)
                }

            res match {
                case b: java.lang.Boolean ⇒ require(b, s"Unexpected FALSE result for: $f")
                case _ ⇒
                    require(
                        requirement = false,
                        s"Unexpected result type [" +
                            s"resType=${if (res == null) "null" else res.getClass.getName}, " +
                            s"resValue=$res, " +
                            s"function=$f" +
                            s"]"
                    )
            }
        }

    protected implicit def convert(pred: String): TestDesc = TestDesc(truth = pred)
}
