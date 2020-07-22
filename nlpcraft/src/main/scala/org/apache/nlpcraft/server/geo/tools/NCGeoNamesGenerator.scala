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

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.nlpcraft.common.{NCE, U}
import org.apache.nlpcraft.server.geo.tools.unstats.{NCUnsdStatsContinent, NCUnsdStatsService}

import scala.collection._

/**
 * Geo data creator based on GeoNames project (http://download.geonames.org/export/dump),
 * mixed with the United Nations Statistics Division project data.
 *
 * Note, that GeoNames data should download on local PC and is not added into control version repository.
 */
object NCGeoNamesGenerator extends App {
    // There are no continents and subcontinents.
    object LocationType extends Enumeration {
        type LocationType = Value
        val CITY, REGION, COUNTRY = Value
    }

    import LocationType._

    case class Location(locationType: LocationType, name: String, parentName: String)
    case class City(
        name: String,
        latitude: Double,
        longitude: Double,
        population: Long,
        elevation: Option[Int],
        dem: Int,
        timezone: String
    )
    case class Region(name: String, cities: Seq[City])
    case class Country(
        name: String,
        iso: String,
        iso3: String,
        code: String,
        capital: Option[String],
        area: Option[Double],
        population: Option[Long],
        continent: String,
        currencyCode: Option[String],
        currencyName: Option[String],
        phone: Option[String],
        postalCodeFormat: Option[String],
        postalCodeRegex: Option[String],
        languages: Option[String],
        neighbours: Option[String],
        var regions: Seq[Region] = Seq.empty
    )
    case class CityInfo(name: String, countryRegion: String, countryCode: String, population: Long)

    // GEO name ID -> internal representation mapping.
    private val ids = mutable.Map.empty[String, Location]

    private def read(path: String): Seq[String] = U.readPath(path, "UTF8").filter(!_.startsWith("#"))

    // Process country and continent information.
    private def processCountries(unsdContinents: Seq[NCUnsdStatsContinent]): Set[Country] = {
        case class SubContinent(continent: String, subContinent: String)

        def getSubContinent(countryIso: String): SubContinent = {
            val hs: Seq[SubContinent] = unsdContinents.flatMap(c ⇒
                c.subContinents.find(_.countries.exists(_.iso3 == countryIso)) match {
                    case Some(sc) ⇒ Some(SubContinent(c.name, sc.name))
                    case None ⇒ None
                }
            )

            require(hs.lengthCompare(1) == 0)

            hs.head
        }

        // File format is defined with fixed tabulations.
        // Country - String(sunContinent name)
        val map: Map[Country, String] = read(pathCountryInfo).flatMap(line ⇒ {
            val seq = line.split("\t").toSeq

            def getStringOpt(idx: Int): Option[String] =
                try {
                    val s = seq(idx).trim

                    if (s.nonEmpty) Some(s) else None
                }
                catch {
                    case e: ArrayIndexOutOfBoundsException ⇒
                        // 16 is last mandatory field index (geonameid).
                        if (idx != 17)
                            throw new NCE(s"Error [line=$line, length=${seq.length}, idx=$idx]", e)

                        Some("")
                }

            def getString(idx: Int): String =
                getStringOpt(idx) match {
                    case Some(s) ⇒ s
                    case None ⇒ throw new NCE(s"Empty value [$line=$line, idx=$idx]")
                }

            def getLongOpt(idx: Int): Option[Long] =
                getStringOpt(idx) match {
                    case Some(s) ⇒ Some(s.toLong)
                    case None ⇒ None
                }
            def getDoubleOpt(idx: Int): Option[Double] =
                getStringOpt(idx) match {
                    case Some(s) ⇒ Some(s.toDouble)
                    case None ⇒ None
                }

            val iso = getString(0)
            val iso3 = getString(1)
            val code = getString(2)
            val name = normalize(getString(4))
            val capital = getStringOpt(5)
            val area = getDoubleOpt(6)
            val population = getLongOpt(7)
            val continent = getString(8)
            val currencyCode = getStringOpt(10)
            val currencyName = getStringOpt(11)
            val phone = getStringOpt(12)
            val postalCodeFormat = getStringOpt(13)
            val postalCodeRegex = getStringOpt(14)
            val languages = getStringOpt(15)
            val geoId = getString(16)
            val neighbours = getStringOpt(17)

            if (!NCUnsdStatsService.skip(iso3)) {
                val sch: SubContinent = getSubContinent(iso3)

                ids.put(geoId, Location(COUNTRY, name, sch.subContinent))

                val country =
                    Country(
                        name,
                        iso,
                        iso3,
                        code,
                        capital,
                        area,
                        population,
                        continent,
                        currencyCode,
                        currencyName,
                        phone,
                        postalCodeFormat,
                        postalCodeRegex,
                        languages,
                        neighbours
                    )

                Some(country → sch.subContinent)
            }
            else
                None

        }).toMap

        map.keySet
    }

