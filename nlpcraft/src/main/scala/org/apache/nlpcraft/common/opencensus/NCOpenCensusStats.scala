/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.common.opencensus

import io.opencensus.stats.Aggregation.{Count, Distribution}
import io.opencensus.stats.Measure
import io.opencensus.stats.Measure._
import io.opencensus.stats.View.Name
import io.opencensus.stats._

import java.util
import java.util.Collections

/**
 * Base trait for Open Census stats implementation.
 */
trait NCOpenCensusStats {
    private val LAT_DIST = Distribution.create(BucketBoundaries.create(util.Arrays.asList(
        0.0, 25.0, 100.0, 200.0, 400.0, 800.0, 10000.0
    )))

    /**
     * Records OpenCensus metrics.
     *
     * @param pairs Pairs of Open Census measure and its value. Values must be `Long` or `Double` only.
     */
    def recordStats(pairs: (Measure, AnyVal)*): Unit = {
        val map = Stats.getStatsRecorder.newMeasureMap()

        for ((m, v) <- pairs) {
            m match {
                case d: MeasureDouble => map.put(d, v.asInstanceOf[Double])
                case l: MeasureLong => map.put(l, v.asInstanceOf[Long])
                case _ => throw new AssertionError()
            }
        }

        map.record()
    }

    def mkViews(m: Measure, call: String): List[View] = {
        List(
            View.create(
                Name.create(s"$call/latdist"),
                s"The distribution of the '$call' call latencies",
                m,
                LAT_DIST,
                Collections.emptyList()
            ),
            View.create(
                Name.create(s"$call/count"),
                s"The number of the '$call' invocations",
                m,
                Count.create,
                Collections.emptyList()
            )
        )
    }
}
