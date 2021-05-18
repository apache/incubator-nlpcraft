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

package org.apache.nlpcraft.common.ascii

import java.io.{IOException, PrintStream}
import com.typesafe.scalalogging.Logger
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ascii.NCAsciiTable._
import org.apache.nlpcraft.common.ansi.NCAnsi._

import scala.collection.mutable
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.Using

/**
 * `ASCII`-based table with minimal styling support.
 */
class NCAsciiTable {
    /**
     * Cell style.
     */
    private sealed case class Style(
        var leftPad: Int = 1, // >= 0
        var rightPad: Int = 1, // >= 0
        var maxWidth: Int = Int.MaxValue, // > 0
        var align: String = "center" // center, left, right
    ) {
        /** Gets overall padding (left + right). */
        def padding: Int = leftPad + rightPad
    }

    /**
     * Cell style.
     */
    private object Style {
        /**
         *
         * @param sty Style text.
         */
        def apply(sty: String): Style = {
            val cs = new Style

            if (sty.nonEmpty) {
                for (e <- sty.split(',')) {
                    val a = e.split(":")

                    require(a.length == 2, s"Invalid cell style: ${e.trim}")

                    val a0 = a(0).trim
                    val a1 = a(1).trim

                    a0 match {
                        case "leftPad" => cs.leftPad = a1.toInt
                        case "rightPad" => cs.rightPad = a1.toInt
                        case "maxWidth" => cs.maxWidth = a1.toInt
                        case "align" => cs.align = a1.toLowerCase
                        case _ => assert(assertion = false, s"Invalid style: ${e.trim}")
                    }
                }
            }

            require(cs.leftPad >= 0, "Style 'leftPad' must >= 0.")
            require(cs.rightPad >= 0, "Style 'rightPad' must >= 0.")
            require(cs.maxWidth > 0, "Style 'maxWidth' must > 0.")
            require(cs.align == "center" || cs.align == "left" || cs.align == "right", "Style 'align' must be 'left', 'right' or 'center'.")

            cs
        }
    }

    /**
     * Cell holder.
     *
     * @param style
     * @param lines Lines that are already cut up per `style`, if required.
     */
    private sealed case class Cell(style: Style, lines: Seq[String]) {
        // Cell's calculated width including padding.
        lazy val width: Int = style.padding + (if (height > 0) lines.map(U.stripAnsi(_).length).max else 0)

        // Gets height of the cell.
        lazy val height: Int = lines.length
    }

    /**
     * Margin holder.
     */
    private sealed case class Margin(
        top: Int = 0,
        right: Int = 0,
        bottom: Int = 0,
        left: Int = 0
    )

    // Table drawing symbols.
    private val HDR_HOR = c("=")
    private val HDR_VER = c("|")
    private val HDR_CRS = c("+")
    private val ROW_HOR = c("-")
    private val ROW_VER = c("|")
    private val ROW_CRS = c("+")

    // Headers & rows.
    private var hdr = IndexedSeq.empty[Cell]
    private var rows = IndexedSeq.empty[IndexedSeq[Cell]]

    // Current row, if any.
    private var curRow: IndexedSeq[Cell] = _

    // Table's margin, if any.
    private var margin = Margin()

    /**
     * Global flag indicating whether or not to draw inside horizontal lines
     * between individual rows.
     */
    var insideBorder = false

    /**
     * Global Flag indicating whether of not to automatically draw horizontal lines
     * for multiline rows.
     */
    var autoBorder = true

    /**
     * If lines exceeds the style's maximum width it will be broken up
     * either by nearest space (by whole words) or mid-word.
     */
    var breakUpByWords = true

    /** Default row style. */
    var defaultRowStyle: String = DFLT_ROW_STYLE

    /** Default header style. */
    var defaultHeaderStyle: String = DFLT_HEADER_STYLE

    // Dash drawing.
    private def dash(ch: String, len: Int): String = ch * len

    private def space(len: Int): String = " " * len

    /**
     * Sets table's margin.
     *
     * @param top    Top margin.
     * @param right  Right margin.
     * @param bottom Bottom margin.
     * @param left   Left margin.
     */
    def margin(top: Int = 0, right: Int = 0, bottom: Int = 0, left: Int = 0): NCAsciiTable = {
        margin = Margin(top, right, bottom, left)

        this
    }

    /**
     * Starts data row.
     */
    def startRow(): Unit = {
        curRow = IndexedSeq.empty[Cell]
    }

    /**
     * Ends data row.
     */
    def endRow(): Unit = {
        rows :+= curRow

        curRow = null
    }

