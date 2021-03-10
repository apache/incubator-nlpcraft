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

package org.apache.nlpcraft.probe.mgrs.model

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.probe.mgrs.NCProbeModel
import org.apache.nlpcraft.probe.mgrs.deploy._

import scala.collection.convert.DecorateAsScala
import scala.util.control.Exception._

/**
  * Model manager.
  */
object NCModelManager extends NCService with DecorateAsScala {
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
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span ⇒
        ackStarting()

        val tbl = NCAsciiTable("Model", "Intents")

        mux.synchronized {
            data = NCDeployManager.getModels.map(w ⇒ {
                w.model.onInit()
                w.model.getId → w
            }).toMap

            data.values.foreach(w ⇒ {
                val mdl = w.model

                val synCnt = w.synonyms.flatMap(_._2.map(_._2.count)).sum
                val elmCnt = w.elements.keySet.size
                val intentCnt = w.intents.size

                tbl += (
                    Seq(
                        s"${mdl.getName}",
                        s"ID: ${bo(mdl.getId)}, ver: ${mdl.getVersion}",
                        s"Elements: $elmCnt" + (if (elmCnt == 0) s" ${r("(!)")}" else ""),
                        s"Synonyms: $synCnt" + (if (synCnt == 0) s" ${r("(!)")}" else ""),
                        s"Intents: $intentCnt" + (if (intentCnt == 0) s" ${r("(!)")}" else "")
                    ),
                    w.intents
                        .map(_.dsl)
                        .flatMap(s ⇒
                            s
                            .replaceAll("intent=", s"${g("intent")}=")
                            .replaceAll("fragment=", s"${b("fragment")}=")
                            .replaceAll(" term", s"\n  ${c("term")}").split("\n")
                        )
                )
            })
        }

        tbl.info(logger, Some(s"Models deployed: ${data.size}"))

        addTags(
            span,
            "deployedModels" → data.values.map(_.model.getId).mkString(",")
        )

        ackStarted()
    }

    /**
     * Stops this component.
     */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        ackStopping()

        mux.synchronized {
            if (data != null)
                data.values.foreach(m ⇒ discardModel(m.model))
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
        startScopedSpan("getAllModels", parent) { _ ⇒
            mux.synchronized { data.values.toList }
        }

    /**
      *
      * @param mdlId Model ID.
      */
    def getModelOpt(mdlId: String, parent: Span = null): Option[NCProbeModel] =
        startScopedSpan("getModelOpt", parent, "mdlId" → mdlId) { _ ⇒
            mux.synchronized { data.get(mdlId) }
        }

    /**
     *
     * @param mdlId Model ID.
     */
    def getModel(mdlId: String, parent: Span = null): NCProbeModel =
        getModelOpt(mdlId, parent).getOrElse(throw new NCE(s"Model not found: $mdlId"))
}