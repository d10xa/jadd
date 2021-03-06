package ru.d10xa.jadd.shortcuts

import ru.d10xa.jadd.testkit.TestBase

import scala.io.Source

class ArtifactShortcutsCsvFileTest extends TestBase {

  val lines: Vector[String] =
    Source.fromResource("jadd-shortcuts.csv").getLines().toVector
  lazy val shortcuts: Vector[String] = lines.tail.map(_.split(",").head)
  lazy val groupAndArtifactIds: Vector[String] =
    lines.tail.map(_.split(",").last)

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

  import ArtifactShortcutsCsvFileTest.SeqDuplicatesImplicits

  test("shortcuts unique") {
    val duplicates = shortcuts.duplicates()
    duplicates.map(d => s"duplicate: $d").foreach(println)
    duplicates.size shouldEqual 0
  }

  test("artifacts unique") {
    groupAndArtifactIds.size shouldEqual groupAndArtifactIds.distinct.size
  }

  test("lines sorted by shortcut") {
    shortcuts.sorted shouldEqual shortcuts
  }

}

object ArtifactShortcutsCsvFileTest {
  implicit class SeqDuplicatesImplicits[T](val seq: Seq[T]) extends AnyVal {
    def duplicates(): Iterable[T] =
      seq
        .groupBy(identity)
        .collect { case (x, xs) if xs.lengthCompare(1) > 0 => x }
  }
}
