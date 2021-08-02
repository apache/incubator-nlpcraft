/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.model.tools.sqlgen.impl

import java.io.{File, FileOutputStream, IOException}
import java.sql.{Connection, DriverManager, ResultSet}
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime}
import java.util
import java.util.regex.{Pattern, PatternSyntaxException}
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.nlp.core.NCNlpPorterStemmer
import org.apache.nlpcraft.common.version.NCVersion
import org.apache.nlpcraft.model.impl.json.{NCElementJson, NCMacroJson, NCModelJson}
import org.apache.nlpcraft.model.tools.sqlgen.NCSqlJoinType
import org.apache.nlpcraft.common._

import scala.util.Using
import scala.collection.immutable.HashMap
import scala.collection.{Seq, mutable}
import scala.jdk.CollectionConverters.{SeqHasAsJava, SetHasAsJava, MapHasAsJava}
import scala.util.Try

/**
 * Scala-based SQL model engine.
 */
object NCSqlModelGeneratorImpl {
    case class Join(
        fromColumns: Seq[String],
        toTable: String,
        toColumns: Seq[String]
    )

    trait NamedEntity {
        val nameLc: String
        val elmNameLc: String

        private lazy val nameWs = U.normalize(elmNameLc.replaceAll("_"," ")," ")

        lazy val synonym: String =
            if (elmNameLc == nameWs)
                substituteMacros(elmNameLc)
            else
                s"{${substituteMacros(elmNameLc)}|${substituteMacros(removeSeqDups(nameWs))}}"
    }
    
    // https://docs.oracle.com/javase/8/docs/api/java/sql/DatabaseMetaData.html#getColumns-java.lang.String-java.lang.String-java.lang.String-java.lang.String-
    case class Column(
        name: String,
        elmName: String, // Name with optionally removed prefix and suffix.
        dataType: Int,
        isNullable: String,
        isPk: Boolean
    ) extends NamedEntity {
        val nameLc: String = name.toLowerCase()
        val elmNameLc: String = elmName.toLowerCase()
        lazy val isNull: Boolean = isNullable == "YES"
    }

    case class Table(
        name: String,
        elmName: String, // Name with optionally removed prefix and suffix.
        joins: Seq[Join],
        columns: mutable.ArrayBuffer[Column] = mutable.ArrayBuffer.empty[Column]
    ) extends NamedEntity {
        val nameLc: String = name.toLowerCase()
        val elmNameLc: String = elmName.toLowerCase()
    }
    
    case class ParametersHolder(
        var cmdLine: String = null,
        var url: String = null,
        var driver: String = null,
        var user: String = null,
        var password: String = null,
        var output: String = null,
        var schema: String  = null,
        var modelId: String  = "sql.model.id",
        var modelVer: String  = s"1.0.0-${Instant.now.getEpochSecond}",
        var modelName: String  = "SQL-based model.",
        var synonyms: Boolean = true,
        var parent: Boolean = false,
        var preSpec: String = "",
        var sufSpec: String = "",
        var inclSpec: String = "",
        var exclSpec: String = "",
        var inclPred: (String, String) => Boolean = (_, _) => true,
        var exclPred: (String, String) => Boolean = (_, _) => false,
        var preFun: String => String = s => s,
        var sufFun: String => String = s => s,
        var overRide: Boolean = false // 'override' is a reserved keyword.
    ) {
        lazy val isJson: Boolean = {
            require(output != null)

            val lc = output.toLowerCase()

            lc.endsWith(".js") || lc.endsWith(".json")
        }
    }
    
    final private val ver = NCVersion.getCurrent
    
    /**
     *
     * @param s
     * @return
     */
    def substituteMacros(s: String): String =
        s.split(" ").filter(_.nonEmpty).map(w => {
            if (w == "id")
                "<ID>"
            else
                w
        }).mkString(" ")
    
    /**
     * Note that it only removed one, first found, prefix.
     *
     * @param s
     * @return
     */
    private def mkPrefixFun(s: String): String => String = {
        val arr = U.splitTrimFilter(s, ",")
        
        z => (for (fix <- arr if z.startsWith(fix)) yield z.substring(fix.length)).headOption.getOrElse(z)
    }
    
