package ru.d10xa.jadd.shortcuts

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

  test("unshort mavenCentral") {
    unshortRepository("mavenCentral") shouldEqual "http://central.maven.org/maven2"
  }

  test("unshort jcenter") {
    unshortRepository("jcenter") shouldEqual "https://jcenter.bintray.com"
  }

  test("unshort google") {
    unshortRepository("google") shouldEqual "https://dl.google.com/dl/android/maven2"
  }

  test("as is") {
    unshortRepository("https://dl.bintray.com/groovy/maven") shouldEqual "https://dl.bintray.com/groovy/maven"
  }

}
