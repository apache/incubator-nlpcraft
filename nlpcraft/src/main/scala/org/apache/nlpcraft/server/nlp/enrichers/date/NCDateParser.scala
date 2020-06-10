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

package org.apache.nlpcraft.server.nlp.enrichers.date

import java.util.{Locale, Calendar ⇒ C}
import scala.collection.JavaConverters._

/**
  * Date parser.
  */
object NCDateParser {
    // For english calendar settings.
    Locale.setDefault(Locale.forLanguageTag("EN"))

    // USA week.
    private val FIRST_DAY_OF_WEEK = C.SUNDAY
    private val LAST_DAY_OF_WEEK = C.SATURDAY

    private val CAL_MONTHS = Seq(
        C.JANUARY,
        C.FEBRUARY,
        C.MARCH,
        C.APRIL,
        C.MAY,
        C.JUNE,
        C.JULY,
        C.AUGUST,
        C.SEPTEMBER,
        C.OCTOBER,
        C.NOVEMBER,
        C.DECEMBER
    )

    private val MONTH_NUM_MAP: Map[Int, Int] = zipValueIndex(CAL_MONTHS)
    private val NUM_MONTH_MAP: Map[Int, Int] = zipIndexValue(CAL_MONTHS)

    private val QUARTERS_BEGIN = Map(
        1 → C.JANUARY,
        2 → C.APRIL,
        3 → C.JULY,
        4 → C.OCTOBER
    )

    private val QUARTERS: Map[Int, Int] = NUM_MONTH_MAP.map(m ⇒ m._1 → (m._2 / 3 + 1))

    private val SEASONS = Map(
        C.DECEMBER → 1,
        C.JANUARY → 1,
        C.FEBRUARY → 1,
        C.MARCH → 2,
        C.APRIL → 2,
        C.MAY → 2,
        C.JUNE → 3,
        C.JULY → 3,
        C.AUGUST → 3,
        C.SEPTEMBER → 4,
        C.OCTOBER → 4,
        C.NOVEMBER → 4
    )

    private val SEASONS_BEGIN = Map(
        1 → C.DECEMBER,
        2 → C.MARCH,
        3 → C.JUNE,
        4 → C.SEPTEMBER
    )

    // USA week.
    private val WEEK_DAYS = Seq(
        C.SUNDAY,
        C.MONDAY,
        C.TUESDAY,
        C.WEDNESDAY,
        C.THURSDAY,
        C.FRIDAY,
        C.SATURDAY
    )

    private val PERIODS_WEIGHT = Map(
        "d" → 1,
        "dw" → 1,
        "w" → 2,
        "m" → 3,
        "q" → 4,
        "s" → 4,
        "y" → 5,
        "e" → 6,
        "c" → 7
    )

    private val NUM_WEEK_DAYS_MAP: Map[Int, Int] = zipIndexValue(WEEK_DAYS)

    def calculate(f: String, base: Long, inclFrom: Boolean = true, inclTo: Boolean = true): NCDateRange = {
        val seq = f.split(":")

        seq.length match {
            // from d1
            case _ if f.startsWith(":") ⇒
                val res = calculatePart(f.drop(1), base)

                val to = mkTo(res, inclTo)

                NCDateRange(NCDateRange.MIN_VALUE, to, f, (Seq(":") ++ res.periods).asJava)

            // to d2
            case _ if f.endsWith(":") ⇒
                val res = calculatePart(f.take(f.length - 1), base)

                var from = mkFrom(res, inclFrom)

                if (from > getTruncatedNow)
                    from = shift(from, getShiftPeriod(res.period), -1)
    
                NCDateRange(from, NCDateRange.MAX_VALUE, f, (res.periods :+ ":").asJava)

            // between d1 and d2
            case 2 ⇒
                val part1 = seq.head
                val part2 = seq.last

                val res1 = calculatePart(part1, base)
                val res2 = calculatePart(part2, base)

                val d1 = mkFrom(res1, inclFrom)
                val d2 = mkTo(res2, inclTo)

                val sumPeriods = (res1.periods :+ ":") ++ res2.periods

                // Example: 11m:1m, d-1:1m (but current January already finished.)
                def tryUsingWeights(): NCDateRange =
                    if (PERIODS_WEIGHT(res1.period) > PERIODS_WEIGHT(res2.period))
                        NCDateRange(shift(d1, getShiftPeriod(res1.period), -1), d2, f, sumPeriods.asJava)
                    else
                        NCDateRange(d1, shift(d2, getShiftPeriod(res2.period), 1), f, sumPeriods.asJava)

                // Tries to resolve without guarantee.

                if (d1 > d2) {
                    // Special cases.
                    // ==============
                    // Example 1.
                    // ----------
                    // "from october to november of 2010"
                    // October should be processed as month of 2010 but not current year.
                    //
                    // Example 2.
                    // ----------
                    // "from 25th october to 11th november of 2010"
                    // 25th October should be processed as day of 2010, but not current year.
                    if (res1.samePeriods("m") && res2.samePeriods("my") ||
                        res1.samePeriods("dm") && res2.samePeriods("dmy"))
                        NCDateRange(changeYear(d1, d2), d2, f, sumPeriods.asJava)
                    else
                        tryUsingWeights()
                }
                else if (d1 == d2)
                    tryUsingWeights()
                else
                    NCDateRange(d1, d2, f, sumPeriods.asJava)
            case _ ⇒
                val res = calculatePart(f, base)
    
                NCDateRange(res.from, res.to, f, res.periods.asJava)
        }
    }

