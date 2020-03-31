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

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, Locale}

import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.server.nlp.enrichers.date.NCDateConstants._
import org.apache.nlpcraft.server.nlp.enrichers.date.NCDateFormatType._
import org.apache.nlpcraft.server.nlp.enrichers.date.tools.NCDateGenerator.LHM_SS
import org.apache.nlpcraft.server.nlp.enrichers.date.{NCDateParser, NCDateRange}
import org.junit.jupiter.api.Assertions.{assertTrue, fail}
import org.junit.jupiter.api.{Disabled, Test}

import scala.collection.JavaConverters._
import scala.collection._
import scala.collection.mutable.{LinkedHashMap ⇒ LHM}
import scala.language.implicitConversions

/**
 * Tests for dates templates generators.
 * Excluded from suite because added for manual testing.
 */
@Disabled
class NCDateGeneratorSpec  {
    private def print(
        f: LHM[String, String] ⇒ Unit, cnt: Int = 500, asc: Boolean = true, keys2Check: Option[Seq[String]] = None
    ) {
        val base = scala.compat.Platform.currentTime

        val map = new LHM[String, String]

        f(map)

        val tbl = NCAsciiTable()

        tbl #= ("Template Text", "Function", "Function result")

        val seq = if (asc) map.take(cnt).toSeq else map.toSeq.takeRight(cnt)

        seq.foreach(t ⇒ tbl += (t._1, t._2, NCDateParser.calculate(t._2, base)))

        tbl.render()

        println(s"\nGenerated records count is ${map.size}.")

        keys2Check match {
            case Some(keys) ⇒
                val missed = keys.filter(k ⇒ !map.contains(k))

                assertTrue(missed.isEmpty, s"Missed keys: ${missed.mkString(", ")}")
            case None ⇒ // No-op.
        }
    }

    private def checkFormatters(
        d: Date,
        fs: Seq[SimpleDateFormat],
        m: Map[String, Seq[SimpleDateFormat]],
        withDup: Boolean) {
        val res = fs.map(_.format(d)) ++ NCDateGenerator.format(d, m)

        res.foreach(println(_))

        val diff = res.diff(res.distinct)

        if (!withDup && diff.nonEmpty) //{
            fail(s"The same data: $diff.")
        else if (withDup && diff.isEmpty)
            fail(s"Duplicated data expected.")
    }

    @Test
    def testFormatters(): Unit = {
        val f = new SimpleDateFormat("MM.dd.yyyy")

        // M.D
        val d1 = f.parse("01.01.2010")

        checkFormatters(d1, NCDateGenerator.FMT_DATES_COMMON, NCDateGenerator.FMT_DATES_DIGITS, withDup = false)
        checkFormatters(d1, NCDateGenerator.FMT_DAYS_YEAR_COMMON, NCDateGenerator.FMT_DAYS_YEAR_DIGITS, withDup = false)

        // MM.D
        val d2 = f.parse("10.01.2010")

        checkFormatters(d2, NCDateGenerator.FMT_DATES_COMMON, NCDateGenerator.FMT_DATES_DIGITS, withDup = false)
        // Months are not duplicated for these set.
        checkFormatters(d2, NCDateGenerator.FMT_DAYS_YEAR_COMMON, NCDateGenerator.FMT_DAYS_YEAR_DIGITS, withDup = false)

        // M.DD
        val d3 = f.parse("01.10.2010")

        checkFormatters(d3, NCDateGenerator.FMT_DATES_COMMON, NCDateGenerator.FMT_DATES_DIGITS, withDup = true)
        checkFormatters(d3, NCDateGenerator.FMT_DAYS_YEAR_COMMON, NCDateGenerator.FMT_DAYS_YEAR_DIGITS, withDup = true)

        // MM.DD
        val d4 = f.parse("10.10.2010")

        checkFormatters(d4, NCDateGenerator.FMT_DATES_COMMON, NCDateGenerator.FMT_DATES_DIGITS, withDup = true)
        checkFormatters(d4, NCDateGenerator.FMT_DAYS_YEAR_COMMON, NCDateGenerator.FMT_DAYS_YEAR_DIGITS, withDup = true)
    }

