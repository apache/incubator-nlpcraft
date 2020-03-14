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
import org.apache.nlpcraft.server.nlp.enrichers.date.tools.NCDateGenerator.LHM_SS
import org.apache.nlpcraft.server.nlp.enrichers.date.{NCDateParser, NCDateRange}
import org.apache.nlpcraft.server.nlp.enrichers.date.NCDateFormatType._

import org.scalatest.FunSuite

import scala.collection.JavaConverters._
import scala.collection._
import scala.collection.mutable.{LinkedHashMap ⇒ LHM}
import org.apache.nlpcraft.server.nlp.enrichers.date.NCDateConstants._
import scala.language.implicitConversions

/**
  * Tests for dates templates generators.
  */
class NCDateGeneratorSpec extends FunSuite {
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

                assert(missed.isEmpty, s"Missed keys: ${missed.mkString(", ")}")
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

    test("formatters") {
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

    test("relativeDays") {
        print(NCDateGenerator.relativeDays)
    }

    test("periods") {
        print(NCDateGenerator.periods)
    }

    test("years") {
        print(NCDateGenerator.years)
    }

    test("months") {
        print((df: LHM_SS) ⇒ NCDateGenerator.months(df, MONTH_YEAR_COMMON.map(new SimpleDateFormat(_))))
    }

    test("months.dmy") {
        print((df: LHM_SS) ⇒ NCDateGenerator.months(df, MONTH_YEAR_COUNTRY(DMY).map(new SimpleDateFormat(_))))
    }

    test("months.mdy") {
        print((df: LHM_SS) ⇒ NCDateGenerator.months(df, MONTH_YEAR_COUNTRY(MDY).map(new SimpleDateFormat(_))))
    }

    test("months.ymd") {
        print((df: LHM_SS) ⇒ NCDateGenerator.months(df, MONTH_YEAR_COUNTRY(YMD).map(new SimpleDateFormat(_))))
    }

    test("seasons") {
        print(NCDateGenerator.seasons)
    }

    test("days") {
        print((df: LHM_SS) ⇒ NCDateGenerator.days(df, NCDateGenerator.FMT_DAYS_YEAR_COMMON), cnt = 5000)
    }

    test("dates") {
        print((df: LHM_SS) ⇒ NCDateGenerator.dates(df, NCDateGenerator.FMT_DATES_COMMON), cnt = 2000)
        print((df: LHM_SS) ⇒ NCDateGenerator.days(df, NCDateGenerator.FMT_DAYS_YEAR_COMMON), cnt = 2000, asc = false)
    }

    test("simpleYears") {
        print(NCDateGenerator.simpleYears)
    }

    test("simpleQuarters") {
        print(NCDateGenerator.simpleQuarters, cnt = 5000)
    }

    test("durationDays") {
        print(NCDateGenerator.durationDays, cnt = 1000, keys2Check = Some(Seq("next couple of days", "few next days", "few last days")))
        print(NCDateGenerator.durationDays, cnt = 5000, asc = false)
    }

    test("durationWeeks") {
        print(NCDateGenerator.durationWeeks)
        print(NCDateGenerator.durationWeeks, asc = false)
    }

    test("durationMonths") {
        print(NCDateGenerator.durationMonths)
        print(NCDateGenerator.durationMonths, cnt = 5000, asc = false)
    }

    test("durationYears") {
        print(NCDateGenerator.durationYears)
        print(NCDateGenerator.durationYears, asc = false)
    }

    test("durationDecades") {
        print(NCDateGenerator.durationDecades, cnt = 5000)
    }

    test("durationQuarters") {
        print(NCDateGenerator.durationQuarters, cnt = 5000)
    }

    test("durationCenturies") {
        print(NCDateGenerator.durationCenturies, cnt = 5000)
    }

    test("relativeDaysOfWeekByNum") {
        print(NCDateGenerator.relativeDaysOfWeekByNum, cnt = 5000)
    }

    test("relativeDaysOfWeekByName") {
        print(NCDateGenerator.relativeDaysOfWeekByName)
    }

    test("relativeDaysOfMonth") {
        print(NCDateGenerator.relativeDaysOfMonth, cnt = 5000)
    }

    test("relativeWeeksOfMonth") {
        print(NCDateGenerator.relativeWeeksOfMonth)
    }

    test("relativeWeeksOfQuarter") {
        print(NCDateGenerator.relativeWeeksOfQuarter, cnt = 5000)
    }

    test("relativeWeeksOfYear") {
        print(NCDateGenerator.relativeWeeksOfYear, cnt = 5000)
    }

    test("relativeMonthsOfYear") {
        print(NCDateGenerator.relativeMonthsOfYear, cnt = 5000)
    }

    test("relativeQuartersOfYear") {
        print(NCDateGenerator.relativeQuartersOfYear)
    }

    test("relativeYearOfDecade") {
        print(NCDateGenerator.relativeYearOfDecade)
    }

    test("relativeFromYear") {
        print(NCDateGenerator.relativeFromYear, cnt = 5000)
        print(NCDateGenerator.relativeFromYear, cnt = 5000, asc = false)
    }

    test("calendar.behaviour") {
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

            assert(c.getTime == sunday)
        })
    }

    test("all") {
        val n = (NCDateGenerator.generateFull() ++ NCDateGenerator.generateParts()).size

        println(s"Summary generated $n templates.")
    }

    test("last day of month command") {
        def test(base: String, exp: String): Unit = {
            val date = new SimpleDateFormat("MM.dd.yyyy").parse(base)

            val f = "m, $dm, 1dw, w1"

            val res = NCDateParser.calculatePart(f, date.getTime)
            val range = NCDateRange(res.from, res.to, f, res.periods.asJava)

            println(s"Date: $date")
            println(s"Result: $res")
            println(s"Range: $range")

            assert(range.toString == exp)
        }

        test("06.10.2018", "[06-24-2018:07-01-2018]")
        test("11.20.2017", "[11-26-2017:12-03-2017]")
    }

    test("last day function") {
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

    test("parse") {
        val date = scala.compat.Platform.currentTime

        val f = "m, $dm, 1dw, w1"

        val res = NCDateParser.calculatePart(f, date)
        val range = NCDateRange(res.from, res.to, f, res.periods.asJava)

        println(s"Date: $date")
        println(s"Result: $res")
        println(s"Range: $range")
    }
}