    /**
     * Note that it only removed one, first found, prefix.
     *
     * @param s
     * @return
     */
    private def mkSuffixFun(s: String): String => String = {
        val arr = U.splitTrimFilter(s, ",")
        
        z => (for (fix <- arr if z.endsWith(fix)) yield z.substring(0, z.length - fix.length)).headOption.getOrElse(z)
    }
    
    /**
      *
      * @param s Semicolon-separate list of include/exclude expressions.
      * @return
      */
    private def mkPredicate(s: String): (String, String) => Boolean = {
        def convert(expr: String): (String, String) => Boolean = {
            val s = U.splitTrimFilter(expr, "#")

            val (tbl: String, col: String) = s.length match {
                case 1 if !expr.contains("#") => (s.head, "")  // 'table'
                case 1 if expr.contains("#") => ("", s.head)   // '#column'
                case 2 => (s.head, s(1))                       // 'table#column'
                case _ => throw new Exception(s"Invalid table and/or column filter: $C$expr$RST")
            }

            val (tblRx, colRx) = try {
                (
                    if (tbl != "") Pattern.compile(tbl) else null,
                    if (col != "") Pattern.compile(col) else null
                )
            }
            catch {
                case e: PatternSyntaxException => throw new Exception(s"Invalid regular expression: $C${e.getMessage}$RST")
            }

            (t: String, c: String) => {
                require(t != null)

                def check(s: String, rx: Pattern): Boolean = {
                    if (rx == null)
                        true
                    else if (s == null)
                        false
                    else
                        rx.matcher(s).matches()
                }

                check(t, tblRx) && check(c, colRx)
            }
        }

        val predicates = U.splitTrimFilter(s,";").map(convert)

        (tbl: String, col: String) => predicates.exists(_(tbl, col))
    }

    /**
      *
      * @param meta
      * @param key
      * @param v
      */
    private def add(meta: JavaMeta, key: String, v: Any): Unit = {
        val obj = v.asInstanceOf[AnyRef]

        if (obj != null) {
            val ok = obj match {
                case x: String => x.nonEmpty
                case _ => true
            }

            if (ok)
                meta.put(key, obj)
        }
    }

    /**
     * Removes sequential duplicate words from given string.
     *
     * @param syn Synonym to de-dup.
     * @return
     */
    private def removeSeqDups(syn: String): String = {
        val words = U.splitTrimFilter(syn, " ")
        
        words
            .zip(words.map(NCNlpPorterStemmer.stem))
            .foldRight[List[(String, String)]](Nil)((pair, list) => list.headOption match {
                case Some(head) if head._2 == pair._2 => list // Skip duplicate 'w' stems.
                case _ => pair :: list
            }).map(_._1).mkString(" ")
    }
    
