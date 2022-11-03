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

package org.apache.nlpcraft.examples.lightswitch

import org.apache.nlpcraft.*
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Using

/**
  * Model validation.
  */
class LightSwitchRuModelSpec extends AnyFunSuite:
    test("test") {
        Using.resource(new NCModelClient(new LightSwitchRuModel)) { client =>
            def check(txt: String): Unit = 
                require(client.debugAsk(txt, "userId", true).getIntentId == "ls")

            check("Выключи свет по всем доме")
            check("Выруби электричество!")
            check("Включи свет в детской")
            check("Включай повсюду освещение")
            check("Включайте лампы в детской комнате")
            check("Свет на кухне, пожалуйста, приглуши")
            check("Нельзя ли повсюду выключить свет?")
            check("Пожалуйста без света")
            check("Отключи электричество в ванной")
            check("Выключи, пожалуйста, тут всюду свет")
            check("Выключай все!")
            check("Свет пожалуйста везде включи")
            check("Зажги лампу на кухне")
        }
    }