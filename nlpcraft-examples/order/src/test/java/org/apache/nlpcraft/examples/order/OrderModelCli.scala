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

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.examples.order.OrderModelCli.logger
import org.apache.nlpcraft.*
import org.apache.nlpcraft.NCResultType.*

import scala.util.Using

object OrderModelCli extends App with LazyLogging :
    main()

    private def main(): Unit = Using.resource(new NCModelClient(new OrderModel)) { client =>
        logger.info("Application started.")

        var resp: NCResult = null

        while (true)
            val prompt =
                if resp == null || resp.getType == ASK_RESULT then "Ask your question to the model: "
                else "Your should answer on the model's question: "
            logger.info(s">>>>>>>>>> $prompt")

            try
                resp = client.ask(scala.io.StdIn.readLine(), null, "userId")

                logger.info(s">>>>>>>>>> Response type: ${resp.getType}")
                logger.info(s">>>>>>>>>> Response body:\n${resp.getBody}")
            catch
                case e: NCRejection => logger.info(s"Request rejected: ${e.getMessage}")
                case e: Throwable => logger.error(s"Unexpected error: ${e.getMessage}")
                    logger.error("Application exit.")
                    System.exit(-1)
    }
