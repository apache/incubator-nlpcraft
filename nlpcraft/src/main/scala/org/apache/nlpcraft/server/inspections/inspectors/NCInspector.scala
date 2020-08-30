package org.apache.nlpcraft.server.inspections.inspectors

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.inspections.NCInspectionResult

import scala.concurrent.Future

private[inspections] trait NCInspector {
    def inspect(mdlId: String, inspId: String, args: String, parent: Span = null): Future[NCInspectionResult]
}
