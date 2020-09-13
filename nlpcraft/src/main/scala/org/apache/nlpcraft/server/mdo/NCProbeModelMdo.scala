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

package org.apache.nlpcraft.server.mdo

import org.apache.nlpcraft.server.mdo.impl._

/**
  * Probe model MDO.
  */
@NCMdoEntity(sql = false)
case class NCProbeModelMdo(
    @NCMdoField id: String,
    @NCMdoField name: String,
    @NCMdoField version: String,
    @NCMdoField enabledBuiltInTokens: Set[String]
) extends NCAnnotatedMdo[NCProbeModelMdo] {
    override def hashCode(): Int = s"$id$name".hashCode()
    
    override def equals(obj: Any): Boolean = {
        obj match {
            case x: NCProbeModelMdo ⇒ x.id == id
            case _ ⇒ false
        }
    }
}
