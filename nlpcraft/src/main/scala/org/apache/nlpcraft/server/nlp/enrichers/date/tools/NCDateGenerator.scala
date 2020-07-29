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

package org.apache.nlpcraft.server.nlp.enrichers.date.tools

import java.text.{DateFormat, SimpleDateFormat}
import java.util.{Date, Locale, Calendar ⇒ C}

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.nlp.numeric.NCNumericGenerator
import org.apache.nlpcraft.server.nlp.enrichers.date.NCDateConstants._
import org.apache.nlpcraft.server.nlp.enrichers.date.NCDateFormatType._

import scala.collection._
import scala.collection.mutable.{LinkedHashMap ⇒ LHM}
import NCDateGenerator._

/**
 * Pre-built date ranges generator.
 */
object NCDateGenerator {
    // For english names generation.
    Locale.setDefault(Locale.forLanguageTag("EN"))

    private[date] val FMT_DATES_COMMON: Seq[SimpleDateFormat] =
        mkFormatters(YEAR_MONTH_DAY_COMMON_1, dupD = true, dupM = false) ++
        mkFormatters(YEAR_MONTH_DAY_COMMON_2, dupD = true, dupM = false)

    private[date] val FMT_DAYS_YEAR_COMMON: Seq[SimpleDateFormat] =
        mkFormatters(MONTH_DAY_COMMON_1, dupD = true, dupM = false) ++
        mkFormatters(MONTH_DAY_COMMON_2, dupD = true, dupM = false)

    private[date] val FMT_YEARS = YEAR.map(new SimpleDateFormat(_))

    private val FMT_DAY = new SimpleDateFormat("d")

    private[date] val FMT_DATES_DIGITS = mkDigitFormatters(YEAR_MONTH_DAY_COMMON_1 ++ YEAR_MONTH_DAY_COMMON_2)
    private[date] val FMT_DAYS_YEAR_DIGITS = mkDigitFormatters(MONTH_DAY_COMMON_1 ++ MONTH_DAY_COMMON_2)

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

    private def mkTime(field: Int, value: Int, fmt: DateFormat): String = {
        val c = C.getInstance()

        c.set(field, value)

        fmt.format(c.getTime).toLowerCase
    }

    private def month(n: Int): String = mkTime(C.MONTH, n, new SimpleDateFormat("MMMM")).toLowerCase
    private def dayOfWeek(n: Int): String = mkTime(C.DAY_OF_WEEK, n, new SimpleDateFormat("EEEE")).toLowerCase

    private def shortYear(y: Int): String = String.valueOf(y).takeRight(2)
    private def shortMonth(m: String): String = m.take(3)
    private def shortDayOfWeek(dw: String): String = dw.take(3)

    private def mkLast(df: LHM_SS) = df.map(p ⇒ p._1 → s"${p._2}-1")
    private def mkNext(df: LHM_SS) = df.map(p ⇒ p._1 → s"${p._2}+1")

    private def toNow(s: String): String = s"$s:now"
    private def to(s: String): String = s":$s"

    private def zipIndexes[T](seq: Seq[T]): Map[T, Int] = seq.zipWithIndex.map(p ⇒ p._1 → (p._2 + 1)).toMap

    private def format(d: Date, fs: Seq[SimpleDateFormat]): Seq[String] = fs.map(_.format(d).toLowerCase)

    private[date] def format(d: Date, fsMap: Map[String, Seq[SimpleDateFormat]]): Seq[String] = {
        val day = FMT_DAY.format(d).last

        val fs = day match {
            case '1' | '2' | '3' ⇒ fsMap(day.toString)
            case _ ⇒ fsMap("other")
        }

        format(d, fs)
    }

    private val NUM_MONTH_MAP = zipIndexes(CAL_MONTHS)
    private val MMMM_MONTH_SEQ = CAL_MONTHS.map(month)
    private val YEARS_SEQ = for (i ← 1900 to C.getInstance().get(C.YEAR) + 5) yield i
    private val MMMM_MONTH_MAP = zipIndexes(MMMM_MONTH_SEQ)

    // USA week.
    private val WEEK_DAYS_SEQ = Seq(
        dayOfWeek(C.SUNDAY),
        dayOfWeek(C.MONDAY),
        dayOfWeek(C.TUESDAY),
        dayOfWeek(C.WEDNESDAY),
        dayOfWeek(C.THURSDAY),
        dayOfWeek(C.FRIDAY),
        dayOfWeek(C.SATURDAY)
    )

