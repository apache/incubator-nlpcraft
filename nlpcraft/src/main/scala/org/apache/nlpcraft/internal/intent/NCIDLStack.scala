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

package org.apache.nlpcraft.internal.intent

import scala.collection.mutable

/**
  *
  */
case class NCIDLStackItem (
    value: Object,
    entUse: Int
)

/**
  *
  */
object NCIDLStackItem {
    def apply(v: Boolean, f: Int): NCIDLStackItem = new NCIDLStackItem(Boolean.box(v), f)
    def apply(v: Long, f: Int): NCIDLStackItem = new NCIDLStackItem(Long.box(v), f)
    def apply(v: Double, f: Int): NCIDLStackItem = new NCIDLStackItem(Double.box(v), f)
}

/**
  *
  */
trait NCIDLStackType extends (() => NCIDLStackItem)

/**
  *
  */
class NCIDLStack extends mutable.Stack[NCIDLStackType]:
    /**
      * Special marker for stack frames.
      */
    final val PLIST_MARKER: NCIDLStackType = () => { NCIDLStackItem(null, 0) }

