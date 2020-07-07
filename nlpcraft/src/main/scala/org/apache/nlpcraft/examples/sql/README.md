<!--
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

<img src="https://nlpcraft.apache.org/images/nlpcraft_logo_black.gif" height="80px">
<br>

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/apache/opennlp/master/LICENSE)
[![build](https://github.com/apache/incubator-nlpcraft/workflows/build/badge.svg)](https://github.com/apache/incubator-nlpcraft/actions)
[![Documentation Status](https://img.shields.io/:docs-latest-green.svg)](https://nlpcraft.apache.org/docs.html)
[![Gitter](https://badges.gitter.im/apache-nlpcraft/community.svg)](https://gitter.im/apache-nlpcraft/community)

### SQL Model Example
This example demonstrates the relatively complex case of taking an existing SQL schema and developing
a natural language interface to it. Although it does not support many standard SQL capabilities (to limit the size of the 
example itself) - it is designed in a such a way that it can be easily extended and developed further.  

### Running
This example can be run the same way as other examples with one exception - it needs to run H2 database server. 
The unit test for this example runs H2 database server automatically (which can be run manually, if necessary).
You need to start H2 database server standalone when you are using `NCSqlModelGenerator` model generation utility.
You can run necessary JVMs for this example similarly from command line or IDE.
NOTE: that you don't need to start Data Probe standalone if you are only running the unit test as it uses the 
embedded probe and starts it automatically:
 *  Run REST server:
    * **Main class:** `org.apache.nlpcraft.NCStart`
    * **Program arguments:** `-server`
 * To run unit test with embedded probe:
    * **JUnit 5 test:** `org.apache.nlpcraft.examples.sql.SqlTest`
 * To run probe standalone and use your own [REST client](https://nlpcraft.apache.org/using-rest.html):
    * **Main class:** `org.apache.nlpcraft.NCStart`
    * **VM arguments:** `-Dconfig.override_with_env_vars=true`
    * **Environment variables:** `CONFIG_FORCE_nlpcraft_probe_models.0=org.apache.nlpcraft.examples.sql.SqlModel`
    * **Program arguments:** `-probe`
    
    When running data probe standalone you need run H2 database sever manually (from command line or IDE):
    * **Main class:** `org.apache.nlpcraft.examples.sql.db.SqlServerRunner`

### Documentation  
See [Getting Started](https://nlpcraft.apache.org/getting-started.html) guide for more instructions on how to run these examples.

For any questions, feedback or suggestions:

 * View & run other [examples](https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples)
 * Latest [Javadoc](http://nlpcraft.apache.org/apis/latest/index.html) and [REST APIs](https://nlpcraft.apache.org/using-rest.html)
 * Download & Maven/Grape/Gradle/SBT [instructions](https://nlpcraft.apache.org/download.html)
 * File a bug or improvement in [JIRA](https://issues.apache.org/jira/projects/NLPCRAFT)
 * Post a question at [Stack Overflow](https://stackoverflow.com/questions/ask) using <code>nlpcraft</code> tag
 * Access [GitHub](https://github.com/apache/incubator-nlpcraft) mirror repository.
 * Join project developers on [dev@nlpcraft.apache.org](mailto:dev-subscribe@nlpcraft.apache.org)
 
### Copyright
Copyright (C) 2020 Apache Software Foundation

<img src="https://www.apache.org/img/ASF20thAnniversary.jpg" height="64px">