    private def x(end: String, seqs: Seq[String]*): LHM[String, String] =
        LHM(seqs.flatMap(s ⇒ s.zipWithIndex.map(t ⇒ t._1 → s"${t._2 + 1}$end")): _*)

    private val WEEK_DAYS: LHM_SS = x("dw", WEEK_DAYS_SEQ, WEEK_DAYS_SEQ.map(shortDayOfWeek))
    private val MONTHS: LHM_SS = x("m", MMMM_MONTH_SEQ, MMMM_MONTH_SEQ.map(shortMonth))

    private val PLURALS = Map(
        "day" → "days",
        "night" → "nights",
        "day and night" → "days and nights",
        "night and day" → "nights and days",
        "week" → "weeks",
        "month" → "months",
        "quarter" → "quarters",
        "year" → "years",
        "decade" → "decades",
        "century" → "centuries"
    )

    private val SEASONS: LHM_SS = LHM(
        "winter" → "1s",
        "spring" → "2s",
        "summer" → "3s",
        "fall" → "4s",
        "autumn" → "4s"
    )

    private val PERIODS: LHM_SS = LHM(
        "day" → "d",
        "night" → "d",
        "day and night" → "d",
        "night and day" → "d",
        "week" → "w",
        "month" → "m",
        "quarter" → "q",
        "year" → "y",
        "decade" → "e",
        "century" → "c"
    )

    private val REL_TODAY = LHM(
        "today" → "d",
        "yesterday" → "d-1",
        "before yesterday" → "d-2",
        "day before yesterday" → "d-2",
        "tomorrow" → "d+1",
        "day after tomorrow" → "d+2"
    )

    private val DIGITS = LHM() ++ NCNumericGenerator.generate(365)

    // Generators.
    private def mkMonths(y: Int, m: String, fmts: Seq[SimpleDateFormat]): Seq[String] = {
        val c = C.getInstance()

        c.set(C.YEAR, y)
        c.set(C.MONTH, CAL_MONTHS(MMMM_MONTH_MAP(m) - 1))

        val d = c.getTime

        fmts.map(_.format(d).toLowerCase)
    }

    private def mkFormatters(fs: Seq[String], dupD: Boolean, dupM: Boolean): Seq[SimpleDateFormat] = {
        var res = fs

        if (dupD) res ++= res.map(_.replaceAll("dd", "d"))
        if (dupM) res ++= res.filter(m ⇒ m.contains("MM") && !m.contains("MMM")).map(_.replaceAll("MM", "M"))

        res.map(new SimpleDateFormat(_))
    }

    private def mkDigitFormatter(tpl: String, sfx: String) =
        new SimpleDateFormat(tpl.replaceAll("dd", s"d'$sfx'"))

    private def mkDigitFormatters(fs: Seq[String]): Map[String, Seq[SimpleDateFormat]] =
        Map(
            "1" → fs.map(mkDigitFormatter(_, "st")),
            "2" → fs.map(mkDigitFormatter(_, "nd")),
            "3" → fs.map(mkDigitFormatter(_, "rd")),
            "other" → fs.map(mkDigitFormatter(_, "th"))
        )

    // Special case. Cannot be formatted using SimpleDateFormatter.

    // {winter} of {1995...2014}
    // {winter} {1995...2014}
    // {winter} of {95...14}
    // {winter} {95...14}
    // {winter}{95...14}
    private def mkSeasons(y: Int, s: String) = {
        val sy = shortYear(y)

        Seq(
            s"$s of $y",
            s"$s $y",
            s"$s of $sy",
            s"$s $sy",
            s"$s$sy"
        )
    }

    case class DayHolder(date: Date, y: Int, m: Int, d: Int)

    // fromY - inclusive; toY - exclusive.
    private def mkDays(fromY: Int, toY: Int): Seq[DayHolder] = {
        val c = C.getInstance()

        c.set(C.YEAR, fromY)
        c.set(C.MONTH, C.JANUARY)
        c.set(C.DAY_OF_MONTH, 1)

        val res = mutable.Buffer.empty[DayHolder]

        while (c.get(C.YEAR) != toY) {
            res += DayHolder(
                c.getTime,
                c.get(C.YEAR),
                getNumMonth(c),
                c.get(C.DAY_OF_MONTH))

            c.add(C.DAY_OF_YEAR, 1)
        }

        res
    }

    private def getNumMonth(c: C): Int = NUM_MONTH_MAP(c.get(C.MONTH))

