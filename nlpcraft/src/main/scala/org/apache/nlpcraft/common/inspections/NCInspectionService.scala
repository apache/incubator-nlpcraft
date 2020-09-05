package org.apache.nlpcraft.common.inspections

import java.util.concurrent.{ExecutorService, Executors}

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.util.NCUtils

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

/**
 * Base trait for inspection implementation.
 */
trait NCInspectionService extends NCService {
    /** */
    @volatile private var pool: ExecutorService = _

    /** */
    @volatile private var executor: ExecutionContextExecutor = _

    /**
     *
     * @param mdlId
     * @param inspId
     * @param args
     * @param parent
     * @return
     */
    def inspect(mdlId: String, inspId: String, args: Option[String], parent: Span = null): Future[NCInspectionResult]

    /**
     *
     * @return
     */
    def getExecutor = executor

    override def start(parent: Span): NCService =
        startScopedSpan("start", parent) { _ ⇒
            pool = Executors.newCachedThreadPool()
            executor = ExecutionContext.fromExecutor(pool)

            super.start(parent)
        }

    override def stop(parent: Span): Unit =
        startScopedSpan("stop", parent) { _ ⇒
            super.stop(parent)

            NCUtils.shutdownPools(pool)

            pool = null
            executor = null
        }
}
