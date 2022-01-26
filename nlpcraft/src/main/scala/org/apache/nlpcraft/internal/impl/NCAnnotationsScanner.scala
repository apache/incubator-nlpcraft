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

package org.apache.nlpcraft.internal.impl

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.intent.*
import org.apache.nlpcraft.internal.intent.compiler.*
import org.apache.nlpcraft.internal.makro.NCMacroParser
import org.apache.nlpcraft.internal.util.NCUtils

import java.io.*
import java.lang.annotation.Annotation
import java.lang.reflect.*
import java.util
import java.util.function.Function
import java.util.stream.Collectors
import javax.lang.model.util.Elements.Origin
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Using

private[internal] case class NCCallback(method: Method, cbFun: Function[NCIntentMatch, NCResult]):
    val id: String = method.toString
    val clsName: String = method.getDeclaringClass.getName
    val funName: String = method.getName

private[internal] case class NCIntentData(intent: NCIDLIntent, callback: NCCallback, samples: Seq[Seq[String]])

/**
  *
  */
object NCAnnotationsScanner extends LazyLogging:
    private final val SEPARATORS = Seq('?', ',', '.', '-', '!')

    private final val CLS_INTENT = classOf[NCIntent]
    private final val CLS_INTENT_REF = classOf[NCIntentRef]
    private final val CLS_QRY_RES = classOf[NCResult]
    private final val CLS_INTENT_MATCH = classOf[NCIntentMatch]
    private final val CLS_SAMPLE = classOf[NCIntentSample]
    private final val CLS_SAMPLE_REF = classOf[NCIntentSampleRef]
    private final val CLS_MDL_CLS_REF = classOf[NCModelAddClasses]
    private final val CLS_MDL_PKGS_REF = classOf[NCModelAddPackage]
    private final val CLS_INTENT_IMPORT = classOf[NCIntentImport]

    // Java and scala lists.
    private final val CLS_SCALA_SEQ = classOf[Seq[_]]
    private final val CLS_SCALA_LST = classOf[List[_]]
    private final val CLS_SCALA_OPT = classOf[Option[_]]
    private final val CLS_JAVA_LST = classOf[util.List[_]]
    private final val CLS_JAVA_OPT = classOf[util.Optional[_]]

    private final val CLS_ENTITY = classOf[NCEntity]

    private final val COMP_CLS: Set[Class[_]] = Set(
        CLS_SCALA_SEQ,
        CLS_SCALA_LST,
        CLS_SCALA_OPT,
        CLS_JAVA_LST,
        CLS_JAVA_OPT
    )

    /**
      *
      * @param method
      * @param claxx
      * @param obj
      */
    private case class MethodOwner(method: Method, claxx: Class[_], obj: Any):
        require(method != null)
        require(claxx != null ^ obj != null)

        lazy val (data, className) =
            if obj != null then (obj, obj.getClass.getSimpleName)
            else
                try
                    (claxx.getDeclaredConstructor().newInstance(), claxx.getSimpleName) // Try java reflection  first.
                catch
                    case e: Throwable => throw e // TODO: implement and test scala classes and objects.

    /**
      *
      * @param cls
      * @return
      */
    private def class2Str(cls: Class[_]): String = if cls == null then "null" else s"'${cls.getSimpleName}'"

    /**
      *
      * @param wct
      * @return
      */
    private def wc2Str(wct: WildcardType): String = if wct == null then "null" else s"'${wct.getTypeName}'"

    /**
      *
      * @param mtd
      * @return
      */
    private def method2Str(mtd: Method): String =
        val cls = mtd.getDeclaringClass.getSimpleName
        val name = mtd.getName
        val args = mtd.getParameters.map(_.getType.getSimpleName).mkString(", ")

        s"$cls#$name($args)"

    /**
      *
      * @param f
      * @return
      */
    private def field2Str(f: Field): String =
        val cls = f.getDeclaringClass.getSimpleName
        val name = f.getName

        s"$cls.$name"

    /**
      *
      * @param mdlId
      * @param mtd
      * @param paramClss
      * @param argsList
      * @param ctxFirstParam
      * @return
      */
    private def prepareParams(mdlId: String, mtd: Method, paramClss: Seq[Class[_]], argsList: Seq[util.List[NCEntity]], ctxFirstParam: Boolean): Seq[AnyRef] =
        paramClss.zip(argsList).zipWithIndex.map { case ((paramCls, argList), i) =>
            def mkArg(): String = arg2Str(mtd, i, ctxFirstParam)

            val entsCnt = argList.size()

            // Single entity.
            if paramCls == CLS_ENTITY then
                if entsCnt != 1 then
                    E(s"Expected single entity (found $entsCnt) in @NCIntentTerm annotated argument [mdlId=$mdlId, arg=${mkArg()}]")

                argList.get(0)
            // Array of entities.
            else if paramCls.isArray then
                argList.asScala.toArray
            // Scala and Java list of entities.
            else if paramCls == CLS_SCALA_SEQ then
                argList.asScala.toSeq
            else if paramCls == CLS_SCALA_LST then
                argList.asScala.toList
            else if paramCls == CLS_JAVA_LST then
                argList
            // Scala and java optional entity.
            else if paramCls == CLS_SCALA_OPT then
                entsCnt match
                    case 0 => None
                    case 1 => Option(argList.get(0))
                    case _ => E(s"Too many entities ($entsCnt) for scala.Option[_] @NCIntentTerm annotated argument [mdlId$mdlId, arg=${mkArg()}]")
            else if paramCls == CLS_JAVA_OPT then
                entsCnt match
                    case 0 => util.Optional.empty()
                    case 1 => util.Optional.of(argList.get(0))
                    case _ => E(s"Too many entities ($entsCnt) for java.util.Optional @NCIntentTerm annotated argument [mdlId$mdlId, arg=${mkArg()}]")
            else
                // All allowed arguments types already checked...
                throw new AssertionError(s"Unexpected callback @NCIntentTerm argument type [mdlId=$mdlId, type=$paramCls, arg=${mkArg()}]")
        }

    /**
      *
      * @param mdlId
      * @param mo
      * @param args
      * @return
      */
    private def invoke(mdlId: String, mo: MethodOwner, args: scala.Array[AnyRef]): NCResult =
        val obj = if Modifier.isStatic(mo.method.getModifiers) then null else mo.data
        var flag = mo.method.canAccess(obj)

        try
            if !flag then
                mo.method.setAccessible(true)

                flag = true
            else
                flag = false

            mo.method.invoke(obj, args: _*).asInstanceOf[NCResult]
        catch
            case e: InvocationTargetException =>
                e.getTargetException match
                    case cause: NCIntentSkip => throw cause
                    case cause: NCRejection => throw cause
                    case cause: NCException => throw cause
                    case cause: Throwable => E(s"Intent callback invocation error [mdlId=$mdlId, callback=${method2Str(mo.method)}]", cause)

            case e: Throwable => E(s"Unexpected intent callback invocation error [mdlId=$mdlId, callback=${method2Str(mo.method)}]", e)
        finally
            if flag then
                try
                    mo.method.setAccessible(false)
                catch
                    case e: SecurityException => E(s"Access or security error in intent callback [mdlId=$mdlId, callback=${method2Str(mo.method)}]", e)

    /**
      *
      * @param mtd
      * @param argIdx
      * @param cxtFirstParam
      */
    private def arg2Str(mtd: Method, argIdx: Int, cxtFirstParam: Boolean): String =
        s"#${argIdx + (if cxtFirstParam then 1 else 0)} of ${method2Str(mtd)}"

    /**
      * Gets its own methods including private and accessible from parents.
      *
      * @param o Object.
      * @return Methods.
      */
    private def getAllMethods(o: AnyRef): Set[Method] = getAllMethods(o.getClass)

    /**
      * Gets its own methods including private and accessible from parents.
      *
      * @param claxx Class.
      * @return Methods.
      */
    private def getAllMethods(claxx: Class[_]): Set[Method] = (claxx.getDeclaredMethods ++ claxx.getMethods).toSet

    /**
      * Gets its own fields including private and accessible from parents.
      *
      * @param claxx Class
      * @return Fields.
      */
    private def getAllFields(claxx: Class[_]): Set[Field] = (claxx.getDeclaredFields ++ claxx.getFields).toSet

    /**
      * Gets its own fields including private and accessible from parents.
      *
      * @param o Object.
      * @return Fields.
      */
    private def getAllFields(o: AnyRef): Set[Field] = getAllFields(o.getClass)

    /**
      *
      * @param s
      * @return
      */
    private def isNullOrEmpty(a: Any): Boolean =
        a == null ||
        (a match
            case s: String => s.strip.isEmpty
            case _ => false
        )

    /**
      *
      * @param it
      * @return
      */
    private def isNullOrEmpty(it: Iterable[_]): Boolean = it == null || it.isEmpty || it.exists(isNullOrEmpty)

    /**
      *
      * @param anns
      * @param origin
      * @tparam T
      */
    private def emptyError(anns: Iterable[_], origin: String): Unit =
        require(anns != null && anns.nonEmpty)

        E(s"Unexpected empty annotation definition @${anns.head.getClass.getSimpleName} in $origin") // TODO: text

    /**
      *
      * @param anns
      * @param getValues
      * @param src
      * @tparam T
      * @tparam K
      */
    private def checkMultiple[T, K](anns: Iterable[T], getValues: T => Iterable[K], origin: => String): Unit =
        if anns.exists(a => a == null || isNullOrEmpty(getValues(a))) then emptyError(anns, origin)

    /**
      *
      * @param anns
      * @param getValue
      * @param src
      * @tparam T
      * @tparam K
      */
    private def checkSingle[T, K](anns: Iterable[T], getValue: T => K, origin: => String): Unit =
        if anns.exists(a => a == null || isNullOrEmpty(getValue(a))) then emptyError(anns, origin)

