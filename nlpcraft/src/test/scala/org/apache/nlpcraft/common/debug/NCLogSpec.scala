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

package org.apache.nlpcraft.common.debug

import com.google.gson.reflect.TypeToken
import com.google.gson.{GsonBuilder, JsonElement}
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.nlpcraft.NCTestEnvironment
import org.apache.nlpcraft.common.JavaMeta
import org.apache.nlpcraft.model.meta.NCMetaSpecAdapter
import org.apache.nlpcraft.model.tools.test.NCTestClientBuilder._
import org.apache.nlpcraft.model.{NCIntent, NCIntentMatch, NCModelAdapter, NCResult}
import org.junit.jupiter.api.Test

import java.lang.Boolean.TRUE
import java.lang.reflect.Type
import java.util
import scala.collection.JavaConverters._
import scala.language.implicitConversions

object NCLogSpecModel {
    val MDL_ID = "nlpcraft.any.model.test"
    val RESULT = "OK"
}

import org.apache.nlpcraft.common.debug.NCLogSpecModel._

/**
  */
class NCLogSpecModel extends NCModelAdapter(MDL_ID, "IDL Test Model", "1.0") {
    @NCIntent("intent=i term(a)={tok_id()=='nlpcraft:nlp'}")
    private def callback(ctx: NCIntentMatch): NCResult = NCResult.text(RESULT)
}

/**
  * Log test.
  */
@NCTestEnvironment(model = classOf[NCLogSpecModel], startClient = false)
class NCLogSpec extends NCMetaSpecAdapter {
    private val CLIENT = HttpClients.createDefault
    private val GSON = new GsonBuilder().setPrettyPrinting().create
    private val TYPE_RESP = new TypeToken[util.HashMap[String, AnyRef]]() {}.getType
    private val TYPE_LOG = new TypeToken[NCLogHolder]() {}.getType

    private def postHttp(url: String, params: (String, AnyRef)*): String = {
        val post = new HttpPost(DFLT_BASEURL + url)

        try {
            post.setHeader("Content-Type", "application/json")
            post.setEntity(new StringEntity(GSON.toJson(params.toMap.asJava), "UTF-8"))

            CLIENT.execute(
                post,
                (resp: HttpResponse) => {
                    val code = resp.getStatusLine.getStatusCode
                    val entity = resp.getEntity

                    code match {
                        case 200 => EntityUtils.toString(entity)
                        case _ => throw new Exception(s"Unexpected response [code=$code, entity=$entity]")
                    }
                }
            )
        }
        finally
            post.releaseConnection()
    }

    private def extract[T](js: JsonElement, t: Type): T = GSON.fromJson(js, t)
    private def getField[T](m: util.Map[String, AnyRef], fn: String): T = m.get(fn).asInstanceOf[T]

    private def ask(txt: String): Map[String, Any] = {
        require(tkn != null)

        val res: java.util.HashMap[String, Any] = extract(
            GSON.toJsonTree(
                getField(
                    GSON.fromJson(
                        postHttp("ask/sync", "acsTok" -> tkn, "txt" -> txt, "mdlId" -> MDL_ID, "enableLog" -> TRUE),
                        TYPE_RESP
                    ),
                    "state"
                )
            ),
            classOf[java.util.HashMap[String, Any]]
        )

        val m = res.asScala.toMap

        require(m("resBody") == RESULT, s"Unexpected result: ${m("resBody")}")

        m
    }

    private def check(meta: MetaHolder): Unit = {
        val data = ask("test")

        val log: NCLogHolder =
            GSON.fromJson(GSON.toJson(data("logHolder").asInstanceOf[JavaMeta]), TYPE_LOG)

        val ctx = log.queryContext

        println(s"Company meta=${ctx.request.company.meta}")
        println(s"User meta=${ctx.request.user.meta}")

        def norm(m: JavaMeta): JavaMeta = if (m != null && m.isEmpty) null else m

        require(
            norm(ctx.request.company.meta) == norm(meta.companyMeta),
            s"Unexpected company meta [expected=${meta.companyMeta}, meta=${ctx.request.company.meta}"
        )
        require(
            norm(ctx.request.user.meta) == norm(meta.userMeta),
            s"Unexpected user meta [expected=${meta.userMeta}, meta=${ctx.request.user.meta}"
        )
    }

    @Test
    def testLogMeta(): Unit = {
        val meta = getMeta()

        try {
            check(meta)

            def mkMeta(k: String, v: Object): JavaMeta = Map(k -> v).asJava

            val newMeta = MetaHolder(userMeta = mkMeta("userKey", "v1"), companyMeta = mkMeta("compKey", "v2"))

            setMeta(newMeta)

            check(newMeta)
        }
        finally {
            setMeta(meta)

            check(meta)
        }
    }
}

