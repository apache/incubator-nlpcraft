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

package org.apache.nlpcraft.model.opencensus.stats

import java.util
import java.util.Collections

import io.opencensus.stats.Aggregation.{Count, Distribution}
import io.opencensus.stats.Measure.{MeasureDouble, MeasureLong}
import io.opencensus.stats.View.Name
import io.opencensus.stats._

/**
  * OpenCensus stats instrumentation.
  */
trait NCOpenCensusModelStats {
    val M_SYS_LATENCY_MS: MeasureLong =
        MeasureLong.create("sys_latency", "The latency of system tasks", "ms")
    val M_USER_LATENCY_MS: MeasureLong =
        MeasureLong.create("user_latency", "The latency of user tasks", "ms")
    
    /**
      * Records OpenCensus metrics.
      *
      * @param pairs Pairs of OC measure and its value. Values must be `Long` or `Double` only.
      */
    def recordStats(pairs: (Measure, AnyVal)*): Unit = {
        val map = Stats.getStatsRecorder.newMeasureMap()
        
        for ((m, v) ← pairs) {
            m match {
                case d: MeasureDouble ⇒ map.put(d, v.asInstanceOf[Double])
                case l: MeasureLong ⇒ map.put(l, v.asInstanceOf[Long])
                case _ ⇒ throw new AssertionError()
            }
        }
        
        map.record()
    }
    
    init()
    
    /**
      *
      */
    private def init(): Unit = {
        val restLatDist = Distribution.create(BucketBoundaries.create(util.Arrays.asList(
            0.0, 25.0, 100.0, 200.0, 400.0, 800.0, 10000.0
        )))
        
        def mkViews(m: Measure, name: String): List[View] = {
            List(
                View.create(
                    Name.create(s"$name/latdist"),
                    s"The distribution of the '$name' latency",
                    m,
                    restLatDist,
                    Collections.emptyList()
                ),
                View.create(
                    Name.create(s"$name/count"),
                    s"The number of the '$name' invocations",
                    m,
                    Count.create,
                    Collections.emptyList()
                )
            )
        }
        
        val views = List(
            mkViews(M_SYS_LATENCY_MS, "sys_task"),
            mkViews(M_USER_LATENCY_MS, "user_task")
        ).flatten
        
        val viewMgr = Stats.getViewManager
        
        // Add all views.
        for (view ← views)
            viewMgr.registerView(view)
    }
}
