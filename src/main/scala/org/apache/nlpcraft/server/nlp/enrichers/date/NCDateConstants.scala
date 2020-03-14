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

import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.server.nlp.enrichers.date.NCDateFormatType._

/**
 * Date enricher constants.
 */
private[date] object NCDateConstants {
    private final val parser = NCMacroParser()
    
    val BETWEEN_INCLUSIVE: Seq[(String, String)] = Seq(
        ("from", "to"),
        ("since", "to"),
        ("since", "till"),
        ("from", "till")
    )

    val BETWEEN_EXCLUSIVE: Seq[(String, String)] = Seq(
        ("between", "and")
    )

    val FROM: Seq[String] = Seq(
        "from",
        "after",
        "starting from",
        "from start of",
        "beginning from",
        "since",
        "starting since",
        "beginning since",
        "from beginning of"
    )

    val TO: Seq[String] = Seq(
        "to",
        "till",
        "until"
    )

    val FOR: Seq[String] = Seq(
        "for",
        "during",
        "during {the|*} {time|*} period of",
        "within",
        "within {the|*} {time|*} period of",
        "of",
        "for {the|*} {time|*} period of"
    ).flatMap(parser.expand)

    val AGO: Seq[String] = Seq(
        "ago",
        "before",
        "back"
    )

    val FORWARD: Seq[String] = Seq(
        "next",
        "following",
        "forward"
    )

    val LAST: Seq[String] = Seq(
        "last"
    )

    val FIRST: Seq[String] = Seq(
        "first"
    )

    val ON: Seq[String] = Seq(
        "on",
        "in",
        "for"
    )

    val PREVIOUS: Seq[String] = Seq(
        "previous",
        "past",
        "earlier",
        "preceding"
    )

    val CURRENT: Seq[String] = Seq(
        "this",
        "current",
        "present"
    )

    val NEXT: Seq[String] = Seq(
        "next",
        "following",
        "future"
    )

    // Formats below will be duplicated.
    // They are grouped according to duplication rules.

    // 'dd' will be duplicated as 'd'
    // 'dd' will be duplicated as 'dth' (st, rd)
    val YEAR_MONTH_DAY_COMMON_1: Seq[String] = Seq(
        "{yy|yyyy} dd {'of'|*} {MMMM|MMM}",
        "dd {'of'|*} {MMMM|MMM} {'of'|*} yyyy"
    ).flatMap(parser.expand)

    // 'dd' will be duplicated as 'd'.
    val YEAR_MONTH_DAY_COMMON_2: Seq[String] = Seq(
        "{yy|yyyy} {MMMM|MMM} dd",
        "{MMMM|MMM} dd {yyyy|yy}"
    ).flatMap(parser.expand)

    // 'dd' will be duplicated as 'd'.
    // 'MM' will be duplicated as 'M'.
    val YEAR_MONTH_DAY_COUNTRY: Map[NCDateFormatType, Seq[String]] =
        Map(
            MDY →
                Seq(
                    "MM.dd.yyyy",
                    "MM.dd.yy",
                    "MM/dd/yyyy",
                    "MM/dd/yy",
                    "MM-dd-yyyy",
                    "MM-dd-yy"
                ),
            DMY →
                Seq(
                    "dd.MM.yyyy",
                    "dd.MM.yy",
                    "dd/MM/yyyy",
                    "dd/MM/yy",
                    "dd-MM-yyyy",
                    "dd-MM-yy"
                ),
            YMD →
                Seq(
                    "yyyy.MM.dd",
                    "yy.MM.dd",
                    "yyyy/MM/dd",
                    "yy/MM/dd",
                    "yyyy-MM-dd",
                    "yy-MM-dd"
                )
        )

    // 'dd' will be duplicated as 'd'
    // 'dd' will be duplicated as 'dth' (st, rd)
    val MONTH_DAY_COMMON_1: Seq[String] = Seq(
        "dd {'of'|*} {MMMM|MMM}"
    ).flatMap(parser.expand)

    // 'dd' will be duplicated as 'd'.
    val MONTH_DAY_COMMON_2: Seq[String] = Seq(
        "{MMM|MMMM} dd"
    ).flatMap(parser.expand)

    // Without duplicates.
    val MONTH_DAY_COUNTRY: Map[NCDateFormatType, Seq[String]] =
        Map(
            MDY → Seq("MM.dd", "MM/dd", "MM-dd"),
            DMY → Seq("dd.MM", "dd/MM", "dd-MM"),
            YMD → Seq("MM.dd", "MM/dd", "MM-dd")
        )

    val MONTH_YEAR_COMMON: Seq[String] = Seq(
        "{'month of'|'month'|*} {MMMM|MMM} {'of'|'year'|'of year'|'year of'|*} {yy|yyyy} {'year'|*}",
        "yyyy {MMMM|MMM}",
        "{'month of'|'month'|*} {MMMMyyyy|MMMyyyy|MMMMyy|MMMyy}",
        "{'month of'|'month'|*} {yyyyMMMM|yyyyMMM|yyMMMM|yyMMM}",
        "{'month of'|'month'|*} {yyyyMMMM|yyyyMMM|yyMMMM|yyMMM}",
        "{'month of'|'month'|*} {MM.yyyy|MM/yyyy|MM-yyyy|yyyy.MM|yyyy/MM|yyyy-MM}"
    ).flatMap(parser.expand)

    val MONTH_YEAR_COUNTRY: Map[NCDateFormatType, Seq[String]] =
        Map(
            MDY → Seq("{'month of'|'month'} {MM.yy|MM/yy|MM-yy}"),
            DMY → Seq("{'month of'|'month'} {MM.yy|MM/yy|MM-yy}"),
            YMD → Seq("{'month of'|'month'} {yy.MM|yy/MM|yy-MM}")
        ).map { case (typ, seq) ⇒ typ → seq.flatMap(parser.expand) }

    val YEAR: Seq[String] = Seq(
        "'year' {'of'|*} {yyyy|yy}",
        "{yy|yyyy} 'year'",
        "yyyy"
    ).flatMap(parser.expand)

    // https://www.compart.com/en/unicode/category/Pd
    val DASHES: Seq[Char] = Seq(
        '\u002D',
        '\u058A',
        '\u05BE',
        '\u1400',
        '\u1806',
        '\u2010',
        '\u2011',
        '\u2012',
        '\u2013',
        '\u2014',
        '\u2015',
        '\u2E17',
        '\u2E1A',
        '\u2E3A',
        '\u2E3B',
        '\u2E40',
        '\u301C',
        '\u3030',
        '\u30A0',
        '\uFE31',
        '\uFE32',
        '\uFE58',
        '\uFE63',
        '\uFF0D'
    )

    val DASHES_LIKE: Seq[Char] = Seq('/', ':', '~')
}