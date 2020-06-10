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

import java.util
import com.google.gson.Gson
import org.apache.nlpcraft.common.util.NCUtils
import org.apache.nlpcraft.model._

import scala.collection.JavaConverters._

/*
 * NOTE: these classes are specifically designed for JSON marshalling.
 */

case class NCLogGroupToken(token: NCToken, used: Boolean, conversation: Boolean)

import org.apache.nlpcraft.common.debug.NCLogHolder._

/**
  * Log data holder.
  */
class NCLogHolder extends Serializable {
    private val intents = new util.ArrayList[IntentJson]()
    private var queryContext: ContextJson = _

    case class TokenJson(
        metadata: util.Map[String, Object],
        serverRequestId: String,
        id: String,
        parentId: String,
        value: String,
        groups: util.List[String]
    )
    
    case class UserJson(
        id: Long,
        firstName: String,
        lastName: String,
        email: String,
        avatarUrl: String,
        isAdmin: Boolean,
        signupTimestamp: Long,
        properties: util.Map[String, String]
    )
    
    case class CompanyJson(
        id: Long,
        name: String,
        website: String,
        country: String,
        city: String,
        region: String,
        postalCode: String,
        address: String
    )

    case class RequestJson(
        serverRequestId: String,
        normalizedText: String,
        receiveTimestamp: Long,
        user: UserJson,
        company: CompanyJson,
        remoteAddress: String,
        clientAgent: String,
        data: Object
    )
    
    case class ContextJson(
        request: RequestJson,
        variants: util.Collection[util.List[TokenJson]],
        convTokens: util.List[TokenJson]
    )

    case class GroupTokenJson(
        token: TokenJson,
        used: Boolean,
        conversation: Boolean
    )

    case class IntentJson(
        id: String,
        exactMatch: Boolean,
        weight: util.List[Int],
        tokensGroups: util.Map[String, util.List[GroupTokenJson]],
        var best: Boolean
    )
    /**
      *
      * @param p
      */
    private def convert(p: NCToken): TokenJson =
        TokenJson(
            metadata = p.getMetadata,
            serverRequestId = p.getServerRequestId,
            id = p.getId,
            parentId = p.getParentId,
            value = p.getValue,
            groups = p.getGroups
        )

    /**
      * Adds intent data.
      *
      * @param id Intent ID.
      * @param exactMatch Exact match flag.
      * @param weight Weigh.
      * @param groups Groups.
      */
    def addIntent(id: String, exactMatch: Boolean, weight: Seq[Int], groups: Map[String, Seq[NCLogGroupToken]]): Unit =
        intents.add(
            IntentJson(
                id = id,
                exactMatch = exactMatch,
                weight = weight.asJava,
                tokensGroups = groups.map { case (group, toks) ⇒
                    group → toks.map(g ⇒ GroupTokenJson(convert(g.token), g.used, g.conversation)).asJava
                }.asJava,
                best = false
            )
        )

    /**
      * Sets matched intent index.
      *
      * @param idx Index.
      */
    def setMatchedIntentIndex(idx: Int): Unit = intents.get(idx).best = true

    /**
      * Sets query context.
      *
      * @param ctx Query context.
      */
    def setContext(ctx: NCContext): Unit = {
        val req = ctx.getRequest
        val usr = req.getUser
        val comp = req.getCompany
        
        val usrJs = UserJson(
            id = usr.getId,
            firstName = usr.getFirstName.orElse(null),
            lastName = usr.getLastName.orElse(null),
            email = usr.getEmail.orElse(null),
            avatarUrl = usr.getAvatarUrl.orElse(null),
            isAdmin = usr.isAdmin,
            signupTimestamp = usr.getSignupTimestamp,
            properties = usr.getProperties.orElse(null)
        )
    
        val compJs = CompanyJson(
            id = comp.getId,
            name = comp.getName,
            website = comp.getWebsite.orElse(null),
            country = comp.getCountry.orElse(null),
            region = comp.getRegion.orElse(null),
            address = comp.getAddress.orElse(null),
            city = comp.getCity.orElse(null),
            postalCode = comp.getPostalCode.orElse(null)
        )

        val reqJs = RequestJson(
            serverRequestId = req.getServerRequestId,
            normalizedText = req.getNormalizedText,
            receiveTimestamp = req.getReceiveTimestamp,
            user = usrJs,
            company = compJs,
            remoteAddress = req.getRemoteAddress.orElse(null),
            clientAgent = req.getClientAgent.orElse(null),
            data = if (req.getData.isPresent) {
                val str = req.getData.get

                try
                    NCUtils.js2Obj(str)
                catch {
                    case _: Exception ⇒ str
                }
            }
            else
                null
        )
        
        this.queryContext = ContextJson(
            request = reqJs,
            variants = ctx.getVariants.asScala.map(seq ⇒ seq.asScala.map(convert).asJava).toSeq.asJava,
            convTokens = ctx.getConversation.getTokens.asScala.map(convert).asJava
        )
    }
    
    def toJson: String = GSON.toJson(this)
}

/**
  * Log data holder helper.
  */
object NCLogHolder {
    private final val GSON = new Gson()
}


