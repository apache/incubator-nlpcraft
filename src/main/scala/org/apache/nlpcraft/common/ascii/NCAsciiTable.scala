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
import resource._

import scala.collection.JavaConverters._

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
                for (e ← sty.split(',')) {
                    val a = e.split(":")

                    assume(a.length == 2, s"Invalid cell style: ${e.trim}")

                    val a0 = a(0).trim
                    val a1 = a(1).trim

                    a0 match {
                        case "leftPad" ⇒ cs.leftPad = a1.toInt
                        case "rightPad" ⇒ cs.rightPad = a1.toInt
                        case "align" ⇒ cs.align = a1
                        case _ ⇒ assert(assertion = false, s"Invalid style: ${e.trim}")
                    }
                }
            }

            cs
        }
    }

    /**
     * Cell holder.
     */
    private sealed case class Cell(style: Style, lines: Seq[String]) {
        // Cell's calculated width including padding.
        lazy val width: Int =
            if (height > 0)
                style.padding + lines.max(Ordering.by[String, Int](_.length)).length
            else
                style.padding

        // Gets height of the cell.
        def height: Int = lines.length
    }

    /**
     * Margin holder.
     */
    private sealed case class Margin(
        top: Int = 0,
        right: Int = 0,
        bottom: Int = 0,
        left: Int = 0) {
    }

    // Table drawing symbols.
    private val HDR_HOR = "="
    private val HDR_VER = "|"
    private val HDR_CRS = "+"
    private val ROW_HOR = "-"
    private val ROW_VER = "|"
    private val ROW_CRS = "+"

    // Headers & rows.
    private var hdr = IndexedSeq.empty[Cell]
    private var rows = IndexedSeq.empty[IndexedSeq[Cell]]

    // Current row, if any.
    private var curRow: IndexedSeq[Cell] = _

    // Table's margin, if any.
    private var margin = Margin()

    /**
     * Flag indicating whether or not to draw inside horizontal lines
     * between individual rows.
     */
    var insideBorder = false

    /**
     * Flag indicating whether of not to automatically draw horizontal lines
     * for multiline rows.
     */
    var autoBorder = true

    /**
     * Maximum width of the cell. If any line in the cell exceeds this width
     * it will be cut in two or more lines.
     *
     * '''NOTE''': it doesn't include into account the padding. Only the actual
     * string length is counted.
     */
    var maxCellWidth: Int = Int.MaxValue

    /** Row style. */
    var rowStyle: String = DFLT_ROW_STYLE

    /** Header style. */
    var headerStyle: String = DFLT_HEADER_STYLE

    // Dash drawing.
    private def dash(ch: String, len: Int): String = (for (_ ← 1 to len) yield ch).mkString("")
    private def space(len: Int): String = dash(" ", len)

    /**
     * Sets table's margin.
     *
     * @param top Top margin.
     * @param right Right margin.
     * @param bottom Bottom margin.
     * @param left Left margin.
     */
    def margin(top: Int = 0, right: Int = 0, bottom: Int = 0, left: Int = 0) {
        margin = Margin(top, right, bottom, left)
    }

    /**
     * Starts data row.
     */
    def startRow() {
        curRow = IndexedSeq.empty[Cell]
    }

    /**
     * Ends data row.
     */
    def endRow() {
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
            case i: Iterable[_] ⇒ addRowCell(i.iterator.toSeq: _*)
            case a ⇒ addRowCell(a)
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

        cells.asScala.foreach(p ⇒ addRowCell(p))

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
            case i: Iterable[_] ⇒ addHeaderCell(i.iterator.toSeq: _*)
            case a ⇒ addHeaderCell(a)
        }

        this
    }

    /**
      * Adds headers.
      *
      * @param cells Header cells.
      */
    def addHeaders(cells: java.util.List[Any]): NCAsciiTable = {
        cells.asScala.foreach(p ⇒ addHeaderCell(p))

        this
    }

    // Handles the 'null' strings.
    private def x(s: Any): String = s match {
        case null ⇒ "<null>"
        case _ ⇒ s.toString
    }

    /**
     * Adds single header cell.
     *
     * @param lines One or more cell lines.
     */
    def addHeaderCell(lines: Any*): NCAsciiTable = {
        hdr :+= Cell(Style(headerStyle), (for (line ← lines) yield x(line).grouped(maxCellWidth)).flatten)

        this
    }

    /**
     * Adds single row cell.
     *
     * @param lines One or more row cells. Multiple lines will be printed on separate lines.
     */
    def addRowCell(lines: Any*): NCAsciiTable = {
        curRow :+= Cell(Style(rowStyle), (for (line ← lines) yield x(line).grouped(maxCellWidth)).flatten)

        this
    }

    /**
     *
     * @param txt Text to align.
     * @param width Width already accounts for padding.
     * @param sty Style.
     */
    private def aligned(txt: String, width: Int, sty: Style): String = {
        val d = width - txt.length

        sty.align.trim match {
            case "center" ⇒ space(d / 2) + txt + space(d / 2 + d % 2)
            case "left" ⇒ space(sty.leftPad) + txt + space(d - sty.leftPad)
            case "right" ⇒ space(d - sty.rightPad) + txt + space(sty.rightPad)
            case _ ⇒ throw new AssertionError(s"Invalid align option in: $sty")
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
        for (r ← rows)
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
        for (i ← hdr.indices) {
            val c = hdr(i)

            colWs(i) = c.width

            hdrH = math.max(hdrH, c.height)
        }

        // Calculate row heights and column widths.
        for (i ← rows.indices; j ← 0 until colsNum) {
            val c = rows(i)(j)

            rowHs(i) = math.max(rowHs(i), c.height)
            colWs(j) = math.max(colWs(j), c.width)
        }

        // Table width without the border.
        val tableW = colWs.sum + colsNum - 1

        val tbl = new StringBuilder

        // Top margin.
        for (_ ← 0 until margin.top)
            tbl ++= " \n"

        // Print header, if any.
        if (isHdr) {
            tbl ++= s"${space(margin.left)}$HDR_CRS${dash(HDR_HOR, tableW)}$HDR_CRS${space(margin.right)}\n"

            for (i ← 0 until hdrH) {
                // Left margin and '|'.
                tbl ++= s"${space(margin.left)}$HDR_VER"

                for (j ← hdr.indices) {
                    val c = hdr(j)

                    if (i >= 0 && i < c.height)
                        tbl ++= aligned(c.lines(i), colWs(j), c.style)
                    else
                        tbl ++= space(colWs(j))

                    tbl ++= HDR_VER // '|'
                }

                // Right margin.
                tbl ++= s"${space(margin.right)}\n"
            }

            tbl ++= s"${space(margin.left)}$HDR_CRS${dash(HDR_HOR, tableW)}$HDR_CRS${space(margin.right)}\n"
        }
        else
            tbl ++= s"${space(margin.left)}$ROW_CRS${dash(ROW_HOR, tableW)}$ROW_CRS${space(margin.right)}\n"

        // Print rows, if any.
        if (rows.nonEmpty) {
            val horLine = (i: Int) ⇒ {
                // Left margin and '+'
                tbl ++= s"${space(margin.left)}$ROW_CRS"

                for (k ← rows(i).indices)
                    tbl ++= s"${dash(ROW_HOR, colWs(k))}$ROW_CRS"

                // Right margin.
                tbl ++= s"${space(margin.right)}\n"
            }

            for (i ← rows.indices) {
                val r = rows(i)

                val rowH = rowHs(i)

                if (i > 0 && ((rowH > 1 && autoBorder) || insideBorder) && rowHs(i - 1) == 1)
                    horLine(i)

                for (j ← 0 until rowH) {
                    // Left margin and '|'
                    tbl ++= s"${space(margin.left)}$ROW_VER"

                    for (k ← r.indices) {
                        val c = r(k)
                        val w = colWs(k)

                        if (j < c.height)
                            tbl ++= aligned(c.lines(j), w, c.style)
                        else
                            tbl ++= space(w)

                        tbl ++= ROW_VER // '|'
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
        for (_ ← 1 to margin.bottom)
            tbl ++= s" \n"

        tbl.toString()
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
    def render(path: String): Unit =
        try
            managed(new PrintStream(path)) acquireAndGet { ps ⇒
                ps.print(mkString)
            }
        catch {
            case e: IOException ⇒ throw new NCE(s"Error writing file: $path", e)
        }

    /**
     * Renders this table to file.
     *
     * @param file File.
     */
    def render(file: java.io.File): Unit =
        try
            managed(new PrintStream(file)) acquireAndGet { ps ⇒
                ps.print(mkString)
            }
        catch {
            case e: IOException ⇒ throw new NCE(s"Error writing file: $file", e)
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
     */
    def apply() = new NCAsciiTable

    /**
     * Creates new ASCII table with given header cells.
     *
     * @param cells Header.
     */
    def apply(cells: Any*): NCAsciiTable = new NCAsciiTable #= (cells: _*)
}
