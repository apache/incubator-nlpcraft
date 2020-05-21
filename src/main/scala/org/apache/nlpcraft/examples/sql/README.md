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
[![Jenkins](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fbuilds.apache.org%2Fview%2FIncubator%2520Projects%2Fjob%2Fincubator-nlpcraft%2F)](https://builds.apache.org/view/Incubator%20Projects/job/incubator-nlpcraft/)
[![Documentation Status](https://img.shields.io/:docs-latest-green.svg)](https://nlpcraft.apache.org/docs.html)
[![Gitter](https://badges.gitter.im/apache-nlpcraft/community.svg)](https://gitter.im/apache-nlpcraft/community)

### SQL model example.

SQL model example can be used as all other examples with one difference, is needs started H2 database server.
   - When you start Probe with deployed SQL model, you have to run org.apache.nlpcraft.examples.sql.db.SqlServerRunner before to start database instance.
   - You also have to run org.apache.nlpcraft.examples.sql.db.SqlServerRunner when you use org.apache.nlpcraft.model.tools.sqlgen.NCSqlModelGenerator.
   - You don't need to run org.apache.nlpcraft.examples.sql.db.SqlServerRunner when run org.apache.nlpcraft.examples.sql.SqlModelTest because it is already started H2 database server during tests initialization inside.       

### Copyright
Copyright (C) 2020 Apache Software Foundation

<img src="https://www.apache.org/img/ASF20thAnniversary.jpg" height="64px">


