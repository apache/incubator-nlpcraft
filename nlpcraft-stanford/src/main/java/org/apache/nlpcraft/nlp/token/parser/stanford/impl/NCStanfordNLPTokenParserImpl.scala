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

package org.apache.nlpcraft.nlp.token.parser.stanford.impl

import edu.stanford.nlp.ling.*
import edu.stanford.nlp.ling.CoreAnnotations.*
import edu.stanford.nlp.pipeline.*
import edu.stanford.nlp.util.*
import org.apache.nlpcraft.*

import java.io.StringReader
import java.util
import java.util.stream.Collectors
import java.util.{Properties, List as JList}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/**
  *
  * @param stanford
  */
class NCStanfordNLPTokenParserImpl(stanford: StanfordCoreNLP) extends NCTokenParser:
    require(stanford != null)

    override def tokenize(text: String): JList[NCToken] =
        val doc = new CoreDocument(text)
        stanford.annotate(doc)
        val ann = doc.annotation().get(classOf[SentencesAnnotation])
        if ann == null then E("Sentence annotation not found.")

        val toks = ann.asScala.flatMap(_.asInstanceOf[ArrayCoreMap].get(classOf[TokensAnnotation]).asScala).
            zipWithIndex.map { (t, idx) =>
                new NCPropertyMapAdapter with NCToken :
                    override val getText: String = t.originalText()
                    override val getLemma: String = t.lemma()
                    override val getPos: String = t.tag()
                    override val getIndex: Int = idx
                    override val getStartCharIndex: Int = t.beginPosition()
                    override val getEndCharIndex: Int = t.endPosition()
            }.toSeq

        toks.asJava