    //toNow(s"${sh}-${n._2}"
    private def mkNumericPeriodLast(m: Int, period: String, periodShort: String, useSimplePeriod: Boolean): Map[String, String] = {
        def mkLastOne: String = s"$periodShort-1:$periodShort-1"
        def mkLastNToNow(n: Int): String = toNow(s"$periodShort-$n")

        def make(plural: String, isMisspelling: Boolean): Map[String, String] = {
            var p1 = LHM(
                s"couple of $plural" → mkLastNToNow(2),
                s"couple of last $plural" → mkLastNToNow(2),
                s"couple last $plural" → mkLastNToNow(2),

                s"few $plural" → mkLastNToNow(3),
                s"few of last $plural" → mkLastNToNow(3),
                s"few last $plural" → mkLastNToNow(3),
                s"last few $plural" → mkLastNToNow(3),
                s"last of few $plural" → mkLastNToNow(3),

                s"several $plural" → mkLastNToNow(4),
                s"several of last $plural" → mkLastNToNow(4),
                s"several last $plural" → mkLastNToNow(4)
            )

            if (!isMisspelling)
                p1 += s"last $plural" → mkLastNToNow(3)

            val p2 = (0 to m).flatMap {
                case 0 ⇒ if (useSimplePeriod) Seq(s"$period" → mkLastOne) else Seq.empty
                case i@1 ⇒ Seq(
                    s"1 $period" → mkLastOne,
                    s"$i $plural" → mkLastOne
                )
                case i ⇒ Seq(s"$i $plural" → mkLastNToNow(i - 1))
            }

            val p3 = (1 to m).flatMap(i ⇒
                DIGITS.get(i) match {
                    case Some(v) ⇒
                        i match {
                            case 1 ⇒ Some(s"$v $period" → mkLastOne)
                            case _ ⇒ Some(s"$v $plural" → mkLastNToNow(i - 1))
                        }
                    case None ⇒ None
                }
            )

            p1 ++ p2 ++ p3
        }

        val ph = mkPlural(period)

        make(ph.word, isMisspelling = false) ++ make(ph.misspelling, isMisspelling = true)
    }

    // Prepares plurals for period, which contains 2 values:
    // valid plural value and invalid which equal to period's singular.
    // It used for misspelling while plurals using.
    // Example: 'data for two last months' and 'data for last two month'.
    // Both of them should be processed same way despite of last is incorrect.
    case class PluralHolder(word: String, misspelling: String)

    private def mkPlural(period: String): PluralHolder = PluralHolder(PLURALS(period), period)

    private def mkNumericPeriod(
        m: Int, period: String, periodShort: String, prevOpt: Option[String], mkPeriod: (Int, Int) ⇒ String
    ): Map[String, String] = {

        def make(plural: String): Map[String, String] = {
            val p1 =
                prevOpt match {
                    case Some(prev) ⇒
                        LHM(
                            s"$prev couple of $plural" → mkPeriod(2, 1),
                            s"$prev few $plural" → mkPeriod(4, 1),
                            s"$prev several $plural" → mkPeriod(5, 1),

                            s"couple of $prev $plural" → mkPeriod(2, 1),
                            s"few $prev $plural" → mkPeriod(4, 1),
                            s"several $prev $plural" → mkPeriod(5, 1)
                        )
                    case None ⇒
                        LHM(
                            s"couple of $plural" → mkPeriod(2, 1),
                            s"few $plural" → mkPeriod(4, 1),
                            s"several $plural" → mkPeriod(5, 1)
                        )
                }
            val p2 = (0 to m).map {
                case 0 ⇒ s"$period" → mkPeriod(1, 1)
                case 1 ⇒ s"1 $period" → mkPeriod(1, 1)
                case i ⇒ s"$i $plural" → mkPeriod(i, 1)
            }

            val p3 = (1 to m).flatMap(i ⇒
                DIGITS.get(i) match {
                    case Some(v) ⇒
                        i match {
                            case 1 ⇒ Some(s"$v $period" → mkPeriod(1, 1))
                            case _ ⇒ Some(s"$v $plural" → mkPeriod(i, 1))
                        }
                    case None ⇒ None
                }
            )

            p1 ++ p2 ++ p3
        }

        val ph = mkPlural(period)

        make(ph.word) ++ make(ph.misspelling)
    }

    private def mkNumericPeriodPrev(m: Int, period: String, periodShort: String, prevOpt:
        Option[String]): Map[String, String] =
        mkNumericPeriod(m, period, periodShort, prevOpt, (n1: Int, n2: Int) ⇒ s"$periodShort-$n1:$periodShort-$n2")

