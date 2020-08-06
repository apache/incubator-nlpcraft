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

package org.apache.nlpcraft.model.tools.test.impl

import java.lang.reflect.Method

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.util.NCUtils
import org.apache.nlpcraft.model.intent.impl.NCIntentDslCompiler
import org.apache.nlpcraft.model.tools.test.NCTestClientBuilder
import org.apache.nlpcraft.model.{NCIntent, NCIntentRef, NCIntentSample, NCModel}
import org.apache.nlpcraft.probe.embedded.NCEmbeddedProbe

/**
  * Implementation for `NCTestAutoModelValidator` class.
  */
private [test] object NCTestAutoModelValidatorImpl extends LazyLogging {
    case class IntentSamples(intentId: String, samples: List[String])
    
    private final val PROP_MODELS = "NLPCRAFT_TEST_MODELS"

    private final val CLS_SAMPLE = classOf[NCIntentSample]
    private final val CLS_INTENT = classOf[NCIntent]
    private final val CLS_INTENT_REF = classOf[NCIntentRef]

    @throws[Exception]
    def isValid: Boolean =
        NCUtils.sysEnv(PROP_MODELS) match {
            case Some(p) ⇒ isValid(getClasses(p.split(",")))
            case None ⇒
                logger.warn(s"System property '$PROP_MODELS' is not defined.")

                true
        }

    @throws[Exception]
    def isValidForClass(claxx: Class[_ <: NCModel]): Boolean =
        isValid(Seq(claxx))

    @throws[Exception]
    def isValidForModelIds(mdlIds: String): Boolean =
        isValid(getClasses(mdlIds.split(",")))
    
    @throws[Exception]
    def isValidForModelIds(mdlIds: java.util.Collection[String]): Boolean =
        isValid(getClasses(mdlIds.toArray().asInstanceOf[Array[String]]))

    @throws[Exception]
    def isValidForModelIds(mdlIds: Array[String]): Boolean = isValid(getClasses(mdlIds))

    @throws[Exception]
    private def isValid(classes: Seq[Class[_ <: NCModel]]) = {
        val samples = getSamples(classes.map(cl ⇒ cl → cl.getDeclaredConstructor().newInstance().getId).toMap)

        NCEmbeddedProbe.start(classes: _*)

        try
            process(samples)
        finally
            NCEmbeddedProbe.stop()
    }

    /**
      *
      * @param samples
      * @return
      */
    private def process(samples: Map[/*Model ID*/String, List[IntentSamples]]): Boolean = {
        case class Result(
            modelId: String,
            intentId: String,
            text: String,
            pass: Boolean,
            error: Option[String]
        )
        
        val results = samples.flatMap { case (mdlId, smpList) ⇒
            val cli = new NCTestClientBuilder().newBuilder.build
    
            cli.open(mdlId)
    
            try {
                def ask(intentId: String, txt: String): Result = {
                    val res = cli.ask(txt)
            
                    if (res.isFailed)
                        Result(mdlId, intentId, txt, false, Some(res.getResultError.get()))
                    else if (intentId != res.getIntentId)
                        Result(mdlId, intentId, txt, false, Some(s"Unexpected intent ID '${res.getIntentId}'"))
                    else
                        Result(mdlId, intentId, txt, true, None)
                }
                
                for (smp ← smpList; txt ← smp.samples) yield ask(smp.intentId, txt)
            }
            finally
                cli.close()
        }.toList
        
        // Sort for better output.
        results.sortBy(res ⇒ (res.modelId, res.intentId))
    
        val tbl = NCAsciiTable()
    
        tbl #= ("Model ID", "Intent ID", "+/-", "Text", "Error")
        
        for (res ← results)
            tbl += (
                res.modelId,
                res.intentId,
                if (res.pass) "OK" else "FAIL",
                res.text,
                res.error.getOrElse("")
            )
        
        val passCnt = results.count(_.pass)
        val failCnt = results.count(!_.pass)
        
        if (failCnt > 0)
            logger.error(s"Some model auto-validation failed - see details below...")
        
        logger.info(s"\n\nModel auto-validation results: OK $passCnt, FAIL $failCnt:\n${tbl.toString}")
        
        failCnt == 0
    }
    
    private def mkMethodName(mtd: Method): String =
        s"${mtd.getDeclaringClass.getName}#${mtd.getName}(...)"

    /**
      *
      * @param mdls
      * @return
      */
    private def getSamples(mdls: Map[Class[_ <: NCModel], String]): Map[/*Model ID*/String, List[IntentSamples]] =
        mdls.flatMap { case (claxx, mdlId) ⇒
            var annFound = false

            val mdlData = claxx.getDeclaredMethods.flatMap(method ⇒ {
                val smpAnn = method.getAnnotation(CLS_SAMPLE)
                val intAnn = method.getAnnotation(CLS_INTENT)
                val refAnn = method.getAnnotation(CLS_INTENT_REF)

                if (smpAnn != null || intAnn != null || refAnn != null) {
                    annFound = true

                    def mkIntentId(): String =
                        if (intAnn != null)
                            NCIntentDslCompiler.compile(intAnn.value(), mdlId).id
                        else if (refAnn != null)
                            refAnn.value().trim
                        else
                            throw new AssertionError()

                    if (smpAnn != null) {
                        if (intAnn == null && refAnn == null) {
                            logger.warn(s"@NCTestSample annotation without corresponding @NCIntent or @NCIntentRef annotations " +
                                s"in method (ignoring): ${mkMethodName(method)}")

                            None
                        }
                        else {
                            val samples = smpAnn.value().toList

                            if (samples.isEmpty) {
                                logger.warn(s"@NCTestSample annotation is empty in method (ignoring): ${mkMethodName(method)}")

                                None
                            }
                            else
                                Some(IntentSamples(mkIntentId(), samples))
                        }
                    }
                    else {
                        logger.warn(s"@NCTestSample annotation is missing in method (ignoring): ${mkMethodName(method)}")

                        None
                    }
                }
                else
                    None
            }).toList

            if (mdlData.isEmpty) {
                if (!annFound)
                    logger.warn(s"Model '$mdlId' doesn't have any intents to test.")

                None
            }
            else
                Some(mdlId → mdlData)
        }

    /**
      *
      * @param s
      * @return
      */
    private def getClasses(s: Array[String]): Seq[Class[_ <: NCModel]] = {
        val clsLdr = Thread.currentThread().getContextClassLoader

        s.
            map(_.trim).
            filter(_.nonEmpty).
            map(clsLdr.loadClass).
            map(_.asSubclass(classOf[NCModel]))
    }
}
