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

package org.apache.nlpcraft.model.intent

import org.apache.nlpcraft.common._

import java.util.StringTokenizer
import java.util.regex.Pattern

/**
  * 
  */
object NCDslSyntaxHighlighter {
    private final val NUM_REGEX = Pattern.compile("-?[0-9]+")
    private final val STR_REGEX = Pattern.compile("""(["'])[^"]*\1""")

    private val KEYWORDS = Seq("flow", "fragment", "url", "intent", "meta", "term", "ordered")
    private val LITERALS = Seq("true", "false", "null")
    private val FUNCTIONS = Seq(
        "meta_token",
        "meta_part",
        "meta_model",
        "meta_intent",
        "meta_req",
        "meta_user",
        "meta_company",
        "meta_sys",
        "meta_conv",
        "meta_frag",
        "json",
        "if",
        "id",
        "this",
        "part",
        "parts",
        "ancestors",
        "parent",
        "groups",
        "value",
        "aliases",
        "start_idx",
        "end_idx",
        "req_id",
        "req_normtext",
        "req_tstamp",
        "req_addr",
        "req_agent",
        "user_id",
        "user_fname",
        "user_lname",
        "user_email",
        "user_admin",
        "user_signup_tstamp",
        "comp_id",
        "comp_name",
        "comp_website",
        "comp_country",
        "comp_region",
        "comp_city",
        "comp_addr",
        "comp_postcode",
        "trim",
        "strip",
        "uppercase",
        "lowercase",
        "is_alpha",
        "is_alphanum",
        "is_whitespace",
        "is_num",
        "is_numspace",
        "is_alphaspace",
        "is_alphanumspace",
        "substring",
        "charAt",
        "regex",
        "soundex",
        "split",
        "split_trim",
        "replace",
        "abs",
        "ceil",
        "floor",
        "rint",
        "round",
        "signum",
        "sqrt",
        "cbrt",
        "pi",
        "euler",
        "acos",
        "asin",
        "atan",
        "cos",
        "sin",
        "tan",
        "cosh",
        "sinh",
        "tanh",
        "atn2",
        "degrees",
        "radians",
        "exp",
        "expm1",
        "hypot",
        "log",
        "log10",
        "log1p",
        "pow",
        "rand",
        "square",
        "list",
        "map",
        "get",
        "index",
        "has",
        "tail",
        "add",
        "remove",
        "first",
        "last",
        "keys",
        "values",
        "length",
        "count",
        "take",
        "drop",
        "size",
        "reverse",
        "is_empty",
        "non_empty",
        "to_string",
        "avg",
        "max",
        "min",
        "stdev",
        "sum",
        "year",
        "month",
        "day_of_month",
        "day_of_week",
        "day_of_year",
        "hour",
        "minute",
        "second",
        "week_of_month",
        "week_of_year",
        "quarter",
        "now"
    )
    
    /**
      * Returns given DSL string with DSL syntax highlighted.
      * 
      * @param dsl DSL string to highlight.
      * @return
      */
    def color(dsl: String): String = {
        val toks = new StringTokenizer(dsl, " \r\n[]{}()=,~/", true)
        var res = new StringBuilder
        
        while (toks.hasMoreTokens) {
            val tok = toks.nextToken()
            
            if (KEYWORDS.contains(tok))
                res ++= (if (tok == "intent") bold(blue(tok)) else cyan(tok))
            else if (LITERALS.contains(tok) || NUM_REGEX.matcher(tok).matches())
                res ++= green(tok)
            else if (FUNCTIONS.contains(tok))
                res ++= yellow(tok)
            else if (STR_REGEX.matcher(tok).matches())
                res ++= magenta(tok)
            else
                res ++= tok
        }
        
        res.toString
    }
}
