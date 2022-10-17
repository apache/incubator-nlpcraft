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
  * JUnit models validation.
  */
class LightSwitchFrModelSpec extends AnyFunSuite:
    test("test") {
        Using.resource(new NCModelClient(new LightSwitchFrModel)) { client =>
            def check(txt: String): Unit =
                require(client.debugAsk(txt, "userId", true).getIntentId == "ls")

            check("Éteignez les lumières dans toute la maison.")
            check("Éteignez toutes les lumières maintenant.")
            check("Allumez l'éclairage dans le placard de la chambre des maîtres.")
            check("Éteindre les lumières au 1er étage.")
            check("Allumez les lumières.")
            check("Allumes dans la cuisine.")
            check("S'il vous plait, éteignez la lumière dans la chambre à l'étage.")
            check("Allumez les lumières dans toute la maison.")
            check("Éteignez les lumières dans la chambre d'hôtes.")
            check("Pourriez-vous éteindre toutes les lumières s'il vous plait?")
            check("Désactivez l'éclairage au 2ème étage.")
            check("Éteignez les lumières dans la chambre au 1er étage.")
            check("Lumières allumées à la cuisine du deuxième étage.")
            check("S'il te plaît, pas de lumières!")
            check("Coupez toutes les lumières maintenant!")
            check("Éteindre les lumières dans le garage.")
            check("Lumières éteintes dans la cuisine!")
            check("Augmentez l'éclairage dans le garage et la chambre des maîtres.")
            check("Baissez toute la lumière maintenant!")
            check("Pas de lumières dans la chambre, s'il vous plait.")
            check("Allumez le garage, s'il vous plait.")
            check("Tuez l'illumination maintenant.")
        }
    }