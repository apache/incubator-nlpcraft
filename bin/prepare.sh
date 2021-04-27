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

# Make sure that maven and gradle installed on your PC.

if [[ $1 = "" ]] ; then
    echo "Version must be set as input parameter."
    exit 1
fi

#
# Change this for your local GPG fingerprint:
# ===========================================
localUser=79BB50129889A04C68BD7A4ABCD48C7B6C94ED02

zipDir=zips
tmpDir=apache-nlpcraft
zipFileBin=apache-nlpcraft-incubating-bin-$1.zip # NOT an official ASF release.
zipFileSrc=apache-nlpcraft-incubating-$1.zip # An OFFICIAL ASF release.
coreModule=nlpcraft
stanfordModule=nlpcraft-stanford
examples=nlpcraft-examples
exampleAlarm=${examples}/alarm
exampleEcho=${examples}/echo
exampleHelloworld=${examples}/helloworld
exampleLightswitch=${examples}/lightswitch
exampleMinecraft=${examples}/minecraft
exampleMinecraftMod=${examples}/minecraft-mod
examplePhone=${examples}/phone
exampleSql=${examples}/sql
exampleTime=${examples}/time
exampleWeather=${examples}/weather

curDir=$(pwd)

cd ../

mvn clean package -P stanford-corenlp,release,examples

cd nlpcraft-examples/minecraft-mod || exit
./gradlew clean build

cd ../../

rm -R ${zipDir} 2> /dev/null

mkdir -p ${zipDir}/${tmpDir}/${coreModule}
mkdir -p ${zipDir}/${tmpDir}/${stanfordModule}
mkdir -p ${zipDir}/${tmpDir}/${exampleAlarm}
mkdir -p ${zipDir}/${tmpDir}/${exampleEcho}
mkdir -p ${zipDir}/${tmpDir}/${exampleHelloworld}
mkdir -p ${zipDir}/${tmpDir}/${exampleLightswitch}
mkdir -p ${zipDir}/${tmpDir}/${exampleMinecraft}
mkdir -p ${zipDir}/${tmpDir}/${exampleMinecraftMod}
mkdir -p ${zipDir}/${tmpDir}/${examplePhone}
mkdir -p ${zipDir}/${tmpDir}/${exampleSql}
mkdir -p ${zipDir}/${tmpDir}/${exampleTime}
mkdir -p ${zipDir}/${tmpDir}/${exampleWeather}

mkdir ${zipDir}/${tmpDir}/build
mkdir ${zipDir}/${tmpDir}/build/examples

#=====================#
# Prepare BINARY ZIP. #
#=====================#

rsync -avzq bin ${zipDir}/${tmpDir} --exclude '**/.DS_Store' --exclude bin/prepare.sh --exclude bin/MAVEN-RELEASE.md
rsync -avzq openapi ${zipDir}/${tmpDir} --exclude '**/.DS_Store'

function cpSrc() {
  rsync -avzq "$1"/src ${zipDir}/${tmpDir}/"$1" --exclude '**/.DS_Store' --exclude '**/*.iml'
}

cpSrc ${coreModule}
cpSrc ${stanfordModule}
cpSrc ${exampleAlarm}
cpSrc ${exampleEcho}
cpSrc ${exampleHelloworld}
cpSrc ${exampleLightswitch}
cpSrc ${exampleMinecraft}
cpSrc ${exampleMinecraftMod}
cpSrc ${examplePhone}
cpSrc ${exampleSql}
cpSrc ${exampleTime}
cpSrc ${exampleWeather}
rsync -avzq sql ${zipDir}/${tmpDir} --exclude '**/.DS_Store'

cp bindist/LICENSE ${zipDir}/${tmpDir}
cp bindist/NOTICE ${zipDir}/${tmpDir}
cp DISCLAIMER ${zipDir}/${tmpDir}

cp ${coreModule}/src/main/resources/nlpcraft.conf ${zipDir}/${tmpDir}/build

function cpConf() {
  cp "$1"/src/main/resources/nlpcraft.conf ${zipDir}/${tmpDir}/build/examples/"$(echo "$1" | tr '/' '-')".conf
}

cpConf ${exampleAlarm}
cpConf ${exampleEcho}
cpConf ${exampleHelloworld}
cpConf ${exampleLightswitch}
cpConf ${exampleMinecraft}
cpConf ${examplePhone}
cpConf ${exampleSql}
cpConf ${exampleTime}
cpConf ${exampleWeather}

cp ${coreModule}/src/main/resources/ignite.xml ${zipDir}/${tmpDir}/build
cp ${coreModule}/src/main/resources/log4j2.xml ${zipDir}/${tmpDir}/build

rsync -avzq ${coreModule}/target/*all-deps.jar ${zipDir}/${tmpDir}/build
rsync -avzq ${coreModule}/target/apidocs/** ${zipDir}/${tmpDir}/javadoc --exclude '**/.DS_Store'

function cpJar() {
  rsync -avzq "$1"/target/*.jar ${zipDir}/${tmpDir}/build --exclude '*-sources.jar'
}

function cpJarExamples() {
  rsync -avzq "$1"/target/*.jar ${zipDir}/${tmpDir}/build/examples --exclude '*-sources.jar'
}

cpJar ${stanfordModule}
cpJarExamples ${exampleAlarm}
cpJarExamples ${exampleEcho}
cpJarExamples ${exampleHelloworld}
cpJarExamples ${exampleLightswitch}
cpJarExamples ${exampleMinecraft}
cpJarExamples ${examplePhone}
cpJarExamples ${exampleSql}
cpJarExamples ${exampleTime}
cpJarExamples ${exampleWeather}
rsync -avzq ${exampleMinecraftMod}/build/libs/*.jar ${zipDir}/${tmpDir}/build/examples --exclude '*-sources.jar'

# Prepares bin zip.
cd ${zipDir} || exit
zip -rq "${zipFileBin}" ${tmpDir} 2> /dev/null
echo "Binary zip created: " "${zipFileBin}"

# Deletes some data for src zip
rm -R ${tmpDir}/build 2> /dev/null
rm -R ${tmpDir}/javadoc 2> /dev/null

#=====================#
# Prepare SOURCE ZIP. #
#=====================#

# Adds some data for src zip.
cd ../
mkdir ${zipDir}/${tmpDir}/javadoc

function cpPom() {
  cp "$1"/pom.xml ${zipDir}/${tmpDir}/"$1"
}

cpPom ${coreModule}
cpPom ${stanfordModule}
cpPom ${exampleAlarm}
cpPom ${exampleEcho}
cpPom ${exampleHelloworld}
cpPom ${exampleLightswitch}
cpPom ${exampleMinecraft}
cpPom ${examplePhone}
cpPom ${exampleSql}
cpPom ${exampleTime}
cpPom ${exampleWeather}

cp ${exampleMinecraftMod}/build.gradle ${zipDir}/${tmpDir}/${exampleMinecraftMod}
cp ${exampleMinecraftMod}/gradlew ${zipDir}/${tmpDir}/${exampleMinecraftMod}
cp ${exampleMinecraftMod}/gradlew.bat ${zipDir}/${tmpDir}/${exampleMinecraftMod}

cp pom.xml ${zipDir}/${tmpDir}
cp LICENSE ${zipDir}/${tmpDir}
cp NOTICE ${zipDir}/${tmpDir}
cp assembly.xml ${zipDir}/${tmpDir}
cp README.md ${zipDir}/${tmpDir}
cp javadoc/stylesheet.css ${zipDir}/${tmpDir}/javadoc

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