import org.apache.nlpcraft.internal.impl.NCAnnotationsScanner.*

/**
  *
  * @param mdl
  */
class NCAnnotationsScanner(mdl: NCModel) extends LazyLogging:
    private val cfg = mdl.getConfig
    private val id = cfg.getId
    private val origin = cfg.getOrigin

    /**
      *
      * @return
      */
    private def scanClasses(): (mutable.Buffer[NCIDLIntent], Iterable[Class[_]]) =
        val m = mutable.HashMap.empty[Class[_], Class[_]]
        val intentDecls = mutable.Buffer.empty[NCIDLIntent]

        def processImports(anns: scala.Array[NCIntentImport], orig: => String): Unit =
            checkMultiple(anns, (a: NCIntentImport) => a.value, orig)

            for (
                ann <- anns;
                res <- ann.value;
                intent <- NCIDLCompiler.compile(NCUtils.readResource(res.strip).mkString("\n"), cfg, res)
            )
                if intentDecls.exists(_.id == intent.id) then
                    E(s"Duplicate intent ID [mdlId=$id, origin=$origin, resource=$res, id=${intent.id}]")
                intentDecls += intent

        def processAddClasses0(classes: Iterable[Class[_]], src: Class[_]): Unit =
            for (claxx <- classes)
                // TODO: text
                if m.getOrElse(claxx, null) == src then E(s"Cyclical reference found first class=$src, second=$claxx")

                m += claxx -> src

                scanClass(claxx)

        def processAddClass(classesAdd: scala.Array[NCModelAddClasses], src: Class[_]): Unit =
            checkMultiple(classesAdd, (a: NCModelAddClasses) => a.value, src.getSimpleName)
            processAddClasses0(classesAdd.flatMap(_.value()), src)

        def processAddPackage(packsAdd: scala.Array[NCModelAddPackage], src: Class[_]): Unit =
            checkMultiple(packsAdd, (a: NCModelAddPackage) => a.value, src.getSimpleName)
            for (pack <- packsAdd.flatMap(_.value.map(_.strip))) processAddClasses0(getPackageClasses(pack), src)

        def scanClass(claxx: Class[_]): Unit =
            processImports(claxx.getAnnotationsByType(CLS_INTENT_IMPORT), claxx.getSimpleName)
            processAddClass(claxx.getAnnotationsByType(CLS_MDL_CLS_REF), claxx)
            processAddPackage(claxx.getAnnotationsByType(CLS_MDL_PKGS_REF), claxx)

            for (m <- getAllMethods(claxx))
                processImports(m.getAnnotationsByType(CLS_INTENT_IMPORT), method2Str(m))
                processAddClass(m.getAnnotationsByType(CLS_MDL_CLS_REF), claxx)
                processAddPackage(m.getAnnotationsByType(CLS_MDL_PKGS_REF), claxx)

            for (f <- getAllFields(claxx))
                processImports(f.getAnnotationsByType(CLS_INTENT_IMPORT), field2Str(f))
                processAddClass(f.getAnnotationsByType(CLS_MDL_CLS_REF), claxx)
                processAddPackage(f.getAnnotationsByType(CLS_MDL_PKGS_REF), claxx)

        scanClass(mdl.getClass)

        (intentDecls, m.keys)

    /**
      *
      * @return
      */
    def scan(): Seq[NCIntentData] =
        val (intentDecls: mutable.Buffer[NCIDLIntent], classes: Iterable[Class[_]]) = scanClasses()

        val intents = mutable.Buffer.empty[NCIntentData]

        for (m <- getAllMethods(mdl))
            processMethod(intentDecls, intents, MethodOwner(method = m, claxx = null, obj = mdl))

        for (claxx <- classes; m <- getAllMethods(claxx))
            processMethod(intentDecls, intents, MethodOwner(method = m, claxx = claxx, obj = null))

        val unusedIntents = intentDecls.filter(i => !intents.exists(_._1.id == i.id))

        if unusedIntents.nonEmpty then
            logger.warn(s"Intents are unused (have no callback): [mdlId=$id, origin=$origin, intentIds=${unusedIntents.map(_.id).mkString("(", ", ", ")")}]")

        if (intents.nonEmpty) {
            // Check the uniqueness of intent IDs.
            NCUtils.getDups(intents.map(_._1).toSeq.map(_.id)) match
                case ids if ids.nonEmpty => E(s"Duplicate intent IDs [mdlId=$id, origin=$origin, ids=${ids.mkString(",")}]")
                case _ => ()
        }
        else
            logger.warn(s"Model has no intent: $id")

        intents.toSeq

    /**
      *
      * @param mtd
      * @param paramCls
      * @param paramGenTypes
      * @param ctxFirstParam
      */
    private def checkTypes(mtd: Method, paramCls: Seq[Class[_]], paramGenTypes: Seq[Type], ctxFirstParam: Boolean): Unit =
        require(paramCls.sizeIs == paramGenTypes.length)

        paramCls.zip(paramGenTypes).zipWithIndex.foreach { case ((pClass, pGenType), i) =>
            def mkArg(): String = arg2Str(mtd, i, ctxFirstParam)

            // Entity.
            if pClass == CLS_ENTITY then () // No-op.
            else if pClass.isArray then
                val compType = pClass.getComponentType

                if compType != CLS_ENTITY then
                    E(s"Unexpected array element type for @NCIntentTerm annotated argument [mdlId=$id, origin=$origin, type=${class2Str(compType)}, arg=${mkArg()}]")
            // Entities collection and optionals.
            else if COMP_CLS.contains(pClass) then
                pGenType match
                    case pt: ParameterizedType =>
                        val actTypes = pt.getActualTypeArguments
                        val compTypes = if actTypes == null then Seq.empty else actTypes.toSeq

                        if compTypes.sizeIs != 1 then
                            E(s"Unexpected generic types count for @NCIntentTerm annotated argument [mdlId=$id, origin=$origin, count=${compTypes.length}, arg=${mkArg()}]")

                        val compType = compTypes.head

                        compType match
                            // Java, Groovy.
                            case _: Class[_] =>
                                val genClass = compTypes.head.asInstanceOf[Class[_]]

                                if genClass != CLS_ENTITY then
                                    E(s"Unexpected generic type for @NCIntentTerm annotated argument [mdlId=$id, origin=$origin, type=${class2Str(genClass)}, arg=${mkArg()}]")

                            // Kotlin.
                            case _: WildcardType =>
                                val wildcardType = compTypes.head.asInstanceOf[WildcardType]

                                val lowBounds = wildcardType.getLowerBounds
                                val upBounds = wildcardType.getUpperBounds

                                if lowBounds.nonEmpty || upBounds.size != 1 || upBounds(0) != CLS_ENTITY then
                                    E(s"Unexpected Kotlin generic type for @NCIntentTerm annotated argument [mdlId=$id, origin=$origin, type=${wc2Str(wildcardType)}, arg=${mkArg()}]")
                            case _ => E(s"Unexpected generic type for @NCIntentTerm annotated argument [mdlId=$id, origin=$origin, type=${compType.getTypeName}, arg=${mkArg()}]")

                    // TODO: scala 3 ParameterizedType doesn't here.
                    case  _ : Any if pGenType.getTypeName == "scala.collection.immutable.Seq" ||
                                     pGenType.getTypeName == "scala.collection.immutable.List" ||
                                     pGenType.getTypeName == "scala.Option"  => // No-op.
                    case _ =>
                        E(s"Unexpected parameter type for @NCIntentTerm annotated argument [mdlId=$id, origin=$origin, type=${pGenType.getTypeName}, arg=${mkArg()}]")
            // Other types.
            else
                E(s"Unexpected parameter type for @NCIntentTerm annotated argument [mdlId=$id, origin=$origin, type=${class2Str(pClass)}, arg=${mkArg()}]")
        }

    /**
      *
      * @param mdl
      * @param mtd
      * @param paramCls
      * @param limits
      * @param ctxFirstParam
      */
    private def checkMinMax(mtd: Method, paramCls: Seq[Class[_]], limits: Seq[(Int, Int)], ctxFirstParam: Boolean): Unit =
        require(paramCls.sizeIs == limits.length)

        paramCls.zip(limits).zipWithIndex.foreach { case ((cls, (min, max)), i) =>
            def mkArg(): String = arg2Str(mtd, i, ctxFirstParam)

            val p1 = "its @NCIntentTerm annotated argument"
            val p2 = s"[mdlId=$id, origin=$origin, arg=${mkArg()}]"

            // Argument is single entity but defined as not single entity.
            if cls == CLS_ENTITY && (min != 1 || max != 1) then
                E(s"Intent term must have [1,1] quantifier because $p1 is a single value $p2")
            // Argument is not single entity but defined as single entity.
            else if cls != CLS_ENTITY && (min == 1 && max == 1) then
                E(s"Intent term has [1,1] quantifier but $p1 is not a single value $p2")
            // Argument is optional but defined as not optional.
            else if (cls == CLS_SCALA_OPT || cls == CLS_JAVA_OPT) && (min != 0 || max != 1) then
                E(s"Intent term must have [0,1] quantifier because $p1 is optional $p2")
            // Argument is not optional but defined as optional.
            else if (cls != CLS_SCALA_OPT && cls != CLS_JAVA_OPT) && (min == 0 && max == 1) then
                E(s"Intent term has [0,1] quantifier but $p1 is not optional $p2")
        }

    /**
      *
      * @param mo
      */
    private def processMethod(intentDecls: mutable.Buffer[NCIDLIntent], intents: mutable.Buffer[NCIntentData], mo: MethodOwner): Unit =
        val m = mo.method
        val mtdStr = method2Str(m)
        lazy val samples = scanSamples(mo.method)

        def bindIntent(intent: NCIDLIntent, cb: NCCallback): Unit =
            if intents.exists(i => i._1.id == intent.id && i._2.id != cb.id) then
                E(s"The intent cannot be bound to more than one callback [mdlId=$id, origin=$origin, class=${mo.className}, intentId=${intent.id}]")
            else
                intentDecls += intent
                intents += NCIntentData(intent, prepareCallback(mo, intent), samples.getOrElse(intent.id, Seq.empty))

        def existsForOtherMethod(id: String): Boolean =
            intents.find(_.intent.id == id) match
                case Some(i) => i.callback.method != m
                case None => false

        // 1. Process inline intent declarations by @NCIntent annotation.
        val annsIntents = m.getAnnotationsByType(CLS_INTENT)
        checkSingle(annsIntents, (a:NCIntent) => a.value, mtdStr)

        for (ann <- annsIntents; intent <- NCIDLCompiler.compile(ann.value, cfg, mtdStr))
            if intentDecls.exists(_.id == intent.id && existsForOtherMethod(intent.id)) then
                E(s"Duplicate intent ID [mdlId=$id, origin=$origin, callback=$mtdStr, id=${intent.id}]")
            else
                bindIntent(intent, prepareCallback(mo, intent))

        // 2. Process intent references from @NCIntentRef annotation.
        val annRefs = m.getAnnotationsByType(CLS_INTENT_REF)
        checkSingle(annRefs, (a:NCIntentRef) => a.value, mtdStr)

        for (ann <- annRefs)
            val refId = ann.value.trim

            intentDecls.find(_.id == refId) match
                case Some(intent) => bindIntent(intent, prepareCallback(mo, intent))
                case None => E(s"@NCIntentRef(\"$refId\") references unknown intent ID [mdlId=$id, origin=$origin, refId=$refId, callback=$mtdStr]")

    /**
      *
      * @param mo
      * @param intent
      * @return
      */
    private def prepareCallback(mo: MethodOwner, intent: NCIDLIntent): NCCallback =
        val mtd = mo.method

        // Checks method result type.
        if mtd.getReturnType != CLS_QRY_RES then
            E(s"Unexpected result type for @NCIntent annotated method [mdlId=$id, intentId=${intent.id}, type=${class2Str(mtd.getReturnType)}, callback=${method2Str(mtd)}]")

        val allParamTypes = mtd.getParameterTypes.toSeq
        val ctxFirstParam = allParamTypes.nonEmpty && allParamTypes.head == CLS_INTENT_MATCH

        def getSeq[T](data: Seq[T]): Seq[T] =
            if data == null then Seq.empty
            else if ctxFirstParam then data.drop(1)
            else data

        val allAnns = mtd.getParameterAnnotations
        val tokParamAnns = getSeq(allAnns.toIndexedSeq).filter(_ != null)
        val tokParamTypes = getSeq(allParamTypes)

        // Checks entities parameters annotations count.
        if tokParamAnns.sizeIs != tokParamTypes.length then
            E(s"Unexpected annotations count for @NCIntent annotated method [mdlId=$id, intentId=${intent.id}, count=${tokParamAnns.size}, callback=${method2Str(mtd)}]")

        // Gets terms IDs.
        val termIds = tokParamAnns.toList.zipWithIndex.map {
            case (annArr, idx) =>
                def mkArg(): String = arg2Str(mtd, idx, ctxFirstParam)

                val termAnns = annArr.filter(_.isInstanceOf[NCIntentTerm])

                // Each method arguments (second and later) must have one NCIntentTerm annotation.
                termAnns.length match
                    case 1 => termAnns.head.asInstanceOf[NCIntentTerm].value
                    case 0 =>
                        if idx == 0 then
                            E(s"Missing @NCIntentTerm annotation or wrong type of the 1st parameter (must be 'NCIntentMatch') for [mdlId=$id, intentId=${intent.id}, arg=${mkArg()}]")
                        else
                            E(s"Missing @NCIntentTerm annotation for [mdlId=$id, intentId=${intent.id}, arg=${mkArg()}]")

                    case _ => E(s"Too many @NCIntentTerm annotations for [mdlId=$id, intentId=${intent.id}, arg=${mkArg()}]")
            }

        if NCUtils.containsDups(termIds) then
            E(s"Duplicate term IDs in @NCIntentTerm annotations [mdlId=$id, intentId=${intent.id}, dups=${NCUtils.getDups(termIds).mkString(", ")}, callback=${method2Str(mtd)}]")

        val terms = intent.terms

        // Checks correctness of term IDs.
        // Note we don't restrict them to be duplicated.
        val intentTermIds = terms.flatMap(_.id)
        val invalidIds = termIds.filter(id => !intentTermIds.contains(id))

        if invalidIds.nonEmpty then
            // Report only the first one for simplicity & clarity.
            E(s"Unknown term ID in @NCIntentTerm annotation [mdlId=$id, intentId=${intent.id}, termId=${invalidIds.head}, callback=${method2Str(mtd)}]")

        val paramGenTypes = getSeq(mtd.getGenericParameterTypes.toIndexedSeq)

        require(tokParamTypes.sizeIs == paramGenTypes.length)

        // Checks parameters.
        checkTypes(mtd, tokParamTypes, paramGenTypes, ctxFirstParam)

        // Checks limits.
        val allLimits = terms.map(t => t.id.orNull -> (t.min, t.max)).toMap

        checkMinMax(mtd, tokParamTypes, termIds.map(allLimits), ctxFirstParam)

        NCCallback(mtd,
            (ctx: NCIntentMatch) =>
                val args = mutable.Buffer.empty[AnyRef]
                if ctxFirstParam then args += ctx
                args ++= prepareParams(id, mtd, tokParamTypes, termIds.map(ctx.getTermEntities), ctxFirstParam)

                invoke(id, mo, args.toArray)
        )

    /**
      *
      * @param mdl
      * @return
      */
    private def scanSamples(m: Method): Map[String, Seq[Seq[String]]] =
        val smpAnns = m.getAnnotationsByType(CLS_SAMPLE)
        val smpAnnsRef = m.getAnnotationsByType(CLS_SAMPLE_REF)
        lazy val mtdStr = method2Str(m)
        lazy val intAnns = m.getAnnotationsByType(CLS_INTENT)
        lazy val refAnns = m.getAnnotationsByType(CLS_INTENT_REF)
        lazy val samples = mutable.HashMap.empty[String, Seq[Seq[String]]]

        if smpAnns.nonEmpty || smpAnnsRef.nonEmpty then
            if intAnns.isEmpty && refAnns.isEmpty then
                E(s"@NCIntentSample or @NCIntentSampleRef annotations without corresponding @NCIntent or @NCIntentRef annotations: $mtdStr")
            else
                def read[T](annArr: scala.Array[T], annName: String, getSamples: T => Seq[String], getSource: Option[T => String]): Seq[Seq[String]] =
                    for (ann <- annArr.toSeq) yield
                        val samples = getSamples(ann).map(_.strip).filter(s => s.nonEmpty && s.head != '#')

                        if samples.isEmpty then
                            getSource match
                                case None => logger.warn(s"$annName annotation has no samples: $mtdStr")
                                case Some(f) => logger.warn(s"$annName annotation references '${f(ann)}' file that has no samples: $mtdStr")

                            Seq.empty
                        else
                            samples
                .filter(_.nonEmpty)

                val seqSeq =
                    read[NCIntentSample](
                        smpAnns, "@NCIntentSample", _.value.toSeq, None
                    ) ++
                    read[NCIntentSampleRef](
                        smpAnnsRef, "@NCIntentSampleRef", a => NCUtils.readResource(a.value), Option(_.value)
                    )

                if NCUtils.containsDups(seqSeq.flatMap(_.toSeq).toList) then
                    logger.warn(s"@NCIntentSample and @NCIntentSampleRef annotations have duplicates: $mtdStr")

                val distinct = seqSeq.map(_.distinct).distinct

                for (ann <- intAnns; intent <- NCIDLCompiler.compile(ann.value, cfg, mtdStr))
                    samples += intent.id -> distinct

                for (ann <- refAnns) samples += (ann.value -> distinct)
        else if intAnns.nonEmpty || refAnns.nonEmpty then
            logger.warn(s"@NCIntentSample or @NCIntentSampleRef annotations are missing for: $mtdStr")

        samples.toMap

    /**
      *
      * @param pack
      * @return
      */
    private def getPackageClasses(pack: String): Set[Class[_]] =
        val classes =
            Using.resource(new BufferedReader(
                new InputStreamReader(ClassLoader.getSystemClassLoader.getResourceAsStream(
                    pack.replaceAll("[.]", "/")))
            )) {
                reader =>
                    val lines: util.List[String] = reader.lines().collect(Collectors.toList)

                    lines.asScala.filter(_.endsWith(".class")).
                        flatMap(className =>
                            def make(name: String): Option[Class[_]] =
                                try
                                    Option(Class.forName(name))
                                catch
                                    case e: ClassNotFoundException =>
                                        logger.warn(s"Class cannot loaded: $name", e)

                                        None
                            make(s"$pack.${className.substring(0, className.lastIndexOf('.'))}")
                        )
            }

        // Check should be after classes loading attempt.
        if Thread.currentThread().getContextClassLoader.getDefinedPackage(pack) == null then
            E(s"Invalid additional references in @${CLS_MDL_PKGS_REF.getSimpleName} annotation [mdlId=$id, origin=$origin, package=$pack]")

        classes.toSet