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
cannot be readily integrated into standard Scaladoc toolchain and thus NLPCraft annotations are listed here 
along with their source code for the purpose of documentation: 

`org.apache.nlpcraft.annotations.`**NCIntent**
<pre> 
// Annotation to bind an intent with the method serving as its callback.
@Documented
@Retention(value=RUNTIME)
@Target(value={METHOD, TYPE})
@Repeatable(NCIntent.NCIntentList.class)
public @interface NCIntent {
    // Intent specification using IDL.
    String value() default "";

    // Grouping annotation required for when more than one 'NCIntent' annotation is used.
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Target(value={METHOD, TYPE})
    @interface NCIntentList {
        // Gets the list of all 'NCIntent' annotations attached to the callback or class. 
        NCIntent[] value();
    }
}
</pre>
`org.apache.nlpcraft.annotations.`**NCIntentRef**
<pre>
// Annotation referencing an intent defined outside of callback method declaration.
@Documented
@Retention(value=RUNTIME)
@Target(value=METHOD)
@Repeatable(NCIntentRef.NCIntentRefList.class)
public @interface NCIntentRef {
    // ID of the intent defined externally.
    String value() default "";

    // Grouping annotation required for when more than one 'NCIntentRef' annotation is used.
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value=METHOD)
    @Documented
    @interface NCIntentRefList {
        // Gets the list of all 'NCIntentRef' annotations attached to the callback.
        NCIntentRef[] value();
    }
}
</pre>

`org.apache.nlpcraft.annotations.`**NCIntentTerm**
<pre>
// Annotation to mark callback parameter to receive intent term's tokens. 
@Documented
@Retention(value=RUNTIME)
@Target(value=PARAMETER)
public @interface NCIntentTerm {
    // ID of the intent term.
    String value();
}
</pre>

`org.apache.nlpcraft.annotations`.**NCIntentObject**
<pre>
// Marker annotation that can be applied to class member of main model.
// The fields objects annotated with this annotation are scanned the same way as main model.
@Documented
@Retention(value=RUNTIME)
@Target(value=FIELD)
public @interface NCIntentObject {
    // No-op.
}
</pre>
