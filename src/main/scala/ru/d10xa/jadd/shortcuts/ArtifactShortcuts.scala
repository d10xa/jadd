package ru.d10xa.jadd.shortcuts

import scala.io.Source

class ArtifactShortcuts(source: Source) {

  lazy val shortcuts: Map[String, String] = {
    val lines = source
      .getLines()
      .toSeq
    val linesWithoutHeader =
      if (lines.head == "shortcut,artifact") lines.tail else lines
    linesWithoutHeader
      .map(_.split(','))
      .map {
        case Array(short, full) => (short, full)
      }
      .toMap
  }

  def unshort(rawArtifact: String): Option[String] =
    shortcuts.get(rawArtifact)

}

object ArtifactShortcuts {
  object ArtifactShortcutsClasspath
      extends ArtifactShortcuts(Source.fromResource("jadd-shortcuts.csv"))
}