    /**
     * Adds row (one or more row cells).
     *
     * @param cells Row cells. For multi-line cells - use `Seq(...)`.
     */
    def +=(cells: Any*): NCAsciiTable = {
        startRow()

        cells foreach {
            case i: Iterable[_] => addRowCell(i.iterator.toSeq: _*)
            case a => addRowCell(a)
        }

        endRow()

        this
    }

    /**
     * Adds row (one or more row cells) with a given style.
     *
     * @param cells Row cells tuples (style, text). For multi-line cells - use `Seq(...)`.
     */
    def +/(cells: (String, Any)*): NCAsciiTable = {
        startRow()

        cells foreach {
            case i if i._2.isInstanceOf[Iterable[_]] =>
                addStyledRowCell(i._1, i._2.asInstanceOf[Iterable[_]].iterator.toSeq: _*)
            case a =>
                addStyledRowCell(a._1, a._2)
        }

        endRow()

        this
    }

    /**
     * Adds row.
     *
     * @param cells Row cells.
     */
    def addRow(cells: java.util.List[Any]): NCAsciiTable = {
        startRow()

        cells.asScala.foreach(p => addRowCell(p))

        endRow()

        this
    }

    /**
     * Adds header (one or more header cells).
     *
     * @param cells Header cells. For multi-line cells - use `Seq(...)`.
     */
    def #=(cells: Any*): NCAsciiTable = {
        cells foreach {
            case i: Iterable[_] => addHeaderCell(i.iterator.toSeq: _*)
            case a => addHeaderCell(a)
        }

