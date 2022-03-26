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

package org.apache.nlpcraft

import org.apache.nlpcraft.*

import scala.annotation.targetName

/**
  * Global syntax sugar for throwing [[NCException]].
  *
  * @param msg Exception message.
  * @param cause Optional cause.
  */
def E[T](msg: String, cause: Throwable = null): T = throw new NCException(msg, cause)

// Internal deep debug flag (more verbose tracing).
final val DEEP_DEBUG = false

extension[T](opt: Option[T])
    @targetName("equalEqualEqualOpt")
    def === (x: Option[T]): Boolean = opt.isDefined && x.isDefined && opt.get == x.get
    @targetName("equalEqualEqual")
    def ===(x: T): Boolean = opt.isDefined && opt.get == x

extension(v: Int)
    def MS: Int = v
    def SECS: Int = v * 1000
    def MINS: Int = v * 1000 * 60
    def HOURS: Int = v * 1000 * 60 * 60
    def DAYS: Int = v * 1000 * 60 * 60 * 24
    def FPS: Int = 1000 / v
    def ms: Int = MS
    def secs: Int = SECS
    def mins: Int = MINS
    def hours: Int = HOURS
    def days: Int = DAYS
    def fps: Int = 1000 / v
    def TB: Int = v * 1024 * 1024 * 1024 * 1024
    def GB: Int = v * 1024 * 1024 * 1024
    def MB: Int = v * 1024 * 1024
    def KB: Int = v * 1024
    def tb: Int = TB
    def gb: Int = GB
    def mb: Int = MB
    def kb: Int = KB

extension(v: Long)
    def TB: Long = v * 1024 * 1024 * 1024 * 1024
    def GB: Long = v * 1024 * 1024 * 1024
    def MB: Long = v * 1024 * 1024
    def KB: Long = v * 1024
    def tb: Long = TB
    def gb: Long = GB
    def mb: Long = MB
    def kb: Long = KB
    def MS: Long = v
    def SECS: Long = v * 1000
    def MINS: Long = v * 1000 * 60
    def HOURS: Long = v * 1000 * 60 * 60
    def DAYS: Long = v * 1000 * 60 * 60 * 24
    def FPS: Long = 1000 / v
    def ms: Long = MS
    def secs: Long = SECS
    def mins: Long = MINS
    def hours: Long = HOURS
    def days: Long = DAYS
    def fps: Long = 1000 / v
