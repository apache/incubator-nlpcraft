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

zipDir=zips
tmpDir=apache-nlpcraft
zipFileBin=apache-nlpcraft-bin-$1.zip
zipFileSrc=apache-nlpcraft-src-$1.zip

curDir=$(pwd)

cd ../

mvn clean package -P release

rm -R ${zipDir} 2> /dev/null

mkdir ${zipDir}
mkdir ${zipDir}/${tmpDir}
mkdir ${zipDir}/${tmpDir}/build

rsync -avzq bin ${zipDir}/${tmpDir} --exclude '**/.DS_Store' --exclude bin/prepare.sh
rsync -avzq openapi ${zipDir}/${tmpDir} --exclude '**/.DS_Store'
rsync -avzq src ${zipDir}/${tmpDir} --exclude '**/.DS_Store' --exclude '**/*.iml'
rsync -avzq sql ${zipDir}/${tmpDir} --exclude '**/.DS_Store'

cp LICENSE ${zipDir}/${tmpDir}
cp NOTICE ${zipDir}/${tmpDir}
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

# Adds some data for src zip.
cd ../
cp pom.xml ${zipDir}/${tmpDir}
cp assembly.xml ${zipDir}/${tmpDir}
cp README.md ${zipDir}/${tmpDir}

# Prepares src zip.
cd ${zipDir}
zip -rq ${zipFileSrc} ${tmpDir} 2> /dev/null

rm -R ${tmpDir} 2> /dev/null

function sign() {
  shasum -a 1 $1 > $1.sha1
  shasum -a 256 $1 > $1.sha256
  md5 $1 > $1.md5
  gpg --detach-sign $1
}

sign "${zipFileBin}"
sign "${zipFileSrc}"

cd ${curDir}

echo
echo "Files prepared in folder: ${zipDir}"