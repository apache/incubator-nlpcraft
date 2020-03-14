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

package org.apache.nlpcraft.models.stm

import java.util

import org.apache.nlpcraft.model.{NCIntentMatch, _}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

/**
  * STM test model.
  */
class NCStmTestModel extends NCModelAdapter("nlpcraft.stm.test", "STM Test Model", "1.0") {
    private implicit def convert(s: String): NCResult = NCResult.text(s)

    override def getElements: util.Set[NCElement] =
        Set(
            mkElement("sale", Seq("A"), Seq.empty),
            mkElement("buy", Seq("A"), Seq.empty),
            mkElement("best_employee", Seq("A", "B"), Seq("best"))
        ).asJava

    private def mkElement(id: String, groups: Seq[String], syns: Seq[String]): NCElement =
        new NCElement {
            override def getId: String = id
            override def getSynonyms: util.List[String] = syns.asJava
            override def getGroups: util.List[String] = groups.asJava
        }

    @NCIntent("intent=sale term={id=='sale'}")
    private def onSale(ctx: NCIntentMatch): NCResult = "sale"

    @NCIntent("intent=buy term={id=='buy'}")
    private def onBuy(ctx: NCIntentMatch): NCResult = "buy"

    @NCIntent("intent=sale_best_employee term={id=='sale'} term={id=='best_employee'}")
    private def onBestEmployeeSale(ctx: NCIntentMatch): NCResult = "sale_best_employee"

    @NCIntent("intent=buy_best_employee term={id=='buy'} term={id=='best_employee'}")
    private def onBestEmployeeBuy(ctx: NCIntentMatch): NCResult = "buy_best_employee"
}
