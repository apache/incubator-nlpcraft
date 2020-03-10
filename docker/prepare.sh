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
#!/usr/bin/env bash

if [[ $1 = "" ]] ; then
    echo "Dockerhub username must be defined as the first parameter."
    exit -1
fi

if [[ $2 = "" ]] ; then
    echo "Image must be defined as the second parameter."
    exit -1
fi

if [[ $3 = "" ]] ; then
    echo "Image version must be defined as the third parameter."
    exit -1
fi

export JF=nlpcraft-${3}-all-deps.jar

echo "Dockerhub username: ${1}, image: ${2}, version: ${3}, deployed jar: ${JF}"

cp ../target/${JF} ${JF}
rc=$?
if [[ ${rc} != 0 ]] ; then
    echo "Error copying: ../target/${JF}"
    exit ${rc}
fi

docker build --build-arg JAR_FILE=${JF} -t ${1}/${2}:${3} .

rm ${JF}