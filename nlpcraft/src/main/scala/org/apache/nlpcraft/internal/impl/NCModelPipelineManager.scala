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
import org.apache.nlpcraft.internal.ascii.NCAsciiTable
import org.apache.nlpcraft.internal.conversation.*
import org.apache.nlpcraft.internal.dialogflow.NCDialogFlowManager
import org.apache.nlpcraft.internal.impl.*
import org.apache.nlpcraft.internal.intent.matcher.*
import org.apache.nlpcraft.internal.util.*

import java.util
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import java.util.function.Predicate
import java.util.{ArrayList, Objects, UUID, Collections as JColls, List as JList, Map as JMap}
import scala.collection.{immutable, mutable}
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

/**
  *
  * @param request
  * @param variants
  * @param tokens
  */
case class NCPipelineData(request: NCRequest, variants: Seq[NCVariant], tokens: JList[NCToken])

/**
  *
  * @param cfg
  * @param pipeline
  */
class NCModelPipelineManager(cfg: NCModelConfig, pipeline: NCPipeline) extends LazyLogging:
    private val pool = new java.util.concurrent.ForkJoinPool()
    private val tokParser = pipeline.getTokenParser
    private val tokEnrichers = nvl(pipeline.getTokenEnrichers)
    private val entEnrichers = nvl(pipeline.getEntityEnrichers)
    private val entParsers = nvl(pipeline.getEntityParsers)
    private val tokVals = nvl(pipeline.getTokenValidators)
    private val entVals = nvl(pipeline.getEntityValidators)
    private val entMappers = nvl(pipeline.getEntityMappers)
    private val varFilterOpt = pipeline.getVariantFilter.toScala

    private val allComps: Seq[NCLifecycle] =
        tokEnrichers ++ entEnrichers ++ entParsers ++ tokVals ++ entVals ++ entMappers ++ varFilterOpt.toSeq

    /**
      * Processes pipeline components.
      *
      * @param act Action to process.
      * @param actVerb Action descriptor.
      */
    private def processComponents(act: NCLifecycle => Unit, actVerb: String): Unit =
        NCUtils.execPar(allComps.map(p =>
            () => {
                act(p)
                logger.info(s"Component $actVerb: '${p.getClass.getName}'")
            }
        )*)(ExecutionContext.Implicits.global)

    /**
      *
      * @param list
      * @tparam T
      * @return
      */
    private def nvl[T](list: JList[T]): Seq[T] = if list == null then Seq.empty else list.asScala.toSeq

    /**
      *
      * @param m
      * @return
      */
    private def mkProps(m: NCPropertyMap): String =
        if m.keysSet().isEmpty then ""
        else m.keysSet().asScala.toSeq.sorted.map(p => s"$p=${m.get[Any](p)}").mkString("{", ", ", "}")

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      * @return
      */
    def prepare(txt: String, data: JMap[String, AnyRef], usrId: String): NCPipelineData =
        require(txt != null && usrId != null)

        /**
          *
          * @param ents
          * @return
          */
        def newVariant(ents: Seq[NCEntity]): NCVariant =
            new NCVariant:
                override val getEntities: JList[NCEntity] = ents.asJava

        val req: NCRequest = new NCRequest:
            override val getUserId: String = usrId
            override val getRequestId: String = UUID.randomUUID().toString
            override val getText: String = txt
            override val getReceiveTimestamp: Long = System.currentTimeMillis()
            override val getRequestData: JMap[String, AnyRef] = data

        val toks = tokParser.tokenize(txt)

        if toks.size() > 0 then
            for (e <- tokEnrichers) e.enrich(req, cfg, toks)

        val tbl = NCAsciiTable("Text", "Start index", "End index", "Properties")

        for (t <- toks.asScala)
            tbl += (
                t.getText,
                t.getStartCharIndex,
                t.getEndCharIndex,
                mkProps(t)
            )
        tbl.info(logger, Option(s"Tokens for: ${req.getText}"))

        // NOTE: we run validators regardless of whether token list is empty.
        for (v <- tokVals) v.validate(req, cfg, toks)

        var entsList: util.List[NCEntity] = new util.ArrayList[NCEntity]()

        for (p <- entParsers) entsList.addAll(p.parse(req, cfg, toks))

        if entsList.size() > 0 then
            for (e <- entEnrichers) e.enrich(req, cfg, entsList)

        // NOTE: we run validators regardless of whether entity list is empty.
        for (v <- entVals) v.validate(req, cfg, entsList)

        for (m <- entMappers)
            entsList = m.map(req, cfg, entsList)
            if entsList == null then E("Entity mapper cannot return null values.")

        val entities = entsList.asScala.toSeq

        val overlapEnts: Seq[Set[NCEntity]] =
            toks.asScala.
            // Looks at each token.
            map(t => t.getIndex -> entities.filter(_.getTokens.contains(t))).
            // Collects all overlapped entities.
            map { case (_, ents) => if (ents.sizeIs > 1) ents.toSet else Set.empty }.filter(_.nonEmpty).toSeq

        var variants: JList[NCVariant] =
            if overlapEnts.nonEmpty then
                NCModelPipelineHelper.
                    findCombinations(overlapEnts.map(_.asJava).asJava, pool).
                    asScala.map(_.asScala).map(delComb =>
                        val delSet = delComb.toSet
                        newVariant(entities.filter(!delSet.contains(_)))
                    ).asJava
            else
                Seq(newVariant(entities)).asJava

        if varFilterOpt.isDefined then variants = varFilterOpt.get.filter(req, cfg, variants)

        // Skips empty variants.
        val vrnts = variants.asScala.toSeq.filter(!_.getEntities.isEmpty)

        for ((v, i) <- vrnts.zipWithIndex)
            val tbl = NCAsciiTable("EntityId", "Tokens", "Tokens Position", "Properties")

            for (e <- v.getEntities.asScala)
                val toks = e.getTokens.asScala
                tbl += (
                    e.getId,
                    toks.map(_.getText).mkString("|"),
                    toks.map(p => s"${p.getStartCharIndex}-${p.getEndCharIndex}").mkString("|"),
                    mkProps(e)
                )
            tbl.info(logger, Option(s"Variant: ${i + 1} (${vrnts.size})"))

        NCPipelineData(req, vrnts, toks)

    def start(): Unit = processComponents(_.onStart(cfg), "started")
    /**
      *
      */
    def close(): Unit =
        processComponents(_.onStop(cfg), "stopped")
        NCUtils.shutdownPool(pool)
