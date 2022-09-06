<pre>
     _   ____      ______           ______ 
    / | / / /___  / ____/________ _/ __/ /_
   /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/
  / /|  / / /_/ / /___/ /  / /_/ / __/ /_  
 /_/ |_/_/ .___/\____/_/   \__,_/_/  \__/  
        /_/                  

SCALA3 API TO CONVERT NATURAL LANGUAGE INTO ACTION
</pre>

- Full documentation at [[https://nlpcraft.apache.org]]
- Examples [[https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft-examples]]
- GitHub project [[https://github.com/apache/incubator-nlpcraft]]

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/apache/opennlp/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.apache.nlpcraft/nlpcraft)](https://search.maven.org/artifact/org.apache.nlpcraft/nlpcraft)

#### **Maven dependency:**
<pre>
&lt;dependency&gt;
  &lt;groupId&gt;org.apache.nlpcraft&lt;/groupId&gt;
  &lt;artifactId&gt;nlpcraft&lt;/artifactId&gt;
  &lt;version&gt;1.0.0&lt;/version&gt;
&lt;/dependency&gt;
</pre>

#### **SBT dependency:**
<pre>
libraryDependencies += "org.apache.nlpcraft" % "nlpcraft" % "1.0.0"
</pre>

#### **Annotations:**
Due to Scala 3 limitation on runtime retained annotations NLPCraft annotations are written in Java. Javadoc documentation
cannot be readily integrated into standard Scaladoc tooling chain and therefore NLPCraft annotations are listed here 
along with their source code for the purpose of documentation: 

`org.apache.nlpcraft.annotations.`**NCIntent**

`org.apache.nlpcraft.annotations.`**NCIntentRef**

`org.apache.nlpcraft.annotations.`**NCIntentTerm**

`org.apache.nlpcraft.annotations`.**NCIntentObject**
