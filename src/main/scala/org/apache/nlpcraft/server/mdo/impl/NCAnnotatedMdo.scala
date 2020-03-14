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

package org.apache.nlpcraft.server.mdo.impl

import java.lang.reflect.{Constructor, Method}
import java.sql.ResultSet

import com.thoughtworks.paranamer.{AnnotationParanamer, BytecodeReadingParanamer, CachingParanamer}
import org.apache.nlpcraft.common.ascii.{NCAsciiLike, NCAsciiTable}
import org.apache.nlpcraft.common.crypto.NCCipher
import org.apache.nlpcraft.server.sql.NCSql.Implicits.RsParser
import org.apache.nlpcraft.server.json.{NCJson, NCJsonLike}
import org.apache.nlpcraft.common._

import scala.collection.mutable
import scala.language.existentials
import scala.reflect.runtime.universe._
import scala.reflect.runtime.{universe ⇒ ru}
import scala.util.control.Exception._

sealed case class NCMdoEntityAnnotationException(c: Class[_])
    extends NCE(s"Annotated MDO doesn't have 'NCMdoEntity' annotation: $c")
sealed case class NCMdoFieldAnnotationException(c: Class[_])
    extends NCE(s"Not all main constructor parameters have 'NCMdoField' annotation in: $c")
sealed case class NCMdoSqlNotSupportedException(c: Class[_])
    extends NCE(s"SQL is not supported in: $c")
sealed case class NCMdoJsonConverterException(msg: String, c: Class[_], mtdName: String)
    extends NCE(s"$msg [name=$mtdName, class=$c]")
sealed case class NCMdoMissingGetterException(a: NCMdoField, c: Class[_])
    extends NCE(s"Missing getter for 'NCMdoField' annotation [class=$c, annotation=$a]")
sealed case class NCMdoCheckException(msg: String) extends NCE(msg)

case class MdoParameter(
    ann: NCMdoField,
    jsonConverter: Option[Method],
    getterName: String
) {
    // Getter name or custom name.
    lazy val jsonName: String = if (ann.jsonName() == "") getterName else ann.jsonName
}

case class MdoEntity(
    ann: NCMdoEntity,
    tblName: String,
    params: Seq[MdoParameter],
    ctor: Constructor[_]
)

object NCAnnotatedMdo {
    private val nameFinder = new CachingParanamer(new AnnotationParanamer(new BytecodeReadingParanamer()))
    private val entities = mutable.HashMap.empty[Class[_], MdoEntity]

    // Java reflection mirror for current class loader.
    private val rtMir = ru.runtimeMirror(getClass.getClassLoader)

    /**
      * Scala-reflection based method invocation.
      *
      * @param obj Target object of invocation.
      * @param mtdName Method name.
      * @param params Optional arguments.
      */
    private def invoke(obj: AnyRef, mtdName: String, params: Any*): Any = {
        val objMir = rtMir.reflect(obj)
        val mtdSym = objMir.symbol.typeSignature.member(TermName(mtdName)).asMethod

        objMir.reflectMethod(mtdSym)(params:_ *)
    }

