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

package org.apache.nlpcraft.common.config

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.common._

import java.io.File
import java.net.{MalformedURLException, URL}
import scala.collection.JavaConverters._

/**
  * Mixin for configuration factory based on https://github.com/lightbend/config.
  */
trait NCConfigurable {
    import NCConfigurable._
    
    // Accessor to the loaded config. It should reload config.
    private def hocon: Config = cfg
    
    /**
      *
      * @param name Full configuration property path (name).
      */
    private def checkMandatory(name: String): Unit =
        if (!hocon.hasPath(name))
            throw new NCE(s"Mandatory configuration property not found: $name")
    
    /**
      * Gets mandatory configuration property.
      *
      * @param name Full configuration property path (name).
      */
    def getInt(name: String): Int = {
        checkMandatory(name)
        
        hocon.getInt(name)
    }
    
    /**
      * Gets mandatory configuration property.
      *
      * @param name Full configuration property path (name).
      */
    def getBool(name: String): Boolean = {
        checkMandatory(name)
        
        hocon.getBoolean(name)
    }
    
    /**
      * Gets optional configuration property.
      *
      * @param name Full configuration property path (name).
      */
    def getBoolOpt(name: String): Option[Boolean] =
        if (!hocon.hasPath(name)) None else Some(hocon.getBoolean(name))
    
    /**
      * Gets mandatory configuration property.
      *
      * @param name Full configuration property path (name).
      */
    def getLong(name: String): Long = {
        checkMandatory(name)
        
        hocon.getLong(name)
    }
    
    /**
      * Gets mandatory configuration property.
      *
      * @param name Full configuration property path (name).
      */
    def getConfig(name: String): Config = {
        checkMandatory(name)
        
        hocon.getConfig(name)
    }
    
    /**
      * Gets optional configuration property.
      *
      * @param name Full configuration property path (name).
      */
    def getConfigOpt(name: String): Option[Config] =
        if (!hocon.hasPath(name)) None else Some(hocon.getConfig(name))
    
    /**
      * Gets mandatory configuration property.
      *
      * @param name Full configuration property path (name).
      */
    def getLongList(name: String): java.util.List[java.lang.Long] = {
        checkMandatory(name)
        
        hocon.getLongList(name)
    }
    
    /**
      * Gets mandatory configuration property in `host:port` format.
      *
      * @param name Full configuration property path (name).
      */
    def getHostPort(name: String): (String, Integer) = {
        checkMandatory(name)
        
        val ep = getString(name)
    
        val i = ep.indexOf(':')
    
        if (i <= 0)
            throw new NCE(s"Invalid 'host:port' endpoint configuration property format [" +
                s"name=$name, " +
                s"endpoint=$ep" +
            s"]")
            
        try
            ep.substring(0, i) → ep.substring(i + 1).toInt
        catch {
            case _: NumberFormatException ⇒
                throw new NCE(s"Invalid 'host:port' endpoint configuration property port [" +
                    s"name=$name, " +
                    s"endpoint=$ep" +
                s"]")
        }
    }
    
    /**
      * Gets optional configuration property in `host:port` format.
      *
      * @param name Full configuration property path (name).
      */
    def getHostPortOpt(name: String): Option[(String, Integer)] =
        if (hocon.hasPath(name)) Some(getHostPort(name)) else None
    
    /**
      * Gets optional configuration property in `host:port` format.
      *
      * @param name Full configuration property path (name).
      * @param dfltHost Default host value.
      * @param dfltPort Default port value.
      */
    def getHostPortOrElse(name: String, dfltHost: String, dfltPort: Int): (String, Integer) =
        getHostPortOpt(name).getOrElse(dfltHost → dfltPort)

    /**
      * Gets mandatory configuration property.
      *
      * @param name Full configuration property path (name).
      */
    def getString(name: String): String = {
        checkMandatory(name)
        
        hocon.getString(name)
    }
    
    /**
      * Gets mandatory configuration property.
      *
      * @param name Full configuration property path (name).
      */
    def getObject[T](name: String, f: String ⇒ T): T = {
        checkMandatory(name)
    
        val v = hocon.getString(name)
        
        try
            f(v)
        catch {
            case _: Exception ⇒
                throw new NCE(s"Configuration property cannot be extracted [" +
                    s"name=$name, " +
                    s"value='$v'" +
                s"]")
        }
    }
    
    /**
      * Gets mandatory configuration property.
      *
      * @param name Full configuration property path (name).
      */
    def getMap[K, V](name: String): Map[K, V] = {
        checkMandatory(name)
        
        try
            hocon.getAnyRef(name).asInstanceOf[java.util.Map[K, V]].asScala.toMap
        catch {
            case e: ClassCastException ⇒
                throw new NCE(s"Configuration property has unexpected type (expecting 'java.util.Map') [" +
                    s"name=$name" +
                s"]", e)
        }
    }
    