    private def zipIndexValue[T](seq: Seq[T]): Map[Int, T] = seq.zipWithIndex.map(p ⇒ (p._2 + 1) → p._1).toMap

    private def zipValueIndex[T](seq: Seq[T]): Map[T, Int] = seq.zipWithIndex.map(p ⇒ p._1 → (p._2 + 1)).toMap

    private def set(cal: C, pairs: (Int, Int)*): C = {
        for (pair ← pairs) cal.set(pair._1, pair._2)

        cal
    }

    private def add(cal: C, pairs: (Int, Int)*): C = {
        for (pair ← pairs) cal.add(pair._1, pair._2)

        cal
    }

    private def mkCalendar(d: Option[Long] = None): C = {
        val cal = C.getInstance()

        if (d.isDefined)
            cal.setTimeInMillis(d.get)

        set(
            cal,
            C.HOUR_OF_DAY → 0,
            C.MINUTE → 0,
            C.SECOND → 0,
            C.MILLISECOND → 0
        )
    }

    private def getTruncatedNow: Long = mkCalendar().getTimeInMillis

    private def mkFrom(period: String, opt: Option[Int], d: Long): Long =
        opt match {
            case Some(v) ⇒ mkFrom(period, v, d)
            case None ⇒ mkFrom(period, d)
        }

    // Winter starts in previous year.
    private def adjustWinter(season: Int, cal: C) = if (season == 1) add(cal, C.YEAR → -1)

    private def mkFrom(period: String, d: Long): Long = {
        val cal = mkCalendar(Some(d))

        def set0(pairs: (Int, Int)*): C = set(cal, pairs: _*)

        (period match {
            case "d" | "dw" ⇒ cal
            case "w" ⇒ set0(C.DAY_OF_WEEK → FIRST_DAY_OF_WEEK)
            case "m" ⇒ set0(C.DAY_OF_MONTH → 1)
            case "q" ⇒ set0(
                C.MONTH → QUARTERS_BEGIN(QUARTERS(MONTH_NUM_MAP(cal.get(C.MONTH)))),
                C.DAY_OF_MONTH → 1
            )
            case "y" ⇒ set0(
                C.MONTH → C.JANUARY,
                C.DAY_OF_YEAR → 1
            )
            case "e" ⇒
                set0(
                    C.YEAR → (cal.get(C.YEAR) / 10 * 10 + 1),
                    C.MONTH → C.JANUARY,
                    C.DAY_OF_YEAR → 1
                )
            case "c" ⇒ set0(
                C.YEAR → (cal.get(C.YEAR) / 100 * 100 + (if (cal.get(C.YEAR) < 100) 0 else 1)),
                C.MONTH → C.JANUARY,
                C.DAY_OF_YEAR → 1
            )
            case "s" ⇒
                val s = SEASONS(cal.get(C.MONTH))

                adjustWinter(s, cal)

                set0(
                    C.MONTH → SEASONS_BEGIN(s),
                    C.DAY_OF_MONTH → 1
                )

        }).getTimeInMillis
    }

