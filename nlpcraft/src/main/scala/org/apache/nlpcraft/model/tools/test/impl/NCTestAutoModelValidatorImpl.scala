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

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.util.NCUtils
import org.apache.nlpcraft.model.tools.embedded.NCEmbeddedProbe
import org.apache.nlpcraft.model.tools.test.NCTestClientBuilder
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager

/**
  * Implementation for `NCTestAutoModelValidator` class.
  */
private [test] object NCTestAutoModelValidatorImpl extends LazyLogging {
    private final val PROP_MODELS = "NLPCRAFT_TEST_MODELS"

    @throws[Exception]
    def isValid: Boolean =
        NCUtils.sysEnv(PROP_MODELS) match {
            case Some(p) ⇒ isValid(getClasses(p.split(",")))
            case None ⇒
                logger.warn(s"System property '$PROP_MODELS' is not defined.")

                // TODO:
                false
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
        NCEmbeddedProbe.start(classes: _*)

        try
            process(NCModelManager.getAllModelsData().map(p ⇒ p.model.getId → p.samples.toMap).toMap.filter(_._2.nonEmpty))
        finally
            NCEmbeddedProbe.stop()
    }

    /**
      *
      * @param samples
      * @return
      */
    private def process(samples: Map[/*Model ID*/String, Map[String/*Intent ID*/, Seq[String]/*Samples*/]]): Boolean = {
        case class Result(
            modelId: String,
            intentId: String,
            text: String,
            pass: Boolean,
            error: Option[String]
        )
        
        val results = samples.flatMap { case (mdlId, samples) ⇒
            val cli = new NCTestClientBuilder().newBuilder.build
    
            cli.open(mdlId)
    
            try {
                def ask(intentId: String, txt: String): Result = {
                    val res = cli.ask(txt)
            
                    if (res.isFailed)
                        Result(mdlId, intentId, txt, pass = false, Some(res.getResultError.get()))
                    else if (intentId != res.getIntentId)
                        Result(mdlId, intentId, txt, pass = false, Some(s"Unexpected intent ID '${res.getIntentId}'"))
                    else
                        Result(mdlId, intentId, txt, pass = true, None)
                }
                
                for ((intentId, seq) ← samples; txt ← seq) yield ask(intentId, txt)
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
