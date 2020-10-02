::
:: Licensed to the Apache Software Foundation (ASF) under one or more
:: contributor license agreements.  See the NOTICE file distributed with
:: this work for additional information regarding copyright ownership.
:: The ASF licenses this file to You under the Apache License, Version 2.0
:: (the "License"); you may not use this file except in compliance with
:: the License.  You may obtain a copy of the License at
::
::      http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.
::

@echo off

::
:: Following several functions are copied from Apache Ignite.
:: See https://github.com/apache/ignite for more details.
::

Setlocal EnableDelayedExpansion

if "%OS%" == "Windows_NT"  setlocal

:: Check JAVA_HOME.
if defined JAVA_HOME goto checkJdk
    echo ERROR:
    echo ------
    echo JAVA_HOME environment variable is not found.
    echo Please point JAVA_HOME variable to location of JDK 11 or later.
    echo You can also download latest JDK at http://java.com/download.
goto :eof

:checkJdk
:: Check that JDK is where it should be.
if exist "%JAVA_HOME%\bin\java.exe" goto checkJdkVersion
    echo ERROR:
    echo ------
    echo JAVA is not found in JAVA_HOME=%JAVA_HOME%.
    echo Please point JAVA_HOME variable to installation of JDK 11 or later.
    echo You can also download latest JDK at http://java.com/download.
goto :eof

:checkJdkVersion
set cmd="%JAVA_HOME%\bin\java.exe"
for /f "tokens=* USEBACKQ" %%f in (`%cmd% -version 2^>^&1`) do (
    set var=%%f
    goto :escape
)
:escape

for /f "tokens=1-3  delims= " %%a in ("%var%") do set JAVA_VER_STR=%%c
set JAVA_VER_STR=%JAVA_VER_STR:"=%

for /f "tokens=1,2 delims=." %%a in ("%JAVA_VER_STR%.x") do set MAJOR_JAVA_VER=%%a& set MINOR_JAVA_VER=%%b
if %MAJOR_JAVA_VER% == 1 set MAJOR_JAVA_VER=%MINOR_JAVA_VER%

if %MAJOR_JAVA_VER% LSS 11 (
    echo ERROR:
    echo ------
    echo The version of JAVA installed in %JAVA_HOME% is incorrect.
    echo Please point JAVA_HOME variable to installation of JDK 11 or later.
    echo You can also download latest JDK at http://java.com/download.
	goto :eof
)

:: Absolute directory path of this script.
pushd "%~dp0"
set SCRIPT_HOME=%CD%
popd

:: Filename of the script.
set SCRIPT_NAME=%~nx0

:: NLPCraft installation home.
pushd "%SCRIPT_HOME%"\..
set INSTALL_HOME=%CD%
popd

:: Directory containing JARs.
set BUILD_HOME=%INSTALL_HOME%\build

if not exist "%BUILD_HOME%" (
    echo ERROR:
    echo ------
    echo Folder '%INSTALL_HOME%\build' does not exist.
    echo This folder should contain NLPCraft JARs and is required to run this script.
	goto :eof
)

:: Build classpath.
for %%f in ("%BUILD_HOME%"\*.jar) do ( set CP=%%f;!CP! )

set MAIN_CLASS=org.apache.nlpcraft.model.tools.cmdline.NCCli
set JVM_OPTS= ^
    -ea ^
    -Xms1g ^
    -Xmx1g ^
    -server ^
    -XX:+UseG1GC ^
    -XX:MaxMetaspaceSize=256m ^
    -DNLPCRAFT_CLI= ^
    -DNLPCRAFT_CLI_CP="%CP%" ^
    -DNLPCRAFT_CLI_JAVA="%JAVA_HOME%\bin\java.exe" ^
    -DNLPCRAFT_CLI_SCRIPT="%SCRIPT_NAME%" ^
    -DNLPCRAFT_CLI_INSTALL_HOME="%INSTALL_HOME%"

"%JAVA_HOME%\bin\java.exe" %JVM_OPTS% -cp "%CP%" %MAIN_CLASS% %*