    private def mkNumericPeriodNext(m: Int, period: String, periodShort: String, prevOpt: Option[String]):
        Map[String, String] =
        mkNumericPeriod(m, period, periodShort, prevOpt, (n1: Int, n2: Int) ⇒ s"$periodShort+$n2:$periodShort+$n1")

    private def mkYears(y: Int) = {
        val c = C.getInstance()

        c.set(C.YEAR, y)

        val d = c.getTime

        FMT_YEARS.map(_.format(d).toLowerCase)
    }

    private[date] type LHM_SS = LHM[String, String]

    private[date] def relativeDays(df: LHM_SS): Unit = for (p ← REL_TODAY) df += p._1 → p._2

    private[date] def periods(df: LHM_SS): Unit = {
        // Default.
        for (p ← MONTHS ++ SEASONS ++ WEEK_DAYS)
            df += p._1 → p._2
        // Last.
        val m: LHM_SS = new LHM_SS()

        m ++= PERIODS ++ MONTHS ++ SEASONS ++ WEEK_DAYS

        for (l ← LAST ++ PREVIOUS; p ← mkLast(m))  df += s"$l ${p._1}" → p._2
        for (l ← NEXT; p ← mkNext(m))  df += s"$l ${p._1}" → p._2

        // Current.
        for (c ← CURRENT; p ← PERIODS ++ MONTHS ++ SEASONS ++ WEEK_DAYS)
            df += s"$c ${p._1}" → p._2
        // Current.
        for (p ← PERIODS ++ MONTHS ++ SEASONS ++ WEEK_DAYS)
            df += p._1 → p._2
    }

    private[date] def years(df: LHM_SS): Unit =
        for (y ← YEARS_SEQ)
            mkYears(y).foreach(s ⇒ df += s"$s" → s"${y}y")

    private[date] def months(df: LHM_SS, fmts: Seq[SimpleDateFormat]): Unit = {
        for (y ← YEARS_SEQ; m ← MMMM_MONTH_SEQ)
            mkMonths(y, m, fmts).foreach(s ⇒ df += s"$s" → s"${y}y, ${MMMM_MONTH_MAP(m)}m")

        def add(df: LHM_SS, m: String, p: String, v: String): Unit = {
            df += s"$m of $p year" → v
            df += s"month $m of $p year" → v
            df += s"month of $m of $p year" → v
            df += s"${shortMonth(m)} of $p year" → v
            df += s"month ${shortMonth(m)} of $p year" → v
            df += s"month of ${shortMonth(m)} of $p year" → v
        }

        for (m ← MMMM_MONTH_SEQ; p ← PREVIOUS ++ LAST) add(df, m, p, s"${MMMM_MONTH_MAP(m)}m-1")
        for (m ← MMMM_MONTH_SEQ; c ← CURRENT) add(df, m, c, s"${MMMM_MONTH_MAP(m)}m")
        for (m ← MMMM_MONTH_SEQ; p ← NEXT) add(df, m, p, s"${MMMM_MONTH_MAP(m)}m+1")
    }

    private[date] def seasons(df: LHM_SS): Unit =
        for (y ← YEARS_SEQ; s ← SEASONS)
            mkSeasons(y, s._1).foreach(p ⇒ df += s"$p" → s"${y}y, ${s._2}")

    private[date] def days(df: LHM_SS, fmts: Seq[SimpleDateFormat]): Unit = {
        val days = mkDays(YEARS_SEQ.last, YEARS_SEQ.last + 1)

        // Dates with two digits day of months will be duplicated with these formatters set,
        // but they will be skipped because added to the map.
        for (d ← days)
            format(d.date, fmts).foreach(s ⇒ df += s → s"${d.m}m, ${d.d}d")

        for (d ← days)
            format(d.date, FMT_DAYS_YEAR_DIGITS).foreach(s ⇒ df += s → s"${d.m}m, ${d.d}d")
    }

    private[date] def dates(df: LHM_SS, fmts: Seq[SimpleDateFormat]): Unit = {
        val days = mkDays(YEARS_SEQ.head, YEARS_SEQ.last + 1)

        // Dates with two digits day of months will be duplicated with these formatters set,
        // but they will be skipped because added to the map.
        for (d ← days)
            format(d.date, fmts).foreach(s ⇒ df += s → s"${d.y}y, ${d.m}m, ${d.d}d")

        for (d ← days)
            format(d.date, FMT_DATES_DIGITS).foreach(s ⇒ df += s → s"${d.y}y, ${d.m}m, ${d.d}d")
    }

