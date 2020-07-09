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

package org.apache.nlpcraft.common.version

import java.time.LocalDate

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.common._

/**
  * Release version holder. Note that this is manually changing property. For every official
  * release the new version will be added to this object manually.
  */
object NCVersion extends LazyLogging {
    final val copyright = s"Copyright (C) 2020 Apache Software Foundation"
    
    /**
      *
      * @param version Semver-based release version of the NLPCraft.
      * @param date Date of this release.
      */
    case class Version(
        version: String, // Semver.
        date: LocalDate
    ) {
        override def toString = s"Version [version=$version, date=$date]"
    }
    
    // +=================================================+
    // | UPDATE THIS SEQUENCE FOR EACH RELEASE MANUALLY. |
    // +=================================================+
    private final val VERSIONS = Seq(
        Version("0.5.0", LocalDate.of(2020, 4, 16)),
        Version("0.6.0", LocalDate.of(2020, 5, 25)),
        Version("0.6.2", LocalDate.of(2020, 7, 9))
    ).sortBy(_.version)
    // +=================================================+
    // | UPDATE THIS SEQUENCE FOR EACH RELEASE MANUALLY. |
    // +=================================================+
    
    if (U.distinct(VERSIONS.map(_.version).toList).lengthCompare(VERSIONS.size) != 0)
        throw new AssertionError(s"Versions are NOT unique.")
    
    /**
      * Gets current version.
      */
    lazy val getCurrent: Version = VERSIONS.last
}

