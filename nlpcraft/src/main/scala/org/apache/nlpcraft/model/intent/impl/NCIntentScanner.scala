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

package org.apache.nlpcraft.model.intent.impl

import java.lang.reflect.{InvocationTargetException, Method, ParameterizedType, Type}
import java.util
import java.util.function.Function

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.intent.utils.NCDslIntent

import scala.collection.JavaConverters._
import scala.collection._

/**
  * Scanner for `NCIntent`, `NCIntentRef` and `NCIntentTerm` annotations.
  */
object NCIntentScanner {
    type Callback = Function[NCIntentMatch, NCResult]

    private final val CLS_INTENT = classOf[NCIntent]
    private final val CLS_INTENT_REF = classOf[NCIntentRef]
    private final val CLS_TERM = classOf[NCIntentTerm]
    private final val CLS_QRY_RES = classOf[NCResult]
    private final val CLS_SLV_CTX = classOf[NCIntentMatch]

    // Java and scala lists.
    private final val CLS_SCALA_SEQ = classOf[Seq[_]]
    private final val CLS_SCALA_LST = classOf[List[_]]
    private final val CLS_SCALA_OPT = classOf[Option[_]]
    private final val CLS_JAVA_LST = classOf[util.List[_]]
    private final val CLS_JAVA_OPT = classOf[util.Optional[_]]

    private final val CLS_TOKEN = classOf[NCToken]

    private final val COMP_CLS: Set[Class[_]] = Set(
        CLS_SCALA_SEQ,
        CLS_SCALA_LST,
        CLS_SCALA_OPT,
        CLS_JAVA_LST,
        CLS_JAVA_OPT
    )

    /**
      *
      * @param cls
      * @return
      */
    private def class2Str(cls: Class[_]): String = if (cls == null) "null" else s"'${cls.getSimpleName}'"

    /**
      *
      * @param m
      * @return
      */
    private def method2Str(m: Method): String = {
        val cls = m.getDeclaringClass.getSimpleName
        val name = m.getName
        val args = m.getParameters.map(_.getType.getSimpleName).mkString(", ")

        s"method '$cls#$name($args)'"
    }

    /**
      *
      * @param m
      * @param argIdx
      * @param cxtFirstParam
      */
    private def arg2Str(m: Method, argIdx: Int, cxtFirstParam: Boolean): String =
        s"argument #${argIdx + (if (cxtFirstParam) 1 else 0)} of ${method2Str(m)}"

    /**
      *
      * @param m
      * @param obj
      * @param intent
      */
    @throws[NCE]
    private def prepareCallback(m: Method, obj: Any, intent: NCDslIntent): Callback = {
        // Checks method result type.
        if (m.getReturnType != CLS_QRY_RES)
            throw new NCE(s"@NCIntent error - unexpected result type ${class2Str(m.getReturnType)} for ${method2Str(m)}")

        val allParamTypes = m.getParameterTypes.toSeq

        val ctxFirstParam = allParamTypes.nonEmpty && allParamTypes.head == CLS_SLV_CTX

        def getTokensSeq[T](data: Seq[T]): Seq[T] =
            if (data == null)
                Seq.empty
            else if (ctxFirstParam)
                data.drop(1)
            else
                data

        val allAnns = m.getParameterAnnotations
        val tokParamAnns = getTokensSeq(allAnns).filter(_ != null)
        val tokParamTypes = getTokensSeq(allParamTypes)

        // Checks tokens parameters annotations count.
        if (tokParamAnns.length != tokParamTypes.length)
            throw new NCE(s"@NCIntent error - unexpected annotations count ${tokParamAnns.size} for ${method2Str(m)}")

        // Gets terms identifiers.
        val termIds =
            tokParamAnns.zipWithIndex.
                map { case (anns, idx) ⇒
                    def mkArg: String = arg2Str(m, idx, ctxFirstParam)

                    val annsTerms = anns.filter(_.isInstanceOf[NCIntentTerm])

                    // Each method arguments (second and later) must have one NCIntentTerm annotation.
                    annsTerms.length match {
                        case 1 ⇒ annsTerms.head.asInstanceOf[NCIntentTerm].value()

                        case 0 ⇒ throw new NCE(s"@NCIntentTerm error - " +
                            s"missed annotation ${class2Str(CLS_TERM)} for $mkArg")
                        case _ ⇒ throw new NCE(s"@NCIntentTerm error - " +
                            s"too many annotations ${class2Str(CLS_TERM)} for $mkArg")
                    }
                }

        val terms = intent.terms.toSeq

        // Checks correctness of term IDs.
        // Note we don't restrict them to be duplicated.
        val intentTermIds = terms.filter(_.getId != null).map(_.getId)
        val invalidIds = termIds.filter(id ⇒ !intentTermIds.contains(id))

        if (invalidIds.nonEmpty)
            throw new NCE(s"@NCIntentTerm error - invalid term identifiers '${invalidIds.mkString(", ")}' for ${method2Str(m)}")

        val paramGenTypes = getTokensSeq(m.getGenericParameterTypes)

        require(tokParamTypes.length == paramGenTypes.length)

        // Checks parameters.
        checkTypes(m, tokParamTypes, paramGenTypes, ctxFirstParam)

        // Checks limits.
        val allLimits = terms.map(t ⇒ t.getId → (t.getMin, t.getMax)).toMap

        checkMinMax(m, tokParamTypes, termIds.map(allLimits), ctxFirstParam)

        // Prepares invocation method.
        (ctx: NCIntentMatch) => {
            invoke(
                m,
                obj,
                (
                    (if (ctxFirstParam) Seq(ctx)
                    else Seq.empty) ++
                        prepareParams(m, tokParamTypes, termIds.map(ctx.getTermTokens), ctxFirstParam)
                    ).toArray
            )
        }
    }

