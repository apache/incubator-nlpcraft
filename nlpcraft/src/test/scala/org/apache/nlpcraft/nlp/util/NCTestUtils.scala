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

package org.apache.nlpcraft.nlp.util

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.ascii.NCAsciiTable

import java.util
import java.util.List as JList
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOptional

/**
  *
  */
object NCTestUtils:
    /**
      *
      * @param m
      * @return
      */
    private def mkProps(m: NCPropertyMap): String =
        m.keysSet().asScala.toSeq.sorted.map(p => s"$p=${m.get[Any](p)}").mkString("{", ", ", "}")

    /**
      * @param toks
      */
    def printTokens(toks: Seq[NCToken]): Unit =
        val tbl = NCAsciiTable("Text", "Index", "Stopword", "Start", "End", "Properties")

        for (t <- toks)
            tbl += (
                t.getText,
                t.getIndex,
                t.getOpt[Boolean]("stopword").toScala match
                    case Some(b) => b.toString
                    case None => "undef."
                ,
                t.getStartCharIndex,
                t.getEndCharIndex,
                mkProps(t)
            )

        tbl.print(s"Request: ${toks.map(_.getText).mkString(" ")}")

    /**
      *
      * @param req
      * @param ents
      */
    def printEntities(req: String, ents: Seq[NCEntity]): Unit =
        val tbl = NCAsciiTable("EntityId", "Tokens", "Tokens Position", "Properties")

        for (e <- ents)
            val toks = e.getTokens.asScala
            tbl += (
                e.getId,
                toks.map(_.getText).mkString("|"),
                toks.map(p => s"${p.getStartCharIndex}-${p.getEndCharIndex}").mkString("|"),
                mkProps(e)
            )

        tbl.print(s"Request: $req")