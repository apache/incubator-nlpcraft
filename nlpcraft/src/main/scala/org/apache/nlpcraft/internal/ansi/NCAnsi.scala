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

package org.apache.nlpcraft.internal.ansi

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.internal.*
import org.apache.nlpcraft.internal.util.NCUtils

/**
  *
  */
sealed trait NCAnsi extends LazyLogging:
    import NCAnsi.*

    private final val ESC = "\u001b"
    private final val BELL = "\u0007"
    private final val CSI = s"$ESC["
    private final val OSC = s"$ESC]"
    private final val RESET = s"${CSI}0m"

    // 4-bit colors.
    private final val BLACK = s"${CSI}30m"
    private final val RED = s"${CSI}31m"
    private final val GREEN = s"${CSI}32m"
    private final val YELLOW = s"${CSI}33m"
    private final val BLUE = s"${CSI}34m"
    private final val MAGENTA = s"${CSI}35m"
    private final val CYAN = s"${CSI}36m"
    private final val WHITE = s"${CSI}37m"
    private final val BLACK_B = s"${CSI}40m"
    private final val RED_B = s"${CSI}41m"
    private final val GREEN_B = s"${CSI}42m"
    private final val YELLOW_B = s"${CSI}43m"
    private final val BLUE_B = s"${CSI}44m"
    private final val MAGENTA_B = s"${CSI}45m"
    private final val CYAN_B = s"${CSI}46m"
    private final val WHITE_B = s"${CSI}47m"

    def ansi256Fg(color: Int): String = if isEnabled then s"[38;5;${color}m" else ""
    def ansi256Bg(color: Int): String = if isEnabled then s"[48;5;${color}m" else ""
    def ansi256Fg(fgColor: Int, s: Any): String = s"${ansi256Fg(fgColor)}${s.toString}$ansiReset"
    def ansi256(fgColor: Int, bgColor: Int, s: Any): String = s"${ansi256Fg(fgColor)}${ansi256Bg(bgColor)}${s.toString}$ansiReset"

    // Effects.
    private final val BOLD = s"${CSI}1m"
    private final val UNDERLINED = s"${CSI}4m"
    private final val BLINK = s"${CSI}5m"
    private final val REVERSED = s"${CSI}7m"
    private final val INVISIBLE = s"${CSI}8m"

    // Erase functions.
    private final val CLEAR_SCREEN = s"${CSI}J"
    private final val CLEAR_SCREEN_AFTER = s"${CSI}0J"
    private final val CLEAR_SCREEN_BEFORE = s"${CSI}1J"
    private final val CLEAR_LINE = s"${CSI}K"
    private final val CLEAR_LINE_AFTER = s"${CSI}0K"
    private final val CLEAR_LINE_BEFORE = s"${CSI}1K"

    // Cursor moves.
    private final val CURSOR_UP = s"${CSI}1A"
    private final val CURSOR_DOWN = s"${CSI}1B"
    private final val CURSOR_LEFT = s"${CSI}1D"
    private final val CURSOR_RIGHT = s"${CSI}1C"
    private final val CURSOR_POS_SAVE= s"${CSI}s"
    private final val CURSOR_POS_RESTORE = s"${CSI}u"
    private final val CURSOR_LINE_HOME = s"${CSI}0G"
    private final val CURSOR_SCREEN_HOME = s"${CSI}H"
    private final val CURSOR_HIDE = s"$CSI?25l"
    private final val CURSOR_SHOW = s"$CSI?25h"

    def isEnabled: Boolean = !NCUtils.isSysEnvTrue(PROP)


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

    // Color functions.
    def ansiBlackFg: String = if isEnabled then BLACK else ""
    def ansiBlackBg: String = if isEnabled then BLACK_B else ""
    def ansiRedFg: String = if isEnabled then RED else ""
    def ansiRedBg: String = if isEnabled then RED_B else ""
    def ansiGreenFg: String = if isEnabled then GREEN else ""
    def ansiGreenBg: String = if isEnabled then GREEN_B else ""
    def ansiYellowFg: String = if isEnabled then YELLOW else ""
    def ansiYellowBg: String = if isEnabled then YELLOW_B else ""
    def ansiBlueFg: String = if isEnabled then BLUE else ""
    def ansiBlueBg: String = if isEnabled then BLUE_B else ""
    def ansiMagentaFg: String = if isEnabled then MAGENTA else ""
    def ansiMagentaBg: String = if isEnabled then MAGENTA_B else ""
    def ansiCyanFg: String = if isEnabled then CYAN else ""
    def ansiCyanBg: String = if isEnabled then CYAN_B else ""
    def ansiWhiteFg: String = if isEnabled then WHITE else ""
    def ansiWhiteBg: String = if isEnabled then WHITE_B else ""

    // Effect functions.
    def ansiBold: String = if isEnabled then BOLD else ""
    def ansiUnderlined: String = if isEnabled then UNDERLINED else ""
    def ansiReset: String = if isEnabled then RESET else ""
    def ansiReversed: String = if isEnabled then REVERSED else ""
    def ansiBlink: String = if isEnabled then BLINK else ""
    def ansiInvisible: String = if isEnabled then INVISIBLE else ""

    def ansiGreen(s: Any): String = s"$ansiGreenFg${s.toString}$ansiReset"
    def ansiRed(s: Any): String = s"$ansiRedFg${s.toString}$ansiReset"
    def ansiCyan(s: Any): String = s"$ansiCyanFg${s.toString}s$ansiReset"
    def ansiYellow(s: Any): String = s"$ansiYellowFg${s.toString}$ansiReset"
    def ansiBlack(s: Any): String = s"$ansiBlackFg${s.toString}s$ansiReset"
    def ansiWhite(s: Any): String = s"$ansiWhiteFg${s.toString}$ansiReset"
    def ansiBlue(s: Any): String = s"$ansiBlueFg${s.toString}$ansiReset"
    def ansiMagenta(s: Any): String = s"$ansiMagentaFg${s.toString}$ansiReset"
    def ansiBold(s: Any): String = s"$ansiBold${s.toString}$ansiReset"

    // Erase functions.
    def ansiClearScreen: String = if isEnabled then CLEAR_SCREEN else ""
    def ansiClearScreenAfter: String = if isEnabled then CLEAR_SCREEN_AFTER else ""
    def ansiClearScreenBefore: String = if isEnabled then CLEAR_SCREEN_BEFORE else ""
    def ansiClearLine: String = if isEnabled then CLEAR_LINE else ""
    def ansiClearLineAfter: String = if isEnabled then CLEAR_LINE_AFTER else ""
    def ansiClearLineBefore: String = if isEnabled then CLEAR_LINE_BEFORE else ""

    // Cursor movement functions.
    def ansiCursorUp: String = if isEnabled then CURSOR_UP else ""
    def ansiCursorDown: String = if isEnabled then CURSOR_DOWN else ""
    def ansiCursorLeft: String = if isEnabled then CURSOR_LEFT else ""
    def ansiCursorRight: String = if isEnabled then CURSOR_RIGHT else ""
    def ansiCursorLineHome: String = if isEnabled then CURSOR_LINE_HOME else ""
    def ansiCursorScreenHome: String = if isEnabled then CURSOR_SCREEN_HOME else ""
    def ansiCursorPosSave: String = if isEnabled then CURSOR_POS_SAVE else ""
    def ansiCursorPosRestore: String = if isEnabled then CURSOR_POS_RESTORE else ""
    def ansiCursorShow: String = if isEnabled then CURSOR_SHOW else ""
    def ansiCursorHide: String = if isEnabled then CURSOR_HIDE else ""

/**
  *
  */
object NCAnsi extends NCAnsi:
    // Enabled by default.
    // NOTE: it's not static as it can be changed at runtime.
    private final val PROP = "NLPCRAFT_ANSI_COLOR_DISABLED"

    /**
      *
      * @param f
      */
    def setEnabled(f: Boolean): Unit = System.setProperty(PROP, (!f).toString)

    /**
      *
      */
    def ackStatus(): Unit =
        if isEnabled then
            logger.info(s"${NCUtils.bgRainbow4Bit("ANSI")} coloring is enabled. Use '-D${ansiCyanFg}NLPCRAFT_ANSI_COLOR_DISABLED$ansiReset=true' to disable it.", 130, 147)

