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
val scalaMinVer = "2.2"
val log4jVer = "2.21.0"
val scalaLoggingVer = "3.9.5"
val orgAntlr4Ver = "4.13.0"
val jlineVer = "3.25.1"
val commonsIoVer = "2.14.0"
val commonsLang3Ver = "3.13.0"
val commonsCodecVer = "1.16.0"
val commonsCollectionsVer = "4.4"
val gsonVer = "2.10.1"
val jacksonVer = "2.15.3"
val apacheOpennlpVer = "2.3.2"

// Stanford project libraries.
val stanfordCoreNLPVer  = "4.5.6"

// Examples libraries.
val languagetoolVer = "6.0"
val luceneAnalyzersCommonVer = "8.11.2"

ThisBuild / scalaVersion := s"$scalaMajVer.$scalaMinVer"
ThisBuild / version := nlpcraftVer
ThisBuild / organization := "org.apache"
ThisBuild / organizationName := "nlpcraft"
ThisBuild / description := "An open source API to convert natural language into actions."
ThisBuild / licenses := List("Apache-2.0" -> url("https://github.com/sbt/sbt/blob/develop/LICENSE"))
ThisBuild / homepage := Some(url("https://nlpcraft.apache.org/"))
ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/apache/incubator-nlpcraft"), "scm:git@github.com/apache/incubator-nlpcraft.git"))
ThisBuild / autoAPIMappings := true
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
    "org.scalatest" %% "scalatest" % "3.2.19" % "test"
)

val commonScalaDoc = Seq(
    "-skip-by-regex:org.apache.nlpcraft.internal",
    "-skip-by-regex:org.apache.nlpcraft.nlp.enrichers.tools",
    "-project-footer", "Apache NLPCraft",
    "-project-version", nlpcraftVer,
    "-siteroot", ".",
    "-doc-root-content", "scaladoc/docroot.md",
    "-source-links:github://apache/incubator-nlpcraft/master",
    "-social-links:github::https://github.com/apache/incubator-nlpcraft"
)

lazy val nlpcraft = (project in file("nlpcraft"))
    .settings(
        name := "NLPCraft",
        version := nlpcraftVer,

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        // Dependencies.
        libraryDependencies ++= libs,
        libraryDependencies += "org.apache.commons" % "commons-lang3" % commonsLang3Ver
    )

lazy val nlpcraftStanford = (project in file("nlpcraft-stanford"))
    .dependsOn(nlpcraft)
    .settings(
        name := "NLPCraft Stanford",
        version := nlpcraftVer,

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        // Dependencies.
        libraryDependencies ++= libs,
        libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % stanfordCoreNLPVer,
        libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % stanfordCoreNLPVer classifier "models"
    )

lazy val timeExample = (project in file("nlpcraft-examples/time"))
    .dependsOn(nlpcraft)
    .settings(
        name := "NLPCraft Time Example",
        version := nlpcraftVer,

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        // Dependencies.
        libraryDependencies ++= libs
    )

lazy val lightSwitchExample = (project in file("nlpcraft-examples/lightswitch"))
    .dependsOn(nlpcraft)
    .settings(
        name := "NLPCraft LightSwitch Example",
        version := nlpcraftVer,

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        // Dependencies.
        libraryDependencies ++= libs
    )

lazy val lightSwitchRuExample = (project in file("nlpcraft-examples/lightswitch-ru"))
    .dependsOn(nlpcraft)
    .settings(
        name := "NLPCraft LightSwitch RU Example",
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
        name := "NLPCraft LightSwitch FR Example",
        version := nlpcraftVer,

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        // Dependencies.
        libraryDependencies ++= libs,
        libraryDependencies += "org.apache.lucene" % "lucene-analyzers-common" % luceneAnalyzersCommonVer,
        libraryDependencies += "org.languagetool" % "languagetool-core" % languagetoolVer,
        libraryDependencies += "org.languagetool" % "language-fr" % languagetoolVer
    )

lazy val pizzeriaExample = (project in file("nlpcraft-examples/pizzeria"))
    .dependsOn(nlpcraft, nlpcraftStanford)
    .settings(
        name := "NLPCraft Pizzeria Example",
        version := nlpcraftVer,

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        // Dependencies.
        libraryDependencies ++= libs
    )

lazy val calculatorExample = (project in file("nlpcraft-examples/calculator"))
    .dependsOn(nlpcraft, nlpcraftStanford)
    .settings(
        name := "NLPCraft Calculator Example",
        version := nlpcraftVer,

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        // Dependencies.
        libraryDependencies ++= libs
    )

lazy val nlpcraftWithStanford = (project in file("."))
    .enablePlugins(ScalaUnidocPlugin)
    .aggregate(nlpcraft, nlpcraftStanford)
    .settings(
        name := "NLPCraft",
        version := nlpcraftVer,
        ScalaUnidoc / unidoc / unidocProjectFilter := inAnyProject -- inProjects(timeExample, lightSwitchExample, lightSwitchRuExample, lightSwitchFrExample, pizzeriaExample, calculatorExample),

        // Scaladoc config.
        Compile / doc / scalacOptions ++= commonScalaDoc,

        libraryDependencies ++= libs,
        libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % stanfordCoreNLPVer,
        libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % stanfordCoreNLPVer classifier "models"
    )