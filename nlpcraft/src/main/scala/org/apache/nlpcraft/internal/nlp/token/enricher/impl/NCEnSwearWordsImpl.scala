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

package org.apache.nlpcraft.internal.nlp.token.enricher.impl

import com.typesafe.scalalogging.LazyLogging
import opennlp.tools.stemmer.PorterStemmer
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.nlp.token.parser.opennlp.impl.NCEnOpenNlpImpl
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.internal.util.NCUtils.getStream

import java.io.*

/**
  *
  */
object NCEnSwearWordsImpl:
    /**
      *
      * @param mdlFile
      * @return
      */
    def apply(mdlFile: File): NCEnSwearWordsImpl = new NCEnSwearWordsImpl(
        new BufferedInputStream(new FileInputStream(mdlFile)), mdlFile.getPath
    )

    /**
      *
      * @param mdlSrc
      * @return
      */
    def apply(mdlSrc: String): NCEnSwearWordsImpl = new NCEnSwearWordsImpl(
        NCUtils.getStream(mdlSrc), mdlSrc
    )

/**
  *
  */
class NCEnSwearWordsImpl(is: InputStream, res: String) extends NCTokenEnricher with LazyLogging:
    @volatile private var swearWords: Set[String] = _

    override def start(): Unit =
        val stemmer = new PorterStemmer
        swearWords = NCUtils.readTextStream(is, "UTF-8").map(stemmer.stem).toSet
        logger.trace(s"Loaded resource: $res")
    override def stop(): Unit = swearWords = null
    override def enrich(req: NCRequest, cfg: NCModelConfig, toks: java.util.List[NCToken]): Unit =
        toks.forEach(t => t.put("swear:en", swearWords.contains(t.getStem)))

