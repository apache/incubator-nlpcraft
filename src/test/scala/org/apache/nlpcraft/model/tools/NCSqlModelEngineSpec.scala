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

package org.apache.nlpcraft.model.tools

import org.scalatest.FlatSpec

/**
 * TODO: add description.
 */
class NCSqlModelEngineSpec extends FlatSpec {
    behavior of "SQL model engine"
    
    /**
     * Copy of the private method from 'NCSqlModelEngine'.
     *
     * @param s
     * @return
     */
    def substituteMacros(s: String): String =
        s.split(" ").filter(_.nonEmpty).map(w ⇒ {
            if (w == "id")
                "<ID>"
            else
                w
        }).mkString(" ")
    
    /**
     * Note that it only removed one, first found, prefix.
     *
     * @param s
     * @return
     */
    private def mkPrefixFun(s: String): String ⇒ String = {
        val arr = s.split(",").map(_.trim).filter(_.nonEmpty)

        z ⇒ (for (fix ← arr if z.startsWith(fix)) yield z.substring(fix.length)).headOption.getOrElse(z)
    }
    
    /**
     * Note that it only removed one, first found, prefix.
     *
     * @param s
     * @return
     */
    private def mkSuffixFun(s: String): String ⇒ String = {
        val arr = s.split(",").map(_.trim).filter(_.nonEmpty)
        
        z ⇒ (for (fix ← arr if z.endsWith(fix)) yield z.substring(0, z.length - fix.length)).headOption.getOrElse(z)
    }

    /**
     * Copy of the private method from 'NCSqlModelEngine'.
     *
     * @param s
     * @return
     */
    private def removeSeqDups(s: String): String =
        s.split(" ").filter(_.nonEmpty).foldRight[List[String]](Nil)((w, list) ⇒ list.headOption match {
            case Some(head) if head == w ⇒ list // Skip duplicate 'w'.
            case _ ⇒ w :: list
        }).mkString(" ")
    
    it should "correctly remove prefix" in {
        val fun1 = mkPrefixFun("tbl_, col_")
        val fun2 = mkPrefixFun("")
        val fun3 = mkPrefixFun("tbl_tbl_, col_")
        
        assert(fun1("tbl_table") == "table")
        assert(fun1("tbl_tbl_table") == "tbl_table")
        assert(fun3("tbl_tbl_table") == "table")
        assert(fun1("col_column") == "column")
        assert(fun1("_col_column") == "_col_column")
        assert(fun2("col_column") == "col_column")
    }
    
    it should "correctly remove suffix" in {
        val fun1 = mkSuffixFun("_tmp, _old")
        val fun2 = mkSuffixFun("")
        val fun3 = mkSuffixFun("_tmp_tmp")
        
        assert(fun1("table_old") == "table")
        assert(fun1("table_old_old") == "table_old")
        assert(fun3("table_tmp_tmp") == "table")
        assert(fun1("column_old") == "column")
        assert(fun1("column_old_") == "column_old_")
        assert(fun2("column_old") == "column_old")
    }

    it should "correctly substitute macros" in {
        assert(substituteMacros("a id") == "a <ID>")
        assert(substituteMacros("a     id   ") == "a <ID>")
        assert(substituteMacros("id") == "<ID>")
    }
    
    it should "correctly remove sequential dups" in {
        assert(removeSeqDups("a a b b") == "a b")
        assert(removeSeqDups("aa b b") == "aa b")
        assert(removeSeqDups("aa     b    b") == "aa b")
        assert(removeSeqDups("aa     b    bb") == "aa b bb")
        assert(removeSeqDups("a a b b") != "a b c")
    }
}
