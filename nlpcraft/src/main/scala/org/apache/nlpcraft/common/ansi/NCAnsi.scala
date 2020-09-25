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

package org.apache.nlpcraft.common.ansi

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.common._

/**
 * Scala 2.13 shim for `scala.io.AnsiColor` + more functionality.
 */
sealed trait NCAnsi extends LazyLogging {
    import NCAnsi._

    private final val BLACK = "\u001b[30m"
    private final val RED = "\u001b[31m"
    private final val GREEN = "\u001b[32m"
    private final val YELLOW = "\u001b[33m"
    private final val BLUE = "\u001b[34m"
    private final val MAGENTA = "\u001b[35m"
    private final val CYAN = "\u001b[36m"
    private final val WHITE = "\u001b[37m"
    private final val BLACK_B = "\u001b[40m"
    private final val RED_B = "\u001b[41m"
    private final val GREEN_B = "\u001b[42m"
    private final val YELLOW_B = "\u001b[43m"
    private final val BLUE_B = "\u001b[44m"
    private final val MAGENTA_B = "\u001b[45m"
    private final val CYAN_B = "\u001b[46m"
    private final val WHITE_B = "\u001b[47m"
    private final val RESET = "\u001b[0m"
    private final val BOLD = "\u001b[1m"
    private final val UNDERLINED = "\u001b[4m"
    private final val BLINK = "\u001b[5m"
    private final val REVERSED = "\u001b[7m"
    private final val INVISIBLE = "\u001b[8m"

    def isEnabled: Boolean = !U.isSysEnvTrue(PROP)

    def ansiBlackFg: String = if (isEnabled) BLACK else ""
    def ansiBlackBg: String = if (isEnabled) BLACK_B else ""
    def ansiRedFg: String = if (isEnabled) RED else ""
    def ansiRedBg: String = if (isEnabled) RED_B else ""
    def ansiGreenFg: String = if (isEnabled) GREEN else ""
    def ansiGreenBg: String = if (isEnabled) GREEN_B else ""
    def ansiYellowFg: String = if (isEnabled) YELLOW else ""
    def ansiYellowBg: String = if (isEnabled) YELLOW_B else ""
    def ansiBlueFg: String = if (isEnabled) BLUE else ""
    def ansiBlueBg: String = if (isEnabled) BLUE_B else ""
    def ansiMagentaFg: String = if (isEnabled) MAGENTA else ""
    def ansiMagentaBg: String = if (isEnabled) MAGENTA_B else ""
    def ansiCyanFg: String = if (isEnabled) CYAN else ""
    def ansiCyanBg: String = if (isEnabled) CYAN_B else ""
    def ansiWhiteFg: String = if (isEnabled) WHITE else ""
    def ansiWhiteBg: String = if (isEnabled) WHITE_B else ""
    def ansiBold: String = if (isEnabled) BOLD else ""
    def ansiUnderlined: String = if (isEnabled) UNDERLINED else ""
    def ansiReset: String = if (isEnabled) RESET else ""
    def ansiReversed: String = if (isEnabled) REVERSED else ""
    def ansiBlink: String = if (isEnabled) BLINK else ""
    def ansiInvisible: String = if (isEnabled) INVISIBLE else ""

    def ansiGreen(s: String): String = s"$ansiGreenFg$s$ansiReset"
    def ansiRed(s: String): String = s"$ansiRedFg$s$ansiReset"
    def ansiCyan(s: String): String = s"$ansiCyanFg$s$ansiReset"
    def ansiYellow(s: String): String = s"$ansiYellowFg$s$ansiReset"
    def ansiBlack(s: String): String = s"$ansiBlackFg$s$ansiReset"
    def ansiWhite(s: String): String = s"$ansiWhiteFg$s$ansiReset"
    def ansiBlue(s: String): String = s"$ansiBlueFg$s$ansiReset"
}

object NCAnsi extends NCAnsi {
    // Enabled by default.
    // NOTE: it's not static as it can be changed at runtime.
    private final val PROP = "NLPCRAFT_ANSI_COLOR_DISABLED"

    /**
     *
     * @param f
     */
    def setEnabled(f: Boolean): Unit =
        System.setProperty(PROP, (!f).toString)

    /**
     *
     */
    def ackStatus(): Unit =
        if (isEnabled)
            logger.info(
                s"${U.bgRainbow("ANSI coloring")} ${U.fgRainbow("is enabled")}. " +
                s"Use '-D${ansiCyanFg}NLPCRAFT_ANSI_COLOR_DISABLED$ansiReset=true' to disable it."
            )
}
