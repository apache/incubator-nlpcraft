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

#
# NOTE: this script uses ANSI color sequences by default.
#

#
# POSIX-complaint implementation for GNU "readlink -f" functionality.
# Works on Linux/MacOS.
#
readlinkf_posix() {
  [ "${1:-}" ] || return 1
  max_symlinks=40
  CDPATH=''

  target=$1
  [ -e "${target%/}" ] || target=${1%"${1##*[!/]}"}
  [ -d "${target:-/}" ] && target="$target/"

  cd -P . 2>/dev/null || return 1
  while [ "$max_symlinks" -ge 0 ] && max_symlinks=$((max_symlinks - 1)); do
    if [ ! "$target" = "${target%/*}" ]; then
      case $target in
        /*) cd -P "${target%/*}/" 2>/dev/null || break ;;
        *) cd -P "./${target%/*}" 2>/dev/null || break ;;
      esac
      target=${target##*/}
    fi

    if [ ! -L "$target" ]; then
      target="${PWD%/}${target:+/}${target}"
      printf '%s\n' "${target:-/}"
      return 0
    fi

    link=$(ls -dl -- "$target" 2>/dev/null) || break
    target=${link#*" $target -> "}
  done
  return 1
}

# Absolute directory path of this script.
SCRIPT_HOME="$(dirname $(readlinkf_posix "$0"))"

# Filename of the script.
SCRIPT_NAME="$(basename "$0")"

# NLPCraft installation home.
INSTALL_HOME="$(dirname "$SCRIPT_HOME")"

# Directories containing JARs:
#   'build' - JARs from binary distribution.
#   'dev' - JARs from built sources by 'mvn clean package'.
BUILD_JARS="$INSTALL_HOME/build"
DEV_JARS="$INSTALL_HOME/nlpcraft/target"

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
            echo -e "\e[31mERR:\e[0m JAVA_HOME environment variable is not found."
            echo -e "\e[31mERR:\e[0m Please point JAVA_HOME variable to location of JDK 11 or later."
            echo -e "\e[31mERR:\e[0m You can also download latest JDK at http://java.com/download"

            exit 1
        fi

        JAVA_HOME=
    else
        JAVA=${JAVA_HOME}/bin/java
    fi

    # Check JDK.
    javaMajorVersion "$JAVA"

    if [ "$version" -lt 11 ]; then
        echo -e "\e[31mERR:\e[0m The version \e[36m$version\e[0m of JAVA installed in \e[36m'$JAVA_HOME'\e[0m is incompatible."
        echo -e "\e[31mERR:\e[0m Please point JAVA_HOME variable to installation of JDK 11 or later."
        echo -e "\e[31mERR:\e[0m You can also download latest JDK at http://java.com/download"

        exit 1
    fi
}

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

if ! [ -d "$BUILD_JARS" ] && ! [ -d "$DEV_JARS" ]; then
    echo -e "\e[31mERR:\e[0m Cannot find JARs for NLPCraft in either of these folders:"
    echo -e "\e[31mERR:\e[0m   \e[33m+-\e[0m $BUILD_JARS"
    echo -e "\e[31mERR:\e[0m   \e[33m+-\e[0m $DEV_JARS"

    exit 1
fi

# Build classpath.
# NOTE: JARs from 'build' override JARs from 'dev'.
for file in "$DEV_JARS"/*-all-deps.jar
do
    CP=$CP$SEP$file
done
for file in "$BUILD_JARS"/*-all-deps.jar
do
    CP=$CP$SEP$file
done

# Check Java version.
checkJava

MAIN_CLASS=org.apache.nlpcraft.model.tools.cmdline.NCCli
JVM_OPTS="\
    -ea \
    -Xms1g \
    -Xmx1g \
    -server \
    -XX:+UseG1GC \
    -XX:MaxMetaspaceSize=256m \
    -DNLPCRAFT_CLI= \
    -DNLPCRAFT_CLI_CP=$CP \
    -DNLPCRAFT_CLI_JAVA=$JAVA \
    -DNLPCRAFT_CLI_SCRIPT=$SCRIPT_NAME \
    -DNLPCRAFT_CLI_INSTALL_HOME=$INSTALL_HOME"

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