package org.apache.nlpcraft.common.inspections

import java.util.concurrent.{ExecutorService, Executors}

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.inspections.impl.NCInspectionResultImpl
import org.apache.nlpcraft.common.util.NCUtils
import org.apache.nlpcraft.model.NCModel
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager

import scala.collection.mutable
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
     * @param inspName
     * @param args
     * @param parent
     * @return
     */
    def inspect(mdlId: String, inspName: String, args: Option[String], parent: Span = null): Future[NCInspectionResult] =
        startScopedSpan(
            "inspect",
            parent,
            "modelId" → mdlId,
            "inspName" -> inspName) { _ ⇒
            Future {
                val now = System.currentTimeMillis()

                val errs = mutable.Buffer.empty[String]
                val warns = mutable.Buffer.empty[String]
                val suggs = mutable.Buffer.empty[String]

                NCModelManager.getModel(mdlId) match {
                    case Some(x) => bodyOnProbe(x.model, args, suggs, warns, errs)
                    case None => errs += s"Model not found: $mdlId"
                }

                NCInspectionResultImpl(
                    inspectionId = inspName,
                    modelId = mdlId,
                    durationMs = System.currentTimeMillis() - now,
                    timestamp = now,
                    warnings = warns,
                    suggestions = suggs,
                    errors = errs
                )
            }(getExecutor)
        }

    /**
     * Convenient adapter for the probe-side inspection implementation.
     *
     * @param mdl
     * @param args
     * @param suggs Mutable collector for suggestions.
     * @param warns Mutable collector for warnings.
     * @param errs Mutable collector for errors.
     */
    protected def bodyOnProbe(
        mdl: NCModel,
        args: Option[String],
        suggs: mutable.Buffer[String],
        warns: mutable.Buffer[String],
        errs: mutable.Buffer[String]
    ) = {}

    /**
     *
     * @return
     */
    def getExecutor = executor

    /**
     *
     * @return
     */
    def getName: String

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