    /**
      * Scans ans caches class information.
      *
      * @param cls Class to scan.
      */
    @throws[NCE]
    private def scanAndGet(cls: Class[_]): MdoEntity = entities.synchronized {
        if (!entities.contains(cls)) {
            // Get runtime mirror for the class.
            val rtCls = rtMir.runtimeClass(rtMir.classSymbol(cls))

            val entAnn = rtCls.getAnnotation(classOf[NCMdoEntity])

            if (entAnn == null)
                throw NCMdoEntityAnnotationException(cls)

            val sql = entAnn.sql
            val tblName = entAnn.table()

            // Ctor with maximum number of parameters.
            val ctor = rtCls.getConstructors.maxBy(_.getParameterCount)
            // Methods with zero parameters.
            val getters = rtCls.getMethods.filter(_.getParameterCount == 0)
            // 'NCMdoField' annotated main ctor parameters.
            val ctorParams = ctor.getParameters.zip(nameFinder.lookupParameterNames(ctor, true)).
                filter(_._1.getAnnotation(classOf[NCMdoField]) != null)

            if (ctorParams.length != ctor.getParameterCount)
                throw NCMdoFieldAnnotationException(cls)

            val params = ctorParams.map { tup ⇒
                val (ctorParam, name) = tup
                val fldAnn = ctorParam.getAnnotation(classOf[NCMdoField])
                val getter = getters.find(_.getName == name) match {
                    case Some(g) ⇒ g
                    case None ⇒ throw NCMdoMissingGetterException(fldAnn, cls)
                }
                val jsonConverter = fldAnn.jsonConverter() match {
                    case "" ⇒ None
                    case mtdName ⇒
                        val zeroArgMtd = catching(classOf[NoSuchMethodException]) either
                            rtCls.getMethod(mtdName)
                        val oneArgMtd = catching(classOf[NoSuchMethodException]) either
                            rtCls.getMethod(mtdName, getter.getReturnType)

                        if (zeroArgMtd.isRight && oneArgMtd.isRight)
                            throw NCMdoJsonConverterException("Duplicate converter found", cls, mtdName)
                        else if(zeroArgMtd.isLeft && oneArgMtd.isLeft)
                            throw NCMdoJsonConverterException("No converter found", cls, mtdName)

                        Some(zeroArgMtd.right.getOrElse(oneArgMtd.right.get))
                }

                MdoParameter(fldAnn, jsonConverter, getter.getName)
            }.toSeq

            var uniqCols = Seq.empty[String]
            var uniqJsons = Seq.empty[String]
            var pkFound = false

            // Checks.
            for (param ← params) {
                if (sql && param.ann.sql()) {
                    val col = param.ann.column

                    if (col == "")
                        throw NCMdoCheckException(s"SQL column is not specified in: $param.ann")

                    if (uniqCols.contains(col))
                        throw NCMdoCheckException(s"Duplicate SQL column in: $param.ann")
                    else
                        uniqCols = uniqCols :+ col

                    if (param.ann.pk() && pkFound)
                        throw NCMdoCheckException(s"Duplicate SQL primary key in: $param.ann")
                    else
                        pkFound = true
                }

                if (uniqJsons.contains(param.jsonName))
                    throw NCMdoCheckException(s"Duplicate JSON name in: $param.ann")
                else
                    uniqJsons = uniqJsons :+ param.jsonName
            }

            entities.put(cls, MdoEntity(entAnn, tblName, params, ctor))
        }

        entities(cls)
    }

    /**
      * Auto-generated result set parser.
      */
    def mkRsParser[T <: NCAnnotatedMdo[T]](cls: Class[T]): RsParser[T] = { rs: ResultSet ⇒
        val entity = scanAndGet(cls)

        if (!entity.ann.sql)
            throw NCMdoSqlNotSupportedException(cls)

        val ctor = entity.ctor

        val args: Seq[Any] = entity.params.zip(ctor.getParameterTypes).map { t ⇒
            val (p, cls) = t
            val col = p.ann.column()

            cls match {
                // Special handling for options.
                case x if x == classOf[Option[_]] ⇒
                    val obj = rs.getObject(col)

                    if (rs.wasNull())
                        None
                    else
                        Some(obj)

                // Handle AnyVals manually to get proper values in case of `NULL`s.
                case x if x == classOf[Long] ⇒ rs.getLong(col)
                case x if x == classOf[Int] ⇒ rs.getInt(col)
                case x if x == classOf[Short] ⇒ rs.getShort(col)
                case x if x == classOf[Byte] ⇒ rs.getByte(col)
                case x if x == classOf[Float] ⇒ rs.getFloat(col)
                case x if x == classOf[Double] ⇒ rs.getDouble(col)
                case x if x == classOf[Boolean] ⇒ rs.getBoolean(col)
                case x if x == classOf[Array[Byte]] ⇒ rs.getBytes(col)

                // Bulk-handle AnyRefs.
                case _ ⇒ rs.getObject(col)
            }
        }

        // Shouldn't we use Scala-base reflection here as well?
        ctor.newInstance(args.asInstanceOf[Seq[Object]]: _*).asInstanceOf[T]
    }
}

/**
  * Mixin trait for MDOs that use 'NCMdoEntity' and 'NCMdoField' annotations.
  */
trait NCAnnotatedMdo[T <: NCAnnotatedMdo[T]] extends NCJsonLike with NCAsciiLike {
    import NCAnnotatedMdo._

    private val entity: MdoEntity = scanAndGet(getClass)

    /**
      *
      */
    private def checkSql(): Unit =
        if (!entity.ann.sql)
            throw NCMdoSqlNotSupportedException(getClass)
    
