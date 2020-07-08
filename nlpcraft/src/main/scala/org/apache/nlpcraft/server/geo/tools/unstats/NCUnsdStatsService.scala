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

package org.apache.nlpcraft.server.geo.tools.unstats

import org.apache.nlpcraft.common.U

import scala.collection._

/**
 * Subcontinents based on The United Nations Statistics Division data.
 * http://unstats.un.org/unsd/methods/m49/m49regin.htm
 *
 * Source file copied from this page and marked manually.
 */
object NCUnsdStatsService {
    // Mapping between continents and subcontinents.
    private val CONTINENTS = Map(
        "002" → Seq("014", "017", "015", "018", "011"),
        "019" → Seq("029", "013", "005", "021"),
        "142" → Seq("143", "030", "034", "035", "145"),
        "150" → Seq("151", "154", "039", "155"),
        "009" → Seq("053", "054", "057", "061")
    )

    // (United States Minor Outlying Islands,UM,UMI)
    // (Heard Island and McDonald Islands,HM,HMD)
    // (Bouvet Island,BV,BVT)
    // (Antarctica,AQ,ATA)
    // (French Southern Territories,TF,ATF)
    // (South Georgia and the South Sandwich Islands,GS,SGS)
    // (Serbia and Montenegro,CS,SCG)
    // (Christmas Island,CX,CXR)
    // (British Indian Ocean Territory,IO,IOT)
    // (Cocos Islands,CC,CCK)

    // Countries represented in GEONAMES but not represented in The United Nations Statistics Division.
    // They skipped because their size or status.
    private val SKIPPED_COUNTRIES_ISO3 = Seq("UMI", "HMD", "BVT", "ATA", "ATF", "SGS", "SCG", "CXR", "IOT", "CCK")

    // (Netherlands Antilles,AN,ANT)
    // (Taiwan,TW,TWN)
    // (Kosovo,XK,XKX)

    // Countries represented in GEONAMES but not represented in The United Nations Statistics Division/
    // They defined manually (Country iso codes mapped to subcontinents codes)
    private val SPEC_CASES = Map(
        // Caribbean
        "029" → Seq(NCUnsdStatsCountry("ANT", "Netherlands Antilles")),
        // Eastern Asia
        "030" -> Seq(NCUnsdStatsCountry("TWN", "Taiwan")),
        // Southern Europe
        "039" → Seq(NCUnsdStatsCountry("XKX", "Kosovo"))
    )

    private val dir = s"${U.mkPath("nlpcraft/src/main/scala")}/${U.toPath(this)}"
    private val subContsPath = s"$dir/subcontinents.txt"
    private val codesPath = s"$dir/codes.txt"

    private def read(path: String): Seq[String] =
        U.readPath(path, "UTF-8").map(_.trim).filter(_.nonEmpty).filter(_.head != '#').toSeq

    def skip(iso: String): Boolean = SKIPPED_COUNTRIES_ISO3.contains(iso)

    def mkContinents(): Seq[NCUnsdStatsContinent] = {
        val countries = read(codesPath).map(line ⇒ {
            val numCode = line.take(3)
            val isoCode = line.drop(line.length - 3)
            val name = line.slice(3, line.length - 6 + 3).trim

            numCode → NCUnsdStatsCountry(isoCode, name)
        }).toMap

        val subContsCodes = CONTINENTS.flatMap(_._2).toSeq
        val contsCodes = CONTINENTS.unzip._1.toSeq

        val buf = mutable.Buffer.empty[NCUnsdStatsContinent]

        // # -  Comment was added manually for for unknown 419 code Latin 'America and the Caribbean'
        read(subContsPath).foreach(line ⇒ {
            val numCode = line.take(3)
            val name = line.drop(3).trim

            if (contsCodes.contains(numCode))
                buf += NCUnsdStatsContinent(name)
            else if (subContsCodes.contains(numCode)) {
                val sc = NCUnsdStatsSubContinent(name)

                SPEC_CASES.get(numCode) match {
                    case Some(cntrs) ⇒ sc.countries ++= cntrs
                    case None ⇒ // No-op.
                }

                buf.last.subContinents += sc
            }
            else
                buf.last.subContinents.last.countries += countries(numCode)
        })

        buf
    }
}