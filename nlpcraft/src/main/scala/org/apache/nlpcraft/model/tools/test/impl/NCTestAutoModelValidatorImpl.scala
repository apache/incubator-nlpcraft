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

package org.apache.nlpcraft.model.tools.test.impl

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model.tools.embedded.NCEmbeddedProbe
import org.apache.nlpcraft.model.tools.test.{NCTestAutoModelValidator, NCTestClientBuilder}
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager

import scala.jdk.CollectionConverters.SeqHasAsJava

/**
  * Implementation for `NCTestAutoModelValidator` class.
  */
private [test] object NCTestAutoModelValidatorImpl extends LazyLogging {
    private final val PROP_PROBE_CFG = "NLPCRAFT_PROBE_CONFIG"

    /**
     *
     * @throws Exception Thrown in case of any errors.
     * @return
     */
    @throws[Exception]
    def isValid: Boolean = {
        val prop = NCTestAutoModelValidator.PROP_MODELS

        val classes = U.sysEnv(prop) match {
            case Some(s) => U.splitTrimFilter(s, ",")
            case None =>
                logger.info(s"You can use $C-D$prop=my.Model1,my.Model2$RST to specify model(s) to test with model validator.")

                null
        }
        val cfgFile = U.sysEnv(PROP_PROBE_CFG).orNull

        if (NCEmbeddedProbe.start(cfgFile, classes.asJava))
            try
                process(NCModelManager.getAllModels().map(p => p.model.getId -> p.samples.toMap).toMap.filter(_._2.nonEmpty))
            finally
                NCEmbeddedProbe.stop()
        else
            false
    }

    /**
      *
      * @param samples
      * @return
      */
    private def process(samples: Map[/*Model ID*/String, Map[String/*Intent ID*/, Seq[Seq[String]]/*Samples*/]]): Boolean = {
        case class Result(
            modelId: String,
            intentId: String,
            text: String,
            pass: Boolean,
            error: Option[String],
            time: Long
        )

        val results = samples.flatMap { case (mdlId, samples) =>
            def ask(intentId: String, txts: Seq[String]): Seq[Result] = {
                val cli = new NCTestClientBuilder().newBuilder.build

                try {
                    cli.open(mdlId)

                    txts.map (txt => {
                        var t = U.now()

                        val res = cli.ask(txt)

                        t = U.now() - t

                        if (res.isFailed)
                            Result(mdlId, intentId, txt, pass = false, Some(res.getResultError.get()), t)
                        else if (intentId != res.getIntentId)
                            Result(mdlId, intentId, txt, pass = false, Some(s"Unexpected intent ID '${res.getIntentId}'"), t)
                        else
                            Result(mdlId, intentId, txt, pass = true, None, t)
                    })
                }
                finally
                    cli.close()
            }

            for ((intentId, seq) <- samples; txts <- seq)  yield ask(intentId, txts)
        }.flatMap(_.toSeq)

        val tbl = NCAsciiTable()

        tbl #= ("Model ID", "Intent ID", "+/-", "Text", "Error", "ms.")

        for (res <- results)
            tbl += (
                res.modelId,
                res.intentId,
                if (res.pass) g("OK") else r("FAIL"),
                res.text,
                res.error.getOrElse(""),
                res.time
            )

        val passCnt = results.count(_.pass)
        val failCnt = results.count(!_.pass)
        
        logger.info(s"Model auto-validation results: " +
            s"${g("OK")} $passCnt, ${r("FAIL")} $failCnt:\n${tbl.toString}"
        )
        
        failCnt == 0
    }
}
