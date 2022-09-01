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

extension[T](opt: Option[T])
    /**
      * Provides equality check between two options' values. This check is `true` when both options
      * are defined and have the same value.
      *
      * @param x Option to compare.
      */
    @targetName("equalEqualEqualOpt")
    def === (x: Option[T]): Boolean = opt.isDefined && x.isDefined && opt.get == x.get

    /**
      * Provides equality check between the option and the value of the same type. This check is `true` when
      * the option is defined and its value is equal to the given value.
      *
      * @param x Value to compare the option to
      */
    @targetName("equalEqualEqual")
    def ===(x: T): Boolean = opt.isDefined && opt.get == x

extension(v: Int)
    /** Converts milliseconds `int` value to seconds. */
    def secs: Int = v * 1000
    /** Converts milliseconds `int` value to minutes. */
    def mins: Int = v * 1000 * 60
    /** Converts milliseconds `int` value to hours. */
    def hours: Int = v * 1000 * 60 * 60
    /** Converts milliseconds `int` value to days. */
    def days: Int = v * 1000 * 60 * 60 * 24
    /** Converts bytes `int` value to terabytes. */
    def tb: Int = v * 1024 * 1024 * 1024 * 1024
    /** Converts bytes `int` value to gigabytes. */
    def gb: Int = v * 1024 * 1024 * 1024
    /** Converts bytes `int` value to megabytes. */
    def mb: Int = v * 1024 * 1024
    /** Converts bytes `int` value to kilobytes. */
    def kb: Int = v * 1024

extension(v: Long)
    /** Converts milliseconds `long` value to seconds. */
    def secs: Long = v * 1000
    /** Converts milliseconds `long` value to minutes. */
    def mins: Long = v * 1000 * 60
    /** Converts milliseconds `long` value to hours. */
    def hours: Long = v * 1000 * 60 * 60
    /** Converts milliseconds `long` value to days. */
    def days: Long = v * 1000 * 60 * 60 * 24
    /** Converts bytes `long` value to terabytes. */
    def tb: Long = v * 1024 * 1024 * 1024 * 1024
    /** Converts bytes `long` value to gigabytes. */
    def gb: Long = v * 1024 * 1024 * 1024
    /** Converts bytes `long` value to megabytes. */
    def mb: Long = v * 1024 * 1024
    /** Converts bytes `long` value to kilobytes. */
    def kb: Long = v * 1024