    @Test
    def testRelativeDays(): Unit = {
        print(NCDateGenerator.relativeDays)
    }

    @Test
    def testPeriods(): Unit = {
        print(NCDateGenerator.periods)
    }

    @Test
    def testYears(): Unit = {
        print(NCDateGenerator.years)
    }

    @Test
    def testMonths(): Unit = {
        print((df: LHM_SS) ⇒ NCDateGenerator.months(df, MONTH_YEAR_COMMON.map(new SimpleDateFormat(_))))
    }

    @Test
    def testMonthsDmy(): Unit = {
        print((df: LHM_SS) ⇒ NCDateGenerator.months(df, MONTH_YEAR_COUNTRY(DMY).map(new SimpleDateFormat(_))))
    }

    @Test
    def testMonthsMdy(): Unit = {
        print((df: LHM_SS) ⇒ NCDateGenerator.months(df, MONTH_YEAR_COUNTRY(MDY).map(new SimpleDateFormat(_))))
    }

    @Test
    def testMonthsYmd(): Unit = {
        print((df: LHM_SS) ⇒ NCDateGenerator.months(df, MONTH_YEAR_COUNTRY(YMD).map(new SimpleDateFormat(_))))
    }

    @Test
    def testSeasons(): Unit = {
        print(NCDateGenerator.seasons)
    }

    @Test
    def testDays(): Unit = {
        print((df: LHM_SS) ⇒ NCDateGenerator.days(df, NCDateGenerator.FMT_DAYS_YEAR_COMMON), cnt = 5000)
    }

    @Test
    def testDates(): Unit = {
        print((df: LHM_SS) ⇒ NCDateGenerator.dates(df, NCDateGenerator.FMT_DATES_COMMON), cnt = 2000)
        print((df: LHM_SS) ⇒ NCDateGenerator.days(df, NCDateGenerator.FMT_DAYS_YEAR_COMMON), cnt = 2000, asc = false)
    }

    @Test
    def testSimpleYears(): Unit = {
        print(NCDateGenerator.simpleYears)
    }

    @Test
    def tesSimpleQuarters(): Unit = {
        print(NCDateGenerator.simpleQuarters, cnt = 5000)
    }

    @Test
    def testDurationDays(): Unit = {
        print(NCDateGenerator.durationDays, cnt = 1000, keys2Check = Some(Seq("next couple of days", "few next days", "few last days")))
        print(NCDateGenerator.durationDays, cnt = 5000, asc = false)
    }

    @Test
    def testDurationWeeks(): Unit = {
        print(NCDateGenerator.durationWeeks)
        print(NCDateGenerator.durationWeeks, asc = false)
    }

    @Test
    def testDurationMonths(): Unit = {
        print(NCDateGenerator.durationMonths)
        print(NCDateGenerator.durationMonths, cnt = 5000, asc = false)
    }

    @Test
    def testDurationYears(): Unit = {
        print(NCDateGenerator.durationYears)
        print(NCDateGenerator.durationYears, asc = false)
    }

    @Test
    def testDurationDecades(): Unit = {
        print(NCDateGenerator.durationDecades, cnt = 5000)
    }

    @Test
    def testDurationQuarters(): Unit = {
        print(NCDateGenerator.durationQuarters, cnt = 5000)
    }

    @Test
    def testDurationCenturies(): Unit = {
        print(NCDateGenerator.durationCenturies, cnt = 5000)
    }

    @Test
    def testRelativeDaysOfWeekByNum(): Unit = {
        print(NCDateGenerator.relativeDaysOfWeekByNum, cnt = 5000)
    }

