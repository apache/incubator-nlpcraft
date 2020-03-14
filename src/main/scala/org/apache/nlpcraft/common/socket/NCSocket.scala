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

package org.apache.nlpcraft.common.socket

import java.io._
import java.net.Socket
import java.security.Key
import java.util.Base64

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.crypto.NCCipher

/**
  * Socket wrapper that does optional encryption and uses HTTP POST protocol for sending and receiving.
  */
case class NCSocket(socket: Socket, host: String, soTimeout: Int = 20000) extends LazyLogging {
    require(socket != null)
    require(host != null)
    require(soTimeout >= 0)

    socket.setSoTimeout(soTimeout)

    private final val rwLock = new Object()
    private lazy val writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream, "UTF8"))
    private lazy val reader = new BufferedReader(new InputStreamReader(socket.getInputStream, "UTF8"))

    override def toString: String = socket.toString
    override def hashCode(): Int = socket.hashCode()
    override def equals(obj: scala.Any): Boolean =
        obj != null && obj.isInstanceOf[NCSocket] && socket.equals(obj.asInstanceOf[NCSocket].socket)

    /**
      *
      */
    def close(): Unit = {
        logger.trace(s"Closing socket: $socket")

        // Note that we don't synchronize on closing.
        U.close(socket)
    }
    
    /**
      *
      * @param key Optional encryption key.
      */
    @throws[NCE]
    @throws[IOException]
    def read[T](key: Key = null): T = {
        if (!socket.isConnected || socket.isInputShutdown)
            throw new EOFException()

        val arr =
            rwLock.synchronized {
                val line = reader.readLine()

                if (line == null)
                    throw new EOFException()

                val len =
                    try
                        Integer.parseInt(line.trim)
                    catch {
                        case e: NumberFormatException ⇒ throw new NCE(s"Unexpected content length: $line", e)
                    }

                if (len <= 0)
                    throw new NCE(s"Unexpected data length: $len")

                val arr = new Array[Char](len)

                var n = 0

                while (n != arr.length) {
                    val k = reader.read(arr, n, arr.length - n)

                    if (k == -1)
                        throw new EOFException()

                    n = n + k
                }

                arr
            }

        try {
            val bytes =
                Base64.getDecoder.decode(
                    if (key != null) NCCipher.decrypt(new String(arr), key) else new String(arr)
                )

            val res: T = U.deserialize(bytes)

            res
        }
        catch {
            case e: Exception ⇒ throw new NCE("Error reading data.", e)
        }
    }
    
    /**
      *
      * @param v Value to send.
      * @param key Optional encryption key.
      */
    @throws[NCE]
    @throws[IOException]
    def write(v: Serializable, key: Key = null): Unit = {
        if (!socket.isConnected || socket.isOutputShutdown)
            throw new IOException("Connection closed.")

        val data =
            try {
                val serRes = U.serialize(v)
                val base64 = Base64.getEncoder.encodeToString(serRes)
                
                if (key == null) base64 else NCCipher.encrypt(base64, key)
            }
            catch {
                case e: Exception ⇒ throw new NCE("Error sending data.", e)
            }

        rwLock.synchronized {
            writer.write(s"${data.length}\r\n")
            writer.write(data)

            writer.flush()
        }
    }
}