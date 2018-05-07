package ru.d10xa.jadd

import org.scalatest.FunSuite
import org.scalatest.Matchers
import ru.d10xa.jadd.shortcuts.RepositoryShortcutsImpl.unshortRepository

class UnshortRepositoryTest extends FunSuite with Matchers {

  test("unshort bintray") {
    unshortRepository("bintray/groovy/maven") shouldEqual "https://dl.bintray.com/groovy/maven"
  }

  test("unshort sonatype") {
    unshortRepository("sonatype/snapshots") shouldEqual "https://oss.sonatype.org/content/repositories/snapshots"
  }

  test("as is") {
    unshortRepository("https://dl.bintray.com/groovy/maven") shouldEqual "https://dl.bintray.com/groovy/maven"
  }

}
