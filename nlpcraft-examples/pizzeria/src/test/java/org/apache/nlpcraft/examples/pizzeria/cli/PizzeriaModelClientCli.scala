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

package org.apache.nlpcraft.examples.pizzeria.cli

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.NCResultType.*

import java.io.*
import java.net.URI
import java.net.http.*
import java.net.http.HttpRequest.*
import java.net.http.HttpResponse.*
import scala.util.Using

object PizzeriaModelClientCli extends LazyLogging :
    private val client = HttpClient.newHttpClient()

    private def ask(req: String): String =
        try
            val resp: HttpResponse[String] = client.send(
                HttpRequest.
                    newBuilder().
                    uri(new URI(PizzeriaModelServer.URI)).
                    headers("Content-Type", "text/plain;charset=UTF-8").
                    POST(BodyPublishers.ofString(req)).
                    build(),
                BodyHandlers.ofString()
            )
            if resp.statusCode() != 200 then throw new IOException(s"Unexpected response type: ${resp.statusCode()}")
            resp.body
        catch
            case e: IOException => throw e
            case e: Throwable => throw new IOException(e)

    def main(args: Array[String]): Unit =
        println("Application started.")

        // Clears possible saved sessions.tea
        ask("stop")

        var applStarted = true

        Runtime.getRuntime.addShutdownHook(
            new Thread("shutdownHook"):
                override def run(): Unit = applStarted = false
        )

        while (true)
            print(s">>> ")

            try
                var in = scala.io.StdIn.readLine()

                if in != null then
                    in = in.trim
                    if in.nonEmpty then println(ask(in))
                println
            catch
                case e: NCRejection => println(s"Request rejected: ${e.getMessage}")
                case e: IOException => if applStarted then println(s"IO error: ${e.getMessage}")
                case e: Throwable =>
                    if applStarted then
                        e.printStackTrace()
                        println("Application exit.")
                        System.exit(-1)
