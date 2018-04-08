package ru.d10xa.jadd.inserts

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.shortcuts.ArtifactInfoFinder

import scala.io.Source

class ArtifactInfoFinderTest extends FunSuite with Matchers {

  import ru.d10xa.jadd.Utils._

  val finder = new ArtifactInfoFinder(Source.fromURL("https://github.com/d10xa/jadd/raw/master/src/main/resources/jadd-shortcuts.csv"))

  test("find full by shortcut") {
    finder
      .unshort("junit") shouldBe Some("junit:junit")
  }

  test("find shortcut by full") {
    finder
      .unshort("junit") shouldBe Some("junit:junit")
  }

  test("find unknown") {
    finder
      .unshort("unknown") shouldBe None
  }

  test("a"){

    // TODO add tests
    sourceFromSpringUri("classpath:jadd-repositories.csv").mkString
    sourceFromSpringUri("https://github.com/d10xa/jadd/raw/master/src/main/resources/jadd-shortcuts.csv").mkString
  }

}
