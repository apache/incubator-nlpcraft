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

import java.util
import scala.collection.JavaConverters._

/**
  * Note that suggestions and data can be simple type or java collections to be transfer between server and probe as JSON
  */
case class NCInspection(
    errors: Option[Seq[String]] = None,
    warnings: Option[Seq[String]] = None,
    suggestions: Option[Seq[AnyRef]] = None,

    // Information for next inspection layer.
    data: Option[AnyRef] = None
) {
    def serialize(): java.util.Map[String, AnyRef] = {
        val m: util.Map[String, AnyRef] = new java.util.HashMap[String, AnyRef]

        m.put("errors", errors.getOrElse(Seq.empty).asJava)
        m.put("warnings", warnings.getOrElse(Seq.empty).asJava)
        m.put("suggestions", suggestions.getOrElse(Seq.empty).asJava)
        m.put("data", data.orNull)

        m
    }
}

object NCInspection {
    def deserialize(m: util.Map[String, AnyRef]): NCInspection = {
        def getSeq(name: String): Option[Seq[String]] = {
            val seq = m.get(name).asInstanceOf[java.util.List[String]]

            if (seq.isEmpty) None else Some(seq.asScala)
        }

        NCInspection(
            errors = getSeq("errors"),
            warnings = getSeq("warnings"),
            suggestions = getSeq("suggestions"),
            data = Option(m.get("data"))
        )
    }
}
