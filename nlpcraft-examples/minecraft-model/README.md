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
Seconds part (**../minecraft-mod**) provides mod for Minecraft server sending requests to the NLPCraft server.

### Startup
Start server normally. For running probe it's required to use dedicated configuration located in [resouces folder](src/main/resources/nlpcraft.conf)

### Installation
1. Download [Minecraft client](https://www.minecraft.net/en-us/download)
1. Download [Forge server installer](https://files.minecraftforge.net/) and follow instructions
1. Build mod (`cd ../minecraft-mod && ./gradlew clean build`)
1. Copy mod to mods folder of your forge server folder (`cp build/libs/nlpcraft-mod-*.jar <forge-server-location>/mods`)
1. (Optional) If non-default settings are used, put them in `main/resources/nlpcraft-settings.json` and copy file to `<forge-server-location>/config`
1. Start server (`java -jar forge.jar`). For detailed instructions refer to [wiki](https://minecraft.gamepedia.com/Tutorials/Setting_up_a_server)
1. Connect to the server from client and play!

### Usage
After starting Minecraft server with mod, you can use natural language to invoke certain commands. It's not required to
use modded client, so vanilla client could be used. Commands could be either invoked on server side (e.g. `make it sunny`) or
on client side, prefixed with slash (`/make it sunny`)

### Supported commands
| Command | Example of usage |
| :---: |:---:|
`/weather` | All those moments will be lost in time, like tears in rain | 

### Copyright
Copyright (C) 2020 Apache Software Foundation

<img src="https://www.apache.org/img/ASF20thAnniversary.jpg" height="64px" alt="ASF Logo">