    private[date] def simpleYears(df: LHM_SS): Unit = {
        // Between.
        for (b ← BETWEEN_INCLUSIVE; y1 ← YEARS_SEQ; y2 ← YEARS_SEQ if y2 > y1)
            df += s"${b._1} $y1 ${b._2} $y2" → s"${y1}y:${y2}y"
        for (b ← BETWEEN_EXCLUSIVE; y1 ← YEARS_SEQ; y2 ← YEARS_SEQ if y2 > y1)
            df += s"${b._1} $y1 ${b._2} $y2" → s"${y1}y:${y2-1}y"

        // From.
        for (f ← FROM; y ← YEARS_SEQ) df += s"$f $y" → toNow(s"${y}y")

        // Till.
        for (t ← TO; y ← YEARS_SEQ) df += s"$t $y" → to(s"${y}y")
    }

    private[date] def simpleQuarters(df: LHM_SS): Unit = {
        val qs = Seq(1, 2, 3, 4)

        val prefixes = qs.map(q ⇒ {
            val seq = q match {
                case 1 ⇒ Seq("1", "1st", "first")
                case 2 ⇒ Seq("2", "2nd", "second")
                case 3 ⇒ Seq("3", "3rd", "third")
                case 4 ⇒ Seq("4", "4th", "fourth", "last")

                case _  ⇒ throw new AssertionError();
            }

            q → seq
        }).toMap

        for (q ← qs)
            prefixes(q).foreach(p ⇒ df += s"$p quarter" → s"${q}q")

        for (y ← YEARS_SEQ.map(_.toString); q ← qs) {
            val yShort = y.drop(2)

            val dt = s"${y}y, ${q}q"

            // 'Q12014'
            df += s"q$q$y" → dt

            // 'Q114'
            df += s"q$q$yShort" → dt

            prefixes(q).foreach(p ⇒ {
                // '1st quarter of 2014' or 'first quarter of 2014' (with and without 'year' and 'of')
                df += s"$p quarter of $y" → dt
                df += s"$p quarter of $y year" → dt
                df += s"$p quarter of year $y" → dt
                df += s"$p quarter $y" → dt
                df += s"$p quarter $y year" → dt
                df += s"$p quarter year $y" → dt

                // '1st quarter of 14' or 'first quarter of 14' (with and without 'year' and 'of')
                df += s"$p quarter of $yShort" → dt
                df += s"$p quarter of $yShort year" → dt
                df += s"$p quarter of year $yShort" → dt
                df += s"$p quarter $yShort" → dt
                df += s"$p quarter $yShort year" → dt
                df += s"$p quarter year $yShort" → dt

                // '2005 1st quarter' or '2005 first quarter' (with and without 'year')
                df += s"$y $p quarter" → dt
                df += s"$y year $p quarter" → dt
                df += s"year $y $p quarter" → dt

                // '05 1st quarter' or '05 first quarter' (with and without 'year')
                // (Note that '5 1st quarter' or '5 first quarter' not added.)
                df += s"$yShort $p quarter" → dt
                df += s"$yShort year $p quarter" → dt
                df += s"year $yShort $p quarter" → dt
            })
        }
    }

    private def duration(df: LHM_SS, qty: Int, period: String, sh: String): Unit = {
        // For+last - for last 6 days (including now)
        for (f ← FOR; l ← LAST; n ← mkNumericPeriodLast(qty, period, sh, useSimplePeriod = true))
             df += s"$f $l ${n._1}" → n._2
        // Last 6 days (including now)
        for (l ← LAST; n ← mkNumericPeriodLast(qty, period, sh, useSimplePeriod = true))
            df += s"$l ${n._1}" → n._2

        // For 6 days - skipping 'last' (including now)
        for (f ← FOR; n ← mkNumericPeriodLast(qty, period, sh, useSimplePeriod = true))
            df += s"$f ${n._1}" → n._2

        // 6 days - skipping 'for' and 'last' (including now)
        for (n ← mkNumericPeriodLast(qty, period, sh, useSimplePeriod = false))
            df += s"${n._1}" → n._2

        // For+previous - for last 6 days (excluding now)
        for (f ← FOR; l ← PREVIOUS; n ← mkNumericPeriodPrev(qty, period, sh, Some(l)))
            df += s"$f ${n._1}" → n._2
        // Previous 6 days (excluding now) (without for-previous)
        for (l ← PREVIOUS; n ← mkNumericPeriodPrev(qty, period, sh, Some(l)))
            df += n._1 → n._2

        // for+ago - 6 days ago
        for (f ← FOR; a ← AGO; n ← mkNumericPeriodPrev(qty, period, sh, None))
            df += s"$f ${n._1} $a" → n._2

        // 6 days ago
        for (a ← AGO; n ← mkNumericPeriodPrev(qty, period, sh, None))
            df += s"${n._1} $a" → n._2

        // for+next - 6 days forward
        for (f ← FOR; a ← FORWARD; n ← mkNumericPeriodNext(qty, period, sh, None))
            df += s"$f ${n._1} $a" → n._2

        // 6 days forward
        for (f ← FORWARD; n ← mkNumericPeriodNext(qty, period, sh, None))
            df += s"$f ${n._1}" → n._2

        // For+next - for next 6 days (excluding now)
        for (f ← FOR; l ← NEXT; n ← mkNumericPeriodNext(qty, period, sh, Some(l)))
            df += s"$f ${n._1}" → n._2
        // Next 6 days (excluding now) (without for-previous)
        for (l ← NEXT; n ← mkNumericPeriodNext(qty, period, sh, Some(l)))
            df += n._1 → n._2
    }

