package ru.d10xa.jadd.shortcuts

import org.scalatest.FunSuite
import org.scalatest.Matchers

import scala.io.Source

class ArtifactShortcutsCsvFileTest extends FunSuite with Matchers {

  val lines: Vector[String] = Source.fromResource("jadd-shortcuts.csv").getLines().toVector
  lazy val shortcuts: Vector[String] = lines.tail.map(_.split(",").head)
  lazy val groupAndArtifactIds: Vector[String] = lines.tail.map(_.split(",").last)

  test("csv has header") {
    // github has a pretty view for csv files and it requires header
    // https://github.com/d10xa/jadd/blob/master/src/main/resources/jadd-shortcuts.csv
    lines.head shouldEqual "shortcut,artifact"
  }

  test("must be at least 90 artifacts") {
    lines.tail.size should be >= 90
  }

  test("all lines has exactly one comma") {
    lines.map(_.count(_ == ',')).distinct shouldEqual Vector(1)
  }

  test("shourtcuts unique") {
    shortcuts.size shouldEqual shortcuts.distinct.size
  }

  test("artifacts unique") {
    groupAndArtifactIds.size shouldEqual groupAndArtifactIds.distinct.size
  }

  test("lines sorted by shortcut") {
    shortcuts.sorted shouldEqual shortcuts
  }

}
