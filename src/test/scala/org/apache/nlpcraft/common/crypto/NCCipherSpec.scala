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

package org.apache.nlpcraft.common.crypto

import java.util.Base64

import org.apache.nlpcraft.common._
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
/**
 * Tests for crypto and PKI support.
 */
class NCCipherSpec  {
    private final val IN = "abcdefghijklmnopqrstuvwxyz0123456789"

    @Test
    def testEncryptDecrypt() {
        val s1 = NCCipher.encrypt(IN)
        val s2 = NCCipher.decrypt(s1)

        assertEquals(IN, s2)
    }

    @Test
    def testDifference() {
        val s1 = NCCipher.encrypt(IN)
        val s2 = NCCipher.encrypt(IN)
        val s3 = NCCipher.encrypt(IN)

        assertTrue(s1 != s2)
        assertTrue(s2 != s3)

        val r1 = NCCipher.decrypt(s1)
        val r2 = NCCipher.decrypt(s2)
        val r3 = NCCipher.decrypt(s3)

        assertEquals(IN, r1)
        assertEquals(IN, r2)
        assertEquals(IN, r3)
    }

    @Test
    def testCorrectness() {
        val buf = new StringBuilder
        
        // Max long string.
        for (i ← 0 to 1275535) buf.append(i.toString)
        
        val str = buf.toString
        
        val bytes = U.serialize(str)
        
        val key = NCCipher.makeTokenKey(U.genGuid())
        
        val now = System.currentTimeMillis()
        
        val sec = NCCipher.encrypt(Base64.getEncoder.encodeToString(bytes), key)
        
        val dur = System.currentTimeMillis() - now
        
        println(s"Input length: ${str.length}")
        println(s"Output length: ${sec.length}")
        println(s"Total: $dur ms.")
    }
}
