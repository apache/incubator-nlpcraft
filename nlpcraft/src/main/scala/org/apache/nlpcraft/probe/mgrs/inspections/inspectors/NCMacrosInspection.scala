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

package org.apache.nlpcraft.probe.mgrs.inspections.inspectors

import org.apache.nlpcraft.common.inspections.NCInspectionService
import org.apache.nlpcraft.model.NCModel

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Inspection for model's macros.
 */
object NCMacrosInspection extends NCInspectionService {
    override def getName: String = "macros"

    override def bodyOnProbe(
        mdl: NCModel,
        args: Option[String],
        suggs: mutable.Buffer[String],
        warns: mutable.Buffer[String],
        errs: mutable.Buffer[String]): Unit = {
        if (args.isDefined && args.get.nonEmpty)
            warns += s"Invalid inspection arguments (ignoring): ${args.get}"

        val syns = mdl.getElements.asScala.flatMap(_.getSynonyms.asScala)

        warns ++= mdl.getMacros.asScala.keys.flatMap(makro ⇒
            if (syns.exists(_.contains(makro))) None else Some(s"Unused macro: $makro")
        )
    }
}
