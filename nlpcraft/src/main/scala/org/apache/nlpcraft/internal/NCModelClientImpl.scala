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

package org.apache.nlpcraft.internal

import org.apache.nlpcraft.{NCModelPipeline, *}
import org.apache.nlpcraft.internal.util.NCUtils
import com.typesafe.scalalogging.LazyLogging

import java.util.concurrent.*
import java.util.List as JList
import java.util.Map as JMap
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.ExecutionContext
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

// TODO: move it to right package.

/**
  *
  * @param mdl
  */
class NCModelClientImpl(mdl: NCModel) extends LazyLogging:
    private val plProc = NCPipelineProcessor(mdl)

    private var plSrvs: Seq[NCLifecycle] = _

    init(mdl.getConfig, mdl.getPipeline)

    /**
      *
      * @param cfg
      * @param pipeline
      */
    private def init(cfg: NCModelConfig, pipeline: NCModelPipeline): Unit =
        // TODO: error texts.
        def check(obj: AnyRef, name: String): Unit =
            if obj == null then throw new NCException(s"Element cannot be null: '$name'")
        def checkList(list: JList[_], name: String): Unit =
            if list == null then throw new NCException(s"List cannot be null: '$name'")
            else if list.isEmpty then throw new NCException(s"List cannot be empty: '$name'")

        check(cfg.getId, "Id")
        check(cfg.getName, "Name")
        check(cfg.getVersion, "Version")
        check(pipeline.getTokenParser, "Token parser")
        checkList(pipeline.getEntityParsers, "Entity parsers")

        val buf = mutable.ArrayBuffer.empty[NCLifecycle] ++ pipeline.getEntityParsers.asScala

        def add[T <: NCLifecycle](list: JList[T]): Unit = if list != null then buf ++= list.asScala

        add(pipeline.getTokenEnrichers)
        add(pipeline.getTokenValidators)
        add(pipeline.getEntityParsers)
        add(pipeline.getEntityParsers)
        add(pipeline.getEntityValidators)
        add(pipeline.getTokenValidators)

        plSrvs = buf.toSeq
        processServices(_.onStart(cfg), "started")

    /**
      *
      * @param action
      * @param actionVerb
      */
    private def processServices(action: NCLifecycle => Unit, actionVerb: String): Unit =
        NCUtils.execPar(plSrvs.map(p =>
            () => {
                action(p)
                logger.info(s"Service $actionVerb: '${p.getClass.getName}'") // TODO:  text.
            }
        )*)(ExecutionContext.Implicits.global)

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      * @return
      */
    def ask(txt: String, data: JMap[String, AnyRef], usrId: String): CompletableFuture[NCResult] = plProc.ask(txt, data, usrId)

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      * @return
      */
    def askSync(txt: String, data: JMap[String, AnyRef], usrId: String): NCResult = plProc.askSync(txt, data, usrId)

    /**
      *
      * @param usrId
      */
    def clearConversation(usrId: String): Unit = () // TODO: implement

    /**
      *
      * @param usrId
      */
    def clearDialog(usrId: String): Unit = () // TODO: implement

    /**
      *
      */
    def close(): Unit =
        plProc.close()
        processServices(_.onStop(mdl.getConfig), "stopped")
