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

# Make sure that maven installed on your PC.

if [[ $1 = "" ]] ; then
    echo "Version must be set as input parameter."
    exit 1
fi

#
# Change this for your local GPG fingerprint:
# ===========================================
localUser=6374898D3A9757F80D3C88B310BABC80DA5CFD15

#
# Scala version:
# ===========================================
scalaVer=3.2.2

zipDir=zips
tmpDir=apache-nlpcraft
zipFileBin=apache-nlpcraft-incubating-bin-$1.zip # NOT an official ASF release.
zipFileSrc=apache-nlpcraft-incubating-$1.zip # An OFFICIAL ASF release.
coreModule=nlpcraft
stanfordModule=nlpcraft-stanford
examples=nlpcraft-examples
exampleLightswitch=${examples}/lightswitch
exampleLightswitchRu=${examples}/lightswitch-ru
exampleLightswitchFr=${examples}/lightswitch-fr
exampleTime=${examples}/time
exampleCalculator=${examples}/calculator
examplePizzeria=${examples}/pizzeria

curDir=$(pwd)

cd ../

mvn clean package -DskipTests=true -P stanford-core,release,examples
sbt doc

rm -R ${zipDir} 2> /dev/null

mkdir -p ${zipDir}/${tmpDir}/${coreModule}
mkdir -p ${zipDir}/${tmpDir}/${stanfordModule}
mkdir -p ${zipDir}/${tmpDir}/${exampleLightswitch}
mkdir -p ${zipDir}/${tmpDir}/${exampleLightswitchRu}
mkdir -p ${zipDir}/${tmpDir}/${exampleLightswitchFr}
mkdir -p ${zipDir}/${tmpDir}/${exampleTime}
mkdir -p ${zipDir}/${tmpDir}/${exampleCalculator}
mkdir -p ${zipDir}/${tmpDir}/${examplePizzeria}

mkdir ${zipDir}/${tmpDir}/build

#=====================#
# Prepare BINARY ZIP. #
#=====================#

function cpSrc() {
  rsync -avzq "$1"/src ${zipDir}/${tmpDir}/"$1" --exclude '**/.DS_Store' --exclude '**/*.iml'
}

function cpCore {
  rsync -avzq "${coreModule}"/src ${zipDir}/${tmpDir}/"${coreModule}" --exclude '**/.DS_Store' --exclude '**/*.iml' --exclude "main/python/ctxword/data/" --exclude "**/__pycache__/"
}

cpCore
cpSrc ${stanfordModule}
cpSrc ${exampleLightswitch}
cpSrc ${exampleLightswitchRu}
cpSrc ${exampleLightswitchFr}
cpSrc ${exampleTime}
cpSrc ${exampleCalculator}
cpSrc ${examplePizzeria}

cp bindist/LICENSE ${zipDir}/${tmpDir}
cp bindist/NOTICE ${zipDir}/${tmpDir}
cp DISCLAIMER-WIP ${zipDir}/${tmpDir}

cp ${coreModule}/src/main/resources/log4j2.xml ${zipDir}/${tmpDir}/build

rsync -avzq ${coreModule}/target/*all-deps.jar ${zipDir}/${tmpDir}/build
rsync -avzq ${coreModule}/target/scala-${scalaVer}/api/** ${zipDir}/${tmpDir}/scaladoc --exclude '**/.DS_Store'

function cpExample() {
  mkdir -p ${zipDir}/${tmpDir}/build/"$1"
  rsync -avzq "$1"/target/*.jar ${zipDir}/${tmpDir}/build/"$1" --exclude '*-sources.jar'
}

rsync -avzq "${stanfordModule}"/target/*.jar ${zipDir}/${tmpDir}/build --exclude '*-sources.jar'

cpExample ${exampleLightswitch}
cpExample ${exampleLightswitchRu}
cpExample ${exampleLightswitchFr}
cpExample ${exampleTime}
cpExample ${exampleCalculator}
cpExample ${examplePizzeria}


# Prepares bin zip.
cd ${zipDir} || exit
zip -rq "${zipFileBin}" ${tmpDir} 2> /dev/null
echo "Binary zip created: " "${zipFileBin}"

# Deletes some data for src zip
rm -R ${tmpDir}/build 2> /dev/null
rm -R ${tmpDir}/scaladoc 2> /dev/null

#=====================#
# Prepare SOURCE ZIP. #
#=====================#

# Adds some data for src zip.
cd ../

function cpPom() {
  cp "$1"/pom.xml ${zipDir}/${tmpDir}/"$1"
}

cpPom ${coreModule}
cpPom ${stanfordModule}
cpPom ${exampleLightswitch}
cpPom ${exampleLightswitchRu}
cpPom ${exampleLightswitchFr}
cpPom ${exampleTime}
cpPom ${exampleCalculator}
cpPom ${examplePizzeria}

cp pom.xml ${zipDir}/${tmpDir}
cp LICENSE ${zipDir}/${tmpDir}
cp NOTICE ${zipDir}/${tmpDir}
cp assembly.xml ${zipDir}/${tmpDir}
cp README.md ${zipDir}/${tmpDir}

# Prepares src zip.
cd ${zipDir} || exit
zip -rq "${zipFileSrc}" ${tmpDir} 2> /dev/null
echo "Source zip created: " "${zipFileSrc}"

rm -R ${tmpDir} 2> /dev/null

function sign() {
  shasum -a 256 "$1" > "$1".sha256
  gpg --local-user ${localUser} --sign --armor --output "$1".asc --detach-sign "$1"
}

sign "${zipFileBin}"
sign "${zipFileSrc}"

cd "${curDir}" || exit

echo
echo "****************************"
echo "ZIPs are prepared in folder: '${zipDir}'"
echo "****************************"