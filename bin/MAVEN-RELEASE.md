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

<img src="https://nlpcraft.apache.org/images/nlpcraft_logo_black.gif" height="80px" alt="">

## 1. Configure Release Environment

### Apache release
- Configure file `prepare.sh` by setting your local GPG fingerprint `localUser={YOUR_VALUE}`

### Maven release
- Prepare maven passwords encryption (master and apache passwords) - https://maven.apache.org/guides/mini/guide-encryption.html
- Encode **gpg.passphrase** for desired user - https://central.sonatype.org/pages/working-with-pgp-signatures.html 
 
As result, you should have the following files in the folder `{USER_HOME}/.m2`:
- `settings-security.xml` file should contain the following content:
    ```xml
    <settingsSecurity>
        <master>{YOUR_MASTER_PASSWORD}</master>
    </settingsSecurity>
    ```
- `settings.xml` file should contain the following content:
    ```xml  
    <server>
      <id>apache.snapshots.https</id>
      <username>YOUR_APACHE_USERNAME</username>
      <password>{YOUR_APACHE_PASSWORD}</password>
    </server>
    
    <server>
      <id>apache.releases.https</id>
      <username>YOUR_APACHE_USERNAME</username>
      <password>{YOUR_APACHE_PASSWORD}</password>
    </server>
    
    <profiles>
        <profile>
            <id>gpg</id>
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
    ```
    **Note:** encrypted passwords must be in curly brackets.      
## 2. Make Apache Release 
Note that `pom.xml` scm tag should contain actual tag name.
For example, if version is `0.5.0` and tag name is `v0.5.0` following pom `scm` section should be:
```xml   
<scm>
    <url>https://github.com/apache/incubator-nlpcraft.git</url>
    <connection>scm:git:ssh://git@github.com/apache/incubator-nlpcraft.git</connection>
    <developerConnection>scm:git:ssh://git@github.com/apache/incubator-nlpcraft.git</developerConnection>
    <!-- Set actual tag name here -->
    <tag>v0.5.0</tag>
</scm>
```     

The following `pom.xml` files should be updates:
- nlpcraft/pom.xml
- nlpcraft-stanford/pom.xml
- pom.xml

## 3. Make Maven Release
  - `cd <PROJECT_FOLDER>`
  - `mvn -DskipTests=true clean deploy -P 'stanford-corenlp,release'`
  - Login into https://repository.apache.org
  - Look at staging repositories https://repository.apache.org/#stagingRepositories
  - Find and close `nlpcraft` release, then check its state.   
