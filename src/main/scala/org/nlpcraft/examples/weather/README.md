<img src="https://nlpcraft.org/images/nlpcraft_logo_black.gif" height="80px">
<br>
<a target=_ href="https://gitter.im/apache-nlpcraft/community"><img alt="Gitter" src="https://badges.gitter.im/apache-nlpcraft/community.svg"></a>&nbsp;

### Weather Service Example
This example demonstrates relatively complete NLI-based weather service with JSON output and a non-trivial
intent matching logic. It uses https://www.apixu.com REST service for the actual
weather information.

### Running
You can run this example from command line or IDE in a similar way:
 1. Run REST server:
    * **Main class:** `org.nlpcraft.NCStart`
    * **Program arguments:** `-server`
 2. Run data probe:
    * **Main class:** `org.nlpcraft.NCStart`
    * **VM arguments:** `-Dconfig.override_with_env_vars=true`
    * **Environment variables:** `CONFIG_FORCE_nlpcraft_probe_models.0=org.nlpcraft.examples.weather.WeatherModel`
    * **Program arguments:** `-probe`
 2. Run test:
    * **JUnit 5 test:** `org.nlpcraft.examples.weather.WeatherTest`
    * or use NLPCraft [REST APIs](https://nlpcraft.org/using-rest.html) with your favorite REST client

### Documentation  
See [Getting Started](https://nlpcraft.org/getting-started.html) guide for more instructions on how to run these examples.

For any questions, feedback or suggestions:

 * View & run other [examples](https://github.com/apache/incubator-nlpcraft/tree/master/src/main/scala/org/nlpcraft/examples)
 * Latest [Javadoc](https://github.com/apache/incubator-nlpcraft/apis/latest/index.html) and [REST APIs](https://nlpcraft.org/using-rest.html)
 * Download & Maven/Grape/Gradle/SBT [instructions](https://nlpcraft.org/download.html)
 * File a bug or improvement in [JIRA](https://issues.apache.org/jira/projects/NLPCRAFT)
 * Post a question at [Stack Overflow](https://stackoverflow.com/questions/ask) using <code>nlpcraft</code> tag
 * Access [GitHub](https://github.com/apache/incubator-nlpcraft) mirror repository.
 * Join project developers on [dev@nlpcraft.apache.org](mailto:dev@nlpcraft.apache.org)

### Copyright
Copyright (C) 2020 Apache Software Foundation

<img src="https://www.apache.org/img/ASF20thAnniversary.jpg" height="64px">


