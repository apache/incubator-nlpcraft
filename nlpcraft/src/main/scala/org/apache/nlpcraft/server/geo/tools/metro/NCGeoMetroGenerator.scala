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

package org.apache.nlpcraft.server.geo.tools.metro

import java.io.{File, PrintStream}

import net.liftweb.json.Extraction._
import net.liftweb.json._
import org.apache.nlpcraft.common.U
import org.apache.nlpcraft.server.geo.NCGeoSynonym
import resource._

/**
 * Generator of ga:metro values and synthetic synonyms.
 */
object NCGeoMetroGenerator extends App {
    private val in = s"${U.mkPath("src/main")}/${U.toPath(this)}/ga_metro.txt"
    private val out_vals =
        U.mkPath(s"src/main/resources/geo/metro.json")
    private val out_syns =
        U.mkPath(s"src/main/resources/geo/synonyms/metro.json")

    private val SYNTH_PART = Seq(
        "Designated Market Area",
        "Market Area",
        "DMA",
        "metro",
        "ga:metro"
    )

    private def deleteBrackets(s: String): String =
        s.replaceAll("\\(", " ").replaceAll("\\)", " ").split(" ").map(_.trim).filter(_.nonEmpty).mkString(" ")

    private def generate() {
        val lines = U.readPath(in, "UTF-8").toSeq.map(_.trim).filter(_.nonEmpty)

        case class Holder(name: String)

        // Skips header.
        val metro = lines.tail.filter(!_.contains("(not set)")).map(line ⇒ Holder(line.takeWhile(_ != ',')))

        // Required for Lift JSON processing.
        implicit val formats: DefaultFormats.type = net.liftweb.json.DefaultFormats

        managed(new PrintStream(new File(out_vals))) acquireAndGet { ps ⇒
            ps.println(prettyRender(decompose(metro)))
        }

        println(s"File created: $out_vals")

        val sync = metro.map(p ⇒ {
            val metro = p.name
            val normMetro = deleteBrackets(metro)

            def mkSeq(s: String): Seq[String] = SYNTH_PART.map(p ⇒ s"$p $s") ++ SYNTH_PART.map(p ⇒ s"$s $p")
            
            val last3Symbols = normMetro.drop(normMetro.length - 3)

            val addSeq =
                if (last3Symbols.head == ' ' && last3Symbols.drop(1).forall(ch ⇒ ch.isLetter && ch.isUpper))
                    mkSeq(normMetro.take(normMetro.length - 3))
                else
                    Seq.empty

            NCGeoSynonym(None, None, None, None, None, Some(metro), (mkSeq(normMetro) ++ addSeq).toList)
        })

        managed(new PrintStream(new File(out_syns))) acquireAndGet { ps ⇒
            ps.println(prettyRender(decompose(sync)))
        }

        println(s"Synonyms file created: $out_syns")
    }

    generate()
}
