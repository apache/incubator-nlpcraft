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

### Overview
This module is part of Minecraft example. This part provides model for Minecraft server commands. 
Second part (**../minecraft-mod**) provides mod for Minecraft server sending requests to the NLPCraft server.

### Installation
1. Download [Minecraft client](https://www.minecraft.net/en-us/download) and install it on your local machine. This is the actual
   game. Note that Minecraft is NOT a free game, and you will need to purchase it and have an account to play it.
1. Download Forge Server. Minecraft is a client-server game. This is the server to which the Minecraft client will connect to and where the NLPCraft's Minecraft mod will be
   installed. [Download](https://github.com/apache/incubator-nlpcraft/raw/master/nlpcraft-examples/minecraft/assets/forge-1.16.5-36.1.0-installer.jar) 
   the version 1.16.5-36.1.0 or grab the latest from [here](https://files.minecraftforge.net/) (warning: _this download location, however,
   is full or harmful online ads and banners_). 
1. Install downloaded Forge Server by going to your download location and running `java -jar forge-1.16.5-36.1.0-installer.jar`. Make sure
   to select 'Install Server' when presented with options. Note the location of the installed Forge Server (default or selected by you) in the last line of the 
   installer log output. For example, on Windows it installs by default to `C:\Users\User\AppData\Roaming\.minecraft`.
   For convenience, set `%FORGE_SRV%` variable to point to this location.
1. Copy pre-built NLPCraft's Minecraft mod to Forge Server `mods` folder. Pre-built mod is located in `assets` sub-folder of the `minecraft-mod` module. To copy, first 
   create folder `%FORGE_SRV%\mods` and run this 
   from the command line `cp .\nlpcraft-examples\minecraft-mod\assets\nlpcraft-example-minecraft-mod-1.0.jar %FORGE_SRV%\mods`

### Start
1. Start NLPCraft server in a [standard](http://nlpcraft.apache.org/server-and-probe.html#server) way.
1. Start NLPCraft probe with Minecraft model in a [standard](http://nlpcraft.apache.org/server-and-probe.html#probe) way (from `minecraft` example).
1. Accept Forge Server EULA by opening `%FORGE_SRV%\eula.txt` file and changing `eula=false` to `eula-=true`.
1. Start Forge Server from the `%FORGE_SRV%` location: `java -jar .\forge-1.16.5-36.1.0.jar`. 
1. Start Minecraft Client and login with your Minecraft account.
1. Choose 'Multiplayer' -> 'Add Server' and add '127.0.0.1' local server. Double-click on the newly added server to connect to it.
1. Play Minecraft! 🤘

### Usage
Natural language commands could be entered either on Forge Server (e.g. `make it sunny`) or in the game itself, i.e. on the Minecraft client side, 
prefixed with slash (`/make it sunny`). See file `MinecraftModel.kt` file for some examples of possible commands.

### Documentation
For any questions, feedback or suggestions:

* View & run other [examples](https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft-examples)
* Read [documentation](https://nlpcraft.apache.org/docs.html), latest [Javadoc](https://nlpcraft.apache.org/apis/latest/index.html) and [REST APIs](https://nlpcraft.apache.org/using-rest.html)
* Download & Maven/Grape/Gradle/SBT [instructions](https://nlpcraft.apache.org/download.html)
* File a bug or improvement in [JIRA](https://issues.apache.org/jira/projects/NLPCRAFT)
* Post a question at [Stack Overflow](https://stackoverflow.com/questions/ask) using <code>nlpcraft</code> tag
* Access [GitHub](https://github.com/apache/incubator-nlpcraft) mirror repository.
* Join project developers on [dev@nlpcraft.apache.org](mailto:dev-subscribe@nlpcraft.apache.org)


### Copyright
Copyright (C) 2020 Apache Software Foundation

<img src="https://www.apache.org/img/ASF20thAnniversary.jpg" height="64px" alt="ASF Logo">
