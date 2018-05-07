package ru.d10xa.jadd

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.util.Try
import scala.xml.Elem

object MavenMetadataVersionsRawReader {

  def versions(root: Elem): Seq[String] = {
    require(root.label == "metadata")
    for {
      versioning <- root \ "versioning"
      versions <- versioning \ "versions"
      v <- versions \\ "version"
      version = v.text
    } yield version
  }

  def lastUpdated(root: Elem): Option[String] = {
    require(root.label == "metadata")
    val lastUpdatedText = (for {
      versioning <- root \ "versioning"
      lastUpdated <- versioning \ "lastUpdated"
      text = lastUpdated.text
    } yield text).headOption
    val formatIn = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    val formatOut = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    def prettyPrintDateFormat(dateStr: String): String =
      Try(LocalDateTime.parse(dateStr, formatIn).format(formatOut))
        .getOrElse(dateStr)
    lastUpdatedText.map(prettyPrintDateFormat)
  }

}
