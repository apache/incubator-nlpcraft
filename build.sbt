/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

val nlpcraftVer = "1.0.0"

// Common libraries.
val scalaMajVer = "3"
val scalaMinVer = "1.3"
val log4jVer = "2.17.2"
val scalaLoggingVer = "3.9.4"
val scalatestVer = "3.2.11"
val scalaParColections = "1.0.4"
val orgAntlr4Ver = "4.10.1"
val jlineVer = "3.21.0"
val commonsIoVer = "2.11.0"
val commonsLang3Ver = "3.12.0"
val commonsCodecVer = "1.15"
val commonsCollectionsVer = "4.4"
val scala3RefVer = "1.1.4"
val gsonVer = "2.9.0"
val jacksonVer = "2.13.2"
val jacksonYamlVer = "2.13.2"
val apacheOpennlpVer = "1.9.4"

// Test libraries.
val junitVer = "5.8.2"

val jmhVer = "1.35"

// Stanford project libraries.
val stanfordCorenlpVer  = "4.4.0"

// Examples libraries.
val languagetoolVer = "5.8"
val luceneAnalyzersCommonVer = "8.11.1"

ThisBuild / scalaVersion := s"$scalaMajVer.$scalaMinVer"
ThisBuild / version := nlpcraftVer
ThisBuild / organization := "org.apache"
ThisBuild / organizationName := "NlpCraft"
ThisBuild / description := "An open source API to convert natural language into actions."
ThisBuild / licenses := List("Apache-2.0" -> url("https://github.com/sbt/sbt/blob/develop/LICENSE"))
ThisBuild / homepage := Some(url("https://nlpcraft.apache.org/"))
ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/apache/incubator-nlpcraft"), "scm:git@github.com/apache/incubator-nlpcraft.git"))

ThisBuild / developers ++= List(
    "aradzinski" -> "Aaron Radzinski ",
    "skhdl" -> "Sergey Kamov"
).map {
    case (username, fullName) => Developer(username, fullName, s"@$username", url(s"https://github.com/$username"))
}

lazy val libs = Seq(
    "com.typesafe.scala-logging" % s"scala-logging_$scalaMajVer" % scalaLoggingVer,
    "com.google.code.gson" % "gson" % gsonVer,
    "commons-io" % "commons-io" % commonsIoVer,
    "commons-codec" % "commons-codec" % commonsCodecVer,
    "org.apache.commons" % "commons-collections4" % commonsCollectionsVer,
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % jacksonVer,
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVer,
    "com.fasterxml.jackson.module" % "jackson-module-scala_3" % jacksonVer,
    "org.antlr" % "antlr4-runtime" % orgAntlr4Ver,
    "org.apache.opennlp" % "opennlp-tools" % apacheOpennlpVer,
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVer,
    "org.apache.logging.log4j" % "log4j-api" % log4jVer,
    "org.jline" % "jline-terminal" % jlineVer,
    "org.junit.jupiter" % "junit-jupiter-engine" % junitVer % Test
)

val commonScalaDoc = Seq(
    "-project-footer", "Apache, Nlpcraft",
    "-project-version", nlpcraftVer,
    "-siteroot", ".",
    "-doc-root-content", "scaladoc/docroot.md",
    "-source-links:github://apache/incubator-nlpcraft/master",
    "-social-links:github::https://github.com/apache/incubator-nlpcraft"
)

lazy val nlpcraft = (project in file("nlpcraft"))
    .settings(
        name := "NlpCraft",
        version := nlpcraftVer,

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        // Dependencies.
        libraryDependencies ++= libs,
        libraryDependencies += "org.apache.commons" % "commons-lang3" % commonsLang3Ver,
        libraryDependencies += "org.openjdk.jmh" % "jmh-core" % jmhVer % Test
    )

lazy val nlpcraftStanford = (project in file("nlpcraft-stanford"))
    .dependsOn(nlpcraft)
    .settings(
        name := "NlpCraft Stanford",
        version := nlpcraftVer,

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        // Dependencies.
        libraryDependencies ++= libs,
        libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % stanfordCorenlpVer,
        libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % stanfordCorenlpVer classifier "models"
    )

lazy val timeExample = (project in file("nlpcraft-examples/time"))
    .dependsOn(nlpcraft)
    .settings(
        name := "NlpCraft Time Example",
        version := nlpcraftVer,

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        // Dependencies.
        libraryDependencies ++= libs
    )

lazy val lightSwitchExample = (project in file("nlpcraft-examples/lightswitch"))
    .dependsOn(nlpcraft)
    .settings(
        name := "NlpCraft LightSwitch Example",
        version := nlpcraftVer,

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        // Dependencies.
        libraryDependencies ++= libs
    )

lazy val lightSwitchRuExample = (project in file("nlpcraft-examples/lightswitch-ru"))
    .dependsOn(nlpcraft)
    .settings(
        name := "NlpCraft LightSwitch RU Example",
        version := nlpcraftVer,

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        // Dependencies.
        libraryDependencies ++= libs,
        libraryDependencies += "org.apache.lucene" % "lucene-analyzers-common" % luceneAnalyzersCommonVer,
        libraryDependencies += "org.languagetool" % "languagetool-core" % languagetoolVer,
        libraryDependencies += "org.languagetool" % "language-ru" % languagetoolVer
    )

lazy val lightSwitchFrExample = (project in file("nlpcraft-examples/lightswitch-fr"))
    .dependsOn(nlpcraft)
    .settings(
        name := "NlpCraft LightSwitch FR Example",
        version := nlpcraftVer,

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        // Dependencies.
        libraryDependencies ++= libs,
        libraryDependencies += "org.apache.lucene" % "lucene-analyzers-common" % luceneAnalyzersCommonVer,
        libraryDependencies += "org.languagetool" % "languagetool-core" % languagetoolVer,
        libraryDependencies += "org.languagetool" % "language-fr" % languagetoolVer
    )