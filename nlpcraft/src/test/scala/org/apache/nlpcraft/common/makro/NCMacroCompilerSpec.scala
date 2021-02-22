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

package org.apache.nlpcraft.common.makro

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
  * Unit tests for the macro compiler.
  */
class NCMacroCompilerSpec {
    /**
      *
      * @param txt
      * @param exp
      */
    private def checkEq(txt: String, exp: Seq[String]): Unit = {
        val res = NCMacroCompiler.compile(txt).toSeq.sorted
        val z = exp.sorted
        
        if (res != z)
            println(s"$res != $z")
        
        assertTrue(res == z)
    }
    
    @Test
    def testCompiler(): Unit = {
        checkEq("A", Seq("A"))
        checkEq("    A   ", Seq("A"))
        checkEq("A B", Seq("A B"))
        checkEq("A           B", Seq("A B"))
        checkEq("{A}", Seq("A"))
        checkEq("{A}[0,2]", Seq("", "A", "A A"))
        checkEq("{A  }   [0  ,2]", Seq("", "A", "A A"))
        checkEq("{A          }", Seq("A"))
        checkEq("      {      A          }", Seq("A"))
        checkEq("{A}{B}", Seq("A B"))
        checkEq(" {  A   }{B}", Seq("A B"))
        checkEq(" {  A   }      {B}", Seq("A B"))
        checkEq("A {B | C}", Seq("A B", "A C"))
    }
}
