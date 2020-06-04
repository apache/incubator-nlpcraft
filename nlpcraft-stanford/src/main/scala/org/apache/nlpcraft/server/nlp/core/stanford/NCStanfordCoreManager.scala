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

package org.apache.nlpcraft.server.nlp.core.stanford

import java.util.Properties

import edu.stanford.nlp.pipeline.{CoreDocument, StanfordCoreNLP}
import io.opencensus.trace.Span
import org.apache.ignite.IgniteCache
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.server.ignite.NCIgniteHelpers._
import org.apache.nlpcraft.server.ignite.NCIgniteInstance

import scala.util.control.Exception.catching

/**
  * Stanford core manager.
  */
object NCStanfordCoreManager extends NCService with NCIgniteInstance {
    @volatile private var stanford: StanfordCoreNLP = _
    @volatile private var cache: IgniteCache[String, CoreDocument] = _

    /**
      * Starts this component.
      */
    override def start(parent: Span = null): NCService = {
        val p = new Properties()

        p.setProperty("customAnnotatorClass.nctokenize", classOf[NCStanfordAnnotator].getName)
        p.setProperty("annotators", "nctokenize, ssplit, pos, lemma, ner")

        // Created with hardcoded properties just for minimize configuration issues.
        stanford = new StanfordCoreNLP(p)

        catching(wrapIE) {
            cache = ignite.cache[String, CoreDocument]("stanford-cache")
        }

        super.start()
    }

    override def stop(parent: Span = null): Unit = {
        cache = null

        super.stop()
    }

    /**
      *
      * @param txt
      * @return
      */
    def annotate(txt: String): CoreDocument =
        catching(wrapIE) {
            cache(txt) match {
                case Some(doc) ⇒ doc
                case None ⇒
                    val doc = new CoreDocument(txt)

                    stanford.annotate(doc)

                    cache += txt → doc

                    doc
            }
        }
}
