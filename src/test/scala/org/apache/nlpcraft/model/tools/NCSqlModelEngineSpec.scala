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

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * TODO: add description.
 */
class NCSqlModelEngineSpec {
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

    @Test
    def testRemovePrefix() {
        val fun1 = mkPrefixFun("tbl_, col_")
        val fun2 = mkPrefixFun("")
        val fun3 = mkPrefixFun("tbl_tbl_, col_")
        
        assertTrue(fun1("tbl_table") == "table")
        assertTrue(fun1("tbl_tbl_table") == "tbl_table")
        assertTrue(fun3("tbl_tbl_table") == "table")
        assertTrue(fun1("col_column") == "column")
        assertTrue(fun1("_col_column") == "_col_column")
        assertTrue(fun2("col_column") == "col_column")
    }

    @Test
    def testRemoveSuffix() {
        val fun1 = mkSuffixFun("_tmp, _old")
        val fun2 = mkSuffixFun("")
        val fun3 = mkSuffixFun("_tmp_tmp")
        
        assertTrue(fun1("table_old") == "table")
        assertTrue(fun1("table_old_old") == "table_old")
        assertTrue(fun3("table_tmp_tmp") == "table")
        assertTrue(fun1("column_old") == "column")
        assertTrue(fun1("column_old_") == "column_old_")
        assertTrue(fun2("column_old") == "column_old")
    }

    @Test
    def testSubstituteMacros() {
        assertTrue(substituteMacros("a id") == "a <ID>")
        assertTrue(substituteMacros("a     id   ") == "a <ID>")
        assertTrue(substituteMacros("id") == "<ID>")
    }

    @Test
    def testRemoveSequentialDups() {
        assertTrue(removeSeqDups("a a b b") == "a b")
        assertTrue(removeSeqDups("aa b b") == "aa b")
        assertTrue(removeSeqDups("aa     b    b") == "aa b")
        assertTrue(removeSeqDups("aa     b    bb") == "aa b bb")
        assertTrue(removeSeqDups("a a b b") != "a b c")
    }
}
