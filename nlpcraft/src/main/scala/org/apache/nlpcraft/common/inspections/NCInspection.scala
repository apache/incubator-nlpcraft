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

package org.apache.nlpcraft.common.inspections

import scala.collection.JavaConverters._

/**
  * Note that 'suggestions' and 'data' must be simple type or java collections to be transfer between server and probe as JSON
  */
case class NCInspection(
    errors: java.util.List[String],
    warnings: java.util.List[String],
    suggestions: java.util.List[AnyRef],

    // Information for next inspection layer.
    data: AnyRef = None
)
object NCInspection {
    def apply(
        errors: Option[Seq[String]],
        warnings: Option[Seq[String]],
        suggestions: Option[Seq[AnyRef]],
        data: Option[AnyRef]
    ): NCInspection = {
        def convert[T](optSeq: Option[Seq[T]]): java.util.List[T] = optSeq.getOrElse(Seq.empty).asJava

        new NCInspection(
            errors = convert(errors),
            warnings = convert(warnings),
            suggestions = convert(suggestions),
            data = data.orNull
        )
    }

    def apply(
        errors: Option[Seq[String]],
        warnings: Option[Seq[String]],
        suggestions: Option[Seq[AnyRef]]
    ): NCInspection = apply(errors, warnings, suggestions, None)

    def apply(): NCInspection  = NCInspection(None, None, None, None)
}