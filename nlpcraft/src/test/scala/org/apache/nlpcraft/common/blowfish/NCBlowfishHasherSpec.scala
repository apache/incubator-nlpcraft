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

package org.apache.nlpcraft.common.blowfish

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
  * Tests for Blowfish hasher.
  */
class NCBlowfishHasherSpec {
    @Test
    def testWithSequenceHeader() {
        val email = "dev@nlpcraft.org"
        val passwd = "test"
        
        val salt1 = NCBlowfishHasher.hash(email)
        val salt2 = NCBlowfishHasher.hash(email)
    
        println(s"Salt1: $salt1")
        println(s"Salt2: $salt2")
        
        val hash1 = NCBlowfishHasher.hash(passwd, salt1)
        val hash2 = NCBlowfishHasher.hash(passwd, salt1)

        assertTrue(hash1 == hash2)

        println(s"Salt: $salt1")
        println(s"Hash: $hash1")
    }
}
