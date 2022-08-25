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
import org.apache.nlpcraft.annotations.*
import org.apache.nlpcraft.internal.intent.*
import org.apache.nlpcraft.internal.intent.compiler.*
import org.apache.nlpcraft.internal.makro.NCMacroParser
import org.apache.nlpcraft.internal.util.NCUtils

import java.lang.annotation.Annotation
import java.lang.reflect.*
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Using

/**
  *
  * @param ctx
  * @param im
  */
case class NCCallbackInput(ctx: NCContext, im: NCIntentMatch)

/**
  *
  * @param intent
  * @param function
  */
case class NCModelIntent(intent: NCIDLIntent, function: NCCallbackInput => NCResult)

object NCModelScanner extends LazyLogging:
    private final val CLS_INTENT = classOf[NCIntent]
    private final val CLS_INTENT_REF = classOf[NCIntentRef]
    private final val CLS_QRY_RES = classOf[NCResult]
    private final val CLS_CTX = classOf[NCContext]
    private final val CLS_INTENT_MATCH = classOf[NCIntentMatch]
    private final val CLS_INTENT_OBJ = classOf[NCIntentObject]

    private final val CLS_SCALA_SEQ = classOf[Seq[_]]
    private final val CLS_SCALA_LST = classOf[List[_]]
    private final val CLS_SCALA_OPT = classOf[Option[_]]

    private final val CLS_ENTITY = classOf[NCEntity]

    private lazy val I = "@NCIntent"
    private lazy val IT = "@NCIntentTerm"
    private lazy val IR = "@NCIntentRef"

    private final val COMP_CLS: Set[Class[_]] = Set(
        CLS_SCALA_SEQ,
        CLS_SCALA_LST,
        CLS_SCALA_OPT
    )

    /**
      *
      * @param intent
      * @param function
      * @param method
      */
    private case class IntentHolder(intent: NCIDLIntent, function: NCCallbackInput => NCResult, method: Method)

    /**
      *
      */
    private object IntentHolder:
        def apply(cfg: NCModelConfig, intent: NCIDLIntent, obj: AnyRef, mtd: Method): IntentHolder =
            new IntentHolder(intent, prepareCallback(cfg, mtd, obj, intent), mtd)

    /**
      *
      * @param iter
      * @return
      */
    private def col2Str(iter: Iterable[_]): String = iter.mkString("(", ",", ")")

    /**
      *
      * @param clazz
      * @return
      */
    private def class2Str(clazz: Class[_]): String =
        val cls = clazz.getSimpleName.strip
        // Anonymous classes (like `class foo.bar.name$1`) doesn't have simple names.
        if cls.nonEmpty then cls else clazz.getName.reverse.takeWhile(_ != '.').reverse

    /**
      *
      * @param mtd
      * @return
      */
    private def method2Str(mtd: Method): String =
        val cls = class2Str(mtd.getDeclaringClass)
        val name = mtd.getName
        val args = mtd.getParameters.map(_.getType.getSimpleName).mkString(", ")
        s"$cls#$name($args)"

    /**
      *
      * @param f
      * @return
      */
    private def field2Str(f: Field): String =
        val cls = class2Str(f.getDeclaringClass)
        val name = f.getName
        s"$cls.$name"

    /**
      *
      * @param cfg
      * @param mtd
      * @param prmClss
      * @param argsList
      * @return
      */
    private def prepareParams(cfg: NCModelConfig, mtd: Method, prmClss: List[Class[_]], argsList: List[List[NCEntity]]): Seq[AnyRef] =
        prmClss.zip(argsList).zipWithIndex.map { case ((paramCls, argList), i) =>
            def mkArg(): String = arg2Str(mtd, i)

            lazy val z = s"mdlId=${cfg.getId}, type=$paramCls, arg=${mkArg()}"
            val entsCnt = argList.size

            // Single entity.
            if paramCls == CLS_ENTITY then
                if entsCnt != 1 then E(s"Expected single entity (found $entsCnt) in $IT annotated argument [$z]")

                argList.head
            // Array of entities.
            else if paramCls.isArray then
                argList.toArray
            else if paramCls == CLS_SCALA_SEQ then
                argList
            else if paramCls == CLS_SCALA_LST then
                argList
            else if paramCls == CLS_SCALA_OPT then
                entsCnt match
                    case 0 => None
                    case 1 => Option(argList.head)
                    case _ => E(s"Too many entities ($entsCnt) for 'scala.Option[_]' $IT annotated argument [$z]")
            else
                // All allowed arguments types already checked.
                throw new AssertionError(s"Unexpected type for callback's $IT argument [$z]")
        }

    /**
      *
      * @param cfg
      * @param mtd
      * @param obj
      * @param args
      * @return
      */
    private def invoke(cfg: NCModelConfig, mtd: Method, obj: AnyRef, args: scala.Array[AnyRef]): NCResult =
        val methodObj = if Modifier.isStatic(mtd.getModifiers) then null else obj
        var flag = mtd.canAccess(methodObj)
        lazy val z = s"mdlId=${cfg.getId}, callback=${method2Str(mtd)}"
        try
            if !flag then
                mtd.setAccessible(true)
                flag = true
            else
                flag = false
            mtd.invoke(methodObj, args: _*).asInstanceOf[NCResult]
        catch
            case e: InvocationTargetException =>
                e.getTargetException match
                    case cause: NCIntentSkip => throw cause
                    case cause: NCRejection => throw cause
                    case cause: NCException => throw cause
                    case cause: Throwable => E(s"Intent callback invocation error [$z]", cause)
            case e: Throwable => E(s"Unexpected intent callback invocation error [$z]", e)
        finally
            if flag then
                try mtd.setAccessible(false)
                catch case e: SecurityException => E(s"Access or security error in intent callback [$z]", e)

    /**
      *
      * @param cfg
      * @param field
      * @param obj
      * @return
      */
    private def getFieldObject(cfg: NCModelConfig, field: Field, obj: AnyRef): AnyRef =
        lazy val fStr = field2Str(field)
        val fieldObj = if Modifier.isStatic(field.getModifiers) then null else obj
        var flag = field.canAccess(fieldObj)
        lazy val z = s"mdlId=${cfg.getId}, field=$fStr"
        val res =
            try
                if !flag then
                    field.setAccessible(true)
                    flag = true
                else
                    flag = false
                field.get(fieldObj)
            catch case e: Throwable => E(s"Unexpected field access error [$z]", e)
            finally
                if flag then
                    try field.setAccessible(false)
                    catch case e: SecurityException => E(s"Access or security error in field [$z]", e)

        if res == null then throw new NCException(s"Field value cannot be 'null' for [$z]")
        res

    /**
      *
      * @param mtd
      * @param argIdx
      */
    private def arg2Str(mtd: Method, argIdx: Int): String = s"#${argIdx + 2} of ${method2Str(mtd)}"

    /**
      * Gets its own methods including private and accessible from parents.
      *
      * @param o Object.
      * @return Methods.
      */
    private def getAllMethods(o: AnyRef): Set[Method] =
        val cls = o.getClass
        (cls.getDeclaredMethods ++ cls.getMethods).toSet

    /**
      * Gets its own fields including private and accessible from parents.
      *
      * @param o Object.
      * @return Fields.
      */
    private def getAllFields(o: AnyRef): Set[Field] =
        val cls = o.getClass
        (cls.getDeclaredFields ++ cls.getFields).toSet

    /**
      *
      * @param cfg
      * @param mtd
      * @param argClasses
      * @param paramGenTypes
      */
    private def checkTypes(cfg: NCModelConfig, mtd: Method, argClasses: Seq[Class[_]], paramGenTypes: Seq[Type]): Unit =
        require(argClasses.sizeIs == paramGenTypes.length)

        var warned = false

        argClasses.zip(paramGenTypes).zipWithIndex.foreach { case ((argClass, paramGenType), i) =>
            def mkArg(): String = arg2Str(mtd, i)

            lazy val z = s"mdlId=${cfg.getId}, type=${class2Str(argClass)}, arg=${mkArg()}"

            // Entity.
            if argClass == CLS_ENTITY then () // No-op.
            else if argClass.isArray then
                val compType = argClass.getComponentType
                if compType != CLS_ENTITY then
                    E(s"Unexpected array element type for $IT annotated argument [$z]")
                // Entities collection and optionals.
            else if COMP_CLS.contains(argClass) then
                paramGenType match
                    case pt: ParameterizedType =>
                        val actTypes = pt.getActualTypeArguments
                        val compTypes = if actTypes == null then Seq.empty else actTypes.toSeq
                        if compTypes.sizeIs != 1 then
                            E(s"Unexpected generic types count for $IT annotated argument [count=${compTypes.length}, $z]")
                        val compType = compTypes.head
                        compType match
                            case _: Class[_] =>
                                val genClass = compTypes.head.asInstanceOf[Class[_]]
                                if genClass != CLS_ENTITY then
                                    E(s"Unexpected generic type for $IT annotated argument [$z]")
                            case _: WildcardType =>
                                val wildcardType = compTypes.head.asInstanceOf[WildcardType]
                                val lowBounds = wildcardType.getLowerBounds
                                val upBounds = wildcardType.getUpperBounds
                                if lowBounds.nonEmpty || upBounds.size != 1 || upBounds(0) != CLS_ENTITY then
                                    E(s"Unexpected generic type for $IT annotated argument [$z]")
                            case _ => E(s"Unexpected generic type for $IT annotated argument [$z]")
                    case _ =>
                        if COMP_CLS.exists(_ == paramGenType) then
                            if !warned then
                                warned = true
                                logger.warn(s"Unable to detected and type-check method argument [${method2Str(mtd)}, $z]")
                            end if
                        else
                            E(s"Unexpected parameter type for $IT annotated argument [$z]")
            else
                // Other types.
                E(s"Unexpected parameter type for $IT annotated argument [$z]")
        }

    /**
      *
      * @param cfg
      * @param mtd
      * @param paramCls
      * @param limits
      */
    private def checkMinMax(cfg: NCModelConfig, mtd: Method, paramCls: Seq[Class[_]], limits: Seq[(Int, Int)]): Unit =
        require(paramCls.sizeIs == limits.length)

        paramCls.zip(limits).zipWithIndex.foreach { case ((cls, (min, max)), i) =>
            def mkArg(): String = arg2Str(mtd, i)

            val p1 = "its $IT annotated argument"
            val p2 = s"mdlId=${cfg.getId}, arg=${mkArg()}"

            // Argument is single entity but defined as not single entity.
            if cls == CLS_ENTITY && (min != 1 || max != 1) then
                E(s"Intent term must have [1,1] quantifier because $p1 is a single value [$p2]")
            // Argument is not single entity but defined as single entity.
            else if cls != CLS_ENTITY && (min == 1 && max == 1) then
                E(s"Intent term has [1,1] quantifier but $p1 is not a single value [$p2]")
            // Argument is optional but defined as not optional.
            else if cls == CLS_SCALA_OPT && (min != 0 || max != 1) then
                E(s"Intent term must have [0,1] quantifier because $p1 is optional [$p2]")
            // Argument is not optional but defined as optional.
            else if cls != CLS_SCALA_OPT && (min == 0 && max == 1) then
                E(s"Intent term has [0,1] quantifier but $p1 is not optional [$p2]")
        }

    /**
      *
      * @param cfg
      * @param method
      * @param obj
      * @param intent
      * @return
      */
    private def prepareCallback(cfg: NCModelConfig, method: Method, obj: AnyRef, intent: NCIDLIntent): NCCallbackInput => NCResult =
        lazy val z = s"mdlId=${cfg.getId}, intentId=${intent.id}, type=${class2Str(method.getReturnType)}, callback=${method2Str(method)}"

        // Checks method result type.
        if method.getReturnType != CLS_QRY_RES && !CLS_QRY_RES.isAssignableFrom(method.getReturnType) then
            E(s"Unexpected result type for @NCIntent annotated method [$z]")

        val allParamTypes = method.getParameterTypes.toList

        if allParamTypes.sizeIs < 2 then E(s"Unexpected parameters count for $I annotated method [count=${allParamTypes.size}, method=$z]")
        if allParamTypes.head != CLS_CTX then E(s"First parameter for $I annotated method must be NCContext [method=$z]")
        if allParamTypes(1) != CLS_INTENT_MATCH then E(s"Second parameter for $I annotated method must be NCIntentMatch [method=$z]")

        val tokParamAnns = method.getParameterAnnotations.toList.drop(2).filter(_ != null)
        val tokParamTypes = allParamTypes.drop(2)

        // Checks entities parameters annotations count.
        if tokParamAnns.sizeIs != tokParamTypes.length then
            E(s"Unexpected annotations count for $I annotated method [count=${tokParamAnns.size}, $z]")

        // Gets terms IDs.
        val termIds = tokParamAnns.zipWithIndex.map {
            case (annArr, idx) =>
                val termAnns = annArr.filter(_.isInstanceOf[NCIntentTerm])

                // Each method arguments (second and later) must have one 'NCIntentTerm' annotation.
                termAnns.length match
                    case 1 => termAnns.head.asInstanceOf[NCIntentTerm].value
                    case 0 =>
                        if idx == 0 then E(s"Missing $IT annotation or wrong type of the 1st parameter (must be 'NCIntentMatch') for [$z]")
                        else E(s"Missing $IT annotation for [$z]")
                    case _ => E(s"Too many $IT annotations for [$z]")
        }

        if NCUtils.containsDups(termIds) then
            E(s"Duplicate term IDs in $IT annotations [dups=${NCUtils.getDups(termIds).mkString(", ")}, $z]")

        val terms = intent.terms

        // Checks correctness of term IDs.
        // Note we don't restrict them to be duplicated.
        val intentTermIds = terms.flatMap(_.id)
        val invalidIds = termIds.filter(id => !intentTermIds.contains(id))

        if invalidIds.nonEmpty then
            // Report only the first one for simplicity & clarity.
            E(s"Unknown term ID in $IT annotation [termId=${invalidIds.head}, $z]")

        // Checks parameters.
        val paramGenTypes = method.getGenericParameterTypes.toList.drop(2)
        checkTypes(cfg, method, tokParamTypes, paramGenTypes)

        // Checks limits.
        val allLimits = terms.map(t => t.id.orNull -> (t.min, t.max)).toMap
        checkMinMax(cfg, method, tokParamTypes, termIds.map(allLimits))

        (cbData: NCCallbackInput) =>
            val args = mutable.Buffer.empty[AnyRef]
            args += cbData.ctx
            args += cbData.im
            args ++= prepareParams(cfg, method, tokParamTypes, termIds.map(id => cbData.im.getTermEntities(id)))
            invoke(cfg, method, obj, args.toArray)

    /**
      *
      * @return
      */
    def scan(mdl: NCModel): Seq[NCModelIntent] =
        require(mdl != null)

        var compiler = new NCIDLCompiler(mdl.getConfig)

        // Overrides current compiler with new intents but without any cache (imports ang fragments)
        def callNoCache[T](f: () => T): T =
            val cp = compiler.clone()
            try f()
            finally compiler = cp.clone(compiler)

        // Recovers initial compiler state if any error occur, clears all intermediate results.
        def callClear[T](f: () => T): T =
            val cp = compiler.clone()
            try f()
            catch case e: Throwable => { compiler = cp; throw e }

        val cfg = mdl.getConfig
        lazy val z = s"mdlId=${cfg.getId}"
        val intentsMtds = mutable.HashMap.empty[Method, IntentHolder]
        val intentDecls = mutable.HashMap.empty[String, NCIDLIntent]
        val objs = mutable.Buffer.empty[AnyRef]
        val processed = mutable.HashSet.empty[Class[_]]

        def addDecl(intent: NCIDLIntent): Unit =
            intentDecls.get(intent.id) match
                case Some(ex) => if ex.idl != intent.idl then E(s"Intent with given ID already found with different definition [$z, id=${intent.id}]")
                case None => // No-op.
            intentDecls += intent.id -> intent

        def addIntent(intent: NCIDLIntent, mtd: Method, obj: AnyRef): Unit =
            if intentsMtds.contains(mtd) then E(s"The callback cannot have more one intent [$z, callback=${method2Str(mtd)}]")
            intentsMtds += mtd -> IntentHolder(cfg, intent, obj, mtd)

        /**
          *  It is done such way because intents can contain references to 'fragments',
          *  but annotations can be received via java reflection in inordered way.
          */
        def addIntent2Phases(anns: scala.Array[NCIntent], origin: String): Iterable[NCIDLIntent] =
            val errAnns = mutable.ArrayBuffer.empty[NCIntent]
            val intents = mutable.ArrayBuffer.empty[NCIDLIntent]

            def addIntents(ann: NCIntent) = intents ++= compiler.compile(ann.value, origin)

            // 1. First pass.
            for (ann <- anns)
                try callClear(() => addIntents(ann))
                catch case _: NCException => errAnns += ann

            // 2. Second pass.
            for (ann <- errAnns) addIntents(ann)

            // Process all compiled intents.
            for (intent <- intents) addDecl(intent)

            intents

        def processClassAnnotations(cls: Class[_]): Unit =
            if cls != null && processed.add(cls) then
                addIntent2Phases(cls.getAnnotationsByType(CLS_INTENT), class2Str(cls))

                processClassAnnotations(cls.getSuperclass)
                cls.getInterfaces.foreach(processClassAnnotations)

        // First phase scan.
        // For given object finds references via fields (NCIntentObject). Scans also each reference recursively and collects them.
        // For all methods of processed object collects  intents (NCIntent)
        def scan(obj: AnyRef): Unit =
            objs += obj
            processClassAnnotations(obj.getClass)

            // Scans annotated fields.
            for (f <- getAllFields(obj) if f.isAnnotationPresent(CLS_INTENT_OBJ)) scan(getFieldObject(cfg, f, obj))

            val methods = getAllMethods(obj)

            // // Collects intents for each method.
            for (mtd <- methods)
                callNoCache(
                    () =>
                        for (ann <- mtd.getAnnotationsByType(CLS_INTENT); intent <- compiler.compile(ann.value, method2Str(mtd), isMethodLevel = true))
                            addDecl(intent)
                            addIntent(intent, mtd, obj)
                )

        scan(mdl)

        // Second phase. For model and all its references scans each method and finds intents references (NCIntentRef)
        for (
            obj <- objs;
            mtd <- getAllMethods(obj);
            ann <- mtd.getAnnotationsByType(CLS_INTENT_REF)
        )
            val refId = ann.value.strip
            val intent = intentDecls.getOrElse(
                refId,
                E(s"$IR(\"$refId\") references unknown intent ID [$z, callback=${method2Str(mtd)}]")
            )

            addDecl(intent)
            addIntent(intent, mtd, obj)

        val intents = intentsMtds.values

        val unusedIds = intentDecls.keys.filter(k => !intents.exists(_.intent.id == k))
        if unusedIds.nonEmpty then
            logger.warn(s"Intents are unused (have no callback): [$z, intentIds=${col2Str(unusedIds)}]")

        if intents.nonEmpty then
            // Check the uniqueness of intent IDs.
            NCUtils.getDups(intents.map(_.intent.id).toSeq) match
                case ids if ids.nonEmpty => E(s"Duplicate intent IDs [$z, ids=${col2Str(ids)}]")
                case _ => // No-op.
        else
            logger.warn(s"Model has no intent: ${cfg.getId}")

        intents.map(i => NCModelIntent(i.intent, i.function)).toSeq