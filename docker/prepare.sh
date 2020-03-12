#!/bin/bash
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