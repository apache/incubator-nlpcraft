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
import org.apache.nlpcraft.internal.makro.NCMacroParser
import org.apache.nlpcraft.internal.util.NCUtils

import java.lang.reflect.Method
import scala.collection.mutable

/**
  *
  * @param mdl
  */
object NCIntentsProcessor extends LazyLogging:
    type Sample = (String/* Intent ID */, Seq[Seq[String]] /* List of list of input samples for that intent. */)

    private final val SEPARATORS = Seq('?', ',', '.', '-', '!')

    private final val CLS_INTENT = classOf[NCIntent]
    private final val CLS_INTENT_REF = classOf[NCIntentRef]
    private final val CLS_SAMPLE = classOf[NCIntentSample]
    private final val CLS_SAMPLE_REF = classOf[NCIntentSampleRef]

    // TODO:
//    private final val CLS_INTENT_MATCH = classOf[NCIntentMatch]
//    private final val CLS_QRY_RES = classOf[NCResult]
//    private final val CLS_MDL_CLS_REF = classOf[NCModelAddClasses]
//    private final val CLS_MDL_PKGS_REF = classOf[NCModelAddPackage]

    def scan(mdl: NCModel): NCIntentsHolder = null // TODO:

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
      *
      * @param mtd
      * @return
      */
    private def method2Str(mtd: Method): String =
        val cls = mtd.getDeclaringClass.getSimpleName
        val name = mtd.getName
        val args = mtd.getParameters.map(_.getType.getSimpleName).mkString(", ")

        s"$cls#$name($args)"

    private def scanSamples(mdl: NCModel): Set[Sample] =
        val mdlId = mdl.getConfig.getId
        val samples = mutable.Buffer.empty[Sample]

        for (m <- getAllMethods(mdl))
            val mtdStr = method2Str(m)
            val smpAnns = m.getAnnotationsByType(CLS_SAMPLE)
            val smpAnnsRef = m.getAnnotationsByType(CLS_SAMPLE_REF)
            val intAnns = m.getAnnotationsByType(CLS_INTENT)
            val refAnns = m.getAnnotationsByType(CLS_INTENT_REF)

            if smpAnns.nonEmpty || smpAnnsRef.nonEmpty then
                if intAnns.isEmpty && refAnns.isEmpty then
                    throw new NCException(s"@NCIntentSample or @NCIntentSampleRef annotations without corresponding @NCIntent or @NCIntentRef annotations: $mtdStr")
                else
                    def read[T](
                        annArr: Array[T],
                        annName: String,
                        getSamples: T => Seq[String],
                        getSource: Option[T => String]
                    ): Seq[Seq[String]] =
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
                            smpAnns, "@NCIntentSample", _.value().toSeq, None
                        ) ++
                            read[NCIntentSampleRef](
                                smpAnnsRef, "@NCIntentSampleRef", a => NCUtils.readResource(a.value()), Option(_.value())
                            )

                    if NCUtils.containsDups(seqSeq.flatMap(_.toSeq).toList) then
                        logger.warn(s"@NCIntentSample and @NCIntentSampleRef annotations have duplicates: $mtdStr")

                    val distinct = seqSeq.map(_.distinct).distinct

                    // TODO:
//                    for (ann <- intAnns; intent <- NCIdlCompiler.compileIntents(ann.value(), mdl, mtdStr))
//                        samples += (intent.id -> distinct)

                    for (ann <- refAnns) samples += (ann.value() -> distinct)
            else if intAnns.nonEmpty || refAnns.nonEmpty then
                logger.warn(s"@NCIntentSample or @NCIntentSampleRef annotations are missing for: $mtdStr")

        samples.toSet


