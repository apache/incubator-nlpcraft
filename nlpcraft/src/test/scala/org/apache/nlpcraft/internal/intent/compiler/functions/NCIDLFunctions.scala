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
import org.apache.nlpcraft.nlp.util.*
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.BeforeEach

import java.util
import java.util.UUID
import scala.jdk.CollectionConverters.*
import scala.language.implicitConversions

/**
  *
  */
private[functions] object NCIDLFunctions:
    private final val MODEL_ID = "test.mdl.id"

    /**
      *
      * @param truth
      * @param entity
      * @param idlCtx
      * @param expectedRes
      * @param entitiesUsed
      */
    case class TestDesc(
        truth: String,
        entity: Option[NCEntity] = None,
        idlCtx: NCIDLContext,
        expectedRes: Boolean = true,
        entitiesUsed: Option[Int] = None
    ):
        lazy val term: NCIDLTerm =
            val intents = NCIDLCompiler.compile(s"intent=i term(t)={$truth}", idlCtx.mdlCfg, MODEL_ID)

            require(intents.size == 1)
            require(intents.head.terms.sizeIs == 1)

            intents.head.terms.head

        override def toString: String =
            entity match
                case Some(e) => s"Predicate [body='$truth', entity=${e2s(e)}]"
                case None => s"Predicate '$truth'"

    object TestDesc:
        /**
          *
          * @param truth
          * @return
          */
        def apply(truth: String): TestDesc =
            new TestDesc(truth = truth, idlCtx = mkIdlContext())

        /**
          *
          * @param truth
          * @param entity
          * @param idlCtx
          * @return
          */
        def apply(truth: String, entity: NCEntity, idlCtx: NCIDLContext): TestDesc =
            new TestDesc(truth = truth, entity = Option(entity), idlCtx = idlCtx)

        /**
          *
          * @param truth
          * @param entity
          * @return
          */
        def apply(truth: String, entity: NCEntity): TestDesc =
            new TestDesc(truth = truth, entity = Option(entity), idlCtx = mkIdlContext(entities = Seq(entity)))

    given Conversion[String, TestDesc] with
        def apply(truth: String): TestDesc = TestDesc(truth)

    /**
      *
      * @param e
      * @return
      */
    private def e2s(e: NCEntity): String = s"${e.getId} (${e.getTokens.asScala.map(_.getText).mkString(" ")})"

    /**
      *
      * @param entities
      * @param cfg
      * @param reqId
      * @param txt
      * @param ts
      * @param userId
      * @param reqData
      * @param intentMeta
      * @param convMeta
      * @param fragMeta
      * @return
      */
    def mkIdlContext(
        entities: Seq[NCEntity] = Seq.empty,
        cfg: NCModelConfig = CFG,
        reqId: String = null,
        txt: String = null,
        ts: Long = 0,
        userId: String = null,
        reqData: Map[String, AnyRef] = Map.empty,
        intentMeta: Map[String, AnyRef] = Map.empty,
        convMeta: Map[String, AnyRef] = Map.empty,
        fragMeta: Map[String, AnyRef] = Map.empty
    ): NCIDLContext =
        NCIDLContext(
            cfg,
            entities,
            intentMeta = intentMeta,
            convMeta = convMeta,
            fragMeta = fragMeta,
            req = NCTestRequest(txt = txt, userId = userId, reqId = reqId, ts = ts, data = reqData)
        )

    /**
      *
      * @param id
      * @param reqId
      * @param value
      * @param groups
      * @param meta
      * @param tokens
      * @return
      */
    def mkEntity(
        id: String = UUID.randomUUID().toString,
        reqId: String = UUID.randomUUID().toString,
        value: String = null, // TODO: add tests for usage.
        groups: Set[String] = null,
        meta: Map[String, AnyRef] = Map.empty[String, AnyRef],
        tokens: NCTestToken*
    ): NCEntity =
        require(tokens.nonEmpty)

        NCTestEntity(id, reqId, groups, meta, tokens*)

import org.apache.nlpcraft.internal.intent.compiler.functions.NCIDLFunctions.*

/**
  * Tests for IDL functions.
  */
private[functions] trait NCIDLFunctions:
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
                    val ent = f.entity.orNull
                    val idEnt =
                        if ent != null then
                            require(ent.getTokens != null && !ent.getTokens.isEmpty)

                            NCIDLEntity(ent, ent.getTokens.asScala.minBy(_.getIndex).getIndex)
                        else
                            null

                    f.term.pred.apply(idEnt, f.idlCtx)
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

            f.entitiesUsed match
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