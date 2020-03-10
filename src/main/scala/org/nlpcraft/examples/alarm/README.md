<img src="https://nlpcraft.org/images/nlpcraft_logo_black.gif" height="80px">
<br>
<a target=_ href="https://gitter.im/apache-nlpcraft/community"><img alt="Gitter" src="https://badges.gitter.im/apache-nlpcraft/community.svg"></a>&nbsp;
<a target=_ href="https://travis-ci.org/nlpcrafters/nlpcraft#"><img alt="Build" src="https://travis-ci.org/nlpcrafters/nlpcraft.svg?branch=master"></a>&nbsp;
<a target=_ href="https://search.maven.org/search?q=org.apache.nlpcraft"><img src="https://maven-badges.herokuapp.com/maven-central/org.apache.nlpcraft/nlpcraft/badge.svg" alt="Maven"></a>

### Alarm Example
This example provides a simple "alarm clock" interface where you can ask to set the timer for a 
specific duration from now expressed in hours, minutes and/or seconds. You can say 
`ping me in 3 minutes`, `buzz me in an hour and 15 minutes`, or `set my alarm for 30 secs`. 
When the timers is up it will simply print out "BEEP BEEP BEEP" in the probe console.

### Running
You can run this example from command line or IDE in a similar way:
 1. Run REST server:
    * **Main class:** `org.nlpcraft.NCStart`
    * **Program arguments:** `-server`
 2. Run data probe:
    * **Main class:** `org.nlpcraft.NCStart`
    * **VM arguments:** `-Dconfig.override_with_env_vars=true`
    * **Environment variables:** `CONFIG_FORCE_nlpcraft_probe_models.0=org.nlpcraft.examples.lightswitch.LightSwitchModel`
    * **Program arguments:** `-probe`
 2. Run test:
    * **JUnit 5 test:** `org.nlpcraft.examples.alarm.AlarmTest`
    * or use NLPCraft [REST APIs](https://nlpcraft.org/using-rest.html) with your favorite REST client

### Documentation
See [Getting Started](https://nlpcraft.org/getting-started.html) guide for more instructions on how to run these examples.

For any questions, feedback or suggestions:

 * Send us a note at [support@nlpcraft.org](mailto:support@nlpcraft.org)
 * Post a question at [Stack Overflow](https://stackoverflow.com/questions/ask) using <code>nlpcraft</code> tag
 * If you found a bug or have an idea file new issue on [GitHub](https://github.com/apache/incubator-nlpcraft/issues).

### Copyright
Copyright (C) 2020 Apache Software Foundation

<img src="https://www.apache.org/img/ASF20thAnniversary.jpg" height="64px">


