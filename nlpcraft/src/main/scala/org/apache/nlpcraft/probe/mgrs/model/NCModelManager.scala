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

package org.apache.nlpcraft.probe.mgrs.model

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.probe.mgrs.NCProbeModel
import org.apache.nlpcraft.probe.mgrs.deploy._

import scala.util.control.Exception._

/**
  * Model manager.
  */
object NCModelManager extends NCService {
    // Deployed models keyed by their IDs.
    @volatile private var data: Map[String, NCProbeModel] = _

    // Access mutex.
    private final val mux = new Object()

    /**
     *
     * @param parent Optional parent span.
     * @throws NCE
     * @return
     */
    @throws[NCE]
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span =>
        ackStarting()

        mux.synchronized {
            data = NCDeployManager.getModels.map(pm => {
                pm.model.onInit()
                pm.model.getId -> pm
            }).toMap

            logger.info(s"Models deployed: ${data.size}")

            data.values.foreach(pm => {
                val mdl = pm.model

                val contCnt = pm.continuousSynonyms.flatMap(_._2.map(_._2.count)).sum
                val sparseCnt = pm.sparseSynonyms.map(_._2.size).sum
                val allIdlSyns = pm.idlSynonyms.values.flatMap(_.toSeq)
                val sparseIdlCnt = allIdlSyns.count(_.sparse)
                val contIdlCnt = allIdlSyns.size - sparseIdlCnt

                def withWarn(i: Int): String = if (i == 0) s"0 ${r("(!)")}" else i.toString

                val tbl = NCAsciiTable()

                tbl += Seq(
                    s"${B}Name:$RST                  ${bo(c(mdl.getName))}",
                    s"${B}ID:$RST                    ${bo(mdl.getId)}",
                    s"${B}Version:$RST               ${mdl.getVersion}",
                    s"${B}Origin:$RST                ${mdl.getOrigin}",
                    s"${B}Elements:$RST              ${withWarn(pm.elements.keySet.size)}"
                )
                tbl += Seq(
                    s"${B}Synonyms:$RST",
                    s"$B   Simple continuous:$RST  $contCnt",
                    s"$B   Simple sparse:$RST      $sparseCnt",
                    s"$B   IDL continuous:$RST     $contIdlCnt",
                    s"$B   IDL sparse:$RST         $sparseIdlCnt",

                )
                tbl += Seq(s"${B}Intents:$RST ${withWarn(pm.intents.size)}") ++
                    (
                        for (cb <- pm.callbacks) yield
                        s"   ${g(bo(cb._1))} @ ${m(cb._2.origin)} -> ${c(cb._2.className)}${bo("#")}${c(cb._2.methodName)}(...)"
                    ).toSeq

                tbl.info(logger, Some("Model:"))
            })
        }

        addTags(
            span,
            "deployedModels" -> data.values.map(_.model.getId).mkString(",")
        )

        ackStarted()
    }

    /**
     * Stops this component.
     */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        mux.synchronized {
            if (data != null)
                data.values.foreach(m => discardModel(m.model))
        }

        ackStopped()
    }

    /**
      *
      * @param mdl
      */
    private def discardModel(mdl: NCModel): Unit = {
        require(Thread.holdsLock(mux))

        ignoring(classOf[Throwable]) {
            // Ack.
            logger.info(s"Model discarded: ${mdl.getId}")

            mdl.onDiscard()
        }
    }

    /**
      *
      * @return
      */
    def getAllModels(parent: Span = null): List[NCProbeModel] =
        startScopedSpan("getAllModels", parent) { _ =>
            mux.synchronized { data.values.toList }
        }

    /**
      *
      * @param mdlId Model ID.
      */
    def getModelOpt(mdlId: String, parent: Span = null): Option[NCProbeModel] =
        startScopedSpan("getModelOpt", parent, "mdlId" -> mdlId) { _ =>
            mux.synchronized { data.get(mdlId) }
        }

    /**
     *
     * @param mdlId Model ID.
     */
    def getModel(mdlId: String, parent: Span = null): NCProbeModel =
        getModelOpt(mdlId, parent).getOrElse(throw new NCE(s"Model not found: $mdlId"))
}