    /**
      *
      * @param m
      * @param obj
      * @param args
      */
    @throws[NCE]
    private def invoke(m: Method, obj: Any, args: Array[AnyRef]): NCResult = {
        // TODO: fix in Java 9+.
        var flag = m.isAccessible

        try {
            if (!flag) {
                m.setAccessible(true)

                flag = true
            }
            else
                flag = false

            m.invoke(obj, args: _*).asInstanceOf[NCResult]
        }
        catch {
            case e: InvocationTargetException ⇒
                e.getTargetException match {
                    case e: NCIntentSkip ⇒ throw e
                    case e: NCRejection ⇒ throw e
                    case e: NCE ⇒ throw e
                    case e: Throwable ⇒ throw new NCE(s"Invocation error in ${method2Str(m)}", e)
                }
            case e: Throwable ⇒ throw new NCE(s"Invocation error in ${method2Str(m)}", e)
        }
        finally
            if (flag)
                try
                    m.setAccessible(false)
                catch {
                    case e: SecurityException ⇒ throw new NCE(s"Access error in ${method2Str(m)}", e)
                }
    }

    /**
      *
      * @param m
      * @param paramClss
      * @param argsList
      * @param ctxFirstParam
      */
    @throws[NCE]
    private def prepareParams(
        m: Method,
        paramClss: Seq[Class[_]],
        argsList: Seq[util.List[NCToken]],
        ctxFirstParam: Boolean
    ): Seq[AnyRef] =
        paramClss.zip(argsList).zipWithIndex.map { case ((paramCls, argList), i) ⇒
            def mkArg: String = arg2Str(m, i, ctxFirstParam)

            val toksCnt = argList.size()

            // Single token.
            if (paramCls == CLS_TOKEN) {
                if (toksCnt != 1)
                    throw new NCE(s"@NCIntentTerm error - expected single token, but found $toksCnt for $mkArg")

                argList.get(0)
            }
            // Array of tokens.
            else if (paramCls.isArray)
                argList.asScala.toArray
            // Scala and java list of tokens.
            else if (paramCls == CLS_SCALA_SEQ)
                argList.asScala
            else if (paramCls == CLS_SCALA_LST)
                argList.asScala.toList
            else if (paramCls == CLS_JAVA_LST)
                argList
            // Scala and java optional token.
            else if (paramCls == CLS_SCALA_OPT)
                toksCnt match {
                    case 0 ⇒ None
                    case 1 ⇒ Some(argList.get(0))
                    case _ ⇒ throw new NCE(s"@NCIntentTerm error - too many tokens $toksCnt for option $mkArg")
                }
            else if (paramCls == CLS_JAVA_OPT)
                toksCnt match {
                    case 0 ⇒ util.Optional.empty()
                    case 1 ⇒ util.Optional.of(argList.get(0))
                    case _ ⇒ throw new NCE(s"@NCIntentTerm error - too many tokens $toksCnt for optional $mkArg")
                }
            else
                // Arguments types already checked.
                throw new AssertionError(s"Unexpected type $paramCls for $mkArg")
        }

