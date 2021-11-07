package uk.co.danielrendall.saas.excel.model

import org.apache.poi.ss.usermodel.Comment
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFCellStyle, XSSFComment, XSSFCreationHelper, XSSFRow, XSSFSheet, XSSFWorkbook}
import org.apache.poi.ss.util.WorkbookUtil

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, LocalTime}

case class Spreadsheet(name: String, tabs: Seq[Tab]):

  def createInWorkbook(wb: XSSFWorkbook): Unit = {
    implicit val creationHelper: XSSFCreationHelper = wb.getCreationHelper

    val dateTimeStyle: XSSFCellStyle = {
      val style = wb.createCellStyle
      style.setDataFormat(creationHelper.createDataFormat.getFormat("yyyy/mm/dd hh:mm:ss"))
      style
    }

    val dateStyle: XSSFCellStyle = {
      val style = wb.createCellStyle
      style.setDataFormat(creationHelper.createDataFormat.getFormat("yyyy/mm/dd"))
      style
    }

    implicit val cellStyleRegistry = CellStyleRegistry(dateTimeStyle, dateStyle)

    tabs.foreach(_.addToWorkbook(wb))
  }


case class Tab(name: String, headerOpt: Option[RowContainer], body: RowContainer):

  def addToWorkbook(wb: XSSFWorkbook)
                   (implicit creationHelper: XSSFCreationHelper,
                    cellStyleRegistry: CellStyleRegistry): Unit = {
    val sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(name))
      implicit val helper = new ExcelHelper(creationHelper, sheet.createDrawingPatriarch(), cellStyleRegistry)
      headerOpt.foreach { header =>
      header.addToSheet(sheet)
      sheet.createFreezePane(0, header.size)
    }
    body.addToSheet(sheet)
  }


case class RowContainer(rows: Seq[Row]) {
  def size = rows.size

  def addToSheet(sheet: XSSFSheet)
                (implicit helper: ExcelHelper): Unit = {
    rows.foreach { row =>
      row.addToRow(sheet.createRow(sheet.getLastRowNum + 1))
    }
  }
}

case class Row(cells: Seq[Cell]) {

  def addToRow(excelRow: XSSFRow)
              (implicit helper: ExcelHelper): Unit = {
    cells.zipWithIndex.foreach { case (cell, index) =>
      cell.addToCell(excelRow.createCell(index))
    }
  }


}

sealed trait Cell:
  def addToCell(excelCell: XSSFCell)
               (implicit helper: ExcelHelper): Unit

case class StringCell(s: String) extends Cell:
  override def addToCell(excelCell: XSSFCell)
                        (implicit helper: ExcelHelper): Unit = excelCell.setCellValue(s)

case class IntCell(i: BigInt) extends Cell:
  // TODO
  override def addToCell(excelCell: XSSFCell)
                        (implicit helper: ExcelHelper): Unit = excelCell.setCellValue(i.toDouble)


case class BooleanCell(b: Boolean) extends Cell:
  override def addToCell(excelCell: XSSFCell)
                        (implicit helper: ExcelHelper): Unit = excelCell.setCellValue(b)

case class DateTimeCell(dt: LocalDateTime) extends Cell:
  override def addToCell(excelCell: XSSFCell)
                        (implicit helper: ExcelHelper): Unit = {
    excelCell.setCellStyle(helper.styles.dateTimeStyle)
    excelCell.setCellValue(dt)
  }

case class DateCell(d: LocalDate) extends Cell:
  override def addToCell(excelCell: XSSFCell)
                        (implicit helper: ExcelHelper): Unit = {
    excelCell.setCellStyle(helper.styles.dateStyle)
    excelCell.setCellValue(d)
  }

case class TimeCell(t: LocalTime) extends Cell:
  override def addToCell(excelCell: XSSFCell)
                        (implicit helper: ExcelHelper): Unit = excelCell.setCellValue(t.format(TimeCell.timeFormat))

object TimeCell {
  private val timeFormat: DateTimeFormatter = DateTimeFormatter.ISO_TIME
}

case class DecimalCell(d: BigDecimal) extends Cell:
  override def addToCell(excelCell: XSSFCell)
                        (implicit helper: ExcelHelper): Unit = excelCell.setCellValue(d.toDouble)

case class ErrorCell(s: String, expectedType: String) extends Cell:
  override def addToCell(excelCell: XSSFCell)
                        (implicit helper: ExcelHelper): Unit = {
    excelCell.setCellValue(s)
    val anchor = helper.creationHelper.createClientAnchor();
    anchor.setCol1(excelCell.getColumnIndex());
    anchor.setCol2(excelCell.getColumnIndex() + 1);
    anchor.setRow1(excelCell.getRowIndex());
    anchor.setRow2(excelCell.getRowIndex() + 3);

    // Create the comment and set the text+author
    val comment = helper.drawingPatriach.createCellComment(anchor);
    val commentText = helper.creationHelper.createRichTextString("Should have been a " + expectedType);
    comment.setString(commentText);
    excelCell.setCellComment(comment)
  }