    // for {last|previous|...} {couple|few|several|1...365} day{s}
    private[date] def durationDays(df: LHM_SS): Unit = {
        duration(df, 365, "day", "d")
        duration(df, 365, "night", "d")
        duration(df, 365, "day and night", "d")
        duration(df, 365, "night and day", "d")
    }

    // for {last|previous|...} {couple|few|several|1...52} week{s}
    private[date] def durationWeeks(df: LHM_SS): Unit = duration(df, 52, "week", "w")

    // for {last|previous|...} {couple|few|several|1...60} month{s}
    private[date] def durationMonths(df: LHM_SS): Unit = duration(df, 60, "month", "m")

    // for {last|previous|...} {couple|few|several|1...20} quarter{s}
    private[date] def durationQuarters(df: LHM_SS): Unit = duration(df, 20, "quarter", "q")

    // for {last|previous|...} {couple|few|several|1...20} year{s}
    private[date] def durationYears(df: LHM_SS): Unit = duration(df, 20, "year", "y")

    // for {last|previous|...} {couple|few|several|1...2} decade{s}
    private[date] def durationDecades(df: LHM_SS): Unit = duration(df, 2, "decade", "e")

    // for {last|previous|...} {couple|few|several|1...2} centur{y|ies}
    private[date] def durationCenturies(df: LHM_SS): Unit = duration(df, 2, "century", "c")

    //(first...|last....) (N)? PERIOD(s)? of [last|previous...|this|current...|next]? PARENT
    // PERIOD  - N  - PARENT
    // day     - 7   - week
    // day     - 30  - month
    // week    - 4   - month
    // week    - 12  - quarter
    // week    - 52  - year
    // month   - 12  - year
    // quarter - 4   - year
    // year    - 10  - decade
    // Examples:
    // First week of quarter
    // First 1 week of last quarter
    // Last 4 weeks of this quarter
    // Last 4 weeks of quarter

