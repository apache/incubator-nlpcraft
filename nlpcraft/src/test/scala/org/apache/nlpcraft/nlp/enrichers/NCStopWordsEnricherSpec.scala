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

package org.apache.nlpcraft.nlp.enrichers

import org.apache.nlpcraft.*
import nlp.util.*
import nlp.enrichers.NCEnStopWordsTokenEnricher
import org.scalatest.funsuite.AnyFunSuite
import org.apache.nlpcraft.internal.util.NCUtils

import scala.collection.*
import scala.concurrent.ExecutionContext

/**
  *
  */
class NCStopWordsEnricherSpec extends AnyFunSuite:
    /**
      *
      * @param stopEnricher
      * @param txt
      * @param boolVals
      */
    private def add(stopEnricher: => NCEnStopWordsTokenEnricher, txt: String, boolVals: Boolean*)
        (using bodies: mutable.ArrayBuffer[() => Unit], errs: mutable.ArrayBuffer[Throwable]): Unit =
        val body: () => Unit = () =>
            try
                val toks = EN_TOK_PARSER.tokenize(txt)
                require(toks.size == boolVals.size)

                toks.foreach(tok => require(tok.get[Boolean]("stopword").isEmpty))

                val req = NCTestRequest(txt)

                EN_TOK_LEMMA_POS_ENRICHER.enrich(req, CFG, toks)
                stopEnricher.enrich(req, CFG, toks)

                NCTestUtils.printTokens(toks)
                toks.zip(boolVals).foreach { (tok, boolVal) => require(tok[Boolean]("stopword") == boolVal) }
            catch
                case e: Throwable => errs.synchronized { errs += e }

        bodies += body

    test("test") {
        val errs = mutable.ArrayBuffer.empty[Throwable]
        val bodies = mutable.ArrayBuffer.empty[() => Unit]

        given mutable.ArrayBuffer[Throwable] = errs
        given mutable.ArrayBuffer[() => Unit] = bodies

        add(
            EN_TOK_STOP_ENRICHER,
            "the test",
            true,
            false
        )
        add(
            new NCEnStopWordsTokenEnricher(addSet = Set("test"), exclSet = Set("the")),
            "the test",
            false,
            true
        )
        // The synonym is defined as lemma => all kind of input words should be found.
        add(
            new NCEnStopWordsTokenEnricher(addSet = Set("woman")),
            "woman women",
            true,
            true
        )
        // The synonym is defined in some form => only in the same form input words should be found.
        add(
            new NCEnStopWordsTokenEnricher(addSet = Set("women")),
            "woman women",
            false,
            true
        )
        // The synonym is defined in some form, but stemmer is very rough =>  all kind of input words should be found.
        add(
            new NCEnStopWordsTokenEnricher(addSet = Set("women"), stemmer = _.take(3)),
            "woman women",
            true,
            true
        )
        // The synonym is defined as lemma => all kind of input words should be found, but excluded set is defined.
        add(
            new NCEnStopWordsTokenEnricher(addSet = Set("woman"), exclSet = Set("women")),
            "woman women",
            true,
            false
        )
        // Very rough stemmers defined.
        add(
            new NCEnStopWordsTokenEnricher(addSet = Set("women"), stemmer = _.head.toString),
            "weather windows noun",
            true,
            true,
            false
        )
        add(
            new NCEnStopWordsTokenEnricher(stemmer = _ => ""),
            "weather noun",
            true,
            true
        )

        NCUtils.execPar(bodies)(ExecutionContext.Implicits.global)
        errs.foreach(_.printStackTrace)
        require(errs.isEmpty)
    }