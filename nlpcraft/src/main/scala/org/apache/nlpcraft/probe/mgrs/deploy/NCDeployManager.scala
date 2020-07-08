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

package org.apache.nlpcraft.probe.mgrs.deploy

import java.io._
import java.util.jar.{JarInputStream ⇒ JIS}

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.factories.basic.NCBasicModelFactory
import org.apache.nlpcraft.model.impl.NCModelImpl
import org.apache.nlpcraft.model.intent.impl.{NCIntentScanner, NCIntentSolver}
import resource.managed

import scala.collection.JavaConverters._
import scala.collection.convert.DecorateAsScala
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Exception._

/**
  * Model deployment manager.
  */
object NCDeployManager extends NCService with DecorateAsScala {
    @volatile private var models: ArrayBuffer[NCModel] = _
    @volatile private var modelFactory: NCModelFactory = _
    
    private final val ID_REGEX = "^[_a-zA-Z]+[a-zA-Z0-9:-_]*$"

    object Config extends NCConfigurable {
        private final val pre = "nlpcraft.probe"

        // It should reload config.
        def modelFactoryType: Option[String] = getStringOpt(s"$pre.modelFactory.type")
        def modelFactoryProps: Option[Map[String, String]] = getMapOpt(s"$pre.modelFactory.properties")
        def models: Seq[String] = getStringList(s"$pre.models")
        def jarsFolder: Option[String] = getStringOpt(s"$pre.jarsFolder")
    }

    /**
      * Gives a list of JAR files at given path.
      * 
      * @param path Path to scan.
      * @return
      */
    private def scanJars(path: File): Seq[File] = {
        val jars = path.listFiles(new FileFilter {
            override def accept(f: File): Boolean =
                f.isFile && f.getName.toLowerCase.endsWith(".jar")
        })

        if (jars == null) Seq.empty else jars.toSeq
    }
    
    /**
      *
      * @param mdl
      * @return
      */
    @throws[NCE]
    private def wrap(mdl: NCModel): NCModel = {
        // Scan for intent annotations in the model class.
        val intents = NCIntentScanner.scan(mdl)

        if (intents.nonEmpty) {
            // Check the uniqueness of intent IDs.
            U.getDups(intents.keys.toSeq.map(_.id)) match {
                case ids if ids.nonEmpty ⇒ throw new NCE(s"Duplicate intent IDs found for '${mdl.getId}' model: ${ids.mkString(",")}")
                case _ ⇒ ()
            }
    
            logger.info(s"Intents found in the model: ${mdl.getId}")

            val solver = new NCIntentSolver(
                intents.toList.map(x ⇒ (x._1, (z: NCIntentMatch) ⇒ x._2.apply(z)))
            )
    
            new NCModelImpl(mdl, solver)
        }
        else {
            logger.warn(s"Model has no intents: ${mdl.getId}")
    
            new NCModelImpl(mdl, null)
        }
    }
    
    /**
      *
      * @param clsName Factory class name.
      */
    @throws[NCE]
    private def makeModelFactory(clsName: String): NCModelFactory =
        catching(classOf[Throwable]) either Thread.currentThread().getContextClassLoader.
            loadClass(clsName).
            getDeclaredConstructor().
            newInstance().
            asInstanceOf[NCModelFactory]
        match {
            case Left(e) ⇒ throw new NCE(s"Failed to instantiate model factory: $clsName", e)
            case Right(factory) ⇒ factory
        }

    /**
      *
      * @param clsName Model class name.
      */
    @throws[NCE]
    private def makeModel(clsName: String): NCModel =
        try
            wrap(
                makeModelFromSource(
                    Thread.currentThread().getContextClassLoader.loadClass(clsName).asSubclass(classOf[NCModel]),
                    clsName
                )
            )
        catch {
            case e: Throwable ⇒ throw new NCE(s"Failed to instantiate model: $clsName", e)
        }

    /**
      * 
      * @param cls Model class.
      * @param src Model class source.
      */
    @throws[NCE]
    private def makeModelFromSource(cls: Class[_ <: NCModel], src: String): NCModel =
        catching(classOf[Throwable]) either modelFactory.mkModel(cls) match {
            case Left(e) ⇒
                throw new NCE(s"Failed to instantiate model [" +
                    s"class=${cls.getName}, " +
                    s"factory=${modelFactory.getClass.getName}, " +
                    s"source=$src" +
                "]", e)

            case Right(model) ⇒ model
        }
    
