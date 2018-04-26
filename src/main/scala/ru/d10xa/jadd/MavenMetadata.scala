package ru.d10xa.jadd

import scala.xml.Elem

case class MavenMetadata(
  url: Option[String] = None,
  versions: Seq[String] = Seq.empty,
  lastUpdated: Option[String] = None
)

object MavenMetadata {
  def read(root: Elem): MavenMetadata = MavenMetadata(
    versions = MavenMetadataVersionsRawReader.versions(root),
    lastUpdated = MavenMetadataVersionsRawReader.lastUpdated(root)
  )
}
