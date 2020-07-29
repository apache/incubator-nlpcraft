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

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.nlpcraft.common.U

/**
 * Generator for US state names.
 */
object NCGeoStateNamesGenerator extends App {
    // Produce a map of regions (countryCode + regCode -> region name)).
    private def getStates(txtFile: String): Map[String, String] =
        U.readPath(txtFile, "UTF8").filter(!_.startsWith("#")).flatMap(line ⇒ {
            val seq = line.split("\t").toSeq

            if (seq(7) == "ADM1" && seq(8) == "US") {
                val name = seq(2)
                val code = seq(10)

                Some(name → code)
            }
            else
                None
        }).toMap

    // Folder with files downloaded from GEO names server.
    private val GEO_NAMES_DIR = U.homeFileName("geoNames")

    // File with nicknames downloaded from GEO names.
    private val allCntrs = s"$GEO_NAMES_DIR/allCountries.txt"

    // Output directory.
    private val outDir = U.mkPath(s"nlpcraft/src/main/resources/geo")
    private val out = s"$outDir/synonyms/states.yaml"

    // JSON extractor for synonyms.
    case class Synonym(
      region: String,
      country: String = "United States",
      synonyms: Seq[String])

    // Go over regions and create them.
    val syns = getStates(allCntrs).map(s ⇒ {
        val name = s._1
        val code = s._2

        val seq = Seq(
            code,
            s"state of $name",
            s"$name state",
            s"$code state",
            s"state of $code")

        Synonym(region = name, synonyms = seq)
    }).toSeq.sortBy(_.region)

    val mapper = new ObjectMapper(new YAMLFactory)

    mapper.registerModule(new DefaultScalaModule())
    mapper.setSerializationInclusion(Include.NON_NULL)
    mapper.setSerializationInclusion(Include.NON_EMPTY)

    mapper.writeValue(new File(out), syns)

    println(s"Files generated OK: $out")
}
