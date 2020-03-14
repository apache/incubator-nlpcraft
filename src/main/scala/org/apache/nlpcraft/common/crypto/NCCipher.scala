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

import java.security.{Key, SecureRandom, GeneralSecurityException ⇒ GSE}

import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import org.apache.commons.codec.binary.Base64
import org.apache.nlpcraft.common._

import scala.util.control.Exception._

/**
  * PKI and crypto-processing manager.
  */
object NCCipher {
    // Symmetric 16-byte master private key.
    // -------------------------------------
    // This is rather unsafe and should be changed.
    // Key should be loaded from HSM or employ a similar approach.
    private final val SYM_PRI_KEY = Array(
        0x01.toByte, 0xde.toByte, 0x37.toByte, 0xf4.toByte,
        0x02.toByte, 0x4e.toByte, 0x77.toByte, 0xd4.toByte,
        0x31.toByte, 0xae.toByte, 0x88.toByte, 0xe4.toByte,
        0x41.toByte, 0x9e.toByte, 0x99.toByte, 0xc4.toByte
    )

    // Secure RNG.
    private final val RAND = new SecureRandom()

    // Cypher algorithm.
    private final val ALGO = "AES"
    private final val MODE = "CBC"
    private final val PADDING = "PKCS5Padding"

    // Key holder.
    private final val KEY_SPEC = new SecretKeySpec(SYM_PRI_KEY, ALGO)

    /**
      * Encrypts given string with default key.
      * 
      * @param data String to encrypt.
      * @return
      */
    @throws[NCE]
    def encrypt(data: String): String =
        encrypt(data, KEY_SPEC)

    /**
      *
      * @param data String to encrypt.
      * @param key Key.
      * @return
      */
    @throws[NCE]
    def encrypt(data: String, key: Key): String = {
        catching(wrapGSE) {
            val iv = generateIv()

            val cipher = newCipher

            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv))

            // Raw encrypted.
            val d1 = cipher.doFinal(data.getBytes)

            // Allocate for raw encrypted + IV (salt).
            val d2 = Array.ofDim[Byte](d1.length + 16)

            // Add raw encrypted and IV (salt).
            Array.copy(d1, 0, d2, 0, d1.length)
            Array.copy(iv, 0, d2, d1.length, 16)

            new String(Base64.encodeBase64(d2))
        }
    }

    // Generates IV.
    private def generateIv(): Array[Byte] = {
        val iv = Array.ofDim[Byte](16)

        RAND.nextBytes(iv)

        iv
    }
    
    /**
      * Makes key out of given probe token.
      *
      * @param token Probe token.
      */
    def makeTokenKey(token: String): Key = {
        val src = token.getBytes("UTF-8")
        val dst = new Array[Byte](16)

        Array.copy(src, 0, dst, 0, Math.min(src.length, dst.length))

        new SecretKeySpec(dst, ALGO)
    }
    
    /**
      *
      * @param data Base64 encoded string to decrypt with default key.
      */
    def decrypt(data: String): String = decrypt(data, KEY_SPEC)

    /**
      *
      * @param data Base64 encoded string to decrypt.
      * @param key Encryption key.
      * @return
      */
    @throws[NCE]
    def decrypt(data: String, key: Key): String = {
        catching(wrapGSE) {
            val cipher = newCipher

            // Gets bytes from Base64 encoded string.
            val d1 = Base64.decodeBase64(data.getBytes)
            val len = d1.length - 16

            val iv = Array.ofDim[Byte](16)
            val d2 = Array.ofDim[Byte](len)

            // Get data and IV (salt).
            Array.copy(d1, len, iv, 0, 16)
            Array.copy(d1, 0, d2, 0, len)

            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv))

            new String(cipher.doFinal(d2))
        }
    }

    /**
      *
      * @return
      */
    @throws[NCE]
    private def wrapGSE[R]: Catcher[R] = {
        case e: GSE ⇒ throw new NCE(s"Cryptography error: ${e.getMessage}", e)
    }

    /**
      * Gets cipher instance.
      */
    @throws[GSE]
    private def newCipher = Cipher.getInstance(s"$ALGO/$MODE/$PADDING")
}
