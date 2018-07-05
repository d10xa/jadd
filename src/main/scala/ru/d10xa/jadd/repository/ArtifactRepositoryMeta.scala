package ru.d10xa.jadd.repository

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import ru.d10xa.jadd.xml.MavenMetadataVersionsRawReader

import scala.util.Try
import scala.xml.Elem

sealed trait ArtifactRepositoryMeta

sealed case class MavenMetadata(
  url: Option[String] = None,
  repository: Option[String] = None,
  versions: Seq[String] = Seq.empty,
  lastUpdated: Option[String] = None
) extends ArtifactRepositoryMeta {
  lazy val lastUpdatedPretty: Option[String] =
    lastUpdated.map(MavenMetadata.lastUpdatedPretty)
}

object MavenMetadata {
  def readFromXml(from: MavenMetadata, root: Elem): MavenMetadata = from.copy(
    versions = MavenMetadataVersionsRawReader.versions(root),
    lastUpdated = MavenMetadataVersionsRawReader.lastUpdated(root)
  )
  def lastUpdatedPretty(lastUpdated: String): String = {
    val formatIn = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    val formatOut = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    def prettyPrintDateFormat(dateStr: String): String =
      Try(LocalDateTime.parse(dateStr, formatIn).format(formatOut))
        .getOrElse(dateStr)
    prettyPrintDateFormat(lastUpdated)
  }
}
