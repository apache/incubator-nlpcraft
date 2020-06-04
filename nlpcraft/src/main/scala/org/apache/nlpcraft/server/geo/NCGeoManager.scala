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

package org.apache.nlpcraft.server.geo

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.nlp.dict.{NCDictionaryManager, NCDictionaryType}
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.server.json.NCJson

import scala.collection.{immutable, mutable}

/**
  * Geo manager.
  */
object NCGeoManager extends NCService {
    // Config files.
    private final val COUNTRY_DIR = "geo/countries"
    private final val CONT_PATH = "geo/continents.json"
    private final val METRO_PATH = "geo/metro.json"
    private final val SYNONYMS_DIR_PATH = "geo/synonyms"
    private final val CASE_SENSITIVE_DIR_PATH = s"$SYNONYMS_DIR_PATH/case_sensitive"
    
    // Special file, the data of which are not filtered by common dictionary words.
    private final val SYNONYMS_MANUAL_FILES = Seq("manual.json", "states.json")

    @volatile private var model: NCGeoModel = _
    
    // Auxiliary words for GEO names. Example: CA state, Los Angeles city.
    private final val CITY_AUX = Seq("city", "town", "metropolis")
    private final val REGION_AUX = Seq("region", "state", "area")

    case class JsonMetro(name: String)
    case class JsonCountry(name: String, iso: String)

    case class JsonCity(
        name: String,
        latitude: Double,
        longitude: Double,
        population: Long,
        elevation: Option[Int],
        dem: Int,
        timezone: String
    )
    case class JsonRegion(name: String, cities: List[JsonCity])
    case class JsonCountryHolder(
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
        regions: Seq[JsonRegion]
    )
    case class JsTopCity(name: String, region: String, country: String)

    /**
      * Starts manager.
      */
    @throws[NCE]
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        model = readAndConstructModel(true)
        
