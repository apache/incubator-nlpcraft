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