    // Produce a map of regions (countryCode + regCode -> region name)).
    private def processRegions(): mutable.Map[String, String] = {
        val map = mutable.Map.empty[String, String]

        read(pathAllCountries).foreach(line ⇒ {
            val seq = line.split("\t").toSeq

            if (seq(7) == "ADM1") {
                val id = seq.head
                val name = normalize(seq(2))
                val code = seq(8)
                val regNr = seq(10)

                ids.put(id, Location(REGION, name, code))

                map.put(code + '_' + regNr, name)
            }
        })

        map
    }

    // Process cities. Produce map of CountryCode + RegionNr -> list of cities.
    // Some cities do not have Region information. Some countries does not have
    // region breakdown.
    private def processCities(regCodes: mutable.Map[String, String], isoToNames: Map[String, String]):
    (Map[String, Seq[City]], Set[CityInfo], Set[CityInfo]) = {
        val map = mutable.Map.empty[String, mutable.Buffer[City]] ++
            regCodes.keys.map(key ⇒ key → mutable.Buffer.empty[City])

        val worldTop = mutable.Set.empty[CityInfo]

        val usTop = mutable.Set.empty[CityInfo]

        read(pathCities5000).zipWithIndex.foreach { case (line, lineNum) ⇒
            val seq = line.split("\t").toSeq

            def getStringOpt(idx: Int): Option[String] = {
                val s = seq(idx).trim

                if (s.nonEmpty) Some(s) else None
            }

            def getString(idx: Int): String =
                getStringOpt(idx) match {
                    case Some(s) ⇒ s
                    case None ⇒ throw new NCE(s"Empty value [$line=$line, idx=$idx, line: $lineNum]")
                }

            def getIntOpt(idx: Int): Option[Int] =
                getStringOpt(idx) match {
                    case Some(s) ⇒ Some(s.toInt)
                    case None ⇒ None
                }
            def getLong(idx: Int): Long = getString(idx).toLong
            def getInt(idx: Int): Int = getString(idx).toInt
            def getDouble(idx: Int): Double = getString(idx).toDouble

            val id = getString(0)
            // Exception with Ak"yar and alike.
            val name = normalize(getStringOpt(2).getOrElse(getString(1)))
            val latitude = getDouble(4)
            val longitude = getDouble(5)
            val cntrIso = getString(8)
            val regNr = getStringOpt(10).getOrElse("")
            val population = getLong(14)
            val elevation = getIntOpt(15)
            val dem = getInt(16)
            val timezone = getString(17)

            // Enforce preconditions.
            require(U.neon(name) && U.neon(id) && U.neon(cntrIso))

            var cntrReg = s"${cntrIso}_$regNr"

            // Check region.
            if (!regCodes.contains(cntrReg)) {
                println(s"City $name but unknown combination of country and region codes: $cntrReg.")
                println("\twill put to default region.")

                cntrReg = s"${cntrIso}_"

                // Create region named after country.
                isoToNames.get(cntrIso) match {
                    case Some(cntr) ⇒ regCodes.put(cntrReg, cntr)
                    case None ⇒ // No-op.
                }
            }

            if (cntrIso.toLowerCase == "us" && population > 100000)
                usTop.add(CityInfo(name, cntrReg, cntrIso, population))
            else if (cntrIso.toLowerCase != "us" && population > 1000000)
                worldTop.add(CityInfo(name, cntrReg, cntrIso, population))

            ids.put(id, Location(CITY, name, cntrReg))

            map.getOrElseUpdate(cntrReg, mutable.Buffer.empty[City]) +=
                City(name, latitude, longitude, population, elevation, dem, timezone)
        }

        (map, worldTop, usTop)
    }

