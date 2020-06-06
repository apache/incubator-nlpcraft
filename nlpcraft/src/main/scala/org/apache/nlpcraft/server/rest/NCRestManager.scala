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

package org.apache.nlpcraft.server.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import io.opencensus.trace.Span
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.{NCService, _}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

/**
  * REST manager.
  */
object NCRestManager extends NCService {
    private implicit val SYSTEM: ActorSystem = ActorSystem("server-rest")
    private implicit val MATERIALIZER: ActorMaterializer = ActorMaterializer()
    private implicit val CTX: ExecutionContextExecutor = SYSTEM.dispatcher

    @volatile private var bindFut: Future[Http.ServerBinding] = _

    @volatile private implicit var handleErrors: ExceptionHandler = _
    @volatile private implicit var handleRejections: RejectionHandler = _

    private final object Config extends NCConfigurable {
        final private val pre = "nlpcraft.server.rest"

        def host: String = getString(s"$pre.host")
        def port: Int = getInt(s"$pre.port")
        def apiImpl: String = getString(s"$pre.apiImpl")
    
        /**
          *
          */
        def check(): Unit = {
            if (!(port > 0 && port < 65535))
                abortWith(s"Configuration property port '$pre.port' must be > 0 and < 65535: $port")
            if (host == null)
                abortWith(s"Configuration property port '$pre.host' must be specified.")
            if (apiImpl == null)
                abortWith(s"Configuration property port '$pre.apiImpl' must be specified (use 'org.apache.nlpcraft.server.rest.NCBasicRestApi' as default).")
        }
    }

    Config.check()

    /**
      * Starts this component.
      */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span ⇒
        val url = s"${Config.host}:${Config.port}"
        val api: NCRestApi = U.mkObject(Config.apiImpl)
        
        addTags(span,
            "url" → url,
            "api" → Config.apiImpl
        )

        handleErrors = api.getExceptionHandler
        handleRejections = api.getRejectionHandler

        bindFut = Http().bindAndHandleAsync(Route.asyncHandler(api.getRoute), Config.host, Config.port)

        bindFut.onComplete {
            case Success(_) ⇒ logger.info(s"REST server is listening on '$url'.")
            case Failure(_) ⇒ logger.info(s"REST server failed to start on '$url'.")
        }

        super.start()
    }

    /**
      * Stops this component.
      */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        if (bindFut != null)
            bindFut.flatMap(_.unbind()).onComplete(_ ⇒ SYSTEM.terminate())

        super.stop()
    }
}
