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

package org.apache.nlpcraft.server.rest

import java.util
import java.util.UUID

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jayway.jsonpath.JsonPath
import org.apache.http.HttpResponse
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{AfterEach, BeforeEach}

import scala.collection.JavaConverters._

object NCRestSpec {
    private final val DFLT_BASEURL = "http://localhost:8081/api/v1/"
    private final val TYPE_RESP = new TypeToken[util.Map[String, Object]]() {}.getType
    private final val GSON = new GsonBuilder().setPrettyPrinting().create()
    private final val CLI = HttpClients.createDefault
    private final val HANDLER: ResponseHandler[java.util.Map[String, Object]] =
        (resp: HttpResponse) ⇒ {
            val code = resp.getStatusLine.getStatusCode
            val e = resp.getEntity

            val js = if (e != null) EntityUtils.toString(e) else null

            if (js == null)
                throw new RuntimeException(s"Unexpected empty response [code=$code]")

            code match {
                case 200 ⇒ GSON.fromJson(js, TYPE_RESP)

                case 400 ⇒ throw new RuntimeException(js)
                case _ ⇒ throw new RuntimeException(s"Unexpected response [code=$code, response=$js]")
            }
        }

    /**
      *
      * @param url
      * @param ps
      */
    private def post0(url: String, ps: (String, Any)*): Map[String, Object] = {
        val post = new HttpPost(DFLT_BASEURL + url)

        post.setHeader("Content-Type", "application/json")
        post.setEntity(new StringEntity(GSON.toJson(ps.filter(_._2 != null).toMap.asJava), "UTF-8"))

        try
            CLI.execute(post, HANDLER).asScala.toMap
        finally
            post.releaseConnection()
    }
}

import org.apache.nlpcraft.server.rest.NCRestSpec._

class NCRestSpec {
    final val TYPE_MAP = new TypeToken[util.Map[String, Object]]() {}.getType
    final val TYPE_LIST_MAP = new TypeToken[util.List[util.Map[String, Object]]]() {}.getType

    private var acsTok: String = _

    type DataMap = java.util.List[java.util.Map[String, Object]]
    type JList[T] = java.util.List[T]

    private def checkStatus(resp: Map[String, Object]): Unit = {
        assertTrue(resp.contains("status"))
        assertEquals("API_OK", resp("status"))
    }

    @BeforeEach
    def signin(): Unit = acsTok = signin("admin@admin.com", "admin")

    @AfterEach
    def signout(): Unit =
        if (acsTok != null) {
            signout(acsTok)

            acsTok = null
        }

    protected def signout(tkn: String): Unit = {
        val resp = post0("signout", "acsTok" → tkn)

        checkStatus(resp)
    }

    protected def signin(email: String, passwd: String): String = {
        val resp = post0("signin", "email" → email, "passwd" → passwd)

        val tkn = resp("acsTok").asInstanceOf[String]

        assertNotNull(tkn)

        tkn
    }

    protected def post[T](url: String, tkn: String, ps: (String, Any)*)(validations: (String, T ⇒ Unit)*): Unit = {
        val resp = post0(url, Seq("acsTok" → tkn) ++ ps:_*)

        checkStatus(resp)

        val js = GSON.toJson(resp.asJava)

        println(s"Request [url=$url, parameters=[${ps.map { case (k, v) ⇒ s"$k=$v" }.mkString(", ")}]")
        println("Response:")
        println(js)

        val ctx = JsonPath.parse(js)

        validations.foreach { case (name, check) ⇒
            val v: Object = ctx.read(name)

            println(s"Checked value [$name=$v]")

            check(v match {
                case arr: net.minidev.json.JSONArray ⇒ (0 until arr.size()).map(i ⇒ arr.get(i)).asJava.asInstanceOf[T]
                case _ ⇒ v.asInstanceOf[T]
            })
        }
    }


    protected def post[T](url: String, ps: (String, Any)*)(validations: (String, T ⇒ Unit)*): Unit = {
        assertNotNull(acsTok)

        post(url, acsTok, ps:_*)(validations:_*)
    }

    /**
      *
      * @param data
      * @param field
      * @param extract
      * @param expected
      */
    protected def contains[T](data: DataMap, field: String, extract: Object ⇒ T, expected: T): Boolean =
        data.asScala.exists(p ⇒ extract(p.get(field)) == expected)

    /**
      *
      * @param data
      * @param field
      * @param expected
      */
    protected def containsLong(data: DataMap, field: String, expected: Long): Boolean =
        contains(data, field, (o: Object) ⇒ o.asInstanceOf[Number].longValue(), expected)

    /**
      *
      * @return
      */
    protected def rnd(): String = UUID.randomUUID().toString
}