    /**
      * 
      * @param jarFile JAR file to extract from.
      */
    @throws[NCE]
    private def extractModels(jarFile: File): Seq[NCModel] = {
        val clsLdr = Thread.currentThread().getContextClassLoader
        
        val classes = mutable.ArrayBuffer.empty[Class[_ <: NCModel]]

        managed(new JIS(new BufferedInputStream(new FileInputStream(jarFile)))) acquireAndGet { in ⇒
            var entry = in.getNextJarEntry

            while (entry != null) {
                if (!entry.isDirectory && entry.getName.endsWith(".class")) {
                    val clsName = entry.getName.substring(0, entry.getName.length - 6).replace('/', '.')

                    try {
                        val cls = clsLdr.loadClass(clsName)

                        if (classOf[NCModel].isAssignableFrom(cls) && !cls.isInterface)
                            classes += cls.asSubclass(classOf[NCModel])
                    }
                    catch {
                        // Errors are possible for JARs like log4j etc, which have runtime dependencies.
                        // We don't need these messages in log beside trace, so ignore...
                        case _: ClassNotFoundException  ⇒ ()
                        case _: NoClassDefFoundError ⇒ ()
                    }
                }

                entry = in.getNextJarEntry
            }
        }
    
        classes.map(cls ⇒
            wrap(
                makeModelFromSource(cls, jarFile.getPath)
            )
        )
    }
    
    @throws[NCE]
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        modelFactory = new NCBasicModelFactory
        models = ArrayBuffer.empty[NCModel]

        // Initialize model factory (if configured).
        Config.modelFactoryType match {
            case Some(mft) ⇒
                modelFactory = makeModelFactory(mft)
    
                modelFactory.initialize(Config.modelFactoryProps.getOrElse(Map.empty[String, String]).asJava)
                
            case None ⇒ // No-op.
        }
        
        models ++= Config.models.map(makeModel)
        
        Config.jarsFolder match {
            case Some(jarsFolder) ⇒
                val jarsFile = new File(jarsFolder)
    
                if (!jarsFile.exists())
                    throw new NCE(s"JAR folder path '$jarsFolder' does not exist.")
                if (!jarsFile.isDirectory)
                    throw new NCE(s"JAR folder path '$jarsFolder' is not a directory.")
    
                val src = this.getClass.getProtectionDomain.getCodeSource
                val locJar = if (src == null) null else new File(src.getLocation.getPath)
    
                for (jar ← scanJars(jarsFile) if jar != locJar)
                    models ++= extractModels(jar)
                
            case None ⇒ // No-op.
        }

        // Verify models' identities.
        models.foreach(mdl ⇒ {
            val mdlName = mdl.getName
            val mdlId = mdl.getId
            val mdlVer = mdl.getVersion

            if (mdlId == null)
                throw new NCE(s"Model ID is not provided: $mdlName")
            if (mdlName == null)
                throw new NCE(s"Model name is not provided: $mdlId")
            if (mdlVer == null)
                throw new NCE(s"Model version is not provided: $mdlId")
            if (mdlName != null && mdlName.isEmpty)
                throw new NCE(s"Model name cannot be empty string: $mdlId")
            if (mdlId != null && mdlId.isEmpty)
                throw new NCE( s"Model ID cannot be empty string: $mdlId")
            if (mdlVer != null && mdlVer.length > 16)
                throw new NCE(s"Model version cannot be empty string: $mdlId")
            if (mdlName != null && mdlName.length > 64)
                throw new NCE(s"Model name is too long (64 max): $mdlId")
            if (mdlId != null && mdlId.length > 32)
                throw new NCE(s"Model ID is too long (32 max): $mdlId")
            if (mdlVer != null && mdlVer.length > 16)
                throw new NCE(s"Model version is too long (16 max): $mdlId")
            
            for (elm ← mdl.getElements.asScala)
                if (!elm.getId.matches(ID_REGEX))
                    throw new NCE(s"Model element ID '${elm.getId}' does not match '$ID_REGEX' regex in: $mdlId")
        })

        if (U.containsDups(models.map(_.getId).toList))
            throw new NCE("Duplicate model IDs detected.")
        
        super.start()
    }

    @throws[NCE]
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        if (modelFactory != null)
            modelFactory.terminate()    

        if (models != null)
            models.clear()
        
        super.stop()
    }

    /**
      *
      * @return
      */
    def getModels: Seq[NCModel] = models
}