    // Go over all countries and prepare internal representation.
    private def combine(
        countries: Set[Country],
        isoToNames: Map[String, String],
        regCodes: Map[String, String],
        regs: Map[String, Seq[City]]
    ): Set[Country] = {
        val isoToCountries = countries.map(p ⇒ p.iso → p).toMap

        val m = mutable.Map.empty[String, Country]
        val ctrsToRegions = mutable.Map.empty[Country, mutable.ArrayBuffer[Region]]

        def addRegion(country: Country, region: Region): Unit =
            ctrsToRegions.get(country) match {
                case Some(seq) ⇒ seq += region
                case None ⇒
                    val seq = mutable.ArrayBuffer.empty[Region]

                    seq += region

                    ctrsToRegions += country → seq
            }

        // Map of CountryCode + RegionNr -> list of cities.
        for ((countryIsoStr, regCities) ← regs) {
            val countryIso = countryIsoStr.substring(0, 2)

            regCodes.get(countryIsoStr) match {
                case Some(regName) ⇒
                    m.get(countryIso) match {
                        case Some(country) ⇒ addRegion(country, Region(regName, regCities))
                        case None ⇒
                            isoToCountries.get(countryIso) match {
                                case Some(country) ⇒ addRegion(country,Region(regName, regCities))
                                case None ⇒ // No-op.
                            }
                    }

                case None ⇒ // No-op.
            }
        }

        isoToNames.foreach(p ⇒ {
            val iso = p._1

            // Just for compatibility.
            if (!m.contains(iso))
                m += iso → isoToCountries(iso)
        })

        val set = m.values.toSet

        set.foreach(country ⇒ country.regions = ctrsToRegions.getOrElse(country, Seq.empty))

        set
    }

    // Go over all countries and serialize to files.
    private def writeCountries(mapper: ObjectMapper, countries: Set[Country]) {
        val dirPath = s"$outDir/countries"

        val dir = new File(dirPath)

        if (dir.exists() && dir.isFile)
            throw new NCE(s"Invalid folder $dirPath")
        else if (!dir.exists()) {
            if (!dir.mkdir())
                throw new NCE(s"Couldn't create folder $dirPath")
        }

        for (country ← countries)
            mapper.writeValue(new File(s"$outDir/countries/${country.iso}.yaml"), country)
    }

    // Process synonyms for countries, regions and cities.
    // There are not synonyms for continents and subcontinents.
    private def processSynonyms() = {
        val map = mutable.Map.empty[Location, mutable.Set[String]]

        read(pathAlternateNames).foreach(line ⇒ {
            val seq = line.split("\t").toSeq

            val origId = seq(1).trim
            val lang = seq(2).trim
            val name = normalize(seq(3))

            // Skips too short synonyms (one letter names)
            if (lang == "en" || lang.trim.isEmpty && name.length > 1) {
                // Find out the target of this synonym.
                ids.get(origId) match {
                    case Some(x) ⇒
                        // Some names contain 'No.' which causes lemmatizer to fail.
                        if (name != x.name && isAscii(name) && !name.contains("http") && !name.contains("No.")) {
                            val seq = map.getOrElseUpdate(x, mutable.Set.empty[String])

                            seq += name
                        }
                    case None ⇒ // No-op
                }
            }
        })

        map.toMap
    }

    def normalize(str: String): String = {
        str.
            trim.
            replaceAll("\\(\\p{ASCII}*\\)", "").
            replaceAll("–", "-").
            replaceAll("- +", "-").
            replaceAll(" +-", "-").
            replaceAll(" +/ +", "/").
            replaceAll("//", "").
            replaceAll("'", "").
            replaceAll(" +", " ").
            trim
    }

    def isAscii(str: String) = str.matches("\\A\\p{ASCII}*\\z")

    // Go over all synonyms and serialize to file.
    private def writeSynonyms(
        mapper: ObjectMapper,
        synonyms: Map[Location, Set[String]],
        cntrCodes: Map[String, String], // Country code → Country name.
        regsCodes: Map[String, String]): Unit = {
        case class Holder(country: String, region: Option[String], city: Option[String], synonyms: List[String])

        val hs = synonyms.flatMap(s ⇒ {
            val loc = s._1
            val locSyns = s._2.toList

            loc.locationType match {
                case COUNTRY ⇒ Some(Holder(loc.name, None, None, locSyns))
                case REGION ⇒
                    cntrCodes.get(loc.parentName) match {
                        case Some(cntrCode) ⇒ Some(Holder(cntrCode, Some(loc.name), None, locSyns))
                        case None ⇒ None
                    }
                case CITY ⇒
                    val cntrReg = loc.parentName
                    val cntrCode = cntrReg.substring(0, 2)

                    cntrCodes.get(cntrCode) match {
                        case Some(country) ⇒ Some(Holder(country, Some(regsCodes(cntrReg)), Some(loc.name), locSyns))
                        case None ⇒ None
                    }
            }
        }).toSeq.sortBy(p ⇒ (p.region.isDefined, p.city.isDefined, p.country, p.region, p.city))

        mapper.writeValue(new File(outSynonyms), hs)
    }

