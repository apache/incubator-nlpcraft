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

package org.apache.nlpcraft.common.util

import org.apache.nlpcraft.common._
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Utilities tests.
 */
class NCUtilsSpec  {
    @Test
    def testInflateDeflate() {
        val rawStr = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. " +
            "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when " +
            "an unknown printer took a galley of type and scrambled it to make a type specimen book. " +
            "It has survived not only five centuries, but also the leap into electronic typesetting, " +
            "remaining essentially unchanged. It was popularised in the 1960s with the release of " +
            "Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing " +
            "software like Aldus PageMaker including versions of Lorem Ipsum."
        
        println(s"Original length: " + rawStr.length())
        
        val zipStr = U.compress(rawStr)
        val rawStr2 = U.uncompress(zipStr)
        
        println(s"Compressed length: " + zipStr.length())
        
        assertTrue(rawStr == rawStr2)
    }

    @Test
    def testGetDups() {
        assertTrue(U.getDups(Seq("a", "b", "a")) == Set("a"))
        assertTrue(U.getDups(Seq("a", "a", "a", "b", "a")) == Set("a"))
        assertTrue(U.getDups(Seq("a", "d", "c", "b", "e")) == Set())
    }

    @Test
    def testToFirstLastName() {
        assertTrue(U.toFirstLastName("A BbbBB") == ("A", "Bbbbb"))
        assertTrue(U.toFirstLastName("aBC BbbBB CCC") == ("Abc", "Bbbbb ccc"))
        assertTrue(U.toFirstLastName("abc b C C C") == ("Abc", "B c c c"))
    }

    @Test
    def testWorkWithoutUnnecessaryLogging() {
        val t = new Thread() {
            override def run(): Unit = {
                while (!isInterrupted) {
                    println("before sleep")

                    U.sleep(100)

                    println("after sleep")
                }
            }
        }

        t.start()

        U.sleep(550)

        t.interrupt()

        t.join()

        println("OK")
    }
}