    private def mkFrom(period: String, v: Int, d: Long): Long = {
        val cal = mkCalendar(Some(d))

        def set0(pairs: (Int, Int)*): C = set(cal, pairs: _*)

        (period match {
            case "d" ⇒ set0(C.DAY_OF_MONTH → v)
            case "w" ⇒ set0(
                C.DAY_OF_WEEK → FIRST_DAY_OF_WEEK,
                C.WEEK_OF_MONTH → v
            )
            case "m" ⇒ set0(
                C.MONTH → NUM_MONTH_MAP(v),
                C.DAY_OF_MONTH → 1
            )
            case "q" ⇒ set0(
                C.MONTH → QUARTERS_BEGIN(v),
                C.DAY_OF_MONTH → 1
            )
            case "y" ⇒ set0(
                C.YEAR → v,
                C.MONTH → C.JANUARY,
                C.DAY_OF_MONTH → 1
            )
            case "e" ⇒
                set0(
                    C.YEAR → (cal.get(C.YEAR) / 100 * 100 + (v - 1) * 10 + 1),
                    C.MONTH → C.JANUARY,
                    C.DAY_OF_YEAR → 1
                )
            case "c" ⇒ set0(
                C.YEAR → ((v - 1) * 100 + 1),
                C.MONTH → C.JANUARY,
                C.DAY_OF_MONTH → 1
            )
            case "dw" ⇒ set0(C.DAY_OF_WEEK → NUM_WEEK_DAYS_MAP(v))
            case "s" ⇒
                adjustWinter(v, cal)

                set0(
                    C.MONTH → SEASONS_BEGIN(v),
                    C.DAY_OF_MONTH → 1
                )
        }).getTimeInMillis
    }

    private def mkTo(period: String, from: Long): Long = {
        val cal = mkCalendar(Some(from))

        def add0(pairs: (Int, Int)*) = add(cal, pairs: _*)

        (period match {
            case "d" ⇒ add0(C.DAY_OF_YEAR → 1)
            case "w" ⇒ add0(C.WEEK_OF_YEAR → 1)
            case "m" ⇒ add0(C.MONTH → 1)
            case "q" ⇒ add0(C.MONTH → 3)
            case "y" ⇒ add0(C.YEAR → 1)
            case "e" ⇒ add0(C.YEAR → 10)
            case "c" ⇒ add0(C.YEAR → 100)
            case "dw" ⇒ add0(C.DAY_OF_YEAR → 1)
            case "s" ⇒ add0(C.MONTH → 3)
        }).getTimeInMillis
    }

    private def shift(d: Long, period: String, n: Int): Long = {
        val cal = mkCalendar(Some(d))

        def add0(pairs: (Int, Int)*) = add(cal, pairs: _*)

        (period match {
            case "d" ⇒ add0(C.DAY_OF_YEAR → n)
            case "w" ⇒ add0(C.WEEK_OF_YEAR → n)
            case "m" ⇒ add0(C.MONTH → n)
            case "q" ⇒ add0(C.MONTH → (3 * n))
            case "y" ⇒ add0(C.YEAR → n)
            case "e" ⇒ add0(C.YEAR → (10 * n))
            case "c" ⇒ add0(C.YEAR → (100 * n))
            case "dw" ⇒ add0(C.WEEK_OF_YEAR → n)
            case "s" ⇒ add0(C.YEAR → n)
        }).getTimeInMillis
    }

    private def parseInt(s: String): Option[Int] = if (!s.isEmpty) Some(s.toInt) else None

    private def isSign(ch: Char) = ch == '+' || ch == '-'

    private def isDigit(ch: Char) = ch.isDigit

    private def isLetter(ch: Char) = !isSign(ch) && !isDigit(ch)

    private def isSignOrDigit(ch: Char) = isSign(ch) || isDigit(ch)

    private def getShiftPeriod(period: String) =
        period match {
            case "m" | "q" | "s" ⇒ "y"
            case "dw" ⇒ "w"
            case _ ⇒ period //d, w, y, e, c
        }

