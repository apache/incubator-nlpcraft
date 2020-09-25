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

import java.io.{File, FileInputStream, FileOutputStream, IOException, ObjectInputStream, ObjectOutputStream}
import java.util.concurrent.CountDownLatch

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.lang3.SystemUtils
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ansi.NCAnsiColor
import org.apache.nlpcraft.common.ansi.NCAnsiColor._
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.extcfg.NCExternalConfigManager
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
import org.apache.nlpcraft.server.sugsyn.NCSuggestSynonymManager
import org.apache.nlpcraft.server.tx.NCTxManager
import org.apache.nlpcraft.server.user.NCUserManager
import resource.managed

import scala.collection.mutable
import scala.compat.Platform.currentTime
import scala.util.control.Exception.{catching, ignoring}

/**
  * NLPCraft server app.
  */
object NCServer extends App with NCIgniteInstance with LazyLogging with NCOpenCensusTrace {
    private final val PID_PATH = ".nlpcraft/server_pid"

    private val startedMgrs = mutable.Buffer.empty[NCService]

    /**
      * Prints ASCII-logo.
      */
    private def asciiLogo() {
        val ver = NCVersion.getCurrent

        logger.info(
            U.NL +
            U.asciiLogo() +
            s"${U.NL}" +
            s"Server${U.NL}" +
            s"Version: $ansiBold${ver.version}${U.NL}$ansiReset" +
            s"${NCVersion.copyright}${U.NL}"
        )
    }

    /**
      * Starts all managers.
      */
    private def startManagers(): Unit = {
        val ver = NCVersion.getCurrent

        // Lifecycle manager has to be started outside of the tracing span.
        NCServerLifecycleManager.start()
        NCServerLifecycleManager.beforeStart()
    
        startScopedSpan("startManagers", "relVer" → ver.version, "relDate" → ver.date) { span ⇒
            startedMgrs += NCExternalConfigManager.start(span)
            startedMgrs += NCWordNetManager.start(span)
            startedMgrs += NCDictionaryManager.start(span)
            startedMgrs += NCTxManager.start(span)
            startedMgrs += NCSqlManager.start(span)
            startedMgrs += NCProcessLogManager.start(span)
            startedMgrs += NCGeoManager.start(span)
            startedMgrs += NCNlpCoreManager.start(span)
            startedMgrs += NCNlpServerManager.start(span)
            startedMgrs += NCNumericManager.start(span)
            startedMgrs += NCSpellCheckManager.start(span)
            startedMgrs += NCPreProcessManager.start(span)
            startedMgrs += NCServerEnrichmentManager.start(span)
            startedMgrs += NCUserManager.start(span)
            startedMgrs += NCCompanyManager.start(span)
            startedMgrs += NCProbeManager.start(span)
            startedMgrs += NCSuggestSynonymManager.start(span)
            startedMgrs += NCFeedbackManager.start(span)
            startedMgrs += NCQueryManager.start(span)
            startedMgrs += NCRestManager.start(span)
    
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
            startedMgrs.reverse.foreach(p ⇒
                try
                    p.stop(span)
                catch {
                    case e: Exception ⇒ U.prettyError(logger, s"Error stopping manager: ${p.name}", e)
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
        
        tbl += s"Server started $ansiBlueFg$dur$ansiReset"
        
        tbl.info(logger)
    }

    /**
      *
      * @return
      */
    private def setSysProps(): Unit = {
        System.setProperty("java.net.preferIPv4Stack", "true")
    }

    /**
      *
      */
    private def start(): Unit = {
        NCAnsiColor.ackStatus()

        setSysProps()

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

        storePid()
        
        val lifecycle = new CountDownLatch(1)
    
        catching(classOf[Throwable]) either startManagers() match {
            case Left(e) ⇒ // Exception.
                U.prettyError(logger, "Failed to start server:", e)

                stopManagers()

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

    /**
     *
     */
    private def storePid(): Unit = {
        val path = new File(SystemUtils.getUserHome, PID_PATH)

        /**
         *
         */
        def storeInUserHome() =
            try {
                managed(new ObjectOutputStream(new FileOutputStream(path))) acquireAndGet { stream ⇒
                    stream.writeLong(ProcessHandle.current().pid())
                    stream.flush()
                }

                logger.info(s"PID stored in: ${path.getAbsolutePath}")
            }
            catch {
                case e: IOException ⇒ U.prettyError(logger, "Failed to store server PID.", e)
            }

        if (path.exists())
            catching(classOf[IOException]) either {
                managed(new ObjectInputStream(new FileInputStream(path))) acquireAndGet { _.readLong() }
            } match {
                case Left(e) ⇒ U.prettyError(logger, s"Failed to read existing PID from: ${path.getAbsolutePath}", e)
                case Right(pid) ⇒
                    if (ProcessHandle.of(pid).isPresent)
                        logger.error(s"Cannot store PID file as another local server detected [existing-pid=$pid]")
                    else
                        storeInUserHome()
            }
        else
            storeInUserHome()
    }

    NCIgniteRunner.runWith(
        args.find(_.startsWith("-igniteConfig=")) match {
            case None ⇒ null // Will use default on the classpath 'ignite.xml'.
            case Some(s) ⇒ s.substring(s.indexOf("=") + 1)
        },
        start()
    )
}
