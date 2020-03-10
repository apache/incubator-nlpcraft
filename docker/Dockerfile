#
#  "Commons Clause" License, https://commonsclause.com/
#
#  The Software is provided to you by the Licensor under the License,
#  as defined below, subject to the following condition.
#
#  Without limiting other conditions in the License, the grant of rights
#  under the License will not include, and the License does not grant to
#  you, the right to Sell the Software.
#
#  For purposes of the foregoing, "Sell" means practicing any or all of
#  the rights granted to you under the License to provide to third parties,
#  for a fee or other consideration (including without limitation fees for
#  hosting or consulting/support services related to the Software), a
#  product or service whose value derives, entirely or substantially, from
#  the functionality of the Software. Any license notice or attribution
#  required by the License must also include this Commons Clause License
#  Condition notice.
#
#  Software:    NLPCraft
#  License:     Apache 2.0, https://www.apache.org/licenses/LICENSE-2.0
#  Licensor:    Copyright (C) NLPCraft. https://nlpcraft.org
#
#      _   ____      ______           ______
#     / | / / /___  / ____/________ _/ __/ /_
#    /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/
#   / /|  / / /_/ / /___/ /  / /_/ / __/ /_
#  /_/ |_/_/ .___/\____/_/   \__,_/_/  \__/
#         /_/
#
FROM openjdk:8-alpine
LABEL Software="https://nlpcraft.org"
LABEL License="Apache 2.0, https://www.apache.org/licenses/LICENSE-2.0"
LABEL Licensor="Copyright (C) NLPCraft. https://www.nlpcraft.org"
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