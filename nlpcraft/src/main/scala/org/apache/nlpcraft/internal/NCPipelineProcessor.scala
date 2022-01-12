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

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.*

import java.util
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference
import java.util.{ArrayList, UUID, List as JList, Map as JMap}
import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*

// TODO: move it to right package.

/**
  *
  */
object NCPipelineProcessor {
    case class VariantsHolder(request: NCRequest, variants: Seq[NCVariant], checkCancel: Option[() => Unit])
}

import org.apache.nlpcraft.internal.NCPipelineProcessor.*

/**
  *
  * @param mdl */
class NCPipelineProcessor(mdl: NCModel) extends LazyLogging :
    require(mdl != null)
    require(mdl.getPipeline.getTokenParser != null)

    private val pipeline = mdl.getPipeline
    private val pool = new java.util.concurrent.ForkJoinPool()
    private val cfg = mdl.getConfig
    private val tokParser = pipeline.getTokenParser
    private val tokEnrichers = nvl(pipeline.getTokenEnrichers)
    private val entEnrichers = nvl(pipeline.getEntityEnrichers)
    private val entParsers = nvl(pipeline.getEntityParsers)
    private val tokenValidators = nvl(pipeline.getTokenValidators)
    private val entityValidators = nvl(pipeline.getEntityValidators)
    private val variantValidators = nvl(pipeline.getVariantValidators)

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
    private def matchAndExecute(h: VariantsHolder): NCResult = ??? // TODO: implement.

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      * @param checkCancel
      * @return
      */
    // It returns intermediate variants holder just for test reasons.
    private[internal] def prepare(
        txt: String, data: JMap[String, AnyRef], usrId: String, checkCancel: Option[() => Unit] = None
    ): VariantsHolder =
        require(txt != null && usrId != null)

        val check = checkCancel.getOrElse(() => ())

        val toks = tokParser.tokenize(txt)
        if toks.isEmpty then throw new NCException(s"Unsupported empty request: $txt") // TODO: error text

        val req: NCRequest = new NCRequest:
            override val getUserId: String = usrId
            override val getRequestId: String = UUID.randomUUID().toString
            override val getText: String = txt
            override val getReceiveTimestamp: Long = System.currentTimeMillis()
            override val getRequestData: JMap[String, AnyRef] = data

        for (e <- tokEnrichers)
            check()
            e.enrich(req, cfg, toks)

        for (v <- tokenValidators)
            check()
            v.validate(req, cfg, toks)

        val entsList = new util.ArrayList[NCEntity]()

        for (p <- entParsers)
            check()
            val ents = p.parse(req, cfg, toks)
            if ents == null then
                // TODO: error text.
                throw new NCException(s"Invalid entities parser null result [text=$txt, parser=${p.getClass.getName}]")
            entsList.addAll(ents)

        // TODO: error text.
        if entsList.isEmpty then throw new NCException(s"No entities found for text: $txt")

        for (e <- entEnrichers)
            check()
            e.enrich(req, cfg, entsList)
        for (v <- entityValidators)
            check()
            v.validate(req, cfg, entsList)

        val entities = entsList.asScala.toSeq

        val overEntities: Seq[Set[NCEntity]] =
            toks.asScala.
            // Looks at each token.
            map(t => t.getIndex -> entities.filter(_.getTokens.contains(t))).
            // Collects all overlapped entities.
            map { case (_, ents) => if (ents.sizeIs > 1) ents.toSet else Set.empty }.filter(_.nonEmpty).toSeq

        def mkVariant(entities: Seq[NCEntity]): NCVariant =
            new NCVariant:
                override def getEntities: JList[NCEntity] = entities.asJava

        var variants: JList[NCVariant] =
            if overEntities.nonEmpty then
                val dels = NCSentenceHelper.findCombinations(overEntities.map(_.asJava).asJava, pool).asScala.map(_.asScala)

                dels.map(delComb =>
                    val delSet = delComb.toSet
                    mkVariant(entities.filter(e => !delSet.contains(e)))
                ).asJava
            else
                Seq(mkVariant(entities)).asJava

        for (v <- variantValidators)
            check()
            variants = v.filter(req, cfg, variants)

        VariantsHolder(req, variants.asScala.toSeq, checkCancel)

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      * @return
      */
    def askSync(txt: String, data: JMap[String, AnyRef], usrId: String): NCResult =
        matchAndExecute(prepare(txt, data, usrId))

    /**
      *
      * @param txt
      * @param data
      * @param usrId
      * @return
      */
    def ask(txt: String, data: JMap[String, AnyRef], usrId: String): CompletableFuture[NCResult] =
        val fut = new CompletableFuture[NCResult]

        // TODO: error text.
        def check = () =>
            if fut.isCancelled then
                val txt = "Execution interrupted."
                logger.warn(txt)
                throw new NCException(txt)

        fut.completeAsync(() => matchAndExecute(prepare(txt, data, usrId, Option(check))))

    /**
      *
      */
    def close(): Unit = NCUtils.shutdownPool(pool)
