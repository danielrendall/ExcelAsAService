package uk.co.danielrendall.saas.excel.bridge

import uk.co.danielrendall.saas.excel.generated.v1.*
import uk.co.danielrendall.saas.excel.model.{DateTimeCell, DecimalCell, StringCell, *}

import java.time.{LocalDate, LocalDateTime, LocalTime}
import scala.util.control.NonFatal

object V1:

  def apply(rawSpreadsheet: RawSpreadsheet): Spreadsheet =
    Spreadsheet(rawSpreadsheet.name, rawSpreadsheet.tab.map(makeTab))

  def makeTab(rawTab: RawTab): Tab =
    Tab(rawTab.name, rawTab.header.map(makeRowContainer), makeRowContainer(rawTab.body))

  def makeRowContainer(rawRowContainer: RawRowContainer): RowContainer =
    RowContainer(rawRowContainer.row.map(makeRow))

  def makeRow(rawRow: RawRow): Row =
    Row(rawRow.cell.map(makeCell))

  def makeCell(rawCell: RawCell): Cell =
    try {
      rawCell.typeValue match {
        case _@RawString => {
          rawCell.href match {
            case Some(href) =>
              HyperlinkCell(rawCell.value, href.toURL.toExternalForm)
            case None =>
              StringCell(rawCell.value)
          }
        }
        case _@RawInt =>
          IntCell(BigInt(rawCell.value))
        case _@RawBoolean =>
          BooleanCell(rawCell.value.toBoolean)
        case _@RawDateTime =>
          DateTimeCell(LocalDateTime.parse(rawCell.value))
        case _@RawDate =>
          DateCell(LocalDate.parse(rawCell.value))
        case _@RawTime =>
          TimeCell(LocalTime.parse(rawCell.value))
        case _@RawDecimal =>
          DecimalCell(BigDecimal(rawCell.value))
      }
    } catch {
      case NonFatal(e) =>
        ErrorCell(rawCell.value, s"Expected type ${rawCell.typeValue.toString} - error was ${e.getMessage}")
    }
