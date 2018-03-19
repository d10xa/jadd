package ru.d10xa.jadd

import scala.xml.Elem
import scala.xml.XML

object MavenMetadataVersionsRawReader {

  implicit val mavenMetadataVersionsRawReaderImplicit: VersionsRawReader[Elem] =
    VersionsRawReader[Elem] { root: Elem =>
      versionsDesc(root).toList
    }

  def versionsDesc(root: Elem): Seq[String] = {
    require(root.label == "metadata")
    (for {
      versioning <- root \ "versioning"
      versions <- versioning \ "versions"
      v <- versions \\ "version"
      version = v.text
    } yield version).reverse
  }

  def xmlContentToVersionsDesc(xml: String): Seq[String] = versionsDesc(XML.loadString(xml))

}