    private def writeTopCities(
        mapper: ObjectMapper,
        countries: Map[String, Map[String, Region]],
        isoToNames: Map[String, String],
        regCodes: Map[String, String],
        worldTop: Set[CityInfo],
        usTop: Set[CityInfo]) {

        def write(cities: Set[CityInfo], file: String, qty: Int): Unit = {
            val topCities = cities.map(p ⇒ {
                val cityName = p.name.toLowerCase

                // Checks consistent state.
                // Data were collected from various files.
                val regs: Map[String, Region] = countries(p.countryCode)
                val regName: String = regCodes(p.countryRegion)
                val reg: Region = regs(regName)

                require(reg.cities.exists(_.name.toLowerCase == cityName))

                CityInfo(cityName, p.countryRegion, p.countryCode, p.population)
            }).toSeq.sortBy(-_.population).take(qty)

            case class Holder(name: String, country: String, region: String)

            val sorder =
                topCities.map(
                    c ⇒ Holder(
                        c.name,
                        isoToNames(c.countryCode).toLowerCase,
                        regCodes.getOrElse(c.countryRegion, "").toLowerCase
                    )
                ).sortBy(p ⇒ (p.country, p.name))

            mapper.writeValue(new File(file), sorder)
        }

        write(worldTop, outWorldTop, 200)
        write(usTop, outUsTop, 300)
    }

    private def writeContinents(
        mapper: ObjectMapper,
        continents: Seq[NCUnsdStatsContinent],
        isoToNames: Map[String, String],
        iso3ToIso: Map[String, String]
    ): Unit = {
        case class Continent(name: String, iso3: String, iso: String)

        val hs = continents.map(p ⇒
            p.name → p.subContinents.map(p ⇒
                p.name → p.countries.map(p ⇒ {
                    val iso = iso3ToIso(p.iso3)

                    Continent(isoToNames(iso), p.iso3, iso)
                })
            ).toMap
        ).toMap

        mapper.writeValue(new File(outContinents), hs)
    }

    private def generate() {
        val mapper = U.getYamlMapper

        val continents = NCUnsdStatsService.mkContinents()
        val countries = processCountries(continents)

        val isoToNames = countries.map(p ⇒ p.iso → p.name).toMap
        val iso3ToIso = countries.map(p ⇒ p.iso3 → p.iso).toMap

        // Go over regions and create them.
        val regCodes = processRegions()

        // Go over major cities and create index.
        val (cities, worldTop, usTop) = processCities(regCodes, isoToNames)

        // Combine all information.
        val countriesFixes = combine(countries, isoToNames, regCodes, cities)

        // Go over alternative names and create synonyms.
        val syns = processSynonyms()

        writeContinents(mapper, continents, isoToNames, iso3ToIso)
        writeCountries(mapper, countriesFixes)
        writeSynonyms(mapper, syns, isoToNames, regCodes)
        writeTopCities(
            mapper,
            countriesFixes.map(h ⇒ h.iso → h.regions.map(p ⇒ p.name → p).toMap).toMap,
            isoToNames,
            regCodes,
            worldTop,
            usTop
        )

        println(s"Files generated OK in: $outDir.")
    }

    // Input files.
    private val GEO_NAMES_DIR = U.homeFileName("geoNames")
    // User's home.
    private val pathCountryInfo = s"$GEO_NAMES_DIR/countryInfo.txt"
    private val pathCities5000 = s"$GEO_NAMES_DIR/cities5000.txt"
    private val pathAllCountries = s"$GEO_NAMES_DIR/allCountries.txt"
    private val pathAlternateNames = s"$GEO_NAMES_DIR/alternateNames.txt"

    // Output files.
    private val outDir = U.mkPath(s"nlpcraft/src/main/resources/geo")
    private val outContinents = s"$outDir/continents.yaml"
    private val outWorldTop = s"$outDir/world_top.yaml"
    private val outUsTop = s"$outDir/us_top.yaml"
    private val outSynonyms = s"$outDir/synonyms/geonames.yaml"

    generate()
}