        this
    }

    /**
     * Adds styled header (one or more header cells).
     *
     * @param cells Header cells tuples (style, text). For multi-line cells - use `Seq(...)`.
     */
    def #/(cells: (String, Any)*): NCAsciiTable = {
        cells foreach {
            case i if i._2.isInstanceOf[Iterable[_]] => addStyledHeaderCell(i._1, i._2.asInstanceOf[Iterable[_]].iterator.toSeq: _*)
            case a => addStyledHeaderCell(a._1, a._2)
        }

        this
    }

    /**
     * Adds headers.
     *
     * @param cells Header cells.
     */
    def addHeaders(cells: java.util.List[Any]): NCAsciiTable = {
        cells.asScala.foreach(addHeaderCell(_))

        this
    }

    /**
     * Adds headers with the given `style`.
     *
     * @param style Style top use.
     * @param cells Header cells.
     */
    def addStyledHeaders(style: String, cells: java.util.List[Any]): NCAsciiTable = {
        cells.asScala.foreach(addHeaderCell(style, _))

        this
    }

    // Handles the 'null' strings.
    private def x(s: Any): String = s match {
        case null => "<null>"
        case _ => s.toString
    }

    /**
     *
     * @param maxWidth
     * @param lines
     * @return
     */
    private def breakUpByNearestSpace(maxWidth: Int, lines: Seq[String]): Seq[String] =
        lines.flatMap(line => {
            if (line.isEmpty)
                mutable.Buffer("")
            else {
                val leader = line.indexWhere(_ != ' ') // Number of leading spaces.

                val buf = mutable.Buffer.empty[String]

                var start = 0
                var lastSpace = -1
                var curr = 0
                val len = line.length

                def addLine(s: String): Unit =
                    buf += (if (buf.isEmpty) s else space(leader) + s)

                while (curr < len) {
                    if (curr - start > maxWidth) {
                        val end = if (lastSpace == -1) curr else lastSpace + 1 /* Keep space at the end of the line. */

                        addLine(line.substring(start, end))

                        start = end
                    }

                    if (line.charAt(curr) == ' ')
                        lastSpace = curr

                    curr += 1
                }

                if (start < len) {
                    val lastLine = line.substring(start)

                    if (lastLine.trim.nonEmpty) {
                        addLine(lastLine)
                    }
                }

                buf
            }
        })

    /**
     *
     * @param hdr
     * @param style
     * @param lines
     * @return
     */
    private def mkStyledCell(hdr: Boolean, style: String, lines: Any*): Cell = {
        val st = Style(style)
        var strLines = lines.map(x)

        if (hdr)
            strLines = strLines.map(s => s"$ansiBlueFg$s$ansiReset")

        Cell(
            st,
            if (breakUpByWords)
                breakUpByNearestSpace(st.maxWidth, strLines)
            else
                (for (str <- strLines) yield str.grouped(st.maxWidth)).flatten
        )
    }

    /**
     * Adds single header cell with the default style..
     *
     * @param lines One or more cell lines.
     */
    def addHeaderCell(lines: Any*): NCAsciiTable = {
        hdr :+= mkStyledCell(
            true,
            defaultHeaderStyle,
            lines: _*
        )

        this
    }

    /**
     * Adds single row cell with the default style.
     *
     * @param lines One or more row cells. Multiple lines will be printed on separate lines.
     */
    def addRowCell(lines: Any*): NCAsciiTable = {
        curRow :+= mkStyledCell(
            false,
            defaultRowStyle,
            lines: _*
        )

        this
    }

    /**
     * Adds single header cell with the default style..
     *
     * @param style Style to use.
     * @param lines One or more cell lines.
     */
    def addStyledHeaderCell(style: String, lines: Any*): NCAsciiTable = {
        hdr :+= mkStyledCell(
            hdr = true,
            if (style.trim.isEmpty) defaultHeaderStyle else style,
            lines: _*
        )

        this
    }

    /**
     * Adds single row cell with the default style.
     *
     * @param style Style to use.
     * @param lines One or more row cells. Multiple lines will be printed on separate lines.
     */
    def addStyledRowCell(style: String, lines: Any*): NCAsciiTable = {
        curRow :+= mkStyledCell(
            false,
            if (style.trim.isEmpty) defaultRowStyle else style,
            lines: _*
        )

        this
    }

    /**
     *
     * @param txt Text to align.
     * @param width Width already accounts for padding.
     * @param sty Style.
     */
    private def aligned(txt: String, width: Int, sty: Style): String = {
        val d = width - U.stripAnsi(txt).length

        sty.align match {
            case "center" => space(d / 2) + txt + space(d / 2 + d % 2)
            case "left" => space(sty.leftPad) + txt + space(d - sty.leftPad)
            case "right" => space(d - sty.rightPad) + txt + space(sty.rightPad)
            case _ => throw new AssertionError(s"Invalid align option: $sty")
        }
    }

    override def toString: String = mkString

    /**
     * Prepares output string.
     */
    private def mkString: String = {
        // Make sure table is not empty.
        if (hdr.isEmpty && rows.isEmpty)
            return ""

        var colsNum = -1

        val isHdr = hdr.nonEmpty

        if (isHdr)
            colsNum = hdr.size

        // Calc number of columns and make sure all rows are even.
        for (r <- rows)
            if (colsNum == -1)
                colsNum = r.size
            else if (colsNum != r.size)
                assert(assertion = false, "Table with uneven rows.")

        assert(colsNum > 0, "No columns found.")

        // At this point all rows in the table have the
        // the same number of columns.

        val colWs = new Array[Int](colsNum) // Column widths.
        val rowHs = new Array[Int](rows.length) // Row heights.

        // Header height.
        var hdrH = 0

        // Initialize column widths with header row (if any).
        for (i <- hdr.indices) {
            val c = hdr(i)

            colWs(i) = c.width

            hdrH = math.max(hdrH, c.height)
        }

        // Calculate row heights and column widths.
        for (i <- rows.indices; j <- 0 until colsNum) {
            val c = rows(i)(j)

            rowHs(i) = math.max(rowHs(i), c.height)
            colWs(j) = math.max(colWs(j), c.width)
        }

        // Table width without the border.
        val tableW = colWs.sum + colsNum - 1

        val tbl = new StringBuilder

        // Top margin.
        for (_ <- 0 until margin.top)
            tbl ++= " \n"

        /**
         *
         * @param crs
         * @param cor
         * @return
         */
        def mkAsciiLine(crs: String, cor: String): String =
            s"${space(margin.left)}$crs${dash(cor, tableW)}$crs${space(margin.right)}\n"

        // Print header, if any.
        if (isHdr) {
            tbl ++= mkAsciiLine(HDR_CRS, HDR_HOR)

            for (i <- 0 until hdrH) {
                // Left margin and '|'.
                tbl ++= s"${space(margin.left)}$HDR_VER"

                for (j <- hdr.indices) {
                    val c = hdr(j)

                    if (i >= 0 && i < c.height)
                        tbl ++= aligned(c.lines(i), colWs(j), c.style)
                    else
                        tbl ++= space(colWs(j))

                    tbl ++= s"$HDR_VER" // '|'
                }

                // Right margin.
                tbl ++= s"${space(margin.right)}\n"
            }

            tbl ++= mkAsciiLine(HDR_CRS, HDR_HOR)
        }
        else
            tbl ++= mkAsciiLine(ROW_CRS, ROW_HOR)

        // Print rows, if any.
        if (rows.nonEmpty) {
            val horLine = (i: Int) => {
                // Left margin and '+'
                tbl ++= s"${space(margin.left)}$ROW_CRS"

                for (k <- rows(i).indices)
                    tbl ++= s"${dash(ROW_HOR, colWs(k))}$ROW_CRS"

                // Right margin.
                tbl ++= s"${space(margin.right)}\n"
            }

            for (i <- rows.indices) {
                val r = rows(i)

                val rowH = rowHs(i)

                if (i > 0 && ((rowH > 1 && autoBorder) || insideBorder) && rowHs(i - 1) == 1)
                    horLine(i)

                for (j <- 0 until rowH) {
                    // Left margin and '|'
                    tbl ++= s"${space(margin.left)}$ROW_VER"

                    for (k <- r.indices) {
                        val c = r(k)
                        val w = colWs(k)

                        if (j < c.height)
                            tbl ++= aligned(c.lines(j), w, c.style)
                        else
                            tbl ++= space(w)

                        tbl ++= s"$ROW_VER" // '|'
                    }

                    // Right margin.
                    tbl ++= s"${space(margin.right)}\n"
                }

                if (i < rows.size - 1 && ((rowH > 1 && autoBorder) || insideBorder))
                    horLine(i)
            }

            tbl ++= s"${space(margin.left)}$ROW_CRS${dash(ROW_HOR, tableW)}$ROW_CRS${space(margin.right)}\n"
        }

        // Bottom margin.
        for (_ <- 1 to margin.bottom)
            tbl ++= s" \n"

        val res = tbl.toString

        res.substring(0, res.length - 1)
    }

    /**
     * Prepares table string representation for logger.
     * @param header Optional header.
     */
    private def mkLogString(header: Option[String] = None): String = s"${header.getOrElse("")}\n$mkString"

    /**
      * Renders this table to log as debug.
      *
      * @param log Logger.
      * @param header Optional header.
      */
    def debug(log: Logger, header: Option[String] = None): Unit = log.debug(mkLogString(header))

    /**
      * Renders this table to log as info.
      *
      * @param log Logger.
      * @param header Optional header.
      */
    def info(log: Logger, header: Option[String] = None): Unit = log.info(mkLogString(header))

    /**
      * Renders this table to log as warn.
      *
      * @param log Logger.
      * @param header Optional header.
      */
    def warn(log: Logger, header: Option[String] = None): Unit = log.warn(mkLogString(header))

    /**
      * Renders this table to log as error.
      *
      * @param log Logger.
      * @param header Optional header.
      */
    def error(log: Logger, header: Option[String] = None): Unit = log.error(mkLogString(header))

    /**
      * Renders this table to log as trace.
      *
      * @param log Logger.
      * @param header Optional header.
      */
    def trace(log: Logger, header: Option[String] = None): Unit = log.trace(mkLogString(header))

    /**
     * Renders this table to output stream.
     *
     * @param ps Output stream.
     */
    def render(ps: PrintStream = System.out): Unit = ps.println(mkString)

    /**
     * Renders this table to file.
     *
     * @param path File path.
     */
    def render(path: String): Unit = renderPrintStream(new PrintStream(path), path)

    /**
     * Renders this table to file.
     *
     * @param file File.
     */
    def render(file: java.io.File): Unit = renderPrintStream(new PrintStream(file), file.getAbsolutePath)


    private def renderPrintStream(f: => PrintStream, file: String): Unit =
        try
            Using.resource(f) { ps =>
                ps.print(mkString)
            }
        catch {
            case e: IOException => throw new NCE(s"Error writing file: $file", e)
        }
}

/**
 * Static context.
 */
object NCAsciiTable {
    // Default styles.
    private final val DFLT_ROW_STYLE = "align:left"
    private final val DFLT_HEADER_STYLE = "align:center"

    /**
     * Creates new ASCII text table with all defaults.
     *
     * @return Newly created ASCII table.
     */
    def apply() = new NCAsciiTable

    /**
     * Creates new ASCII table with given header cells.
     *
     * @param hdrs Header.
     * @return Newly created ASCII table.
     */
    def apply(hdrs: Any*): NCAsciiTable = new NCAsciiTable #= (hdrs: _*)

    /**
     * Creates new ASCII table with given headers and data.
     *
     * @param hdrs Headers.
     * @param data Table data (sequence of rows).
     * @return Newly created ASCII table.
     */
    def of(hdrs: Seq[Any], data: Seq[Seq[Any]]): NCAsciiTable = {
        val tbl = new NCAsciiTable #= (hdrs: _*)

        data.foreach(tbl += (_: _*))

        tbl
    }
}
