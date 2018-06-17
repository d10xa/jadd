package ru.d10xa.jadd.shortcuts

import org.scalatest.FunSuite
import org.scalatest.Matchers

class ArtifactShortcutsTest extends FunSuite with Matchers {

  import ru.d10xa.jadd.Utils._

  val artifactShortcuts = new ArtifactShortcuts()
  val unshort: String => Option[String] = artifactShortcuts.unshort

  test("find full by shortcut") {
    unshort("junit") shouldBe Some("junit:junit")
  }

  test("find shortcut by full") {
    unshort("junit") shouldBe Some("junit:junit")
  }

  test("find unknown") {
    unshort("unknown") shouldBe None
  }

  test("sourceFromSpringUri") {

    // TODO add tests
    sourceFromSpringUri("classpath:jadd-shortcuts.csv")
      .mkString shouldNot be(empty)

    //    sourceFromSpringUri("https://github.com/d10xa/jadd/raw/master/src/main/resources/jadd-shortcuts.csv")
    //      .mkString shouldNot be(empty)
  }

}
