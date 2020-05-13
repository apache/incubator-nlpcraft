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

1. Release computer environment configuration.

    1.1 Apache release:
    - configure file prepare.sh, set your local GPG fingerprint localUser={YOUR_VALUE}

    1.2 Maven release:
    - Prepare maven passwords encryption (master and apache passwords) - https://maven.apache.org/guides/mini/guide-encryption.html
    - Encode gpg.passphrase for user aradzinski@datalingvo.com Aaron Radzinski https://superuser.com/questions/972204/how-to-use-gnupg-with-passphrase 
 
    As result, you should have in the folder {USER_HOME}/.m2 two following files:
    
    - settings-security.xml file should contain following content:
  
            <settingsSecurity>
                <master>{YOUR_MASTER_PASSWORD_CODE}</master>
            </settingsSecurity>
  
    - settings.xml file should contain following content:
  
            ...
            <server>
              <id>apache.snapshots.https</id>
              <username>YOUR_APACHE_USERNAME</username>
              <password>{YOUR_APACHE_PASSWORD_CODE}</password>
            </server>
            
            <server>
              <id>apache.releases.https</id>
              <username>YOUR_APACHE_USERNAME</username>
              <password>{YOUR_APACHE_PASSWORD_CODE}</password>
            </server>
            ...
            <profiles>
                ...
                <profile>
                    <id>ossrh</id>
                    <activation>
                        <activeByDefault>true</activeByDefault>
                    </activation>
                    <properties>
                        <gpg.executable>gpg2</gpg.executable>
                        <gpg.passphrase>YOUR_GPG2_PASSPHRASE</gpg.passphrase>
                        <gpg.keyname>YOUR_GPG2_KEY_ID</gpg.keyname>
                    </properties>
                </profile>
            </profiles>
            ...
            
2. Make Apache release. Note that pom.xml scm tag should contain actual tag name.

    Example:
    
        <scm>
            <url>https://github.com/apache/incubator-nlpcraft.git</url>
            <connection>scm:git:ssh://git@github.com/apache/incubator-nlpcraft.git</connection>
            <developerConnection>scm:git:ssh://git@github.com/apache/incubator-nlpcraft.git</developerConnection>
            <!-- Set actual tag name here -->
            <tag>v0.5.0</tag>
        </scm
3. Make Maven release
  - cd <PROJECT_FOLDER>
  - mvn -DskipTests=true clean deploy -P release
  - login https://repository.apache.org
  - look at staging repositories (order by date desc) https://repository.apache.org/#stagingRepositories
  - find and close nlpcraft release, check its state.   
  
  