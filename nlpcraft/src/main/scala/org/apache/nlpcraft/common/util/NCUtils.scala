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

package org.apache.nlpcraft.common.util

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.common.ansi.NCAnsi.*

import java.util.Random
import scala.sys.SystemProperties

/**
  *
  */
object NCUtils extends LazyLogging:
    final val NL = System getProperty "line.separator"
    private final val RND = new Random()
    private val sysProps = new SystemProperties
    private val ANSI_FG_8BIT_COLORS = for (i <- 16 to 255) yield ansi256Fg(i)
    private val ANSI_BG_8BIT_COLORS = for (i <- 16 to 255) yield ansi256Bg(i)
    private val ANSI_FG_4BIT_COLORS = Seq(
        ansiRedFg,
        ansiGreenFg,
        ansiBlueFg,
        ansiYellowFg,
        ansiWhiteFg,
        ansiBlackFg,
        ansiCyanFg
    )
    private val ANSI_BG_4BIT_COLORS = Seq(
        ansiRedBg,
        ansiGreenBg,
        ansiBlueBg,
        ansiYellowBg,
        ansiWhiteBg,
        ansiBlackBg,
        ansiCyanBg
    )
    private val ANSI_4BIT_COLORS = for (fg <- ANSI_FG_4BIT_COLORS; bg <- ANSI_BG_4BIT_COLORS) yield s"$fg$bg"

    /**
      * Gets system property, or environment variable (in that order), or `None` if none exists.
      *
      * @param s Name of the system property or environment variable.
      */
    def sysEnv(s: String): Option[String] = sysProps.get(s).orElse(sys.env.get(s))

    /**
      * Tests whether given system property of environment variable is set or not.
      *
      * @param s @param s Name of the system property or environment variable.
      * @return
      */
    def isSysEnvSet(s: String): Boolean = sysProps.get(s).nonEmpty || sys.env.contains(s)

    /**
      * Returns `true` if given system property, or environment variable is provided and has value
      * 'true'. In all other cases returns `false`.
      *
      * @param s Name of the system property or environment variable.
      */
    def isSysEnvTrue(s: String): Boolean = sysEnv(s) match
        case None => false
        case Some(v) => java.lang.Boolean.valueOf(v) == java.lang.Boolean.TRUE


    /**
      * Gets random value from given sequence.
      *
      * @param seq Sequence.
      */
    def getRandom[T](seq: Seq[T]): T = seq(RND.nextInt(seq.size))

    /**
      * Makes random filled sequence with given length from initial.
      *
      * @param seq Initial sequence.
      * @param n Required sequence length.
      */
    def getRandomSeq[T](seq: Seq[T], n: Int): Seq[T] =
        require(seq.lengthCompare(n) >= 0)

        val src = scala.collection.mutable.ArrayBuffer.empty[T] ++ seq
        val dest = scala.collection.mutable.ArrayBuffer.empty[T]

        (0 until n).foreach(_ => dest += src.remove(RND.nextInt(src.size)))

        dest.toSeq

    /**
      * Prints 4-bit ASCII-logo.
      */
    def asciiLogo4Bit(): String =
        raw"$ansiBlueFg    _   ____     $ansiCyanFg ______           ______   $ansiReset$NL" +
        raw"$ansiBlueFg   / | / / /___  $ansiCyanFg/ ____/________ _/ __/ /_  $ansiReset$NL" +
        raw"$ansiBlueFg  /  |/ / / __ \$ansiCyanFg/ /   / ___/ __ `/ /_/ __/  $ansiReset$NL" +
        raw"$ansiBlueFg / /|  / / /_/ /$ansiCyanFg /___/ /  / /_/ / __/ /_    $ansiReset$NL" +
        raw"$ansiBold$ansiRedFg/_/ |_/_/ .___/$ansiRedFg\____/_/   \__,_/_/  \__/      $ansiReset$NL" +
        raw"$ansiBold$ansiRedFg       /_/                                              $ansiReset$NL"

    /**
      * Prints 8-bit ASCII-logo.
      */
    def asciiLogo8Bit1(): String =
        fgRainbow4Bit(
            raw"${ansi256Fg(28)}    _   ____      ______           ______   $ansiReset$NL" +
            raw"${ansi256Fg(64)}   / | / / /___  / ____/________ _/ __/ /_  $ansiReset$NL" +
            raw"${ansi256Fg(100)}  /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/  $ansiReset$NL" +
            raw"${ansi256Fg(136)} / /|  / / /_/ / /___/ /  / /_/ / __/ /_    $ansiReset$NL" +
            raw"${ansi256Fg(172)}/_/ |_/_/ .___/\____/_/   \__,_/_/  \__/    $ansiReset$NL" +
            raw"${ansi256Fg(208)}       /_/                                  $ansiReset$NL"
        )

    /**
      * Prints 8-bit ASCII-logo.
      */
    def asciiLogo8Bit(): String =
        val startColor = getRandom(Seq(16, 22, 28, 34, 40, 46))
        val range = 6

        (for (lineIdx <- Seq(
            raw"    _   ____      ______           ______   $NL",
            raw"   / | / / /___  / ____/________ _/ __/ /_  $NL",
            raw"  /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/  $NL",
            raw" / /|  / / /_/ / /___/ /  / /_/ / __/ /_    $NL",
            raw"/_/ |_/_/ .___/\____/_/   \__,_/_/  \__/    $NL",
            raw"       /_/                                  $NL"
        ).zipWithIndex) yield {
            val line = lineIdx._1
            val idx = lineIdx._2
            val start = startColor + (36 * idx)
            val end = start + range - 1

            gradAnsi8BitFgLine(line, start, end)
        })
        .mkString("")

    /**
      *
      * @param line
      * @param startColor Inclusive.
      * @param endColor Inclusive.
      * @return
      */
    def gradAnsi8BitFgLine(line: String, startColor: Int, endColor: Int): String =
        line.zipWithIndex.foldLeft(new StringBuilder())((buf, zip) => {
            val ch = zip._1
            val idx = zip._2
            val color = startColor + idx % (endColor - startColor + 1)

            buf ++= s"${ansi256Fg(color)}$ch"
        })
        .toString + ansiReset

    /**
      *
      * @param line
      * @param startColor Inclusive.
      * @param endColor Inclusive.
      * @return
      */
    def gradAnsi8BitBgLine(line: String, startColor: Int, endColor: Int): String =
        line.zipWithIndex.foldLeft(new StringBuilder())((buf, zip) => {
            val ch = zip._1
            val idx = zip._2
            val color = startColor + idx % (endColor - startColor + 1)

            buf ++= s"${ansi256Bg(color)}$ch"
        })
        .toString + ansiReset

    /**
      *
      * @param s
      * @return
      */
    def fgRainbow4Bit(s: String, addOn: String = ""): String = rainbowImpl(s, ANSI_FG_4BIT_COLORS, addOn)

    /**
      *
      * @param s
      * @return
      */
    def fgRainbow8Bit(s: String, addOn: String = ""): String = rainbowImpl(s, ANSI_FG_8BIT_COLORS, addOn)

    /**
      *
      * @param s
      * @return
      */
    def bgRainbow4Bit(s: String, addOn: String = ""): String = rainbowImpl(s, ANSI_BG_4BIT_COLORS, addOn)

    /**
      *
      * @param s
      * @return
      */
    def bgRainbow8Bit(s: String, addOn: String = ""): String = rainbowImpl(s, ANSI_BG_8BIT_COLORS, addOn)

    /**
      *
      * @param s
      * @return
      */
    def rainbow4Bit(s: String, addOn: String = ""): String = randomRainbowImpl(s, ANSI_4BIT_COLORS, addOn)

    /**
      *
      * @param s
      * @param colors
      * @param addOn
      * @return
      */
    private def randomRainbowImpl(s: String, colors: Seq[String], addOn: String): String =
        s.zipWithIndex.foldLeft(new StringBuilder())((buf, zip) => {
            buf ++= s"${colors(RND.nextInt(colors.size))}$addOn${zip._1}"
        })
        .toString + ansiReset

    /**
      *
      * @param s
      * @param colors
      * @param addOn
      * @return
      */
    private def rainbowImpl(s: String, colors: Seq[String], addOn: String): String =
        s.zipWithIndex.foldLeft(new StringBuilder())((buf, zip) => {
            buf ++= s"${colors(zip._2 % colors.size)}$addOn${zip._1}"
        })
        .toString + ansiReset

    /**
      * ANSI color JSON string.
      *
      * @param json JSON string to color.
      * @return
      */
    def colorJson(json: String): String =
        val buf = new StringBuilder
        var inQuotes = false
        var isValue = false

        for (ch <- json)
            ch match
                case ':' if !inQuotes => buf ++= r(":"); isValue = true
                case '[' | ']' | '{' | '}' if !inQuotes => buf ++= y(s"$ch"); isValue = false
                case ',' if !inQuotes => buf ++= ansi256Fg(213, s"$ch"); isValue = false
                case '"' =>
                    if inQuotes then
                        buf ++= ansi256Fg(105, s"$ch")
                    else
                        buf ++= s"${ansi256Fg(105)}$ch"
                        buf ++= (if isValue then G else ansiCyanFg)

                    inQuotes = !inQuotes

                case _ => buf ++= s"$ch"


        buf.append(RST)
        buf.toString()

    /**
      * Shortcut - current timestamp in milliseconds.
      */
    def now(): Long = System.currentTimeMillis()
