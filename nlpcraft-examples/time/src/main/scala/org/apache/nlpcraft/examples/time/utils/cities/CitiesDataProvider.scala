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
package org.apache.nlpcraft.examples.time.utils.cities

import org.apache.nlpcraft.NCException

import java.io.*
import java.util.Objects
import scala.util.Using

case class City(name: String, country: String)
case class CityData(timezone: String, latitude: Double, longitude: Double)

object CitiesDataProvider:
    def get: Map[City, CityData] =
        def convert(arr: Array[String]): (City, CityData) =
            City(arr(0), arr(1)) -> CityData(arr(2), arr(3).toDouble, arr(4).toDouble)

        try
            scala.io.Source.fromResource("cities_timezones.txt", Thread.currentThread().getContextClassLoader).getLines().
                map(_.strip).filter(p => p.nonEmpty && !p.startsWith("#")).map(p => convert(p.split("\t"))).toMap
        catch
            case e: IOException => throw new NCException("Failed to read data file.", e)