    /**
      * Gets optional configuration property.
      *
      * @param name Full configuration property path (name).
      */
    def getMapOpt[K, V](name: String): Option[Map[K, V]] =
        if (!hocon.hasPath(name)) None else Some(getMap(name))
    
    /**
      * Gets optional configuration property.
      *
      * @param name Full configuration property path (name).
      */
    def getStringOpt(name: String): Option[String] =
        if (!hocon.hasPath(name)) None else Some(hocon.getString(name))
    
    /**
      * Gets optional configuration property.
      *
      * @param name Full configuration property path (name).
      */
    def getStringOrElse(name: String, dflt: String): String =
        getStringOpt(name).getOrElse(dflt)

    /**
      * Gets mandatory configuration property.
      * Note that processed parameter is CSV single string.
      *
      * @param name Full configuration property path (name).
      */
    def getStringList(name: String): Seq[String] = {
        checkMandatory(name)

        parseCsv(hocon.getString(name))
    }

    /**
      * Gets optional configuration property.
      * Note that processed parameter is CSV single string.
      *
      * @param name Full configuration property path (name).
      */
    def getStringListOpt(name: String): Option[Seq[String]] = if (!hocon.hasPath(name)) None else Some(getStringList(name))

    /**
      *
      * @param s
      */
    private def parseCsv(s: String): Seq[String] = U.splitTrimFilter(s,",")
}

object NCConfigurable extends LazyLogging {
    private var cfg: Config = _

    /**
      * Initializes system-wide configuration singleton with given parameters. All specific implementations
      * of `NCConfigurable` trait will reuse this config instance.
      * <p>
      * Override configuration, if given, will override any other loaded or merged configuration.
      * If configuration file name is provided it will be looked up in the current working directory or on the
      * classpath (as class loader resource). Default configuration, if provided, will be used as a final
      * fallback.
      * <p>
      * To override configuration from outside of JVM using environment variables:
      *   1. Set system variable -Dconfig.override_with_env_vars=true
      *   2. Use environment variables in a form of 'CONFIG_FORCE_x_y_z' to override configuration
      *      property 'x.y.z' from the file.
      * <p>
      * Examples:
      *   CONFIG_FORCE_nlpcraft_server_rest_host="localhost"
      *   CONFIG_FORCE_nlpcraft_server_models="com.mymodels.MyModel"
      *
      * @param overrideCfg Optional overriding configuration.
      * @param cfgFileOpt Optional file name.
      * @param dfltCfg Optional default config.
      * @param valFun Validation method.
      */
    def initialize(
        overrideCfg: Option[Config],
        cfgFileOpt: Option[String],
        dfltCfg: Option[Config],
        valFun: Config ⇒ Boolean
    ): Unit = {
        require(cfgFileOpt.isDefined || dfltCfg.isDefined)

        val tmpCfg =
            // Only default configuration is provided.
            if (cfgFileOpt.isEmpty) {
                logger.info(s"Using built-in default configuration.")

                ConfigFactory.load(dfltCfg.get)
            }
            else {
                val name = cfgFileOpt.get

                logger.info(s"Attempting to load/merge configuration from configuration file: $name")

                // Order is: file, URL, resource (File and URL can override resource)
                var cfg = ConfigFactory.parseFile(new File(name))

                try
                    cfg = cfg.withFallback(ConfigFactory.parseURL(new URL(name)))
                catch {
                    case _: MalformedURLException ⇒ // No-op.
                }

                cfg = cfg.withFallback(ConfigFactory.parseResources(name))

                if (dfltCfg.isDefined)
                    cfg = cfg.withFallback(dfltCfg.get)

                cfg
            }
        
        // Validate.
        if (!valFun(tmpCfg)) {
            logger.error(s"Invalid configuration.")
            logger.error(s"Note that you can use environment variable to provide configuration properties - see https://nlpcraft.apache.org/server-and-probe.html.")
    
            throw new NCE(s"No valid configuration found: ${tmpCfg.origin().description()}")
        }
        else {
            if (overrideCfg.isDefined)
                cfg = ConfigFactory.load(overrideCfg.get).withFallback(tmpCfg)
            else
                cfg = ConfigFactory.load(tmpCfg)
            
            val lines = U.splitTrimFilter(cfg.origin().description(),",").drop(1).distinct
            
            logger.info(s"NLPCraft configuration successfully loaded as a merge of: ${lines.mkString("\n  + ", "\n  + ", "")}")
        }

        // Set parsed configuration into Java shim.
        NCConfigurableJava.setConfig(cfg)
    }
}