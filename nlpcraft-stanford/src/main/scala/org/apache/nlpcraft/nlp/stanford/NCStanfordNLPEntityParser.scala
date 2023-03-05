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

package org.apache.nlpcraft.nlp.stanford

import edu.stanford.nlp.ling.CoreAnnotations.NormalizedNamedEntityTagAnnotation
import edu.stanford.nlp.pipeline.*
import org.apache.nlpcraft.*

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/**
  * [[https://nlp.stanford.edu/ Stanford NLP]] based language independent [[NCEntityParser entity parser]] configured by
  * given Stanford NLP pipeline instance.
  *
  * This parser creates entities with ID `stanford:modelName`, where `modelName` is model configured in Stanford NLP pipeline.
  * Also this parser copies optional `nne` string and `confidence` double [[NCPropertyMap metadata]] properties to the
  * created entities extracted from Stanford NLP annotations.

  * @param stanford Stanford NLP pipeline instance.
  * @param supported Supported Stanford NLP model names. Only supported models will be used for [[NCEntity]] instances generation.
  */
class NCStanfordNLPEntityParser(stanford: StanfordCoreNLP, supported: Set[String]) extends NCEntityParser:
    require(stanford != null, "Stanford instance cannot be null.")
    require(supported != null && supported.nonEmpty, "Supported elements set cannot be null or empty.")

    private val supportedLc = supported.map(_.toLowerCase)

    /** @inheritdoc */
    override def parse(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): List[NCEntity] =
        val doc = new CoreDocument(req.getText)
        stanford.annotate(doc)

        val res = scala.collection.mutable.ArrayBuffer.empty[NCEntity]

        for (e <- doc.entityMentions().asScala)
            val typ = e.entityType().toLowerCase

            if supportedLc.contains(typ) then
                val offsets = e.charOffsets()
                val t1 = toks.find(_.getStartCharIndex == offsets.first)
                lazy val t2 = toks.find(_.getEndCharIndex == offsets.second)

                if t1.nonEmpty && t2.nonEmpty then
                    val props = mutable.ArrayBuffer.empty[(String, Any)]

                    val nne = e.coreMap().get(classOf[NormalizedNamedEntityTagAnnotation])
                    if nne != null then props += "nne" -> nne

                    // Key ignored because it can be category with higher level (`location` for type `country`).
                    val conf = e.entityTypeConfidences()
                    if conf != null && conf.size() == 1 then props += "confidence" -> conf.asScala.head._2

                    val entToks = toks.filter(
                        t => t.getStartCharIndex >= t1.get.getStartCharIndex && t.getEndCharIndex <= t2.get.getEndCharIndex
                    )

                    if entToks.nonEmpty then
                        res +=
                            new NCPropertyMapAdapter with NCEntity:
                                props.foreach { (k, v) => put(s"stanford:$typ:$k", v) }

                                override val getTokens: List[NCToken] = entToks
                                override val getRequestId: String = req.getRequestId
                                override val getType: String = s"stanford:$typ"

        res.toList