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

package org.apache.nlpcraft.probe.mgrs

import java.io._

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ascii._

import scala.collection.mutable

/**
  * Probe-server protocol message. Every message has at least these values: TYPE, GUID, TSTAMP.
  *
  * @param typ Type (name) of the message.
  */
class NCProbeMessage(val typ: String) extends mutable.HashMap[String/*Name*/, Serializable/*Value*/]
    with Serializable with NCAsciiLike {
    private val guid = U.genGuid()
    private val hash = guid.hashCode()
    
    put("TYPE", typ)
    put("GUID", guid)
    
    override def equals(obj: Any): Boolean = obj match {
        case msg: NCProbeMessage ⇒ msg.guid == guid
        case _ ⇒ false
    }
    
    override def hashCode(): Int = hash
    
    // Shortcuts.
    def getType: String = typ
    def getGuid: String = data[String]("GUID") // Message GUID.
    def getProbeToken: String = data[String]("PROBE_TOKEN")
    def getProbeId: String = data[String]("PROBE_ID")
    def getProbeGuid: String = data[String]("PROBE_GUID") // Probe GUID.
    
    def setProbeToken(tkn: String): NCProbeMessage = {
        put("PROBE_TOKEN", tkn)
        
        this
    }
    def setProbeId(id: String): NCProbeMessage = {
        put("PROBE_ID", id)
        
        this
    }
    def setProbeGuid(guid: String): NCProbeMessage = {
        put("PROBE_GUID", guid)
        
        this
    }
    
    /**
      *
      * @param key Map key.
      * @tparam T Return value type.
      * @return Map value (including `null` values)
      */
    def data[T](key: String): T =
        dataOpt[T](key) match {
            case None ⇒ throw new AssertionError(s"Probe message missing key [key=$key, data=$this]")
            case Some(x) ⇒ x.asInstanceOf[T]
        }
    
    /**
      *
      * @param key Map key.
      * @tparam T Return value type.
      * @return `None` or `Some` map value (including `null` values).
      */
    def dataOpt[T](key: String): Option[T] =
        get(key) match {
            case None ⇒ None
            case Some(x) ⇒ x match {
                case None | null ⇒ None
                case z ⇒ Some(z.asInstanceOf[T])
            }
        }
    
    override def toAscii: String =
        iterator.toSeq.sortBy(_._1).foldLeft(NCAsciiTable("Key", "Value"))((t, p) ⇒ t += p).toString
    
    override def toString(): String =
        iterator.toSeq.sortWith((t1, t2) ⇒ {
            if (t1._1 == "TYPE")
                true
            else if (t2._1 == "TYPE")
                false
            else
                t1._1.compare(t2._1) <= 0
        }).map(t ⇒ s"${t._1} -> ${t._2}").mkString("{", ", ", "}")
}

object NCProbeMessage {
    /**
      *
      * @param typ Message type.
      * @param pairs Parameters.
      */
    def apply(typ: String, pairs: (String, Serializable)*): NCProbeMessage = {
        val impl = new NCProbeMessage(typ)
    
        for ((k, v) ← pairs)
            impl.put(k, v)
        
        impl
    }
}