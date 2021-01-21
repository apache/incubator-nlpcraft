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

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.lang3.SystemUtils
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ansi.NCAnsi
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.extcfg.NCExternalConfigManager
import org.apache.nlpcraft.common.module.NCModule
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.nlp.dict.NCDictionaryManager
import org.apache.nlpcraft.common.nlp.numeric.NCNumericManager
import org.apache.nlpcraft.common.opencensus.NCOpenCensusTrace
import org.apache.nlpcraft.common.pool.NCPoolManager
import org.apache.nlpcraft.common.version._
import org.apache.nlpcraft.model.tools.cmdline.NCCliServerBeacon
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

import java.io._
import java.util.concurrent.CountDownLatch
import scala.collection.mutable
import scala.compat.Platform.currentTime
import scala.util.control.Exception.{catching, ignoring}

/**
 * NLPCraft server app.
 */
object NCServer extends App with NCIgniteInstance with LazyLogging with NCOpenCensusTrace {
    private final val BEACON_PATH = ".nlpcraft/server_beacon"

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
                s"Version: ${bo(ver.version)}${U.NL}" +
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
            startedMgrs += NCPoolManager.start(span)
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

        tbl += s"Server started ${b(dur)}"

        tbl.info(logger)
    }

    /**
     *
     * @return
     */
    private def setSysProps(): Unit = {
        System.setProperty("java.net.preferIPv4Stack", "true")
        NCModule.setModule(NCModule.SERVER)
    }

    /**
     *
     */
    private def start(): Unit = {
        NCAnsi.ackStatus()

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

        val lifecycle = new CountDownLatch(1)

        catching(classOf[Throwable]) either startManagers() match {
            case Left(e) ⇒ // Exception.
                U.prettyError(logger, "Failed to start server:", e)

                stopManagers()

                System.exit(1)

            case _ ⇒ // Managers started OK.
                // Store beacon file once all managers started OK.
                storeBeacon()

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
    private def storeBeacon(): Unit = {
        val path = new File(SystemUtils.getUserHome, BEACON_PATH)

        /**
         *
         */
        def save(): Unit = {
            final object Config extends NCConfigurable {
                final private val pre = "nlpcraft.server"

                lazy val pid = ProcessHandle.current().pid()
                lazy val restHost = getString(s"$pre.rest.host")
                lazy val restApi = getString(s"$pre.rest.apiImpl")
                lazy val restPort = getInt(s"$pre.rest.port")
                lazy val upLink = getString(s"$pre.probe.links.upLink")
                lazy val downLink = getString(s"$pre.probe.links.downLink")
                lazy val dbUrl = getString(s"$pre.database.jdbc.url")
                lazy val dbDriver = getString(s"$pre.database.jdbc.driver")
                lazy val dbPoolMin = getInt(s"$pre.database.c3p0.pool.minSize")
                lazy val dbPoolMax = getInt(s"$pre.database.c3p0.pool.maxSize")
                lazy val dbPoolInit = getInt(s"$pre.database.c3p0.pool.initSize")
                lazy val dbPoolInc = getInt(s"$pre.database.c3p0.pool.acquireIncrement")
                lazy val dbInit = getBool(s"$pre.database.igniteDbInitialize")
                lazy val tokProviders = getString(s"$pre.tokenProviders")
                lazy val acsToksScanMins = getInt(s"$pre.user.timeoutScannerFreqMins")
                lazy val acsToksExpireMins = getInt(s"$pre.user.accessTokenExpireTimeoutMins")
                lazy val nlpEngine = getString("nlpcraft.nlpEngine")
                lazy val extCfgUrl = getString("nlpcraft.extConfig.extUrl")
                lazy val extCfgCheckMd5 = getBool("nlpcraft.extConfig.checkMd5")
                lazy val restEndpoint = s"${Config.restHost}:${Config.restPort}/api/v1"
            }

            try {
                managed(new ObjectOutputStream(new FileOutputStream(path))) acquireAndGet { stream ⇒
                    stream.writeObject(NCCliServerBeacon(
                        pid = Config.pid,
                        dbUrl = Config.dbUrl,
                        dbDriver = Config.dbDriver,
                        dbPoolMin = Config.dbPoolMin,
                        dbPoolMax = Config.dbPoolMax,
                        dbPoolInit = Config.dbPoolInit,
                        dbPoolInc = Config.dbPoolInc,
                        dbInit = Config.dbInit,
                        restEndpoint = Config.restEndpoint,
                        restApi = Config.restApi,
                        upLink = Config.upLink,
                        downLink = Config.downLink,
                        tokenProviders = Config.tokProviders,
                        nlpEngine = Config.nlpEngine,
                        extConfigUrl = Config.extCfgUrl,
                        extConfigCheckMd5 = Config.extCfgCheckMd5,
                        acsToksScanMins = Config.acsToksScanMins,
                        acsToksExpireMins = Config.acsToksExpireMins,
                        beaconPath = path.getAbsolutePath,
                        startMs = currentTime
                    ))

                    stream.flush()
                }

                // Make sure beacon is deleted when server process exits.
                path.deleteOnExit()

                val tbl = NCAsciiTable()

                tbl += (s"${b("PID")}", Config.pid)
                tbl += (s"${b("Database:")}", "")
                tbl += (s"${b("  JDBC URL")}", Config.dbUrl)
                tbl += (s"${b("  JDBC Driver")}", Config.dbDriver)
                tbl += (s"${b("  Pool min")}", Config.dbPoolMin)
                tbl += (s"${b("  Pool init")}", Config.dbPoolInit)
                tbl += (s"${b("  Pool max")}", Config.dbPoolMax)
                tbl += (s"${b("  Pool increment")}", Config.dbPoolInc)
                tbl += (s"${b("  Reset on start")}", Config.dbInit)
                tbl += (s"${b("REST:")}", "")
                tbl += (s"${b("  Endpoint")}", Config.restEndpoint)
                tbl += (s"${b("  API provider")}", Config.restApi)
                tbl += (s"${b("Probe:")}", "")
                tbl += (s"${b("  Uplink")}", Config.upLink)
                tbl += (s"${b("  Downlink")}", Config.downLink)
                tbl += (s"${b("Token providers")}", Config.tokProviders)
                tbl += (s"${b("NLP engine")}", Config.nlpEngine)
                tbl += (s"${b("Access tokens:")}", "")
                tbl += (s"${b("  Scan frequency")}", s"${Config.acsToksScanMins} mins")
                tbl += (s"${b("  Expiration timeout")}", s"${Config.acsToksExpireMins} mins")
                tbl += (s"${b("External config:")}", "")
                tbl += (s"${b("  URL")}", Config.extCfgUrl)
                tbl += (s"${b("  Check MD5")}", Config.extCfgCheckMd5)

                logger.info(s"Sever configuration:\n$tbl")
            }
            catch {
                case e: IOException ⇒ U.prettyError(logger, "Failed to save server beacon.", e)
            }
        }

        if (path.exists())
            catching(classOf[IOException]) either {
                managed(new ObjectInputStream(new FileInputStream(path))) acquireAndGet {
                    _.readObject()
                }
            } match {
                case Left(e) ⇒
                    logger.trace(s"Failed to read existing server beacon: ${path.getAbsolutePath}", e)
                    logger.trace(s"Overriding corrupted server beacon: ${path.getAbsolutePath}")

                    save()

                case Right(rawObj) ⇒
                    val beacon = rawObj.asInstanceOf[NCCliServerBeacon]

                    if (ProcessHandle.of(beacon.pid).isPresent) {
                        logger.warn(s"Another local server detected (safely ignored) [pid=${beacon.pid}]")
                        logger.warn(s"NOTE: ${c("only one")} locally deployed server can be managed by 'nlpcraft.{sh|cmd}' management script.")

                        // Leave the existing server beacon as is...
                    } else {
                        logger.trace(s"Overriding server beacon for a phantom process [pid=${beacon.pid}]")

                        save()
                    }
            }
        else
        // No existing beacon file detected.
            save()
    }

    // Screen view.
    new Thread() {
        override def run(): Unit = U.gaScreenView("server")
    }
        .start()

    NCIgniteRunner.runWith(
        args.find(_.startsWith("-igniteConfig=")) match {
            case None ⇒ null // Will use default on the classpath 'ignite.xml'.
            case Some(s) ⇒ s.substring(s.indexOf("=") + 1)
        },
        start()
    )
}
