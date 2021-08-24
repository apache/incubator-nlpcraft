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

package org.apache.nlpcraft.examples.solarsystem.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.examples.solarsystem.api.SolarSystemOpenApiService.BodiesBean

import java.net.http.HttpClient.Version
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.{URI, URLEncoder}

object SolarSystemOpenApiService {
    case class BodiesBean(bodies: Seq[Map[String, Object]])

    private var s: SolarSystemOpenApiService = _

    def getInstance(): SolarSystemOpenApiService =
        this.synchronized {
            if (s == null) {
                s = new SolarSystemOpenApiService

                s.start()
            }

            s
        }
}

class SolarSystemOpenApiService extends LazyLogging {
    private final val URL_BODIES = "https://api.le-systeme-solaire.net/rest/bodies"
    private final val MAPPER = new ObjectMapper().registerModule(DefaultScalaModule)

    private var client: HttpClient = _

    private var planets: Map[String, String] = _
    private var discovers: Seq[String] = _

    // Simplified implementation of 'https://api.le-systeme-solaire.net/rest/bodies/' request.
    // Only single filter can be used.
    def bodyRequest(): SolarSystemOpenApiBodyRequest = new SolarSystemOpenApiBodyRequest() {
        case class Filter(data: String, oper: String, value: String) {
            require(data != null)
            require(oper != null)
            require(value != null)
        }

        private var params: Seq[String] = _
        private var filter: Filter = _

        override def withParameters(params: String*): SolarSystemOpenApiBodyRequest = {
            this.params = params

            this
        }

        override def withFilter(data: String, oper: String, value: String): SolarSystemOpenApiBodyRequest = {
            this.filter = Filter(data, oper, value)

            this
        }

        override def execute(): Seq[Map[String, Object]] = {
            var url = URL_BODIES

            def getSeparator: String = if (url == URL_BODIES) "?" else "&"

            if (params != null)
                url = s"$url${getSeparator}data=${params.mkString(",")}"

            if (filter != null)
                url = s"$url${getSeparator}filter=${filter.data},${filter.oper},${URLEncoder.encode(filter.value, "UTF-8")}"

            logger.info(s"Request prepared: $url")

            val respJs = client.sendAsync(
                HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json").GET().build(),
                HttpResponse.BodyHandlers.ofString()
            ).get().body()

            logger.info(s"Response received: $respJs")

            MAPPER.readValue(respJs, classOf[BodiesBean]).bodies
        }
    }

    def start(): Unit = {
        client = HttpClient.newBuilder.version(Version.HTTP_2).build

        val res = bodyRequest().withParameters("id", "englishName", "discoveredBy").execute()

        def str(m: Map[String, Object], name: String): String = m(name).asInstanceOf[String].strip

        planets =
            res.map(row => str(row, "id") -> str(row, "englishName")).
                filter(p => p._1.nonEmpty && p._2.nonEmpty).toMap

        discovers = res.map(row => str(row, "discoveredBy")).distinct

        logger.info(
            s"Solar System Open Api Service started. " +
            s"Initial data discovered [planets=${planets.size}, discovers=${discovers.size}]"
        )
    }

    def stop(): Unit = {
        planets = null
        discovers = null

        client = null

        logger.info(s"Solar System Open Api Service stopped.")
    }

    def getAllPlanets: Map[String, String] = planets
    def getAllDiscovers: Seq[String] = discovers
}
