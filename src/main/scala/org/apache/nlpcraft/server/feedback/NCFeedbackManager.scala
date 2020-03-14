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

package org.apache.nlpcraft.server.feedback

import io.opencensus.trace.Span
import org.apache.ignite.IgniteAtomicSequence
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.server.ignite.NCIgniteInstance
import org.apache.nlpcraft.server.mdo.NCFeedbackMdo
import org.apache.nlpcraft.server.sql.{NCSql, NCSqlManager}

import scala.util.control.Exception._

/**
  * Feedback manager.
  */
object NCFeedbackManager extends NCService with NCIgniteInstance {
    @volatile private var seq: IgniteAtomicSequence = _

    override def stop(parent: Span): Unit = startScopedSpan("start", parent) { _ ⇒
        super.stop()
    }
    
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        catching(wrapIE) {
            seq = NCSql.mkSeq(ignite, "feedbackSeq", "feedback", "id")
        }

        super.start()
    }

    /**
      *
      * @param srvReqId Server request ID.
      * @param score Feedback score.
      * @param comment Feedback comment.
      * @param parent Parent tracing span.
      */
    def addFeedback(srvReqId: String, usrId: Long, score: Double, comment: Option[String], parent: Span): Long =
        startScopedSpan(
            "addFeedback", parent,
            "srvReqId" → srvReqId,
            "userId" → usrId) { span ⇒
            NCSql.sql {
                NCSqlManager.addFeedback(seq.incrementAndGet(), srvReqId, usrId, score, comment, span)
            }
        }

    /**
      *
      * @param id Feedback record ID.
      * @param parent Parent tracing span.
      */
    def deleteFeedback(id: Long, parent: Span): Unit =
        startScopedSpan(
            "deleteFeedback", parent,
            "usrId" → id
        ) { span ⇒
            NCSql.sql {
                NCSqlManager.deleteFeedback(id, span)
            }
        }

    /**
      *
      * @param companyId Company ID.
      * @param parent Parent tracing span.
      */
    def deleteAllFeedback(companyId: Long, parent: Span): Unit =
        startScopedSpan(
            "deleteAllFeedback",
            parent,
            "companyId" → companyId
        ) { span ⇒
            NCSql.sql {
                NCSqlManager.deleteAllFeedback(companyId, span)
            }
        }

    /**
      *
      * @param companyId Company ID.
      * @param srvReqId Server request ID.
      * @param usrId User ID.
      * @param parent Parent tracing span.
      * @return
      */
    def getFeedback(
        companyId: Long,
        srvReqId: Option[String],
        usrId: Option[Long],
        parent: Span
    ): Seq[NCFeedbackMdo] =
        startScopedSpan(
            "getFeedback",
            parent,
            "companyId" → companyId,
            "srvReqId" → srvReqId.orNull,
            "userId" → usrId.getOrElse(() ⇒ null)
        ) { span ⇒
            NCSql.sql {
                NCSqlManager.getFeedback(companyId, srvReqId, usrId, span)
            }
        }

    /**
      *
      * @param id Feedback record ID.
      * @param parent Parent tracing span.
      * @return
      */
    def getFeedback(id: Long, parent: Span): Option[NCFeedbackMdo] =
        startScopedSpan("getFeedback", parent) { span ⇒
            NCSql.sql {
                NCSqlManager.getFeedback(id, span)
            }
        }
}