    // Build-in JSON converters for legacy handling of `null` dates.
    def utilDateConverter(d: java.util.Date): Long = if (d == null) 0 else d.getTime
    def sqlDateConverter(d: java.sql.Date): Long = if (d == null) 0 else d.getTime
    def sqlTimeConverter(d: java.sql.Time): Long = if (d == null) 0 else d.getTime
    def sqlTstampConverter(d: java.sql.Timestamp): Long = if (d == null) 0 else d.getTime
    def asDecryptedJson(json: String): NCJson = NCJson(NCCipher.decrypt(json))
    def asJson(s: String): NCJson = NCJson(s)
    def decrypt(s: String): String = NCCipher.decrypt(s)
    
    /**
      *
      * @return
      */
    def isSql: Boolean = {
        entity.ann.sql
    }
    
    /**
      *
      * @return
      */
    def tableName(): String = {
        checkSql()
        
        entity.tblName
    }
    
    /**
      *
      * @return
      */
    def insertSql(): String = {
        throw new AssertionError("Not implemented yet.")
    }
    
    /**
      *
      * @return
      */
    def updateSql(): String = {
        throw new AssertionError("Not implemented yet.")
    }
    
    /**
      *
      * @return
      */
    def insertUpdateData(): Seq[Any] = {
        throw new AssertionError("Not implemented yet.")
    }
    
    /**
      *
      * @return
      */
    override def toAscii: String = {
        entity.params.filter(_.ann.json).map { p ⇒
            val pVal = invoke(this, p.getterName)
        
            val v = p.jsonConverter match {
                case None ⇒ pVal
                case Some(f) ⇒ f.getParameterCount match {
                    case 0 ⇒ invoke(this, f.getName)
                    case 1 ⇒ invoke(this, f.getName, pVal.asInstanceOf[Object])
                    case _ ⇒ throw new AssertionError(s"Invalid JSON converter: $f")
                }
            }
        
            jsonValue(v) match {
                case Some(a) ⇒ p.jsonName → a
                case None ⇒ p.jsonName → ""
            }
        }.foldLeft(NCAsciiTable())((tbl, pair) ⇒ tbl += (pair._1, pair._2)).toString
    }
    
    /**
      *
      * @param v Value to convert to JSON.
      */
    private def jsonValue(v: Any): Option[String] =
        if (v == null)
            None
        else
            v match {
                case s: String ⇒ Some(s""""${U.escapeJson(s)}"""")
                case _: Unit ⇒ None
                case z: Boolean ⇒ Some(z.toString)
                case b: Byte ⇒ Some(b.toString)
                case c: Char ⇒ Some(s""""$c"""")
                case s: Short ⇒ Some(s.toString)
                case i: Int ⇒ Some(i.toString)
                case j: Long ⇒ Some(j.toString)
                case f: Float ⇒ Some(f.toString)
                case d: Double ⇒ Some(d.toString)
                case t: Traversable[_] ⇒ Some(s"[${t.filter(_ != null).flatMap(jsonValue).mkString(",")}]")
                case a: Array[_] ⇒ Some(s"[${a.filter(_ != null).flatMap(jsonValue).mkString(",")}]")
                case j: NCJson ⇒ Some(j.compact)
                case x: NCJsonLike ⇒ Some(x.toJson)
                case d: java.util.Date ⇒ Some(d.getTime.toString) // Special handling for dates.
                case Some(s) ⇒ jsonValue(s)
                case None ⇒ None // Skip 'None' values.
                case _ ⇒ Some(s""""${U.escapeJson(v.toString)}"""")
            }
    
    /**
      * Convert to JSON presentation.
      */
    override def toJson: NCJson = {
        val fields = entity.params.filter(_.ann.json).flatMap { p ⇒
            val pVal = invoke(this, p.getterName)
            
            val v = p.jsonConverter match {
                case None ⇒ pVal
                case Some(f) ⇒ f.getParameterCount match {
                    case 0 ⇒ invoke(this, f.getName)
                    case 1 ⇒ invoke(this, f.getName, pVal.asInstanceOf[Object])
                    case _ ⇒ throw new AssertionError(s"Invalid JSON converter: $f")
                }
            }
            
            jsonValue(v) match {
                case Some(a) ⇒ Some(s""""${p.jsonName}": $a""")
                case None ⇒ None
            }
        }
        
        NCJson(s"{${fields.mkString(", ")}}")
    }
}

