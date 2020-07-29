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

package org.apache.nlpcraft.server.geo.tools

import java.io.File

import com.fasterxml.jackson.core.`type`.TypeReference
import net.liftweb.json._
import org.apache.nlpcraft.common.nlp.dict.NCDictionaryManager
import org.apache.nlpcraft.common.{NCE, U}
import org.apache.nlpcraft.server.geo._

import scala.collection._

/**
  * Generator of additional synonyms for geo names.
  */
object NCGeoSyntheticNamesGenerator extends App {

    // Base synonym should be saved for console debug message.
    case class Holder(base: String, var entries: Set[NCGeoEntry])

    private def process(outFile: String) {
        val file = new File(outFile)

        if (file.exists() && !file.delete())
            throw new NCE(s"Couldn't delete file: $file")

        NCDictionaryManager.start()
        NCGeoManager.start(false)

        val hs = mutable.Map.empty[String, Holder]

        println(s"Synonyms count: ${NCGeoManager.getModel.synonyms.size}")

        for ((synonym, entries) ← NCGeoManager.getModel.synonyms) {
            val strs2Process = mutable.Set.empty[String] + synonym

            def add(s: String, base: String) =
                if (!NCGeoManager.getModel.synonyms.contains(s) && !hs.contains(s)) {
                    strs2Process += s

                    hs.get(s) match {
                        case Some(syn) ⇒ syn.entries ++= entries
                        case None ⇒ hs += s → Holder(base, entries)
                    }
                }

            def generateDash(str: String) {
                def generate(a: String, b: String): Unit =
                    if (str.contains(a))
                        add(str.replaceAll(a, b), str)

                generate(" ", "-")
                generate("-", " ")
            }

            def generateSaints(str: String) {
                def generate(str: String, beginStr: String, replacements: String*): Unit =
                    if (str.startsWith(beginStr))
                        replacements.foreach(r ⇒ add(str.replaceFirst(beginStr, r), str))

                generate(str, "st. ", "saint ", "saint-", "st.", "st-", "st ")
                generate(str, "saint ", "saint-", "st. ", "st.", "st-", "st ")

                if (str.length > 3 && str(3) != ' ' && str(3) != '-')
                    generate(str, "st.", "saint ", "saint-", "st. ", "st-", "st ")
            }

            while (strs2Process.nonEmpty) {
                val str = strs2Process.last

                strs2Process.remove(str)

                generateDash(str)
                generateSaints(str)
            }
        }

        NCGeoManager.stop()
        NCDictionaryManager.stop()

        if (hs.nonEmpty) {
            printResults(hs)
            writeJson(hs, outFile)
        }
        else
            println("All synthetic names already generated. Nothing to add.")
    }

    private def writeJson(buf: Map[String, Holder], outFile: String) {
        val syns = mutable.Map.empty[NCGeoEntry, NCGeoSynonym]

        buf.foreach(p ⇒ {
            val s: String = p._1
            val es: Set[NCGeoEntry] = p._2.entries

            for (e ← es) {
                syns.get(e) match {
                    case Some(syn) ⇒ syn.synonyms :+= s
                    case None ⇒
                        val synonym = e match {
                            case e: NCGeoMetro ⇒
                                NCGeoSynonym(None, None, None, None, None, Some(e.name), List(s))

                            case e: NCGeoContinent ⇒
                                NCGeoSynonym(None, None, None, None, Some(e.name), None, List(s))

                            case e: NCGeoSubContinent ⇒
                                NCGeoSynonym(None, None, None, Some(e.name), Some(e.continent.name), None, List(s))

                            // Short representation (without subcontinent and continent.)
                            case e: NCGeoCountry ⇒
                                NCGeoSynonym(None, None, Some(e.name), None, None, None, List(s))

                            // Short representation (without subcontinent and continent.)
                            case e: NCGeoRegion ⇒
                                NCGeoSynonym(None, Some(e.name), Some(e.country.name), None, None, None, List(s))

                            // Short representation (without subcontinent and continent.)
                            case e: NCGeoCity ⇒
                                NCGeoSynonym(
                                    Some(e.name), Some(e.region.name), Some(e.region.country.name), None, None, None, List(s)
                                )

                            case _ ⇒ throw new AssertionError(s"Unexpected object: $e")
                        }

                        syns += e → synonym
                }
            }
        })

        // Required for Lift JSON processing.
        implicit val formats: DefaultFormats.type = net.liftweb.json.DefaultFormats

        val f = new File(outFile)

        val exists =
            if (f.exists())
                U.extractYamlFile(f, ignoreCase = false, new TypeReference[List[NCGeoSynonym]] {})
            else
                Seq.empty[NCGeoSynonym]

        U.getYamlMapper.writeValue(new File(outFile), (syns.values ++ exists).toSet)
    }

    private def printResults(buf: Map[String, Holder]) {
        val map = mutable.Map.empty[String, Seq[String]]

        buf.map(p ⇒ {
            val baseSyn = p._2.base
            val newSyn = p._1

            map.get(baseSyn) match {
                case Some(seq) ⇒ map += baseSyn → (seq :+ newSyn)
                case None ⇒ map += baseSyn → Seq(newSyn)
            }
        })

        map.toSeq.sortBy(_._1).foreach(p ⇒ {
            val s = p._2.map(p ⇒ s"'$p'").mkString(", ")

            println(s"Synonyms added: $s for base: '${p._1}'.")
        })

        println(s"Synonyms count: ${buf.size}.")
    }

    process(
        U.mkPath(s"nlpcraft/src/main/resources/geo/synonyms/synthetic.yaml")
    )
}