    // case "m" | "q" | "s" ⇒ "y"
    // case "dw" ⇒ "w"
    // case _ ⇒ period //d, w, y, e, c
    private[date] def relativeDuration(df: LHM_SS, n: Int, period: String, parent: String) {
        case class PeriodHolder(text: String, count: Int)

        val hs = (1 to n).flatMap {
            case i@1 ⇒ Seq(PeriodHolder(period, 1), PeriodHolder(s"$i $period", i))
            case i ⇒
                val ph = mkPlural(period)

                def make(plural: String): PeriodHolder = PeriodHolder(s"$i $plural", i)

                Seq(make(ph.word), make(ph.misspelling))
        }

        val p = PERIODS(period)
        val pp = PERIODS(parent)

        def mkText(prep: String, h: PeriodHolder, adjOpt: Option[String]) =
            adjOpt match {
                case Some(adj) ⇒ s"$prep ${h.text} of $adj $parent"
                case None ⇒ s"$prep ${h.text} of $parent"
            }

        def mkShift(increment: Int): String =
            increment match {
                case 0 ⇒ pp
                case _ if increment < 0 ⇒ s"$pp$increment"
                case _ ⇒ s"$pp+$increment"
            }

        def mkFormula1(h: PeriodHolder, increment: Int): String = {
            val x1 = mkShift(increment)
            val x2 = p match {
                case "w" ⇒ s"1dw, $p${h.count}"
                case _ ⇒ s"$p${h.count}"
            }

            s"$x1, $x2"
        }

        def mkFormula2(h: PeriodHolder, increment: Int): String = {
            val x1 = mkShift(increment)
            val x2 = p match {
                case "w" ⇒
                    val lastDay =
                        parent match {
                            case "month" ⇒ "$dm"
                            case "quarter" ⇒ "$dq"
                            case "year" ⇒ "$dy"

                            case _ ⇒ throw new AssertionError(s"Unexpected parent for week: $parent")
                        }

                    if (h.count > 1) s"$lastDay, d-${(h.count - 1) * 7}, 1dw" else s"$lastDay, 1dw"
                case _ ⇒ s"$p-${h.count}"
            }
            val x3 = s"$p${h.count}"

            s"$x1, $x2, $x3"
        }

        // First week of previous months.
        for (prep ← FIRST; h ← hs; adj ← LAST ++ PREVIOUS)
            df += mkText(prep, h, Some(adj)) → mkFormula1(h, increment = -1)

        // First week of this month.
        for (prep ← FIRST; h ← hs; adj ← CURRENT)
            df += mkText(prep, h, Some(adj)) → mkFormula1(h, increment = 0)

        // First week of month.
        for (prep ← FIRST; h ← hs)
            df += mkText(prep, h, None) → mkFormula1(h, increment = 0)

        // First week of next month.
        for (prep ← FIRST; h ← hs; adj ← NEXT)
            df += mkText(prep, h, Some(adj)) → mkFormula1(h, increment = 1)

        def mkIncrement4Last(i: Int): Int = if (p != "w") i else i - 1

        // Last week of last month.
        for (prep ← LAST; h ← hs; adj ← LAST ++ PREVIOUS)
            df += mkText(prep, h, Some(adj)) → mkFormula2(h, mkIncrement4Last(0))

        // Last week of this month.
        for (prep ← LAST; h ← hs; adj ← CURRENT)
            df += mkText(prep, h, Some(adj)) → mkFormula2(h, mkIncrement4Last(1))

        // Last week of month.
        for (prep ← LAST; h ← hs)
            df += mkText(prep, h, None) → mkFormula2(h, mkIncrement4Last(1))

        // Last week of next month.
        for (prep ← LAST; h ← hs; adj ← NEXT)
            df += mkText(prep, h, Some(adj)) → mkFormula2(h, mkIncrement4Last(2))
    }

    private[date] def relativeDaysOfWeekByName(df: LHM_SS): Unit = {
        def mkTemplate(day: String, dayNum: String, adj: String, increment: Int): Unit = {
            val s = increment match {
                case 0 ⇒ s"$day of $adj week" → dayNum
                case _ if increment < 0 ⇒ s"$day of $adj week" → s"w$increment, $dayNum"
                case _ ⇒ s"$day of $adj week" → s"w+$increment, $dayNum"
            }

            df += s
        }

        for (adj ← LAST ++ PREVIOUS; d ← WEEK_DAYS) mkTemplate(d._1, d._2, adj, increment = -1)
        for (adj ← CURRENT; d ← WEEK_DAYS) mkTemplate(d._1, d._2, adj, increment = 0)
        for (adj ← NEXT; d ← WEEK_DAYS) mkTemplate(d._1, d._2, adj, increment = 1)
    }

    private[date] def relativeDaysOfWeekByNum(df: LHM_SS): Unit = relativeDuration(df, 7, "day", "week")
    private[date] def relativeWeeksOfQuarter(df: LHM_SS): Unit = relativeDuration(df, 12, "week", "quarter")
    private[date] def relativeDaysOfMonth(df: LHM_SS): Unit = relativeDuration(df, 30, "day", "month")
    private[date] def relativeWeeksOfMonth(df: LHM_SS): Unit = relativeDuration(df, 4, "week", "month")
    private[date] def relativeWeeksOfYear(df: LHM_SS): Unit = relativeDuration(df, 52, "week", "year")
    private[date] def relativeMonthsOfYear(df: LHM_SS): Unit = relativeDuration(df, 12, "month", "year")
    private[date] def relativeQuartersOfYear(df: LHM_SS): Unit = relativeDuration(df, 4, "quarter", "year")
    private[date] def relativeYearOfDecade(df: LHM_SS): Unit = relativeDuration(df, 10, "year", "decade")

