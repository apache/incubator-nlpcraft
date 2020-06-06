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

import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.process.{AbstractTokenizer, CoreLabelTokenFactory}
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager

import NCStanfordTokenizer._

/**
  * Custom tokenizer.
  */
case class NCStanfordTokenizer(sen: String) extends AbstractTokenizer[CoreLabel] {
    private val iter = NCNlpCoreManager.tokenize(sen).map(p ⇒ factory.makeToken(p.token, p.from, p.length)).toIterator

    override def getNext: CoreLabel = if (iter.hasNext) iter.next() else null
}

/**
  * Custom tokenizer helper.
  */
object NCStanfordTokenizer {
    private val factory = new CoreLabelTokenFactory
}
