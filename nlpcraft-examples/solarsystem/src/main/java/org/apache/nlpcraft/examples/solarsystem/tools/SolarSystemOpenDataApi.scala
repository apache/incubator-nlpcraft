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

package org.apache.nlpcraft.examples.solarsystem.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.nlpcraft.examples.solarsystem.tools.SolarSystemOpenDataApi.BodiesBean

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.http.HttpClient.Version

object SolarSystemOpenDataApi {
    case class BodiesBean(bodies: Seq[Map[String, Object]])

    private var s: SolarSystemOpenDataApi = _

    def getInstance(): SolarSystemOpenDataApi = {
        this.synchronized {
            if (s == null) {
                s = new SolarSystemOpenDataApi

                s.start()
            }

            s
        }
    }
}

class SolarSystemOpenDataApi {
    private final val URL_BODIES = "https://api.le-systeme-solaire.net/rest/bodies"
    private final val MAPPER = new ObjectMapper().registerModule(DefaultScalaModule)

    private var client: HttpClient = _
    private var planets: Seq[String] = _
    private var discovers: Seq[String] = _

    private def getBody(params: String*): Seq[Map[String, Object]] = {
        val req = HttpRequest.newBuilder(URI.create(s"$URL_BODIES?data=${params.mkString(",")}")).
            header("Content-Type", "application/json").
            GET().
            build()

        val respJs = client.sendAsync(req, HttpResponse.BodyHandlers.ofString()).get().body()

        MAPPER.readValue(respJs, classOf[BodiesBean]).bodies
    }

    def start(): Unit = {
        client = HttpClient.newBuilder.version(Version.HTTP_2).build

        val res = getBody("englishName,discoveredBy")

        def extract(name: String): Seq[String] =
            res.map(_(name).asInstanceOf[String]).map(_.strip()).filter(_.nonEmpty).distinct

        planets = extract("englishName")
        discovers = extract("discoveredBy")
    }

    def stop(): Unit = {
        planets = null
        discovers = null

        client = null
    }

    def getAllPlanets: Seq[String] = planets
    def getAllDiscovers: Seq[String] = discovers
}
