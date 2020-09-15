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

package org.apache.nlpcraft.server.ctrl

import java.io.{EOFException, InterruptedIOException}
import java.net.{InetSocketAddress, ServerSocket, Socket}
import java.util.concurrent.atomic.AtomicBoolean

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.socket.NCSocket

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Local host control protocol (used by `nlpcraft.{sh|cmd}` script.
 */
object NCControlManager extends NCService {
    private final val PORT = 43011
    private final val LOCALHOST = "127.0.0.1"
    private final val SO_TIMEOUT = 5000

    @volatile private var srvThread: Thread = _
    @volatile private var isStopping: AtomicBoolean = _

    override def stop(parent: Span): Unit =
        startScopedSpan("start", parent) { _ ⇒
            isStopping = new AtomicBoolean(true)

            U.stopThread(srvThread)

            super.stop()
        }

    override def start(parent: Span = null): NCService =
        startScopedSpan("start", parent, "endpoint" → s"$LOCALHOST:$PORT") { _ ⇒
            isStopping = new AtomicBoolean(false)

            srvThread = startServer()

            srvThread.start()

            this
        }

    /**
     *
     * @param sock
     */
    private def processSocket(sock: NCSocket): Unit = {

    }

    /**
     *
     */
    private def startServer(): Thread = {
        new Thread(s"ctrl-mgr") {
            private final val thName = getName
            private var srv: ServerSocket = _
            @volatile private var stopped = false

            override def isInterrupted: Boolean =
                super.isInterrupted || stopped

            override def interrupt(): Unit = {
                super.interrupt()

                U.close(srv)

                stopped = true
            }

            override def run(): Unit = {
                try {
                    body()
                }
                catch {
                    case _: InterruptedException ⇒ logger.trace(s"Thread interrupted: $thName")
                    case e: Throwable ⇒
                        U.prettyError(
                            logger,
                            s"Unexpected error during '$thName' thread execution:",
                            e
                        )
                }
                finally
                    stopped = true
            }

            private def body(): Unit =
                while (!isInterrupted)
                    try {
                        srv = new ServerSocket()

                        srv.bind(new InetSocketAddress(LOCALHOST, PORT))
                        srv.setSoTimeout(SO_TIMEOUT)

                        logger.info(s"Control server is listening on '$LOCALHOST:$PORT'")

                        while (!isInterrupted) {
                            var sock: Socket = null

                            try {
                                sock = srv.accept()

                                logger.trace(s"Control server accepted new connection from: ${sock.getRemoteSocketAddress}")
                            }
                            catch {
                                case _: InterruptedIOException ⇒ // No-op.

                                // Note that server socket must be closed and created again.
                                // So, error should be thrown.
                                case e: Exception ⇒
                                    U.close(sock)

                                    throw e
                            }

                            if (sock != null) {
                                val fut = Future {
                                    processSocket(NCSocket(sock))
                                }

                                fut.onComplete {
                                    case Success(_) ⇒ // No-op.

                                    case Failure(e: NCE) ⇒ logger.warn(e.getMessage, e)
                                    case Failure(_: EOFException) ⇒ () // Just ignoring.
                                    case Failure(e: Throwable) ⇒ logger.warn(s"Ignoring socket error: ${e.getLocalizedMessage}")
                                }
                            }
                        }
                    }
                    catch {
                        case e: Exception ⇒
                            if (!isStopping.get) {
                                // Release socket asap.
                                U.close(srv)

                                val ms = Config.reconnectTimeoutMs

                                // Server socket error must be logged.
                                logger.warn(s"$name server error, re-starting in ${ms / 1000} sec.", e)

                                U.sleep(ms)
                            }
                    }
                    finally {
                        U.close(srv)
                    }
        }
    }
}
