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
import org.apache.nlpcraft.internal.*
import org.apache.nlpcraft.internal.util.*

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference
import java.util.{Objects, List as JList, Map as JMap, Collections as JColls}
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*

/**
  *
  * @param mdl
  */
class NCModelClientImpl(mdl: NCModel) extends LazyLogging:
    private val plProc = NCModelPipelineProcessor(mdl)
    private var plSrvs: Seq[NCLifecycle] = _

    init(mdl.getConfig, mdl.getPipeline)

    /**
      *
      * @param cfg
      * @param pipeline
      */
    private def init(cfg: NCModelConfig, pipeline: NCModelPipeline): Unit =
        Objects.requireNonNull(cfg.getId, "Model ID cannot be null.")
        Objects.requireNonNull(cfg.getName, "Model name cannot be null.")
        Objects.requireNonNull(cfg.getVersion, "Model version cannot be null.")
        Objects.requireNonNull(pipeline.getTokenParser, "Token parser cannot be null.")
        Objects.requireNonNull(pipeline.getEntityParsers, "List of entity parsers in the pipeline cannot be null.")
        if pipeline.getEntityParsers.isEmpty then E(s"At least one entity parser must be specified in the pipeline.")

        val buf = mutable.ArrayBuffer.empty[NCLifecycle] ++ pipeline.getEntityParsers.asScala

        def add[T <: NCLifecycle](list: JList[T]): Unit = if list != null then buf ++= list.asScala

        add(pipeline.getTokenEnrichers)
        add(pipeline.getTokenValidators)
        add(pipeline.getEntityEnrichers)
        add(pipeline.getEntityParsers)
        add(pipeline.getEntityValidators)
        if pipeline.getVariantFilter.isPresent then add(JColls.singletonList(pipeline.getVariantFilter.get()))

        plSrvs = buf.toSeq
        processServices(_.onStart(cfg), "started")

    /**
      *
      * @param act
      * @param actVerb
      */
    private def processServices(act: NCLifecycle => Unit, actVerb: String): Unit =
        NCUtils.execPar(plSrvs.map(p =>
            () => {
                act(p)
                logger.info(s"Service $actVerb: '${p.getClass.getName}'")
            }
        )*)(ExecutionContext.Implicits.global)

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      * @return
      */
    def ask(txt: String, data: JMap[String, AnyRef], usrId: String): CompletableFuture[NCResult] =
        plProc.ask(txt, data, usrId)

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      * @return
      */
    def askSync(txt: String, data: JMap[String, AnyRef], usrId: String): NCResult =
        plProc.askSync(txt, data, usrId)

    /**
      *
      * @param usrId
      */
    def clearConversation(usrId: String): Unit = ???

    /**
      *
      * @param usrId
      */
    def clearDialog(usrId: String): Unit = ???

    /**
      *
      */
    def close(): Unit =
        plProc.close()
        processServices(_.onStop(mdl.getConfig), "stopped")