    @Test
    def testRelativeDaysOfWeekByName(): Unit = {
        print(NCDateGenerator.relativeDaysOfWeekByName)
    }

    @Test
    def testRelativeDaysOfMonth(): Unit = {
        print(NCDateGenerator.relativeDaysOfMonth, cnt = 5000)
    }

    @Test
    def testRelativeWeeksOfMonth(): Unit = {
        print(NCDateGenerator.relativeWeeksOfMonth)
    }

    @Test
    def testRelativeWeeksOfQuarter(): Unit = {
        print(NCDateGenerator.relativeWeeksOfQuarter, cnt = 5000)
    }

    @Test
    def testRelativeWeeksOfYear(): Unit = {
        print(NCDateGenerator.relativeWeeksOfYear, cnt = 5000)
    }

    @Test
    def testRelativeMonthsOfYear(): Unit = {
        print(NCDateGenerator.relativeMonthsOfYear, cnt = 5000)
    }

    @Test
    def testRelativeQuartersOfYear(): Unit = {
        print(NCDateGenerator.relativeQuartersOfYear)
    }

    @Test
    def testelativeYearOfDecade(): Unit = {
        print(NCDateGenerator.relativeYearOfDecade)
    }

    @Test
    def testRelativeFromYear(): Unit = {
        print(NCDateGenerator.relativeFromYear, cnt = 5000)
        print(NCDateGenerator.relativeFromYear, cnt = 5000, asc = false)
    }

    @Test
    def testCalendar(): Unit = {
        Locale.setDefault(Locale.forLanguageTag("EN"))

        // Sunday.
        val sunday = new SimpleDateFormat("MM.dd.yyyy").parse("03.09.2014")

        val c = Calendar.getInstance()

        (0 to 6).foreach(i ⇒ {
            c.setTime(sunday)
            c.add(Calendar.DAY_OF_YEAR, i)

            val s = s"Checked day: ${c.getTime}, number day of week: ${c.get(Calendar.DAY_OF_WEEK)}"

            c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

            println(s"$s, Sunday for this day: ${c.getTime}")

            assertTrue(c.getTime == sunday)
        })
    }

    @Test
    def testAll(): Unit = {
        val n = (NCDateGenerator.generateFull() ++ NCDateGenerator.generateParts()).size

        println(s"Summary generated $n templates.")
    }

    @Test
    def testLastDayOfMonth(): Unit = {
        def test(base: String, exp: String): Unit = {
            val date = new SimpleDateFormat("MM.dd.yyyy").parse(base)

            val f = "m, $dm, 1dw, w1"

            val res = NCDateParser.calculatePart(f, date.getTime)
            val range = NCDateRange(res.from, res.to, f, res.periods.asJava)

            println(s"Date: $date")
            println(s"Result: $res")
            println(s"Range: $range")

            assertTrue(range.toString == exp)
        }

        test("06.10.2018", "[06-24-2018:07-01-2018]")
        test("11.20.2017", "[11-26-2017:12-03-2017]")
    }

    @Test
    def testLastDay(): Unit = {
        val date = scala.compat.Platform.currentTime

        def print(f: String): Unit = {
            val res = NCDateParser.calculatePart(f, date)
            val range = NCDateRange(res.from, res.to, f, res.periods.asJava)

            println(s"Function: $f")
            println(s"Date: $date")
            println(s"Result: $res")
            println(s"Range: $range")
            println()
        }

        print("$dw")
        print("$dm")
        print("$dq")
        print("$dy")
        print("$de")
        print("$dc")
        print("$ds")
    }

    @Test
    def testParse(): Unit = {
        val date = scala.compat.Platform.currentTime

        val f = "m, $dm, 1dw, w1"

        val res = NCDateParser.calculatePart(f, date)
        val range = NCDateRange(res.from, res.to, f, res.periods.asJava)

        println(s"Date: $date")
        println(s"Result: $res")
        println(s"Range: $range")
    }
}