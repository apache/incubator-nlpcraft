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
import internal.ansi.NCAnsi.*

/**
  * Global syntax sugar for throwing [[NCException]].
  *
  * @param msg Exception message.
  * @param cause Optional cause.
  */
def E[T](msg: String, cause: Throwable = null): T = throw new NCException(msg, cause)

// Internal deep debug flag (more verbose tracing).
final val DEEP_DEBUG = false

// Re-route to 8-bit colors.
def G: String = ansi256Fg(34)
def M: String = ansi256Fg(177)
def R: String = ansi256Fg(202)

def C: String = ansiCyanFg
def Y: String = ansiYellowFg
def W: String = ansi256Fg(231)
def B: String = ansiBlueFg
def K: String = ansiBlackFg

def GB: String = ansi256Bg(34)
def MB: String = ansi256Bg(177)
def RB: String = ansi256Bg(202)
def CB: String = ansiCyanBg
def YB: String = ansiYellowBg
def WB: String = ansiWhiteBg
def BB: String = ansiBlueBg
def KB: String = ansiBlackBg

def BO: String = ansiBold
def RST: String = ansiReset

def g(s: Any): String = s"$G${s.toString}$RST"
def m(s: Any): String = s"$M${s.toString}$RST"
def r(s: Any): String = s"$R${s.toString}$RST"
def c(s: Any): String = s"$C${s.toString}$RST"
def y(s: Any): String = s"$Y${s.toString}$RST"
def w(s: Any): String = s"$W${s.toString}$RST"
def b(s: Any): String = s"$B${s.toString}$RST"
def k(s: Any): String = s"$K${s.toString}$RST"

def green(s: Any): String = s"$G${s.toString}$RST"
def magenta(s: Any): String = s"$M${s.toString}$RST"
def red(s: Any): String = s"$R${s.toString}$RST"
def cyan(s: Any): String = s"$C${s.toString}$RST"
def yellow(s: Any): String = s"$Y${s.toString}$RST"
def white(s: Any): String = s"$W${s.toString}$RST"
def blue(s: Any): String = s"$B${s.toString}$RST"
def black(s: Any): String = s"$K${s.toString}$RST"
def gb(s: Any): String = s"$GB${s.toString}$RST"
def rb(s: Any): String = s"$RB${s.toString}$RST"
def cb(s: Any): String = s"$CB${s.toString}$RST"
def yb(s: Any): String = s"$YB${s.toString}$RST"
def wb(s: Any): String = s"$WB${s.toString}$RST"
def bb(s: Any): String = s"$BB${s.toString}$RST"
def kb(s: Any): String = s"$KB${s.toString}$RST"
def greenBg(s: Any): String = s"$GB${s.toString}$RST"
def magentaBg(s: Any): String = s"$MB${s.toString}$RST"
def redBg(s: Any): String = s"$RB${s.toString}$RST"
def cyanBg(s: Any): String = s"$CB${s.toString}$RST"
def yellowBg(s: Any): String = s"$YB${s.toString}$RST"
def whiteBg(s: Any): String = s"$WB${s.toString}$RST"
def blueBg(s: Any): String = s"$BB${s.toString}$RST"
def blackBg(s: Any): String = s"$KB${s.toString}$RST"

// Effect shortcuts...
def rv(s: Any): String = s"$ansiReversed${s.toString}$RST"
def bo(s: Any): String = s"$ansiBold${s.toString}$RST"
def reverse(s: Any): String = s"$ansiReversed${s.toString}$RST"
def bold(s: Any): String = s"$ansiBold${s.toString}$RST"

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
