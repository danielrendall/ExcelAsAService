package uk.co.danielrendall.saas.excel.model

import org.apache.poi.xssf.usermodel.{XSSFCellStyle, XSSFCreationHelper, XSSFDrawing}

case class ExcelHelper(creationHelper: XSSFCreationHelper,
                       drawingPatriach: XSSFDrawing,
                       styles: CellStyleRegistry)

case class CellStyleRegistry(dateTimeStyle: XSSFCellStyle,
                             dateStyle: XSSFCellStyle)