    /**
      *
      * @param m
      * @param paramCls
      * @param paramGenTypes
      * @param ctxFirstParam
      */
    @throws[NCE]
    private def checkTypes(m: Method, paramCls: Seq[Class[_]], paramGenTypes: Seq[Type], ctxFirstParam: Boolean): Unit = {
        require(paramCls.length == paramGenTypes.length)

        paramCls.zip(paramGenTypes).zipWithIndex.foreach { case ((pClass, pGenType), i) ⇒
            def mkArg: String = arg2Str(m, i, ctxFirstParam)

            // Token.
            if (pClass == CLS_TOKEN) {
                // No-op.
            }
            else if (pClass.isArray) {
                val compType = pClass.getComponentType

                if (compType != CLS_TOKEN)
                    throw new NCE(s"@NCIntentTerm error - unexpected array element type ${class2Str(compType)} for $mkArg")
            }
            // Tokens collection and optionals.
            else if (COMP_CLS.contains(pClass))
                pGenType match {
                    case pt: ParameterizedType ⇒
                        val actTypes = pt.getActualTypeArguments
                        val compTypes = if (actTypes == null) Seq.empty else actTypes.toSeq

                        if (compTypes.length != 1)
                            throw new NCE(
                                s"@NCIntentTerm error - unexpected generic types count ${compTypes.length} for $mkArg"
                            )

                        val compType = compTypes.head

                        compType match {
                            case _: Class[_] ⇒
                                val genClass = compTypes.head.asInstanceOf[Class[_]]

                                if (genClass != CLS_TOKEN)
                                    throw new NCE(
                                        s"@NCIntentTerm error - unexpected generic type ${class2Str(genClass)} for $mkArg"
                                    )
                            case _ ⇒
                                throw new NCE(
                                    s"@NCIntentTerm error - unexpected generic type ${compType.getTypeName} for $mkArg"
                                )
                        }

                    case _ ⇒ throw new NCE(
                        s"@NCIntentTerm error - unexpected parameter type ${pGenType.getTypeName} for $mkArg"
                    )
                }
            // Other types.
            else
                throw new NCE(s"@NCIntentTerm error - unexpected parameter type ${class2Str(pClass)} for $mkArg")
        }
    }

    /**
      *
      * @param m
      * @param paramCls
      * @param limits
      * @param ctxFirstParam
      */
    @throws[NCE]
    private def checkMinMax(m: Method, paramCls: Seq[Class[_]], limits: Seq[(Int, Int)], ctxFirstParam: Boolean): Unit = {
        require(paramCls.length == limits.length)

        paramCls.zip(limits).zipWithIndex.foreach { case ((cls, (min, max)), i) ⇒
            def mkArg: String = arg2Str(m, i, ctxFirstParam)

            // Argument is single token but defined as not single token.
            if (cls == CLS_TOKEN && (min != 1 || max != 1))
                throw new NCE(s"@NCIntentTerm error - term must have [1,1] quantifier for $mkArg " +
                    s"because this argument is a single value.")
            // Argument is not single token but defined as single token.
            else if (cls != CLS_TOKEN && (min == 1 && max == 1))
                throw new NCE(s"@NCIntentTerm error - term has [1,1] quantifier for $mkArg " +
                    s"but this argument is not a single value.")
            // Argument is optional but defined as not optional.
            else if ((cls == CLS_SCALA_OPT || cls == CLS_JAVA_OPT) && (min != 0 || max != 1))
                throw new NCE(s"@NCIntentTerm error - term must have [0,1] quantifier for $mkArg " +
                    s"because this argument is optional.")
            // Argument is not optional but defined as optional.
            else if ((cls != CLS_SCALA_OPT && cls != CLS_JAVA_OPT) && (min == 0 && max == 1))
                throw new NCE(s"@NCIntentTerm error - term has [0,1] quantifier for $mkArg " +
                    s"but this argument is not optional.")
        }
    }

    /**
      *
      * @param mdl
      */
    @throws[NCE]
    def scan(mdl: NCModel): Map[NCDslIntent, Callback] =
        mdl.getClass.getDeclaredMethods.flatMap(m ⇒ {
            // Direct in-the-class and referenced intents.
            val clsArr = m.getAnnotationsByType(CLS_INTENT)
            val refArr = m.getAnnotationsByType(CLS_INTENT_REF)
        
            if (clsArr.length > 1 || refArr.length > 1 || (clsArr.nonEmpty && refArr.nonEmpty))
                throw new NCE(s"Only one @NCIntent or @NCIntentRef annotation is allowed in: ${method2Str(m)}")
        
            val cls = m.getAnnotation(CLS_INTENT)
    
            if (cls != null)
                Some(NCIntentDslCompiler.compile(cls.value(), mdl.getId), m)
            else {
                val ref = m.getAnnotation(CLS_INTENT_REF)
    
                if (ref != null)
                    mdl match {
                        case adapter: NCModelFileAdapter ⇒
                            val refId = ref.value().trim
                            
                            val compiledIntents = adapter
                                .getIntents
                                .asScala
                                .map(NCIntentDslCompiler.compile(_, mdl.getId))
                            
                            U.getDups(compiledIntents.toSeq.map(_.id)) match {
                                case ids if ids.nonEmpty ⇒ throw new NCE(s"Duplicate intent IDs found for model from '${adapter.getOrigin}': ${ids.mkString(",")}")
                                case _ ⇒ ()
                            }
                            
                            compiledIntents.find(_.id == refId) match {
                                case Some(intent) ⇒ Some(intent, m)
                                case None ⇒ throw new NCE(s"@IntentRef($refId) references unknown intent ID '$refId' in ${method2Str(m)}.")
                            }
    
                        case _ ⇒ throw new NCE(s"@IntentRef annotation in ${method2Str(m)} can be used only " +
                            s"for models extending 'NCModelFileAdapter'.")
                    }
                else
                    None
            }
        })
        .map {
            case (intent, m) ⇒ intent → prepareCallback(m, mdl, intent)
        }
        .toMap
}
