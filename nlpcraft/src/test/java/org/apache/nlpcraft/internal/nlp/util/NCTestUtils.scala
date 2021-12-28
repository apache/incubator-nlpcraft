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

package org.apache.nlpcraft.internal.nlp.util

import org.apache.nlpcraft.internal.ascii.NCAsciiTable
import org.apache.nlpcraft.*

/**
  *
  */
object NCTestUtils:
    /**
      *
      * @param toks
      * @param props
      */
    def printTokens(toks: Seq[NCToken], props: String*): Unit =
        val tbl = new NCAsciiTable()

        if props.isEmpty
            then tbl #= ("Text", "POS", "Stem", "Lemma", "Start", "End", "Length", "Stopword")
            else tbl #= ("Text", "POS", "Stem", "Lemma", "Start", "End", "Length", "Stopword", "Properties")

        toks.foreach(t =>
            if props.isEmpty then
                tbl += (
                    t.getText,
                    t.getPos,
                    t.getStem,
                    t.getLemma,
                    t.getStartCharIndex,
                    t.getEndCharIndex,
                    t.getLength,
                    t.isStopWord
                )
            else
                tbl += (
                    t.getText,
                    t.getPos,
                    t.getStem,
                    t.getLemma,
                    t.getStartCharIndex,
                    t.getEndCharIndex,
                    t.getLength,
                    t.isStopWord,
                    props.map(p => s"$p=${t.get[Any](p)}").mkString("{", ", ", "}")
                )
        )

        println(s"Request: ${toks.map(_.getText).mkString(" ")}")
        println(tbl.toString)

    /**
      *
      * @param make
      * @tparam T
      * @return
      */
    def makeAndStart[T <: NCLifecycle](make: => T): T =
        def now() = System.currentTimeMillis()

        val start = now()
        val t = make
        val started = now()
        
        t.start(null) // TODO: fix it.
        println(s"'${t.getClass.getSimpleName}' created with time=${started - start} ms and started=${now() - started} ms.")
        t
