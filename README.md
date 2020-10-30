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
[![build](https://github.com/apache/incubator-nlpcraft/workflows/build/badge.svg)](https://github.com/apache/incubator-nlpcraft/actions)
[![Documentation Status](https://img.shields.io/:docs-latest-green.svg)](https://nlpcraft.apache.org/docs.html)
[![Gitter](https://badges.gitter.im/apache-nlpcraft/community.svg)](https://gitter.im/apache-nlpcraft/community)

## What is Apache NLPCraft?
[Apache NLPCraft](https://nlpcraft.apache.org/) is an open source library for adding a natural language interface to any applications. 
Based on semantic modelling it allows rapid implementation and requires no model training or pre-existing text corpora:

 * Download, Maven/Grape/Gradle/SBT, installation [instructions](https://nlpcraft.apache.org/download.html) 
 * Read [documentation](https://nlpcraft.apache.org/docs.html)
 * View & run [examples](https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples)
 * Latest [Javadoc](http://nlpcraft.apache.org/apis/latest/index.html) and [REST APIs](https://nlpcraft.apache.org/using-rest.html)
 * File a bug or improvement in [JIRA](https://issues.apache.org/jira/projects/NLPCRAFT)
 * Post a question at [Stack Overflow](https://stackoverflow.com/questions/ask) using <code>nlpcraft</code> tag
 * Access [GitHub](https://github.com/apache/incubator-nlpcraft) mirror repository.
 * Join project developers on [dev@nlpcraft.apache.org](mailto:dev-subscribe@nlpcraft.apache.org)
 
## Why Natural Language?
Natural Language Interface (NLI) enables users to explore any type of data sources using natural language augmenting existing UI/UX with fidelity and simplicity of a familiar spoken language.
There is no learning curve, no special rules or UI to master, no cumbersome syntax or terms to remember - just a natural language that your users already speak.
 
## Key Features
### Semantic Modeling
Advanced semantic modelling and intent-based matching enables deterministic natural language understanding without requiring deep learning training or pre-existing text corpora.

### Any Data Source
Any data source, device, or service - public or private. From databases and SaaS systems, to smart home devices, voice assistants and chatbots.
 
### English Focused
NLPCraft focuses on processing English language delivering the ease of use and unparalleled comprehension for the language spoken by more than a billion people.

### By Devs - For Devs
Built with a singular focus - provide developers with unprecedented productivity and efficiency when building  modern NLI applications.

### Advanced NLP Toolset
Combines easy-of-use and development productivity with state-of-the-art NLP capabilities like short-term-memory (STM) support, advanced intent DSL with deterministic matching, and composable NEs.
                        
### Java-First
REST API and Java-based implementation natively supports the world's largest ecosystem of development tools, multiple programming languages, frameworks and services.

<a target=_ href="https://www.oracle.com/java/"><img src="https://nlpcraft.apache.org/images/java2.png" height="32px" alt=""></a>
<a target=_ href="https://scala-lang.org/"><img src="https://nlpcraft.apache.org/images/scala-logo.png" height="24px" alt=""></a>
<a target=_ href="https://groovy-lang.org/"><img src="https://nlpcraft.apache.org/images/groovy.png" height="32px" alt=""></a>
<a target=_ href="https://kotlinlang.org/"><img src="https://nlpcraft.apache.org/images/kotlin.png" height="32px" alt=""></a>

### Model-As-A-Code
Model-as-a-code convention natively supports any system development life cycle tools and frameworks in Java eco-system.

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
        <version>0.7.1</version>
    </dependency>
</dependencies>
```
NOTE: **0.7.1** should be the latest NLPCraft version.

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
  - "intent=ls term(act)~{groups @@ 'act'} term(loc)~{id == 'ls:loc'}*"
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

[Learn more about this example >](http://nlpcraft.apache.org/examples/light_switch.html)
 
## Copyright
Copyright (C) 2020 Apache Software Foundation

<img src="https://www.apache.org/img/ASF20thAnniversary.jpg" height="64px" alt="ASF Logo">


