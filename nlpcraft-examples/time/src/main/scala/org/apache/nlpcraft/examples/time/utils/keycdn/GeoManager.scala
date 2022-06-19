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
package org.apache.nlpcraft.examples.time.utils.keycdn

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.apache.nlpcraft.NCRequest

import java.io.*
import java.net.*
import java.text.MessageFormat
import java.util.zip.GZIPInputStream
import scala.io.Source
import scala.util.Using

// Such field names required by 'keycdn.com' service response.
case class ResponseGeoData(country_name: String, city: String, latitude: Double, longitude: Double, timezone: String)
case class ResponseData(geo: ResponseGeoData)
case class Response(status: String, description: String, data: ResponseData)

object GeoManager:
    private val URL: String = "https://tools.keycdn.com/geo.json?host="
    private val GSON: Gson = new Gson

    private val cache = collection.mutable.HashMap.empty[String, ResponseGeoData]

    private var externalIp: Option[String] = None

    /**
      * Gets optional geo data by given sentence.
      *
      * @param sen Sentence.
      * @return Geo data. Optional. */
    def get(sen: NCRequest): Option[ResponseGeoData] =
        try
            externalIp match
                case Some(_) => // No-op.
                case None =>
                    try externalIp = Some(getExternalIp)
                    catch
                        case _: IOException => // No-op.

            externalIp match
                case Some(ip) =>
                    cache.get(ip) match
                        case Some(geo) => Some(geo)
                        case None =>
                            val conn = new URL(URL + ip).openConnection.asInstanceOf[HttpURLConnection]

                            // This service requires "User-Agent" property with its own format.
                            conn.setRequestProperty("User-Agent", "keycdn-tools:https://nlpcraft.apache.org")

                            val enc = conn.getContentEncoding

                            Using.resource(conn.getInputStream) { in =>
                                val stream = if enc != null && enc == "gzip" then new GZIPInputStream(in) else in

                                val resp = GSON.fromJson(new BufferedReader(new InputStreamReader(stream)), classOf[Response])

                                if resp.status != "success" then
                                    throw new IOException(MessageFormat.format("Unexpected response [status={0}, description={1}]", resp.status, resp.description))

                                cache.put(ip, resp.data.geo)

                                Some(resp.data.geo)
                            }
                case None =>
                    System.err.println("External IP cannot be detected for localhost.")
                    None
        catch
            case e: Exception =>
                System.err.println(MessageFormat.format("Unable to answer due to IP location finder (keycdn) error for host: {0}", externalIp))
                e.printStackTrace(System.err)
                None

    /**
      * Gets external IP.
      *
      * @return External IP.
      * @throws IOException If any errors occur. */
    private def getExternalIp: String =
        Using.resource(Source.fromURL(new URL("https://checkip.amazonaws.com"))) { src =>
            src.getLines().toList.head
        }

    /**
      * Gets Silicon Valley location. Used as default value for each example service.
      * This default location definition added here just for accumulating all GEO manipulation logic in one class.
      *
      * @return Silicon Valley location. */
    def getSiliconValley: ResponseGeoData =
        ResponseGeoData(
            country_name = "United States", city = "", latitude = 37.7749, longitude = 122.4194, timezone = "America/Los_Angeles"
        )
