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

package org.apache.nlpcraft.examples.solarsystem.loaders

import org.apache.nlpcraft.examples.solarsystem.api.SolarSystemOpenApiService
import org.apache.nlpcraft.model.{NCElement, NCValue, NCValueLoader}

import java.util
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.{SeqHasAsJava, SetHasAsJava}

class SolarSystemDiscoversValueLoader extends NCValueLoader {
    // Example: "Scott S. Sheppard, David C. Jewitt"
    private def mkValue(discInfo: String): NCValue = {
        val syns = ArrayBuffer.empty[String]

        for (d <- discInfo.split(",").map(_.trim).filter(_.nonEmpty)) {
            val lc = d.toLowerCase

            syns += lc

            val lastNameIdx = lc.lastIndexOf(" ")

            // Tries to detect last name.
            if (lastNameIdx > 0)
                syns += lc.substring(lastNameIdx + 1)
        }

        new NCValue {
            override def getName: String = discInfo
            override def getSynonyms: util.List[String] = syns.asJava
        }
    }

    override def load(owner: NCElement): util.Set[NCValue] =
        SolarSystemOpenApiService.getInstance().getAllDiscovers.map(mkValue).toSet.asJava
}
