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

package org.apache.nlpcraft.server

import java.util.concurrent.CountDownLatch

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.nlp.dict.NCDictionaryManager
import org.apache.nlpcraft.common.nlp.numeric.NCNumericManager
import org.apache.nlpcraft.common.opencensus.NCOpenCensusTrace
import org.apache.nlpcraft.common.version._
import org.apache.nlpcraft.server.company.NCCompanyManager
import org.apache.nlpcraft.server.feedback.NCFeedbackManager
import org.apache.nlpcraft.server.geo.NCGeoManager
import org.apache.nlpcraft.server.ignite.{NCIgniteInstance, NCIgniteRunner}
import org.apache.nlpcraft.server.lifecycle.NCServerLifecycleManager
import org.apache.nlpcraft.server.nlp.core.NCNlpServerManager
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnrichmentManager
import org.apache.nlpcraft.server.nlp.preproc.NCPreProcessManager
import org.apache.nlpcraft.server.nlp.spell.NCSpellCheckManager
import org.apache.nlpcraft.server.nlp.wordnet.NCWordNetManager
import org.apache.nlpcraft.server.probe.NCProbeManager
import org.apache.nlpcraft.server.proclog.NCProcessLogManager
import org.apache.nlpcraft.server.query.NCQueryManager
import org.apache.nlpcraft.server.rest.NCRestManager
import org.apache.nlpcraft.server.sql.NCSqlManager
import org.apache.nlpcraft.server.tx.NCTxManager
import org.apache.nlpcraft.server.user.NCUserManager

import scala.compat.Platform.currentTime
import scala.util.control.Exception.{catching, ignoring}

/**
  * NLPCraft server app.
  */
object NCServer extends App with NCIgniteInstance with LazyLogging with NCOpenCensusTrace {
    /**
      * Prints ASCII-logo.
      */
    private def asciiLogo() {
        val NL = System getProperty "line.separator"
        val ver = NCVersion.getCurrent
        
        val s = NL +
            raw"    _   ____      ______           ______   $NL" +
            raw"   / | / / /___  / ____/________ _/ __/ /_  $NL" +
            raw"  /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/  $NL" +
            raw" / /|  / / /_/ / /___/ /  / /_/ / __/ /_    $NL" +
            raw"/_/ |_/_/ .___/\____/_/   \__,_/_/  \__/    $NL" +
            raw"       /_/                                  $NL$NL" +
            s"Server$NL" +
            s"Version: ${ver.version}$NL" +
            raw"${NCVersion.copyright}$NL"
        
        logger.info(s)
    }

    /**
      * Starts all managers.
      */
    private def startManagers(): Unit = {
        val ver = NCVersion.getCurrent

        // Lifecycle manager has to be started outside of the tracing span.
        NCServerLifecycleManager.start()
        // Lifecycle callback.
        NCServerLifecycleManager.beforeStart()
    
        startScopedSpan("startManagers", "relVer" → ver.version, "relDate" → ver.date) { span ⇒
            U.executeParallel(
                () ⇒ {
                    NCNlpCoreManager.start(span)
                    NCNlpServerManager.start(span)
                    NCNumericManager.start(span)
                },
                () ⇒ {
                    NCTxManager.start(span)
                    NCSqlManager.start(span)
                    NCProcessLogManager.start(span)
                },
                () ⇒ {
                    NCWordNetManager.start(span)
                },
                () ⇒ {
                    NCDictionaryManager.start(span)
                    NCGeoManager.start(span)
                },
                () ⇒ {
                    NCSpellCheckManager.start(span)
                    NCPreProcessManager.start(span)
                }
            )
            
            NCServerEnrichmentManager.start(span)
    
            U.executeParallel(
                () ⇒ {
                    // User manager called from companies manager.
                    NCUserManager.start(span)
                    NCCompanyManager.start(span)
                },
                () ⇒ NCProbeManager.start(span),
                () ⇒ NCFeedbackManager.start(span)
            )
            
            NCQueryManager.start(span)
            NCRestManager.start(span)
    
            // Lifecycle callback.
            NCServerLifecycleManager.afterStart()
        }
    }
    
    /**
      * Stops all managers.
      */
    private def stopManagers(): Unit = {
        // Lifecycle callback.
        NCServerLifecycleManager.beforeStop()
    
        startScopedSpan("stopManagers") { span ⇒
            Seq(
                NCRestManager,
                NCQueryManager,
                NCFeedbackManager,
                NCCompanyManager,
                NCUserManager,
                NCServerEnrichmentManager,
                NCNumericManager,
                NCNlpServerManager,
                NCGeoManager,
                NCPreProcessManager,
                NCSpellCheckManager,
                NCDictionaryManager,
                NCWordNetManager,
                NCProcessLogManager,
                NCSqlManager,
                NCTxManager,
                NCNlpCoreManager
            ).foreach(p ⇒
                try
                    p.stop(span)
                catch {
                    case e: Exception ⇒ logger.warn("Error stopping manager.", e)
                }
            )
        }
        
        // Lifecycle callback.
        NCServerLifecycleManager.afterStop()
    
        NCServerLifecycleManager.stop()
    }
    
    /**
      * Acks server start.
      */
    protected def ackStart() {
        val dur = s"[${U.format((currentTime - executionStart) / 1000.0, 2)}s]"
        
        val tbl = NCAsciiTable()
        
        tbl.margin(top = 1, bottom = 1)
        
        tbl += s"Server started $dur"
        
        tbl.info(logger)
    }

    /**
      *
      * @return
      */
    private def setParameters(): Unit = {
        System.setProperty("java.net.preferIPv4Stack", "true")
    }

    /**
      *
      */
    private def start(): Unit = {
        setParameters()

        NCConfigurable.initialize(
            None, // No overrides.
            args.find(_.startsWith("-config=")) match {
                case Some(s) ⇒
                    val fileName = s.substring("-config=".length)
                    
                    val f = new java.io.File(fileName)

                    if (!(f.exists && f.canRead && f.isFile))
                        throw new NCE(s"Specified server configuration file does not exist or cannot be read: $fileName")

                    Some(fileName)
                    
                case None ⇒
                    Some("nlpcraft.conf") // Default to 'nlpcraft.conf'.
            },
            None, // No defaults.
            (cfg: Config) ⇒ cfg.hasPath("nlpcraft.server")
        )
        
        asciiLogo()
        
        val lifecycle = new CountDownLatch(1)
    
        catching(classOf[Throwable]) either startManagers() match {
            case Left(e) ⇒ // Exception.
                e match {
                    case x: NCE ⇒ logger.error(s"Failed to start server.", x)
                    case x: Throwable ⇒ logger.error("Failed to start server due to unexpected error.", x)
                }
            
                System.exit(1)
        
            case _ ⇒ // Managers started OK.
                ackStart()

                Runtime.getRuntime.addShutdownHook(new Thread() {
                    override def run(): Unit = {
                        ignoring(classOf[Throwable]) {
                            stopManagers()
                        }

                        lifecycle.countDown()
                    }
                })

                U.ignoreInterrupt {
                    lifecycle.await()
                }
        }
    }

    NCIgniteRunner.runWith(
        args.find(_.startsWith("-igniteConfig=")) match {
            case None ⇒ null // Will use default on the classpath 'ignite.xml'.
            case Some(s) ⇒ s.substring(s.indexOf("=") + 1)
        },
        start()
    )
}
