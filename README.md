<!--
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

<img src="https://nlpcraft.apache.org/images/nlpcraft_logo_black.gif" height="80px" alt="">
<br>

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/apache/opennlp/master/LICENSE)
[![Build](https://github.com/apache/incubator-nlpcraft/workflows/build/badge.svg)](https://github.com/apache/incubator-nlpcraft/actions)
[![Documentation Status](https://img.shields.io/:docs-latest-green.svg)](https://nlpcraft.apache.org/docs.html)
[![Gitter](https://badges.gitter.im/apache-nlpcraft/community.svg)](https://gitter.im/apache-nlpcraft/community)

## What is Apache NLPCraft?
[Apache NLPCraft](https://nlpcraft.apache.org/) is an open source library for adding a natural language interface 
for modern applications. It enables people to interact with your products using voice
or text: 

 * Download, Maven/Grape/Gradle/SBT, installation [instructions](https://nlpcraft.apache.org/download.html) 
 * Read [documentation](https://nlpcraft.apache.org/docs.html), latest [Javadoc](https://nlpcraft.apache.org/apis/latest/index.html) and [REST APIs](https://nlpcraft.apache.org/using-rest.html)
 * View & run [examples](https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples)
 * File a bug or improvement in [JIRA](https://issues.apache.org/jira/projects/NLPCRAFT)
 * Post a question at [Stack Overflow](https://stackoverflow.com/questions/ask) using <code>nlpcraft</code> tag
 * Join project developers on [dev@nlpcraft.apache.org](mailto:dev-subscribe@nlpcraft.apache.org)
 
## Why Natural Language?
Natural Language Interface enables users to interact with any type of products
using natural language augmenting existing UI/UX with fidelity and simplicity of a familiar spoken language.
Natural Language has no learning curve, no special rules or UI to master, no cumbersome syntax or 
terms to remember - it's just a natural interface that your users already know.

## Key Features
### Intent Definition Language
Advanced Intent Definition Language (IDL) coupled with deterministic intent matching
provide ease of use and unprecedented expressiveness for designing real-life, non-trivial intents.

### Composable Named Entities
Easily compose, mix and match new named entities out of built-in or external ones, creating new
reusable named entity recognizers on the fly.

### Short-Term-Memory
Advanced out-of-the-box support for maintaining and managing conversational context that is fully integrated with intent matching.
 
### Model-As-A-Code
Everything you do with NLPCraft is part of your source code. No more awkward web UIs
splitting your logic across different incompatible places. Model-as-a-code is built by
engineers, and it reflects how engineers work.

### By Devs - For Devs
Built with a singular focus - provide state-of-the-art developers with unprecedented productivity and efficiency when building
modern natural language applications.

### Java-First
REST API and Java-based implementation natively supports the world's largest ecosystem of development tools, multiple programming languages, frameworks and services.

<a target=_ href="https://www.oracle.com/java/"><img src="https://nlpcraft.apache.org/images/java2.png" height="32px" alt=""></a>
<a target=_ href="https://scala-lang.org/"><img src="https://nlpcraft.apache.org/images/scala-logo.png" height="24px" alt=""></a>
<a target=_ href="https://groovy-lang.org/"><img src="https://nlpcraft.apache.org/images/groovy.png" height="32px" alt=""></a>
<a target=_ href="https://kotlinlang.org/"><img src="https://nlpcraft.apache.org/images/kotlin.png" height="32px" alt=""></a>

### Out-Of-The-Box Integration
NLPCraft natively integrates with 3rd party libraries for base NLP processing and named entity recognition:

<a target=_ href="https://opennlp.apache.org"><img src="https://nlpcraft.apache.org/images/opennlp-logo.png" height="32px" alt=""></a>
<a target=_ href="https://cloud.google.com/natural-language/"><img src="https://nlpcraft.apache.org/images/google-cloud-logo-small.png" height="32px" alt=""></a>
<a target=_ href="https://stanfordnlp.github.io/CoreNLP"><img src="https://nlpcraft.apache.org/images/corenlp-logo.gif" height="48px" alt=""></a>
<a target=_ href="https://spacy.io"><img src="https://nlpcraft.apache.org/images/spacy-logo.png" height="32px" alt=""></a>

[Learn more >](https://nlpcraft.apache.org/docs.html)  

## How It Works
When using NLPCraft you will be dealing with three main components:

<img src="https://nlpcraft.apache.org/images/homepage-fig1.1.png" alt="ASF Logo">

**Data model** specifies how to interpret user input, how to query a data source, and how to format the result back. Developers use model-as-a-code approach to build models using any JVM language like Java, Scala, Groovy or Kotlin.

**Data probe** is a DMZ-deployed application designed to securely deploy and manage data models. Each probe can manage multiple models, and you can have many probes.

**REST server** provides REST endpoint for user applications to securely query data sources using NLI via data models deployed in data probes.
                    
[Learn more >](https://nlpcraft.apache.org/docs.html)          

## Example
As a quick example let's consider a very simple implementation for NLI-powered light switch. Our app should understand something like 
``Turn the lights off in the entire house`` or ``Switch on the illumination in the master bedroom closet``. You can easily 
modify intent callbacks in the model implementation below to perform the actual light switching using HomeKit or Arduino-based controllers.

### Add NLPCraft
Add NLPCraft dependency to your project:
```xml
<dependencies>
    <dependency>
        <groupId>org.apache.nlpcraft</groupId>
        <artifactId>nlpcraft</artifactId>
        <version>0.7.4</version>
    </dependency>
</dependencies>
```
NOTE: **0.7.4** should be the latest NLPCraft version.

### Define Data Model
Declare the static part of the data model using YAML which we will later load in our model implementation. You can declare entire
model in the code - but doing it with JSON or YAML is more productive:
```yaml
id: "nlpcraft.lightswitch.ex"
name: "Light Switch Example Model"
version: "1.0"
description: "NLI-powered light switch example model."
macros:
  - name: "<ACTION>"
    macro: "{turn|switch|dial|control|let|set|get|put}"
  - name: "<ENTIRE_OPT>"
    macro: "{entire|full|whole|total|*}"
  - name: "<LIGHT>"
    macro: "{all|*} {it|them|light|illumination|lamp|lamplight}"
enabledBuiltInTokens: [] # This example doesn't use any built-in tokens.
elements:
  - id: "ls:loc"
    description: "Location of lights."
    synonyms:
      - "<ENTIRE_OPT> {upstairs|downstairs|*} {kitchen|library|closet|garage|office|playroom|{dinning|laundry|play} room}"
      - "<ENTIRE_OPT> {upstairs|downstairs|*} {master|kid|children|child|guest|*} {bedroom|bathroom|washroom|storage} {closet|*}"
      - "<ENTIRE_OPT> {house|home|building|{1st|first} floor|{2nd|second} floor}"
 
  - id: "ls:on"
    groups:
      - "act"
    description: "Light switch ON action."
    synonyms:
      - "<ACTION> <LIGHT>"
      - "<ACTION> on <LIGHT>"
 
  - id: "ls:off"
    groups:
      - "act"
    description: "Light switch OFF action."
    synonyms:
      - "<ACTION> <LIGHT> {off|out}"
      - "{<ACTION>|shut|kill|stop|eliminate} {off|out} <LIGHT>"
      - "no <LIGHT>"
intents:
  - "intent=ls term(act)~{has(groups(), 'act')} term(loc)~{id() == 'ls:loc'}*"
```

### Model Implementation
Once we have model declaration we can provide implementation for intent callbacks. We'll use Scala to 
implement the data model, but you can use any JVM-based language like Java, Groovy, or Kotlin:
```scala
package org.apache.nlpcraft.examples.lightswitch
 
import org.apache.nlpcraft.model.{NCIntentTerm, _}
 
class LightSwitchModel extends NCModelFileAdapter("org/apache/nlpcraft/examples/lightswitch/lightswitch_model.yaml") {
    @NCIntentRef("ls")
    @NCIntentSample(Array(
        "Turn the lights off in the entire house.",
        "Switch on the illumination in the master bedroom closet.",
        "Get the lights on.",
        "Please, put the light out in the upstairs bedroom.",
        "Set the lights on in the entire house.",
        "Turn the lights off in the guest bedroom.",
        "Could you please switch off all the lights?",
        "Dial off illumination on the 2nd floor.",
        "Please, no lights!",
        "Kill off all the lights now!",
        "No lights in the bedroom, please."
    ))
    def onMatch(
        @NCIntentTerm("act") actTok: NCToken,
        @NCIntentTerm("loc") locToks: List[NCToken]
    ): NCResult = {
        val status = if (actTok.getId == "ls:on") "on" else "off"
        val locations =
            if (locToks.isEmpty)
                "entire house"
            else
                locToks.map(_.meta[String]("nlpcraft:nlp:origtext")).mkString(", ")
 
        // Add HomeKit, Arduino or other integration here.
 
        // By default - just return a descriptive action string.
        NCResult.text(s"Lights '$status' in '${locations.toLowerCase}'.")
    }
}
```
NOTES:
 - We are loading our static model declaration that we've defined above using `NCModelFileAdapter` base class.       
 - Annotation `@NCIntentRef` references the intent defined in our YAML model definition.  
 - We use `@NCIntentSample` to provide sample sentences that should satisfy this intent. These 
 samples are used for model auto-testing and synonyms analysis.
 
Done! 👌 

[Learn more about this example >](https://nlpcraft.apache.org/examples/light_switch.html)
 
## Copyright
Copyright (C) 2021 Apache Software Foundation

<img src="https://www.apache.org/img/ASF20thAnniversary.jpg" height="64px" alt="ASF Logo">


