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
#!/bin/bash

if [[ $1 = "" ]] ; then
    echo "Version must be set as input parameter."
    exit -1
fi

zipDir=zips
tmpDir=nlpcraft
zipFile=nlpcraft-$1.zip

curDir=$(pwd)

cd ../

mvn clean package -P release

rm -R ${zipDir} 2> /dev/null

mkdir ${zipDir}
mkdir ${zipDir}/${tmpDir}
mkdir ${zipDir}/${tmpDir}/build

rsync -avzq bin ${zipDir}/${tmpDir} --exclude '**/.DS_Store' --exclude bin/prepare.sh
rsync -avzq openapi ${zipDir}/${tmpDir} --exclude '**/.DS_Store'
rsync -avzq src ${zipDir}/${tmpDir} --exclude '**/.DS_Store'
rsync -avzq sql ${zipDir}/${tmpDir} --exclude '**/.DS_Store'

cp LICENSE ${zipDir}/${tmpDir}
cp src/main/resources/nlpcraft.conf ${zipDir}/${tmpDir}/build
cp src/main/resources/ignite.xml ${zipDir}/${tmpDir}/build
cp src/main/resources/log4j2.xml ${zipDir}/${tmpDir}/build

cp target/*all-deps.jar ${zipDir}/${tmpDir}/build
rsync -avzq target/apidocs/** ${zipDir}/${tmpDir}/javadoc --exclude '**/.DS_Store'

cd ${zipDir}
zip -rq ${zipFile} ${tmpDir} 2> /dev/null

rm -R ${tmpDir} 2> /dev/null

shasum -a 1 ${zipFile} > ${zipFile}.sha1
shasum -a 256 ${zipFile} > ${zipFile}.sha256
md5 ${zipFile} > ${zipFile}.md5
gpg --detach-sign ${zipFile}

cd ${curDir}

echo
echo "Files prepared in folder: ${zipDir}"
