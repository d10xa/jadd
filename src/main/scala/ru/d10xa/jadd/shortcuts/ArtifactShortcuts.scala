package ru.d10xa.jadd.shortcuts

import ru.d10xa.jadd.ArtifactWithoutVersion

import scala.io.BufferedSource
import scala.io.Source

class ArtifactShortcuts(source: BufferedSource = Source.fromResource("jadd-shortcuts.csv")) {

  lazy val shortcuts: Map[String, ArtifactWithoutVersion] =
    source
      .getLines()
      .map(shortcutLineToArtifact)
      .toMap

  def shortcutLineToArtifact(line: String): (String, ArtifactWithoutVersion) = {
    line.split(',') match {
      case Array(short, full) =>
        val Array(a, b) = full.split(":")
        short -> ArtifactWithoutVersion(a, b)
      case wrongArray => throw new IllegalArgumentException(s"wrong array $wrongArray")
    }
  }

}
