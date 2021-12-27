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

package org.apache.nlpcraft.internal.version

import java.time.*
import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.internal.*
import org.apache.nlpcraft.internal.util.NCUtils

/**
  * Release version holder. Note that this is manually changing property. For every official
  * release the new version will be added to this object manually.
  */
object NCVersion extends LazyLogging:
    final val year = Year.now().toString
    final val copyright = s"Copyright (C) $year Apache Software Foundation"
    final val copyrightShort = s"(C) $year ASF"

    /**
      *
      * @param version Semver-based release version of the NLPCraft.
      * @param date Date of this release.
      */
    case class Version(version: String/* Semver */, date: LocalDate):
        override def toString = s"Version [version=$version, date=$date]"

    // +=================================================+
    // | UPDATE THIS SEQUENCE FOR EACH RELEASE MANUALLY. |
    // +=================================================+
    private final val VERSIONS = Seq(
        Version("0.5.0", LocalDate.of(2020, 4, 16)),
        Version("0.6.0", LocalDate.of(2020, 5, 25)),
        Version("0.6.2", LocalDate.of(2020, 7, 9)),
        Version("0.7.0", LocalDate.of(2020, 9, 29)),
        Version("0.7.1", LocalDate.of(2020, 10, 29)),
        Version("0.7.2", LocalDate.of(2020, 11, 19)),
        Version("0.7.3", LocalDate.of(2020, 12, 31)),
        Version("0.7.4", LocalDate.of(2021, 1, 31)),
        Version("0.7.5", LocalDate.of(2021, 4, 30)),
        Version("0.8.0", LocalDate.of(2021, 5, 30)),
        Version("0.9.0", LocalDate.of(2021, 7, 10)),

        // Version '1.0.0+' is incompatible with previous versions.
        Version("1.0.0", LocalDate.of(2022, 3, 1)),
    ).sortBy(_.version)
    // +=================================================+
    // | UPDATE THIS SEQUENCE FOR EACH RELEASE MANUALLY. |
    // +=================================================+

    if NCUtils.distinct(VERSIONS.map(_.version).toList).lengthCompare(VERSIONS.size) != 0 then
        throw new AssertionError(s"Versions are NOT unique.")

    /**
      * Gets current version.
      */
    lazy val getCurrent: Version = VERSIONS.last

