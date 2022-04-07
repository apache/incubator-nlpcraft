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

package org.apache.nlpcraft.examples.order.cli

import com.sun.net.httpserver.*
import org.apache.nlpcraft.*
import org.apache.nlpcraft.NCResultType.*
import org.apache.nlpcraft.examples.order.PizzeriaModel

import java.io.*
import java.net.InetSocketAddress
import scala.util.Using

/**
  *
  */
object PizzeriaModelServer:
    private val host = "localhost"
    private val port = 8087
    private val path = "ask"

    val URI = s"http://$host:$port/$path"

    def main(args: Array[String]): Unit = Using.resource(new NCModelClient(new PizzeriaModel)) { nlpClient =>
        val srv = HttpServer.create(new InetSocketAddress(host, port), 0)

        srv.createContext(
            s"/$path",
            (e: HttpExchange) =>
                def response(txt: String): Unit =
                    Using.resource(new BufferedOutputStream(e.getResponseBody)) { out =>
                        val arr = txt.getBytes
                        e.sendResponseHeaders(200, arr.length)
                        out.write(arr, 0, arr.length)
                    }

                try
                    val req =
                        e.getRequestMethod match
                            case "GET" => e.getRequestURI.toString.split("\\?").last
                            case "POST" =>
                                Using.resource(new BufferedReader(new InputStreamReader(e.getRequestBody))) { r =>
                                    r.readLine
                                }
                            case _ => throw new Exception(s"Unsupported request method: ${e.getRequestMethod}")

                    if req == null || req.isEmpty then Exception(s"Empty request")

                    val resp = nlpClient.ask(req, null, "userId")
                    val prompt = if resp.getType == ASK_DIALOG then "(Your should answer on the model's question below)\n" else ""

                    response(s"$prompt${resp.getBody}")
                catch
                    case e: NCRejection => response(s"Request rejected: ${e.getMessage}")
                    case e: Throwable =>
                        System.err.println("Unexpected error.")
                        e.printStackTrace()
                        response(s"Unexpected error: ${e.getMessage}")
            )

        srv.start()

        var applStarted = true

        Runtime.getRuntime.addShutdownHook(
            new Thread("shutdownHook"):
                override def run(): Unit =
                    applStarted = false
                    srv.stop(0)
                    srv.synchronized { srv.notifyAll() }
                    println("Server stopped.")
        )

        println(s"Server started: $URI")

        while (applStarted)
            srv.synchronized {
                try srv.wait()
                catch
                    case _: InterruptedException => // No-op.
            }
    }
