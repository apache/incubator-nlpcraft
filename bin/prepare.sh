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

curDir=$(pwd)

cd ../

mvn clean package -Prelease

rm -R ${zipDir} 2> /dev/null

mkdir ${zipDir}
mkdir ${zipDir}/${tmpDir}
mkdir ${zipDir}/${tmpDir}/build

rsync -avzq bin ${zipDir}/${tmpDir} --exclude '**/.DS_Store' --exclude bin/prepare.sh
rsync -avzq openapi ${zipDir}/${tmpDir} --exclude '**/.DS_Store'
rsync -avzq src ${zipDir}/${tmpDir} --exclude '**/.DS_Store' --exclude '**/*.iml'
rsync -avzq sql ${zipDir}/${tmpDir} --exclude '**/.DS_Store'

cp bindist/LICENSE ${zipDir}/${tmpDir}
cp NOTICE ${zipDir}/${tmpDir}
cp DISCLAIMER ${zipDir}/${tmpDir}
cp src/main/resources/nlpcraft.conf ${zipDir}/${tmpDir}/build
cp src/main/resources/ignite.xml ${zipDir}/${tmpDir}/build
cp src/main/resources/log4j2.xml ${zipDir}/${tmpDir}/build

cp target/*all-deps.jar ${zipDir}/${tmpDir}/build
rsync -avzq target/apidocs/** ${zipDir}/${tmpDir}/javadoc --exclude '**/.DS_Store'

# Prepares bin zip.
cd ${zipDir}
zip -rq ${zipFileBin} ${tmpDir} 2> /dev/null

# Deletes some data for src zip
rm -R ${tmpDir}/build 2> /dev/null
rm -R ${tmpDir}/javadoc 2> /dev/null

# Adds some data for src zip.
cd ../
cp LICENSE ${zipDir}/${tmpDir}
cp pom.xml ${zipDir}/${tmpDir}
cp assembly.xml ${zipDir}/${tmpDir}
cp README.md ${zipDir}/${tmpDir}

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
echo "Files prepared in folder: ${zipDir}"