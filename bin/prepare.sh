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
    echo "Version must be set as input parameter."
    exit -1
fi

#
# Change this for your local GPG fingerprint:
# ===========================================
localUser=223A2AADD175994F4450467491D161EDD8405C82

zipDir=zips
tmpDir=apache-nlpcraft
zipFileBin=apache-nlpcraft-incubating-bin-$1.zip # NOT an official ASF release.
zipFileSrc=apache-nlpcraft-incubating-$1.zip # An OFFICIAL ASF release.
coreModule=nlpcraft
stanfordModule=nlpcraft-stanford

curDir=$(pwd)

cd ../

mvn clean package -P stanford-corenlp,release

rm -R ${zipDir} 2> /dev/null

mkdir ${zipDir}
mkdir ${zipDir}/${tmpDir}
mkdir ${zipDir}/${tmpDir}/${coreModule}
mkdir ${zipDir}/${tmpDir}/build

#=====================#
# Prepare BINARY ZIP. #
#=====================#

rsync -avzq bin ${zipDir}/${tmpDir} --exclude '**/.DS_Store' --exclude bin/prepare.sh --exclude bin/MAVEN-RELEASE.md
rsync -avzq openapi ${zipDir}/${tmpDir} --exclude '**/.DS_Store'
rsync -avzq ${coreModule}/src ${zipDir}/${tmpDir}/${coreModule} --exclude '**/.DS_Store' --exclude '**/*.iml' --exclude '**/python/ctxword/data' --exclude '**/server/geo/tools/**/*.txt'
rsync -avzq ${stanfordModule}/src ${zipDir}/${tmpDir}/${stanfordModule} --exclude '**/.DS_Store' --exclude '**/*.iml'
rsync -avzq sql ${zipDir}/${tmpDir} --exclude '**/.DS_Store'

cp bindist/LICENSE ${zipDir}/${tmpDir}
cp bindist/NOTICE ${zipDir}/${tmpDir}
cp DISCLAIMER ${zipDir}/${tmpDir}
cp ${coreModule}/src/main/resources/nlpcraft.conf ${zipDir}/${tmpDir}/build
cp ${coreModule}/src/main/resources/ignite.xml ${zipDir}/${tmpDir}/build
cp ${coreModule}/src/main/resources/log4j2.xml ${zipDir}/${tmpDir}/build

rsync -avzq ${coreModule}/target/*all-deps.jar ${zipDir}/${tmpDir}/build
rsync -avzq ${coreModule}/target/apidocs/** ${zipDir}/${tmpDir}/javadoc --exclude '**/.DS_Store'
rsync -avzq ${stanfordModule}/target/*.jar ${zipDir}/${tmpDir}/build --exclude '*-sources.jar'

# Prepares bin zip.
cd ${zipDir}
zip -rq ${zipFileBin} ${tmpDir} 2> /dev/null

# Deletes some data for src zip
rm -R ${tmpDir}/build 2> /dev/null
rm -R ${tmpDir}/javadoc 2> /dev/null

#=====================#
# Prepare SOURCE ZIP. #
#=====================#

# Adds some data for src zip.
cd ../
mkdir ${zipDir}/${tmpDir}/javadoc

cp ${coreModule}/pom.xml ${zipDir}/${tmpDir}/${coreModule}
cp ${stanfordModule}/pom.xml ${zipDir}/${tmpDir}/${stanfordModule}
cp pom.xml ${zipDir}/${tmpDir}
cp LICENSE ${zipDir}/${tmpDir}
cp NOTICE ${zipDir}/${tmpDir}
cp assembly.xml ${zipDir}/${tmpDir}
cp README.md ${zipDir}/${tmpDir}
cp javadoc/stylesheet.css ${zipDir}/${tmpDir}/javadoc

# Prepares src zip.
cd ${zipDir}
zip -rq ${zipFileSrc} ${tmpDir} 2> /dev/null

rm -R ${tmpDir} 2> /dev/null

function sign() {
  shasum -a 256 $1 > $1.sha256
  gpg --local-user ${localUser} --sign --armor --output $1.asc --detach-sign $1
}

sign "${zipFileBin}"
sign "${zipFileSrc}"

cd ${curDir}

echo
echo "****************************"
echo "ZIPs are prepared in folder: '${zipDir}'"
echo "****************************"
