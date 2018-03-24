package ru.d10xa.jadd.shortcuts

import scala.io.BufferedSource
import scala.io.Source

class ArtifactShortcuts(source: BufferedSource = Source.fromResource("jadd-shortcuts.csv")) {

  lazy val shortcuts: Map[String, String] =
    source
      .getLines()
      .map(_.split(','))
      .map {
        case Array(short, full) => (short, full)
      }
      .toMap

  def unshort(rawArtifact: String): Option[String] =
    shortcuts.get(rawArtifact)

}
