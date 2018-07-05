package ru.d10xa.jadd.xml

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
    lastUpdatedText
  }

}
