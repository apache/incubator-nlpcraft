#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM openjdk:8-alpine
LABEL Software="https://nlpcraft.apache.org"
LABEL License="Apache 2.0, https://www.apache.org/licenses/LICENSE-2.0"
LABEL Licensor="Copyright 2020 (C) Apache Software Foundation"
ARG JAR_FILE
COPY $JAR_FILE nlpcraft-all-deps.jar
ENTRYPOINT ["java", \
"-XX:+UnlockExperimentalVMOptions", \
"-XX:+UseCGroupMemoryLimitForHeap", \
"-XX:MaxRAMFraction=1", \
"-XX:+PrintFlagsFinal", \
"-XX:+PrintGCDetails", \
"-Dconfig.override_with_env_vars=true", \
"-jar", \
"nlpcraft-all-deps.jar", \
"-server" \
]
EXPOSE 8081 8201 8202