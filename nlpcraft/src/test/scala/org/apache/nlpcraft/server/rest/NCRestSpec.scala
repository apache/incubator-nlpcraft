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
import com.jayway.jsonpath.{JsonPath, PathNotFoundException}
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.http.{HttpEntity, HttpResponse}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{AfterEach, BeforeEach}

import scala.collection.JavaConverters._

private[rest] object NCRestSpec {
    private final val DFLT_BASEURL = "http://localhost:8081/api/v1/"
    private final val DFLT_ADMIN_EMAIL = "admin@admin.com"
    private final val DFLT_ADMIN_PSWD = "admin"

    private final val TYPE_RESP = new TypeToken[util.Map[String, Object]]() {}.getType
    private final val GSON = new GsonBuilder().setPrettyPrinting().create()
    private final val CLI = HttpClients.createDefault
    private final val HANDLER: ResponseHandler[java.util.Map[String, Object]] =
        (resp: HttpResponse) ⇒ {
            val code = resp.getStatusLine.getStatusCode
            val js = mkJs(code, resp.getEntity)

            code match {
                case 200 ⇒ GSON.fromJson(js, TYPE_RESP)

                case 400 ⇒ throw new RuntimeException(js)
                case _ ⇒ throw new RuntimeException(s"Unexpected response [code=$code, response=$js]")
            }
        }

    /**
      *
      * @param expCode
      * @tparam T
      * @return
      */
    private def mkErrorHandler[T](expCode: Int): ResponseHandler[Int] =
        (resp: HttpResponse) ⇒ {
            val code = resp.getStatusLine.getStatusCode
            val js = mkJs(code, resp.getEntity)

            if (expCode != code)
                throw new RuntimeException(s"Unexpected response [expectedCode=$expCode, code=$code, js=$js]")

            println(js)

            code
        }

    /**
      *
      * @param code
      * @param e
      * @return
      */
    private def mkJs(code: Int, e: HttpEntity): String = {
        if (e == null)
            throw new RuntimeException(s"Unexpected empty response [code=$code]")

        EntityUtils.toString(e)
    }

    /**
      *
      * @param url
      * @param ps
      */
    private def post0(url: String, ps: (String, Any)*): Map[String, Object] = {
        val post = preparePost(url, ps: _*)

        val m =
            try
                CLI.execute(post, HANDLER)
            finally
                post.releaseConnection()

        m.asScala.toMap
    }

    /**
      *
      * @param url
      * @param ps
      * @return
      */
    private def preparePost(url: String, ps: (String, Any)*) = {
        val post = new HttpPost(DFLT_BASEURL + url)

        post.setHeader("Content-Type", "application/json")
        post.setEntity(new StringEntity(GSON.toJson(ps.filter(_._2 != null).toMap.asJava), "UTF-8"))

        post
    }

    /**
      *
      * @param resp
      */
    private def checkStatus(resp: Map[String, Object]): Unit = {
        assertTrue(resp.contains("status"))
        assertEquals("API_OK", resp("status"))
    }
}

import org.apache.nlpcraft.server.rest.NCRestSpec._

private[rest] class NCRestSpec {
    type ResponseContent = java.util.Map[String, Object]
    type ResponseList = java.util.List[ResponseContent]
    type JList[T] = java.util.List[T]

    private var tkn: String = _

    /**
      *
      */
    @BeforeEach
    def signin(): Unit = tkn = signin(DFLT_ADMIN_EMAIL, DFLT_ADMIN_PSWD)

    /**
      *
      */
    @AfterEach
    def signout(): Unit =
        if (tkn != null) {
            signout(tkn)

            tkn = null
        }

    /**
      *
      * @param email
      * @param passwd
      */
    protected def signin(email: String, passwd: String): String = {
        val tkn = post0("signin", "email" → email, "passwd" → passwd)("acsTok").asInstanceOf[String]

        assertNotNull(tkn)

        tkn
    }

    /**
      *
      * @param tkn
      */
    protected def signout(tkn: String): Unit = checkStatus(post0("signout", "acsTok" → tkn))

    /**
      *
      * @param url
      * @param ps
      * @param validations
      */
    protected def post[T](url: String, ps: (String, Any)*)(validations: (String, T ⇒ Unit)*): Unit = {
        assertNotNull(tkn)

        post(url, tkn, ps: _*)(validations: _*)
    }

    /**
      *
      * @param url
      * @param tkn
      * @param ps
      * @param validations
      */
    protected def post[T](url: String, tkn: String, ps: (String, Any)*)(validations: (String, T ⇒ Unit)*): Unit = {
        val resp = post0(url, Seq("acsTok" → tkn) ++ ps: _*)

        checkStatus(resp)

        println("Checked POST:")
        println(GSON.toJson(
            Map(
                "url" → url,
                "params" → new java.util.HashMap[String, Any](ps.toMap.asJava),
                "response" → resp.asJava
            ).asJava
        ))

        val ctx = JsonPath.parse(GSON.toJson(resp.asJava))

        validations.foreach { case (field, validation) ⇒
            val v: Object =
                try
                    ctx.read(field)
                catch {
                    case _: PathNotFoundException ⇒ null
                }

            println(s"Validating value [$field=$v]")

            validation(v match {
                case arr: net.minidev.json.JSONArray ⇒ (0 until arr.size()).map(i ⇒ arr.get(i)).asJava.asInstanceOf[T]
                case _ ⇒ v.asInstanceOf[T]
            })
        }

        println()
    }

    /**
      *
      * @param url
      * @param errCode
      * @param ps
      */
    protected def postError(url: String, errCode: Int, ps: (String, Any)*): Unit = {
        assertNotNull(tkn)

        val post = preparePost(url, Seq("acsTok" → tkn) ++ ps: _*)

        try
            CLI.execute(post, mkErrorHandler(errCode))
        finally
            post.releaseConnection()
    }

    /**
      *
      * @param data
      * @param field
      * @param expected
      */
    protected def containsLong(data: ResponseList, field: String, expected: Long): Boolean =
        contains(data, field, (o: Object) ⇒ o.asInstanceOf[Number].longValue(), expected)

    /**
      *
      * @param data
      * @param field
      * @param expected
      */
    protected def containsStr(data: ResponseList, field: String, expected: String): Boolean =
        contains(data, field, (o: Object) ⇒ o.asInstanceOf[String], expected)

    /**
      *
      * @param data
      * @param field
      * @param extract
      * @param expected
      */
    protected def contains[T](data: ResponseList, field: String, extract: Object ⇒ T, expected: T): Boolean =
        data.asScala.exists(p ⇒ extract(p.get(field)) == expected)

    /**
      *
      * @param data
      * @param field
      * @param extract
      * @param expected
      */
    protected def count[T](data: ResponseList, field: String, extract: Object ⇒ T, expected: T): Int =
        data.asScala.count(p ⇒ extract(p.get(field)) == expected)

    /**
      *
      */
    protected def rnd(): String = UUID.randomUUID().toString
}
