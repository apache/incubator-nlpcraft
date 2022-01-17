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
import org.apache.nlpcraft.internal.impl.*
import org.apache.nlpcraft.internal.util.*

import java.util
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import java.util.{ArrayList, UUID, List as JList, Map as JMap}
import scala.collection.immutable
import scala.jdk.OptionConverters.*
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*

/**
  *
  * @param mdl
  */
class NCModelPipelineProcessor(mdl: NCModel) extends LazyLogging:
    /**
      *
      * @param req
      * @param vars
      * @param checkCancel
      */
    case class VariantsHolder(req: NCRequest, vars: Seq[NCVariant], checkCancel: Option[() => Unit])

    require(mdl != null)
    require(mdl.getPipeline.getTokenParser != null)
    require(mdl.getPipeline.getEntityParsers != null)
    require(mdl.getPipeline.getEntityParsers.size() > 0)

    private val pipeline = mdl.getPipeline
    private val pool = new java.util.concurrent.ForkJoinPool()
    private val cfg = mdl.getConfig
    private val tokParser = pipeline.getTokenParser
    private val tokEnrichers = nvl(pipeline.getTokenEnrichers)
    private val entEnrichers = nvl(pipeline.getEntityEnrichers)
    private val entParsers = nvl(pipeline.getEntityParsers)
    private val tokVals = nvl(pipeline.getTokenValidators)
    private val entVals = nvl(pipeline.getEntityValidators)
    private val varFilter = pipeline.getVariantFilter.toScala

    /**
      *
      * @param list
      * @tparam T
      * @return
      */
    private def nvl[T](list: JList[T]): Seq[T] = if list == null then Seq.empty else list.asScala.toSeq

    /**
      *
      * @param h
      * @return
      */
    private def matchIntent(h: VariantsHolder): NCResult = ???

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      * @param checkCancel
      * @return
      */
    private[internal] def prepVariants(
        txt: String,
        data: JMap[String, AnyRef],
        usrId: String,
        checkCancel: Option[() => Unit] = None
    ): VariantsHolder =
        require(txt != null && usrId != null)

        /**
          *
          * @param ents
          * @return
          */
        def newVariant(ents: Seq[NCEntity]): NCVariant =
            new NCVariant:
                override val getEntities: JList[NCEntity] = ents.asJava

        val check = checkCancel.getOrElse(() => ())
        val req: NCRequest = new NCRequest:
            override val getUserId: String = usrId
            override val getRequestId: String = UUID.randomUUID().toString
            override val getText: String = txt
            override val getReceiveTimestamp: Long = System.currentTimeMillis()
            override val getRequestData: JMap[String, AnyRef] = data

        val toks = tokParser.tokenize(txt)

        if toks.size() > 0 then
            for (e <- tokEnrichers)
                check()
                e.enrich(req, cfg, toks)

        // NOTE: we run validators regardless of whether token list is empty.
        for (v <- tokVals)
            check()
            v.validate(req, cfg, toks)

        val entsList = new util.ArrayList[NCEntity]()

        for (p <- entParsers)
            check()
            entsList.addAll(p.parse(req, cfg, toks))

        if entsList.size() > 0 then
            for (e <- entEnrichers)
                check()
                e.enrich(req, cfg, entsList)

        // NOTE: we run validators regardless of whether entity list is empty.
        for (v <- entVals)
            check()
            v.validate(req, cfg, entsList)

        val entities = entsList.asScala.toSeq

        val overlapEnts: Seq[Set[NCEntity]] =
            toks.asScala.
            // Looks at each token.
            map(t => t.getIndex -> entities.filter(_.getTokens.contains(t))).
            // Collects all overlapped entities.
            map { case (_, ents) => if (ents.sizeIs > 1) ents.toSet else Set.empty }.filter(_.nonEmpty).toSeq

        var variants: JList[NCVariant] =
            if overlapEnts.nonEmpty then
                NCModelPipelineHelper.findCombinations(overlapEnts.map(_.asJava).asJava, pool)
                    .asScala.map(_.asScala).map(delComb =>
                        val delSet = delComb.toSet
                        newVariant(entities.filter(!delSet.contains(_)))
                    ).asJava
            else
                Seq(newVariant(entities)).asJava

        if varFilter.isDefined then
            check()
            variants = varFilter.get.filter(req, cfg, variants)

        VariantsHolder(req, variants.asScala.toSeq, checkCancel)

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      * @return
      * @throws NCRejection
      * @throws NCCuration
      * @throws NCException
      */
    def askSync(txt: String, data: JMap[String, AnyRef], usrId: String): NCResult =
        matchIntent(prepVariants(txt, data, usrId))

    /**
      * TODO: explain all exceptions that are thrown by the future.
      *
      * @param txt
      * @param data
      * @param usrId
      * @return
      */
    def ask(txt: String, data: JMap[String, AnyRef], usrId: String): CompletableFuture[NCResult] =
        val fut = new CompletableFuture[NCResult]
        val check = () => if fut.isCancelled then
            E(s"Asynchronous ask is interrupted [txt=$txt, usrId=$usrId]")

        fut.completeAsync(() => matchIntent(prepVariants(txt, data, usrId, Option(check))))

    /**
      *
      */
    def close(): Unit = NCUtils.shutdownPool(pool)