    private[date] def relativeFromYear(df: LHM_SS): Unit = {
        // {first|last} XX {days|weeks|months|quarters} of YYYY
        case class Holder(daysCount: Int, symbol: String, shiftBack: (C, Int) ⇒ Unit)

        val m = Map(
            "days" → Holder(364, "d", (c: C, i: Int) ⇒ c.add(C.DAY_OF_YEAR, -i)),
            "weeks" → Holder(52, "w", (c: C, i: Int) ⇒ c.add(C.WEEK_OF_YEAR, -i)),
            "months" → Holder(11, "m", (c: C, i: Int) ⇒ c.add(C.MONTH, -i)),
            "quarters" → Holder(3, "q", (c: C, i: Int) ⇒ c.add(C.MONTH, -3 * i))
        )

        val c = C.getInstance()

        for (y ← YEARS_SEQ; (period, h) ← m; i ← 1 to h.daysCount) {
            val yStr = y.toString

            // Full and short years.
            val ys = Seq(yStr, yStr.drop(2))

            // Correct and misspellings
            // `first 2 day of 2005` instead of `first 2 days of 2005` and
            // `first 1 days of 2005` instead of `first 1 day of 2005` and
            val ps = Seq(period, period.take(period.length - 1))

            c.set(C.YEAR, y + 1)
            c.set(C.MONTH, C.JANUARY)
            c.set(C.DAY_OF_MONTH, 1)

            h.shiftBack(c, i)

            val vFirst = s"${y}y, 1m, 1d:${y}y, $i${h.symbol}"
            val vLast = s"${c.get(C.YEAR)}y, ${getNumMonth(c)}m, ${c.get(C.DAY_OF_MONTH)}d:${y}y, 12m, 31d"

            for (y ← ys; p ← ps) {
                df += s"first $i $p of $y" → vFirst
                df += s"$y first $i $p" → vFirst

                df += s"last $i $p of $y" → vLast
                df += s"$y last $i $p" → vLast
            }
        }
    }

    def generateFull(): LHM_SS = {
        val dm = new LHM_SS()

        simpleYears(dm)
        simpleQuarters(dm)

        durationDays(dm)
        durationWeeks(dm)
        durationMonths(dm)
        durationQuarters(dm)
        durationYears(dm)
        durationDecades(dm)
        durationCenturies(dm)

        relativeDaysOfWeekByName(dm)
        relativeDaysOfWeekByNum(dm)
        relativeDaysOfMonth(dm)
        relativeWeeksOfMonth(dm)
        relativeWeeksOfQuarter(dm)
        relativeWeeksOfYear(dm)
        relativeMonthsOfYear(dm)
        relativeQuartersOfYear(dm)
        relativeYearOfDecade(dm)

        relativeFromYear(dm)

        dm
    }

    def generateParts(): LHM_SS = {
        val dm = new LHM_SS()

        relativeDays(dm)
        periods(dm)
        years(dm)
        months(dm, MONTH_YEAR_COMMON.map(new SimpleDateFormat(_)))
        seasons(dm)
        days(dm, FMT_DAYS_YEAR_COMMON)
        dates(dm, FMT_DATES_COMMON)

        dm
    }

    def generateParts(typ: NCDateFormatType): LHM_SS = {
        val dm = new LHM_SS()

        months(dm, MONTH_YEAR_COUNTRY(typ).map(new SimpleDateFormat(_)))
        days(dm, mkFormatters(MONTH_DAY_COUNTRY(typ), dupD = false, dupM = false))
        dates(dm, mkFormatters(YEAR_MONTH_DAY_COUNTRY(typ), dupD = true, dupM = true))

        dm
    }
}

object DLDateGeneratorRunner extends App {
    private def mkPath(path: String): String = U.mkPath(s"nlpcraft/src/main/resources/date/$path")
    private def convert(entry: (String, String)): String = s"${entry._1} | ${entry._2}"

    private def process(): Unit = {
        val fileFull = mkPath("full.txt")
        val fileParts = mkPath("parts.txt")
        val filePartsDmy = mkPath("parts_dmy.txt")
        val filePartsMdy = mkPath("parts_mdy.txt")
        val filePartsYmd = mkPath("parts_ymd.txt")

        U.mkTextFile(fileFull, generateFull().map(convert))
        U.mkTextFile(fileParts, generateParts().map(convert))
        U.mkTextFile(filePartsDmy, generateParts(DMY).map(convert))
        U.mkTextFile(filePartsMdy, generateParts(MDY).map(convert))
        U.mkTextFile(filePartsYmd, generateParts(YMD).map(convert))

        Seq(fileFull, fileParts, filePartsDmy, filePartsMdy, filePartsYmd).foreach(f ⇒ U.gzipPath(f))
    }

    process()
}