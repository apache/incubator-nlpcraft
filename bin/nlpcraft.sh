#!/usr/bin/env bash
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

# Absolute directory path of this script.
SCRIPT_HOME="$(dirname "$(readlink -f "$0")")"

# Filename of the script.
SCRIPT_NAME="$(basename "$0")"

# NLPCraft installation home.
INSTALL_HOME="$(dirname "$SCRIPT_HOME")"

# Directory containing JARs.
BUILD_HOME="$INSTALL_HOME/build"

# Mac OS specific support to display correct name in the dock.
if [ "${DOCK_OPTS:-}" == "" ]; then
    DOCK_OPTS="-Xdock:name=NLPCraft"
fi

# Extract java version to `version` variable.
javaVersion() {
    version=$("$1" -version 2>&1 | awk -F[\"-] '/version/ {print $2}')
}

#
# Following several functions are copied from Apache Ignite.
# See https://github.com/apache/ignite for more details.
#

# Extract only major version of java to `version` variable.
javaMajorVersion() {
    javaVersion "$1"
    version="${version%%.*}"

    if [ "${version}" -eq 1 ]; then
        # Version seems to start from 1, we need second number.
        javaVersion "$1"
        version=$(awk -F[\"\.] '{print $2}' <<< "${version}")
    fi
}

# Discovers path to Java executable and checks it's version.
# The function exports JAVA variable with path to Java executable.
checkJava() {
    # Check JAVA_HOME.
    if [ "${JAVA_HOME:-}" = "" ]; then
        JAVA=$(type -p java)
        RETCODE=$?

        if [ $RETCODE -ne 0 ]; then
            echo "ERROR:"
            echo "------"
            echo "JAVA_HOME environment variable is not found."
            echo "Please point JAVA_HOME variable to location of JDK 11 or later."
            echo "You can also download latest JDK at http://java.com/download"

            exit 1
        fi

        JAVA_HOME=
    else
        JAVA=${JAVA_HOME}/bin/java
    fi

    # Check JDK.
    javaMajorVersion "$JAVA"

    if [ "$version" -lt 11 ]; then
        echo "ERROR:"
        echo "------"
        echo "The $version version of JAVA installed in JAVA_HOME=$JAVA_HOME is incompatible."
        echo "Please point JAVA_HOME variable to installation of JDK 11 or later."
        echo "You can also download latest JDK at http://java.com/download"

        exit 1
    fi
}

MAIN_CLASS=org.apache.nlpcraft.model.tools.cmdline.NCCommandLine
JVM_OPTS="\
    -ea \
    -Xms1g \
    -Xmx1g \
    -server \
    -XX:+UseG1GC \
    -XX:MaxMetaspaceSize=256m \
    -DNLPCRAFT_CLI_SCRIPT=$SCRIPT_NAME \
    -DNLPCRAFT_CLI_INSTALL_HOME=$INSTALL_HOME"

osname=$(uname)

# OS specific classpath separator.
SEP=":"

case "${osname}" in
    MINGW*)
        SEP=";"
        ;;
    CYGWIN*)
        SEP=";"
        ;;
esac

if ! [ -d "$BUILD_HOME" ]; then
    echo "ERROR:"
    echo "------"
    echo "Folder '${INSTALL_HOME}\build' does not exist."
    echo "This folder should contain NLPCraft JARs and is required to run this script."

    exit 1
fi

# Build classpath.
for file in "$BUILD_HOME"/*.jar
do
    CP=$CP$SEP$file
done

# Check Java version.
checkJava

case $osname in
    Darwin*)
        # shellcheck disable=SC2086
        "$JAVA" $JVM_OPTS "$DOCK_OPTS" -cp "${CP}" $MAIN_CLASS "$@"
        ;;
    *)
        # shellcheck disable=SC2086
        "$JAVA" $JVM_OPTS -cp "${CP}" $MAIN_CLASS "$@"
        ;;
esac