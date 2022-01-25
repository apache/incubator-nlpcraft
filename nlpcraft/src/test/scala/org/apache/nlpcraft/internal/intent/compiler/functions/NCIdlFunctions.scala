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

package org.apache.nlpcraft.internal.intent.compiler.functions

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.intent.*
import org.apache.nlpcraft.internal.intent.compiler.*
import org.apache.nlpcraft.internal.intent.compiler.functions.*
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.BeforeEach

import scala.jdk.CollectionConverters.*
import scala.language.implicitConversions

private[functions] object NCIdlFunctions:
    private final val MODEL_ID = "test.mdl.id"

    case class TestDesc(
        truth: String,
        entity: Option[NCEntity] = None,
        idlCtx: NCIDLContext,
        isCustom: Boolean = false,
        expectedRes: Boolean = true,
        tokensUsed: Option[Int] = None
    ):
        // It should be lazy for errors verification methods.
        lazy val term: NCIDLTerm =
            val (s1, s2) = if isCustom then ('/', '/') else ('{', '}')

            val intents = NCIDLCompiler.compile(s"intent=i term(t)=$s1$truth$s2", CFG, MODEL_ID)

            require(intents.size == 1)
            require(intents.head.terms.sizeIs == 1)

            intents.head.terms.head

        override def toString: String =
            entity match
                case Some(e) => s"Predicate [body='$truth', token=${e2s(e)}]"
                case None => s"Predicate '$truth'"

    object TestDesc:
        def apply(truth: String): TestDesc =
            new TestDesc(truth = truth, idlCtx = mkIdlContext())

        def apply(truth: String, entity: NCEntity, idlCtx: NCIDLContext): TestDesc =
            new TestDesc(truth = truth, entity = Option(entity), idlCtx = idlCtx)

        def apply(truth: String, entity: NCEntity): TestDesc =
            new TestDesc(truth = truth, entity = Option(entity), idlCtx = mkIdlContext(entities = Seq(entity)))

    given Conversion[String, TestDesc] with
        def apply(s: String): TestDesc = TestDesc(s)

    private def e2s(t: NCEntity): String =
        // TODO:
        t.toString

    //        def nvl(s: String, name: String): String = if s != null then s else s"$name (not set)"
    //
    //        s"text=${nvl(t.getOriginalText, "text")} [${nvl(t.getId, "id")}]"

    def mkIdlContext(
        entities: Seq[NCEntity] = Seq.empty,
        reqSrvReqId: String = null,
        reqNormText: String = null,
        reqTstamp: Long = 0,
        reqAddr: String = null,
        reqAgent: String = null,
        reqData: Map[String, AnyRef] = Map.empty,
        intentMeta: Map[String, AnyRef] = Map.empty,
        convMeta: Map[String, AnyRef] = Map.empty,
        fragMeta: Map[String, AnyRef] = Map.empty
    ): NCIDLContext =
        NCIDLContext(
            CFG,
            entities,
            intentMeta = intentMeta,
            convMeta = convMeta,
            fragMeta = fragMeta,
            req = new NCRequest:
                override def getUserId: String = "userID" // TODO:
                override def getRequestId: String = reqSrvReqId
                override def getText: String = reqNormText
                override def getReceiveTimestamp: Long = reqTstamp
                override def getRequestData: java.util.Map[String, AnyRef] = reqData.asJava
        )

    // TODO:
    def mkEntity(
        id: String = null,
        srvReqId: String = null,
        parentId: String = null,
        value: String = null,
        txt: String = null,
        normTxt: String = null,
        start: Int = 0,
        end: Int = 0,
        groups: Seq[String] = Seq.empty,
        ancestors: Seq[String] = Seq.empty,
        aliases: Set[String] = Set.empty,
        partTokens: Seq[NCToken] = Seq.empty,
        `abstract`: Boolean = false,
        meta: Map[String, AnyRef] = Map.empty[String, AnyRef]
    ): NCEntity =
    // TODO:
        null
//        val map = new util.HashMap[String, AnyRef]
//
//        map.putAll(meta.asJava)
//
//        map.put("nlpcraft:nlp:origtext", txt)
//        map.put("nlpcraft:nlp:normtext", normTxt)
//
//        new NCPropertyMapAdapter with NCEntity():
//            override def getTokens: util.List[NCToken] = ???
//            override def getRequestId: String = ???
//            override def getId: String = ???
//


import org.apache.nlpcraft.internal.intent.compiler.functions.NCIdlFunctions.*

/**
  * Tests for IDL functions.
  */
private[functions] trait NCIdlFunctions:
    @BeforeEach
    def before(): Unit = NCIDLCompilerGlobal.clearCache(MODEL_ID)

    /**
      *
      * @param funcs
      */
    protected def test(funcs: TestDesc*): Unit =
        for (f <- funcs)
            val item =
                try
                    // Process declarations.
                    f.idlCtx.vars ++= f.term.decls

                    // Execute term's predicate.
                    // TODO: index
                    f.term.pred.apply(NCIDLEntity(f.entity.getOrElse(mkEntity()), 0), f.idlCtx)
                catch
                    case e: NCException => throw e
                    case e: Exception => throw new Exception(s"Execution error processing: $f", e)

            item.value match
                case b: java.lang.Boolean => require(if f.expectedRes then b else !b, s"Unexpected '$b' result for: $f")
                case _ =>
                    require(
                        requirement = false,
                        s"Unexpected result type [resType=${
                            if item.value == null then "null"
                            else item.value.getClass.getName
                        }, resValue=${item.value}, function=$f]"
                    )

            f.tokensUsed match
                case Some(exp) =>
                    require(
                        exp == item.entUse,
                        s"Unexpected tokens used [expectedTokensUsed=$exp, resultEntityUsed=${item.entUse}, function=$f]"
                    )

                case None => // No-op.

    /**
      *
      * @param funcs
      */
    protected def expectError(funcs: TestDesc*): Unit =
        for (f <- funcs)
            try
                test(f)

                require(false)
            catch
                case e: Exception =>
                    println(s"Expected error: ${e.getLocalizedMessage}")

                    var cause = e.getCause

                    while (cause != null)
                        println(s"  Cause: ${cause.getLocalizedMessage} (${cause.getClass.getName})")

                        cause = cause.getCause