    /**
      *
      * @param tables
      * @param params
      */
    private def generateModel(tables: Seq[Table], params: ParametersHolder): Unit = {
        val elems = mutable.ArrayBuffer.empty[NCElementJson]
        
        for (tbl <- tables) {
            val e = new NCElementJson

            e.setId(s"tbl:${tbl.nameLc}")
            e.setDescription(s"Auto-generated from '${tbl.nameLc}' table.")
            e.setGroups(Array("table"))

            val meta = new util.LinkedHashMap[String, Object]()

            add(meta, "sql:name", tbl.nameLc)
            add(meta, "sql:defaultselect", tbl.columns.take(3).map(_.nameLc).asJava) // First 3 columns by default.
            add(meta, "sql:defaultsort", tbl.columns.filter(_.isPk).map(col => s"${tbl.nameLc}.${col.nameLc}#desc").asJava)
            add(meta, "sql:extratables", tbl.joins.map(_.toTable.toLowerCase).toSet.asJava)

            e.setMetadata(meta)

            if (params.synonyms)
                e.setSynonyms(Array(tbl.synonym).distinct)

            elems += e
        }

        val tablesMap = tables.map(t => t.name -> t).toMap

        for (tbl <- tables; col <- tbl.columns) {
            val e = new NCElementJson

            e.setId(s"col:${tbl.nameLc}_${col.nameLc}")
            e.setDescription(s"Auto-generated from '${tbl.nameLc}.${col.nameLc}' column.")
            e.setGroups(Array("column"))

            if (params.parent)
                e.setParentId(tbl.nameLc)

            val meta = new util.LinkedHashMap[String, Object]()

            add(meta, "sql:name", col.nameLc)
            add(meta, "sql:tablename", tbl.nameLc)
            add(meta, "sql:datatype", col.dataType)
            add(meta, "sql:isnullable", col.isNull)
            add(meta, "sql:ispk", col.isPk)

            e.setMetadata(meta)

            if (params.synonyms) {
                e.setSynonyms(Array(
                    col.synonym,
                    s"${tbl.synonym} ${col.synonym}",
                    s"${col.synonym} <OF> ${tbl.synonym}"
                ).distinct)
            }

            elems += e
        }

        val mdl = new NCModelJson()

        mdl.setId(params.modelId)
        mdl.setName(params.modelName)
        mdl.setVersion(params.modelVer)
        mdl.setDescription(s"SQL-based model auto-generated by NCSqlModelGenerator utility.")

        mdl.setMacros(Array(
            new NCMacroJson("<OF>", "{of|for|per}"),
            new NCMacroJson("<ID>", "{unique|_} {id|identifier}")
        ))
        mdl.setAdditionalStopWords(Array())
        mdl.setSuspiciousWords(Array())
        mdl.setEnabledBuiltInTokens(Array())
        mdl.setIntents(Array())

        mdl.setMetadata(
            HashMap[String, AnyRef](
                "sql:timestamp" -> s"${Instant.now}",
                "sql:url" -> params.url,
                "sql:driver" -> params.driver,
                "sql:user" -> params.user,
                "sql:output" -> params.output,
                "sql:schema" -> params.schema,
                "sql:cmdline" -> params.cmdLine,
                "sql:joins" ->
                    tables.flatMap(t => t.joins.map(j => {
                        val fromTable = t.name.toLowerCase
                        val toTable = j.toTable.toLowerCase
                        val fromCols = j.fromColumns.map(_.toLowerCase)
                        val toCols = j.toColumns.map(_.toLowerCase)

                        def mkNullables(t: String, cols: Seq[String]): Seq[Boolean] = {
                            val tabCols = tablesMap(t).columns

                            cols.map(col => tabCols.find(_.name == col).get.isNull)
                        }

                        val fromColsNulls = mkNullables(fromTable, fromCols)
                        val toColsNulls =mkNullables(toTable, toCols)

                        def forall(seq: Seq[Boolean], v: Boolean): Boolean = seq.forall(_ == v)

                        val typ =
                            if (forall(fromColsNulls, v = true) && forall(toColsNulls, v = false))
                                NCSqlJoinType.LEFT
                            else if (forall(fromColsNulls, v = false) && forall(toColsNulls, v = true))
                                NCSqlJoinType.RIGHT
                            else
                                // Default value.
                                NCSqlJoinType.INNER

                        val m = new util.LinkedHashMap[String, Object]()

                        m.put("fromtable", fromTable)
                        m.put("fromcolumns", fromCols.asJava)
                        m.put("totable", toTable)
                        m.put("tocolumns", toCols.asJava)
                        m.put("jointype", typ.toString.toLowerCase)

                        m
                    })).asJava
            ).asJava
        )
        
        mdl.setElements(elems.toArray)

        val mapper =
            if (params.isJson)
                new ObjectMapper()
            else
                new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER))

        mapper.setSerializationInclusion(Include.NON_NULL)
        mapper.setSerializationInclusion(Include.NON_EMPTY)

        var file = new File(params.output)

        if (file.exists()) {
            if (!params.overRide) {
                val fileName = file.getName
    
                val name = fileName.takeWhile(_ != '.')
                val ext = fileName.drop(name.length + 1)
                val unique = DateTimeFormatter.ofPattern("MMddA").format(LocalDateTime.now())
    
                file = new File(file.getParent, s"${name}_$unique.$ext")
                
                println(
                    s"Output file already exist and override is disabled ($C-z=false$RST):\n" +
                    s"  $G+--$RST using $C'${file.getName}'$RST filename instead."
                )
            }
            else
                println(
                    s"Existing file '${file.getName}' will be overridden:\n" +
                    s"  $G+--$RST use $C'-z=false'$RST to disable override."
                )
        }

        try {
            Using.resource(new FileOutputStream(file)) { stream =>
                mapper.writerWithDefaultPrettyPrinter().writeValue(stream, mdl)

                stream.flush()
            }
        }
        catch {
            case e: IOException => errorExit(s"Failed to write output file: $C'${file.getAbsolutePath}'$RST", e)
        }

        val tbl = NCAsciiTable()

        tbl += ("JDBC URL", params.url)
        tbl += ("JDBC driver", params.driver)
        tbl += ("SQL schema", params.schema)
        tbl += ("SQL user", if (params.user == null) "" else params.user)
        tbl += ("SQL user password", if (params.password == null) "" else params.password.replaceAll(".", "*"))
        tbl += ("Generate synonyms", params.synonyms)
        tbl += ("Override output", params.overRide)
        tbl += ("Use parent ID", params.parent)
        tbl += ("Includes", params.inclSpec)
        tbl += ("Excludes", params.exclSpec)
        tbl += ("Prefix to remove", params.preSpec)
        tbl += ("Suffix to remove", params.sufSpec)
        tbl += ("Model ID", params.modelId)
        tbl += ("Model name", params.modelName)
        tbl += ("Model version", params.modelVer)
        tbl += ("Tables scanned", tables.size)
        tbl += ("Columns scanned", tables.flatMap(_.columns).size)
        tbl += ("Model elements", elems.size)

        println(s"Model generated: $G${file.getAbsolutePath}$RST")

        println(tbl)

        if (params.synonyms)
            println("NOTE: review auto-generated synonyms for validness.")
    }

    /**
      * @param rs
      * @param col
      * @return
      */
    //noinspection SameParameterValue
    private def getString(rs: ResultSet, col: String): String =
        Try(rs.getString(col)).getOrElse("No data")

    /**
      * @param rs
      * @param col
      * @return
      */
    //noinspection SameParameterValue
    private def getInt(rs: ResultSet, col: String): Int =
        Try(rs.getInt(col)).getOrElse(-1)

    /**
      *
      * @param params
      * @return
      */
    private def readSqlMetadata(params: ParametersHolder): Seq[Table] = {
        val tables = mutable.HashMap.empty[String, Table]

        try {
            Class.forName(params.driver)

            def getConnection: Connection =
                if (params.user == null)
                    DriverManager.getConnection(params.url)
                else
                    DriverManager.getConnection(params.url, params.user, params.password)

            val pks = mutable.HashSet.empty[String]

            Using.resource { getConnection } { conn =>
                val md = conn.getMetaData

                Using.resource {
                    md.getColumns(null, params.schema, null, null)
                } { rs =>
                    while (rs.next()) {
                        val schNameOrigin = rs.getString("TABLE_SCHEM")
                        val tblNameOrigin = rs.getString("TABLE_NAME")
                        val colNameOrigin = rs.getString("COLUMN_NAME")
    
                        val schName = schNameOrigin.toLowerCase
                        val tblName = tblNameOrigin.toLowerCase
                        val colName = colNameOrigin.toLowerCase

                        val key = s"$schName.$tblName"

                        /**
                         *
                         * @param tbl
                         * @param col
                         * @return
                         */
                        def isAllowed(tbl: String, col: String = null): Boolean =
                            params.inclPred.apply(tbl, col) && !params.exclPred.apply(tbl, col)

                        val tbl: Table = tables.get(key) match {
                            case Some(t) => t
                            case None if isAllowed(tblName) =>
                                pks.clear()
                              
                                Using.resource { md.getPrimaryKeys(null, schNameOrigin, tblNameOrigin) } { rs =>
                                    while (rs.next())
                                        pks += rs.getString("COLUMN_NAME").toLowerCase
                                }

                                case class Fk(
                                    name: String,
                                    fromTableSchema: String,
                                    fromTable: String,
                                    fromColumn: String,
                                    toTableSchema: String,
                                    toTable: String,
                                    toColumn: String
                                )

                                val fks = mutable.ArrayBuffer.empty[Fk]

                                Using.resource { md.getImportedKeys(null, schNameOrigin, tblNameOrigin) } { rs =>
                                    while (rs.next())
                                        fks += Fk(
                                            name = rs.getString("FK_NAME"),
                                            fromTableSchema = rs.getString("FKTABLE_SCHEM"),
                                            fromTable = rs.getString("FKTABLE_NAME"),
                                            fromColumn = rs.getString("FKCOLUMN_NAME"),
                                            toTableSchema = rs.getString("PKTABLE_SCHEM"),
                                            toTable = rs.getString("PKTABLE_NAME"),
                                            toColumn = rs.getString("PKCOLUMN_NAME")
                                        )
                                }

                                val joins =
                                    fks.filter(fk =>
                                        fk.fromTableSchema == null && fk.toTableSchema == null ||
                                        fk.fromTableSchema == schNameOrigin && fk.toTableSchema == schNameOrigin
                                    ).groupBy(_.name).map { case (_, fksGrp) =>
                                        Join(
                                            fromColumns = fksGrp.map(_.fromColumn),
                                            toTable = fksGrp.head.toTable,
                                            toColumns = fksGrp.map(_.toColumn)
                                        )
                                    }

                                val t = Table(
                                    tblName,
                                    params.preFun(params.sufFun(tblName)),
                                    joins.toSeq
                                )

                                tables += key -> t

                                t
                            case _ => null
                        }
                        
                        if (tbl != null && isAllowed(tblName, colName))
                            tbl.columns +=
                                Column(
                                    name = colName,
                                    params.preFun(params.sufFun(colName)),
                                    dataType = getInt(rs, "DATA_TYPE"),
                                    isNullable = getString(rs, "IS_NULLABLE"),
                                    isPk = pks.contains(colName)
                                )
                    }
                }
            }
        }
        catch {
            case e: ClassNotFoundException => errorExit(s"Unknown JDBC driver class: $C${params.driver}$RST", e)
            case e: Exception => errorExit(s"Failed to generate model for: $C'${params.url}'$RST", e)
        }
        
        tables.values.toSeq.filter(_.columns.nonEmpty)
    }

    /**
     *
      */
    private def help(): Unit =
        System.out.println(
            s"""
               |${bo("NAME:")}
               |    ${c("NCSqlModelGenerator")} -- NLPCraft model generator from SQL databases.
               |
               |${bo("SYNOPSIS:")}
               |    java -cp apache-nlpcraft-incubating-${ver.version}-all-deps.jar org.apache.nlpcraft.model.tools.sqlgen.NCSqlModelGenerator [PARAMETERS]
               |
               |${bo("DESCRIPTION:")}
               |    This utility generates NLPCraft model stub from a given SQL database schema. You
               |    can choose database schema, set of tables and columns for which you
               |    want to generate NLPCraft model. After the model is generated you can
               |    further configure and customize it for your specific needs.
               |
               |    This Java class can be run from the command line or from an IDE like any other
               |    Java application. Note that required JDBC driver class must be available on the
               |    classpath and therefore its JAR should be added to the classpath when running
               |    this application.
               |
               |${bo("PARAMETERS:")}
               |    ${c("--url|-r")}=${g("url")}
               |        Mandatory database JDBC URL.
               |
               |    ${c("--driver|-d")}=${g("class")}
               |        Mandatory JDBC driver class. Note that 'class' must be a
               |        fully qualified class name. It should also be available on
               |        the classpath.
               |
               |    ${c("--schema|-s")}=${g("schema")}
               |        Mandatory database schema to scan.
               |
               |    ${c("--out|-o")}=${g("filename")}
               |        Mandatory name of the output JSON or YAML model file. It should
               |        have one of the following extensions: .js, .json, .yml, or .yaml
               |        File extension determines the output file format.
               |
               |    ${c("--user|-u")}=${g("username")}
               |        Optional database user name.
               |
               |    ${c("--password|-w")}=${g("password")}
               |        Optional database user password.
               |
               |    ${c("--mdlId|-m")}=${g("id")}
               |        Optional generated model ID. By default, the model ID will be 'sql.model.id'.
               |
               |    ${c("--mdlVer|-v")}=${g("version")}
               |        Optional generated model version. By default, the model version will be '1.0.0-timestamp'.
               |
               |    ${c("--mdlName|-n")}=${g("name")}
               |        Optional generated model name. By default, the model name will be 'SQL-based model'.
               |
               |    ${c("--exclude|-e")}=${g("list")}
               |        Optional semicolon-separate list of tables and/or columns to exclude. By
               |        default, none of the tables and columns in the schema are excluded. See below
               |        for more information.
               |
               |    ${c("--prefix|-f")}=${g("list")}
               |        Optional comma-separate list of table or column name prefixes to remove.
               |        These prefixes will be removed when name is used for model elements
               |        synonyms. By default, no prefixes will be removed.
               |
               |    ${c("--suffix|-q")}=${g("list")}
               |        Optional comma-separate list of table or column name suffixes to remove.
               |        These suffixes will be removed when name is used for model elements
               |        synonyms. By default, no suffixes will be removed.
               |
               |    ${c("--include|-i")}=${g("list")}
               |        Optional semicolon-separate list of tables and/or columns to include. By
               |        default, all tables and columns in the schema are included. See below
               |        for more information.
               |
               |    ${c("--synonyms|-y")}=${g("true|false")}
               |        Optional flag on whether to generated auto synonyms for the model elements.
               |        Default is true.
               |
               |    ${c("--override|-z")}=${g("true|false")}
               |        Optional flag to determine whether to override output file if it already exist.
               |        If override is disabled (default) and output file exists - a unique file name will
               |        be used instead.
               |        Default is false.
               |
               |    ${c("--parent|-p")}=${g("true|false")}
               |        Optional flag on whether to use element's parent relationship for
               |        defining SQL columns and their containing (i.e. parent) tables.
               |        Default is false.
               |
               |    ${c("--help|-h|-?")}
               |        Prints this usage information.
               |
               |${bo("DETAILS:")}
               |    ${c("-r")}, ${c("-d")}, ${c("-s")}, and ${c("-o")} are mandatory parameters, everything else is optional.
               |
               |    Parameter values can be placed in double (") or single (') quotes which will be
               |    automatically discarded. Use it to pass strings containing spaces in the command line.
               |
               |    Each -i or -e parameter is a semicolon (;) separated  list of table or columns names.
               |    Each table or column name can be one of following forms:
               |      - ${g("table")}         -- to filter on table names only.
               |      - ${g("table#column")}  -- to filter on both table and column names.
               |      - ${g("#column")}       -- to filter on columns only (regardless of the table).
               |
               |    Table and column names are treated as standard Java regular expressions. Note that
               |    both '#' and ';' cannot be used inside of the regular expression:
               |
               |    ${c("-e")}="${g("#_.+")}"             -- excludes any columns starting with '_'.
               |    ${c("-e")}="${g("tmp.+")}"            -- excludes all tables starting with 'tmp'.
               |    ${c("-i")}="${g("Order.*;#[^_].+")}"  -- includes only tables starting with 'Order' and columns that
               |                             do not start with '_'.
               |
               |${bo("EXAMPLES:")}
               |    java -cp apache-nlpcraft-incubating-${ver.version}-all-deps.jar org.apache.nlpcraft.model.tools.sqlgen.NCSqlModelGenerator
               |        ${c("-r")}=jdbc:postgresql://localhost:5432/mydb
               |        ${c("-d")}=org.postgresql.Driver
               |        ${c("-f")}="tbl_, col_"
               |        ${c("-q")}="_tmp, _old, _unused"
               |        ${c("-s")}=public
               |        ${c("-e")}="#_.+"
               |        ${c("-o")}=model.json
                """.stripMargin
        )
    
    /**
     *
     * @param msg Error message.
     * @param e Cause exception.
     */
    private def errorExit(msg: String, e: Throwable): Unit = {
        U.prettyError(s"${R}X:$RST $msg", e)

        throw new Exception(msg, e)
    }
    
    /**
     *
     * @param v
     * @param name
     */
    private def mandatoryParam(v: String, name: String): Unit =
        if (v == null)
            throw new IllegalArgumentException(s"Missing mandatory parameter: $C$name$RST")
    
    /**
     *
     * @param v
     * @param name
     * @return
     */
    private def parseBoolean(v: String, name: String): Boolean =
        v.toLowerCase match {
            case "true" => true
            case "false" => false
            case _ => throw new IllegalArgumentException(s"Invalid boolean value: $C$name=$v$RST")
        }
        
    /**
     *
     * @param cmdArgs
     * @return
     */
    private def parseCmdParameters(cmdArgs: Array[String]): ParametersHolder = {
        val params = ParametersHolder()
        
        var i = 0
        
        try {
            while (i < cmdArgs.length) {
                val arg = cmdArgs(i)
                val eq = arg.indexOf('=')

                if (eq == -1)
                    throw new IllegalArgumentException(s"Invalid argument: $C$arg$RST")

                val k: String = arg.substring(0, eq)
                val v: String = U.trimQuotes(arg.substring(eq + 1))

                k match {
                    case "--url" | "-r" => params.url = v
                    case "--driver" | "-d" => params.driver = v
                    case "--user" | "-u" => params.user = v
                    case "--password" | "-w" => params.password = v
                    case "--schema" | "-s" => params.schema = v
                    case "--mdlId" | "-m" => params.modelId = v
                    case "--mdlName" | "-n" => params.modelName = v
                    case "--mdlVer" | "-v" => params.modelVer = v
                    case "--out" | "-o" => params.output = v
                    case "--include" | "-i" => params.inclSpec = v; params.inclPred = mkPredicate(v)
                    case "--exclude" | "-e" => params.exclSpec = v; params.exclPred = mkPredicate(v)
                    case "--prefix" | "-f" => params.preSpec = v; params.preFun = mkPrefixFun(v)
                    case "--suffix" | "-q" => params.sufSpec = v; params.sufFun = mkSuffixFun(v)
                    case "--parent" | "-p" => params.parent = parseBoolean(v, k)
                    case "--synonyms" | "-y" => params.synonyms = parseBoolean(v, k)
                    case "--override" | "-z" => params.overRide = parseBoolean(v, k)

                    case _ => throw new IllegalArgumentException(s"Invalid argument: $C$arg$RST")
                }

                i += 1
            }
        
            mandatoryParam(params.url, "--url")
            mandatoryParam(params.driver, "--driver")
            mandatoryParam(params.output, "--out")
            mandatoryParam(params.schema, "--schema")
        
            val outLc = params.output.toLowerCase
        
            if (!outLc.endsWith(".json") &&
                !outLc.endsWith(".js") &&
                !outLc.endsWith(".yaml") &&
                !outLc.endsWith(".yml"))
                throw new IllegalArgumentException(s"Unsupported output file extension in $C'${params.output}'$RST output.")
            
            params.cmdLine = cmdArgs.mkString(" ")
        }
        catch {
            case e: Exception => errorExit(e.getMessage, e)
        }
        
        params
    }

    /**
     *
     * @param args Command line arguments.
     */
    def process(args: Array[String]): Unit = {
        if (args.isEmpty || !args.intersect(Seq("--help", "-h", "-help", "--?", "-?", "/?", "/help")).isEmpty)
            help()
        else {
            val params = parseCmdParameters(args)
            val tbls = readSqlMetadata(params)

            generateModel(tbls, params)
        }
    }
}
