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

import java.util.UUID
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import java.util.function.Predicate
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*


/**
  *
  * @param request
  * @param variants
  * @param tokens
  */
case class NCPipelineData(request: NCRequest, variants: List[NCVariant], tokens: List[NCToken])

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
    private val varFilters = nvl(pipeline.getVariantFilters)

    private val allComps: Seq[NCLifecycle] =
        tokEnrichers ++ entEnrichers ++ entParsers ++ tokVals ++ entVals ++ entMappers ++ varFilters

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
                logger.info(s"${NCUtils.capitalize(actVerb)}: '${p.getClass.getName}'")
            }
        ))(ExecutionContext.Implicits.global)

    /**
      *
      * @param list
      * @tparam T
      */
    private def nvl[T](list: List[T]): Seq[T] = if list == null then List.empty else list

    /**
      *
      * @param m
      */
    private def mkProps(m: NCPropertyMap): String =
        if m.keysSet.isEmpty then ""
        else m.keysSet.toSeq.sorted.map(p => s"$p=${m(p)}").mkString("{", ", ", "}")

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      */
    def prepare(txt: String, data: Map[String, Any], usrId: String): NCPipelineData =
        require(txt != null && usrId != null)

        /**
          *
          * @param ents
          */
        def newVariant(ents: List[NCEntity]): NCVariant =
            new NCVariant:
                override val getEntities: List[NCEntity] = ents

        val req: NCRequest = new NCRequest:
            override val getUserId: String = usrId
            override val getRequestId: String = UUID.randomUUID().toString
            override val getText: String = txt
            override val getReceiveTimestamp: Long = System.currentTimeMillis()
            override val getRequestData: Map[String, Any] = data

        val toks = tokParser.tokenize(txt)

        if toks.nonEmpty then
            for (e <- tokEnrichers) e.enrich(req, cfg, toks)

        val tbl = NCAsciiTable("Text", "Start index", "End index", "Properties")

        for (t <- toks)
            tbl += (
                t.getText,
                t.getStartCharIndex,
                t.getEndCharIndex,
                mkProps(t)
            )
        tbl.info(logger, s"Tokens for: ${req.getText}".?)

        // NOTE: we run validators regardless of whether token list is empty.
        for (v <- tokVals) v.validate(req, cfg, toks)

        var entities: List[NCEntity] = List.empty

        for (p <- entParsers) entities ++= p.parse(req, cfg, toks)

        if entities.nonEmpty then
            for (e <- entEnrichers) e.enrich(req, cfg, entities)

        // NOTE: we run validators regardless of whether entity list is empty.
        for (v <- entVals) v.validate(req, cfg, entities)

        for (m <- entMappers)
            entities = m.map(req, cfg, entities)
            if entities == null then E("Entity mapper cannot return null values.")

        val overlapEnts: Seq[Set[NCEntity]] =
            toks.
            // Looks at each token.
            map(t => t.getIndex -> entities.filter(_.getTokens.contains(t))).
            // Collects all overlapped entities.
            map { case (_, ents) => if ents.sizeIs > 1 then ents.toSet else Set.empty }.filter(_.nonEmpty)

        var variants: List[NCVariant] =
            if overlapEnts.nonEmpty then
                NCModelPipelineHelper.
                    findCombinations(overlapEnts.map(_.asJava).asJava, pool).asScala.
                    map(_.asScala).map(delComb =>
                        val delSet = delComb.toSet
                        newVariant(entities.filter(!delSet.contains(_)))
                    ).toList
            else
                List(newVariant(entities))

        variants = varFilters.foldRight(variants)((filter, vars) => filter.filter(req, cfg, vars))

        // Skips empty variants.
        val vrns = variants.filter(_.getEntities.nonEmpty)

        for (v, i) <- vrns.zipWithIndex do
            val tbl = NCAsciiTable("EntityId", "Tokens", "Tokens Position", "Properties")

            for e <- v.getEntities do
                val toks = e.getTokens
                tbl += (
                    e.getId,
                    toks.map(_.getText).mkString("|"),
                    toks.map(p => s"${p.getStartCharIndex}-${p.getEndCharIndex}").mkString("|"),
                    mkProps(e)
                )
            tbl.info(logger, s"Variant: ${i + 1} (${vrns.size})".?)

        NCPipelineData(req, vrns, toks)

    def start(): Unit = processComponents(_.onStart(cfg), "started")

    /**
      *
      */
    def close(): Unit =
        processComponents(_.onStop(cfg), "stopped")
        NCUtils.shutdownPool(pool)
