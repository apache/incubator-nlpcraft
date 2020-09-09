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

import java.util

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.impl.NCModelWrapper
import org.apache.nlpcraft.model.intent.impl.NCIntentScanner
import org.apache.nlpcraft.probe.mgrs.deploy._

import scala.collection.JavaConverters._
import scala.collection.convert.DecorateAsScala
import scala.util.control.Exception._

/**
  * Model manager.
  */
object NCModelManager extends NCService with DecorateAsScala {
    // Deployed models keyed by their IDs.
    @volatile private var models: Map[String, NCModelWrapper] = _

    // Access mutex.
    private final val mux = new Object()


    @throws[NCE]
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span ⇒
        val tbl = NCAsciiTable("Model ID", "Name", "Ver.", "Elements", "Synonyms")

        mux.synchronized {
            models = NCDeployManager.getModels.map(mdl ⇒ {
                mdl.onInit()

                mdl.proxy.getId → mdl
            }).toMap

            models.values.foreach(mdl ⇒ {
                val synCnt = mdl.syns.values.flatMap(_.values).flatten.size

                tbl += (
                    mdl.getId,
                    mdl.getName,
                    mdl.getVersion,
                    mdl.elms.keySet.size,
                    synCnt
                )
            })
        }

        tbl.info(logger, Some(s"Models deployed: ${models.size}\n"))

        addTags(
            span,
            "deployedModels" → models.values.map(_.getId).mkString(",")
        )

        super.start()
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
      * Stops this component.
      */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        mux.synchronized {
            if (models != null)
                models.values.foreach(m ⇒ discardModel(m))
        }

        super.stop()
    }


    /**
      *
      * @return
      */
    def getAllModels(parent: Span = null): List[NCModelWrapper] =
        startScopedSpan("getAllModels", parent) { _ ⇒
            mux.synchronized {
                models.values.toList
            }
        }

    /**
      *
      * @param mdlId Model ID.
      * @return
      */
    def getModel(mdlId: String, parent: Span = null): Option[NCModelWrapper] =
        startScopedSpan("getModel", parent, "modelId" → mdlId) { _ ⇒
            mux.synchronized {
                models.get(mdlId)
            }
        }

    /**
      * TODO:
      * Gets model data which can be transferred between probe and server.
      *
      * @param mdlId Model ID.
      * @param parent
      * @return
      */
    def getModelInfo(mdlId: String, parent: Span = null): java.util.Map[String, Any] =
        startScopedSpan("getModel", parent, "mdlId" → mdlId) { _ ⇒
            val mdl = mux.synchronized { models.get(mdlId) }.getOrElse(throw new NCE(s"Model not found: '$mdlId'"))

            val data = new util.HashMap[String, Any]()

            data.put("macros", mdl.getMacros)
            data.put("synonyms", mdl.getElements.asScala.map(p ⇒ p.getId → p.getSynonyms).toMap.asJava)
            data.put("samples", NCIntentScanner.scanIntentsSamples(mdl.proxy).samples.map(p ⇒ p._1 → p._2.asJava).asJava)

            data
        }
}