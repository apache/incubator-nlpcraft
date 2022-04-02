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

package org.apache.nlpcraft.examples.order

import com.sun.net.httpserver.*
import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.NCResultType.*

import java.io.*
import scala.util.Using
import com.sun.net.httpserver.HttpServer

import java.net.InetSocketAddress
import scala.language.postfixOps


object OrderModelRest extends App with LazyLogging:
    private val HOST = "localhost"
    private val PORT = 8087

    main()

    private def main(): Unit = Using.resource(new NCModelClient(new OrderModel)) { client =>
        val srv = HttpServer.create(new InetSocketAddress(HOST, PORT), 0)

        srv.createContext(
            "/ask",
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

                    val resp = client.ask(req, null, "userId")
                    val prompt =
                        if resp.getType == ASK_RESULT then "Ask your question to the model: "
                        else "Your should answer on the model's question: "

                    response(s"$prompt ${resp.getBody}")
                catch
                    case e: NCRejection => response(s"Request rejected: ${e.getMessage}")
                    case e: Throwable =>
                        logger.error("Unexpected error.", e)
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
                    logger.info("Server stopped.")
        )

        logger.info(s"Server started: http://$HOST:$PORT/ask")

        while (applStarted)
            srv.synchronized {
                try srv.wait()
                catch
                    case _: InterruptedException => // No-op.
            }
    }