    private[date] def calculatePart(fns: String, base: Long): PartResult = {
        var res = PartResult(base, base, "", Seq.empty[String])

        for (fn ← fns.split(",").map(_.trim)) {
            val resFrom = res.from

            def after(heads: String*): String = fn.drop(heads.map(_.length).sum)

            // Numeric period with optional shift.
            // 4d; 4d+2
            def periodRange(): PartResult = {
                val d1 = fn.takeWhile(isDigit)
                val period = after(d1).takeWhile(isLetter)
                val d2Opt = after(period, d1).takeWhile(isSignOrDigit)

                val from = mkFrom(period, parseInt(d1), resFrom)

                parseInt(d2Opt) match {
                    case Some(d2) ⇒
                        val to = mkTo(period, from)

                        val periodShift = getShiftPeriod(period)

                        PartResult(shift(from, periodShift, d2), shift(to, periodShift, d2), period, res.periods)
                    case None ⇒
                        val to = mkTo(period, from)

                        PartResult(from, to, period, res.periods)
                }
            }

            // Duration (2d etc) and current period (d etc) with optional shift.
            // m, m+2, m2 (not m2+2)
            def durationRange(): PartResult = {
                val period = fn.takeWhile(isLetter)
                val d1Opt = after(period).takeWhile(isDigit)
                val d2Opt = after(period).takeWhile(isSignOrDigit)

                parseInt(d1Opt) match {
                    // m2 (shift impossible)
                    case Some(d1) ⇒
                        val from = resFrom
                        val to = shift(from, period, d1)

                        PartResult(from, to, period, res.periods)
                    // m
                    case None ⇒
                        val from = mkFrom(period, None, resFrom)
                        val to = mkTo(period, from)

                        parseInt(d2Opt) match {
                            case Some(d2) ⇒
                                PartResult(shift(from, period, d2), shift(to, period, d2), period, res.periods)
                            case None ⇒ PartResult(from, to, period, res.periods)
                        }
                }
            }

            def mkDayResult(c: C): PartResult =
                PartResult(c.getTimeInMillis, add(c, C.DAY_OF_YEAR → 1).getTimeInMillis, "d", res.periods)

            def now(): PartResult = mkDayResult(mkCalendar(Some(base)))

            def lastDay(shift: C ⇒ C): PartResult = mkDayResult(shift(mkCalendar(Some(resFrom))))

            def ldw(): PartResult = lastDay((c: C) ⇒ set(c, C.DAY_OF_WEEK → LAST_DAY_OF_WEEK))
            def ldm(): PartResult = lastDay((c: C) ⇒ set(c, C.DAY_OF_MONTH → c.getActualMaximum(C.DAY_OF_MONTH)))
            def ldy(): PartResult = lastDay((c: C) ⇒ set(c, C.DAY_OF_YEAR → c.getActualMaximum(C.DAY_OF_YEAR)))

            def ldYears(years: Int): PartResult = lastDay((c: C) ⇒ {
                val curYear = c.get(C.YEAR)
                var shift = curYear % years

                if (shift != 0)
                    shift = years - shift

                // Should't be in one function call (last day is relative)
                set(c, C.YEAR → (curYear + shift))
                set(c, C.DAY_OF_YEAR → c.getActualMaximum(C.DAY_OF_YEAR))
            })

            def lde(): PartResult = ldYears(10)
            def ldc(): PartResult = ldYears(100)

            def ld3M(map3m: Map[Int, Int]): PartResult = lastDay((c: C) ⇒ {
                val n = map3m(MONTH_NUM_MAP(c.get(C.MONTH)))

                // Should't be in one function call (last day is relative)
                // Note that keys in `map3m` sorted.
                set(c, C.MONTH → NUM_MONTH_MAP(map3m.filter(_._2 == n).keys.toSeq.max))
                set(c, C.DAY_OF_MONTH → c.getActualMaximum(C.DAY_OF_MONTH))
            })

            def ldq(): PartResult = ld3M(QUARTERS)
            def lds(): PartResult = ld3M(SEASONS)

            res = fn match {
                case "now" ⇒ now()

                case "$dw" ⇒ ldw()
                case "$dm" ⇒ ldm()
                case "$dq" ⇒ ldq()
                case "$dy" ⇒ ldy()
                case "$de" ⇒ lde()
                case "$dc" ⇒ ldc()
                case "$ds" ⇒ lds()

                case _ if fn.head.isDigit ⇒ periodRange() // 4d; 4d+2

                case _ ⇒ durationRange() // m, m+2, m2 (not m2+2)
            }
        }

        res
    }

    // Creates date based on 'base' with year from 'yd'
    private def changeYear(base: Long, yd: Long): Long = {
        val c = mkCalendar(Some(yd))

        val y = c.get(C.YEAR)

        c.setTimeInMillis(base)
        c.set(C.YEAR, y)

        c.getTimeInMillis
    }

    private def mkFrom(range: PartResult, incl: Boolean): Long = if (incl) range.from else range.to
    private def mkTo(range: PartResult, incl: Boolean): Long = if (incl) range.to else range.from

    case class PartResult(from: Long, to: Long, period: String, parents: Seq[String]) {
        def samePeriods(ps: String): Boolean = periods.mkString.sorted == ps.sorted
        def periods: Seq[String] = if (period.isEmpty) parents else parents :+ period
    }
}