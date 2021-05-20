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

<img src="https://nlpcraft.apache.org/images/nlpcraft_logo_black.gif" height="80px">
<br>

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/apache/opennlp/master/LICENSE)
[![Documentation Status](https://img.shields.io/:docs-latest-green.svg)](https://nlpcraft.apache.org/docs.html)
[![Gitter](https://badges.gitter.im/apache-nlpcraft/community.svg)](https://gitter.im/apache-nlpcraft/community)

### Docker
Starting with version 0.7.3 NLPCraft provides docker image for NLPCraft server. You can 
also prepare your own images following the instructions below. 

#### Using Server Image
 1. [Install](https://docs.docker.com/install/) docker.
 2. Pull actual NLPCraft server image: ```docker pull nlpcraftserver/server:1.0.0```, 
 where `1.0.0` is the desired version.
 3. Run image: ```docker run -m 8G -p 8081:8081 -p 8201:8201 -p 8202:8202 nlpcraftserver/server:1.0.0```

**NOTE**:
 Default docker image run REST server with default configuration (see file `build/nlpcraft.conf`). See [configuration](https://nlpcraft.apache.org/server-and-probe.html)
 documentation on how to override default configuration using environment variables. 

#### Your Own Server Image
You can prepare your own NLPCraft server image following these instructions:
 1. Build the release with maven: ```mvn clean package -P release```  
 2. Prepare docker image with `prepare.sh` (modify it if necessary):
   ```./prepare.sh docker_acct server 1.0.0```, where
    * `1.0.0` - maven build version, prepared on previous step.
    * `docker_acct` - change it to your own [DockerHub](https://hub.docker.com) account.    
 
    Note that docker image name will be `docker_acct/server:1.0.0`   
 3. Login to [DockerHub](https://hub.docker.com): ```docker login```   
 4. Push prepared docker image: ```docker push docker_acct/server:1.0.0```  
 5. Logout from [DockerHub](https://hub.docker.com): ```docker logout```

**NOTE**:
 1. Under MacOS - increase docker limit [resources configuration](https://docs.docker.com/config/containers/resource_constraints/). NLPCraft server requires more memory
than allocated by default to the docker containers. Set memory value to 8G (`-m 8G`). 
          
### Copyright
Copyright (C) 2021 Apache Software Foundation

<img src="https://www.apache.org/img/ASF20thAnniversary.jpg" height="64px" alt="ASF Logo">


