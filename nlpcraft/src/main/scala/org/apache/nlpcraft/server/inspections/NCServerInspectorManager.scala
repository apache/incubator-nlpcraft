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

package org.apache.nlpcraft.server.inspections

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.inspections.NCInspection
import org.apache.nlpcraft.common.inspections.NCInspectionType._
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.server.inspections.inspectors._
import org.apache.nlpcraft.server.probe.NCProbeManager

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
  * TODO: check all texts
  */
object NCServerInspectorManager extends NCService {
    private final val INSPECTORS =
        Map(
            SUGGEST_SYNONYMS → NCInspectorSynonymsSuggestions
        )

    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ ⇒
        INSPECTORS.values.foreach(_.start())

        super.start(parent)
    }

    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()

        INSPECTORS.values.foreach(_.stop())
    }

    /**
      *
      * @param mdlId
      * @param types
      * @param parent
      */
    @throws[NCE]
    def inspect(mdlId: String, types: Seq[NCInspectionType], parent: Span = null): Future[Map[NCInspectionType, NCInspection]] =
        startScopedSpan("enhance", parent, "modelId" → mdlId, "types" → types.map(_.toString)) { _ ⇒
            val promise = Promise[Map[NCInspectionType, NCInspection]]()

            NCProbeManager.inspect(mdlId, types, parent).onComplete {
                case Success(probeRes) ⇒
                    val srvRes =
                        probeRes.map { case (typ, inspectionProbe) ⇒
                            val inspectionSrv = INSPECTORS.get(typ) match {
                                case Some(inspector) ⇒ inspector.inspect(mdlId, Some(inspectionProbe))
                                case None ⇒ NCInspection()
                            }

                            def union[T](seq1: java.util.List[T], seq2: java.util.List[T]): Option[Seq[T]] = {
                                val seq = seq1.asScala ++ seq2.asScala

                                if (seq.isEmpty) None else Some(seq)
                            }

                            typ → NCInspection(
                                errors = union(inspectionProbe.errors, inspectionSrv.errors),
                                warnings = union(inspectionProbe.warnings, inspectionSrv.warnings),
                                suggestions = union(inspectionProbe.suggestions, inspectionSrv.suggestions),
                                // Don't need pass this data on last step.
                                data = None
                            )
                        }

                    promise.success(srvRes)
                case Failure(err) ⇒ throw err
            }(global)

            promise.future
        }
}
