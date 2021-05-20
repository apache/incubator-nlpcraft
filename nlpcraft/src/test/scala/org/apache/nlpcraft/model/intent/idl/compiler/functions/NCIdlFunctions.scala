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

import org.apache.nlpcraft.common.{NCE, ScalaMeta}
import org.apache.nlpcraft.model.impl.NCMetadataAdapter
import org.apache.nlpcraft.model.intent.compiler.{NCIdlCompiler, NCIdlCompilerGlobal}
import org.apache.nlpcraft.model.intent.{NCIdlContext, NCIdlTerm}
import org.apache.nlpcraft.model.{NCCompany, NCModel, NCModelView, NCRequest, NCToken, NCTokenPredicateContext, NCTokenPredicateResult, NCUser}
import org.junit.jupiter.api.BeforeEach

import java.util
import java.util.{Collections, Optional}
import scala.language.implicitConversions

/**
  * Tests for IDL functions.
  */
private[functions] trait NCIdlFunctions {
    private final val MODEL_ID = "test.mdl.id"

    // It shouldn't be anonimous class because we need access to 'trueAlwaysCustomToken' method via reflection.
    class TestModel extends NCModel {
        override val getId: String = MODEL_ID
        override val getName: String = MODEL_ID
        override val getVersion: String = "1.0.0"

        override def getOrigin: String = "test"

        def trueAlwaysCustomToken(ctx: NCTokenPredicateContext): NCTokenPredicateResult =
            new NCTokenPredicateResult(true, 1)
    }

    final val MODEL: NCModel = new TestModel()

    @BeforeEach
    def before(): Unit = NCIdlCompilerGlobal.clearCache(MODEL_ID)

    case class TestDesc(
        truth: String,
        token: Option[NCToken] = None,
        idlCtx: NCIdlContext = ctx(),
        isCustom: Boolean = false,
        expectedRes: Boolean = true,
        tokensUsed: Option[Int] = None
    ) {
        // It should be lazy for errors verification methods.
        lazy val term: NCIdlTerm = {
            val (s1, s2) = if (isCustom) ('/', '/') else ('{', '}')

            val intents = NCIdlCompiler.compileIntents(s"intent=i term(t)=$s1$truth$s2", MODEL, MODEL_ID)

            require(intents.size == 1)
            require(intents.head.terms.size == 1)

            intents.head.terms.head
        }

        override def toString: String =
            token match {
                case Some(t) => s"Predicate [body='$truth', token=${t2s(t)}]"
                case None => s"Predicate '$truth'"
            }
    }

    object TestDesc {
        def apply(truth: String, token: NCToken, idlCtx: NCIdlContext): TestDesc =
            TestDesc(truth = truth, token = Some(token), idlCtx = idlCtx)

        def apply(truth: String, token: NCToken): TestDesc =
            TestDesc(truth = truth, token = Some(token))
    }

    private def t2s(t: NCToken): String = {
        def nvl(s: String, name: String): String = if (s != null) s else s"$name (not set)"

        s"text=${nvl(t.getOriginalText, "text")} [${nvl(t.getId, "id")}]"
    }

    protected def ctx(
        reqUsr: NCUser = null,
        reqComp: NCCompany = null,
        reqSrvReqId: String = null,
        reqNormText: String = null,
        reqTstamp: Long = 0,
        reqAddr: String = null,
        reqAgent: String = null,
        reqData: ScalaMeta = Map.empty[String, AnyRef],
        intentMeta: ScalaMeta = Map.empty[String, AnyRef],
        convMeta: ScalaMeta = Map.empty[String, AnyRef],
        fragMeta: ScalaMeta = Map.empty[String, AnyRef]
    ): NCIdlContext =
        NCIdlContext(
            intentMeta = intentMeta,
            convMeta = convMeta,
            fragMeta = fragMeta,
            req = new NCMetadataAdapter with NCRequest {
                override def getUser: NCUser = reqUsr
                override def getCompany: NCCompany = reqComp
                override def getServerRequestId: String = reqSrvReqId
                override def getNormalizedText: String = reqNormText
                override def getReceiveTimestamp: Long = reqTstamp
                override def getRemoteAddress: Optional[String] = Optional.ofNullable(reqAddr)
                override def getClientAgent: Optional[String] = Optional.ofNullable(reqAgent)
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
        isAbstr: Boolean = false,
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
            override def isAbstract: Boolean = isAbstr
            override def getMetadata: util.Map[String, AnyRef] = map
        }
    }

    protected def test(funcs: TestDesc*): Unit =
        for (f <- funcs) {
            val item =
                try {
                    // Process declarations.
                    f.idlCtx.vars ++= f.term.decls

                    // Execute term's predicate.
                    f.term.pred.apply(f.token.getOrElse(tkn()), f.idlCtx)
                }
                catch {
                    case e: NCE => throw e
                    case e: Exception => throw new Exception(s"Execution error processing: $f", e)
                }

            item.value match {
                case b: java.lang.Boolean => require(if (f.expectedRes) b else !b, s"Unexpected '$b' result for: $f")
                case _ =>
                    require(
                        requirement = false,
                        s"Unexpected result type [" +
                            s"resType=${if (item.value == null) "null" else item.value.getClass.getName}, " +
                            s"resValue=${item.value}, " +
                            s"function=$f" +
                            s"]"
                    )
            }

            f.tokensUsed match {
                case Some(exp) =>
                    require(
                        exp == item.tokUse,
                        s"Unexpected tokens used [" +
                        s"expectedTokensUsed=$exp, " +
                        s"resultTokensUsed=${item.tokUse}, " +
                        s"function=$f" +
                        s"]"
                    )

                case None => // No-op.
            }
        }

    protected def expectError(funcs: TestDesc*): Unit =
        for (f <- funcs)
            try {
                test(f)

                require(false)
            }
            catch {
                case e: Exception =>
                    println(s"Expected error: ${e.getLocalizedMessage}")

                    var cause = e.getCause

                    while (cause != null) {
                        println(s"  Cause: ${cause.getLocalizedMessage} (${cause.getClass.getName})")

                        cause = cause.getCause
                    }
            }

    protected implicit def convert(pred: String): TestDesc = TestDesc(truth = pred)
}