        super.start()
    }

    /**
      * Starts manager. Method is public for generator.
      */
    @throws[NCE]
    def start(extended: Boolean): NCService = {
        model = readAndConstructModel(extended)

        super.start()
    }
    
    /**
      * Stops this component.
      */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        model = null
        
        super.stop()
    }
    
    /**
      * Gets GEO model.
      *
      * @return Immutable GEO model.
      */
    def getModel: NCGeoModel = model

    /**
      *
      * @param m
      * @param name
      * @param v
      */
    private def add(m: mutable.HashMap[String, Any], name: String, v: Any): Unit =
        v match {
            case x: Option[Any] ⇒
                if (x.isDefined)
                    m += name → x.get
            case _ ⇒ m += name → v
        }

    /**
      * Reads and constructs GEO model.
      *
      * @param extended
      */
    @throws[NCE]
    private def readAndConstructModel(extended: Boolean): NCGeoModel = {
        val geoEntries = mutable.HashMap[String, mutable.HashSet[NCGeoEntry]]()
        
        val conts = mutable.HashSet.empty[NCGeoContinent]
        val subs = mutable.HashSet.empty[NCGeoSubContinent]
        val cntrs = mutable.HashSet.empty[NCGeoCountry]
        val regions = mutable.HashSet.empty[NCGeoRegion]
        val cities = mutable.HashSet.empty[NCGeoCity]
        val metro = mutable.HashSet.empty[NCGeoMetro]

        // Add location internal representation.
        def addEntry(key: String, geo: NCGeoEntry, lowerCase: Boolean) {
            val k = if (lowerCase) key.toLowerCase else key

            geoEntries.get(k) match {
                case Some(set) ⇒ set.add(geo)
                case None ⇒ geoEntries += k → mutable.HashSet[NCGeoEntry](geo)
            }
            
            geo match {
                case x: NCGeoContinent ⇒ conts += x
                case x: NCGeoSubContinent ⇒ subs += x
                case x: NCGeoCountry ⇒ cntrs += x
                case x: NCGeoRegion ⇒ regions += x
                case x: NCGeoCity ⇒ cities += x
                case x: NCGeoMetro ⇒ metro += x
                
                case _ ⇒ assert(assertion = false)
            }
        }

        // Subcontinent name -> continent.
        val subCont2ContMap = mutable.HashMap.empty[String, NCGeoContinent]
        
        // +====================+
        // | 1. Process metros. |
        // +====================+
        
        for (p ← NCJson.extractResource[List[JsonMetro]](METRO_PATH, ignoreCase = true))
            addEntry(p.name, NCGeoMetro(p.name), lowerCase = true)

        // +========================+
        // | 2. Process continents. |
        // +========================+
        val ctrsIsoToSubconts = mutable.HashMap.empty[String, NCGeoSubContinent]

        for ((contName, subMap) ← NCJson.extractResource[
            immutable.Map[
                /* Continent name */String,
                /* Map of sub-continents */immutable.Map[String, List[JsonCountry]]
                ]](CONT_PATH, ignoreCase = true)) {
            val gCont = NCGeoContinent(contName)

            addEntry(contName, gCont, lowerCase = true)

            for ((subName, cntrList) ← subMap) {
                val gSub = NCGeoSubContinent(subName, gCont)

                subCont2ContMap += subName → gCont

                addEntry(subName, gSub, lowerCase = true)

                for (cntr ← cntrList)
                    ctrsIsoToSubconts += cntr.iso → gSub
            }
        }

        // +=======================+
        // | 3. Process countries. |
        // +=======================+
        case class CityKey(
            city: String,
            region: String,
            country: String,
            subContinent: Option[String] = None,
            continent: Option[String] = None
        )

        val cntrMap = mutable.HashMap.empty[String, NCGeoCountry]
        val citiesMap = mutable.HashMap.empty[CityKey, NCGeoCity]
        
        for (f ← U.getFilesResources(COUNTRY_DIR)) {
            val countryJs = NCJson.extractResource[JsonCountryHolder](f, ignoreCase = true)

            val meta = mutable.HashMap.empty[String, Any]

            add(meta, "iso", countryJs.iso)
            add(meta, "iso3", countryJs.iso3)
            add(meta, "isoCode", countryJs.code)
            add(meta, "capital", countryJs.capital)
            add(meta, "area", countryJs.area)
            add(meta, "population", countryJs.population)
            add(meta, "continent", countryJs.continent)
            add(meta, "currencyCode", countryJs.currencyCode)
            add(meta, "currencyName", countryJs.currencyName)
            add(meta, "phone", countryJs.phone)
            add(meta, "postalCodeFormat", countryJs.postalCodeFormat)
            add(meta, "postalCodeRegex", countryJs.postalCodeRegex)
            add(meta, "languages", countryJs.languages)
            add(meta, "neighbours", countryJs.neighbours)

            val geoCountry = NCGeoCountry(countryJs.name, ctrsIsoToSubconts(countryJs.iso), meta.toMap)

            addEntry(geoCountry.name, geoCountry, lowerCase = true)

            cntrMap += geoCountry.name → geoCountry

            for (reg ← countryJs.regions) {
                val gReg = NCGeoRegion(reg.name, geoCountry)

                addEntry(reg.name, gReg, lowerCase = true)

                reg.cities.foreach(
                    jsCity ⇒ {
                        val meta = mutable.HashMap.empty[String, Any]

                        add(meta, "latitude", jsCity.latitude)
                        add(meta, "longitude", jsCity.longitude)
                        add(meta, "population", jsCity.population)
                        add(meta, "elevation", jsCity.elevation)
                        add(meta, "dem", jsCity.dem)
                        add(meta, "timezone", jsCity.timezone)

                        val geoCity = NCGeoCity(jsCity.name, gReg, meta.toMap)

                        addEntry(jsCity.name, geoCity, lowerCase = true)

                        citiesMap += CityKey(jsCity.name, gReg.name, geoCountry.name) → geoCity
                        citiesMap +=
                            CityKey(
                                jsCity.name,
                                gReg.name,
                                geoCountry.name,
                                Some(geoCountry.subContinent.name),
                                Some(geoCountry.subContinent.continent.name)
                            ) → geoCity
                    }
                )
            }
        }

        // +======================+
        // | 4. Process synonyms. |
        // +======================+
        
        val dicts = NCDictionaryManager.get(NCDictionaryType.WORD_COMMON)
        
        def readJss(dir: String): Seq[String] =
            if (U.hasResource(dir)) U.getFilesResources(dir).filter(_.endsWith(".json")) else Seq.empty
        
        def extract(res: String, ignoreCase: Boolean): Seq[NCGeoSynonym] =
            NCJson.extractResource[List[NCGeoSynonym]](res, ignoreCase)

        def getCachedCountry(cntr: String, sub: String, cont: String) = {
            val country = cntrMap.getOrElse(cntr, throw new NCE(s"Country not found: $cntr"))

            if (country.subContinent.name != sub)
                throw new NCE(s"Unexpected subcontinent [country=$country, subcontinent=$sub]")

            if (country.subContinent.continent.name != cont)
                throw new NCE(s"Unexpected continent [country=$country, continent=$cont]")
            country
        }

        def process(s: NCGeoSynonym, add: (Seq[String], NCGeoEntry) ⇒ Unit): Unit =
            s match {
                // Metro.
                case NCGeoSynonym(None, None, None, None, None, Some(m), syns) ⇒
                    add(syns, NCGeoMetro(m))
                
                // Continent.
                case NCGeoSynonym(None, None, None, None, Some(cont), None, syns) ⇒
                    add(syns, NCGeoContinent(cont))
                
                // Sub-continent (full representation).
                case NCGeoSynonym(None, None, None, Some(sub), Some(cont), None, syns) ⇒
                    add(syns, NCGeoSubContinent(sub, NCGeoContinent(cont)))

                // Sub-continent (short representation).
                case NCGeoSynonym(None, None, None, Some(sub), None, None, syns) ⇒
                    add(syns, NCGeoSubContinent(sub, subCont2ContMap(sub)))

                // Country (full representation).
                case NCGeoSynonym(None, None, Some(cntr), Some(sub), Some(cont), None, syns) ⇒
                    add(syns, getCachedCountry(cntr, sub, cont))

                // Country (short representation).
                case NCGeoSynonym(None, None, Some(cntr), None, None, None, syns) ⇒
                    add(syns, cntrMap(cntr))
                
                // Region (full representation).
                case NCGeoSynonym(None, Some(reg), Some(cntr), Some(sub), Some(cont), None, syns) ⇒
                    add(syns, NCGeoRegion(reg, getCachedCountry(cntr, sub, cont)))

                // Region (short representation).
                case NCGeoSynonym(None, Some(reg), Some(cntr), None, None, None, syns) ⇒
                    add(syns, NCGeoRegion(reg, cntrMap(cntr)))
                
                // City (full representation).
                case NCGeoSynonym(Some(city), Some(reg), Some(cntr), Some(sub), Some(cont), None, syns) ⇒
                    add(syns, citiesMap(CityKey(city, reg, cntr, Some(sub), Some(cont))))
                
                // City (short representation).
                case NCGeoSynonym(Some(city), Some(reg), Some(cntr), None, None, None, syns) ⇒
                    add(syns, citiesMap(CityKey(city, reg, cntr)))

                case _ ⇒ throw new AssertionError(s"Unexpected synonym: $s")
            }
        
        for (file ← readJss(SYNONYMS_DIR_PATH); synonym ← extract(file, ignoreCase = true))
            process(
                synonym,
                (syns: Seq[String], geoEntry: NCGeoEntry) ⇒
                    geoEntries.get(geoEntry.name.toLowerCase) match {
                        case Some(set) if set.contains(geoEntry) ⇒
                            // NCGeoSynonym shouldn't be matched with common dictionary word.
                            // Exception - manually defined synonyms.
                            syns.filter(s ⇒
                                SYNONYMS_MANUAL_FILES.exists(p ⇒ file.endsWith(s"/$p")) ||
                                    (!dicts.contains(s) &&
                                        !dicts.contains(s.replaceAll("the ", "")) &&
                                        !dicts.contains(s.replaceAll("the-", "")))
                            ).foreach(addEntry(_, geoEntry, lowerCase = true))
                        case _ ⇒ throw new NCE(s"Unknown synonym or its sub-component: $geoEntry")
                    }
            )

        // +=====================================+
        // | 5. Process case sensitive synonyms. |
        // +=====================================+
        
        for (file ← readJss(CASE_SENSITIVE_DIR_PATH); s ← extract(file, ignoreCase = false)) {
            def toLc(opt: Option[String]): Option[String] =
                opt match {
                    case Some(str) ⇒ Some(str.toLowerCase)
                    case None ⇒ None
                }
            
            process(
                NCGeoSynonym(
                    toLc(s.city),
                    toLc(s.region),
                    toLc(s.country),
                    toLc(s.subcontinent),
                    toLc(s.continent),
                    toLc(s.metro),
                    s.synonyms
                ),
                (syns: Seq[String], x: NCGeoEntry) ⇒
                    geoEntries.get(x.name) match {
                        // These synonyms are not checked with dictionaries etc.
                        // Case sensitive synonyms (like abbreviations) configuration used as is.
                        case Some(set) if set.contains(x) ⇒ syns.foreach(addEntry(_, x, lowerCase = false))
                        case _ ⇒ throw new NCE(s"Unknown synonym or its sub-component: $x")
                    }
            )
        }

        if (extended) {
            // Adds constructions like 'city LA' etc
            def addAux(geoSyn: String, geo: NCGeoEntry, auxes: Seq[String]): Unit =
                for (aux ← auxes if !geoSyn.split(" ").contains(aux)) {
                    addEntry(s"$aux $geoSyn", geo, lowerCase = true)
                    addEntry(s"$aux of $geoSyn", geo, lowerCase = true)
                    addEntry(s"$geoSyn $aux", geo, lowerCase = true)
                }

            geoEntries.flatMap(p ⇒ p._2.map(_ → p._1)).foreach(p ⇒
                p._1 match {
                    case _: NCGeoCity ⇒ addAux(p._2, p._1, CITY_AUX)
                    case _: NCGeoRegion ⇒ addAux(p._2, p._1, REGION_AUX)
                    case _ ⇒ // No-op.
                }
            )
        }

        /**
          * Loads top cities from JSON res.
          *
          * @param path JSON res path.
          */
        def mkTopCities(path: String): immutable.Set[NCTopGeoCity] =
            NCJson.extractResource[List[JsTopCity]](path, ignoreCase = true).map(city ⇒
                cntrs.find(_.name == city.country) match {
                    case Some(country) ⇒
                        regions.find(r ⇒ r.name == city.region && r.country == country) match {
                            case Some(region) ⇒ NCTopGeoCity(city.name, region)
                            case None ⇒ throw new AssertionError(s"Region is not found: ${city.region}")
                        }
                    case None ⇒ throw new AssertionError(s"Country is not found: ${city.country}")
                }
            ).toSet
        
        NCGeoModel(
            geoEntries.map(p ⇒ p._1 → p._2.toSet).toMap,
            conts.toSet,
            subs.toSet,
            cntrs.toSet,
            regions.toSet,
            cities.toSet,
            metro.toSet,
            mkTopCities("geo/world_top.json"),
            mkTopCities("geo/us_top.json")
        )
    }
}
