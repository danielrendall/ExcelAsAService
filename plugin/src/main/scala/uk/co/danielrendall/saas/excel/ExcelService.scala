package uk.co.danielrendall.saas.excel

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import uk.co.danielrendall.saas.excel.model.*
import uk.co.danielrendall.saas.excel.bridge.*
import uk.co.danielrendall.saas.interfaces.{ResponseFactory, ResponseHelpers, ServiceMetadata, ServiceSession, ServiceableSupport}

import java.io.{ByteArrayInputStream, File, FileOutputStream, OutputStream}
import java.time.{LocalDateTime, LocalTime}
import scala.util.{Failure, Try}
import scala.xml.XML
import scala.xml.Elem

class ExcelService
  extends ServiceableSupport:

  override def getMetadata: ServiceMetadata = ???

  override def post(session: ServiceSession, first: String, rest: List[String])
                   (implicit responseFactory: ResponseFactory): NanoHTTPD.Response =
    (for {
      byteArray <- Try(session.bodyAsBytes)
      _ <- Try(println("Got body bytes: " + byteArray.length))
      elem <- Try(XML.load(new ByteArrayInputStream(byteArray)))
      _ <- Try(println("Created element"))
      spreadsheet <- loadVersioned(elem)
    } yield {
      println("Got spreadsheet: " + spreadsheet)
      val os = new ByteArrayOutputStream()
      convertToExcel(spreadsheet, os)
      println("Converted to Excel")
      os.close()
      val bytes = os.toByteArray
      println("Got " + bytes.length + " bytes")
      responseFactory.newFixedLengthResponse(Status.OK,
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        new ByteArrayInputStream(bytes),
        bytes.length
      )
    }).toOption.getOrElse(badRequest("Couldn't convert"))

  def loadVersioned(elem: Elem): Try[Spreadsheet] = {
    println("Source element is " + elem.mkString)
    val version = elem.attribute("version").map(_.mkString).getOrElse("")
    if (version == "1") {
      import uk.co.danielrendall.saas.excel.generated.v1.*
      Try {
        V1(scalaxb.fromXML[RawSpreadsheet](elem))
      }
    } else {
      Failure(new Exception("Unsupported version: " + version))
    }
  }

  def convertToExcel(spreadsheet: Spreadsheet, os: OutputStream): Unit = {
    val wb = new XSSFWorkbook()
    spreadsheet.createInWorkbook(wb)
    wb.write(os)
  }

